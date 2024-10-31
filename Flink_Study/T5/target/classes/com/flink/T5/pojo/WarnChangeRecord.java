package com.flink.T5.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarnChangeRecord {
    private String machine_id;
    private String warning_time;
    private String state;
}
