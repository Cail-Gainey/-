package com.flink.T5.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HBaseTable {
    private String rowkey;
    private String machine_id;
    private String total_produce;
}
