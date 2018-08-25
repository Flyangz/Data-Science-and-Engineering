package spark.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KfkConsumer extends Thread {

    private String topic;
    private KafkaConsumer consumer;

    public KfkConsumer(String topic) {
        this.topic = topic;

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "10");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        this.consumer = new KafkaConsumer(props);
    }

//    @Override
//    public void run() {
//
//        consumer.subscribe(Arrays.asList(topic));
//        while (true) {
//            ConsumerRecords<String, String> records = consumer.poll(1);
//            for (ConsumerRecord<String, String> record : records) {
//                System.out.printf("offset2222222 = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
//                System.out.println("++++++");
//
//            }
//        }
//    }
}
