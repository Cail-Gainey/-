package com.flik.t3.work;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;

public class HBaseTableCreator {

    private static final String NAMESPACE = "shtd_result"; // 命名空间
    private static final String TABLE_NAME = "order_info"; // 表名
    private static final String COLUMN_FAMILY = "info"; // 列族名

    public static void main(String[] args) {
        // 创建 HBase 配置
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "master,slave1,slave2");  // 替换为实际的 Zookeeper 地址
        config.set("hbase.zookeeper.property.clientPort", "2181"); // Zookeeper 端口

        try (Connection connection = ConnectionFactory.createConnection(config);
             Admin admin = connection.getAdmin()) {

            // 创建命名空间
            createNamespaceIfNotExists(admin, NAMESPACE);

            // 创建表
            createTable(admin, NAMESPACE, TABLE_NAME, COLUMN_FAMILY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 创建命名空间
    private static void createNamespaceIfNotExists(Admin admin, String namespace) throws Exception {
        try {
            admin.getNamespaceDescriptor(namespace);
            System.out.println("命名空间 " + namespace + " 已存在");
        } catch (Exception e) {
            System.out.println("命名空间 " + namespace + " 不存在，创建中...");
            NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create(namespace).build();
            admin.createNamespace(namespaceDescriptor);
            System.out.println("命名空间 " + namespace + " 创建成功");
        }
    }

    // 创建 HBase 表
    private static void createTable(Admin admin, String namespace, String tableName, String columnFamily) throws Exception {
        TableName fullTableName = TableName.valueOf(namespace + ":" + tableName);

        // 检查表是否已经存在
        if (admin.tableExists(fullTableName)) {
            System.out.println("表 " + fullTableName + " 已经存在");
            return;
        }

        // 创建列族描述
        ColumnFamilyDescriptor familyDescriptor = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(columnFamily))
                .setMaxVersions(3) // 列族最大版本数
                .build();

        // 创建表描述
        TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(fullTableName)
                .setColumnFamily(familyDescriptor)
                .build();

        // 创建表
        admin.createTable(tableDescriptor);
        System.out.println("表 " + fullTableName + " 创建成功");
    }
}
