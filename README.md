<p align="center">
	<a target="_blank" href="https://github.com/zf1976/vertx-ddns/blob/main/LICENSE">
		<img src="https://img.shields.io/badge/license-MIT-blue.svg" ></img>
	</a>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-16+-green.svg" ></img>
	</a>
</p>

# vertx-ddns
自动获取设备公网 `IPv4` 或 `IPv6` 地址或使用自定义的`IP`地址，并解析到对应的域名服务。

- [vertx-ddns](#vertx-ddns)
  - [功能](#功能)
  - [系统中使用](#系统中使用)
  - [Docker中使用](#Docker中使用)
  - [使用IPv6](#使用ipv6)
  - [界面](#界面)
  - [开发&自行编译](#开发自行编译)

<!-- /TOC -->

## 功能

- 支持Mac、Windows、Linux系统，支持ARM、x86架构
- 支持的域名服务商 `Alidns(阿里云)` `Dnspod(腾讯云)` `Cloudflare` `华为云`
- 支持接口/网卡获取IP
- 支持以服务的方式运行
- 默认间隔5分钟同步一次
- 支持同时使用多个服务商解析（希望使用多个域名解析到您的IP）
- 支持多个域名同时解析，公司必备
- 支持多级域名
- 支持域名自定义指向IP
- 支持查询服务商域名解析记录  
- 网页中配置，可设置 `登录用户名和密码` / `禁止从公网访问`
- 支持Webhook，提供模版变量自定义消息内容
- 支持24小时实时解析日志监控

## Docker中使用

> docker镜像提供了`ubuntu --- OpenJ9-16`,`debian:buster-slim --- OpenJ9-16`，`alpine --- OpenJDK-16`，三种基础镜像系统所对应`JRE Runtime`的程序镜像，
> 其中`debian`,`alpine`为基础的镜像经过`jlink`生成的极简`JRE Runtime`，大大减少了镜像体积， 使用OpenJ9有效减少运行内存占用。
> 三种镜像大小`alpine` < `debian` < `ubuntu`。

- 使用host模式, 自行添加--net=host参数，并且不需要再做端口映射（同时支持IPv4/IPv6）
- 如果不挂载主机目录, 删除容器同时会删除配置
- 在浏览器中打开`http://主机IP:8081`，修改你的配置，成功
  ```bash
  # 拉取镜像运行，并随系统重启
  docker run -d -p 8081:8080 --name vertx-ddns --restart=always zf1976/vertx-ddns:alpine
  ```

- [可选] 挂载主机目录, 删除容器后配置不会丢失。可替换 `/root/.vertx_ddns` 默认用户权限root, 配置文件为隐藏文件
  ```bash
  docker run -d -p 8081:8080 --name vertx-ddns --restart=always -v /your_path:/root/.vertx_ddns zf1976/vertx-ddns:alpine
  ```
  
## 注
- 默认登录的用户名密码：**vertx**
- Windows、macOS系统下Docker不支持Docker的host模式
<img src="./img/img.png"/>

## License

[MIT License](https://raw.githubusercontent.com/zf1976/vertx-ddns/main/LICENSE)

## 贡献

目前只有`我`自己在维护这个项目。希望能有更多人加入 :)

感谢[Jetbrains](https://www.jetbrains.com/?from=mayi)制作的IDE，以及免费的开源许可证。

![](./img/jetbraint.png)
