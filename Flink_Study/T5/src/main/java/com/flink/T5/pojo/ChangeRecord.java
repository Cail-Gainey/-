package com.flink.T5.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRecord {
    private String machine_id;
    private String change_time;
    private String last_state;
    private String current_state;
}
