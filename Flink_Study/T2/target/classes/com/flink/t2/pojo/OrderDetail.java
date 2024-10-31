package com.flink.t2.pojo;

import java.io.Serializable;

public class OrderDetail implements Serializable {

    private String orderId;        // 订单ID
    private String productName;    // 商品名称
    private int productCount;      // 商品数量
    private double productPrice;   // 商品价格

    // Constructor
    public OrderDetail(String orderId, String productName, int productCount, double productPrice) {
        this.orderId = orderId;
        this.productName = productName;
        this.productCount = productCount;
        this.productPrice = productPrice;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    @Override
    public String toString() {
        return "OrderDetail{" +
                "orderId='" + orderId + '\'' +
                ", productName='" + productName + '\'' +
                ", productCount=" + productCount +
                ", productPrice=" + productPrice +
                '}';
    }
}
