## Feign

> Feign是一个声明式的Web服务客户端，提供了一种可以使用接口和注解的方式来调用远程HTTP服务的方法

![](https://oss.yiki.tech/gmall/202303090434481.png)

* 缺点
    * 代码冗余。尽管不用写实现，只是写接口，但服务调用方要写与服务controller一致的代码，有几个消费者就要写几次。另外DTO对象，在每个需要feign的工程里面都要维护一份。
    * 增加开发成本。调用方还得清楚知道接口的路径，才能编写正确的FeignClient

## Feign 的最佳实践

![](https://oss.yiki.tech/gmall/202303090434328.png)

## 使用传参注意

```java
feign 请求方式: Get / Post 阉割版的 http 协议, 支持占位符, 支持普通参数, 不支持 form 表单
    少量参数
        占位符: 适用于参数较少时, @PathVariable() 较为麻烦
        普通参数: Feign 是阉割版本的 HTTP 不支持 form 表单 只支持 ? 使用 @RequestParam() 一一接收多个参数
        json: 传递多个参数, @RequestBody 接收, 只支持 Post 请求
```