package com.flink.T5.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateToRun {
    private int change_machine_id;
    private String last_machine_state;
    private int total_change_torun;
    private String in_time;
}
