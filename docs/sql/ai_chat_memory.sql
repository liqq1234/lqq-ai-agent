create table ai_chat_memory
(
    id          bigint auto_increment comment 'id'
        primary key,
    chat_id     varchar(32)                           not null comment '会话id',
    type        varchar(10) default 'user'            not null comment '消息类型',
    content     text                                  not null comment '消息内容',
    create_time datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime    default CURRENT_TIMESTAMP not null comment '更新时间',
    is_del      tinyint(1)  default 0                 not null comment '删除标记,0-未删除;1-已删除',
    user_id     bigint                                not null comment '用户id，对应 users 表的 id',
    constraint fk_user
        foreign key (user_id) references users (id)
            on update cascade on delete cascade
)
    charset = utf8mb4;

create index idx_chat_id
    on ai_chat_memory (chat_id);

create index idx_user_id
    on ai_chat_memory (user_id);