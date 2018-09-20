# Hadoop生态组件

[TOC]

## Hadoop

三大核心HDFS, MapReduce, YARN

**运行**

`hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.9.1.jar wordcount input output` input（dir或file）和output（dir）

### HDFS
**组成**：NameNode、DataNode和secondary namenode

- NameNode：作为master，运行the file system namespace来管理元数据，包括文件名、副本数、block存放的DN（part-0, r:2,{1,3}）和响应client的请求，接收workers的heartbeating和blockreport
- DataNode：通常一个节点一个DataNode，管理数据（data block，存储在disk，包括数据本身和元数据）和处理master、client端的请求。数据只能write-once，除了append和truncate，每次只能一个writer进行。

> 建议两种node不在一台机器上。

- secondary namenode：备用master

代码

```bash
hadoop fs #查询可选的命令
hadoop fs -ls -R / #递归查看Hadoop根目录下的文件
          -mkdir -p /a/b #多层创建文件夹
          -put localpath remotepath
          -cat file #查看内容
          -get remotepath localpath
       jar jar包 className hdfs上的input output
```



### YARN
资源管理系统
The fundamental idea of YARN is to split up the **functionalities of resource management** and **job scheduling/monitoring** into separate daemons. 

**组成**：主要是ResourceManager(RM) + n个NodeManger(NM)

* Client
* ResourceManager: master。处理client请求；启动/监控ApplicationMaster；监控NM；资源分配
   分为scheduler(allocating resources to the various running applications) and applicationManager( accepting job-submissions, negotiating the first container for executing the application)
* ApplicationMaster: 每个app对应一个，负责app程序的管理。向RM申请资源和分配任务；向NM启停task；task的监控和容错
* NodeManger: slave。负责单节点资源管理（Containers）；反映情况；处理RM的命令；资源隔离
* Container: 一个进程的环境，可以运行ApplicationMaster或task。封装了资源信息，比如内存和cup等。

**执行**

client（client模式下为Spark driver）向RM提交任务，RM在一个NM上启动AM（cluster模式下为Spark driver），AM向RM申请资源（containers，如果第一次没有完全满足，后续有空闲会再通知AM），RM将资源的元数据信息发给AM，然后AM请求他NM启动container进程，并监控。（Spark的driver就可以向executors分配task）

查看日志：`yarn logs -applicationId <app ID>`

删除进程：` yarn application -kill applicationId`

### mapreduce（了解后转spark）

分布并行离线（磁盘，内存在线）
Shuffle： map到reduce的过程
HDFS - 分块block（128M） - （后面两步，不同文件类型不一样）FileInputForMat - .listFiles()获取文件 - 统计文件大小/获取文件信息 - 获取分片并且返回 - split（有10%冗余） - map() - 环形缓冲区（默认100M；在这里partition（决定发去不同的reducer），sort（本地分好组），spll to disk；可能有combiner， 压缩） - merge on disk - copy - 网线（shuffle） - merge on memory or disk（混合；有sort，分组） - reduce（） 

#### Mapper
```java
public class MyWordCount{
    public static class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
            // 以单词作为键
            public static Text k = new Text();
            // 以 1 作为 value
            public static IntWritable v = new IntWritable();
            //可以在此加个setup用于初始化
            protected void map(LongWritable key , Text value ,Context context )
                            throws IOException, InterruptedException {
                    //获取文件名(多个文件输入时用)
                    InputSplit is = context.getInputSplit();
                    String fileName = ((FileSplit)is).getPath().getName();
                    
                    String line = value.toString();
                    String [] words - line.split(" ");
                    for ( String word : words){
                            k.set(word);
                            v.setJ(1);
                            context.write(k, v);
                   }
           }
    }
    //可以加个cleanup，用于执行后
}
```
#### Combiner
如果map后的key有很多相同的，可以在map后用combiner预处理
```java
public class combiner extend Reduce<Text, Text, Text, Text>{
        protected void reduce(Text key , Iterable<IntWritable> values ,Context context )
                        throws IOException, InterruptedException {
                String str[] = key.toString().split("_");
                int counter = 0;
                for (Text t : value){
                    counter += Integer.parseInt(t.toString());
                }
                context.write(new Text(str[0]), new Text(str[1]+":"+counter));
```
下面reducer也有starup和cleanup
#### Reducer
```java
public static class WorldCountReduce extends Reducer<Text, IntWritable, Text, IntWritable>{
        public static final int TOP_N = 3;
        TreeSet<TopNWritable> ts = new TreeSet<TopNWritable>();
        @Override
        protected void reduce(Text key , Iterable<IntWritable> values ,Context context )
                        throws IOException, InterruptedException {
                int counter = 0;
                for (IntWritable i : ite ){
                        counter += i .get();
               }
                context.write(key , new IntWritable( counter ));
       }
       //构造最终输出类型（自定义数据类型时用）
       TopNWritable tn = new TopNWritable(key.toString(), counter);
       ts.add(tn);
       //如果ts里面的数据大于TOP_N的时候移除其他
       if(ts.size()>TOP_N){
            ts.remove(ts.last());
        }
       protexted void cleanup(Context context)
            throws IOException, InterruptedException {
            for (TopNWritable tn : ts){
                context.write(tn, NullWritable.get());
            }
        }   
}
```
多个输出时加上这种class
```java
public class Partitioner extends Partitioner<Text, Text>{
    public int get Partition(Text key, Text value, int numPartitions){
        String firstChar = key.toString().substring(0,1);
        if(firstChar.matches("^[A-Z]")){
            return 0%numPartitions；
        }else if(firstChar.matches("^[a-z]")){
            return 1%numPartitions；
        }else if(firstChar.matches("^[0-9]")){
            return 2%numPartitions；
        }else {
            return 3%numPartitions；
```
#### main

```java
public static void main(String[] args ) {
               //获取配置对象信息
               Configuration config = new Configuration();
               Job job = Job.getInstance(config, "name");
               //job的运行主类
               job.setJarByClass(RunJob.class);
               //Map-Reduce
               job.setMapperClass(WordCountMapper.class);
               job.setCombinerClass(Combiner.class);
               job.setReducerClass(WorldCountReduce.class);
               
               job.setMapOutputKeyClass(Text.class);
               job.setMapOutputValueClass(IntWritable.class);
                
                //添加分区信息
                job.setPartitionerClass(Partitioner.class); // 另外写的class
                job.setNumReduceTasks(4);
//输入和输出设置              FileInputFormat.addInputPath(job, new Path(args[0]));
               FileOutputFormat.setOutputPath(job, new Path(args[1]));

//提交job打印信息且完成后退出
System.exit(job.waitForCompletion(true) ? 0 : 1);
}
```
上面写好后
在shell里
```
//引入写好的包
export HADOOP_CLASSPATH=hadoop-examples.jar
//运行
hadoop MaxTemperature input/ncdc/sample.txt output
```
补充自定义数据类型
```
自定义数据类型implements Writable(不能排序）、或WritableComparable（可排序）
方法为write, readFields, compareTo。前两者里的字段个数和顺序要一致(例子中的word和counter)
可以重写toString, equals, hashcode等方法

public class TopNWritable implements WritableComparable<TopNwritable>{
    public String word;
    public int counter;
    public TopNWritable(){}
    #然后自动生成构造器
    //序列化
    public void write(DataOutput out) throws IOException{
        out.writeUTF(this.word); //string用UTF
        out.writeInt(this.counter);
    }
    //反序列化
    public void readFields(DataOutput out) throws IOException{
        this.word = in.readUTF();
        this.counter = in.readInt();
    }
    
    public int compareTo(TopNWritable o){
        return o.counter - this.counter; //倒排，顺排对调减
    }
    //下面还可加toString, equals, hashcode
    
```
还有join，压缩等内容...

## Hive(了解，转spark查询)
建立在Hadoop上的数据仓库，定义了类似SQL的查询语言，通过它来读写和管理分布式存储的数据。它的底层执行引擎可以是MapReduce、Spark等。

优点：简单，扩展性好（计算和存储），统一的元数据管理（它创建的表或其他组件，如Spark创建的表都是通用的）

**架构**
访问：命令行shell, jdbc, web gui
Driver：对访问的代码进行转化，生成执行代码，然后就可以连接到计算引擎来对HDFS进行查询。
Metastore：元数据存储，针对关系型数据库。

Hive在集群当中只充当client，在一台机器部署就可以了。

## Flume

组成：source，channel和sink

event为数据单元

```bash
# Name the components on this agent
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# Describe/configure the source
# 端口
a1.sources.r1.type = netcat 
a1.sources.r1.bind = localhost
a1.sources.r1.port = 44444

# 文件
a1.sources.r1.type = exec 
a1.sources.r1.command = tail -F /Users/flyang/test/flume.log
a1.sources.r1.shell = /bin/sh -c

# 中转，sink为hostname，source为bind
exec-memory-avro.sinks.avro-sink.type = avro
exec-memory-avro.sinks.avro-sink.hostname = localhost
exec-memory-avro.sinks.avro-sink.port = 44444

avro-memory-logger.sources.avro-source.type = avro
avro-memory-logger.sources.avro-source.bind = localhost
avro-memory-logger.sources.avro-source.port = 44444

# Describe the sink
a1.sinks.k1.type = logger

# Use a channel which buffers events in memory
a1.channels.c1.type = memory

# Bind the source and sink to the channel
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

```bash
flume-ng agent \
--conf $FLUME_HOME/conf \
--conf-file example.conf \
--name a1 \
-Dflume.root.logger=INFO,console
```

```bash
#模版
avro source + memory channel + kafka sink

# avro-memory-kafka.conf

agent1.sources = avro-source
agent1.sinks = kafka-sink
agent1.channels = memory-channel

agent1.sources.avro-source.type = avro
agent1.sources.avro-source.bind = localhost
agent1.sources.avro-source.port = 41414

agent1.sinks.kafka-sink.type = org.apache.flume.sink.kafka.KafkaSink
agent1.sinks.kafka-sink.kafka.topic = streaming_topic
agent1.sinks.kafka-sink.brokerList = localhost:9092
agent1.sinks.kafka-sink.batchSize = 5
agent1.sinks.kafka-sink.requiredAcks = 1

agent1.channels.memory-channel.type = memory

agent1.sources.avro-source.channels = memory-channel
agent1.sinks.kafka-sink.channel = memory-channel


=====push=====
simple-agent.sources = netcat-source
simple-agent.sinks = avro-sink
simple-agent.channels = memory-channel

simple-agent.sources.netcat-source.type = netcat
simple-agent.sources.netcat-source.bind = localhost
simple-agent.sources.netcat-source.port = 44444

simple-agent.sinks.avro-sink.type = avro
simple-agent.sinks.avro-sink.hostname = 127.0.0.1
simple-agent.sinks.avro-sink.port = 41414

simple-agent.channels.memory-channel.type = memory

simple-agent.sources.netcat-source.channels = memory-channel
simple-agent.sinks.avro-sink.channel = memory-channel

flume-ng agent \
--conf $FLUME_HOME/conf \
--conf-file flume_push_streaming.conf \
--name simple-agent \
-Dflume.root.logger=INFO,console

=====poll=====
simple-agent.sources = netcat-source
simple-agent.sinks = spark-sink
simple-agent.channels = memory-channel

simple-agent.sources.netcat-source.type = netcat
simple-agent.sources.netcat-source.bind = localhost
simple-agent.sources.netcat-source.port = 44444

simple-agent.sinks.spark-sink.type = org.apache.spark.streaming.flume.sink.SparkSink
simple-agent.sinks.spark-sink.hostname = localhost
simple-agent.sinks.spark-sink.port = 41414

simple-agent.channels.memory-channel.type = memory

simple-agent.sources.netcat-source.channels = memory-channel
simple-agent.sinks.spark-sink.channel = memory-channel

flume-ng agent \
--conf $FLUME_HOME/conf \
--conf-file flume_poll_streaming.conf \
--name simple-agent \
-Dflume.root.logger=INFO,console
```



## Kafka

消息队列和发布/订阅（Kafka通过Topic的方式，使得消息数据可分开处理，通过Consumer Group实现发布/订阅。）

存储信息流，具备兼容性

实时处理信息

**发布订阅信息系统一般规则**：消费者可订阅多个topic（消息队列），同一条数据可被多个消费者消费，消息被消费后不会被立刻删除。

### 概念

producer（发布者，发布消息到话题的任何对象），consumer（订阅者，少于等于partition数目），broker（Kafka集群中的每个节点，存储Topic的一个或多个Partitions），topic

Topic：消息队列，partition是基本存储单元。partition在一个节点上（太大会多个节点）且可设置Replica。一个partition对应一个单独日志，内部多个Segment，Payload不断地被追加到最后一个Segment末尾。

Consumer Group：对于同一个topic，会广播给不同的group，一个group中，consumer协助消费一个Topic，保证一个partition只会被一个consumer消费，所以partition会被有序消费。consumer出现故障，组内其他consumer接管。

Replication Leader：负责Partition上与Producer和Consumer交互。Followers做备份。

ReplicationManager：负责管理当前broker所有分区和副本的信息、KafkaController发起的请求、副本状态切换、添加/读取信息等。

API：Producer、Consumer、Streams、Connectors

消息单元Payload结构：

| Offset | Length | CRC32 | Magic | attributes | Timestamp | Key Length | Key  | Value Length | Value |
| ------ | ------ | ----- | ----- | ---------- | --------- | ---------- | ---- | ------------ | ----- |
| 4      | 4      | 4     | 1     | 1          | 8         | 4          | 不限 | 4            | 不限  |

> CRC32是校验字段，确保完整性。Magic判断是否是Kafka的消息。

实际上是一个字节数组，并不关心实际格式（上面key和value不限大小的字节数组）。key是optional，有key时默认hash分派，没有就是轮询分派。

payload都是以批量方式写入Kafka，按大小或timeout作为触发。

Zookeeper的作用：Kafka依赖于zookeeper来保证系统可用性集群保存一些meta信息（表，记录各个节点IP、端口信息、partition分布等）；建立起生产者和消费者的订阅关系，并实现生产者与消费者的负载均衡。

```bash
# 启动
zkServer.sh start
kafka-server-start.sh $KAFKA_HOME/config/server.properties
#创建topic
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka-example
#查看topics
kafka-topics.sh --list --zookeeper localhost:2181
#发送消息
kafka-console-producer.sh --broker-list localhost:9092 --topic kafka_streaming_topic
#接收信息
kafka-console-consumer.sh --zookeeper localhost:2181 --topic merchants-template --from-beginning
#查看topic信息
kafka-topics.sh --describe --zookeeper localhost:2181 --topic hello_topic

#单节点多broker
kafka-server-start.sh -daemon $KAFKA_HOME/config/server-1.properties &
kafka-server-start.sh -daemon $KAFKA_HOME/config/server-2.properties &
kafka-server-start.sh -daemon $KAFKA_HOME/config/server-3.properties 

kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic my-replicated-topic
```



### 配置

**broker**

`broker.id`, `port`, `zookeeper.connect`, `log.dirs`同一个分区的文件会在相同的文件夹，多个路径会均衡, `num.recovery.threads.per.data.dir`每个log文件目录的线程数，这个值乘上面的值就是总的线程数,`auto.create.topics.enable`一般设为false

**topic** 

`number.partitions`根据消费者处理速度和topic的产出速度确定

`log.segment.bytes` 每个segment的最大数据容量，默认1G。超过后会另起一个，旧的超过一定时间会被删除。同样根据生产者和消费者的速度调整。

`log.segment.ms`segment强制关闭的时间，默认为null。如果有大量partition，且在未达到大小上限时达到关闭时间，会同时关闭全部，性能销毁大。

`message.max.bytes`broker对接收的payload的最大数据量限制（压缩后的，默认1M）。也有限制消费者获取消息的大小的参数。



### 流程

简单流程：生产者push给broker，消费者pull存在broker的数据。生产者和消费者需要的broker地址信息都在zookeeper中。

broker中有一个controller，负责维持集群中leader的存在（某个broker挂掉后，看是否为leader，如果是，则会选出新leader）管理新broker的加入（该broker之前是否有处理过某些分区的信息，有的话让它去同步）

**primary-backup复制机制**

当leader brokers接收后，follower会从leader brokers中拉取数据。`acks`中设计多少个follower拉取成果才算成功。

**发送消息基本流程**：

- new ProducerRecord，包含topic、value、[key]、[partition]
- 通过send()发送，包括序列化key和value，分区规划，将相同topic和partition的放到一起，又另一个线程负责发送给broker。
- broker成功接收数据，会返回一个包含topic、partition和offset的RecordMetadata对象，否则返回异常，生产者会对异常进行处理。



### 生产者配置和发送方式

`bootstrap.servers`一个host:port的broker列表，建议至少两个，防止一个broker down掉。

`key.serializer`和`value.serializer`进行序列化的类，要实现`org.apache.kafka.common.serialization.Serializer`接口，有几个默认实现，如ByteArray, String, Integer

`acks`多少个副本被写入后才算成功写入。可选0，1（leader写入成功为成功），all

`buffer.memory`缓冲发送消息的内存大小。send调用过慢，超过该值会异常。默认1M，一般都足够。

`compression.type`一般用snappy

`retries`

`batch.size`当多条信息要发送到一个分区时，生产者会才用批量发送。

`linger.ms`发送批量消息的等待时间。Kafka默认一空闲就发送。

`client.id`用于识别消息来自哪个客户端

`max.request.size`限制生产者发送消息的大小。broker也有接收信息的大小设置。

发送方式：fire-and-forget, Synchronous send, Asynchronous send

```java
public class MyConsumer {

    private static KafkaProducer<String, String> producer;

    static {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //自定义分区器
        props.put("partitioner.class", "partitioner.CustomPartitioner");
        producer = new KafkaProducer<>(props);
    }

    private static void sendMessageForgetResult(){

        ProducerRecord<String, String> record = new ProducerRecord<>(
                "kafka", "name", "forgetresult");
        producer.send(record);
        producer.close();
    }

    private static void sendMessageSync()throws Exception{

        ProducerRecord<String, String> record = new ProducerRecord<>(
                "kafka", "name", "sync");
        RecordMetadata result = producer.send(record).get();
        System.out.println(result.topic());
        System.out.println(result.partition());
        System.out.println(result.offset());
        producer.close();
    }

    private static void sendMessageCallback(){

        ProducerRecord<String, String> record = new ProducerRecord<>(
                "kafka", "name", "callback");
        producer.send(record, new MayProducerCallback());
        producer.close();
    }

    private static class MayProducerCallback implements Callback {
        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) {
                exception.printStackTrace();
                return;
            }

            System.out.println(metadata.topic());
            System.out.println(metadata.partition());
            System.out.println(metadata.offset());
        }
    }
}
```

### 消费者配置和位移提交

`fetch.min.bytes`当消费者写入量少，且消费者个数多时，提高。

`fetch.max.wait.ms`默认500ms

`max.partition.fetch.bytes`每个分区返回的最大字节数，1M。如果一个topic的分区数/消费者数 x 1M，实际情况会设置更高，应对某些分区down机情况。要比broker能够接收的数据大小要大。

`session.timeout.ms`3s，如果3s没有会话，认为该消费者down掉，会触发分区重平衡。

`enable.auto.commit`默认true

`partition.assignment.strategy`范围或均匀。前者默认，如果消费者不能整除分区数，最后一个消费者只会处理余数的分区。例如c1和c2处理t1和t2，每个topic有3个partition，那么c1会处理每个topic的前两个分区。

`client.id`

`max.poll.records`

**提交位移方式**：自动提交（最常用）、手动提交当前位移、手动异步提交当前位移、手动异步提交当前位移带回调、混合同步与异步提交位移

重平衡问题：重复消费或丢失信息



### 补充

**请求处理原理**

请求类型、版本（能处理不同版本的请求）、关联id、客户端id（请求来源）

生成请求：由生产者发送，包含写入的信息。

拉取请求：消费者和follower发送。

客户端有MetaData Cache来保存谁是leader的信息。

应用场景：信息队列、行为跟踪、元信息监控、日志收集、流处理





## Hbase

没有列数的限制，百万行数据不需要hbase。

建立在HDFS之上，提供对数据的随机准时读/写访问（低延迟访问单个行记录）HDFS只能顺序访问，而Hbase使用哈希表和提供随机接入，并且其存储索引。适合于存储非结构化的数据，基于列的模式。rowkey行健：是hbase表自带的，每个行健对应一条数据。family列族：是创建表时指定的，为qualifier列的集合，每个列族作为一个文件单独存储，存储的数据都是字节数组，其中的数据可以有很多，通过时间戳来区分。

特点：

- 量大：关系型数据库单表一般不会超500万，列不过30；Hbase单表可达百亿行、百万列。

- 面向列，列的多版本（不同时间段插入的数据）
- 稀疏性、扩展性、高可靠
- 高性能：写入，LSM数据接口和Rowkey有序排列；读取，region 切分、主键索引、缓存机制。高并发读写、列动态增加、数据自动切分
- 缺点：不支持条件查询

构成：master、RegionServer

创建的时候只需指定列族，具体列不需指定，会动态增加。列族最好不超过5个，其中的列数没有限制。列只有插入数据后存在，列在列族中是有序的。

```bash
# 启动
zkServer.sh start
start-dfs.sh
start-hbase.sh
hbase shell

create 'table_name', 'col_family'
describe 'table_name' #看表信息
scan 'table' #看表数据
put 'table_name', 'row_key', 'info:col_name', 'value'
disable 'table_name'
get 'table_name', 'row_key', 'info:col_name'
```

row key, row, cell family
两个维度划分，每个cell family都是一个file

## zookeeper

管理分布式服务，强一致性，通常指保证yarn的容错性，即当yarn挂掉后，由zookeeper来启动新的yarn，并保证新的yarn和挂掉的yarn的信息一致。
架构：
奇数台servers，一个leader，其他servers
快速选举机制，原子广播协议
数据模型：树形，携带少量数据

## Azkaban

一个工作流管理框架，可通过job dependencies解决工作顺序，并提供页面来帮助维护和跟踪工作流。

架构： 关系型数据库、WebServer、ExecutorServer

WebServer利用DB实现：项目管理、执行流状态、之前的job、scheduler、遵循sla规则

ExecutorServer利用DB实现：项目访问、读取和更新执行流的数据、日志、内部流依赖

模式：单server，双server，分布式多executor

```bash
# foo.job
type=command
command=echo foo

# bar.job
type=command
dependencies=foo
command=echo bar
```