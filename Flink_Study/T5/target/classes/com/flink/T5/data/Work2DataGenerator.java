package com.flink.T5.data;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class Work2DataGenerator {

    private static final String TOPIC = "ChangeRecord";
    private static final String[] states = {"stopped", "maintenance", "idle", "running"};
    private static final Random random = new Random();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "master:9092,slave1:9092,slave2:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        while (true) {
            int machineId = random.nextInt(10) + 1; // 生成1到10之间的随机数作为machineId
            String changeTime = sdf.format(new Date());
            String lastState = states[random.nextInt(states.length - 1)];
            String currentState = states[random.nextInt(states.length)]; // 随机状态
//            String currentState = "running"; // 随机状态
            String record = String.format("{\"machine_id\":\"%d\",\"change_time\":\"%s\",\"last_state\":\"%s\",\"current_state\":\"%s\"}",
                    machineId, changeTime, lastState, currentState);
            System.out.println(record);
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC, String.valueOf(machineId), record);
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