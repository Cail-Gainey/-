package com.flik.t3.work;

import com.flik.t3.pojo.OrderInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisConfigBase;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;
import org.apache.flink.util.Collector;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Work2 {
    private static final String BOOTSTRAP_SERVER = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "test";
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 设置Kafka配置
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVER);

        // 从Kafka中读取数据源
        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<>(TOPIC, new SimpleStringSchema(), prop));

        // 每分钟统计取消率
        SingleOutputStreamOperator<String> orderStream = source
                .map(new MapFunction<String, OrderInfo>() {
                    @Override
                    public OrderInfo map(String s) throws Exception {
                        // 将输入的JSON字符串解析为OrderInfo对象
                        JsonObject jsonObject = gson.fromJson(s, JsonObject.class);
                        return gson.fromJson(jsonObject, OrderInfo.class);
                    }
                })
                .windowAll(TumblingProcessingTimeWindows.of(Time.minutes(1)))
                .apply(new AllWindowFunction<OrderInfo, String, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow window, Iterable<OrderInfo> iterable, Collector<String> out) throws Exception {
                        // 使用两个集合来追踪订单和取消订单
                        Set<String> orderSet = new HashSet<>();
                        Set<String> cancelSet = new HashSet<>();
                        for (OrderInfo order : iterable) {
                            if (order.getOrder_status().equals("1003")) {
                                cancelSet.add(order.getOrder_id());
                            } else {
                                orderSet.add(order.getOrder_id());
                            }
                        }
                        // 计算取消率
                        int orderCount = orderSet.size();
                        int cancelCount = cancelSet.size();
                        double rate = orderCount == 0 ? 0 : (cancelCount * 100.0) / orderCount;
                        out.collect(String.format("取消率: %.1f%%", rate));
                    }
                });
        orderStream.print();

        orderStream.addSink(new RedisSink<>(getRedisConfig(), new RedisMapper<String>() {
            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.SET, "cancelrate");
            }

            @Override
            public String getKeyFromData(String s) {
                return "cancelrate";
            }

            @Override
            public String getValueFromData(String s) {
                return s;
            }
        }));

        env.execute();
    }
    private static FlinkJedisConfigBase getRedisConfig() {
        return new FlinkJedisPoolConfig.Builder()
                .setHost("master")
                .setPort(6379)
                .build();
    }
}
