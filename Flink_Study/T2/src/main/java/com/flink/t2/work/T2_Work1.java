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

public class T2_Work1 {
    private static final String BOOTSTRAP_SERVER = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "test";

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVER);

        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<>(TOPIC, new SimpleStringSchema(), prop));

        DataStream<Integer> OrderFilter = source.map(new OrderInfoMapFunction());

        OrderFilter.print();

        FlinkJedisConfigBase jedisConfig = new FlinkJedisPoolConfig.Builder()
                .setHost("master")
                .setPort(6379)
                .build();

        OrderFilter.addSink(new RedisSink<>(jedisConfig, new RedisMapper<Integer>() {
            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.HSET, "totalcount");
            }

            @Override
            public String getKeyFromData(Integer data) {
                return "count";
            }

            @Override
            public String getValueFromData(Integer data) {
                return data.toString();
            }
        }));

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class OrderInfoMapFunction implements MapFunction<String, Integer> {
        private int count = 0;

        @Override
        public Integer map(String s) throws Exception {
            if (s == null || s.isEmpty()) {
                return 0;
            }
            JsonObject jsonObject = gson.fromJson(s, JsonObject.class);
            OrderInfo info = gson.fromJson(jsonObject, OrderInfo.class);
            String status = info.getOrder_status();
            if (!"1003".equals(status) && !"1005".equals(status) && !"1006".equals(status)) {
                count++;
            }
            return count;
        }
    }
}