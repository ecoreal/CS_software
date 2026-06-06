-- 创建剧本相关表结构

-- 用户表 (如果不存在)
CREATE TABLE `users` (
    `Id` int NOT NULL AUTO_INCREMENT,
    `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `password_hash` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 剧本表
CREATE TABLE `script` (
                          `id` int NOT NULL AUTO_INCREMENT,
                          `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                          `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
                          `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
                          `last_updated` datetime(6) NOT NULL,

                          `user_id` int NOT NULL,
                          `Stage` int NOT NULL,
                          PRIMARY KEY (`id`),
                          KEY `IX_script_user_id` (`user_id`),
                          CONSTRAINT `FK_script_users_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`Id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 剧本历史表
CREATE TABLE `script_history` (
                                  `id` int NOT NULL AUTO_INCREMENT,
                                  `message` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
                                  `response` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
                                  `created_at` datetime(6) NOT NULL,
                                  `script_id` int NOT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `IX_script_history_script_id` (`script_id`),
                                  CONSTRAINT `FK_script_history_script_script_id` FOREIGN KEY (`script_id`) REFERENCES `script` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 剧本分析表  
CREATE TABLE `script_analysis` (
                                   `id` int NOT NULL AUTO_INCREMENT,
                                   `analysis_result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
                                   `analyzed_at` datetime(6) NOT NULL,
                                   `script_id` int NOT NULL,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `IX_script_analysis_script_id` (`script_id`),
                                   CONSTRAINT `FK_script_analysis_script_script_id` FOREIGN KEY (`script_id`) REFERENCES `script` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 可视化元素表
CREATE TABLE `visual_element` (
                                  `id` int NOT NULL AUTO_INCREMENT,
                                  `type` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                  `description` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
                                  `image_url` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
                                  `image_generated_at` datetime(6) DEFAULT NULL,
                                  `script_id` int NOT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `IX_visual_element_script_id` (`script_id`),
                                  CONSTRAINT `FK_visual_element_script_script_id` FOREIGN KEY (`script_id`) REFERENCES `script` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=152 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `room` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '房间ID',
                        `name` varchar(100) NOT NULL COMMENT '房间名称',
                        `description` varchar(500) DEFAULT NULL COMMENT '房间描述',
                        `havePwd` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否有密码，0无，1有',
                        `password` varchar(16) DEFAULT NULL COMMENT '房间密码',
                        `owner_id` bigint NOT NULL COMMENT '房主ID，关联user表的id',
                        `current_members` int DEFAULT 1 COMMENT '当前成员数',
                        `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间信息表';


CREATE TABLE `room_member` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `room_id` bigint NOT NULL COMMENT '房间ID',
                               `user_id` bigint NOT NULL COMMENT '用户ID',
                               `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                               `role` tinyint DEFAULT 0 COMMENT '成员角色(0-普通成员，1-管理员，2-房主)',
                               `last_active_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_room_user` (`room_id`,`user_id`),
                               KEY `idx_user_id` (`user_id`),
                               KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间成员表';




