<p align="center">
	<a target="_blank" href="https://github.com/zf1976/vertx-ddns/blob/main/LICENSE">
		<img src="https://img.shields.io/badge/license-MIT-blue.svg" ></img>
	</a>
	<a href="https://github.com/zf1976/vertx-ddns/releases/latest">
		<img alt="GitHub release" src="https://img.shields.io/github/v/release/zf1976/vertx-ddns.svg?logo=github&style=flat-square">
	</a>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-16+-green.svg" ></img>
	</a>
	<a target="_blank" herf="https://github.com/zf1976/vertx-ddns/actions/workflows/release.yml">
		<img src="https://github.com/zf1976/vertx-ddns/actions/workflows/release.yml/badge.svg"/>
	</a>
	<a target="_blank" href="https://app.travis-ci.com/zf1976/vertx-ddns">
		<img src="https://app.travis-ci.com/zf1976/vertx-ddns.svg?branch=main"/>
	</a>
	<a target="_blank" href="https://hub.docker.com/repository/docker/zf1976/vertx-ddns">
		<img src="https://img.shields.io/docker/pulls/zf1976/vertx-ddns">
	</a>
</p>

## 简介
基于`Vert.x`实现异步非阻塞的动态DNS解析服务，自动获取设备公网 `IPv4` 或 `IPv6` 地址或使用自定义的`IP`地址，并解析到对应的域名服务。

- [vertx-ddns](#vertx-ddns)
  - [功能](#功能)
  - [系统中使用](#系统中使用)
  - [Docker中使用](#Docker中使用)
  - [Webhook](#Webhook)
  - [响应式布局](#界面)
  - [开发&自行编译](#开发自行编译)
  - [License](#License)

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
- 支持查询、自动创建、删除DNS服务商域名解析记录  
- 网页中配置，可设置 `登录用户名和密码` / `禁止从公网访问`
- 支持Webhook，提供模版变量自定义消息内容
- 支持24小时实时解析日志监控

## 系统中使用
> 这里只展示Linux系统安装部署，更多请查看[wiki](https://github.com/zf1976/vertx-ddns/wiki)
<details> <summary>环境要求</summary>
	
> 为了在使用过程中不出现意外的事故，给出下列推荐的配置
- Debian 10
- 512 MB 以上内存
</details>

<details> <summary>使用已构建的安装包</summary>
  
  > 无需安装Java运行环境，若存在运行环境也不影响
  >
  > <img alt="最新版本" src="https://img.shields.io/github/v/release/zf1976/vertx-ddns.svg?logo=github&style=flat-square">
  ```shell
  # 下载最新的安装包，{{version}} 为版本号，更多下载地址请访问 https://github.com/zf1976/vertx-ddns/releases
  wget https://github.com/zf1976/vertx-ddns/releases/download/{{version}}/ddns-runtime.zip
  
  # 没有梯子的话加速可以使用加速镜像
  wget https://github.91chifun.workers.dev/https://github.com//zf1976/vertx-ddns/releases/download/{{version}}/ddns-runtime.zip
  
  # 解压安装包
  unzip ddns-runtime.zip
	
  # 安装
  cd ddns-runtime
  sudo ./install.sh
  ```
</details>

<details> <summary>安装Java运行环境(Ubuntu/Debian)</summary>
	
  > 若已经存在 Java 运行环境的可略过这一步。
  ```shell
  # 导入 AdoptOpenJDK GPG key
  wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo apt-key add -
   
  # 导入 DEB Repository
  sudo add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
   
  # 若 terminal 提示 Command not found, 运行
  apt-get install -y software-properties-common
   
  # 安装目标 OpenJDK 版本
  sudo apt-get install adoptopenjdk-16-hotspot
  ```
  当然，这只是其中一种比较简单的安装方式，你也可以用其他方式，并不是强制要求使用这种方式安装。
- 运行vertx-ddns
  > vertx-ddns 的整个应用程序只有一个 Jar 包，且不包含用户的任何配置，它放在任何目录都是可行的。vertx-ddns 所有配置文件都存放在`~/.vertx_ddns`目录下。你完全不需要担心安装包的安危，它仅仅是个服务而已。	
  > 
  > <img alt="最新版本" src="https://img.shields.io/github/v/release/zf1976/vertx-ddns.svg?logo=github&style=flat-square">
  ```shell
  # 下载最新的Jar包，{{version}} 为版本号，更多下载地址请访问 https://github.com/zf1976/vertx-ddns/releases
  wget https://github.com/zf1976/vertx-ddns/releases/download/{{version}}/vertx-ddns.jar -O vertx-ddns-latest.jar
  
  # 没有梯子的话加速可以使用加速镜像
  wget https://github.91chifun.workers.dev/https://github.com//zf1976/vertx-ddns/releases/download/{{version}}/vertx-ddns.jar -O vertx-ddns-latest.jar
  
  # 启动测试
  java -jar vertx-ddns-latest.jar
  # 默认使用8080端口，如果需要更换端口
  java -jar vertx-ddns-latest.jar 8888
  ```
  
  如看到以下日志输出，则代表启动成功.
  ```shell
  2021-09-15 11:45:17.656 [vert.x-eventloop-thread-2] INFO  [AbstractWebServerVerticle] - Initialize project working directory：/Users/ant/.vertx_ddns
  2021-09-15 11:45:17.658 [vert.x-eventloop-thread-2] INFO  [AbstractWebServerVerticle] - Initialize DNS configuration file：/Users/ant/.vertx_ddns/dns_config.json
  2021-09-15 11:45:17.659 [vert.x-eventloop-thread-2] INFO  [AbstractWebServerVerticle] - Initialize secure configuration file：/Users/ant/.vertx_ddns/secure_config.json
  2021-09-15 11:45:17.659 [vert.x-eventloop-thread-2] INFO  [AbstractWebServerVerticle] - Initialize webhook configuration file：/Users/ant/.vertx_ddns/webhook_config.json
  2021-09-15 11:45:17.659 [vert.x-eventloop-thread-2] INFO  [AbstractWebServerVerticle] - Initialize rsa key configuration file：/Users/ant/.vertx_ddns/rsa_key.json
  2021-09-15 11:45:17.659 [vert.x-eventloop-thread-2] INFO  [AbstractWebServerVerticle] - Initialize aes key configuration file：/Users/ant/.vertx_ddns/aes_key.json
  2021-09-15 11:45:17.660 [vert.x-eventloop-thread-2] INFO  [AbstractWebServerVerticle] - RSA key has been initialized
  2021-09-15 11:45:17.660 [vert.x-eventloop-thread-2] INFO  [AbstractWebServerVerticle] - AES key has been initialized
  2021-09-15 11:45:17.763 [vert.x-eventloop-thread-2] INFO  [WebServerVerticle] - Vertx web server initialized with port(s):8080(http)
  2021-09-15 11:45:17.764 [vert.x-eventloop-thread-2] INFO  [WebServerVerticle] - Vertx-DDNS is running at http://localhost:8080
  2021-09-15 11:45:17.786 [vert.x-eventloop-thread-2] INFO  [WebServerVerticle] - PeriodicVerticle deploy complete!
  ```
  - 提示
  > 以上的启动仅仅为测试 vertx-ddns 是否可以正常运行，如果我们关闭 ssh 连接，vertx-ddns 也将被关闭。要想一直处于运行状态，请继续看下面的教程。
- 进阶配置
  - 复制vertx-ddns.service 模板
  ```shell
  [Unit]
  Description=Vertx-DDNS Service
  Documentation=https://github.com/zf1976/vertx-ddns/edit/main/README.md
  After=network-online.target
  Wants=network-online.target

  [Service]
  User=USER
  Type=simple
  ExecStart=/usr/bin/java -server -Xms128m -Xmx256m -jar YOUR_JAR_PATH
  ExecStop=/bin/kill -s QUIT $MAINPID
  Restart=always
  StandOutput=syslog

  StandError=inherit

  [Install]
  WantedBy=multi-user.target
  ```
  - 参数
  ```shell
  -Xms256m：为 JVM 启动时分配的内存，请按照服务器的内存做适当调整，512 M 内存的服务器推荐设置为 128，1G 内存的服务器推荐设置为 256，默认为 256。
  -Xmx256m：为 JVM 运行过程中分配的最大内存，配置同上。
  YOUR_JAR_PATH：vertx-ddns 安装包的绝对路径，例如 /www/wwwroot/vertx-ddns-latest.jar。
  USER：运行 vertx-ddns 的系统用户，修改为你的用户名称即可。使用默认用户请删除 User=USER。
  ```
  - 提示
    1. 如果你不是按照上面的方法安装的 JDK，请确保 /usr/bin/java 是正确无误的
    2. systemd 中的所有路径均要写为绝对路径，另外，~ 在 systemd 中也是无法被识别的，所以你不能写成类似 ~/vertx-ddns-latest.jar 这种路径。
    3. 如何检验是否修改正确：把 ExecStart 中的命令拿出来执行一遍。
  - 创建模版文件
  ```shell
  # 将上面模版内容复制到文件内
  sudo vim /etc/systemd/system/vertx-ddns.service
  ```

</details>

<details> <summary>测试运行</summary>
	
  ```shell
  # 修改 service 文件之后需要刷新 Systemd
  sudo systemctl daemon-reload

  # 使 vertx-ddns 开机自启
  sudo systemctl enable vertx-ddns

  # 启动 vertx-ddns
  sudo service vertx-ddns start

  # 重启 vertx-ddns
  sudo service vertx-ddns restart

  # 停止 vertx-ddns
  sudo service vertx-ddns stop

  # 查看 vertx-ddns 的运行状态
  sudo service vertx-ddns status
  ```
 </details>

## Docker中使用

> Docker镜像提供了`ubuntu --- OpenJ9-16`,`debian:buster-slim --- OpenJ9-16`，`alpine --- OpenJDK-16`，三种基础镜像系统所对应`JRE Runtime`的程序镜像，
> 并且都经过`jlink`极简化，大大减少了镜像体积， 使用OpenJ9有效减少运行内存占用。
> 三种镜像大小`alpine` < `debian` < `ubuntu`。

- 支持host模式，并且不需要再做端口映射（同时支持IPv4/IPv6）
- 若不挂载主机目录, 删除容器同时会删除配置
- 在浏览器中打开`http://主机IP:8081`，修改你的配置，成功
  ```shell
  # 拉取镜像运行，并随系统重启
  docker run -d -p 8081:8080 --name vertx-ddns --restart=always zf1976/vertx-ddns:debian
  ```

- [可选] 挂载主机目录, 删除容器后配置不会丢失。可替换 `/root/.vertx_ddns` 默认用户权限root, 配置文件为隐藏文件
  ```shell
  docker run -d -p 8081:8080 --name vertx-ddns --restart=always -v /your_path:/root/.vertx_ddns zf1976/vertx-ddns:debian
  ```
- 若需要挂载日志文件到主机，则加上`-v /your_path:/root/logs`
### Webhook
- 解析记录日志状态发生变化（成功失败、错误），回调提供的Webhook API
- 消息内容支持模版变量，若消息内容为空或系统发送错误，则默认发送解析日志内容

  |  变量名   | 描述  |
  |  ----  | ----  |
  | #provider  | DNS服务商 (DNS Provider) |
  | #sourceIp  | 原IP (Raw IP) |
  | #targetIp  | 变化IP (Update IP) |
  | #time  | 解析时间 (The resolution time) |
  | #status  | 解析状态：`未改变` `失败` `成功` `错误` (Status) |
  | #domain  | 域名 (Domain) |
- 示例
> DNS Provider：#provider，Status：#status   -----parser---->    DNS Provider：ALIYUN, Status：2021-08-28 15:14:01



## 界面
<img src="./img/img.png"/>
<img src="./img/log.png"/>
<img src="./img/webhook.png"/>

## 注
- 默认登录的用户名密码：**vertx**
- Windows、macOS系统下Docker不支持Docker的host模式
- 默认禁止外网访问，如启动需要请编辑$HOME/.vertx_ddns/secure_config.json文件中notAllowWanAccess字段值为false，并重启服务

## License

[MIT License](https://raw.githubusercontent.com/zf1976/vertx-ddns/main/LICENSE)

## 贡献

目前只有`我`自己在维护这个项目。希望能有更多人加入 :)

感谢[Jetbrains](https://www.jetbrains.com/?from=mayi)制作的IDE，以及免费的开源许可证。

<img src="./img/jetbrains.png"/>
