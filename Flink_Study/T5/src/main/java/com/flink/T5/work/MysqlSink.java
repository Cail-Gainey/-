package com.flink.T5.work;

import com.flink.T5.pojo.StateToRun;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class MysqlSink extends RichSinkFunction<StateToRun> {
    Connection conn = null;
    PreparedStatement ps = null;
    String url = "jdbc:mysql://master:3306/shtd_industry?IfNotExistCreate=true&characterEncoding=utf-8&useSSL=false";
    String username = "root";
    String password = "123456";


    @Override
    public void open(Configuration parameters) throws Exception {
        conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);
    }

    @Override
    public void close() throws Exception {
        if (conn != null) {
            conn.close();
        }
        if (ps != null) {
            ps.close();
        }
    }

    @Override
    public void invoke(StateToRun state, Context context) throws Exception {
        String sql = "insert into change_state_other_to_run_agg(`change_machine_id`,`last_machine_state`,`total_change_torun`,`in_time`) values (?,?,?,?)";
        ps = conn.prepareStatement(sql);
        ps.setInt(1, state.getChange_machine_id());
        ps.setString(2, state.getLast_machine_state());
        ps.setInt(3, state.getTotal_change_torun());
        ps.setString(4, state.getIn_time());
        ps.executeUpdate();
        conn.commit();
    }
}