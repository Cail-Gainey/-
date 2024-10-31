package com.flink.T4.work;

import com.flink.T4.pojo.ChangeRecord;
import com.google.gson.Gson;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class T4_Work2 {

    private static final String BOOTSTRAP_SERVERS = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "ChangeRecord";
    private static final Gson gson = new Gson();
    private static final Map<String, Long> lastWarningTime = new HashMap<>();  // 记录设备上次预警时间，防止重复预警

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);

        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<>(TOPIC, new SimpleStringSchema(), prop));

        SingleOutputStreamOperator<String> task = source
                .map(s -> gson.fromJson(s, ChangeRecord.class))
                .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(30)))
                .apply(new WarningFunction());

        // 将预警信息输出到Redis
        task.addSink(redisSink("warning30sMachine"));
        task.print();

        env.execute();
    }

    // Redis Sink 函数
    private static RedisSink<String> redisSink(String redisKey) {
        FlinkJedisConfigBase jedisConfig = new FlinkJedisPoolConfig.Builder()
                .setHost("master")  // Redis服务器地址
                .setPort(6379)  // Redis服务器端口
                .build();

        return new RedisSink<>(jedisConfig, new RedisMapper<String>() {
            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.HSET, redisKey);
            }

            @Override
            public String getKeyFromData(String s) {
                // 从数据中提取设备ID作为Redis的Key
                return s.split(",")[0];  // 假设数据格式为: 设备ID,时间:预警信息
            }

            @Override
            public String getValueFromData(String s) {
                // 返回预警信息作为Value
                return s;
            }
        });
    }

    // 预警检测逻辑：检测是否连续30秒为“预警”
    private static class WarningFunction implements AllWindowFunction<ChangeRecord, String, TimeWindow> {

        @Override
        public void apply(TimeWindow window, Iterable<ChangeRecord> iterable, Collector<String> collector) throws Exception {

            Map<String, ChangeRecord> warningDevices = new HashMap<>();

            for (ChangeRecord change : iterable) {
                // 过滤出状态为“预警”的数据
                if ("预警".equals(change.getState())) {
                    // 记录设备ID和对应的ChangeRecord数据
                    warningDevices.put(change.getDevice_id(), change);
                }
            }

            // 遍历已经处于“预警”状态的设备，防止重复预警
            for (Map.Entry<String, ChangeRecord> entry : warningDevices.entrySet()) {
                String deviceId = entry.getKey();
                ChangeRecord record = entry.getValue();
                long currentTime = System.currentTimeMillis();

                // 判断设备上次预警时间是否超过30秒
                if (!lastWarningTime.containsKey(deviceId) || (currentTime - lastWarningTime.get(deviceId)) >= 30000) {
                    // 生成预警信息
                    String warningMessage = String.format("%s,%s:设备%s 连续30秒为预警状态请尽快处理！",
                            deviceId, record.getChange_start_time(), deviceId);

                    // 输出预警信息
                    collector.collect(warningMessage);

                    // 更新设备上次预警时间
                    lastWarningTime.put(deviceId, currentTime);
                }
            }
        }
    }
}
