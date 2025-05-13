package com.example.service;

import com.example.config.HikariConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchUpdateService {
    private static final Logger logger = LoggerFactory.getLogger(BatchUpdateService.class);
    
    // 方法1：使用单条更新语句
    public void batchUpdateSingle(List<UpdateData> updateDataList) throws SQLException {
        String sql = "UPDATE t_pom_manustockentry SET foutqty = foutqty + ? WHERE fdetailid = ?";
        
        try (Connection conn = HikariConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            String threadName = Thread.currentThread().getName();

            int i=0;
            for (UpdateData data : updateDataList) {
                i++;
                pstmt.setDouble(1, data.getOutQty());
                pstmt.setLong(2, data.getDetailId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();

            Thread.sleep(3000);

            conn.commit();
            logger.info("{} 事务提交完成", threadName);
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    // 方法2：使用IN子句批量更新
    public void batchUpdateIn(List<UpdateData> updateDataList) throws SQLException {
        String placeholders = updateDataList.stream()
                .map(data -> "?")
                .collect(Collectors.joining(","));
        
        String sql = "UPDATE t_pom_manustockentry SET foutqty = foutqty + ? WHERE fdetailid IN (" + placeholders + ")";
        
        try (Connection conn = HikariConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            String threadName = Thread.currentThread().getName();

            // 设置第一个参数（增量值）
            pstmt.setDouble(1, updateDataList.get(0).getOutQty());
            
            // 设置IN子句的参数
            StringBuilder paramValues = new StringBuilder();
            for (int i = 0; i < updateDataList.size(); i++) {
                pstmt.setLong(i + 2, updateDataList.get(i).getDetailId());
                if (i > 0) paramValues.append(", ");
                paramValues.append(updateDataList.get(i).getDetailId());
            }
            
            int affectedRows = pstmt.executeUpdate();
            logger.info("{} 更新执行完成，影响行数: {}", threadName, affectedRows);

            Thread.sleep(3000);

            conn.commit();
            logger.info("{} 事务提交完成", threadName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    // 模拟多线程并发更新
    public void simulateConcurrentUpdates(List<List<UpdateData>> threadDataList, boolean useInClause) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadDataList.size());
        CountDownLatch latch = new CountDownLatch(threadDataList.size());
        
        for (int i = 0; i < threadDataList.size(); i++) {
            final List<UpdateData> updates = threadDataList.get(i);
            final int threadId = i + 1;
            executor.submit(() -> {
                try {
                    if (useInClause) {
                        batchUpdateIn(updates);
                    } else {
                        batchUpdateSingle(updates);
                    }
                } catch (SQLException e) {
                    logger.error("线程 {} 执行失败: {}", threadId, e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
    }
    
    // 数据模型类
    public static class UpdateData {
        private Long detailId;
        private Double outQty;
        
        public UpdateData(Long detailId, Double outQty) {
            this.detailId = detailId;
            this.outQty = outQty;
        }
        
        public Long getDetailId() {
            return detailId;
        }
        
        public Double getOutQty() {
            return outQty;
        }
    }
} 