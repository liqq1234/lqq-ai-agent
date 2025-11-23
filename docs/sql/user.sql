create table users
(
    id         bigint auto_increment comment '用户ID'
        primary key,
    user_name  varchar(50) default 'user'            not null,
    password   varchar(100)                          not null comment '密码（加密存储，建议用bcrypt/MD5+salt）',
    email      varchar(100)                          null comment '邮箱',
    phone      varchar(20)                           null comment '手机号',
    role       varchar(20) default 'user'            not null,
    status     varchar(20) default 'enabled'         not null comment '用户状态：enabled-启用，disabled-禁用',
    created_at datetime    default CURRENT_TIMESTAMP null comment '注册时间',
    updated_at datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    last_login datetime                              null comment '最后登录时间',
    deleted    tinyint     default 0                 null comment '逻辑删除：0未删除，1已删除',
    constraint email
        unique (email),
    constraint phone
        unique (phone),
    constraint chk_role
        check (`role` in (_utf8mb4\'user\',_utf8mb4\'admin\'))
)
comment '用户表' charset=utf8mb4;