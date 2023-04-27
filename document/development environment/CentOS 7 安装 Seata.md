## Seata

### 下载

```shell
cd /mydata/cloud/

wget https://github.com/seata/seata/releases/download/v1.4.2/seata-server-1.4.2.zip
```

![](https://oss.yiki.tech/img/202304280316389.png)

### 解压

```shell
unzip seata-server-1.4.2.zip
```

![](https://oss.yiki.tech/img/202304280317135.png)

### 启动

```shell
cd /mydata/cloud/seata/seata-server-1.4.2/bin/
 
nohup sh seata-server.sh -p 8091 -m file &> seata.log &
```

![](https://oss.yiki.tech/img/202304280317357.png)

