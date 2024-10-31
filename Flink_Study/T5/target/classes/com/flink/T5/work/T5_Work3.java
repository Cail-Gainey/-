package com.flink.T5.work;

import com.flink.T5.pojo.WarnChangeRecord;
import com.google.gson.Gson;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisConfigBase;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

import java.util.Properties;

public class T5_Work3 {
    private static final String BOOTSTRAP_SERVERS = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "ChangeRecord";
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);

        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<String>(TOPIC, new SimpleStringSchema(), prop));

        SingleOutputStreamOperator<Tuple2<String, Integer>> result = source
                .map(value -> gson.fromJson(value, WarnChangeRecord.class)) // 将JSON字符串转换为对象，YourDataClass为您的数据类
                .map(new MapFunction<WarnChangeRecord, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> map(WarnChangeRecord data) throws Exception {
                        return Tuple2.of(data.getMachine_id(), 1);
                    }
                }) // 将设备ID映射为Tuple2<设备ID, 1>
                .keyBy(0) // 按设备ID分组
                .timeWindow(Time.minutes(3), Time.minutes(1)) // 定义3分钟的滚动窗口，1分钟滚动一次
                .sum(1); // 在窗口内对次数进行求和
        result.addSink(redisSink("warning_last3min_everymin_out", ""));
        result.print();

        env.execute("T5_Work3");
    }

    private static SinkFunction<Tuple2<String, Integer>> redisSink(String redisKey, String redisFlied) {
        FlinkJedisConfigBase jedisConfig = new FlinkJedisPoolConfig.Builder()
                .setHost("master").setPort(6379).build();

        return new RedisSink<>(jedisConfig, new RedisMapper<Tuple2<String, Integer>>() {
            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.HSET, redisKey);
            }

            @Override
            public String getKeyFromData(Tuple2<String, Integer> s) {
                return s.f0;
            }

            @Override
            public String getValueFromData(Tuple2<String, Integer> s) {
                return String.valueOf(s.f1);
            }

        });
    }
}


