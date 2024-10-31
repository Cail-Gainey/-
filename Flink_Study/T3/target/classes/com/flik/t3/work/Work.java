package com.flik.t3.work;

import com.flik.t3.pojo.OrderInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.*;

public class Work {
    private static final String BOOTSTRAP_SERVER = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "test";
    private static final Gson gson = new Gson();

    private static final String NAMESPACE = "shtd_result";
    private static final String TABLE_NAME = "order_info";

    private static final List<Tuple3<Integer, String, Double>> topUserConsumption = new ArrayList<>(2);

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVER);

        // Define OutputTags for side output streams
        final OutputTag<String> cancelRateTag = new OutputTag<String>("cancelRate") {};
        final OutputTag<OrderInfo> cancelOrderTag = new OutputTag<OrderInfo>("cancelOrder") {};

        // Kafka source
        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<>(TOPIC, new SimpleStringSchema(), prop));

        // Task 1: Top 2 user consumption
        SingleOutputStreamOperator<String> top2UserConsumptionStream = source
                .map(new MapFunction<String, Tuple3<Integer, String, Double>>() {
                    @Override
                    public Tuple3<Integer, String, Double> map(String s) throws Exception {
                        OrderInfo order = gson.fromJson(s, OrderInfo.class);
                        return Tuple3.of(order.getUser_id(), order.getUser_name(), order.getOrder_count() * order.getPrice());
                    }
                })
                .map(tuple -> updateTop2UserConsumption(tuple));

        top2UserConsumptionStream.addSink(redisSink("top2userconsumption", "top2userconsumption"));
        top2UserConsumptionStream.print("W1");

        // Task 2: Cancel rate calculation every minute
        SingleOutputStreamOperator<String> cancelRateStream = source
                .map(s -> gson.fromJson(s, OrderInfo.class))
                .windowAll(TumblingProcessingTimeWindows.of(Time.minutes(1)))
                .apply(new CancelRateWindowFunction())
                .process(new ProcessFunction<String, String>() {
                    @Override
                    public void processElement(String cancelRate, Context context, Collector<String> collector) {
                        context.output(cancelRateTag, cancelRate);
                    }
                });

        cancelRateStream.getSideOutput(cancelRateTag).addSink(redisSink("cancelrate", "cancelrate"));
        cancelRateStream.getSideOutput(cancelRateTag).print("W2");

        // Task 3: Save "cancel" orders to HBase
        SingleOutputStreamOperator<String> orderProcessingStream = source
                .map(s -> gson.fromJson(s, OrderInfo.class))
                .process(new ProcessFunction<OrderInfo, String>() {
                    @Override
                    public void processElement(OrderInfo order, Context context, Collector<String> collector) throws Exception {
                        if ("1003".equals(order.getOrder_status())) {
                            context.output(cancelOrderTag, order);
                        }
//                        saveToHBase(order); // Save order to HBase
                    }
                });
        orderProcessingStream.getSideOutput(cancelOrderTag).print("W3");
        env.execute("Order Data Processing");
    }

    // Update top 2 user consumption
    private static String updateTop2UserConsumption(Tuple3<Integer, String, Double> tuple) {
        if (topUserConsumption.size() < 2) {
            topUserConsumption.add(tuple);
        } else {
            if (tuple.f2 > topUserConsumption.get(0).f2) {
                topUserConsumption.set(1, topUserConsumption.get(0));
                topUserConsumption.set(0, tuple);
            } else if (tuple.f2 > topUserConsumption.get(1).f2) {
                topUserConsumption.set(1, tuple);
            }
        }
        if (topUserConsumption.size() == 2) {
            return String.format("[%d:%s:%.2f, %d:%s:%.2f]",
                    topUserConsumption.get(0).f0, topUserConsumption.get(0).f1, topUserConsumption.get(0).f2,
                    topUserConsumption.get(1).f0, topUserConsumption.get(1).f1, topUserConsumption.get(1).f2);
        } else if (topUserConsumption.size() == 1) {
            return String.format("[%d:%s:%.2f]", topUserConsumption.get(0).f0, topUserConsumption.get(0).f1, topUserConsumption.get(0).f2);
        } else {
            return "[]";
        }
    }


    // Redis sink
    private static RedisSink<String> redisSink(String redisKey, String redisField) {
        return new RedisSink<>(redisConfig(), new RedisMapper<String>() {
            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.SET, redisKey);
            }

            @Override
            public String getKeyFromData(String data) {
                return redisField;
            }

            @Override
            public String getValueFromData(String data) {
                return data;
            }
        });
    }

    // Redis config
    private static FlinkJedisConfigBase redisConfig() {
        return new FlinkJedisPoolConfig.Builder()
                .setHost("master")
                .setPort(6379)
                .build();
    }

    // Save to HBase
    private static void saveToHBase(OrderInfo order) throws IOException {
        Configuration config = new Configuration();
        config.set("hbase.zookeeper.quorum", "master,slave1,slave2");
        config.set("hbase.zookeeper.property.clientPort", "2181");

        try (Connection connection = ConnectionFactory.createConnection(config)) {
            Table table = connection.getTable(TableName.valueOf(NAMESPACE + ":" + TABLE_NAME));
            Put put = new Put(Bytes.toBytes(order.getOrder_id()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("order_count"), Bytes.toBytes(order.getOrder_count()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("price"), Bytes.toBytes(order.getPrice()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("order_status"), Bytes.toBytes(order.getOrder_status()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("create_time"), Bytes.toBytes(order.getCreate_time()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("operate_time"), Bytes.toBytes(String.valueOf(order.getOperate_time())));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("user_id"), Bytes.toBytes(order.getUser_id()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("user_name"), Bytes.toBytes(order.getUser_name()));
            table.put(put);
            table.close();
        }
    }
}

// Cancel rate window function
class CancelRateWindowFunction implements AllWindowFunction<OrderInfo, String, TimeWindow> {
    @Override
    public void apply(TimeWindow window, Iterable<OrderInfo> orders, Collector<String> out) {
        Set<String> orderSet = new HashSet<>();
        Set<String> cancelSet = new HashSet<>();

        for (OrderInfo order : orders) {
            if ("1003".equals(order.getOrder_status())) {
                cancelSet.add(order.getOrder_id());
            } else {
                orderSet.add(order.getOrder_id());
            }
        }

        int orderCount = orderSet.size();
        int cancelCount = cancelSet.size();
        double rate = orderCount == 0 ? 0 : (cancelCount * 100.0) / orderCount;
        out.collect(String.format("Cancel Rate: %.1f%%", rate));
    }
}
