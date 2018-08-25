package spark.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KfkProducer extends Thread {

    private String topic;
    private KafkaProducer producer;

    public KfkProducer(String topic) {

        this.topic = topic;

        Properties props = new Properties();
        props.put("bootstrap.servers", KfkProperties.BOOTSTRAP);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("acks", "1");

        this.producer = new KafkaProducer(props);
    }

    @Override
    public void run() {

        int messageNo = 1;

        while (true) {
            String message = "message " + messageNo;
            producer.send(new ProducerRecord<Integer, String>(topic, message));
            System.out.println("Sent: " + message);

            messageNo++;

            try {
                Thread.sleep(20000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
