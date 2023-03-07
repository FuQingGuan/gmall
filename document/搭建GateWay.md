## 搭建网关系统

![](https://oss.yiki.tech/gmall/202303080501354.png)

![](https://oss.yiki.tech/gmall/202303080501369.png)

### POM

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 以 gmall 工程作为父工程 -->
    <parent>
        <groupId>com.atguigu</groupId>
        <artifactId>gmall</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <groupId>com.atguigu</groupId>
    <artifactId>gmall-gateway</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>gmall-gateway</name>
    <description>谷粒商城 网关工程</description>

    <dependencies>
        <!-- nacos -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>

        <!-- gateway -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

###  application.yml

```yml
server:
  port: 8888 # 服务端口
spring:
  cloud:
    nacos: # 注册中心配置
      discovery:
        server-addr: 192.168.0.101:8848 # 注册中心地址
    gateway: # 网关
      routes: # 路由
        - id: pms-route # 路由唯一标识, 商品管理路由
          uri: lb://pms-service # 路由到那里, 路由到服务名对应的服务
          predicates: # 断言
            - Path=/pms/** # 什么样的请求到此路由 进而进入路由该服务. pms 开头的请求 都 路由到 pms-service 服务
        - id: ums-route # 路由唯一标识, 用户管理路由
          uri: lb://ums-service # 路由到那里, 路由到服务名对应的服务
          predicates: # 断言
            - Path=/ums/** # 什么样的请求到此路由 进而进入路由该服务. ums 开头的请求 都 路由到 ums-service 服务
        - id: wms-route # 路由唯一标识, 仓库管理路由
          uri: lb://wms-service # 路由到那里, 路由到服务名对应的服务
          predicates: # 断言
            - Path=/wms/** # 什么样的请求到此路由 进而进入路由该服务. wms 开头的请求 都 路由到 wms-service 服务
        - id: oms-route # 路由唯一标识, 订单管理路由
          uri: lb://oms-service # 路由到那里, 路由到服务名对应的服务
          predicates: # 断言
            - Path=/oms/** # 什么样的请求到此路由 进而进入路由该服务. oms 开头的请求 都 路由到 oms-service 服务
        - id: sms-route # 路由唯一标识, 营销管理路由
          uri: lb://sms-service # 路由到那里, 路由到服务名对应的服务
          predicates: # 断言
            - Path=/sms/** # 什么样的请求到此路由 进而进入路由该服务. sms 开头的请求 都 路由到 sms-service 服务
```

###  bootstrap.yml

```yml
spring:
  application:
    name: gatewaty-api # 服务名称
  cloud:
    nacos:
      config:
        server-addr: 192.168.0.101:8848 # 配置中心地址
        namespace:  # 命名空间 ID
        group:  # 配置文件分组
        file-extension:  # 配置文件扩展名, 默认是 properties
```

### 最终效果

![](https://oss.yiki.tech/images/202303052340497.png)
