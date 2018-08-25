# Hadoop
三大核心HDFS, MapReduce, YARN

### 运行
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

client（client模式下为Spark driver）向RM提交任务，RM在一个NM上启动AM（cluster模式下为Spark driver），AM向RM申请资源（containers），然后向其他NM分配和启动进程，并监控。（Spark的driver就可以向executors分配task）

查看日志：`yarn logs -applicationId <app ID>`

### mapreduce（了解后转spark）
分布并行离线（磁盘，内存在线）
Shuffle： map到reduce的过程
HDFS - 分块block（128M） - （后面两步，不同文件类型不一样）FileInputForMat - .listFiles()获取文件 - 统计文件大小/获取文件信息 - 获取分片并且返回 - split（有10%冗余） - map() - 环形缓冲区（默认100M；在这里partition（决定发去不同的reducer），sort（同时分好组？），spll to disk；可能有combiner， 压缩） - merge on disk - copy - 网线（shuffle） - merge on memory or disk（混合；有sort，分组） - reduce（） 
#### Mapper
```
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
#### combiner
如果map后的key有很多相同的，可以在map后用combiner预处理
```
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
```
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
```
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
#### 驱动
```
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

# Hive(了解，转spark查询)
建立在Hadoop上的数据仓库，定义了类似SQL的查询语言，通过它来读写和管理分布式存储的数据。它的底层执行引擎可以是MapReduce、Spark等。

优点：简单，扩展性好（计算和存储），统一的元数据管理（它创建的表或其他组件，如Spark创建的表都是通用的）

**架构**
访问：命令行shell, jdbc, web gui
Driver：对访问的代码进行转化，生成执行代码，然后就可以连接到计算引擎来对HDFS进行查询。
Metastore：元数据存储，针对关系型数据库。

Hive在集群当中只充当client，在一台机器部署就可以了。

# Flume

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



# Kafka

概念：producer，consumer，broker，topic

每条信息由key，value和timestamp组成

```bash
# 启动
zkServer.sh start
kafka-server-start.sh $KAFKA_HOME/config/server.properties
#创建topic
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic streaming_topic
#查看topics
kafka-topics.sh --list --zookeeper localhost:2181
#发送消息
kafka-console-producer.sh --broker-list localhost:9092 --topic kafka_streaming_topic
#接收信息
kafka-console-consumer.sh --zookeeper localhost:2181 --topic streaming_topic 
#查看topic信息
kafka-topics.sh --describe --zookeeper localhost:2181 --topic hello_topic


#单节点多broker
kafka-server-start.sh -daemon $KAFKA_HOME/config/server-1.properties &
kafka-server-start.sh -daemon $KAFKA_HOME/config/server-2.properties &
kafka-server-start.sh -daemon $KAFKA_HOME/config/server-3.properties 

kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic my-replicated-topic
```



```JAVA
public class KfkProperties {

    public static final String ZK = "localhost:2181";
    public static final String TOPIC = "hello_topic";
    public static final String BOOTSTRAP = "localhost:9092";
}

public class KfkProducer {

    private String topic;
    private KafkaProducer producer;

    public KfkProducer(String topic) {

        this.topic = topic;

        Properties props = new Properties();
        props.put("bootstrap.servers", KfkProperties.BOOTSTRAP);
        props.put("key.serializer", "kafka.serializer.StringEncoder");
//        properties.put()
        producer = new KafkaProducer(props);
    }
}
```

# Hbase
建立在HDFS之上，提供对数据的随机实时读/写访问（低延迟访问单个行记录）
HDFS只能顺序访问，而Hbase使用哈希表和提供随机接入，并且其存储索引

```bash
# 启动
zkServer.sh start
start-dfs.sh
start-hbase.sh
hbase shell
```

row key, row, cell family
两个维度划分，每个cell family都是一个file

# sqoop

数据转移：hadoop和structured datastores之间

# zookeeper

管理分布式服务，强一致性，通常指保证yarn的容错性，即当yarn挂掉后，由zookeeper来启动新的yarn，并保证新的yarn和挂掉的yarn的信息一致。
架构：
奇数台servers，一个leader，其他servers
快速选举机制，原子广播协议
数据模型：树形，携带少量数据



