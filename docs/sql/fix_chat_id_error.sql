-- ========================================
-- 修复 chat_id 字段类型转换错误
-- ========================================

-- 步骤1: 检查表中是否有数据
SELECT COUNT(*) as total_rows FROM ai_chat_memory;

-- 步骤2: 查看 chat_id 的数据
SELECT DISTINCT chat_id FROM ai_chat_memory LIMIT 10;

-- ========================================
-- 解决方案 A: 如果表中没有数据（推荐）
-- ========================================

-- 删除表并重建
DROP TABLE IF EXISTS ai_chat_memory;

-- 重建表（使用新结构）
CREATE TABLE ai_chat_memory
(
    id              BIGINT AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    conversation_id BIGINT                                 NOT NULL COMMENT '对话ID',
    role            VARCHAR(20) DEFAULT 'user'             NOT NULL COMMENT '消息角色：user-用户，assistant-AI',
    content         TEXT                                   NOT NULL COMMENT '消息内容',
    create_time     DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time     DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT(1)  DEFAULT 0                 NOT NULL COMMENT '删除标记,0-未删除;1-已删除',
    user_id         BIGINT                                 NOT NULL COMMENT '用户id，对应 users 表的 id',
    
    CONSTRAINT fk_message_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversation (id)
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_message_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON UPDATE CASCADE ON DELETE CASCADE,
    
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_conversation_user (conversation_id, user_id)
)
    COMMENT '消息表' CHARSET = utf8mb4;


-- ========================================
-- 解决方案 B: 如果表中有数据需要保留
-- ========================================

-- B1. 先创建 conversation 表（如果还没创建）
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

-- B2. 为每个唯一的 chat_id 创建 conversation 记录
INSERT INTO conversation (user_id, title, create_time)
SELECT DISTINCT 
    user_id,
    CONCAT('对话-', SUBSTRING(chat_id, 1, 8)) as title,
    MIN(create_time) as create_time
FROM ai_chat_memory
WHERE is_del = 0
GROUP BY chat_id, user_id;

-- B3. 添加临时映射字段
ALTER TABLE ai_chat_memory 
    ADD COLUMN conversation_id BIGINT NULL COMMENT '对话ID' AFTER id,
    ADD COLUMN old_chat_id VARCHAR(32) NULL COMMENT '旧的chat_id备份';

-- B4. 备份旧的 chat_id
UPDATE ai_chat_memory SET old_chat_id = chat_id;

-- B5. 创建临时映射表
CREATE TEMPORARY TABLE chat_id_mapping AS
SELECT 
    m.chat_id,
    m.user_id,
    c.id as conversation_id
FROM (
    SELECT DISTINCT chat_id, user_id 
    FROM ai_chat_memory
) m
JOIN conversation c ON c.user_id = m.user_id
WHERE c.title = CONCAT('对话-', SUBSTRING(m.chat_id, 1, 8));

-- B6. 更新 conversation_id
UPDATE ai_chat_memory m
JOIN chat_id_mapping map ON m.chat_id = map.chat_id AND m.user_id = map.user_id
SET m.conversation_id = map.conversation_id;

-- B7. 检查是否有未映射的记录
SELECT COUNT(*) as unmapped_count 
FROM ai_chat_memory 
WHERE conversation_id IS NULL;

-- 如果有未映射的记录，需要手动处理或删除
-- DELETE FROM ai_chat_memory WHERE conversation_id IS NULL;

-- B8. 设置 conversation_id 为 NOT NULL
ALTER TABLE ai_chat_memory 
    MODIFY COLUMN conversation_id BIGINT NOT NULL COMMENT '对话ID';

-- B9. 删除旧的 chat_id 字段
ALTER TABLE ai_chat_memory DROP COLUMN chat_id;

-- B10. 重命名其他字段
ALTER TABLE ai_chat_memory 
    CHANGE COLUMN type role VARCHAR(20) DEFAULT 'user' NOT NULL COMMENT '消息角色：user-用户，assistant-AI',
    CHANGE COLUMN is_del is_deleted TINYINT(1) DEFAULT 0 NOT NULL COMMENT '删除标记,0-未删除;1-已删除';

-- B11. 添加外键约束
ALTER TABLE ai_chat_memory
    ADD CONSTRAINT fk_message_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversation (id)
            ON UPDATE CASCADE ON DELETE CASCADE;

-- 如果用户外键还在，先删除再重建
ALTER TABLE ai_chat_memory DROP FOREIGN KEY IF EXISTS fk_user;

ALTER TABLE ai_chat_memory
    ADD CONSTRAINT fk_message_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON UPDATE CASCADE ON DELETE CASCADE;

-- B12. 添加索引
CREATE INDEX idx_conversation_id ON ai_chat_memory (conversation_id);
CREATE INDEX idx_conversation_user ON ai_chat_memory (conversation_id, user_id);


-- ========================================
-- 解决方案 C: 如果你只是想保留 chat_id 为字符串
-- ========================================

-- 不修改 chat_id，保持 VARCHAR(32)
-- 只添加 conversation_id 作为新字段，两者并存

ALTER TABLE ai_chat_memory 
    ADD COLUMN conversation_id BIGINT NULL COMMENT '对话ID（新）' AFTER id;

-- 后续通过应用层逐步迁移到使用 conversation_id


-- ========================================
-- 验证脚本
-- ========================================

-- 查看表结构
DESC ai_chat_memory;

-- 查看数据
SELECT * FROM ai_chat_memory LIMIT 10;

-- 查看对话和消息的关系
SELECT 
    c.id as conversation_id,
    c.title,
    COUNT(m.id) as message_count
FROM conversation c
LEFT JOIN ai_chat_memory m ON c.id = m.conversation_id AND m.is_deleted = 0
WHERE c.is_deleted = 0
GROUP BY c.id
ORDER BY c.update_time DESC;
