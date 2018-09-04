# Streaming Project

---

## Basic Knowledge

web/app -> webServer -> Flume -> Kafka -> Spark/ Storm -> RDBMS/ NoSQL -> visualization

### Assembly

**Spark Streaming与flume**

push：先启动Spark Streaming作业，它会启动一个端口。然后才启动flume来接上这个sink端口。最后通过telnet接上flume的source端口来测试。

poll：与上面相反，先启动flume后启动Streaming。



**Spark Streaming与kafka**

Receiver：数据可能丢失，可用Write Ahead Logs解决

Direct（一般用这个）：不需要receiver，利用kafka的API来读取数据，由kafka来防治数据丢失。



**flume-kafka-streaming**

测试时：

在idea编写log4j.properties，确定日志输出到flume的端口

编写flume的avor-memory-kafka.conf

在idea编写kafka-streaming代码。

启动flume，kafka，运行LoggerGenerator类和kafka-streaming代码

生产中：

打包LoggerGenerator的jar运行

flume和kafka一样

打包kafka-streaming后用spark-submit

---

## Complete Project

编写python脚本，利用crontab周期性地产生日志。具体脚本参考LogGenerator.ipynb

```bash
crontab -e 
*/1 * * * * /Users/Name/log_genertor.sh
```

编写Flume conf，连接该日志文件，并传递给Kafka

```bash
exec-memory-kafka.sources = exec-source
exec-memory-kafka.sinks = kafka-sink
exec-memory-kafka.channels = memory-channel

exec-memory-kafka.sources.exec-source.type = exec
exec-memory-kafka.sources.exec-source.command = tail -F /Users/flyang/test/log_gener
exec-memory-kafka.sources.exec-source.shell = /bin/sh -c

exec-memory-kafka.sinks.kafka-sink.type = org.apache.flume.sink.kafka.KafkaSink
exec-memory-kafka.sinks.kafka-sink.kafka.topic = streaming_topic
exec-memory-kafka.sinks.kafka-sink.brokerList = localhost:9092
exec-memory-kafka.sinks.kafka-sink.batchSize = 5
exec-memory-kafka.sinks.kafka-sink.requiredAcks = 1

exec-memory-kafka.channels.memory-channel.type = memory

exec-memory-kafka.sources.exec-source.channels = memory-channel
exec-memory-kafka.sinks.kafka-sink.channel = memory-channel
```

启动Kafka和消费者

```bash
# 启动kafka
zkServer.sh start
kafka-server-start.sh $KAFKA_HOME/config/server.properties

# 启动消费者
kafka-console-consumer.sh --zookeeper localhost:2181 --topic streaming_topic 
```

编写Spark代码，包括数据处理和写入Hbase。详细查看Real_Time_Streaming_Process.ipynb
