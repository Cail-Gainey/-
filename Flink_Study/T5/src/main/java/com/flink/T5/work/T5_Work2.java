package com.flink.T5.work;

import com.flink.T5.pojo.ChangeRecord;
import com.flink.T5.pojo.StateToRun;
import com.google.gson.Gson;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

public class T5_Work2 {
    private static final String BOOTSTRAP_SERVERS = "master:9092,slave1:9092,slave2:9092";
    private static final String TOPIC = "ChangeRecord";
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);

        DataStream<String> source = env.addSource(new FlinkKafkaConsumer<String>(TOPIC, new SimpleStringSchema(), prop));

        SingleOutputStreamOperator<StateToRun> result = source
                .map(s -> gson.fromJson(s, ChangeRecord.class))
                .filter(state -> state.getCurrent_state().equals("running"))
                .keyBy(ChangeRecord::getMachine_id)
                .map(new MapFunction<ChangeRecord, StateToRun>() {
                    int count = 0;
                    String time = "";
                    @Override
                    public StateToRun map(ChangeRecord record) throws Exception {
                        if (record.getCurrent_state().equals("running")) {
                            count++;
                            time = record.getChange_time();
                        }
                        return new StateToRun(Integer.parseInt(record.getMachine_id()), record.getLast_state(), count,time);
                    }
                });
        result.print();
        result.addSink(new MysqlSink());
        env.execute();
    }


}
