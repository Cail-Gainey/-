package com.flink.t2.data;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class DataGenerator {

    private static final String CREATED = "1001";
    private static final String PAID = "1002";
    private static final String CANCELED = "1003";
    private static final String COMPLETED = "1004";
    private static final String RETURN_REQUESTED = "1005";
    private static final String RETURN_COMPLETED = "1006";

    private static final Random RANDOM = new Random();
    private static final Map<String, String> orderStatusMap = new HashMap<>();
    private static final Map<String, Long> orderLastOperateTimeMap = new HashMap<>(); // 用于跟踪每个订单的最后操作时间

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "master:9092,slave1:9092,slave2:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        String topic = "test";
        int batchCount = 0;
        int totalOrders = 10;

        while (batchCount < 100) {

            String orderId;
            String orderStatus;

            if (!orderStatusMap.isEmpty() && RANDOM.nextInt(100) < 80) {
                orderId = pickRandomExistingOrder();
                orderStatus = getNextOrderStatus(orderId);
            } else if (orderStatusMap.size() < totalOrders) {
                orderId = "order_" + orderStatusMap.size();
                orderStatus = CREATED;
                orderStatusMap.put(orderId, orderStatus);
                orderLastOperateTimeMap.put(orderId, System.currentTimeMillis()); // 初始化操作时间
            } else {
                orderId = pickRandomExistingOrder();
                orderStatus = getNextOrderStatus(orderId);
            }

            if (orderStatus == null) {
                continue;
            }

            long createTime = orderLastOperateTimeMap.get(orderId);
            Long operateTime = null;

            if (PAID.equals(orderStatus)) {
                // 确保支付时间在创建时间之后
                operateTime = createTime + RANDOM.nextInt(100000);
            } else if (RETURN_REQUESTED.equals(orderStatus)) {
                // 申请退回时间需要在支付时间之后
                operateTime = orderLastOperateTimeMap.get(orderId) + RANDOM.nextInt(100000);
            }

            long eventTime = (operateTime != null && operateTime > createTime) ? operateTime : createTime;

            // 生成随机订单数据
            String orderInfo = String.format(
                    "{\"order_id\":\"%s\", \"order_status\":\"%s\", \"create_time\":%d, \"operate_time\":%s, \"event_time\":%d}",
                    orderId, orderStatus, createTime, (operateTime != null ? operateTime : "null"), eventTime
            );

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, orderId, orderInfo);
            producer.send(record);

            System.out.println("生成订单: " + orderInfo);

            // 更新订单状态和最后操作时间
            orderStatusMap.put(orderId, orderStatus);
            orderLastOperateTimeMap.put(orderId, eventTime); // 更新最后操作时间

            batchCount++;

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();
    }

    private static String getNextOrderStatus(String orderId) {
        String currentStatus = orderStatusMap.get(orderId);

        switch (currentStatus) {
            case CREATED:
                return RANDOM.nextInt(100) < 50 ? PAID : CANCELED;

            case PAID:
                int choice = RANDOM.nextInt(100);
                if (choice < 70) return COMPLETED;
                if (choice < 85) return RETURN_REQUESTED;
                return CANCELED;

            case COMPLETED:
                return RANDOM.nextInt(100) < 10 ? RETURN_REQUESTED : null;

            case RETURN_REQUESTED:
                return RETURN_COMPLETED;

            case CANCELED:
            case RETURN_COMPLETED:
                return null;

            default:
                return null;
        }
    }

    private static String pickRandomExistingOrder() {
        Object[] orders = orderStatusMap.keySet().toArray();
        return (String) orders[RANDOM.nextInt(orders.length)];
    }
}
