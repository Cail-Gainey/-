package com.flik.t3.work;

import com.flik.t3.pojo.OrderInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
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
import java.util.Properties;

public class Work3 {
    private static final String BOOTSTRAP_SERVER = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "test";
    private static final Gson gson = new Gson();

    // HBase 命名空间和表名
    private static final String NAMESPACE = "shtd_result";
    private static final String TABLE_NAME = "order_info";

    public static void main(String[] args) {
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Kafka 配置
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVER);

        // 定义侧边流输出标签，指定具体的类型 OrderInfo
        final OutputTag<OrderInfo> cancelOrder = new OutputTag<OrderInfo>("cancelOrder"){};

        // 从 Kafka 中读取数据
        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<>(TOPIC, new SimpleStringSchema(), prop));

        // 处理数据，判断是否为取消订单，分流到侧边流
        SingleOutputStreamOperator<String> cancelStream = source
                .map(new MapFunction<String, OrderInfo>() {
                    @Override
                    public OrderInfo map(String s) throws Exception {
                        JsonObject jsonObject = gson.fromJson(s, JsonObject.class);
                        return gson.fromJson(jsonObject, OrderInfo.class);
                    }
                })
                .process(new ProcessFunction<OrderInfo, String>() {
                    @Override
                    public void processElement(OrderInfo order, ProcessFunction<OrderInfo, String>.Context context, Collector<String> collector) throws Exception {
                        // 将订单状态为 "1003" 的订单分流到 cancelOrder 侧边流
                        if (order.getOrder_status().equals("1003")) {
                            context.output(cancelOrder, order);
                        }

                        // 将订单数据存入 HBase
                        saveToHBase(order);
                    }
                });

        // 输出主流数据
        cancelStream.print("正常订单");

        // 从 cancelOrder 侧边流中提取取消订单数据
        DataStream<OrderInfo> cancelOrders = cancelStream.getSideOutput(cancelOrder);

        // 输出取消订单数据
        cancelOrders.print("取消订单");

        try {
            // 执行流作业
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将数据写入 HBase
    private static void saveToHBase(OrderInfo order) throws IOException {
        // HBase 配置
        Configuration config = new Configuration();
        config.set("hbase.zookeeper.quorum", "master,slave1,slave2");  // 替换为你的 Zookeeper 地址
        config.set("hbase.zookeeper.property.clientPort", "2181");

        // 建立 HBase 连接
        try (Connection connection = ConnectionFactory.createConnection(config)) {
            // 获取表
            Table table = connection.getTable(TableName.valueOf(NAMESPACE + ":" + TABLE_NAME));

            // 构建 Put 操作，rowkey 为 order_id
            Put put = new Put(Bytes.toBytes(order.getOrder_id()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("order_count"), Bytes.toBytes(order.getOrder_count()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("price"), Bytes.toBytes(order.getPrice()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("order_status"), Bytes.toBytes(order.getOrder_status()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("create_time"), Bytes.toBytes(order.getCreate_time()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("operate_time"), Bytes.toBytes(String.valueOf(order.getOperate_time() == null ? "" : order.getOperate_time())));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("event_time"), Bytes.toBytes(order.getEvent_time()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("user_id"), Bytes.toBytes(order.getUser_id()));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("user_name"), Bytes.toBytes(order.getUser_name()));

            // 执行插入操作
            table.put(put);

            // 关闭表连接
            table.close();
        }
    }
}
