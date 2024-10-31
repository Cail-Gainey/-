package com.flink.T4.work;

import com.flink.T4.pojo.ProduceRecord;
import com.google.gson.Gson;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
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

import java.util.Properties;

public class T4_Work1 {
    private static final String BOOTSTRAP_SERVERS = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "ProduceRecord";
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);

        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<String>(TOPIC, new SimpleStringSchema(), prop));

        SingleOutputStreamOperator<String> task1 = source
                .map(s -> gson.fromJson(s, ProduceRecord.class))
                .windowAll(TumblingProcessingTimeWindows.of(Time.minutes(1)))
                .apply(new TotalProduceFunction())
                .map(s -> s);
        task1.addSink(redisSink("totalproduce", "totalproduce"));
        task1.print();

        env.execute();
    }

    private static SinkFunction<String> redisSink(String redisKey, String redisField) {
        return new RedisSink<>(redisConfig(), new RedisMapper<String>() {
            @Override
            public RedisCommandDescription getCommandDescription() {
                return new  RedisCommandDescription(RedisCommand.SET, redisKey);
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
    private static class TotalProduceFunction implements AllWindowFunction<ProduceRecord,String, TimeWindow> {
        @Override
        public void apply(TimeWindow window, Iterable<ProduceRecord> produceRecords, Collector<String> collector) throws Exception {
            Integer totalProduce = 0;
            for (ProduceRecord produce : produceRecords) {
                if (produce.getChange_handle_state()==1) {
                    totalProduce++;
                }
            }
            collector.collect(String.valueOf(totalProduce));
        }
    }
}
