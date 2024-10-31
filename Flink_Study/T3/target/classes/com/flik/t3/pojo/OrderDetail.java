package com.flik.t3.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail implements Serializable {

    private String orderId;        // 订单ID
    private String productName;    // 商品名称
    private int productCount;      // 商品数量
    private double productPrice;   // 商品价格
}
