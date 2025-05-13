package com.example;

import com.example.service.BatchUpdateService;
import com.example.service.BatchUpdateService.UpdateData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        BatchUpdateService service = new BatchUpdateService();
        
        try {
            // 创建要更新的记录数据
            List<UpdateData> baseData = Arrays.asList(
                new UpdateData(1626136018002397184L, 1.0),
                new UpdateData(1626136018002397185L, 2.0),
                new UpdateData(1626136018002397186L, 2.0),
                new UpdateData(1626136018002397187L, 2.0),
                new UpdateData(1626136018002397188L, 2.0)
            );

            // 创建多个线程的数据（每个线程都更新相同的记录，但顺序随机）
            List<List<UpdateData>> threadDataList = new ArrayList<>();
            for (int i = 0; i < 10; i++) {  // 创建10个线程
                List<UpdateData> randomOrderData = new ArrayList<>(baseData);
//                Collections.shuffle(randomOrderData); // 随机打乱顺序
                threadDataList.add(randomOrderData);
            }
            
            // 测试场景1：使用单条更新语句
//            System.out.println("测试场景1：使用单条更新语句（每个线程随机顺序更新记录）");
            service.simulateConcurrentUpdates(threadDataList, false);
//
//            Thread.sleep(2000);
            
//            // 测试场景2：使用IN子句批量更新
//            System.out.println("\n测试场景2：使用IN子句批量更新（每个线程更新相同的记录集）");
//            service.simulateConcurrentUpdates(threadDataList, true,2);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接池
            com.example.config.HikariConfig.closeDataSource();
        }
    }
} 