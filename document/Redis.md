> 缓存是一种通过预先存储数据以加快对这些数据的后续访问的技术。使用缓存可以提高应用程序的性能、响应时间和可扩展性，并减少对后端数据库或其他数据存储的请求量，从而提高系统的吞吐量。

# 缓存

> 当用户访问已经缓存的资源时，请求会首先经过Redis。如果Redis中存在缓存数据，则直接返回缓存数据响应请求。如果Redis中不存在缓存数据，请求会继续查询MySQL数据库。如果MySQL中存在具体数据，会将数据写入Redis缓存，并进行响应。如果MySQl中不存在具体数据，则直接响应null值。

![](https://oss.yiki.tech//oss/202302130309730.png)

![](https://oss.yiki.tech/gmall/202303130105110.png)

![](https://oss.yiki.tech/gmall/202303130105174.png)

## 实现缓存案例

> 电商中首页访问量非常大，首页中的商品分类访问更大，鼠标移动就在访问，查询所有数据。它完全满足添加缓存的两个条件
>
> 1. 变化频率低 2. 访问很频繁

![](https://oss.yiki.tech/gmall/202303130105885.png)

```java
/**
 * 使用 String 序列化器: 内存占用率 最低, 性能一般, 可读性较高
 */
@Autowired
private StringRedisTemplate redisTemplate;

/**
 * 模块名称作为第一位 找到团队的缓存, INDEX 首页
 * 模型名称作为第二位 找到工程的缓存, CATES 分类
 * 真正的key作为第三位 找到真正的值
 */
private static final String KEY_PREFIX = "INDEX:CATES:";

// 一般 自定义前缀 + 请求参数作为 key, 返回结果集作为 value 放入缓存
public List<CategoryEntity> queryLvl23CategoriesByPid(Long pid) {
    // 1. 先查询缓存, 如果缓存命中则返回
    String json = redisTemplate.opsForValue().get(
        // 前缀 INDEX:CATES: + 请求参数
        KEY_PREFIX + pid
    );

    // 不为空则
    if (StringUtils.isNotBlank(json)) {
        // 将 Json 字符串数据转换成集合对象
        return JSON.parseArray(json, CategoryEntity.class); // json 字符串, 转换类型
    }

    // 2. 远程调用, 查询数据库 并 放入缓存
    ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryLevel23CategoriesByPid(pid);
    List<CategoryEntity> categoryEntities = categoryResponseVo.getData();

    // 判断数据是否为 null, 为 null 直接返回
    if (CollectionUtils.isEmpty(categoryEntities)) {
        return categoryEntities;
    }

    // 放入缓存
    redisTemplate.opsForValue().set(
        // key, value
        KEY_PREFIX + pid, JSON.toJSONString(categoryEntities) // 将 Java 对象转换换为 JSON 对象
    );

    return categoryEntities;
}
```

## 加入缓存前后对比

![](https://oss.yiki.tech/gmall/202303130105974.png)

![](https://oss.yiki.tech/gmall/202303130105276.png)

<div>
  <!-- mp4格式 -->
  <video id="video" controls="" width="800" height="500" preload="none" poster="封面">
        <source id="mp4" src="https://oss.yiki.tech/gmall/202303130105032.mp4" type="video/mp4">
  </videos>
</div>



## 数据一致性问题

> 管理员修改或删除了MySQL中的数据而没有及时更新Redis中的数据，用户访问这条数据时可能会从Redis中获取到旧的数据，导致数据不一致。比如，管理员将MySQL中的id=1的数据从"旧"修改为"新"，但是Redis缓存中的数据仍然是"旧"，当用户访问这条数据时，经过上述缓存业务逻辑，会从Redis中获取到"旧"的数据，而MySQL中的数据已经更新为"新"，这样就会导致Redis中的数据和MySQL中的数据不一致。

![](https://oss.yiki.tech/gmall/202303130106553.png)

### 数据一致性问题演示

<div>
  <!-- mp4格式 -->
  <video id="video" controls="" width="800" height="500" preload="none" poster="封面">
        <source id="mp4" src="https://oss.yiki.tech/gmall/202303130106512.mp4" type="video/mp4">
  </videos>
</div>



### 数据一致性问题解决方案

#### 双写模式

> 无论是先写 MySQL 还是先写 Redis，在写入数据时都有可能出现异常导致 MySQL 事务回滚，使用这种方式还是会导致数据不一致

![](https://oss.yiki.tech/gmall/202303130106836.png)

#### 失效模式

> 当 Redis 中缓存的数据失效时，需要重新从 MySQL 中读取数据，此时可能会出现数据不一致的问题。例如，当 Redis 中缓存了一条记录，但是在这条记录被访问之前，MySQL 中的数据已经被修改或删除，此时从 Redis 中读取数据就会得到错误的结果。

![](https://oss.yiki.tech/gmall/202303130106972.png)

#### 双删模式

> 双删模式是指在进行更新或删除操作时，先删除缓存中的数据，再进行数据库操作，最后再次删除缓存中的数据。这种方式可以保证数据一致性，因为如果在进行数据库操作时出现异常，数据不会被缓存，下次访问时会再次从数据库中获取最新数据并进行缓存。如果先进行数据库操作再删除缓存，如果在数据库操作时出现异常，那么已经删除的缓存将导致下次访问时无法获取最新数据，导致数据不一致。

![](https://oss.yiki.tech/gmall/202303130106816.png)

##### 双删案例演示

<div>
  <!-- mp4格式 -->
  <video id="video" controls="" width="800" height="500" preload="none" poster="封面">
        <source id="mp4" src="https://oss.yiki.tech/gmall/202303130107228.mp4" type="video/mp4">
  </videos>
</div>


![](https://oss.yiki.tech/gmall/202303130107502.png)

```java
    /**
     * 模块名称作为第一位 找到团队的缓存
     * 模型名称作为第二位 找到工程的缓存
     * 真正的key作为第三位 找到真正的值
     */
    private static final String KEY_PREFIX = "INDEX:CATES:"; // 此处只为演示数据一致性效果, PMS 工程未添加缓存 统一操作 INDEX 工程的缓存

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public void update(CategoryEntity category) {

        String key = KEY_PREFIX + category.getParentId();

        // 删除 Redis 中相应的缓存内容
        redisTemplate.delete(key);

        // 写入 MySQL
        updateById(category);

        /**
         * 发送消息 异步删除 Redis。方法末尾 方法结束之前发送消息, 归为一个事务. 做到要成功都成功, 要失败都失败. 不能存在 分类修改不成功 消息已发送. 或者 分类修改成功 消息没有发送
         *
         * 交换机: 第一位应该取模块名, 可以方便寻找到自己的交换机. 第二位应该设置为操作信息 操作 SPU, 第三位以 EXCHANGE 结尾
         * rk: 指定内容 单品新增, 更新时应该还有一个 category.update, 删除时应该还有一个 category.delete
         * 消息内容: 首页工程中只目前只缓存了 一级分类的子分类, 以 KEY_PREFIX + pid 做为 Key. 以 其下二三级数据为 Value。此处传递 Key 或者 Pid 即可
         */
        rabbitTemplate.convertAndSend("PMS_CATEGORY_EXCHANGE", "category.update", key);

    }
```

```yaml
  rabbitmq:
    host: 192.168.0.101
    port: 5672
    virtual-host: /admin
    username: admin
    password: admin
    listener:
      # 消费者类型. direct 不能多线程消费
      type: simple # simple 另开线程获取消息、direct 直接使用消费者主线程获取消息
      simple:
        # 设置能者多劳(公平分发): rabbitmq 默认是采用轮训的方式分配消息当有多个消费者接入时，消息的分配模式是一个消费者分配一条，直至消息消费完成.
        prefetch: 1 # 消费完一条消息后才回去 队列获取下一条消息
        concurrency: 8 # 数值取决于 cpu 核数. 此时一个连接会有 8 个信道. 充分发挥每个cpu的性能
        # ack 即是确认字符，在数据通信中，接收站发给发送站的一种传输类控制字符。表示发来的数据已确认接收无误
        # 生产者 -> mq -> 消费者. 站在的角度不同 接收站与消费站也不同
        # 消费者确认模式:
        #   nome - 不确认模式, 只要消费者获取了消息, 消息即被确认. 如果程序发生异常等 消息即被丢失
        #   auto - 自动确认, 只要消费者在消费过程中没有异常即被确认, 如果出现异常会无限重试(如果有几条消息发生异常无限重试会耗费大量服务器资源)
        #   manual 手动确认模式, 在消费者最后手动确认 可以 保证消息的安全性
        #
        #     channel.basicack 确认 / basicnack() 不确认 / basicreject() 拒绝消息
        acknowledge-mode: manual
    # 生产者确认类型
    #   none - 不确认模式
    #   simple - 同步阻塞方式确认, 性能不高(等到死为止, 并发不过百可以使用. 可靠性较高)
    #   correlated - 异步非阻塞方式确认, 性能较高
    publisher-confirm-type: correlated
    # 确认消息是否到达队列
    publisher-returns: true
```

```java
@Component // 注入到 Spring 容器中
public class CategoryListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 该方法会声明此方法是一个监听器, 可以监听队列获取消息
    @RabbitListener(bindings = @QueueBinding( // 声明绑定关系
            // 绑定的 队列, 将下方声明的交换机绑定给此队列
            value = @Queue(value = "PMS_CATEGORY_QUEUE",
                    durable = "true", // 使用 durable 指定是否需要持久化 默认是 true
                    // 队列存在的情况下 如果声明一个属性与之前队列不一样 rabbitmq 就会报声明错误, ignoreDeclarationExceptions 可以使用忽略声明异常进行忽略(可以忽略声明异常使用既有的)
                    ignoreDeclarationExceptions = "true"),  // 默认队列不需要设置
            // 需要与生产者中的交换机一致, type 交换机类型
            exchange = @Exchange(value = "PMS_CATEGORY_EXCHANGE", ignoreDeclarationExceptions = "ture", // 通常在 交换机中设置忽略声明异常, 可以避免重复声明
                    // 通配模型
                    type = ExchangeTypes.TOPIC), // 绑定的 交换机
//            key = {"category.update, category.delete"} // rk, 可以绑定多个
            key = {"category.*"} // rk, 可以绑定多个
    ))
    public void syncData(String key, Channel channel, Message message) throws IOException {

        long startTime = System.currentTimeMillis();
        System.err.println("异步删除缓存开始: " + startTime + "ms");

        // 如果为空表示为垃圾消息直接确认
        if (StringUtils.isBlank(key)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        try {
            // 异步删除。避免 在删除 Redis 到写入 MySQL间. 有用户 查询 Redis 发现没有数据 将旧数据 放入 Redis 导致的数据不一致
            redisTemplate.delete(key);

            // 确认消息: 游标 直接 copy, 是否批量确认消息: 设置为 true 的话 从最近确认消息开始到当前消息之间未被确认的消息都被批量确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();

            // 判断消息是否重试投递过
            if (message.getMessageProperties().getRedelivered()) { // 已重试过直接拒绝
                // TODO: 记录日志, 或者保存到数据库表中 通过定时任务, 或者 人工排查 处理消息. 如果有死信队列会进入死信队列

                // 拒绝消息: 游标, 重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else { // 未重试过, 重新入队
                // 不确认消息: 游标, 是否批量确认, 重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }

        long endTime = System.currentTimeMillis(); // 商品信息同步结束
        System.err.println("异步删除缓存结束: " + startTime + "ms");
        System.err.println("异步删除缓存结束! 总耗时: " + (endTime - startTime) + "ms");
    }

}
```

```java
@Configuration // 声明该类是一个配置类
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct // 构造方法执行之后就会执行, 项目时添加此配置类 调用该类的无参构造方法初始化. 添加该注解构造方法执行之后就会执行设置两个回调
//    @PreDestroy // 对象销毁之前执行
    public void init() {
        // 确认消息是否到达交换机的回调, 不管消息是否到达交换机都会执行
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息已到达交换机");
            } else {
                System.err.println("消息没有达到交换机: 原因 " + cause);
            }
        });

        // 确认消息是否到达队列的回调, 只有消息没有到达队列才会执行
        // 例如 消息没有达到交换机: 原因 channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'spring_test_exchange2' in vhost '/admin', class-id=60, method-id=40)
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) ->
                System.err.println("消息没有到达队列: " + " 交换机 " + exchange + " 路由键 " + routingKey
                        + " 消息内容 " + replyText + " 状态码 " + replyCode + " 消息内容 " + new String(message.getBody()))
        );
    }
}
```

## 缓存的三大问题

### 击穿

> 缓存击穿: 一个热点 key 过期, 大量请求直达数据库, 导致 mysql 宕机

![](https://oss.yiki.tech/gmall/202303130107437.png)

### 穿透

> 缓存穿透(数据为空): 大量请求访问不存在的数据, 由于数据不存在, redis 中可能没有, 此时大量请求没有到达数据库, 导致服务器宕机

![](https://oss.yiki.tech/gmall/202303130107124.png)

```Java
        if (CollectionUtils.isNotEmpty(categoryEntities)) {
            redisTemplate.opsForValue().set(
                    // key, value
                    KEY_PREFIX + pid, JSON.toJSONString(categoryEntities) // 将 Java 对象转换换为 JSON 对象
            );
        } else {
            /**
             * 缓存穿透(数据为空): 大量请求访问不存在的数据, 由于数据不存在, redis 中可能没有, 此时大量请求没有到达数据库, 导致服务器宕机
             * 　    基础解决方案: 即使为 null 也缓存, 缓存时间一般不超过 5 分钟(防止存储大量 null 数据导致内存占满 进而导致的服务宕机)
             *       依然存在待解决的问题:  如果每次访问不存在且不重复的数据即使缓存为 null 的值 请求依然会直达数据库 应该使用 布隆过滤器
             */
            redisTemplate.opsForValue().set(
                    // key, value
                    KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), // 将 Java 对象转换换为 JSON 对象
                    5, TimeUnit.MINUTES // 缓存时间 5 分钟
            );
        }
```

### 雪崩

> 缓存雪崩: 由于缓存时间一样, 导致缓存同时失效, 此时大量请求访问这些数据, 请求就会直达数据库, 导致服务器宕机

![](https://oss.yiki.tech/gmall/202303130107988.png)

```java
        if (CollectionUtils.isNotEmpty(categoryEntities)) {
            /**
             * 缓存雪崩: 由于缓存时间一样, 导致缓存同时失效, 此时大量请求访问这些数据, 请求就会直达数据库, 导致服务器宕机
             * 　    解决方案: 给缓存时间添加随机值 缓存时间 + new Random().nextInt(10)
             */
            redisTemplate.opsForValue().set(
                    // key, value
                    KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), // 将 Java 对象转换换为 JSON 对象
                    90 + new Random().nextInt(10), TimeUnit.DAYS // 缓存时间 90 天
            );
        } else {
            /**
             * 缓存穿透(数据为空): 大量请求访问不存在的数据, 由于数据不存在, redis 中可能没有, 此时大量请求没有到达数据库, 导致服务器宕机
             * 　    基础解决方案: 即使为 null 也缓存, 缓存时间一般不超过 5 分钟(防止存储大量 null 数据导致内存占满 进而导致的服务宕机)
             *       依然存在待解决的问题:  如果每次访问不存在且不重复的数据即使缓存为 null 的值 请求依然会直达数据库 应该使用 布隆过滤器
             */
            redisTemplate.opsForValue().set(
                    // key, value
                    KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), // 将 Java 对象转换换为 JSON 对象
                    5, TimeUnit.MINUTES // 缓存时间 5 分钟
            );
        }
```

