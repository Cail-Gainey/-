package com.flink.T4.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRecord {
    private String device_id;
    private String state;
    private Date change_start_time;
}