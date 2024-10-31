package com.flink.t2.work;

import com.flink.t2.pojo.OrderInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisConfigBase;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

import java.util.Properties;

public class T2_Work2 {
    private static final String BOOTSTRAP_SERVER = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "test";
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        // Create the Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Set Kafka consumer properties
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVER);

        // Add Kafka source to the data stream
        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<>(TOPIC, new SimpleStringSchema(), prop));

        // Process and filter the stream to get order status "1005" and count them every minute
        SingleOutputStreamOperator<Integer> countStream = source
                .map(new OrderInfoMapFunction())  // Map string to OrderInfo object
                .filter(order -> "1005".equals(order.getOrder_status()))  // Filter for order status "1005"
                .map(order -> 1)  // Convert each valid order to the integer value 1
                .windowAll(TumblingProcessingTimeWindows.of(Time.minutes(1)))  // Use a tumbling window of 1 minute
                .sum(0);  // Sum all 1s to get the count per minute

        // Print the count to the console once every minute
        countStream.print();

        // Add a Redis sink to store the count per minute in Redis
        countStream.addSink(new RedisSink<>(getRedisConfig(), new RedisMapper<Integer>() {
            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.SET, "countPerMinute");
            }

            @Override
            public String getKeyFromData(Integer data) {
                return "countPerMinute";
            }

            @Override
            public String getValueFromData(Integer data) {
                return data.toString();
            }
        }));

        // Execute the Flink job
        try {
            env.execute("T2_Work2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // MapFunction to convert Kafka JSON strings to OrderInfo objects
    private static class OrderInfoMapFunction implements MapFunction<String, OrderInfo> {
        @Override
        public OrderInfo map(String s) {
            if (s == null || s.isEmpty()) {
                return new OrderInfo(); // Return a default OrderInfo object
            }
            JsonObject jsonObject = gson.fromJson(s, JsonObject.class);
            return gson.fromJson(jsonObject, OrderInfo.class);
        }
    }

    // Method to configure Redis connection details
    private static FlinkJedisConfigBase getRedisConfig() {
        return new FlinkJedisPoolConfig.Builder()
                .setHost("master")
                .setPort(6379)
                .build();
    }
}
