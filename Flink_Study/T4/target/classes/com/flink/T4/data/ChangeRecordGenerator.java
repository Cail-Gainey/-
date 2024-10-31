package com.flink.T4.data;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class ChangeRecordGenerator {
    private static final String TOPIC = "ChangeRecord";
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
                String deviceId = String.valueOf(random.nextInt(10));
                // 随机生成设备状态（正常 或 预警）
                String state = random.nextBoolean() ? "正常" : "预警";
                // 当前时间作为 change_start_time
                String changeStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                // 生成随机状态变化记录
                String record = String.format("{\"device_id\":\"%s\",\"state\":\"%s\",\"change_start_time\":\"%s\"}", deviceId, state, changeStartTime);
                producer.send(new ProducerRecord<>(TOPIC, deviceId, record));

                System.out.println("Sent record: " + record);

                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
