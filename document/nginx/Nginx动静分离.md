## 动静分离

> Nginx 动静分离是指将动态资源和静态资源分别部署在不同的服务器上，以提高服务器性能和稳定性。其基本思想是将访问量较大、并且预计不会频繁变化的静态资源（如：HTML、CSS、JS、图片等）放置在 Nginx 的静态资源服务器上，而将动态资源（如：PHP、JSP、ASP.net，Servlet 等）放置在应用服务器上，通过反向代理和负载均衡技术实现。

```shell
    # 动静分离
    server {
        listen          80; # 监听 80 端口
        server_name     static.xxx.com; # 服务器地址或绑定域名

        location / { # 访问80端口后的所有路径都转发到 proxy_pass 配置的ip中
                root /opt/static;
        }
    }
```

![](https://oss.yiki.tech/img/202305011325516.png)

![](https://oss.yiki.tech/img/202305011325304.png)