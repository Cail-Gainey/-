package com.flink.T4.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProduceRecord {
    private String device_id;
    private Integer change_handle_state;
}
