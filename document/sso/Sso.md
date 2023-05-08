# SSO

> Single Sign On，单点登录. SSO是在多个应用系统中，用户只需要登录一次就可以访问所有相互信任的应用系统

![](https://oss.yiki.tech/img/202305072220725.png)

## Token、Cookie、Session

[一文彻底弄懂cookie、session、token](https://baijiahao.baidu.com/s?id=1705430412910145531&wfr=spider&for=pc)

| 名词    | 解释                                                         |
| ------- | ------------------------------------------------------------ |
| Token   | 令牌、用户登陆状态唯一标识。JSESSIONID、jwt、uuid            |
| Cookie  | 浏览器端的文本域, 可以在浏览器端存储用户的个性化信息 例如 token. 储存在用户本地终端(浏览器)上的数据 |
| Session | 服务器端端文本域, 可以在服务器端保存用户的状态               |

* 为什么选择 cookie 传递 token(两种技术路径不一样, 传递的方式也不一样)
  * 前后端分离: 浏览器地址栏中输入地址时 -> vue 项目 -> 响应页面(无数据), js 解析页面(发送 ajax 请求), 获取 LocalStorage 中的 token 放入 ajax 头信息中 -> 后台服务加载数据(数据接口)
  * 模版引擎: 浏览器地址栏中输入地址时 -> 直接访问后台服务, 加载好数据之后, 响应页面给浏览器 -> 浏览器渲染页面展示效果
    * 只能通过 cookie 形式把 token 传递给后台服务, 因为只要不禁用 cookie, 默认就会携带 cookie

### Cookie 的 作用域 与 作用路径

> 单点登录: 作用域选择一级域名(所有子域名都可以获取到 Cookie), 作用路径: /(所有路径的父路径都是 /)

* domain：作用域
  * 父不可以操作子的 cookie
  * 兄弟域名不能互相操作对方的 cookie
  * 子可以操作父的 cookie

| domain参数   | jd.com | sso.jd.com | order.jd.com |
| ------------ | ------ | ---------- | ------------ |
| jd.com       | √      | √          | √            |
| sso.jd.com   | ×      | √          | ×            |
| order.jd.com | ×      | ×          | √            |

* path
  * 设置/标识项目根路径，访问项目任何位置都会携带
  * response.addCookie默认放在当前路径下，访问当前路径下的所有请求都会带

### 有状态登陆

> 服务器端需要保存用户的登录信息. 例如 tomcat 中的 session

* 缺点
  * 增加服务器端的存储压力, 占用大量内存
  * 无法做到水平扩展
  * 客户端请求依赖服务端，多次请求必须访问同一台服务器

### 无状态登陆

> 服务器端不需要保存用户的登录信息

* 无状态登录的流程
  * 当客户端第一次请求服务时，服务端对用户进行信息认证（登录）
  * 认证通过，将用户信息进行加密形成token，返回给客户端，作为登录凭证
  * 以后每次请求，客户端都携带认证的token
  * 服务对token进行解密，判断是否有效

* 缺点
  * 一旦颁发无法回收(无法强制下线)

![](https://oss.yiki.tech/img/202305072221596.png)

## JWT

> json web tokens，是JSON风格轻量级的授权和身份认证规范，可实现无状态、分布式的Web应用授权；官网：https://jwt.io

### 数据格式

* JWT包含三部分数据(根据 . 进行数据分割)：

  - Header：头部，通常头部有两部分信息：
    - token类型：JWT
    - 加密方式：base64（HS256）
  - Payload：载荷，就是有效数据，一般包含下面信息：
    - 用户身份信息（注意，这里因为采用base64编码，可解码，因此不要存放敏感信息）
    - 注册声明：如token的签发时间，过期时间，签发人等
    - 这部分也会采用base64编码，得到第二部分数据
  - Signature：签名(保证安全性、防止篡改和伪造)，是整个数据的认证信息。根据前两步的数据，再加上指定的密钥（secret）（不要泄漏，最好周期性更换），通过base64编码生成。用于验证整个数据完整和可靠性
    - 加密（头 + 载荷 + 盐）

![](https://oss.yiki.tech/gmall/202303171654336.png)

### 交互流程

* 步骤
  * 用户登录
  * 服务的认证，通过后根据secret生成token
  * 将生成的token返回给浏览器
  * 用户每次请求携带token
  * 服务端利用公钥解读jwt签名，判断签名有效后，从Payload中获取用户信息
  * 处理请求，返回响应结果

> 因为JWT签发的token中已经包含了用户的身份信息，并且每次请求都会携带，这样服务的就无需保存用户信息，甚至无需去数据库查询，完全符合了Rest的无状态规范

![](https://oss.yiki.tech/img/202305072221943.png)

## 非对称加密

> 加密技术是对信息进行编码和解码的技术，编码是把原来可读信息（又称明文）译成代码形式（又称密文），其逆过程就是解码（解密），加密技术的要点是加密算法

- 对称加密，如AES
  - 基本原理：将明文分成N个组，然后使用密钥对各个组进行加密，形成各自的密文，最后把所有的分组密文进行合并，形成最终的密文。
  - 优势：算法公开、计算量小、加密速度快、加密效率高
  - 缺陷：双方都使用同样密钥，安全性得不到保证 
- 非对称加密，如RSA
  - 基本原理：同时生成两把密钥：私钥和公钥，私钥隐秘保存，公钥可以下发给信任客户端
    - 私钥加密，持有公钥才可以解密
    - 公钥加密，持有私钥才可解密
  - 优点：安全，难以破解
  - 缺点：算法比较耗时
- 不可逆加密，如MD5，SHA 
  - 基本原理：加密过程中不需要使用[密钥](https://baike.baidu.com/item/%E5%AF%86%E9%92%A5)，输入明文后由系统直接经过加密算法处理成密文，这种加密后的数据是无法被解密的，无法根据密文推算出明文。