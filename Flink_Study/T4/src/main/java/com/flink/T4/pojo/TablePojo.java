package com.flink.T4.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TablePojo {
    private int change_machine_id;
    private int totalwarning;
    private String window_end_time;
}
