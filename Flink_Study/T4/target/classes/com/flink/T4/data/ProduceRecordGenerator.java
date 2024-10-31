package com.flink.T4.data;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;

public class ProduceRecordGenerator {
    private static final String TOPIC = "ProduceRecord";
    private static final String BOOTSTRAP_SERVERS = "master:9092,slave1:9092,slave2:9092";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        Random random = new Random();

        while (true) {
            try {
                // 随机生成设备ID（1000到9999之间的随机数字）
                String deviceId = String.valueOf(1000 + random.nextInt(9000));
                // 随机生成是否已检验（0 或 1）
                int changeHandleState = random.nextInt(2);

                // 生成随机产品生产记录
                String record = String.format("{\"device_id\":\"%s\",\"change_handle_state\":%d}", deviceId, changeHandleState);
                producer.send(new ProducerRecord<>(TOPIC, deviceId, record));

                System.out.println(record);


                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
