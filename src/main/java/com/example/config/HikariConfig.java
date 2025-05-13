package com.example.config;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class HikariConfig {
    private static HikariDataSource dataSource;

    static {
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://172.18.9.62:5432/feature_sit_scm_smoke_pg_scm");
        config.setUsername("mc_feature_sit_scm_smoke_pg");
        config.setPassword("mc_feature_sit_scm_smoke_pg");
        config.setDriverClassName("org.postgresql.Driver");
        
        // 连接池配置
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);
        config.setMaxLifetime(1800000);
        
        // 添加PostgreSQL驱动日志配置
        Properties props = new Properties();
        props.setProperty("loggerLevel", "TRACE");
        props.setProperty("loggerFile", "pgjdbc.log");
        config.setDataSourceProperties(props);
        
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
} 