## Nginx 代理头信息

> 当一个请求到达网关路由时，网关路由通常会根据请求的路径信息来判断需要将请求路由到哪个后端服务。但是，在某些情况下，网关路由可能需要根据请求的域名信息来进行路由，这就需要使用到代理头信息了。如果不进行代理则找不到对应的服务

```yaml
        - id: index-route # 首页的同步请求路由
          uri: lb://index-service # 路由到那里, 路由到服务名对应的服务
          predicates: # 断言
            - Host=gmall.com, www.gmall.com # 因为首页服务可以没有路径, 再使用 路径 Path 进行路由就不合适了, 应该使用域名进行路由
#            - Path=/index/** # 如果写在一个路由里面, 他们的关系是and关系. 同时满足才可以
```

```shell
    # 代理网关
    server {
        listen       80;            # listen 指定了监听的端口
        server_name  api.gmall.com search.gmall.com gmall.com www.gmall.com; # server_name 指定了域名或服务器地址。

        proxy_set_header Host $host; # 代理设置头信息, 反向代理时把头信息一并携带过去. 把 host 通过 Host 头携带过去

        location / { # location / 指定了所有的请求路径都会被转发到后面的 proxy_pass 配置上。
            proxy_pass http://192.168.0.111:8888; # proxy_pass 配置指定了反向代理的地址，由服务器的 IP 地址和端口号组成。
        }
    }

```