package com.flik.t3.work;

import com.flik.t3.pojo.OrderInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisConfigBase;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class T3_Work1 {
    private static final String BOOTSTRAP_SERVER = "master:9092,slav1:9092,slave2:9092";
    private static final String TOPIC = "test";
    private static final Gson gson = new Gson();
    private static final List<Tuple3<Integer, String, Double>> list = new ArrayList<>(2);

    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVER);

        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<>(TOPIC, new SimpleStringSchema(), prop));

        // 解析订单信息并计算总金额
        DataStream<Tuple3<Integer, String, Double>> filter = source.map(new MapFunction<String, Tuple3<Integer, String, Double>>() {
            @Override
            public Tuple3<Integer, String, Double> map(String s) throws Exception {
                JsonObject jsonObject = gson.fromJson(s, JsonObject.class);
                OrderInfo order = gson.fromJson(jsonObject, OrderInfo.class);
                Integer orderCount = order.getOrder_count();
                Integer userID = order.getUser_id();
                String userName = order.getUser_name();
                Double price = order.getPrice();
                double cost = orderCount * price; // 计算总金额
                return Tuple3.of(userID, userName, cost);
            }
        });

        // 找出金额最大的两个订单
        DataStream<String> data = filter.map(new MapFunction<Tuple3<Integer, String, Double>, String>() {
            @Override
            public String map(Tuple3<Integer, String, Double> tuple) throws Exception {
                if (list.size() < 2) {
                    list.add(tuple); // 如果list中不足两个元素，直接添加
                } else {
                    if (tuple.f2 > list.get(0).f2) {
                        list.set(1, list.get(0)); // 将当前最大元素移到第二位
                        list.set(0, tuple); // 将新元素设置为最大
                    } else if (tuple.f2 > list.get(1).f2) {
                        list.set(1, tuple); // 将新元素设置为第二大
                    }
                }

                // 判断是否已经有足够的元素再进行格式化输出
                if (list.size() == 2) {
                    return String.format("[%d:%s:%.2f, %d:%s:%.2f]",
                            list.get(0).f0, list.get(0).f1, list.get(0).f2,
                            list.get(1).f0, list.get(1).f1, list.get(1).f2);
                } else if (list.size() == 1) {
                    return String.format("[%d:%s:%.2f]", list.get(0).f0, list.get(0).f1, list.get(0).f2);
                } else {
                    return "[]"; // 如果没有数据，返回空
                }
            }
        });

        data.print(); // 打印输出

        data.addSink(new RedisSink<>(getRedisConfig(), new RedisMapper<String>() {
            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.SET, "top2userconsumption");
            }

            @Override
            public String getKeyFromData(String s) {
                return "top2userconsumption";
            }

            @Override
            public String getValueFromData(String s) {
                return s;
            }
        }));

        try {
            env.execute("work1");
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
