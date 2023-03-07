## 逆向工程

### 无法创建到数据库服务器的连接。

```shell
com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException: Could not create connection to database server.
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method) ~[na:1.8.0_352]
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62) ~[na:1.8.0_352]
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45) ~[na:1.8.0_352]
	at java.lang.reflect.Constructor.newInstance(Constructor.java:423) ~[na:1.8.0_352]
	at com.mysql.jdbc.Util.handleNewInstance(Util.java:404) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.Util.getInstance(Util.java:387) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:917) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:896) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:885) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:860) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.ConnectionImpl.connectOneTryOnly(ConnectionImpl.java:2332) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.ConnectionImpl.createNewIO(ConnectionImpl.java:2085) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.ConnectionImpl.<init>(ConnectionImpl.java:795) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.JDBC4Connection.<init>(JDBC4Connection.java:44) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method) ~[na:1.8.0_352]
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62) ~[na:1.8.0_352]
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45) ~[na:1.8.0_352]
	at java.lang.reflect.Constructor.newInstance(Constructor.java:423) ~[na:1.8.0_352]
	at com.mysql.jdbc.Util.handleNewInstance(Util.java:404) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.ConnectionImpl.getInstance(ConnectionImpl.java:400) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.NonRegisteringDriver.connect(NonRegisteringDriver.java:327) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.alibaba.druid.pool.DruidAbstractDataSource.createPhysicalConnection(DruidAbstractDataSource.java:1452) ~[druid-1.0.28.jar:1.0.28]
	at com.alibaba.druid.pool.DruidAbstractDataSource.createPhysicalConnection(DruidAbstractDataSource.java:1516) ~[druid-1.0.28.jar:1.0.28]
	at com.alibaba.druid.pool.DruidDataSource$CreateConnectionThread.run(DruidDataSource.java:2080) ~[druid-1.0.28.jar:1.0.28]
Caused by: java.lang.NullPointerException: null
	at com.mysql.jdbc.ConnectionImpl.getServerCharset(ConnectionImpl.java:3005) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.MysqlIO.sendConnectionAttributes(MysqlIO.java:1916) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.MysqlIO.proceedHandshakeWithPluggableAuthentication(MysqlIO.java:1845) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.MysqlIO.doHandshake(MysqlIO.java:1215) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.ConnectionImpl.coreConnect(ConnectionImpl.java:2255) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	at com.mysql.jdbc.ConnectionImpl.connectOneTryOnly(ConnectionImpl.java:2286) ~[mysql-connector-java-5.1.38.jar:5.1.38]
	... 13 common frames omitted
```

### 原因

> MySQL版本和驱动包不兼容的问题

![](https://oss.yiki.tech/images/202303051638729.png)

![](https://oss.yiki.tech/images/202303051637112.png)

### 解决

> 修改 连接驱动 与 mysql 版本

```yml
driverClassName: com.mysql.cj.jdbc.Driver

<mysql.version>8.0.20</mysql.version>
```

![](https://oss.yiki.tech/images/202303051650530.png)