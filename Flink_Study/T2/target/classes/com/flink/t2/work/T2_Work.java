package com.flink.t2.work;

import com.flink.t2.pojo.OrderInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
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
import org.apache.flink.util.OutputTag;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class T2_Work {
    private static final String BOOTSTRAP_SERVER = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "test";
    private static final Gson gson = new Gson();
    private static final Set<String> orderSet = new HashSet<>();

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVER);

        final OutputTag<String> tag2 = new OutputTag<String>("task2") {};
        final OutputTag<String> tag3 = new OutputTag<String>("task3") {};

        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<String>(TOPIC, new SimpleStringSchema(), prop));

        SingleOutputStreamOperator<String> task1 = source
                .map(new MapFunction<String, String>() {
                    @Override
                    public String map(String s) throws Exception {
                        JsonObject jsonObject = gson.fromJson(s, JsonObject.class);
                        OrderInfo order = gson.fromJson(jsonObject, OrderInfo.class);
                        if (!order.getOrder_status().equals("1005") && !order.getOrder_status().equals("1003") && !order.getOrder_status().equals("1006")) {
                            orderSet.add(order.getOrder_id());
                        }
                        return String.valueOf(orderSet.size());
                    }
                });
        task1.addSink(redisSink("totalcount", "totalcount"));
        task1.print("T1");

        SingleOutputStreamOperator<String> task2 = source
                .map(s -> gson.fromJson(s, OrderInfo.class))
                .windowAll(TumblingProcessingTimeWindows.of(Time.minutes(1)))
                .apply(new CancelCountFunction())
                .process(new ProcessFunction<String, String>() {
                    @Override
                    public void processElement(String s, ProcessFunction<String, String>.Context context, Collector<String> collector) throws Exception {
                        context.output(tag2, s);
                    }
                });
        DataStream<String> task2Side = task2.getSideOutput(tag2);
        task2Side.addSink(redisSink("refundcountminute", "refundcountminute"));
        task2Side.print("T2");

        SingleOutputStreamOperator<String> task3 = source
                .map(s -> gson.fromJson(s, OrderInfo.class))
                .windowAll(TumblingProcessingTimeWindows.of(Time.minutes(1)))
                .apply(new CancelRateFunction())
                .process(new ProcessFunction<String, String>() {
                    @Override
                    public void processElement(String s, ProcessFunction<String, String>.Context context, Collector<String> collector) throws Exception {
                        context.output(tag3, s);
                    }
                });
        DataStream<String> task3Side = task3.getSideOutput(tag3);
        task3Side.addSink(redisSink("cancelrate", "cancelrate"));
        task3Side.print("T3");

        env.execute();
    }

    private static SinkFunction<String> redisSink(String redisKey, String redisField) {
        return new RedisSink<>(redisConfig(), new RedisMapper<String>() {
            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.SET, redisKey);
            }

            @Override
            public String getKeyFromData(String s) {
                return redisField;
            }

            @Override
            public String getValueFromData(String s) {
                return s;
            }
        });

    }

    private static FlinkJedisConfigBase redisConfig() {
        return new FlinkJedisPoolConfig.Builder()
                .setHost("master")
                .setPort(6379)
                .build();
    }

    private static class CancelCountFunction implements AllWindowFunction<OrderInfo, String , TimeWindow> {
        @Override
        public void apply(TimeWindow window, Iterable<OrderInfo> orders, Collector<String> out) throws Exception {
            Set<String> cancelSet = new HashSet<>();

            for (OrderInfo order : orders) {
                if (order.getOrder_status().equals("1003")) {
                    cancelSet.add(order.getOrder_id());
                }
            }
            out.collect(String.valueOf(cancelSet.size()));
        }
    }

    private static class CancelRateFunction implements AllWindowFunction<OrderInfo, String, TimeWindow>{
        @Override
        public void apply(TimeWindow window, Iterable<OrderInfo> orders, Collector<String> out) throws Exception {
            Set<String> cancelSet = new HashSet<>();
            Set<String> orderSet = new HashSet<>();

            for (OrderInfo order : orders) {
                if (order.getOrder_status().equals("1003")) {
                    cancelSet.add(order.getOrder_id());
                } else {
                    orderSet.add(order.getOrder_id());
                }
            }
            int orderCount = orderSet.size();
            int cancelCount = cancelSet.size();
            double rate = orderCount == 0 ? 0 : (cancelCount * 100.0) / orderCount;
            out.collect(String.format("取消率: %.1f%%", rate));
        }
    }
}
