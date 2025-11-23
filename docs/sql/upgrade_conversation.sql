-- ========================================
-- 对话持久化功能数据库升级脚本
-- 执行顺序：按步骤从上到下执行
-- ========================================

-- ========================================
-- 步骤1: 创建 conversation 表
-- ========================================
CREATE TABLE IF NOT EXISTS conversation
(
    id          BIGINT AUTO_INCREMENT COMMENT '对话ID'
        PRIMARY KEY,
    user_id     BIGINT                                 NOT NULL COMMENT '用户ID',
    title       VARCHAR(255) DEFAULT '新对话'           NOT NULL COMMENT '对话标题',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT(1)   DEFAULT 0                 NOT NULL COMMENT '删除标记,0-未删除;1-已删除',
    
    CONSTRAINT fk_conversation_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON UPDATE CASCADE ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
)
    COMMENT '对话表' CHARSET = utf8mb4;


-- ========================================
-- 步骤2: 修改 ai_chat_memory 表
-- ========================================

-- 2.1 先删除旧的外键约束
ALTER TABLE ai_chat_memory DROP FOREIGN KEY IF EXISTS fk_user;

-- 2.2 如果表中已有数据，需要先迁移 chat_id
-- 为现有的每个唯一 chat_id 创建对应的 conversation 记录
INSERT INTO conversation (user_id, title, create_time)
SELECT DISTINCT 
    user_id,
    CONCAT('对话-', chat_id) as title,
    MIN(create_time) as create_time
FROM ai_chat_memory
WHERE is_del = 0
GROUP BY chat_id, user_id;

-- 2.3 添加新字段 conversation_id
ALTER TABLE ai_chat_memory 
    ADD COLUMN conversation_id BIGINT NULL COMMENT '对话ID（新）' AFTER id;

-- 2.4 迁移数据：将 chat_id 映射到 conversation_id
UPDATE ai_chat_memory m
JOIN conversation c ON c.user_id = m.user_id
SET m.conversation_id = c.id
WHERE m.chat_id = SUBSTRING_INDEX(c.title, '-', -1);

-- 2.5 设置 conversation_id 为 NOT NULL
ALTER TABLE ai_chat_memory 
    MODIFY COLUMN conversation_id BIGINT NOT NULL COMMENT '对话ID';

-- 2.6 重命名字段，统一命名规范
ALTER TABLE ai_chat_memory 
    CHANGE COLUMN type role VARCHAR(20) DEFAULT 'user' NOT NULL COMMENT '消息角色：user-用户，assistant-AI',
    CHANGE COLUMN is_del is_deleted TINYINT(1) DEFAULT 0 NOT NULL COMMENT '删除标记,0-未删除;1-已删除';

-- 2.7 添加外键约束
ALTER TABLE ai_chat_memory
    ADD CONSTRAINT fk_message_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversation (id)
            ON UPDATE CASCADE ON DELETE CASCADE,
    ADD CONSTRAINT fk_message_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON UPDATE CASCADE ON DELETE CASCADE;

-- 2.8 添加索引
CREATE INDEX idx_conversation_id ON ai_chat_memory (conversation_id);
CREATE INDEX idx_conversation_user ON ai_chat_memory (conversation_id, user_id);

-- 2.9 删除旧的 chat_id 字段（可选，建议先保留一段时间）
-- ALTER TABLE ai_chat_memory DROP COLUMN chat_id;


-- ========================================
-- 步骤3: 重命名表（可选）
-- ========================================
-- RENAME TABLE ai_chat_memory TO message;


-- ========================================
-- 步骤4: 统一 app 表命名规范（可选）
-- ========================================
-- ALTER TABLE app 
--     CHANGE COLUMN appName app_name VARCHAR(256) NULL COMMENT '应用名称',
--     CHANGE COLUMN initPrompt init_prompt TEXT NULL COMMENT '应用初始化的 prompt',
--     CHANGE COLUMN codeGenType code_gen_type VARCHAR(64) NULL COMMENT '代码生成类型',
--     CHANGE COLUMN deployKey deploy_key VARCHAR(64) NULL COMMENT '部署标识',
--     CHANGE COLUMN deployedTime deployed_time DATETIME NULL COMMENT '部署时间',
--     CHANGE COLUMN userId user_id BIGINT NOT NULL COMMENT '创建用户id',
--     CHANGE COLUMN editTime edit_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
--     CHANGE COLUMN createTime create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
--     CHANGE COLUMN updateTime update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
--     CHANGE COLUMN isDelete is_deleted TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除';


-- ========================================
-- 验证脚本
-- ========================================
-- 查看对话表
SELECT * FROM conversation LIMIT 10;

-- 查看消息表
SELECT * FROM ai_chat_memory LIMIT 10;

-- 查看某个用户的所有对话
SELECT 
    c.id,
    c.title,
    c.create_time,
    c.update_time,
    COUNT(m.id) as message_count
FROM conversation c
LEFT JOIN ai_chat_memory m ON c.id = m.conversation_id AND m.is_deleted = 0
WHERE c.user_id = 1 AND c.is_deleted = 0
GROUP BY c.id
ORDER BY c.update_time DESC;
