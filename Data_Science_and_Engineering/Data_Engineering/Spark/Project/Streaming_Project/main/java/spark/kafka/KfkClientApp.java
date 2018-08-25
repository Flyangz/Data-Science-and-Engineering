package spark.kafka;

public class KfkClientApp {
    public static void main(String[] args) {
        new KfkProducer(KfkProperties.TOPIC).start();
        new KfkConsumer(KfkProperties.TOPIC).start();
    }
}
