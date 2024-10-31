package com.flink.T5.work;

import com.flink.T5.pojo.ProduceRecord;
import com.google.gson.Gson;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Properties;

public class T5_Work1 {
    private static final String BOOTSTRAP_SERVERS = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "ProduceRecord";
    private static final Gson gson = new Gson();

    private static final String NAMESPACE = "gyflinkresult";
    private static final String TABLE_NAME = "Produce5minAgg";

    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);

        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<String>(TOPIC, new SimpleStringSchema(), prop));

        SingleOutputStreamOperator<String> result = source
                .map(s -> gson.fromJson(s, ProduceRecord.class))
                .filter(record -> record.getChange_handle_state().equals("0"))
                .keyBy(ProduceRecord::getMachine_id)
                .window(TumblingProcessingTimeWindows.of(Time.minutes(1)))
                .apply(new WindowFunction<ProduceRecord, String, String, TimeWindow>() {
                    @Override
                    public void apply(String machineID, TimeWindow window, Iterable<ProduceRecord> input, Collector<String> out) throws Exception {
                        int count = 0;
                        String time = "";
                        Tuple3<String,String,Integer> tuple = new Tuple3<>();
                        for (ProduceRecord record : input) {
                            time = record.getProduce_time();
                            count++;
                        }
                        out.collect(String.valueOf(new Tuple2<>(machineID, count)));
                        tuple.setFields(machineID+"-"+time,machineID,count);
                        saveToHBase(tuple);

                    }
                });
        result.print();

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveToHBase(Tuple3<String,String,Integer> tuple) throws IOException {
        Configuration config = new Configuration();
        config.set("hbase.zookeeper.quorum", "master,slave1,slave1");
        config.set("hbase.zookeeper.property.clientPort", "2181");

        try (Connection connection = ConnectionFactory.createConnection(config)) {
            Table table = connection.getTable(TableName.valueOf(NAMESPACE + ":" + TABLE_NAME));

            Put put = new Put(Bytes.toBytes(tuple.f0));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("machine_id"), Bytes.toBytes(tuple.f1));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("total_produce"), Bytes.toBytes(tuple.f2));

            table.put(put);
            table.close();
        }
    }
}
