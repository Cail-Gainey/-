package com.flink.t2.work;

import com.flink.t2.pojo.OrderInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisConfigBase;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

import java.util.Properties;

public class T2_Work3 {
    private static final String BOOTSTRAP_SERVERS = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "test";
    private static final Gson gson = new Gson();
    private static Integer orderCount = 0;
    private static Integer quitCount = 0;

    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);

        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<>(TOPIC, new SimpleStringSchema(), prop));

        DataStream<String> data = source.map(new MapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                JsonObject jsonObject = gson.fromJson(s, JsonObject.class);
                OrderInfo order = gson.fromJson(jsonObject, OrderInfo.class);
                String status = order.getOrder_status();
                if (status.equals("1001")) {
                    orderCount++;
                } else if (status.equals("1003")) {
                    quitCount++;
                }
                double cancelRate = (double) quitCount / orderCount * 100;
                return String.format("%.1f%%", cancelRate);
            }
        });

        data.print();

        data.addSink(new RedisSink<>(getRedisConfig(), new RedisMapper<String>() {
            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.SET, "cancelrate");
            }

            @Override
            public String getKeyFromData(String data) {
                return "cancelrate";
            }

            @Override
            public String getValueFromData(String data) {
                return data;
            }
        }));

        try {
            env.execute("T2_Work3");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FlinkJedisConfigBase getRedisConfig() {
        return new FlinkJedisPoolConfig.Builder()
                .setHost("master")
                .setPort(6379)
                .build();
    }
}
