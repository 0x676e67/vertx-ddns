<p align="center">
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-16+-green.svg" ></img>
	</a>
</p>

# vertx-ddns（还在开发中）（基础功能可用，日志处理暂未完成）

自动获得你的公网 IPv4 或 IPv6 地址或使用您的自定义IP地址，并解析到对应的域名服务。

<!-- TOC -->

- [vertx-ddns](#vertx-ddns)
  - [功能](#功能)
  - [系统中使用](#系统中使用)
  - [Docker中使用](#docker中使用)
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
- 网页中方便快速查看最近50条日志，不需要手动查看运行日志中查看

## Docker中使用

- 使用host模式, 自行添加--net=host参数，并且不需要再做端口映射（同时支持IPv4/IPv6）
- 不挂载主机目录, 删除容器同时会删除配置
- 在浏览器中打开`http://主机IP:8081`，修改你的配置，成功
  ```bash
  # 拉取镜像运行，并随系统重启
  docker run -d -p 8081:8080 --name vertx-ddns --restart=always zf1976/vertx-ddns
  ```

- [可选] 挂载主机目录, 删除容器后配置不会丢失。可替换 `/root/.vertx_ddns` 默认用户权限root, 配置文件为隐藏文件
  ```bash
  docker run -d -p 8081:8080 --name vertx-ddns --restart=always -v /your_path:/root/.vertx_ddns zf1976/vertx-ddns
  ```
  
## 注
- 默认登录的用户名密码：vertx
<img src="./img/1BC05189-0624-4729-B900-2CE4A42177B1.png"/>
