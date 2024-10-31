package com.flink.T5.data;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class Work3DataGenerator {

    private static final String TOPIC = "ChangeRecord";
    private static final String[] machineIds = {"1001", "1002", "1003", "1004"};
    private static final String[] states = {"正常", "警告"};
    private static final Random random = new Random();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "master:9092,slave1:9092,slave2:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        while (true) {
            String machineId = machineIds[random.nextInt(machineIds.length)];
            String warningTime = sdf.format(new Date());
            String state = states[random.nextInt(states.length)];
            String record = String.format("{\"machine_id\":\"%s\",\"warning_time\":\"%s\",\"state\":\"%s\"}",
                    machineId, warningTime, state);
            System.out.println(record);
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC, machineId, record);
            producer.send(producerRecord);

            try {
                Thread.sleep(5000); // 5秒间隔
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // producer.close(); // 不建议在循环中关闭producer
    }
}