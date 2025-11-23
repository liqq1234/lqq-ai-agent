-- 对话表
CREATE TABLE conversation
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
