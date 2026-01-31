-- DevToolMP 分类数据初始化脚本
-- 用途：初始化工具分类表数据

-- 清空现有分类数据（可选）
-- TRUNCATE TABLE categories;

-- 插入默认分类数据
INSERT INTO categories (name, description, sort_order) VALUES
('开发工具', '软件开发相关工具', 1),
('设计工具', '设计相关工具', 2),
('生产力工具', '提升生产力的工具', 3),
('测试工具', '软件测试相关工具', 4);

-- 验证插入结果
SELECT * FROM categories ORDER BY sort_order;
