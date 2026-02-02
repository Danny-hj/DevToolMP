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

            // 执行数据库迁移：移除冗余的 create_date 字段
            try (var stmt = connection.createStatement()) {
                // 检查并移除 favorites 表的 create_date 字段
                try {
                    stmt.execute("ALTER TABLE favorites DROP COLUMN create_date");
                    logger.info("已移除 favorites.create_date 字段");
                } catch (Exception e) {
                    // 字段可能不存在或已删除，忽略错误
                    logger.debug("favorites.create_date 字段处理: {}", e.getMessage());
                }

                // 检查并移除 view_records 表的 create_date 字段
                try {
                    stmt.execute("ALTER TABLE view_records DROP COLUMN create_date");
                    logger.info("已移除 view_records.create_date 字段");
                } catch (Exception e) {
                    logger.debug("view_records.create_date 字段处理: {}", e.getMessage());
                }

                // 检查并移除 install_records 表的 create_date 字段
                try {
                    stmt.execute("ALTER TABLE install_records DROP COLUMN create_date");
                    logger.info("已移除 install_records.create_date 字段");
                } catch (Exception e) {
                    logger.debug("install_records.create_date 字段处理: {}", e.getMessage());
                }

                // 移除相关索引
                try {
                    stmt.execute("ALTER TABLE favorites DROP INDEX idx_create_date");
                    logger.debug("已移除 favorites.idx_create_date 索引");
                } catch (Exception e) {
                    // 索引可能不存在，忽略错误
                    logger.debug("favorites.idx_create_date 索引处理: {}", e.getMessage());
                }
            } catch (Exception e) {
                logger.warn("数据库迁移过程中出现警告: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.error("数据库连接验证失败", e);
            // 不抛出异常，允许应用继续启动
        }
    }
}
