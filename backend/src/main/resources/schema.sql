-- DevToolMP 数据库初始化脚本

-- 分类表
CREATE TABLE IF NOT EXISTS toolmk_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 代码仓库表
CREATE TABLE IF NOT EXISTS toolmk_codehub (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner VARCHAR(255) NOT NULL,
    repo VARCHAR(255) NOT NULL,
    version VARCHAR(50),
    stars INT DEFAULT 0,
    forks INT DEFAULT 0,
    open_issues INT DEFAULT 0,
    watchers INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_owner_repo (owner, repo),
    INDEX idx_stars (stars DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 工具表
CREATE TABLE IF NOT EXISTS toolmk_tools (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id BIGINT,
    codehub_id BIGINT,
    status VARCHAR(20) DEFAULT 'active',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES toolmk_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (codehub_id) REFERENCES toolmk_codehub(id) ON DELETE SET NULL,
    INDEX idx_status (status),
    INDEX idx_category (category_id),
    INDEX idx_codehub (codehub_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 工具标签表
CREATE TABLE IF NOT EXISTS toolmk_tool_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES toolmk_tools(id) ON DELETE CASCADE,
    INDEX idx_tool_tag (tool_id, tag_name),
    INDEX idx_tag (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 收藏表
CREATE TABLE IF NOT EXISTS toolmk_favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES toolmk_tools(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tool_user (tool_id, user_id),
    INDEX idx_tool (tool_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 浏览记录表
CREATE TABLE IF NOT EXISTS toolmk_view_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    user_id VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES toolmk_tools(id) ON DELETE CASCADE,
    INDEX idx_tool_time (tool_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 评价表
CREATE TABLE IF NOT EXISTS toolmk_ratings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    username VARCHAR(255) DEFAULT NULL COMMENT '用户昵称',
    score INT NOT NULL CHECK (score >= 1 AND score <= 5),
    comment TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES toolmk_tools(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tool_user (tool_id, user_id),
    INDEX idx_tool (tool_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 评价回复表
CREATE TABLE IF NOT EXISTS toolmk_comment_replies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rating_id BIGINT NOT NULL,
    user_id VARCHAR(500) NOT NULL,
    username VARCHAR(255) DEFAULT NULL COMMENT '用户昵称',
    reply_to_user_id BIGINT DEFAULT NULL COMMENT '回复给的用户ID',
    reply_to_username VARCHAR(255) DEFAULT NULL COMMENT '回复给的用户名',
    content TEXT NOT NULL COMMENT '回复内容',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (rating_id) REFERENCES toolmk_ratings(id) ON DELETE CASCADE,
    INDEX idx_rating_id (rating_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评价回复表';

-- 评价点赞表
CREATE TABLE IF NOT EXISTS toolmk_rating_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rating_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rating_id) REFERENCES toolmk_ratings(id) ON DELETE CASCADE,
    UNIQUE KEY uk_rating_user (rating_id, user_id),
    INDEX idx_rating (rating_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 安装记录表
CREATE TABLE IF NOT EXISTS toolmk_install_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    user_id VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES toolmk_tools(id) ON DELETE CASCADE,
    INDEX idx_tool_time (tool_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
