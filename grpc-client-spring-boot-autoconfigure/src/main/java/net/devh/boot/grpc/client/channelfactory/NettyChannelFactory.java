/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.devh.boot.grpc.client.channelfactory;

import static java.util.Objects.requireNonNull;
import static net.devh.boot.grpc.common.util.GrpcUtils.DOMAIN_SOCKET_ADDRESS_SCHEME;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.core.io.Resource;

import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.ssl.SslContextBuilder;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;
import net.devh.boot.grpc.client.config.GrpcChannelProperties.Security;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;
import net.devh.boot.grpc.client.config.NegotiationType;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;
import net.devh.boot.grpc.common.security.KeyStoreUtils;
import net.devh.boot.grpc.common.util.GrpcUtils;

/**
 * This channel factory creates and manages netty based {@link GrpcChannelFactory}s.
 *
 * <p>
 * This class utilizes connection pooling and thus needs to be {@link #close() closed} after usage.
 * </p>
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
// Keep this file in sync with ShadedNettyChannelFactory
public class NettyChannelFactory extends AbstractChannelFactory<NettyChannelBuilder> {

    /**
     * Creates a new GrpcChannelFactory for netty with the given options.
     *
     * @param properties The properties for the channels to create.
     * @param globalClientInterceptorRegistry The interceptor registry to use.
     * @param channelConfigurers The channel configurers to use. Can be empty.
     */
    public NettyChannelFactory(final GrpcChannelsProperties properties,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
            final List<GrpcChannelConfigurer> channelConfigurers) {
        super(properties, globalClientInterceptorRegistry, channelConfigurers);
    }

    @Override
    protected NettyChannelBuilder newChannelBuilder(final String name) {
        final GrpcChannelProperties properties = getPropertiesFor(name);
        URI address = properties.getAddress();
        if (address == null) {
            String defaultScheme = getDefaultScheme();
            if (defaultScheme != null) {
                address = URI.create(defaultScheme + name);
            } else {
                address = URI.create(name);
            }
        }
        if (DOMAIN_SOCKET_ADDRESS_SCHEME.equals(address.getScheme())) {
            final String path = GrpcUtils.extractDomainSocketAddressPath(address.toString());
            return NettyChannelBuilder.forAddress(new DomainSocketAddress(path))
                    .channelType(EpollDomainSocketChannel.class)
                    .eventLoopGroup(new EpollEventLoopGroup());
        } else {
            return NettyChannelBuilder.forTarget(address.toString())
                    .defaultLoadBalancingPolicy(properties.getDefaultLoadBalancingPolicy());
        }
    }

    @Override
    // Keep this in sync with ShadedNettyChannelFactory#configureSecurity
    protected void configureSecurity(final NettyChannelBuilder builder, final String name) {
        final GrpcChannelProperties properties = getPropertiesFor(name);

        final NegotiationType negotiationType = properties.getNegotiationType();
        builder.negotiationType(of(negotiationType));

        if (negotiationType == NegotiationType.TLS) {
            final Security security = properties.getSecurity();

            final String authorityOverwrite = security.getAuthorityOverride();
            if (authorityOverwrite != null && !authorityOverwrite.isEmpty()) {
                builder.overrideAuthority(authorityOverwrite);
            }

            final SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient();
            configureProvidedClientCertificate(security, sslContextBuilder);
            configureAcceptedServerCertificates(security, sslContextBuilder);

            if (security.getCiphers() != null && !security.getCiphers().isEmpty()) {
                sslContextBuilder.ciphers(security.getCiphers());
            }

            if (security.getProtocols() != null && security.getProtocols().length > 0) {
                sslContextBuilder.protocols(security.getProtocols());
            }

            try {
                builder.sslContext(sslContextBuilder.build());
            } catch (final SSLException e) {
                throw new IllegalStateException("Failed to create ssl context for grpc client", e);
            }
        }
    }

    /**
     * Configures the client certificate provided by the ssl context.
     *
     * @param security The security configuration to use.
     * @param sslContextBuilder The ssl context builder to configure.
     */
    // Keep this in sync with ShadedNettyChannelFactory#configureProvidedClientCertificate
    protected static void configureProvidedClientCertificate(final Security security,
            final SslContextBuilder sslContextBuilder) {
        if (security.isClientAuthEnabled()) {
            try {
                final Resource privateKey = security.getPrivateKey();
                final Resource keyStore = security.getKeyStore();

                if (privateKey != null) {
                    final Resource certificateChain =
                            requireNonNull(security.getCertificateChain(), "certificateChain");
                    final String privateKeyPassword = security.getPrivateKeyPassword();
                    try (InputStream certificateChainStream = certificateChain.getInputStream();
                            InputStream privateKeyStream = privateKey.getInputStream()) {
                        sslContextBuilder.keyManager(certificateChainStream, privateKeyStream, privateKeyPassword);
                    }

                } else if (keyStore != null) {
                    final KeyManagerFactory keyManagerFactory = KeyStoreUtils.loadKeyManagerFactory(
                            security.getKeyStoreFormat(), keyStore, security.getKeyStorePassword());
                    sslContextBuilder.keyManager(keyManagerFactory);

                } else {
                    throw new IllegalStateException("Neither privateKey nor keyStore configured");
                }
            } catch (final Exception e) {
                throw new IllegalArgumentException("Failed to create SSLContext (PK/Cert)", e);
            }
        }
    }

    /**
     * Configures the server certificates accepted by the ssl context.
     *
     * @param security The security configuration to use.
     * @param sslContextBuilder The ssl context builder to configure.
     */
    // Keep this in sync with ShadedNettyChannelFactory#configureAcceptedServerCertificates
    protected static void configureAcceptedServerCertificates(final Security security,
            final SslContextBuilder sslContextBuilder) {
        try {
            final Resource trustCertCollection = security.getTrustCertCollection();
            final Resource trustStore = security.getTrustStore();

            if (trustCertCollection != null) {
                try (InputStream trustCertCollectionStream = trustCertCollection.getInputStream()) {
                    sslContextBuilder.trustManager(trustCertCollectionStream);
                }

            } else if (trustStore != null) {
                final TrustManagerFactory trustManagerFactory = KeyStoreUtils.loadTrustManagerFactory(
                        security.getTrustStoreFormat(), trustStore, security.getTrustStorePassword());
                sslContextBuilder.trustManager(trustManagerFactory);

            } else {
                // Use system default
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException("Failed to create SSLContext (TrustStore)", e);
        }
    }

    /**
     * Converts the given negotiation type to netty's negotiation type.
     *
     * @param negotiationType The negotiation type to convert.
     * @return The converted negotiation type.
     */
    // Keep this in sync with ShadedNettyChannelFactory#of
    protected static io.grpc.netty.NegotiationType of(final NegotiationType negotiationType) {
        switch (negotiationType) {
            case PLAINTEXT:
                return io.grpc.netty.NegotiationType.PLAINTEXT;
            case PLAINTEXT_UPGRADE:
                return io.grpc.netty.NegotiationType.PLAINTEXT_UPGRADE;
            case TLS:
                return io.grpc.netty.NegotiationType.TLS;
            default:
                throw new IllegalArgumentException("Unsupported NegotiationType: " + negotiationType);
        }
    }

}
