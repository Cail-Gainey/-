# -本人只负责其中模块A和模块B数据采集部分以及模块D部分
## 模块 A：大数据平台环境搭建

参考：

[https://www.cnblogs.com/beast-king/p/17892187.html]()

[https://blog.csdn.net/weixin_54412689/article/details/134129430#3_listenxml9000hadoopclickhouse9001_655]()

[https://developer.aliyun.com/article/1272221]()

## 模块 B：实时数据采集

### 一：Flume采集实时数据

#### 1.、启动zookeeper、[kafka](https://so.csdn.net/so/search?q=kafka&spm=1001.2101.3001.7020)并创建kafka主题

`./bin/zkServer.sh start`



~~~shell
./bin/kafka-server-start.sh -daemon ./config/server.properties
 
./bin/kafka-topic.sh --create --topic test --partitions 1 --replication-factor 1 --zookeeper master:2181
~~~



#### 2、创建flume-kafka.conf配置文件

用于采集socket数据后存入kafka

在flume文件夹中的conf下新建flume-kafka.conf配置文件

```shell
vim flume-kafka.conf
```

~~~shell
a1.sources = s1   数据源
a1.channels = c1    通道
a1.sinks = k1     输出
 
a1.sources.s1.type = netcat   类型
a1.sources.s1.bind = localhost   主机
a1.sources.s1.port = 10050    端口
 
a1.sinks.k1.type = org.apache.flume.sink.kafka.KafkaSink   
a1.sinks.k1.kafka.topic = test   kafka主题
a1.sinks.k1.kafka.bootstrap.servers = master:9092,slave1:9092,slave2:9092  集群
 
a1.channels.c1.type = memory   内存
a1.channels.c1.capacity = 1000  
a1.channels.c1.transactionCapacity = 100
 
a1.sources.s1.channels = c1
a1.sinks.k1.channel = c1
~~~

> 设置监听本地端口10050 netcat发送的socket数据，将采集到的数据存入kafka的test主题中



#### 3、启动flume

`./bin/flume-ng agent -n a1 -c conf -f ./conf/flume-kafka.conf -Dflume.root.logger=INFO,console`



./bin/flume-ng：启动Flume-ng二进制文件。

agent：指定要启动的Flume组件类型为代理。

-c conf：设置Flume配置文件所在的目录为当前目录下的conf目录。

-f ./conf/flume_kafka.conf：指定Flume代理使用的配置文件路径和名称。

-n a1：给Flume代理指定一个名称为a1。

-Dflume.root.logger=INFO,console：设置Flume代理的日志级别为INFO，并将日志输出到控制台。

#### 4、创建kafka消费者

`./bin/kafka-console-consumer.sh --from-beginning --topic test --bootstrap-server master:9092,slave1:9092,slave2:9092`



/bin/kafka-console-consumer.sh：启动 Kafka 控制台消费者。

--from-beginning：从该主题的开始位置读取消息。

--topic test：指定要消费的主题名称为 "test"。

--bootstrap-server 集群各个主机:端口：指定连接到 Kafka 集群的所有 broker 的主机名和端口号



#### 5、netcat向本地10050端口发送socket数据

先开启flume，这里就会直接进入输入模式

`nc localhost 10050`

------

### 二：maxwell采集

参考教程：[https://www.cnblogs.com/wuning/p/12623512.html]()

#### 1、配置MySQL

~~~sql
binlog有三种格式：Statement、Row以及Mixed

–基于SQL语句的复制(statement-based replication,SBR) 
–基于行的复制(row-based replication,RBR) 
–混合模式复制(mixed-based replication,MBR)

STATMENT模式：每一条会修改数据的sql语句会记录到binlog中

ROW模式：不记录sql语句上下文相关信息，仅保存哪条记录被修改

Mixed模式：从5.1.8版本开始，MySQL提供了Mixed格式，实际上就是Statement与Row的结合

~~~

##### 1.1、创建my.cnf

~~~bash
cp /usr/share/mysql/my-default.cnf /etc/my.cnf (若没有/usr/share/mysql/my-default.cnf,可直接vi /etc/my.cnf)

#my.cnfvim 
[mysqld]
server_id=1  # 随机指定
log-bin=master
binlog_format=row #选择 Row 模式
sql_mode=NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES

~~~

##### 2.2、启动MySQL

~~~sql
mysql> set global binlog_format=ROW;
mysql> set global binlog_row_image=FULL;
~~~

##### 2.3、配置Maxwell库及相关权限

Maxwell需要连接MySQL，并创建一个名称为maxwell的数据库存储元数据，且需要能访问需要同步的数据库，新创建一个MySQL用户专门用来给Maxwell使用
**注意**：MaxWell 在启启动时会自动创建 maxwell 库

~~~sql
添加权限：
mysql> CREATE USER 'maxwell'@'%' IDENTIFIED BY '123456';
mysql> GRANT ALL ON maxwell.* TO 'maxwell'@'%';
mysql> GRANT SELECT, REPLICATION CLIENT, REPLICATION SLAVE ON *.* TO 'maxwell'@'%';
mysql> flush privileges; 
~~~

2.4、重启Mysql

~~~sql
systemctl start mysqld

#查看 binlog 相关配置
show global variables like '%log_bin%';

#查看详细的日志配置信息SHOW  GLOBAL VARIABLES LIKE '%log%';

#mysql数据存储目录 show variables like '%dir%';
~~~

![img](https://img2020.cnblogs.com/blog/1802007/202004/1802007-20200402220015965-212132274.png)

#### 2、安装Maxwell

`tar -zxvf maxwell-1.29.0.tar.gz -C /opt/module/`

##### 2.1、config.properties

~~~shel
log_level=info

producer=kafka
kafka.bootstrap.servers=master:9092 slave1:9092 slave2:9092

kafka_topic=maxwell

# mysql login info
host=master
user=root
password=123456

kafka.compression.type=snappy
kafka.retries=0
kafka.acks=1
~~~

##### 2.2、启动Maxwell

~~~bash
#创建 kafka topic  maxwell
kafka-topics.sh --zookeeper master:2181/kafka --create --replication-factor 1 --partitions 1 --topic maxwell

#启动MaxWell
/opt/module/maxwell-1.25.0/bin//maxwell --config /usr/local/src/maxwell/config.properties
注：  /usr/local/src/maxwell/config.properties  这个根据你自己的位置来修改
~~~



#### 3、测试 (先启动Kafka消费者，在插入Mysql数据，方便查看是否有数据)

##### 3.1、创建测试数据

~~~sql
#mysql中创建测试表 
create table test.maxwell_test(id int,name varchar(100));

#向maxwell_test插入数据
INSERT into maxwell_test values(1,'aa');
INSERT into maxwell_test values(2,'bb');
INSERT into maxwell_test values(3,'cc');
INSERT into maxwell_test values(4,'dd');
INSERT into maxwell_test values(5,'ee');

~~~

##### 3.2、消费Kafka数据
`./bin/kafka-console-consumer.sh --topic maxwell --bootstrap-server master:9092,slave1:9092,slave2:9092`
