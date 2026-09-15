-- AI算法测评获客系统 数据库初始化脚本
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4

CREATE DATABASE IF NOT EXISTS ai_assessment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_assessment;

-- =============================================
-- 1. 管理员表
-- =============================================
CREATE TABLE IF NOT EXISTS `admin_user` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`     VARCHAR(50)  NOT NULL COMMENT '用户名',
  `password`     VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname`     VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `role`         VARCHAR(20)  NOT NULL DEFAULT 'admin' COMMENT '角色：admin/super_admin',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `last_login_at` DATETIME    DEFAULT NULL COMMENT '最后登录时间',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- =============================================
-- 2. 题目表
-- =============================================
CREATE TABLE IF NOT EXISTS `question` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`        TEXT         NOT NULL COMMENT '题目内容',
  `options_json` JSON         NOT NULL COMMENT '选项列表，格式：[{"key":"A","text":"..."},...]',
  `correct_answer` VARCHAR(5) NOT NULL COMMENT '正确答案（A/B/C/D）',
  `category`     VARCHAR(50)  NOT NULL COMMENT '题目分类（如：机器学习/深度学习/数据处理/算法基础）',
  `score`        INT          NOT NULL DEFAULT 5 COMMENT '该题分值',
  `sort_order`   INT          NOT NULL DEFAULT 0 COMMENT '排序权重，越小越靠前',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评题目表';

-- =============================================
-- 3. 答题会话表
-- =============================================
CREATE TABLE IF NOT EXISTS `exam_session` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid`       VARCHAR(100) NOT NULL COMMENT '微信openid',
  `total_score`  INT          DEFAULT 0 COMMENT '总得分',
  `total_questions` INT       DEFAULT 0 COMMENT '总题数',
  `correct_count` INT         DEFAULT 0 COMMENT '答对题数',
  `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0进行中 1已完成',
  `start_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time`     DATETIME     DEFAULT NULL COMMENT '结束时间',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_openid` (`openid`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题会话表';

-- =============================================
-- 4. 答题记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `exam_answer` (
  `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id`      BIGINT   NOT NULL COMMENT '会话ID',
  `question_id`     BIGINT   NOT NULL COMMENT '题目ID',
  `selected_option` VARCHAR(5) NOT NULL COMMENT '用户选择的答案',
  `correct_answer`  VARCHAR(5) NOT NULL COMMENT '正确答案（冗余存储）',
  `is_correct`      TINYINT  NOT NULL DEFAULT 0 COMMENT '是否正确：1是 0否',
  `score`           INT      NOT NULL DEFAULT 0 COMMENT '得分',
  `category`        VARCHAR(50) DEFAULT NULL COMMENT '题目分类（冗余）',
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题记录表';

-- =============================================
-- 5. 测评报告表
-- =============================================
CREATE TABLE IF NOT EXISTS `assessment_report` (
  `id`               BIGINT    NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id`       BIGINT    NOT NULL COMMENT '会话ID',
  `openid`           VARCHAR(100) NOT NULL COMMENT '微信openid',
  `total_score`      INT       NOT NULL DEFAULT 0 COMMENT '总分',
  `level`            VARCHAR(20) NOT NULL COMMENT '能力等级：入门级/进阶级/专家级',
  `level_code`       VARCHAR(10) NOT NULL COMMENT '等级代码：beginner/intermediate/expert',
  `summary`          TEXT      NOT NULL COMMENT '综合评价文字',
  `category_scores_json` JSON  NOT NULL COMMENT '各分类得分，格式：[{"category":"机器学习","score":80,"total":100},...]',
  `suggestions_json` JSON      NOT NULL COMMENT '个性化建议列表',
  `created_at`       DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_openid` (`openid`),
  KEY `idx_level_code` (`level_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评报告表';

-- =============================================
-- 6. 线索（留资）表
-- =============================================
CREATE TABLE IF NOT EXISTS `lead` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id`   BIGINT       DEFAULT NULL COMMENT '关联会话ID',
  `openid`       VARCHAR(100) NOT NULL COMMENT '微信openid',
  `phone`        VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `nickname`     VARCHAR(100) DEFAULT NULL COMMENT '微信昵称',
  `avatar_url`   VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `level`        VARCHAR(20)  DEFAULT NULL COMMENT '测评等级（冗余）',
  `total_score`  INT          DEFAULT NULL COMMENT '测评总分（冗余）',
  `source`       VARCHAR(50)  DEFAULT 'miniprogram' COMMENT '来源渠道',
  `remark`       VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `follow_status` TINYINT     NOT NULL DEFAULT 0 COMMENT '跟进状态：0未跟进 1跟进中 2已转化',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_openid` (`openid`),
  KEY `idx_phone` (`phone`),
  KEY `idx_follow_status` (`follow_status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索留资表';

-- =============================================
-- 7. 系统配置表（老师微信/群活码/课程配置）
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_key`  VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT        NOT NULL COMMENT '配置值',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '说明',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =============================================
-- 8. 用户行为记录表（点击行为追踪）
-- =============================================
CREATE TABLE IF NOT EXISTS `user_action` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid`      VARCHAR(100) NOT NULL COMMENT '微信openid',
  `action_type` VARCHAR(50)  NOT NULL COMMENT '行为类型：add_wechat/scan_group/trial_course/buy_course/save_qrcode/copy_wechat',
  `extra`       VARCHAR(500) DEFAULT NULL COMMENT '附加信息（JSON）',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_openid` (`openid`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为记录表';

-- =============================================
-- lead 表扩展字段（已有表执行 ALTER，新建直接包含）
-- =============================================
ALTER TABLE `lead`
  ADD COLUMN IF NOT EXISTS `wechat`              VARCHAR(100) DEFAULT NULL COMMENT '微信号' AFTER `phone`,
  ADD COLUMN IF NOT EXISTS `material_claimed`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否领取资料：1是 0否' AFTER `source`,
  ADD COLUMN IF NOT EXISTS `material_claimed_at` DATETIME     DEFAULT NULL COMMENT '领取资料时间' AFTER `material_claimed`;

-- =============================================
-- 初始数据：管理员账号
-- 默认账号：admin / admin123（BCrypt加密）
-- =============================================
INSERT INTO `admin_user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin', 'super_admin')
ON DUPLICATE KEY UPDATE `username` = `username`;

-- =============================================
-- 初始数据：系统配置（老师微信/群活码/课程）
-- =============================================
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`) VALUES
('teacher_wechat',      'AI_Teacher_001',                                    '老师微信号'),
('teacher_qrcode_url',  '/images/teacher-qrcode.png',                        '老师微信二维码图片路径'),
('group_qrcode_url',    '/images/group-qrcode.png',                          '学习群活码图片路径'),
('group_expire_tip',    '活码每7天更新，扫码加群即可',                          '群活码说明文字'),
('course_name',         'AI算法工程师系统课',                                  '课程名称'),
('course_price',        '1299',                                               '课程原价（元）'),
('course_discount',     '限时优惠价 ¥799',                                    '优惠文案'),
('course_desc',         '从Python基础到模型部署，30天掌握AI算法核心技能',        '课程简介'),
('trial_url',           'https://www.xiaoe-tech.com/trial',                  '试听课链接（小鹅通）'),
('buy_url',             'https://www.xiaoe-tech.com/buy',                    '购买链接（小鹅通）'),
('material_list',       '["AI算法路线图PDF","Python数据分析模板","机器学习速查表","PyTorch模板","面试题100道","实战案例集"]', '资料包列表（JSON数组）')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);

-- =============================================
-- 初始数据：示例题目（20道单选题，4个分类各5题）
-- =============================================
INSERT INTO `question` (`title`, `options_json`, `correct_answer`, `category`, `score`, `sort_order`) VALUES

-- 机器学习（5题）
('以下哪种算法属于监督学习？',
 '[{"key":"A","text":"K-Means聚类"},{"key":"B","text":"线性回归"},{"key":"C","text":"主成分分析（PCA）"},{"key":"D","text":"自编码器"}]',
 'B', '机器学习', 5, 1),

('过拟合（Overfitting）的主要表现是？',
 '[{"key":"A","text":"训练误差高，测试误差低"},{"key":"B","text":"训练误差低，测试误差高"},{"key":"C","text":"训练误差和测试误差都高"},{"key":"D","text":"训练误差和测试误差都低"}]',
 'B', '机器学习', 5, 2),

('正则化（Regularization）的主要作用是？',
 '[{"key":"A","text":"加速模型训练"},{"key":"B","text":"增加模型复杂度"},{"key":"C","text":"防止过拟合"},{"key":"D","text":"提高数据质量"}]',
 'C', '机器学习', 5, 3),

('交叉验证（Cross-Validation）的主要目的是？',
 '[{"key":"A","text":"增加训练数据量"},{"key":"B","text":"评估模型泛化能力"},{"key":"C","text":"加速模型训练"},{"key":"D","text":"降低特征维度"}]',
 'B', '机器学习', 5, 4),

('随机森林（Random Forest）是以下哪种集成方法？',
 '[{"key":"A","text":"Boosting"},{"key":"B","text":"Stacking"},{"key":"C","text":"Bagging"},{"key":"D","text":"Blending"}]',
 'C', '机器学习', 5, 5),

-- 深度学习（5题）
('卷积神经网络（CNN）最擅长处理哪类数据？',
 '[{"key":"A","text":"时序数据"},{"key":"B","text":"图像数据"},{"key":"C","text":"文本数据"},{"key":"D","text":"图结构数据"}]',
 'B', '深度学习', 5, 6),

('Dropout技术在神经网络中的作用是？',
 '[{"key":"A","text":"加速收敛"},{"key":"B","text":"增加网络深度"},{"key":"C","text":"防止过拟合"},{"key":"D","text":"提高学习率"}]',
 'C', '深度学习', 5, 7),

('批归一化（Batch Normalization）的主要优点是？',
 '[{"key":"A","text":"减少参数数量"},{"key":"B","text":"加速训练并稳定梯度"},{"key":"C","text":"增加模型可解释性"},{"key":"D","text":"降低计算复杂度"}]',
 'B', '深度学习', 5, 8),

('ReLU激活函数相比Sigmoid的主要优势是？',
 '[{"key":"A","text":"输出范围更广"},{"key":"B","text":"缓解梯度消失问题"},{"key":"C","text":"计算更复杂"},{"key":"D","text":"适合二分类输出层"}]',
 'B', '深度学习', 5, 9),

('Transformer架构的核心机制是？',
 '[{"key":"A","text":"卷积操作"},{"key":"B","text":"循环连接"},{"key":"C","text":"自注意力机制"},{"key":"D","text":"池化操作"}]',
 'C', '深度学习', 5, 10),

-- 数据处理（5题）
('处理数值型特征缺失值，以下哪种方法最常用？',
 '[{"key":"A","text":"直接删除该特征"},{"key":"B","text":"用均值/中位数填充"},{"key":"C","text":"用0填充"},{"key":"D","text":"用最大值填充"}]',
 'B', '数据处理', 5, 11),

('特征归一化（Normalization）的主要目的是？',
 '[{"key":"A","text":"增加特征数量"},{"key":"B","text":"消除量纲影响，加速收敛"},{"key":"C","text":"去除异常值"},{"key":"D","text":"增加数据量"}]',
 'B', '数据处理', 5, 12),

('处理类别不平衡问题，以下哪种方法属于过采样？',
 '[{"key":"A","text":"随机欠采样"},{"key":"B","text":"SMOTE算法"},{"key":"C","text":"调整决策阈值"},{"key":"D","text":"代价敏感学习"}]',
 'B', '数据处理', 5, 13),

('One-Hot编码主要用于处理哪类特征？',
 '[{"key":"A","text":"连续数值特征"},{"key":"B","text":"有序类别特征"},{"key":"C","text":"无序类别特征"},{"key":"D","text":"时序特征"}]',
 'C', '数据处理', 5, 14),

('以下哪种方法可以有效检测数值型特征中的异常值？',
 '[{"key":"A","text":"One-Hot编码"},{"key":"B","text":"3σ原则或IQR方法"},{"key":"C","text":"主成分分析"},{"key":"D","text":"标签编码"}]',
 'B', '数据处理', 5, 15),

-- 算法基础（5题）
('时间复杂度O(n log n)对应以下哪种排序算法？',
 '[{"key":"A","text":"冒泡排序"},{"key":"B","text":"插入排序"},{"key":"C","text":"快速排序（平均）"},{"key":"D","text":"选择排序"}]',
 'C', '算法基础', 5, 16),

('二分查找的前提条件是？',
 '[{"key":"A","text":"数据无序"},{"key":"B","text":"数据有序"},{"key":"C","text":"数据无重复"},{"key":"D","text":"数据量较小"}]',
 'B', '算法基础', 5, 17),

('动态规划（Dynamic Programming）的核心思想是？',
 '[{"key":"A","text":"分治递归"},{"key":"B","text":"贪心选择"},{"key":"C","text":"记忆化存储子问题结果"},{"key":"D","text":"回溯枚举"}]',
 'C', '算法基础', 5, 18),

('图的广度优先搜索（BFS）通常使用哪种数据结构？',
 '[{"key":"A","text":"栈（Stack）"},{"key":"B","text":"队列（Queue）"},{"key":"C","text":"堆（Heap）"},{"key":"D","text":"哈希表"}]',
 'B', '算法基础', 5, 19),

('哈希表（Hash Table）的平均查找时间复杂度是？',
 '[{"key":"A","text":"O(n)"},{"key":"B","text":"O(log n)"},{"key":"C","text":"O(1)"},{"key":"D","text":"O(n²)"}]',
 'C', '算法基础', 5, 20);
