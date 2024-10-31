package com.flink.T5.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProduceRecord {
    private String machine_id;
    private String produce_time;
    private String change_handle_state;
}