package com.devtoolmp.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 数据库初始化器
 *
 * 注意：所有表结构定义都集中在 schema.sql 文件中
 * 所有数据预置都集中在 data.sql 文件中
 * 本类仅用于数据库连接验证，不进行任何表结构修改或数据预置
 */
@Component
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void initialize() {
        try (var connection = dataSource.getConnection()) {
            logger.info("数据库连接验证成功");
            // 所有表结构定义都在 schema.sql 中，所有数据预置都在 data.sql 中
            // 此处不进行任何动态修改，仅验证连接
        } catch (Exception e) {
            logger.error("数据库连接验证失败", e);
            // 不抛出异常，允许应用继续启动
        }
    }
}
