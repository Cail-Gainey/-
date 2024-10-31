package com.flink.T4.work;

import com.flink.T4.pojo.ChangeRecord;
import com.flink.T4.pojo.TablePojo;
import com.google.gson.Gson;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class T4_Work3 {

    private static final String BOOTSTRAP_SERVERS = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "ChangeRecord";
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);

        // 从Kafka中消费数据
        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<>(TOPIC, new SimpleStringSchema(), prop));

        // 解析Kafka中的JSON数据，统计每3分钟设备“预警”状态总数，并输出格式化的字符串
        SingleOutputStreamOperator<String> warningCounts = source
                .map(s -> gson.fromJson(s, ChangeRecord.class))
                .filter(record -> "预警".equals(record.getState()))  // 只统计“预警”状态的数据
                .keyBy(ChangeRecord::getDevice_id)  // 根据设备ID进行分组
                .window(TumblingProcessingTimeWindows.of(Time.minutes(3)))  // 3分钟的滚动窗口
                .apply(new WarningStateWindowFunction());

        warningCounts.print();

        env.execute("Warning State Aggregation");
    }


    // 窗口函数，计算预警总数并输出符合格式的字符串
    private static class WarningStateWindowFunction implements WindowFunction<ChangeRecord, String, String, TimeWindow> {
        @Override
        public void apply(String deviceId, TimeWindow window, Iterable<ChangeRecord> input, Collector<String> out) throws Exception {
            int count = 0;
            for (ChangeRecord record : input) {
                count++;  // 统计每个窗口内“预警”状态的数量
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String windowEndTime = sdf.format(window.getEnd());

            // 输出符合Redis格式的字符串："设备ID, 总预警数量"
            String result = deviceId + "," + count + "," + windowEndTime;
            out.collect(result);
        }
    }
}
