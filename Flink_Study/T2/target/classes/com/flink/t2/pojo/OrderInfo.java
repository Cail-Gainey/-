package com.flink.t2.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo implements Serializable {

    private String order_id;        // 订单ID
    private String order_status;    // 订单状态
    private long create_time;       // 创建时间
    private Long operate_time;      // 操作时间 (可能为空)
    private long event_time;        // 事件时间 (取createTime和operateTime较大值)
}
