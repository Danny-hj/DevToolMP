-- 移除冗余的 create_date 字段
-- favorites 表已有 create_time 字段，无需额外的 create_date

ALTER TABLE favorites DROP COLUMN create_date;
ALTER TABLE view_records DROP COLUMN create_date;
ALTER TABLE install_records DROP COLUMN create_date;

-- 更新索引
ALTER TABLE favorites DROP INDEX idx_create_date;
