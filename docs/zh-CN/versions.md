# 版本

[<- 返回索引](index.md)

本页面介绍版本策略和生命周期等信息。

## 目录 <!-- omit in toc -->

- [版本策略](#版本策略)
- [版本列表](#版本列表)
  - [2.x 版本](#2.x 版本)
  - [1.x 版本](#1.x 版本)
  - [升级依赖关系](#升级依赖关系)
  - [发布日志](#发布日志)

## 版本策略

这个项目的主要版本定义了我们与哪个Spring-boot版本兼容。

- 1.x.x 版本为 EOL，不会再收到任何更新。
- 2.x.x 是当前的维护版本，如果有 spring-boot 或者 gRPC 版本，将进行更新。

次版本定义了此项目的功能版本。 每次我们更改 spring boot 或 gRPC 版本时，我们也会增加我们的功能版本。 如果我们增加/改变主要特征，也是如此。 在大多数情况下，你不会通过升级获得任何不兼容之处，但因为gRPC 就像它的 API 一样， 这个问题不能被排除在外。 我们试图尽量减少这种影响，但不能排除这种影响。 如果您不使用高级功能，您通常不会收到通知。

我们通常不发布补丁版本，但在下次发布时包含这些补丁。 如果你需要一个修补过的版本，请新开一个 issue。

## 版本列表

下表显示了该项目和 spring boot 以及 gRPC 版本的关系。 在大多数情况下，你可以升级到较新的版本，但如果是 gRPC 改变了其 API。 请将任何问题报告给我们[仓库](https://github.com/yidongnan/grpc-spring-boot-starter/issues)。

> **注意**
>
> 如果您正在使用 non-shaded netty(和相关的库)，请 **严格** 保持这些版本跟 gRPC [文档](https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty) 一致。 (grpc-netty-shaded 通过保持这些版本同步来避免这些问题。)

### 2.x 版本

当前版本。

|     版本     | spring-boot | spring-cloud |  gRPC  |       日期 |
|:----------:|:-----------:|:------------:|:------:| --------:|
| 2.14.0\* |   2.6.11    |   2021.0.3   | 1.49.0 |       待定 |
|   2.13.1   |    2.5.8    |   2020.0.5   | 1.42.2 |  2022年1月 |
|   2.13.0   |    2.5.7    |   2020.0.4   | 1.42.1 | 2021年11月 |
|   2.12.0   |    2.4.5    |   2020.0.2   | 1.37.0 |  2021年5月 |
|   2.11.0   |    2.3.8    |    Hoxton    | 1.35.0 |  2021年2月 |
|   2.10.1   |    2.3.3    |    Hoxton    | 1.31.1 |  2020年8月 |
|   2.10.0   |    2.3.3    |    Hoxton    | 1.31.1 |  2020年8月 |
|   2.9.0    |    2.3.1    |    Hoxton    | 1.30.0 |  2020年6月 |
|   2.8.0    |    2.2.7    |    Hoxton    | 1.29.0 |  2020年6月 |
|   2.7.0    |    2.2.4    |    Hoxton    | 1.27.1 |  2020年2月 |
|   2.6.2    |    2.2.1    |    Hoxton    | 1.25.0 |  2020年1月 |
|   2.6.1    |    2.2.1    |    Hoxton    | 1.25.0 | 2019年11月 |
|   2.6.0    |    2.2.1    |    Hoxton    | 1.24.2 | 2019年11月 |
|   2.5.1    |    2.1.6    |  Greenwich   | 1.22.2 |  2018年8月 |
|   2.5.0    |    2.1.6    |  Greenwich   | 1.22.1 |  2018年8月 |
|   2.4.0    |    2.1.5    |   Finchley   | 1.20.0 |  2019年6月 |
|   2.3.0    |    2.1.4    |   Finchley   | 1.18.0 |  2019年4月 |
|   2.2.1    |    2.0.7    |      ?       | 1.17.1 |  2019年1月 |
|   2.2.0    |    2.0.6    |      ?       | 1.17.1 | 2018年12月 |
|   2.1.0    |    2.0.?    |      ?       | 1.14.0 | 2018年10月 |
|   2.0.1    |    2.0.?    |      ?       | 1.14.0 |  2018年8月 |
|   2.0.0    |    2.0.?    |      ?       | 1.13.1 |  2018年8月 |

(\* 未来的版本)

> 这里列出的依赖版本仅在构建过程中使用，可能被替换。 grpc-spring-boot-starter 通常也与较新的版本或较旧的版本兼容。 如果您遇到了与新版本不兼容的问题，请告诉我们，这样我们就可以更新这个项目！

### 1.x 版本

生命终结——没有计划的更新。

|  版本   | spring-boot |  gRPC  |      日期 |
|:-----:|:-----------:|:------:| -------:|
| 1.4.2 |    1.?.?    | 1.12.0 | 2019年6月 |
| 1.4.1 |    1.?.?    | 1.12.0 | 2018年6月 |
|  ...  |    1.?.?    |  N/A   |         |

### 升级依赖关系

如果你升级任何版本，我们强烈建议使用 bom 文件进行升级：

- [spring-boot](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-parent)
- [grpc-java](https://mvnrepository.com/artifact/io.grpc/grpc-bom)

### 发布日志

有关每个版本的更改，请参考发行说明。

- [grpc-spring-boot-starter](https://github.com/yidongnan/grpc-spring-boot-starter/releases)
- [spring-boot](https://github.com/spring-projects/spring-boot/releases)
- [grpc-java](https://github.com/grpc/grpc-java/releases)

---

[<- 返回索引](index.md)
