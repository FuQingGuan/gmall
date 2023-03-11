## 反向代理

> 通常情况下，一个域名只能绑定一个IP地址和一个端口号。如果有多个服务需要通过同一个域名来访问，那么就需要使用反向代理来实现。
> 我们希望直接通过域名去访问服务资源. `http://xxx.xxx.xxx` 默认都是 80 端口, 而一个服务器 80 端口 只有一个。
> 将来我们希望多个工程可以使用 域名去访问. 此时就需要借助于 nginx 的反向代理. 所有请求通过nginx 转发到对应的服务, 在通过网关路由到具体的服务

![](https://oss.yiki.tech/gmall/202303080505317.png)

![](https://oss.yiki.tech/gmall/202303080505183.png)

![](https://oss.yiki.tech/gmall/202303080505417.png)

![](https://oss.yiki.tech/gmall/202303080505886.png)

![](https://oss.yiki.tech/gmall/202303080505930.png)

### 修改配置文件

```shell
cd /usr/local/nginx/conf/

vim nginx.conf
```

```shell
worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;
    keepalive_timeout  65;

    # 代理网关
    server {
        listen          80; # 监听 80 端口
        server_name     api.gmall.com; # 服务器地址或绑定域名

        location / { # 访问80端口后的所有路径都转发到 proxy_pass 配置的ip中
                proxy_pass      http://192.168.0.111:8888; # 配置反向代理的ip地址和端口号 [注：url地址需加上http://]
        }
    }

    # 代理后台管理
    server {
        listen          80; # 监听 80 端口
        server_name     manager.gmall.com; # 服务器地址或绑定域名

        location / { # 访问80端口后的所有路径都转发到 proxy_pass 配置的ip中
                proxy_pass      http://192.168.0.121:1000; # 配置反向代理的ip地址和端口号 [注：url地址需加上http://]
        }
    }
}
```

```shell
// 进入 nginx 目录
cd /usr/local/nginx/sbin

// 重新加载
./nginx  -s  reload
```

> 需要注意的是，在实际生产环境中，应该根据实际情况来配置Nginx的工作进程数、连接数、超时时间等参数，以及为每个代理服务器配置独立的域名或路径。此外，Nginx还可以使用SSL协议来加密通信，增加数据传输的安全性。

## Nginx 加入后流程

> 域名会经过 hosts 文件 或者 dns 服务器解析成 具体的 Ip 访问 Nginx, Nginx 通过 请求头 Host 将域名携带过去. nginx 配置文件中的 server_name 通过携带的 域名进行其他操作

> 在实际情况下，客户端通常会将域名解析成IP地址，并通过该IP地址访问Nginx服务器。在这个过程中，客户端会向DNS服务器或者本地hosts文件中查询域名对应的IP地址，并将该IP地址用于向Nginx服务器发起请求。有些请求在网关中不是跟据路径进行跳转而是根据域名进行跳转，为了保证Nginx可以正确处理请求，客户端需要在请求头中携带原始的域名信息。在Nginx服务器中，可以通过`$host`变量来获取请求头中的域名信息，并在代理服务器中使用`proxy_set_header`指令将域名信息添加到请求头中。

![](https://oss.yiki.tech/gmall/202303080506488.png)



> 浏览器输入域名 -> 本机hosts 文件对此域名进行解析 -> 真实发送请求是 ip + 请求路径 -> 根据 ip + 80 端口找到 nginx 服务器 -> nginx 配置了反向代理根据 域名头信息找到 当前 server -> 根据反向代理后的  ip 地址找到网关应用 -> 网关根据 断言配置 路由到对应的 微服务 -> 找到对应的 controller 方法处理 -> 处理完后沿路返回到用户浏览器

![](https://oss.yiki.tech/gmall/202303080506014.png)

> 在实际情况下，客户端通常会先通过DNS服务器或本地hosts文件将域名解析成IP地址，并使用该IP地址和端口号向Nginx服务器发起请求。
>
> Nginx服务器通常会根据请求头中的`Host`信息，选择对应的虚拟主机进行处理，并将请求转发到后端的网关应用中。网关应用根据路由规则将请求转发到对应的微服务中，然后微服务根据路由规则找到对应的Controller方法进行处理，并返回结果给网关应用。最后，网关应用将处理结果返回给Nginx服务器，并由Nginx将结果返回给客户端浏览器。
>
> 需要注意的是，在实际生产环境中，应该根据实际情况来配置Nginx服务器、网关应用、微服务等各个组件的参数，并使用安全通信协议保障数据的传输安全性。同时，在设计路由规则时，也需要考虑到性能、可伸缩性、可维护性等多个方面，以便实现更加高效、稳定和可靠的应用程序。

## 静态资源部署

```shell
# 动静分离
server {
    listen          80; # 监听 80 端口
    server_name     static.gmall.com; # 服务器地址或绑定域名

    location / { # 访问80端口后的所有路径都转发到 proxy_pass 配置的ip中
           root /opt/static;
    }
}
```

![](https://oss.yiki.tech/gmall/202303112103070.png)

![](https://oss.yiki.tech/gmall/202303112104285.png)

![](https://oss.yiki.tech/gmall/202303112105996.png)

![](https://oss.yiki.tech/gmall/202303112203842.png)

## 一些常见问题

> 在排查问题时，可以通过不断尝试不同的方法，逐步缩小问题的范围，最终找到问题所在并解决它。

### 502 Bad Gateway

> "502 Bad Gateway"错误通常意味着Nginx服务器无法将请求成功地转发到后端网关服务器

| 解决思路                                                     | 检察方法                                                     |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| 检查后端服务器是否已启动并运行正常。                         | 可以通过尝试在后端服务器上直接访问资源来确定是否存在问题。   |
| 检查Nginx的配置文件是否正确，尤其是代理服务器的配置是否正确。 | 可以通过检查Nginx的错误日志来查找问题所在。                  |
| 检查Nginx和后端服务器之间的网络连接是否正常。                | 例如防火墙或路由器是否正确配置。                             |
| 检查后端服务器的日志文件                                     | 看是否存在错误或异常信息。                                   |
| 如果使用了负载均衡器                                         | 可以尝试重新分配请求，以确保所有的后端服务器都能够被访问到。 |
| 如果后端服务器使用了SSL协议                                  | 需要确保Nginx已正确配置证书和密钥，并且SSL配置是否正确。     |
| 如果使用了代理缓存                                           | 需要确保缓存已正确配置，并且缓存是否已经过期或被清空。       |

![](https://oss.yiki.tech/gmall/202303080506164.png)

### 404

> "404 Not Found"错误表示请求的资源未被找到。这通常意味着客户端请求的URL地址不存在，或者请求的资源被移动或删除了。

| 解决思路                               | 检察方法                                                     |
| -------------------------------------- | ------------------------------------------------------------ |
| 检查URL地址是否正确。                  | 可能是因为输入的URL地址有误，或者与实际路径不匹配。          |
| 检查文件是否存在。                     | 如果请求的资源是文件，那么需要确保该文件存在于服务器上，并且具有正确的权限设置。 |
| 检查目录是否存在。                     | 如果请求的资源是目录，那么需要确保该目录存在于服务器上，并且具有正确的权限设置。 |
| 检查服务器是否配置了正确的默认文档。   | 如果请求的URL地址不包含文件名，那么服务器需要配置默认文档，例如index.html。 |
| 检查服务器是否配置了正确的重定向规则。 | 如果请求的资源被移动或重命名了，那么服务器需要配置重定向规则，以便客户端可以自动访问新的资源。 |

![](https://oss.yiki.tech/gmall/202303080506091.png)

### 网关根据域名路由注意

> 域名不带路径访问首先会解析成 ip 地址访问 nginx, nginx 把请求转发给网关. nginx 是通过 ip:port 转发给网关.
>
> 网关无法拿到域名, 也就进入不了对应的路由进而无法到达 对应的服务. 

![](https://oss.yiki.tech/gmall/202303080506354.png)

```shell
    # 代理网关
    server {
        listen          80; # 监听 80 端口
        server_name     api.gmall.com search.gmall.com www.gmall.com; # 服务器地址或绑定域名

        location / { # 访问80端口后的所有路径都转发到 proxy_pass 配置的ip中
                proxy_pass      http://192.168.0.111:8888; # 配置反向代理的ip地址和端口号 [注：url地址需加上http://]
        }
    }
```

![](https://oss.yiki.tech/gmall/202303080506293.png)

![](https://oss.yiki.tech/gmall/202303080506651.png)

> Nginx可以通过请求头中的`Host`信息获取到访问的域名，但是在默认情况下，Nginx转发请求时不会将`Host`信息一起转发到后端服务。
>
> 因此，需要在代理服务器中通过`proxy_set_header`指令将`Host`信息添加到请求头中，并转发到后端服务中。
>
> 网关可以获取域名就可以正常路由到相应的服务了

```shell
    # 代理网关
    server {
        listen          80; # 监听 80 端口
        server_name     api.gmall.com search.gmall.com www.gmall.com; # 服务器地址或绑定域名

				proxy_set_header Host $host; # 代理设置头信息, 反向代理时把头信息一并携带过去. 把 host 通过 Host 头携带过去

        location / { # 访问80端口后的所有路径都转发到 proxy_pass 配置的ip中
                proxy_pass      http://192.168.0.111:8888; # 配置反向代理的ip地址和端口号 [注：url地址需加上http://]
        }
    }
```

![](https://oss.yiki.tech/gmall/202303080506592.png)

![](https://oss.yiki.tech/gmall/202303080506360.png)
