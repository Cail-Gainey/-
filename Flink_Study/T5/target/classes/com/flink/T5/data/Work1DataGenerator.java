package com.flink.T5.data;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class Work1DataGenerator {

    private static final String TOPIC = "ProduceRecord";
    private static final String[] machineIds = {"1001", "1002", "1003", "1004"};
    private static final Random random = new Random();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:MysqlSink.SSS");

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "master:9092,slave1:9092,slave2:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        while (true) {
            String machineId = machineIds[random.nextInt(machineIds.length)];
            String produceTime = sdf.format(new Date());
            int changeHandleState = random.nextInt(2); // 0 or 1 for inspected or not
            String record = String.format("{\"machine_id\":\"%s\",\"produce_time\":\"%s\",\"change_handle_state\":%d}",
                    machineId, produceTime, changeHandleState);
            System.out.println(record);
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC, machineId, record);

            producer.send(producerRecord);

            try {
                Thread.sleep(5000); // 5秒间隔
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}