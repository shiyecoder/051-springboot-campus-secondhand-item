-- ============================================================================
-- NEW-051 校园二手物品交易平台 · 数据库初始化脚本
-- MySQL 8 / utf8mb4 ｜ 执行：mysql -uroot -p123456 < sql/init.sql
-- 生成器：sql/gen_sql.py（改表结构请改生成器后重跑，勿手改本文件）
-- ============================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
DROP DATABASE IF EXISTS new_051;
CREATE DATABASE new_051 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE new_051;


-- 商品分类 category
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '0表示一级分类，最多两级',
  `category_code` varchar(30) NOT NULL COMMENT '分类编码，大写字母与下划线',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标相对路径',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `is_hot` tinyint NOT NULL DEFAULT 0 COMMENT '1首页热门 0普通',
  `remark` varchar(200) DEFAULT NULL COMMENT '运营备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,UNIQUE KEY uk_category_code (category_code)
  ,KEY idx_parent_sort (parent_id, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

-- 校区面交点 campus_place
CREATE TABLE `campus_place` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `campus_id` int NOT NULL COMMENT '校区编号1东2西3北',
  `campus_name` varchar(50) NOT NULL COMMENT '校区名称',
  `place_name` varchar(80) NOT NULL COMMENT '面交点名称',
  `address_desc` varchar(200) NOT NULL COMMENT '位置说明',
  `open_start` time NOT NULL DEFAULT '08:00:00' COMMENT '开放开始时间',
  `open_end` time NOT NULL DEFAULT '21:00:00' COMMENT '开放结束时间',
  `manager_name` varchar(30) DEFAULT NULL COMMENT '现场联系人，仅运营可见',
  `manager_phone` varchar(20) DEFAULT NULL COMMENT '联系电话，前台脱敏',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_campus_status (campus_name, status)
  ,KEY idx_place_name (place_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校区面交点';

-- 闲置商品 goods
CREATE TABLE `goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_no` varchar(32) NOT NULL COMMENT 'XY+yyMMdd+4位序号',
  `seller_id` bigint NOT NULL COMMENT '卖家 sys_user.id',
  `category_id` bigint NOT NULL COMMENT '二级分类 category.id',
  `title` varchar(120) NOT NULL COMMENT '标题',
  `description` text NOT NULL COMMENT '商品描述，10-1000字',
  `condition_level` varchar(20) NOT NULL DEFAULT '九成新' COMMENT '全新/九成新/七成新/五成新/有瑕疵',
  `price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '出售价',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '入手价，用于折价率',
  `campus_id` int NOT NULL COMMENT '校区编号',
  `campus_name` varchar(50) NOT NULL COMMENT '校区名称（冗余便于列表展示）',
  `trade_place_id` bigint DEFAULT NULL COMMENT '面交点 campus_place.id',
  `trade_type` varchar(20) NOT NULL DEFAULT '面交' COMMENT '面交/自提/可送',
  `status` varchar(20) NOT NULL DEFAULT '待审核' COMMENT '待审核/在售/已下架/已售出/审核驳回/冻结',
  `audit_remark` varchar(200) DEFAULT NULL COMMENT '审核意见，驳回时必填',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览量',
  `favorite_count` int NOT NULL DEFAULT 0 COMMENT '收藏量，用于热度评分',
  `cover_url` varchar(255) NOT NULL COMMENT '封面图相对路径',
  `image_urls` varchar(1000) DEFAULT NULL COMMENT '实拍图，相对路径逗号分隔，最多6张',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,UNIQUE KEY uk_goods_no (goods_no)
  ,KEY idx_category_status (category_id, status)
  ,KEY idx_seller_status (seller_id, status)
  ,KEY idx_campus_status (campus_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闲置商品';

-- 交易订单 trade_order
CREATE TABLE `trade_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(32) NOT NULL COMMENT 'DD+yyMMdd+6位序号',
  `goods_id` bigint NOT NULL COMMENT '商品 goods.id',
  `goods_title` varchar(120) NOT NULL COMMENT '商品标题快照',
  `goods_cover` varchar(255) NOT NULL COMMENT '商品封面快照',
  `buyer_id` bigint NOT NULL COMMENT '买家 sys_user.id',
  `seller_id` bigint NOT NULL COMMENT '卖家，下单时快照',
  `deal_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '成交价，卖家确认时填写',
  `pay_type` varchar(20) NOT NULL DEFAULT '现金' COMMENT '现金/转账/其他',
  `appoint_time` datetime NOT NULL COMMENT '面交时间',
  `appoint_place` varchar(120) NOT NULL COMMENT '面交点',
  `status` varchar(20) NOT NULL DEFAULT '待卖家确认' COMMENT '待卖家确认/待面交/已完成/已取消/纠纷中',
  `cancel_reason` varchar(200) DEFAULT NULL COMMENT '取消原因',
  `buyer_confirm_time` datetime DEFAULT NULL COMMENT '买家确认收货时间',
  `seller_confirm_time` datetime DEFAULT NULL COMMENT '卖家接单时间',
  `lock_expire_time` datetime DEFAULT NULL COMMENT '商品锁定到期时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,UNIQUE KEY uk_order_no (order_no)
  ,KEY idx_buyer_status (buyer_id, status)
  ,KEY idx_goods_status (goods_id, status)
  ,KEY idx_appoint_time (appoint_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单';

-- 交易评价 review
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单 trade_order.id',
  `from_user_id` bigint NOT NULL COMMENT '评价人',
  `to_user_id` bigint NOT NULL COMMENT '被评人',
  `score` tinyint NOT NULL DEFAULT 5 COMMENT '1至5星',
  `tags` varchar(200) DEFAULT NULL COMMENT '描述一致/准时/好沟通/包装完好',
  `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `anonymous` tinyint NOT NULL DEFAULT 0 COMMENT '0显示昵称 1匿名',
  `append_content` varchar(500) DEFAULT NULL COMMENT '追评，30天内一次',
  `status` varchar(20) NOT NULL DEFAULT '待审核' COMMENT '待审核/已展示/已隐藏',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,UNIQUE KEY uk_order_from (order_id, from_user_id)
  ,KEY idx_to_user (to_user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易评价';

-- 举报与纠纷 report
CREATE TABLE `report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `report_no` varchar(32) NOT NULL COMMENT 'JB+yyMMdd+4位序号',
  `biz_type` varchar(20) NOT NULL COMMENT '商品/订单/用户',
  `biz_id` bigint NOT NULL COMMENT '按类型关联 goods/trade_order/sys_user',
  `biz_title` varchar(200) DEFAULT NULL COMMENT '关联对象标题快照',
  `reporter_id` bigint NOT NULL COMMENT '举报人',
  `reason_type` varchar(30) NOT NULL COMMENT '违禁品/虚假描述/诈骗/骚扰/校外人员',
  `description` varchar(500) NOT NULL COMMENT '问题描述，不少于10字',
  `evidence_urls` varchar(1000) DEFAULT NULL COMMENT '证据图片，最多6张',
  `status` varchar(20) NOT NULL DEFAULT '待受理' COMMENT '待受理/处理中/已处理/已驳回/申诉中',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人',
  `handler_name` varchar(30) DEFAULT NULL COMMENT '处理人姓名',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `appeal_status` varchar(20) NOT NULL DEFAULT '无' COMMENT '无/申诉中/申诉通过/申诉驳回',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,UNIQUE KEY uk_report_no (report_no)
  ,KEY idx_status_create (status, create_time)
  ,KEY idx_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报与纠纷';

-- 信用分流水 credit_log
CREATE TABLE `credit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户 sys_user.id',
  `change_value` int NOT NULL DEFAULT 0 COMMENT '正数加分，负数扣分',
  `before_score` int NOT NULL DEFAULT 100 COMMENT '变化前分值',
  `after_score` int NOT NULL DEFAULT 100 COMMENT '变化后分值',
  `reason_type` varchar(30) NOT NULL COMMENT '交易好评/差评/放鸽子/违规下架/人工调整',
  `biz_no` varchar(32) DEFAULT NULL COMMENT '订单号或举报编号',
  `remark` varchar(200) DEFAULT NULL COMMENT '说明',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人，系统自动为空',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_user_create (user_id, create_time)
  ,KEY idx_biz_no (biz_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用分流水';

-- 定价评分规则版本 price_rule
CREATE TABLE `price_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_name` varchar(60) NOT NULL COMMENT '规则名称',
  `rule_version` varchar(20) NOT NULL COMMENT '规则版本号，如 v1/v2',
  `weight_category` int NOT NULL DEFAULT 30 COMMENT '同分类成交价偏离度权重%',
  `weight_condition` int NOT NULL DEFAULT 20 COMMENT '成色权重%',
  `weight_supply` int NOT NULL DEFAULT 15 COMMENT '同校同类在售数量权重%',
  `weight_heat` int NOT NULL DEFAULT 15 COMMENT '发布时长与浏览转化权重%',
  `weight_credit` int NOT NULL DEFAULT 10 COMMENT '卖家信用分权重%',
  `weight_info` int NOT NULL DEFAULT 10 COMMENT '图文完整度权重%',
  `status` varchar(20) NOT NULL DEFAULT '草稿' COMMENT '草稿/启用/停用',
  `effect_time` datetime DEFAULT NULL COMMENT '生效时间',
  `remark` varchar(200) DEFAULT NULL COMMENT '变更说明',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定价评分规则版本';

-- 定价评分记录 price_calc
CREATE TABLE `price_calc` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_id` bigint NOT NULL COMMENT '商品 goods.id',
  `rule_id` bigint NOT NULL COMMENT '规则 price_rule.id',
  `rule_version` varchar(20) NOT NULL COMMENT '计算时使用的规则版本',
  `snapshot` text COMMENT '输入快照 JSON',
  `score_category` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '分类价格偏离得分',
  `score_condition` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '成色得分',
  `score_supply` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '校区供需得分',
  `score_heat` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '热度得分',
  `score_credit` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '信用得分',
  `score_info` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '图文完整度得分',
  `total_score` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '加权总分',
  `grade` varchar(5) NOT NULL COMMENT 'A/B/C',
  `suggest_low` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '建议价下限',
  `suggest_high` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '建议价上限',
  `conclusion` varchar(300) DEFAULT NULL COMMENT '推荐结论',
  `factors` varchar(1000) DEFAULT NULL COMMENT '主要影响因素',
  `missing` varchar(500) DEFAULT NULL COMMENT '未满足条件',
  `corrected_grade` varchar(5) DEFAULT NULL COMMENT '人工修正后的等级',
  `correct_reason` varchar(200) DEFAULT NULL COMMENT '修正原因，不少于5字',
  `correct_by` bigint DEFAULT NULL COMMENT '修正人',
  `correct_time` datetime DEFAULT NULL COMMENT '修正时间',
  `adopted` tinyint NOT NULL DEFAULT 0 COMMENT '卖家是否采纳建议价 0未采纳1采纳',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_goods (goods_id)
  ,KEY idx_rule (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定价评分记录';

-- 用户表 sys_user
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) NOT NULL COMMENT '登录名',
  `password` varchar(100) NOT NULL COMMENT 'BCrypt 密码',
  `real_name` varchar(50) NOT NULL COMMENT '姓名/昵称',
  `student_no` varchar(30) DEFAULT NULL COMMENT '学号，学生账号导入时绑定',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(80) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像相对路径',
  `gender` varchar(10) DEFAULT NULL COMMENT '男/女',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门（运营人员）',
  `campus_id` int DEFAULT NULL COMMENT '绑定校区编号',
  `campus_name` varchar(50) DEFAULT NULL COMMENT '绑定校区名称',
  `credit_score` int NOT NULL DEFAULT 100 COMMENT '信用分 0-150',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `login_fail_count` int NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `lock_until` varchar(30) DEFAULT NULL COMMENT '锁定至',
  `last_login` varchar(30) DEFAULT NULL COMMENT '最近登录时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,UNIQUE KEY uk_username (username)
  ,KEY idx_campus (campus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表 sys_role
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `data_scope` varchar(20) NOT NULL DEFAULT 'self' COMMENT 'all/dept/self',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色表 sys_user_role
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色表';

-- 菜单权限表 sys_menu
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '上级菜单',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `path` varchar(120) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(120) DEFAULT NULL COMMENT '组件路径',
  `perm` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `menu_type` varchar(20) NOT NULL DEFAULT 'menu' COMMENT 'catalog/menu/button',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标键',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '1显示 0隐藏',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 角色菜单表 sys_role_menu
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单表';

-- 字典类型表 sys_dict_type
CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_name` varchar(60) NOT NULL COMMENT '字典名称',
  `dict_type` varchar(60) NOT NULL COMMENT '字典类型编码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 字典数据表 sys_dict_data
CREATE TABLE `sys_dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type` varchar(60) NOT NULL COMMENT '字典类型编码',
  `dict_label` varchar(60) NOT NULL COMMENT '字典标签',
  `dict_value` varchar(60) NOT NULL COMMENT '字典值',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 文件表 sys_file
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_name` varchar(120) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(255) NOT NULL COMMENT '相对路径',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '字节数',
  `file_type` varchar(50) DEFAULT NULL COMMENT '扩展名',
  `biz_type` varchar(30) DEFAULT NULL COMMENT '业务类型 goods/report/avatar',
  `biz_id` bigint DEFAULT NULL COMMENT '业务ID',
  `uploader_id` bigint DEFAULT NULL COMMENT '上传人',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- 公告表 sys_notice
CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(120) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `notice_type` varchar(20) NOT NULL DEFAULT '平台公告' COMMENT '平台公告/交易提醒/规则变更',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `publisher` varchar(50) DEFAULT NULL COMMENT '发布人',
  `status` varchar(20) NOT NULL DEFAULT '已发布' COMMENT '草稿/已发布/已下线',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 站内消息表 sys_message
CREATE TABLE `sys_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `receiver_id` bigint NOT NULL COMMENT '接收人',
  `title` varchar(120) NOT NULL COMMENT '标题',
  `content` varchar(500) NOT NULL COMMENT '内容',
  `biz_type` varchar(30) DEFAULT NULL COMMENT 'goods/order/report/credit',
  `biz_id` bigint DEFAULT NULL COMMENT '业务ID',
  `msg_type` varchar(20) NOT NULL DEFAULT '系统通知' COMMENT '审核/订单/纠纷/信用/系统通知',
  `read_flag` tinyint NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_receiver (receiver_id, read_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- 操作日志表 sys_oper_log
CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `module` varchar(50) NOT NULL COMMENT '模块',
  `oper_type` varchar(30) NOT NULL COMMENT '操作类型',
  `biz_no` varchar(40) DEFAULT NULL COMMENT '业务编号',
  `content` varchar(500) NOT NULL COMMENT '操作内容',
  `oper_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `oper_name` varchar(50) DEFAULT NULL COMMENT '操作人',
  `oper_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `ip` varchar(40) DEFAULT NULL COMMENT 'IP',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
  ,KEY idx_time (oper_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 商品分类：6 个一级 + 18 个二级
INSERT INTO `category` (`id`, `parent_id`, `category_code`, `name`, `icon`, `sort_no`, `status`, `is_hot`, `remark`) VALUES
(1, 0, 'ELEC', '电子产品', NULL, 1, 1, 1, NULL),
(2, 1, 'ELEC_PHONE', '手机平板', NULL, 10, 1, 0, NULL),
(3, 1, 'ELEC_PC', '电脑配件', NULL, 10, 1, 0, NULL),
(4, 1, 'ELEC_AUDIO', '耳机音响', NULL, 10, 1, 0, NULL),
(5, 1, 'ELEC_OTHER', '数码周边', NULL, 10, 1, 0, NULL),
(6, 0, 'BOOK', '图书教材', NULL, 2, 1, 1, NULL),
(7, 6, 'BOOK_TEXT', '教材教辅', NULL, 10, 1, 0, NULL),
(8, 6, 'BOOK_EXAM', '考研考证', NULL, 10, 1, 0, NULL),
(9, 6, 'BOOK_NOVEL', '文学小说', NULL, 10, 1, 0, NULL),
(10, 0, 'LIFE', '生活用品', NULL, 3, 1, 1, NULL),
(11, 10, 'LIFE_DORM', '宿舍用品', NULL, 10, 1, 0, NULL),
(12, 10, 'LIFE_APPL', '小家电', NULL, 10, 1, 0, NULL),
(13, 10, 'LIFE_BIKE', '代步工具', NULL, 10, 1, 0, NULL),
(14, 0, 'SPORT', '运动户外', NULL, 4, 1, 1, NULL),
(15, 14, 'SPORT_FIT', '健身器材', NULL, 10, 1, 0, NULL),
(16, 14, 'SPORT_BALL', '球类装备', NULL, 10, 1, 0, NULL),
(17, 14, 'SPORT_OUT', '户外露营', NULL, 10, 1, 0, NULL),
(18, 0, 'CLOTH', '服饰鞋包', NULL, 5, 1, 0, NULL),
(19, 18, 'CLOTH_TOP', '上装', NULL, 10, 1, 0, NULL),
(20, 18, 'CLOTH_SHOE', '鞋靴', NULL, 10, 1, 0, NULL),
(21, 0, 'STUDY', '学习文具', NULL, 6, 1, 0, NULL),
(22, 21, 'STUDY_DESK', '桌面文具', NULL, 10, 1, 0, NULL),
(23, 21, 'STUDY_DRAW', '画材乐器', NULL, 10, 1, 0, NULL);

-- 校区与面交点
INSERT INTO `campus_place` (`campus_id`, `campus_name`, `place_name`, `address_desc`, `open_start`, `open_end`, `manager_name`, `manager_phone`, `status`) VALUES
(1, '东校区', '图书馆一层大厅', '图书馆正门进，服务台右侧休息区', '08:00:00', '21:30:00', '李文', '13800001001', 1),
(1, '东校区', '一食堂门口广场', '一食堂南门，靠近快递驿站', '07:00:00', '20:00:00', '王倩', '13800001002', 1),
(1, '东校区', '三号宿舍楼下', '三号楼门厅，宿管值班室旁', '08:30:00', '22:00:00', '张磊', '13800001003', 1),
(2, '西校区', '体育馆东门', '体育馆东侧入口，雨天改到馆内前厅', '09:00:00', '21:00:00', '陈晨', '13800001004', 1),
(2, '西校区', '西区快递中心', '快递中心取件口外侧长椅区', '08:00:00', '20:30:00', '赵敏', '13800001005', 1),
(2, '西校区', '实验楼A座大厅', 'A 座一层门禁外侧', '08:00:00', '18:00:00', NULL, NULL, 0),
(3, '北校区', '北门警卫室旁', '北门内 20 米，警务室窗口', '07:30:00', '22:00:00', '孙浩', '13800001006', 1),
(3, '北校区', '北区食堂二楼', '二楼靠窗自习区', '10:00:00', '20:00:00', '周琳', '13800001007', 1);

-- 角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `data_scope`, `status`, `remark`) VALUES
(1, '平台管理员', 'ROLE_ADMIN', 'all', 1, '管理用户、角色、菜单、字典、公告、参数和操作日志'),
(2, '校园运营人员', 'ROLE_OPERATOR', 'dept', 1, '审核商品、维护分类与面交点、受理举报、调整信用分'),
(3, '学生卖家', 'ROLE_SELLER', 'self', 1, '发布闲置、回复咨询、确认面交、下架商品、查看信用分'),
(4, '学生买家', 'ROLE_BUYER', 'self', 1, '浏览检索、收藏商品、下单、确认收货、评价和举报');

-- 用户：默认账号 admin/operator/user，密码统一 123456（BCrypt）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `student_no`, `phone`, `email`, `avatar`, `gender`, `dept_id`, `campus_id`, `campus_name`, `credit_score`, `status`) VALUES
(1, 'admin', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '系统管理员', NULL, '13900000001', 'admin@campus.edu', '/uploads/seed/avatar/01.jpg', '男', 1, 1, '东校区', 100, 1),
(2, 'operator', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '林晓运营', 'OP2026001', '13900000002', 'linxiao@campus.edu', '/uploads/seed/avatar/02.jpg', '女', 2, 1, '东校区', 100, 1),
(3, 'user', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '陈同学', '2023010101', '13900000003', 'chen@campus.edu', '/uploads/seed/avatar/03.jpg', '男', NULL, 1, '东校区', 104, 1),
(4, 'seller01', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '周子航', '2023010203', '13900000004', 'zhouzh@campus.edu', '/uploads/seed/avatar/04.jpg', '男', NULL, 1, '东校区', 128, 1),
(5, 'seller02', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '苏明玉', '2023020108', '13900000005', 'sumy@campus.edu', '/uploads/seed/avatar/05.jpg', '女', NULL, 2, '西校区', 116, 1),
(6, 'seller03', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '何嘉禾', '2022030412', '13900000006', 'hejh@campus.edu', '/uploads/seed/avatar/06.jpg', '女', NULL, 3, '北校区', 92, 1),
(7, 'seller04', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '郑一鸣', '2023040517', '13900000007', 'zhengyiming@campus.edu', '/uploads/seed/avatar/07.jpg', '男', NULL, 1, '东校区', 78, 1),
(8, 'seller05', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '许安然', '2023050122', '13900000008', 'xuar@campus.edu', '/uploads/seed/avatar/08.jpg', '女', NULL, 2, '西校区', 46, 1),
(9, 'buyer01', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '李思远', '2023010511', '13900000009', 'lisy@campus.edu', '/uploads/seed/avatar/03.jpg', '男', NULL, 1, '东校区', 110, 1),
(10, 'buyer02', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '黄雨桐', '2023020525', '13900000010', 'huangyt@campus.edu', '/uploads/seed/avatar/05.jpg', '女', NULL, 2, '西校区', 100, 1),
(11, 'buyer03', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '马骁', '2023030533', '13900000011', 'maxiao@campus.edu', '/uploads/seed/avatar/07.jpg', '男', NULL, 3, '北校区', 88, 1),
(12, 'buyer04', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '邓佳怡', '2023040541', '13900000012', 'dengjy@campus.edu', '/uploads/seed/avatar/06.jpg', '女', NULL, 1, '东校区', 132, 1),
(13, 'buyer05', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '吴桐', '2023050549', '13900000013', 'wutong@campus.edu', '/uploads/seed/avatar/04.jpg', '男', NULL, 2, '西校区', 96, 0),
(14, 'operator02', '$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u', '钱立运营', 'OP2026002', '13900000014', 'qianli@campus.edu', '/uploads/seed/avatar/02.jpg', '男', 2, 2, '西校区', 100, 1);

-- 用户角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(14, 2),
(3, 4),
(4, 3),
(5, 3),
(6, 3),
(7, 3),
(8, 3),
(9, 4),
(10, 4),
(11, 4),
(12, 4),
(13, 4);

-- 菜单（目录/菜单二级，按钮权限单独授权）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `path`, `component`, `perm`, `menu_type`, `icon`, `sort`) VALUES
(1, 0, '运营中心', NULL, NULL, NULL, 'catalog', 'dash', 1),
(2, 1, '运营首页', '/dashboard/index', 'Dashboard', 'dashboard:view', 'menu', 'dash', 1),
(3, 1, '商品审核台', '/admin/audit', 'GoodsAudit', 'goods:audit', 'menu', 'check', 2),
(4, 1, '商品管理', '/admin/goods', 'Goods', 'goods:list', 'menu', 'box', 3),
(5, 1, '订单管理', '/admin/orders', 'Orders', 'order:list', 'menu', 'order', 4),
(6, 1, '评价管理', '/admin/reviews', 'Reviews', 'review:list', 'menu', 'star', 5),
(7, 1, '举报与纠纷', '/admin/reports', 'Reports', 'report:list', 'menu', 'alert', 6),
(8, 1, '信用分流水', '/admin/credit', 'CreditLogs', 'credit:list', 'menu', 'medal', 7),
(9, 1, '定价评分记录', '/admin/price', 'PriceCalcs', 'price:list', 'menu', 'chart', 8),
(10, 1, '运营统计', '/admin/analysis', 'Analysis', 'analysis:view', 'menu', 'chart', 9),
(11, 0, '基础配置', NULL, NULL, NULL, 'catalog', 'gear', 2),
(12, 11, '分类管理', '/admin/categories', 'Categories', 'category:list', 'menu', 'tag', 1),
(13, 11, '校区面交点', '/admin/campus', 'Campus', 'campus:list', 'menu', 'pin', 2),
(14, 11, '定价规则配置', '/admin/rules', 'Rules', 'rule:list', 'menu', 'rule', 3),
(15, 11, '公告管理', '/admin/notices', 'Notices', 'notice:list', 'menu', 'bell', 4),
(16, 0, '系统管理', NULL, NULL, NULL, 'catalog', 'gear', 3),
(17, 16, '用户管理', '/admin/users', 'Users', 'user:list', 'menu', 'user', 1),
(18, 16, '角色管理', '/admin/roles', 'Roles', 'role:list', 'menu', 'shield', 2),
(19, 16, '菜单管理', '/admin/menus', 'Menus', 'menu:list', 'menu', 'list', 3),
(20, 16, '字典管理', '/admin/dicts', 'Dicts', 'dict:list', 'menu', 'book', 4),
(21, 16, '操作日志', '/admin/logs', 'Logs', 'log:list', 'menu', 'doc', 5),
(22, 16, '定价参数', '/admin/params', 'Params', 'param:list', 'menu', 'gear', 6),
(23, 0, '消息与个人', NULL, NULL, NULL, 'catalog', 'bell', 4),
(24, 23, '消息中心', '/admin/messages', 'Messages', 'message:list', 'menu', 'bell', 1),
(25, 23, '个人信息', '/admin/profile', 'Profile', 'profile:view', 'menu', 'user', 2);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `path`, `component`, `perm`, `menu_type`, `icon`, `sort`) VALUES
(26, 3, '商品审核通过', NULL, NULL, 'goods:audit:pass', 'button', NULL, 1),
(27, 3, '商品驳回', NULL, NULL, 'goods:audit:reject', 'button', NULL, 2),
(28, 7, '受理举报', NULL, NULL, 'report:handle', 'button', NULL, 1),
(29, 8, '调整信用分', NULL, NULL, 'credit:adjust', 'button', NULL, 1),
(30, 9, '人工修正评分', NULL, NULL, 'price:correct', 'button', NULL, 1),
(31, 4, '下架商品', NULL, NULL, 'goods:offshelf', 'button', NULL, 1),
(32, 15, '发布公告', NULL, NULL, 'notice:add', 'button', NULL, 1);

-- 角色菜单授权
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(1, 9),
(1, 10),
(1, 11),
(1, 12),
(1, 13),
(1, 14),
(1, 15),
(1, 16),
(1, 17),
(1, 18),
(1, 19),
(1, 20),
(1, 21),
(1, 22),
(1, 23),
(1, 24),
(1, 25),
(1, 26),
(1, 27),
(1, 28),
(1, 29),
(1, 30),
(1, 31),
(1, 32),
(2, 1),
(2, 2),
(2, 3),
(2, 4),
(2, 5),
(2, 6),
(2, 7),
(2, 8),
(2, 9),
(2, 10),
(2, 11),
(2, 12),
(2, 13),
(2, 14),
(2, 15),
(2, 23),
(2, 24),
(2, 25),
(2, 26),
(2, 27),
(2, 28),
(2, 30),
(2, 31),
(2, 32),
(3, 2),
(3, 3),
(3, 4),
(3, 5),
(3, 9),
(3, 12),
(3, 23),
(3, 24),
(3, 25),
(4, 2),
(4, 4),
(4, 5),
(4, 7),
(4, 9),
(4, 23),
(4, 24),
(4, 25),
(4, 28);

-- 字典类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`) VALUES
('商品状态', 'goods_status', 1, '待审核/在售/已下架/已售出/审核驳回/冻结'),
('订单状态', 'order_status', 1, '交易订单状态机'),
('商品成色', 'condition_level', 1, '成色等级'),
('交易方式', 'trade_type', 1, '面交/自提/可送'),
('支付方式', 'pay_type', 1, '线下支付方式记录'),
('举报类型', 'report_reason', 1, '举报原因分类'),
('举报状态', 'report_status', 1, '举报处理状态'),
('评价状态', 'review_status', 1, '评价审核状态'),
('信用变动原因', 'credit_reason', 1, '信用分变动来源'),
('定价等级', 'price_grade', 1, 'A/B/C 三级'),
('申诉状态', 'appeal_status', 1, '被举报人申诉'),
('校区', 'campus', 1, '三个校区'),
('审核时限', 'audit_limit', 1, '参数配置展示');
INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('goods_status', '待审核', '待审核', 1),
('goods_status', '在售', '在售', 2),
('goods_status', '已下架', '已下架', 3),
('goods_status', '已售出', '已售出', 4),
('goods_status', '审核驳回', '审核驳回', 5),
('goods_status', '冻结', '冻结', 6),
('order_status', '待卖家确认', '待卖家确认', 1),
('order_status', '待面交', '待面交', 2),
('order_status', '已完成', '已完成', 3),
('order_status', '已取消', '已取消', 4),
('order_status', '纠纷中', '纠纷中', 5),
('condition_level', '全新', '全新', 1),
('condition_level', '九成新', '九成新', 2),
('condition_level', '七成新', '七成新', 3),
('condition_level', '五成新', '五成新', 4),
('condition_level', '有瑕疵', '有瑕疵', 5),
('trade_type', '面交', '面交', 1),
('trade_type', '自提', '自提', 2),
('trade_type', '可送', '可送', 3),
('pay_type', '现金', '现金', 1),
('pay_type', '转账', '转账', 2),
('pay_type', '其他', '其他', 3),
('report_reason', '违禁品', '违禁品', 1),
('report_reason', '虚假描述', '虚假描述', 2),
('report_reason', '诈骗', '诈骗', 3),
('report_reason', '骚扰', '骚扰', 4),
('report_reason', '校外人员', '校外人员', 5),
('report_status', '待受理', '待受理', 1),
('report_status', '处理中', '处理中', 2),
('report_status', '已处理', '已处理', 3),
('report_status', '已驳回', '已驳回', 4),
('report_status', '申诉中', '申诉中', 5),
('review_status', '待审核', '待审核', 1),
('review_status', '已展示', '已展示', 2),
('review_status', '已隐藏', '已隐藏', 3),
('credit_reason', '交易好评', '交易好评', 1),
('credit_reason', '差评', '差评', 2),
('credit_reason', '放鸽子', '放鸽子', 3),
('credit_reason', '违规下架', '违规下架', 4),
('credit_reason', '中评', '中评', 5),
('credit_reason', '人工调整', '人工调整', 6),
('price_grade', 'A 定价合理', 'A', 1),
('price_grade', 'B 略偏高', 'B', 2),
('price_grade', 'C 偏离市场', 'C', 3),
('appeal_status', '无', '无', 1),
('appeal_status', '申诉中', '申诉中', 2),
('appeal_status', '申诉通过', '申诉通过', 3),
('appeal_status', '申诉驳回', '申诉驳回', 4),
('campus', '东校区', '1', 1),
('campus', '西校区', '2', 2),
('campus', '北校区', '3', 3),
('audit_limit', '审核时限（小时）', '48', 1),
('audit_limit', '订单锁定时长（小时）', '24', 2);

-- 闲置商品：30 条，覆盖待审核/在售/已下架/已售出/审核驳回/冻结六种状态
INSERT INTO `goods` (`goods_no`, `seller_id`, `category_id`, `title`, `description`, `condition_level`, `price`, `original_price`, `campus_id`, `campus_name`, `trade_place_id`, `trade_type`, `status`, `audit_remark`, `view_count`, `favorite_count`, `cover_url`, `image_urls`, `create_time`) VALUES
('XY2604010001', 4, 2, '九成新 iPad Air 4 64G 蓝色', '2025 年入学买的，用了一年少，屏幕无划痕，电池健康 92%，配原装充电头和数据线，可当面验机。', '九成新', 2350, 3999, 1, '东校区', 1, '面交', '在售', NULL, 316, 21, '/uploads/seed/goods/10.jpg', '/uploads/seed/goods/10.jpg,/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg,/uploads/seed/goods/07.jpg', '2026-08-29 05:05:00'),
('XY2604020002', 5, 2, '红米 K70 12+256 白色 保修内', '换了新手机出掉，屏幕无磕碰，边框有一处轻微氧化，功能全部正常，保修到明年三月。', '九成新', 1299, 2199, 2, '西校区', 5, '面交', '在售', NULL, 430, 33, '/uploads/seed/goods/17.jpg', '/uploads/seed/goods/17.jpg,/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg,/uploads/seed/goods/07.jpg', '2026-10-19 00:57:00'),
('XY2604030003', 6, 3, '罗技 G102 二代 有线鼠标', '用了一个学期，左键有轻微使用痕迹，滚轮和按键都正常，适合宿舍打游戏写作业用。', '七成新', 55, 129, 3, '北校区', 7, '自提', '在售', NULL, 469, 28, '/uploads/seed/goods/06.jpg', '/uploads/seed/goods/06.jpg,/uploads/seed/goods/01.jpg,/uploads/seed/goods/07.jpg,/uploads/seed/goods/08.jpg', '2026-11-01 11:26:00'),
('XY2604040004', 7, 3, '机械革命 32G 内存条 DDR4 3200', '买错型号了，全新未拆封，3200 频率单条 32G，笔记本通用，可现场验货。', '全新', 260, 399, 1, '东校区', 1, '可送', '已售出', NULL, 354, 41, '/uploads/seed/goods/08.jpg', '/uploads/seed/goods/08.jpg,/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg,/uploads/seed/goods/07.jpg', '2026-06-29 14:29:00'),
('XY2604050005', 8, 4, '索尼 WH-1000XM4 头戴降噪耳机', '降噪效果很好，耳罩无掉皮，附带原装收纳包和音频线，考研自习室神器。', '九成新', 899, 1899, 2, '西校区', 4, '面交', '已售出', NULL, 744, 29, '/uploads/seed/goods/01.jpg', '/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg,/uploads/seed/goods/07.jpg,/uploads/seed/goods/08.jpg', '2026-10-04 07:06:00'),
('XY2604060006', 4, 4, '小米蓝牙音箱 便携款', '宿舍用了一段时间，音质不错，充电正常，外壳有些许使用痕迹，不影响使用。', '七成新', 45, 99, 1, '东校区', 3, '面交', '在售', '描述与实物不符，疑似批量转卖，请补充细节图', 158, 42, '/uploads/seed/goods/08.jpg', '/uploads/seed/goods/08.jpg,/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg,/uploads/seed/goods/07.jpg', '2026-06-12 04:29:00'),
('XY2604070007', 5, 7, '高数教材 同济第七版 上下册', '有少量笔记和划线，重点章节标注完整，适合期末复习用，两本一起出。', '七成新', 25, 89, 2, '西校区', 4, '自提', '审核驳回', '图片含联系方式水印，请重新上传实拍图', 611, 40, '/uploads/seed/goods/13.jpg', '/uploads/seed/goods/13.jpg,/uploads/seed/goods/21.jpg', '2026-10-30 16:17:00'),
('XY2604080008', 6, 7, '数据结构与算法分析 C语言版', '只翻过前面几章，几乎全新，无笔记，计算机专业考研常用教材。', '九成新', 18, 59, 3, '北校区', 8, '可送', '在售', NULL, 165, 34, '/uploads/seed/goods/21.jpg', '/uploads/seed/goods/21.jpg,/uploads/seed/goods/13.jpg', '2026-09-23 01:30:00'),
('XY2604090009', 7, 8, '考研英语黄皮书 真题全套', '2025 版真题，做题痕迹较多，答案册干净，适合二刷使用，共四本。', '七成新', 60, 168, 1, '东校区', 3, '面交', '已售出', NULL, 226, 30, '/uploads/seed/goods/13.jpg', '/uploads/seed/goods/13.jpg,/uploads/seed/goods/21.jpg', '2026-07-05 14:47:00'),
('XY2604100010', 8, 8, '教资考试 中学综合素质教材', '备考用过一遍，重点已用荧光笔标注，配套网课已过期不影响看书。', '九成新', 22, 68, 2, '西校区', 5, '面交', '冻结', NULL, 814, 33, '/uploads/seed/goods/21.jpg', '/uploads/seed/goods/21.jpg,/uploads/seed/goods/13.jpg', '2026-10-22 14:19:00'),
('XY2604110011', 4, 9, '三毛 撒哈拉的故事 正版', '正版书籍，无折页无笔记，看完一遍出掉，书脊完好。', '九成新', 12, 39, 1, '东校区', 2, '自提', '在售', NULL, 307, 25, '/uploads/seed/goods/13.jpg', '/uploads/seed/goods/13.jpg,/uploads/seed/goods/21.jpg', '2026-11-01 09:10:00'),
('XY2604120012', 5, 11, '宿舍可折叠收纳箱 大号两只', '搬宿舍买的，用过一学期，折叠无破损，可装衣物和被褥，两只一起出。', '九成新', 30, 78, 2, '西校区', 5, '可送', '在售', NULL, 742, 26, '/uploads/seed/goods/19.jpg', '/uploads/seed/goods/19.jpg,/uploads/seed/goods/03.jpg,/uploads/seed/goods/04.jpg,/uploads/seed/goods/05.jpg', '2026-05-20 09:57:00'),
('XY2604130013', 6, 12, '宿舍小风扇 静音落地款', '三档风速，静音效果可以，夏天宿舍必备，扇叶已清洁。', '七成新', 38, 119, 3, '北校区', 7, '面交', '在售', '描述与实物不符，疑似批量转卖，请补充细节图', 137, 11, '/uploads/seed/goods/09.jpg', '/uploads/seed/goods/09.jpg,/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg,/uploads/seed/goods/07.jpg', '2026-08-28 10:04:00'),
('XY2604140014', 7, 12, '美的电热水壶 1.5L', '宿舍烧水用，无结垢，自动断电正常，含原装底座。', '九成新', 45, 139, 1, '东校区', 2, '面交', '已售出', NULL, 729, 20, '/uploads/seed/goods/04.jpg', '/uploads/seed/goods/04.jpg,/uploads/seed/goods/03.jpg,/uploads/seed/goods/05.jpg,/uploads/seed/goods/15.jpg', '2026-10-28 01:41:00'),
('XY2604150015', 8, 13, '捷安特 ATX 山地自行车 26寸', '骑了两年，刚换过刹车片和链条，变速顺畅，带 U 型锁，仅限校内面交自提。', '七成新', 480, 1580, 2, '西校区', 4, '自提', '已售出', NULL, 364, 22, '/uploads/seed/goods/16.jpg', '/uploads/seed/goods/16.jpg,/uploads/seed/goods/14.jpg,/uploads/seed/goods/20.jpg', '2026-07-03 06:50:00'),
('XY2604160016', 4, 13, '折叠自行车 20寸 通勤款', '折叠方便放宿舍阳台，轮胎新换，骑行无异常，附赠车灯。', '九成新', 210, 599, 1, '东校区', 1, '可送', '已售出', NULL, 47, 24, '/uploads/seed/goods/18.jpg', '/uploads/seed/goods/18.jpg,/uploads/seed/goods/16.jpg,/uploads/seed/goods/20.jpg', '2026-08-01 13:12:00'),
('XY2604170017', 5, 15, '哑铃一对 可调节 15kg', '宿舍健身用的，铸铁片无锈，锁扣正常，太重不方便寄，只能自提。', '九成新', 85, 260, 2, '西校区', 4, '面交', '待审核', NULL, 788, 34, '/uploads/seed/goods/20.jpg', '/uploads/seed/goods/20.jpg,/uploads/seed/goods/14.jpg,/uploads/seed/goods/16.jpg', '2026-06-14 06:52:00'),
('XY2604180018', 6, 15, '瑜伽垫 加厚防滑 183x61', '买多了一条，全新未拆，加厚 10mm，防滑面，附绑带。', '全新', 28, 89, 3, '北校区', 8, '面交', '在售', NULL, 465, 41, '/uploads/seed/goods/14.jpg', '/uploads/seed/goods/14.jpg,/uploads/seed/goods/16.jpg,/uploads/seed/goods/20.jpg', '2026-10-02 03:27:00'),
('XY2604190019', 7, 16, '羽毛球拍 尤尼克斯 双拍套装', '打了一学期，拍框无变形，已重新拉线 24 磅，附拍包和三个球。', '七成新', 120, 380, 1, '东校区', 1, '自提', '在售', NULL, 533, 33, '/uploads/seed/goods/20.jpg', '/uploads/seed/goods/20.jpg,/uploads/seed/goods/14.jpg,/uploads/seed/goods/16.jpg', '2026-11-01 17:54:00'),
('XY2604200020', 8, 16, '斯伯丁 7号篮球 室外款', '室外场地打的，表皮有磨损但不影响手感，气足。', '七成新', 65, 189, 2, '西校区', 5, '可送', '已售出', '描述与实物不符，疑似批量转卖，请补充细节图', 172, 33, '/uploads/seed/goods/16.jpg', '/uploads/seed/goods/16.jpg,/uploads/seed/goods/14.jpg,/uploads/seed/goods/20.jpg', '2026-10-10 23:20:00'),
('XY2604210021', 4, 17, '户外露营帐篷 双人自动', '去过两次露营，无破损无漏水，自动开合，收纳袋齐全。', '九成新', 150, 429, 1, '东校区', 3, '面交', '在售', NULL, 505, 44, '/uploads/seed/goods/18.jpg', '/uploads/seed/goods/18.jpg,/uploads/seed/goods/16.jpg,/uploads/seed/goods/20.jpg', '2026-06-22 14:20:00'),
('XY2604220022', 5, 17, '登山杖 铝合金 一对', '可伸缩，锁扣牢固，爬山用过两次，握把干净。', '九成新', 40, 128, 2, '西校区', 5, '面交', '在售', NULL, 82, 39, '/uploads/seed/goods/16.jpg', '/uploads/seed/goods/16.jpg,/uploads/seed/goods/14.jpg,/uploads/seed/goods/20.jpg', '2026-08-18 11:39:00'),
('XY2604230023', 6, 19, '优衣库 男士羽绒服 M码 黑色', '冬天买的，穿过一季，无破损无污渍，已干洗，M 码偏宽松。', '九成新', 130, 499, 3, '北校区', 7, '自提', '已售出', NULL, 137, 30, '/uploads/seed/goods/19.jpg', '/uploads/seed/goods/19.jpg,/uploads/seed/goods/03.jpg,/uploads/seed/goods/04.jpg,/uploads/seed/goods/05.jpg', '2026-06-08 14:23:00'),
('XY2604240024', 7, 20, '匡威帆布鞋 42码 白色', '穿了一学期，鞋底有磨损，已刷洗干净，无开胶。', '七成新', 95, 369, 1, '东校区', 3, '可送', '已售出', NULL, 200, 39, '/uploads/seed/goods/02.jpg', '/uploads/seed/goods/02.jpg,/uploads/seed/goods/19.jpg', '2026-10-31 07:40:00'),
('XY2604250025', 8, 8, '考研数学 复习全书 全套', '全套三本，做题主要在草稿纸上，书内笔记较少，适合基础阶段。', '九成新', 55, 149, 2, '西校区', 4, '面交', '待审核', NULL, 181, 2, '/uploads/seed/goods/13.jpg', '/uploads/seed/goods/13.jpg,/uploads/seed/goods/21.jpg', '2026-06-04 03:09:00'),
('XY2604260026', 4, 3, '机械键盘 87键 青轴', '宿舍用了一年，键帽无打油，灯效正常，青轴声音偏大已换静音轴故出。', '九成新', 105, 299, 1, '东校区', 2, '面交', '在售', NULL, 748, 35, '/uploads/seed/goods/12.jpg', '/uploads/seed/goods/12.jpg,/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg,/uploads/seed/goods/07.jpg', '2026-07-06 00:29:00'),
('XY2604270027', 5, 12, '台灯 护眼可调光 LED', '三档色温，无极调光，宿舍书桌用，附原装电源适配器。', '九成新', 42, 129, 2, '西校区', 4, '自提', '在售', '描述与实物不符，疑似批量转卖，请补充细节图', 727, 16, '/uploads/seed/goods/15.jpg', '/uploads/seed/goods/15.jpg,/uploads/seed/goods/03.jpg,/uploads/seed/goods/04.jpg,/uploads/seed/goods/05.jpg', '2026-11-01 15:38:00'),
('XY2604280028', 6, 23, '画架 实木折叠 含画板', '美院课程用过，木架稳固，画板有铅笔痕迹，可擦拭。', '七成新', 78, 220, 3, '北校区', 8, '可送', '待审核', NULL, 490, 29, '/uploads/seed/goods/21.jpg', '/uploads/seed/goods/21.jpg,/uploads/seed/goods/13.jpg', '2026-06-28 03:51:00'),
('XY2604290029', 7, 22, '桌面收纳架 三层 白色', '宿舍桌面收纳，组装简单，无变形，螺丝齐全。', '九成新', 25, 79, 1, '东校区', 2, '面交', '待审核', NULL, 157, 4, '/uploads/seed/goods/19.jpg', '/uploads/seed/goods/19.jpg,/uploads/seed/goods/03.jpg,/uploads/seed/goods/04.jpg,/uploads/seed/goods/05.jpg', '2026-05-19 18:59:00'),
('XY2604300030', 8, 23, '吉他 41寸 民谣 入门款', '自学半年，琴颈无变形，刚换琴弦，附琴包、变调夹和拨片。', '七成新', 260, 680, 2, '西校区', 5, '面交', '已售出', NULL, 399, 12, '/uploads/seed/goods/11.jpg', '/uploads/seed/goods/11.jpg', '2026-10-22 07:58:00'),
('XY2604310031', 4, 5, '充电宝 20000mAh 双向快充', '容量真实，双向快充正常，外壳有轻微划痕，附原装线。', '九成新', 58, 169, 1, '东校区', 1, '自提', '在售', NULL, 17, 33, '/uploads/seed/goods/17.jpg', '/uploads/seed/goods/17.jpg,/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg,/uploads/seed/goods/07.jpg', '2026-07-04 17:51:00'),
('XY2604320032', 5, 3, '显示器 24寸 1080P IPS', '外接笔记本用的，无坏点无亮点，附 HDMI 线和支架。', '九成新', 320, 799, 2, '西校区', 5, '可送', '已售出', NULL, 677, 30, '/uploads/seed/goods/07.jpg', '/uploads/seed/goods/07.jpg,/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg,/uploads/seed/goods/08.jpg', '2026-06-17 08:20:00'),
('XY2604330033', 6, 4, 'AirPods 二代 有线充电盒', '左右耳电池都正常，续航约三小时，充电盒有轻微划痕，已消毒。', '九成新', 320, 999, 3, '北校区', 7, '面交', '已售出', NULL, 677, 34, '/uploads/seed/goods/01.jpg', '/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg,/uploads/seed/goods/07.jpg,/uploads/seed/goods/08.jpg', '2026-05-17 11:53:00'),
('XY2604340034', 7, 7, '线性代数 同济第六版', '课上用过的教材，习题部分有铅笔演算，可擦除，配套答案册齐全。', '七成新', 15, 42, 1, '东校区', 1, '面交', '在售', '描述与实物不符，疑似批量转卖，请补充细节图', 565, 7, '/uploads/seed/goods/13.jpg', '/uploads/seed/goods/13.jpg,/uploads/seed/goods/21.jpg', '2026-07-26 12:56:00'),
('XY2604350035', 8, 11, '床上小桌板 可折叠', '宿舍床上用，桌面无划痕，折叠支架牢固。', '九成新', 22, 69, 2, '西校区', 4, '自提', '在售', NULL, 815, 14, '/uploads/seed/goods/19.jpg', '/uploads/seed/goods/19.jpg,/uploads/seed/goods/03.jpg,/uploads/seed/goods/04.jpg,/uploads/seed/goods/05.jpg', '2026-11-01 13:22:00'),
('XY2604360036', 4, 15, '跳绳 钢丝竞速款', '买来减脂用的，跳了不到十次，钢丝无打结，附收纳袋。', '九成新', 12, 39, 1, '东校区', 3, '可送', '在售', NULL, 851, 38, '/uploads/seed/goods/20.jpg', '/uploads/seed/goods/20.jpg,/uploads/seed/goods/14.jpg,/uploads/seed/goods/16.jpg', '2026-06-29 00:00:00'),
('XY2604370037', 5, 19, '双肩包 电脑仓 15.6寸', '上课背了一年，拉链顺滑，肩带无断裂，内衬有轻微磨损。', '七成新', 68, 219, 2, '西校区', 4, '面交', '在售', NULL, 197, 13, '/uploads/seed/goods/02.jpg', '/uploads/seed/goods/02.jpg,/uploads/seed/goods/19.jpg', '2026-10-22 15:13:00'),
('XY2604380038', 6, 23, '针管笔套装 0.1-0.8 六支', '买重了，全新未拆封，六种粗细，绘图和手绘都能用。', '全新', 26, 78, 3, '北校区', 8, '面交', '已售出', NULL, 65, 31, '/uploads/seed/goods/21.jpg', '/uploads/seed/goods/21.jpg,/uploads/seed/goods/13.jpg', '2026-07-25 08:49:00'),
('XY2604390039', 7, 5, '投影仪 便携 1080P', '宿舍看电影用的，亮度够用，灯泡使用不到 100 小时，附幕布和三脚架。', '九成新', 420, 1299, 1, '东校区', 3, '自提', '在售', NULL, 50, 30, '/uploads/seed/goods/07.jpg', '/uploads/seed/goods/07.jpg,/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg,/uploads/seed/goods/08.jpg', '2026-08-21 07:10:00'),
('XY2604400040', 8, 17, '运动水壶 750ml 保温', '保温效果正常，杯盖无渗漏，内胆已清洗。', '九成新', 32, 98, 2, '西校区', 5, '可送', '在售', NULL, 430, 1, '/uploads/seed/goods/03.jpg', '/uploads/seed/goods/03.jpg,/uploads/seed/goods/04.jpg,/uploads/seed/goods/05.jpg,/uploads/seed/goods/15.jpg', '2026-08-21 03:58:00');

-- 交易订单：32 条，覆盖待卖家确认/待面交/已完成/已取消/纠纷中五种状态
INSERT INTO `trade_order` (`order_no`, `goods_id`, `goods_title`, `goods_cover`, `buyer_id`, `seller_id`, `deal_price`, `pay_type`, `appoint_time`, `appoint_place`, `status`, `cancel_reason`, `buyer_confirm_time`, `seller_confirm_time`, `lock_expire_time`, `create_time`) VALUES
('DD260402000001', 4, '机械革命 32G 内存条 DDR4 3200', '/uploads/seed/goods/08.jpg', 3, 7, 240, '转账', '2026-11-01 13:00:00', '东校区·面交点2', '已完成', NULL, '2026-11-01 01:19:00', '2026-11-01 08:24:00', '2026-11-01 16:05:00', '2026-10-31 03:22:00'),
('DD260403000002', 5, '索尼 WH-1000XM4 头戴降噪耳机', '/uploads/seed/goods/01.jpg', 9, 8, 889, '现金', '2026-11-01 17:00:00', '西校区·面交点3', '已完成', NULL, '2026-11-01 17:19:00', '2026-10-31 14:28:00', '2026-10-31 04:31:00', '2026-10-30 10:51:00'),
('DD260404000003', 9, '考研英语黄皮书 真题全套', '/uploads/seed/goods/13.jpg', 10, 7, 50, '其他', '2026-10-31 19:30:00', '东校区·面交点1', '已完成', NULL, '2026-10-31 07:15:00', '2026-10-30 00:34:00', '2026-10-30 03:05:00', '2026-10-29 05:34:00'),
('DD260405000004', 14, '美的电热水壶 1.5L', '/uploads/seed/goods/04.jpg', 11, 7, 35, '现金', '2026-10-29 19:00:00', '东校区·面交点2', '已完成', NULL, '2026-10-30 04:16:00', '2026-10-29 09:40:00', '2026-10-29 09:40:00', '2026-10-28 06:06:00'),
('DD260402000005', 15, '捷安特 ATX 山地自行车 26寸', '/uploads/seed/goods/16.jpg', 12, 8, 480, '转账', '2026-10-31 09:00:00', '西校区·面交点3', '已完成', NULL, '2026-11-01 18:35:00', '2026-11-01 09:50:00', '2026-11-01 08:10:00', '2026-10-31 09:24:00'),
('DD260403000006', 16, '折叠自行车 20寸 通勤款', '/uploads/seed/goods/18.jpg', 3, 4, 205, '现金', '2026-11-01 10:30:00', '东校区·面交点1', '已完成', NULL, '2026-11-01 01:15:00', '2026-10-31 15:17:00', '2026-10-31 10:10:00', '2026-10-29 23:24:00'),
('DD260404000007', 20, '斯伯丁 7号篮球 室外款', '/uploads/seed/goods/16.jpg', 9, 8, 65, '其他', '2026-10-30 17:30:00', '西校区·面交点2', '已完成', NULL, '2026-10-31 10:08:00', '2026-10-30 09:35:00', '2026-10-30 01:14:00', '2026-10-29 06:34:00'),
('DD260405000008', 23, '优衣库 男士羽绒服 M码 黑色', '/uploads/seed/goods/19.jpg', 10, 6, 130, '现金', '2026-10-30 15:00:00', '北校区·面交点3', '已完成', NULL, '2026-10-30 08:31:00', '2026-10-29 15:41:00', '2026-10-29 05:51:00', '2026-10-28 13:23:00'),
('DD260546000009', 24, '匡威帆布鞋 42码 白色', '/uploads/seed/goods/02.jpg', 11, 7, 95, '转账', '2026-06-11 13:30:00', '东校区·面交点1', '已完成', NULL, '2026-06-11 18:52:00', '2026-06-10 18:46:00', '2026-06-10 00:04:00', '2026-06-09 09:25:00'),
('DD260501000010', 30, '吉他 41寸 民谣 入门款', '/uploads/seed/goods/11.jpg', 12, 8, 260, '现金', '2026-07-26 20:30:00', '西校区·面交点2', '已完成', NULL, '2026-07-26 16:26:00', '2026-07-25 09:25:00', '2026-07-25 02:32:00', '2026-07-24 09:05:00'),
('DD260546000011', 32, '显示器 24寸 1080P IPS', '/uploads/seed/goods/07.jpg', 3, 5, 320, '其他', '2026-06-12 20:30:00', '西校区·面交点3', '已完成', NULL, '2026-06-11 12:41:00', '2026-06-10 08:57:00', '2026-06-10 03:03:00', '2026-06-09 16:17:00'),
('DD260480000012', 33, 'AirPods 二代 有线充电盒', '/uploads/seed/goods/01.jpg', 9, 6, 310, '现金', '2026-08-15 09:00:00', '北校区·面交点1', '已完成', NULL, '2026-08-16 01:50:00', '2026-08-15 17:40:00', '2026-08-15 04:43:00', '2026-08-14 06:44:00'),
('DD260531000013', 38, '针管笔套装 0.1-0.8 六支', '/uploads/seed/goods/21.jpg', 10, 6, 16, '转账', '2026-06-26 16:30:00', '北校区·面交点2', '已完成', NULL, '2026-06-26 05:31:00', '2026-06-25 19:39:00', '2026-06-25 06:15:00', '2026-06-24 14:51:00'),
('DD260416000014', 1, '九成新 iPad Air 4 64G 蓝色', '/uploads/seed/goods/10.jpg', 3, 4, 2350, '现金', '2026-10-18 12:30:00', '东校区·面交点3', '待面交', NULL, NULL, '2026-10-18 04:29:00', '2026-10-18 07:23:00', '2026-10-17 12:35:00'),
('DD260414000015', 2, '红米 K70 12+256 白色 保修内', '/uploads/seed/goods/17.jpg', 9, 5, 1299, '其他', '2026-10-22 18:30:00', '西校区·面交点1', '待面交', NULL, NULL, '2026-10-20 12:21:00', '2026-10-20 09:51:00', '2026-10-19 15:14:00'),
('DD260405000016', 3, '罗技 G102 二代 有线鼠标', '/uploads/seed/goods/06.jpg', 10, 6, 55, '现金', '2026-10-29 15:00:00', '北校区·面交点2', '待面交', NULL, NULL, '2026-10-29 10:52:00', '2026-10-29 18:23:00', '2026-10-28 10:28:00'),
('DD260416000017', 6, '小米蓝牙音箱 便携款', '/uploads/seed/goods/08.jpg', 11, 4, 45, '转账', '2026-10-18 16:00:00', '东校区·面交点3', '待面交', NULL, NULL, '2026-10-18 17:25:00', '2026-10-18 03:49:00', '2026-10-17 02:26:00'),
('DD260414000018', 8, '数据结构与算法分析 C语言版', '/uploads/seed/goods/21.jpg', 12, 6, 18, '现金', '2026-10-21 11:30:00', '北校区·面交点1', '待面交', NULL, NULL, '2026-10-20 03:31:00', '2026-10-20 09:24:00', '2026-10-19 13:47:00'),
('DD260407000019', 11, '三毛 撒哈拉的故事 正版', '/uploads/seed/goods/13.jpg', 3, 4, 12, '其他', '2026-10-28 09:00:00', '东校区·面交点2', '待面交', NULL, NULL, '2026-10-27 05:03:00', '2026-10-27 16:21:00', '2026-10-25 23:18:00'),
('DD260401000020', 12, '宿舍可折叠收纳箱 大号两只', '/uploads/seed/goods/19.jpg', 11, 5, 30, '现金', '2026-11-01 17:00:00', '西校区·面交点3', '待卖家确认', NULL, NULL, NULL, '2026-11-02 03:30:00', '2026-11-01 15:06:00'),
('DD260401000021', 13, '宿舍小风扇 静音落地款', '/uploads/seed/goods/09.jpg', 12, 6, 38, '转账', '2026-11-01 09:00:00', '北校区·面交点1', '待卖家确认', NULL, NULL, NULL, '2026-11-02 03:30:00', '2026-11-01 16:17:00'),
('DD260401000022', 18, '瑜伽垫 加厚防滑 183x61', '/uploads/seed/goods/14.jpg', 3, 6, 28, '现金', '2026-11-01 13:30:00', '北校区·面交点2', '待卖家确认', NULL, NULL, NULL, '2026-11-02 03:30:00', '2026-11-01 17:28:00'),
('DD260401000023', 19, '羽毛球拍 尤尼克斯 双拍套装', '/uploads/seed/goods/20.jpg', 9, 7, 120, '其他', '2026-11-01 16:30:00', '东校区·面交点3', '待卖家确认', NULL, NULL, NULL, '2026-11-02 03:30:00', '2026-11-01 18:39:00'),
('DD260401000024', 21, '户外露营帐篷 双人自动', '/uploads/seed/goods/18.jpg', 10, 4, 150, '现金', '2026-11-01 14:30:00', '东校区·面交点1', '待卖家确认', NULL, NULL, NULL, '2026-11-02 03:30:00', '2026-11-01 19:50:00'),
('DD260428000025', 22, '登山杖 铝合金 一对', '/uploads/seed/goods/16.jpg', 9, 5, 40, '转账', '2026-10-08 13:00:00', '西校区·面交点2', '已取消', '临时有事，改天再约', NULL, NULL, '2026-10-06 13:32:00', '2026-10-05 15:30:00'),
('DD260423000026', 26, '机械键盘 87键 青轴', '/uploads/seed/goods/12.jpg', 10, 4, 105, '现金', '2026-10-12 11:30:00', '东校区·面交点3', '已取消', '和卖家协商后取消', NULL, NULL, '2026-10-11 11:27:00', '2026-10-10 07:52:00'),
('DD260428000027', 27, '台灯 护眼可调光 LED', '/uploads/seed/goods/15.jpg', 11, 5, 42, '其他', '2026-10-08 11:00:00', '西校区·面交点1', '已取消', '面交时间冲突，重新约', NULL, NULL, '2026-10-06 17:09:00', '2026-10-05 08:08:00'),
('DD260417000028', 31, '充电宝 20000mAh 双向快充', '/uploads/seed/goods/17.jpg', 12, 4, 58, '现金', '2026-10-19 09:00:00', '东校区·面交点2', '已取消', '商品与描述不符，取消', NULL, NULL, '2026-10-17 16:20:00', '2026-10-16 19:49:00'),
('DD260425000029', 34, '线性代数 同济第六版', '/uploads/seed/goods/13.jpg', 3, 7, 15, '转账', '2026-10-09 11:00:00', '东校区·面交点3', '已取消', '临时决定不买了', NULL, NULL, '2026-10-09 03:07:00', '2026-10-08 10:01:00'),
('DD260422000030', 22, '登山杖 铝合金 一对', '/uploads/seed/goods/16.jpg', 9, 5, 40, '现金', '2026-10-14 17:00:00', '西校区·面交点1', '已取消', '卖家超时未确认，系统自动取消', NULL, NULL, '2026-10-12 09:50:00', '2026-10-11 01:03:00'),
('DD260437000031', 26, '机械键盘 87键 青轴', '/uploads/seed/goods/12.jpg', 10, 4, 105, '其他', '2026-09-28 10:00:00', '东校区·面交点2', '已取消', '面交点临时关闭，改约失败', NULL, NULL, '2026-09-27 15:22:00', '2026-09-26 10:52:00'),
('DD260405000032', 27, '台灯 护眼可调光 LED', '/uploads/seed/goods/15.jpg', 11, 5, 42, '现金', '2026-10-29 18:00:00', '西校区·面交点3', '已取消', '重复下单，取消多余订单', NULL, NULL, '2026-10-28 23:10:00', '2026-10-28 07:29:00'),
('DD260430000033', 27, '台灯 护眼可调光 LED', '/uploads/seed/goods/15.jpg', 11, 5, 42, '转账', '2026-10-04 20:30:00', '西校区·面交点1', '纠纷中', '面交未见到卖家，申请纠纷调解', NULL, '2026-10-04 09:52:00', '2026-10-04 12:03:00', '2026-10-03 19:59:00'),
('DD260409000034', 31, '充电宝 20000mAh 双向快充', '/uploads/seed/goods/17.jpg', 12, 4, 58, '现金', '2026-10-26 17:00:00', '东校区·面交点2', '纠纷中', '面交未见到卖家，申请纠纷调解', NULL, '2026-10-25 06:39:00', '2026-10-25 01:22:00', '2026-10-24 05:58:00');

-- 交易评价：完成订单的双向互评，部分仅有单方提交用于验证双盲
INSERT INTO `review` (`order_id`, `from_user_id`, `to_user_id`, `score`, `tags`, `content`, `anonymous`, `status`, `create_time`) VALUES
(1, 3, 7, 5, '描述一致,准时', '东西和描述一样，卖家很准时', True, '已展示', '2026-10-31 03:22:00'),
(1, 7, 3, 5, '准时,好沟通', '买家很爽快，当面确认后立刻付款', 1, '已展示', '2026-10-31 03:22:00'),
(2, 9, 8, 5, '准时,好沟通', '面交很顺利，物品成色不错', False, '已展示', '2026-10-30 10:51:00'),
(2, 8, 9, 4, '准时,好沟通', '沟通顺畅，交易愉快', 0, '已展示', '2026-10-30 10:51:00'),
(3, 10, 7, 4, '描述一致,好沟通,包装完好', '沟通很愉快，推荐', False, '已展示', '2026-10-29 05:34:00'),
(4, 11, 7, 5, '准时', '价格实在，下次还找他', False, '已展示', '2026-10-28 06:06:00'),
(4, 7, 11, 5, '准时,好沟通', '买家很爽快，当面确认后立刻付款', 0, '已展示', '2026-10-28 06:06:00'),
(5, 12, 8, 3, '好沟通', '有点小瑕疵但卖家提前说了', True, '已展示', '2026-10-31 09:24:00'),
(5, 8, 12, 5, '准时,好沟通', '沟通顺畅，交易愉快', 0, '已展示', '2026-10-31 09:24:00'),
(6, 3, 4, 5, '描述一致,准时', '东西和描述一样，卖家很准时', False, '待审核', '2026-10-29 23:24:00'),
(7, 9, 8, 4, '准时,好沟通', '面交很顺利，物品成色不错', False, '已展示', '2026-10-29 06:34:00'),
(7, 8, 9, 5, '准时,好沟通', '买家很爽快，当面确认后立刻付款', 0, '已展示', '2026-10-29 06:34:00'),
(8, 10, 6, 5, '描述一致,好沟通,包装完好', '沟通很愉快，推荐', False, '已展示', '2026-10-28 13:23:00'),
(8, 6, 10, 5, '准时,好沟通', '沟通顺畅，交易愉快', 0, '已展示', '2026-10-28 13:23:00'),
(9, 11, 7, 5, '准时', '价格实在，下次还找他', True, '已展示', '2026-06-09 09:25:00'),
(10, 12, 8, 5, '好沟通', '有点小瑕疵但卖家提前说了', False, '已展示', '2026-07-24 09:05:00'),
(10, 8, 12, 4, '准时,好沟通', '买家很爽快，当面确认后立刻付款', 0, '已展示', '2026-07-24 09:05:00'),
(11, 3, 5, 4, '描述一致,准时', '东西和描述一样，卖家很准时', False, '已展示', '2026-06-09 16:17:00'),
(11, 5, 3, 5, '准时,好沟通', '沟通顺畅，交易愉快', 1, '已展示', '2026-06-09 16:17:00'),
(12, 9, 6, 5, '准时,好沟通', '面交很顺利，物品成色不错', False, '待审核', '2026-08-14 06:44:00'),
(13, 10, 6, 3, '描述一致,好沟通,包装完好', '沟通很愉快，推荐', True, '已展示', '2026-06-24 14:51:00'),
(13, 6, 10, 5, '准时,好沟通', '买家很爽快，当面确认后立刻付款', 0, '已展示', '2026-06-24 14:51:00');

-- 举报与纠纷：12 条，覆盖待受理/处理中/已处理/已驳回与申诉
INSERT INTO `report` (`report_no`, `biz_type`, `biz_id`, `biz_title`, `reporter_id`, `reason_type`, `description`, `evidence_urls`, `status`, `handler_id`, `handler_name`, `handle_result`, `appeal_status`, `create_time`) VALUES
('JB2605010001', '商品', 1, '九成新 iPad Air 4 64G 蓝色', 3, '违禁品', '疑似出售违规电器，宿舍不允许使用大功率电热设备', '/uploads/seed/goods/10.jpg,/uploads/seed/goods/01.jpg', '待受理', NULL, NULL, NULL, '无', '2026-09-21 09:20:00'),
('JB2605020002', '商品', 2, '红米 K70 12+256 白色 保修内', 10, '虚假描述', '标题写九成新，实际边框有明显磕碰，图片是网图', '/uploads/seed/goods/17.jpg,/uploads/seed/goods/01.jpg', '处理中', 2, '林晓运营', '已受理，正在联系双方核实', '无', '2026-10-18 13:42:00'),
('JB2605030003', '商品', 3, '罗技 G102 二代 有线鼠标', 10, '诈骗', '要求先转账后发货，不肯当面交易，感觉是骗子', '/uploads/seed/goods/06.jpg,/uploads/seed/goods/01.jpg', '已处理', 2, '林晓运营', '已按平台规则处理：核实后下架商品并对卖家扣 10 分', '无', '2026-10-04 13:49:00'),
('JB2605040004', '订单', 4, '订单 DD260404000004', 12, '骚扰', '下单后多次在非面交时间联系，要求送到宿舍楼下', '/uploads/seed/goods/08.jpg,/uploads/seed/goods/01.jpg', '已处理', 2, '林晓运营', '已按平台规则处理：核实后下架商品并对卖家扣 10 分', '申诉中', '2026-10-12 10:48:00'),
('JB2605050005', '商品', 5, '索尼 WH-1000XM4 头戴降噪耳机', 12, '校外人员', '卖家自称校外人员，长期批量转卖二手手机', '/uploads/seed/goods/01.jpg,/uploads/seed/goods/06.jpg', '已处理', 2, '林晓运营', '已按平台规则处理：核实后下架商品并对卖家扣 10 分', '申诉通过', '2026-09-05 00:57:00'),
('JB2605060006', '订单', 6, '订单 DD260406000006', 10, '虚假描述', '面交时商品与描述严重不符，拒绝退款', '/uploads/seed/goods/08.jpg,/uploads/seed/goods/01.jpg', '待受理', NULL, NULL, NULL, '无', '2026-09-21 02:15:00'),
('JB2605010007', '商品', 7, '高数教材 同济第七版 上下册', 9, '违禁品', '出售考试作弊器材，影响考试公平', '/uploads/seed/goods/13.jpg,/uploads/seed/goods/21.jpg', '已处理', 2, '林晓运营', '已按平台规则处理：核实后下架商品并对卖家扣 10 分', '无', '2026-10-08 04:45:00'),
('JB2605020008', '商品', 8, '数据结构与算法分析 C语言版', 12, '虚假描述', '标价与详情页价格不一致，引导私下加价', '/uploads/seed/goods/21.jpg,/uploads/seed/goods/13.jpg', '已驳回', 2, '林晓运营', '证据不足，驳回举报', '无', '2026-09-19 11:11:00'),
('JB2605030009', '订单', 9, '订单 DD260409000009', 11, '诈骗', '付款后卖家失联，订单一直不确认', '/uploads/seed/goods/13.jpg,/uploads/seed/goods/21.jpg', '处理中', 2, '林晓运营', '已受理，正在联系双方核实', '无', '2026-10-09 00:06:00'),
('JB2605040010', '用户', 10, '订单 DD260410000010', 10, '骚扰', '对方在评价里公开我的手机号', '/uploads/seed/goods/21.jpg,/uploads/seed/goods/13.jpg', '已处理', 2, '林晓运营', '已按平台规则处理：核实后下架商品并对卖家扣 10 分', '申诉驳回', '2026-09-18 17:37:00'),
('JB2605050011', '商品', 11, '三毛 撒哈拉的故事 正版', 3, '校外人员', '商品图片带商铺水印，疑似商家冒充学生', '/uploads/seed/goods/13.jpg,/uploads/seed/goods/21.jpg', '待受理', NULL, NULL, NULL, '无', '2026-10-11 07:17:00'),
('JB2605060012', '订单', 12, '订单 DD260412000012', 12, '虚假描述', '到手发现缺少配件，卖家称已说明但页面没写', '/uploads/seed/goods/19.jpg,/uploads/seed/goods/03.jpg', '已处理', 2, '林晓运营', '已按平台规则处理：核实后下架商品并对卖家扣 10 分', '无', '2026-10-04 10:41:00');

-- 信用分流水：变动前后值连续，可回溯到 sys_user.credit_score
INSERT INTO `credit_log` (`user_id`, `change_value`, `before_score`, `after_score`, `reason_type`, `biz_no`, `remark`, `operator_id`, `create_time`) VALUES
(3, 2, 100, 102, '交易好评', 'DD260408585562', NULL, NULL, '2026-08-20 14:48:00'),
(3, 2, 102, 104, '交易好评', 'DD260465187803', NULL, NULL, '2026-07-09 16:02:00'),
(3, 2, 104, 106, '交易好评', 'DD260501187687', NULL, NULL, '2026-07-25 16:28:00'),
(3, -2, 106, 104, '人工调整', 'DD260440161311', '运营人工校准信用分', 2, '2026-08-18 15:50:00'),
(4, 2, 100, 102, '交易好评', 'DD260470106498', NULL, NULL, '2026-08-09 19:22:00'),
(4, 2, 102, 104, '交易好评', 'DD260504536008', NULL, NULL, '2026-10-12 05:29:00'),
(4, 24, 104, 128, '人工调整', 'DD260523423221', '运营人工校准信用分', 2, '2026-08-11 15:44:00'),
(5, 10, 100, 110, '手动调整', 'DD260457626797', NULL, NULL, '2026-05-28 15:13:00'),
(5, -15, 110, 95, '违规下架', 'DD260544135287', NULL, NULL, '2026-07-18 18:35:00'),
(5, 2, 95, 97, '交易好评', 'DD260403614301', NULL, NULL, '2026-05-17 05:43:00'),
(5, 2, 97, 99, '交易好评', 'DD260466850816', NULL, NULL, '2026-10-17 13:20:00'),
(5, 10, 99, 109, '手动调整', 'DD260522235954', NULL, NULL, '2026-09-23 13:30:00'),
(5, 7, 109, 116, '人工调整', 'DD260549078069', '运营人工校准信用分', 2, '2026-06-02 13:02:00'),
(6, -5, 100, 95, '差评', 'DD260411668258', NULL, NULL, '2026-06-15 12:56:00'),
(6, -15, 95, 80, '违规下架', 'DD260525964815', NULL, NULL, '2026-09-26 06:52:00'),
(6, 12, 80, 92, '人工调整', 'DD260461331518', '运营人工校准信用分', 2, '2026-08-26 09:23:00'),
(7, -15, 100, 85, '违规下架', 'DD260484661782', NULL, NULL, '2026-07-23 05:37:00'),
(7, 10, 85, 95, '手动调整', 'DD260424798577', NULL, NULL, '2026-10-13 10:19:00'),
(7, 2, 95, 97, '交易好评', 'DD260511736210', NULL, NULL, '2026-10-22 05:27:00'),
(7, 2, 97, 99, '交易好评', 'DD260542628156', NULL, NULL, '2026-07-30 00:34:00'),
(7, -21, 99, 78, '人工调整', 'DD260462023199', '运营人工校准信用分', 2, '2026-09-23 12:48:00'),
(8, -5, 100, 95, '差评', 'DD260454428278', NULL, NULL, '2026-05-19 18:43:00'),
(8, 2, 95, 97, '交易好评', 'DD260515582849', NULL, NULL, '2026-08-25 16:36:00'),
(8, -10, 97, 87, '放鸽子', 'DD260483603774', NULL, NULL, '2026-08-19 14:50:00'),
(8, -41, 87, 46, '人工调整', 'DD260444431873', '运营人工校准信用分', 2, '2026-07-13 09:18:00'),
(9, 2, 100, 102, '交易好评', 'DD260441023714', NULL, NULL, '2026-07-21 18:10:00'),
(9, 10, 102, 112, '手动调整', 'DD260466058731', NULL, NULL, '2026-05-20 18:43:00'),
(9, -15, 112, 97, '违规下架', 'DD260423950875', NULL, NULL, '2026-08-24 18:34:00'),
(9, -10, 97, 87, '放鸽子', 'DD260535324199', NULL, NULL, '2026-06-12 01:33:00'),
(9, 2, 87, 89, '交易好评', 'DD260448139266', NULL, NULL, '2026-10-18 08:20:00'),
(9, 21, 89, 110, '人工调整', 'DD260436925638', '运营人工校准信用分', 2, '2026-08-15 19:13:00'),
(10, 2, 100, 102, '交易好评', 'DD260460536736', NULL, NULL, '2026-10-31 12:20:00'),
(10, -5, 102, 97, '差评', 'DD260530930178', NULL, NULL, '2026-06-05 17:55:00'),
(10, 3, 97, 100, '人工调整', 'DD260425094394', '运营人工校准信用分', 2, '2026-09-02 16:35:00'),
(11, -5, 100, 95, '差评', 'DD260426953493', NULL, NULL, '2026-06-21 13:26:00'),
(11, -10, 95, 85, '放鸽子', 'DD260445618128', NULL, NULL, '2026-09-23 01:57:00'),
(11, 2, 85, 87, '交易好评', 'DD260487315248', NULL, NULL, '2026-06-09 06:38:00'),
(11, 1, 87, 88, '人工调整', 'DD260495647456', '运营人工校准信用分', 2, '2026-09-26 17:02:00'),
(12, 2, 100, 102, '交易好评', 'DD260471963974', NULL, NULL, '2026-08-06 05:21:00'),
(12, -10, 102, 92, '放鸽子', 'DD260500734595', NULL, NULL, '2026-06-25 14:14:00'),
(12, -15, 92, 77, '违规下架', 'DD260543150212', NULL, NULL, '2026-05-28 12:54:00'),
(12, 55, 77, 132, '人工调整', 'DD260430134248', '运营人工校准信用分', 2, '2026-08-16 07:43:00'),
(13, 2, 100, 102, '交易好评', 'DD260494971533', NULL, NULL, '2026-10-21 06:18:00'),
(13, 10, 102, 112, '手动调整', 'DD260411998234', NULL, NULL, '2026-10-27 13:30:00'),
(13, -5, 112, 107, '差评', 'DD260446891393', NULL, NULL, '2026-09-29 14:36:00'),
(13, -11, 107, 96, '人工调整', 'DD260429002431', '运营人工校准信用分', 2, '2026-06-26 19:15:00');

-- 定价规则：v1 停用、v2 启用（权重合计 100%）
INSERT INTO `price_rule` (`rule_name`, `rule_version`, `weight_category`, `weight_condition`, `weight_supply`, `weight_heat`, `weight_credit`, `weight_info`, `status`, `effect_time`, `remark`) VALUES
('商品定价成交概率评分规则', 'v1', 30, 20, 15, 15, 10, 10, '停用', '2026-07-04 10:35:00', '初版权重，同分类成交样本不足时惩罚过重，已停用'),
('商品定价成交概率评分规则', 'v2', 30, 20, 15, 15, 10, 10, '启用', '2026-10-02 07:22:00', '调整供给系数与热度口径，样本不足改为不计分并在结果页提示');

-- 定价评分记录：32 条，含输入快照、六项得分、总分与人工修正留痕
INSERT INTO `price_calc` (`goods_id`, `rule_id`, `rule_version`, `snapshot`, `score_category`, `score_condition`, `score_supply`, `score_heat`, `score_credit`, `score_info`, `total_score`, `grade`, `suggest_low`, `suggest_high`, `conclusion`, `factors`, `missing`, `corrected_grade`, `correct_reason`, `correct_by`, `correct_time`, `adopted`, `create_time`) VALUES
(1, 2, 'v2', '{"categoryId":2,"condition":"九成新","campusId":1,"viewCount":316,"favoriteCount":21,"creditScore":128,"imageCount":4,"descLength":49,"originalPrice":3999,"price":2350}', 94.0, 88, 55, 46, 85.33, 70, 76.48, 'B', 1902.56, 2316.16, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', '同分类近30天成交样本 3 条，样本偏少，价格项参考性有限', NULL, NULL, NULL, NULL, 1, '2026-10-10 13:41:00'),
(2, 2, 'v2', '{"categoryId":2,"condition":"九成新","campusId":2,"viewCount":430,"favoriteCount":33,"creditScore":116,"imageCount":4,"descLength":39,"originalPrice":2199,"price":1299}', 94.0, 88, 62, 40, 77.33, 80, 76.83, 'B', 1051.67, 1280.29, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-09 09:33:00'),
(3, 2, 'v2', '{"categoryId":3,"condition":"七成新","campusId":3,"viewCount":469,"favoriteCount":28,"creditScore":92,"imageCount":4,"descLength":38,"originalPrice":129,"price":55}', 94.0, 72, 69, 79, 61.33, 90, 79.93, 'B', 36.43, 44.35, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-23 09:46:00'),
(4, 2, 'v2', '{"categoryId":3,"condition":"全新","campusId":1,"viewCount":354,"favoriteCount":41,"creditScore":78,"imageCount":4,"descLength":38,"originalPrice":399,"price":260}', 94.0, 100, 76, 84, 52.0, 100, 87.4, 'A', 239.2, 291.2, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, 'B', '人工复核：该商品为开学季刚需，实际成交价高于模型建议', 2, '2026-10-12 10:12:00', 1, '2026-10-27 11:30:00'),
(5, 2, 'v2', '{"categoryId":4,"condition":"九成新","campusId":2,"viewCount":744,"favoriteCount":29,"creditScore":46,"imageCount":4,"descLength":33,"originalPrice":1899,"price":899}', 94.0, 88, 83, 54, 30.67, 70, 76.42, 'B', 727.83, 886.05, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-30 23:03:00'),
(6, 2, 'v2', '{"categoryId":4,"condition":"七成新","campusId":1,"viewCount":158,"favoriteCount":42,"creditScore":128,"imageCount":4,"descLength":35,"originalPrice":99,"price":45}', 94.0, 72, 90, 68, 85.33, 80, 82.83, 'A', 29.81, 36.29, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', '同分类近30天成交样本 3 条，样本偏少，价格项参考性有限', NULL, NULL, NULL, NULL, 0, '2026-10-31 17:08:00'),
(7, 2, 'v2', '{"categoryId":7,"condition":"七成新","campusId":2,"viewCount":611,"favoriteCount":40,"creditScore":116,"imageCount":4,"descLength":32,"originalPrice":89,"price":25}', 94.0, 72, 57, 41, 77.33, 90, 74.03, 'B', 16.56, 20.16, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 1, '2026-10-09 06:10:00'),
(8, 2, 'v2', '{"categoryId":7,"condition":"九成新","campusId":3,"viewCount":165,"favoriteCount":34,"creditScore":92,"imageCount":4,"descLength":29,"originalPrice":59,"price":18}', 94.0, 88, 64, 75, 61.33, 100, 82.78, 'A', 14.57, 17.74, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-07 01:18:00'),
(9, 2, 'v2', '{"categoryId":8,"condition":"七成新","campusId":1,"viewCount":226,"favoriteCount":30,"creditScore":78,"imageCount":4,"descLength":33,"originalPrice":168,"price":60}', 94.0, 72, 71, 76, 52.0, 70, 76.85, 'B', 39.74, 48.38, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-15 23:44:00'),
(10, 2, 'v2', '{"categoryId":8,"condition":"九成新","campusId":2,"viewCount":814,"favoriteCount":33,"creditScore":46,"imageCount":4,"descLength":30,"originalPrice":68,"price":22}', 94.0, 88, 78, 64, 30.67, 80, 78.17, 'B', 17.81, 21.68, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 1, '2026-10-14 23:06:00'),
(11, 2, 'v2', '{"categoryId":9,"condition":"九成新","campusId":1,"viewCount":307,"favoriteCount":25,"creditScore":128,"imageCount":4,"descLength":24,"originalPrice":39,"price":12}', 94.0, 88, 85, 37, 85.33, 90, 81.63, 'A', 9.72, 11.83, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', '同分类近30天成交样本 3 条，样本偏少，价格项参考性有限', 'B', '人工复核：该商品为开学季刚需，实际成交价高于模型建议', 2, '2026-10-22 16:25:00', 0, '2026-11-01 10:42:00'),
(12, 2, 'v2', '{"categoryId":11,"condition":"九成新","campusId":2,"viewCount":742,"favoriteCount":26,"creditScore":116,"imageCount":4,"descLength":32,"originalPrice":78,"price":30}', 94.0, 88, 92, 52, 77.33, 100, 85.13, 'A', 24.29, 29.57, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-22 03:57:00'),
(13, 2, 'v2', '{"categoryId":12,"condition":"七成新","campusId":3,"viewCount":137,"favoriteCount":11,"creditScore":92,"imageCount":4,"descLength":25,"originalPrice":119,"price":38}', 94.0, 72, 59, 47, 61.33, 70, 71.63, 'B', 25.17, 30.64, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 1, '2026-10-29 16:16:00'),
(14, 2, 'v2', '{"categoryId":12,"condition":"九成新","campusId":1,"viewCount":729,"favoriteCount":20,"creditScore":78,"imageCount":4,"descLength":23,"originalPrice":139,"price":45}', 94.0, 88, 66, 39, 52.0, 80, 74.75, 'B', 36.43, 44.35, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-17 19:46:00'),
(15, 2, 'v2', '{"categoryId":13,"condition":"七成新","campusId":2,"viewCount":364,"favoriteCount":22,"creditScore":46,"imageCount":4,"descLength":36,"originalPrice":1580,"price":480}', 94.0, 72, 73, 34, 30.67, 90, 70.72, 'B', 317.95, 387.07, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-28 18:04:00'),
(16, 2, 'v2', '{"categoryId":13,"condition":"九成新","campusId":1,"viewCount":47,"favoriteCount":24,"creditScore":128,"imageCount":4,"descLength":26,"originalPrice":599,"price":210}', 94.0, 88, 80, 77, 85.33, 100, 87.88, 'A', 170.02, 206.98, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', '同分类近30天成交样本 3 条，样本偏少，价格项参考性有限', NULL, NULL, NULL, NULL, 1, '2026-10-25 23:26:00'),
(17, 2, 'v2', '{"categoryId":15,"condition":"九成新","campusId":2,"viewCount":788,"favoriteCount":34,"creditScore":116,"imageCount":4,"descLength":30,"originalPrice":260,"price":85}', 94.0, 88, 87, 38, 77.33, 70, 79.28, 'B', 68.82, 83.78, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-23 02:35:00'),
(18, 2, 'v2', '{"categoryId":15,"condition":"全新","campusId":3,"viewCount":465,"favoriteCount":41,"creditScore":92,"imageCount":4,"descLength":27,"originalPrice":89,"price":28}', 94.0, 100, 94, 75, 61.33, 80, 87.68, 'A', 25.76, 31.36, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, 'B', '人工复核：该商品为开学季刚需，实际成交价高于模型建议', 2, '2026-10-12 23:13:00', 0, '2026-10-13 12:17:00'),
(19, 2, 'v2', '{"categoryId":16,"condition":"七成新","campusId":1,"viewCount":533,"favoriteCount":33,"creditScore":78,"imageCount":4,"descLength":31,"originalPrice":380,"price":120}', 94.0, 72, 61, 83, 52.0, 90, 78.4, 'B', 79.49, 96.77, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 1, '2026-10-25 07:07:00'),
(20, 2, 'v2', '{"categoryId":16,"condition":"七成新","campusId":2,"viewCount":172,"favoriteCount":33,"creditScore":46,"imageCount":4,"descLength":22,"originalPrice":189,"price":65}', 94.0, 72, 68, 82, 30.67, 100, 78.17, 'B', 43.06, 52.42, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-08 10:43:00'),
(21, 2, 'v2', '{"categoryId":17,"condition":"九成新","campusId":1,"viewCount":505,"favoriteCount":44,"creditScore":128,"imageCount":4,"descLength":25,"originalPrice":429,"price":150}', 94.0, 88, 75, 55, 85.33, 70, 80.83, 'A', 121.44, 147.84, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', '同分类近30天成交样本 3 条，样本偏少，价格项参考性有限', NULL, NULL, NULL, NULL, 0, '2026-10-19 02:26:00'),
(22, 2, 'v2', '{"categoryId":17,"condition":"九成新","campusId":2,"viewCount":82,"favoriteCount":39,"creditScore":116,"imageCount":4,"descLength":21,"originalPrice":128,"price":40}', 94.0, 88, 82, 52, 77.33, 80, 81.63, 'A', 32.38, 39.42, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 1, '2026-10-30 08:01:00'),
(23, 2, 'v2', '{"categoryId":19,"condition":"九成新","campusId":3,"viewCount":137,"favoriteCount":30,"creditScore":92,"imageCount":4,"descLength":28,"originalPrice":499,"price":130}', 94.0, 88, 89, 47, 61.33, 90, 81.33, 'A', 105.25, 128.13, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-26 03:03:00'),
(24, 2, 'v2', '{"categoryId":20,"condition":"七成新","campusId":1,"viewCount":200,"favoriteCount":39,"creditScore":78,"imageCount":4,"descLength":22,"originalPrice":369,"price":95}', 94.0, 72, 56, 50, 52.0, 100, 73.7, 'B', 62.93, 76.61, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-18 10:06:00'),
(25, 2, 'v2', '{"categoryId":8,"condition":"九成新","campusId":2,"viewCount":181,"favoriteCount":2,"creditScore":46,"imageCount":4,"descLength":29,"originalPrice":149,"price":55}', 94.0, 88, 63, 31, 30.67, 70, 69.97, 'B', 44.53, 54.21, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 1, '2026-10-27 10:52:00'),
(26, 2, 'v2', '{"categoryId":3,"condition":"九成新","campusId":1,"viewCount":748,"favoriteCount":35,"creditScore":128,"imageCount":4,"descLength":32,"originalPrice":299,"price":105}', 94.0, 88, 70, 58, 85.33, 80, 81.53, 'A', 85.01, 103.49, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', '同分类近30天成交样本 3 条，样本偏少，价格项参考性有限', NULL, NULL, NULL, NULL, 0, '2026-10-26 01:31:00'),
(27, 2, 'v2', '{"categoryId":12,"condition":"九成新","campusId":2,"viewCount":727,"favoriteCount":16,"creditScore":116,"imageCount":4,"descLength":25,"originalPrice":129,"price":42}', 94.0, 88, 77, 37, 77.33, 90, 79.63, 'B', 34.0, 41.4, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-25 13:35:00'),
(28, 2, 'v2', '{"categoryId":23,"condition":"七成新","campusId":3,"viewCount":490,"favoriteCount":29,"creditScore":92,"imageCount":4,"descLength":24,"originalPrice":220,"price":78}', 94.0, 72, 84, 40, 61.33, 100, 77.33, 'B', 51.67, 62.9, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 1, '2026-10-07 18:40:00'),
(29, 2, 'v2', '{"categoryId":22,"condition":"九成新","campusId":1,"viewCount":157,"favoriteCount":4,"creditScore":78,"imageCount":4,"descLength":21,"originalPrice":79,"price":25}', 94.0, 88, 91, 67, 52.0, 70, 81.7, 'A', 20.24, 24.64, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-31 04:08:00'),
(30, 2, 'v2', '{"categoryId":23,"condition":"七成新","campusId":2,"viewCount":399,"favoriteCount":12,"creditScore":46,"imageCount":4,"descLength":27,"originalPrice":680,"price":260}', 94.0, 72, 58, 69, 30.67, 80, 72.72, 'B', 172.22, 209.66, '略高于同类成交区间，建议下调5%至10%或补充描述', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, NULL, NULL, NULL, NULL, 0, '2026-10-14 08:29:00'),
(31, 2, 'v2', '{"categoryId":5,"condition":"九成新","campusId":1,"viewCount":17,"favoriteCount":33,"creditScore":128,"imageCount":4,"descLength":25,"originalPrice":169,"price":58}', 94.0, 88, 65, 47, 85.33, 90, 80.13, 'A', 46.96, 57.16, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', '同分类近30天成交样本 3 条，样本偏少，价格项参考性有限', NULL, NULL, NULL, NULL, 1, '2026-10-21 23:15:00'),
(32, 2, 'v2', '{"categoryId":3,"condition":"九成新","campusId":2,"viewCount":677,"favoriteCount":30,"creditScore":116,"imageCount":4,"descLength":27,"originalPrice":799,"price":320}', 94.0, 88, 72, 47, 77.33, 100, 81.38, 'A', 259.07, 315.39, '定价合理，预计7天内成交，建议保持价格并补充实拍图', '同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权', NULL, 'B', '人工复核：该商品为开学季刚需，实际成交价高于模型建议', 2, '2026-10-17 06:16:00', 0, '2026-10-17 19:06:00');

-- 公告
INSERT INTO `sys_notice` (`title`, `content`, `notice_type`, `publish_time`, `publisher`, `status`) VALUES
('关于春季学期二手交易专场面交点调整的通知', '为配合校园活动，4 月 12 日至 4 月 15 日东校区图书馆一层大厅面交点暂停使用，请改约一食堂门口广场，带来不便敬请谅解。', '平台公告', '2026-10-23 10:00:00', '林晓运营', '已发布'),
('二手交易防诈骗提示：一律当面验货', '近期出现要求先转账后发货的案例，平台不提供在线支付，也不支持预付定金。请务必当面验货、当面交付，遇到可疑账号请立即举报。', '交易提醒', '2026-10-10 09:30:00', '系统管理员', '已发布'),
('商品发布规范更新：实拍图不少于 2 张', '自本月起，发布闲置需上传不少于 2 张实拍图，禁止使用网图或带商铺水印的图片，违规商品将被驳回并计入信用记录。', '规则变更', '2026-09-17 15:00:00', '林晓运营', '已发布'),
('毕业季跳蚤市场线上专区开放', '毕业季专区已开放，教材、宿舍用品、代步工具集中展示，支持按楼栋筛选，欢迎毕业生发布闲置。', '平台公告', '2026-08-23 11:00:00', '钱立运营', '已发布');

-- 站内消息
INSERT INTO `sys_message` (`receiver_id`, `title`, `content`, `biz_type`, `biz_id`, `msg_type`, `read_flag`, `create_time`) VALUES
(4, '商品审核通过（1）', '你发布的商品已通过审核，现已上架展示', 'goods', 1, '审核', True, '2026-09-28 14:39:00'),
(5, '订单待确认提醒（2）', '有买家下单，请在12小时内确认接单，超时订单将自动取消', 'order', 2, '订单', False, '2026-10-15 06:51:00'),
(9, '举报处理结果（3）', '你提交的举报已处理完成，处理结果可在举报记录中查看', 'report', 3, '纠纷', False, '2026-10-17 07:41:00'),
(10, '信用分变动提醒（4）', '你的信用分发生变动，可在我的信用中查看明细', 'credit', 4, '信用', True, '2026-10-17 00:18:00'),
(3, '评价邀请（5）', '订单已完成，请在7天内完成评价', 'order', 5, '系统通知', False, '2026-10-04 02:38:00'),
(6, '商品审核通过（6）', '你发布的商品已通过审核，现已上架展示', 'goods', 6, '审核', False, '2026-10-09 16:13:00'),
(4, '订单待确认提醒（7）', '有买家下单，请在12小时内确认接单，超时订单将自动取消', 'order', 7, '订单', True, '2026-10-05 03:14:00'),
(5, '举报处理结果（8）', '你提交的举报已处理完成，处理结果可在举报记录中查看', 'report', 8, '纠纷', False, '2026-10-04 07:43:00'),
(9, '信用分变动提醒（9）', '你的信用分发生变动，可在我的信用中查看明细', 'credit', 9, '信用', False, '2026-10-31 04:59:00'),
(10, '评价邀请（10）', '订单已完成，请在7天内完成评价', 'order', 10, '系统通知', True, '2026-09-22 17:39:00'),
(3, '商品审核通过（11）', '你发布的商品已通过审核，现已上架展示', 'goods', 11, '审核', False, '2026-10-03 18:21:00'),
(6, '订单待确认提醒（12）', '有买家下单，请在12小时内确认接单，超时订单将自动取消', 'order', 12, '订单', False, '2026-10-11 05:36:00'),
(4, '举报处理结果（13）', '你提交的举报已处理完成，处理结果可在举报记录中查看', 'report', 13, '纠纷', True, '2026-10-07 17:41:00'),
(5, '信用分变动提醒（14）', '你的信用分发生变动，可在我的信用中查看明细', 'credit', 14, '信用', False, '2026-09-29 07:55:00');

-- 操作日志：审核/冻结/信用调整/改权重等关键写操作留痕
INSERT INTO `sys_oper_log` (`module`, `oper_type`, `biz_no`, `content`, `oper_id`, `oper_name`, `oper_time`, `ip`) VALUES
('商品审核', '审核通过', 'XY2604010001', '通过商品审核并上架', 1, '系统管理员', '2026-09-25 14:19:00', '127.0.0.1'),
('举报处理', '审核驳回', 'XY2604020002', '驳回商品：图片含联系方式水印', 2, '林晓运营', '2026-10-22 05:49:00', '127.0.0.1'),
('信用调整', '受理', 'XY2604030003', '受理举报并冻结商品', 2, '林晓运营', '2026-09-08 13:02:00', '127.0.0.1'),
('规则配置', '人工调整', 'XY2604040004', '信用分人工调整 +5，原因：核实后补偿', 2, '钱立运营', '2026-09-04 12:12:00', '127.0.0.1'),
('用户管理', '修改权重', 'XY2604050005', '调整定价规则权重并生成 v2 版本', 1, '系统管理员', '2026-10-15 09:36:00', '127.0.0.1'),
('订单管理', '冻结账号', 'XY2604060006', '冻结账号：校外人员批量转卖', 1, '系统管理员', '2026-09-13 00:34:00', '127.0.0.1'),
('商品审核', '审核通过', 'XY2604070007', '通过商品审核并上架', 1, '系统管理员', '2026-09-29 04:39:00', '127.0.0.1'),
('举报处理', '审核驳回', 'XY2604080008', '驳回商品：图片含联系方式水印', 2, '林晓运营', '2026-10-25 14:55:00', '127.0.0.1'),
('信用调整', '受理', 'XY2604090009', '受理举报并冻结商品', 2, '林晓运营', '2026-09-25 13:55:00', '127.0.0.1'),
('规则配置', '人工调整', 'XY2604100010', '信用分人工调整 +5，原因：核实后补偿', 2, '钱立运营', '2026-09-23 11:52:00', '127.0.0.1'),
('用户管理', '修改权重', 'XY2604110011', '调整定价规则权重并生成 v2 版本', 1, '系统管理员', '2026-10-09 05:10:00', '127.0.0.1'),
('订单管理', '冻结账号', 'XY2604120012', '冻结账号：校外人员批量转卖', 1, '系统管理员', '2026-09-02 00:46:00', '127.0.0.1'),
('商品审核', '审核通过', 'XY2604130013', '通过商品审核并上架', 1, '系统管理员', '2026-10-02 02:45:00', '127.0.0.1'),
('举报处理', '审核驳回', 'XY2604140014', '驳回商品：图片含联系方式水印', 2, '林晓运营', '2026-09-02 16:10:00', '127.0.0.1'),
('信用调整', '受理', 'XY2604150015', '受理举报并冻结商品', 2, '林晓运营', '2026-09-25 23:18:00', '127.0.0.1'),
('规则配置', '人工调整', 'XY2604160016', '信用分人工调整 +5，原因：核实后补偿', 2, '钱立运营', '2026-10-22 13:30:00', '127.0.0.1'),
('用户管理', '修改权重', 'XY2604170017', '调整定价规则权重并生成 v2 版本', 1, '系统管理员', '2026-10-08 01:13:00', '127.0.0.1'),
('订单管理', '冻结账号', 'XY2604180018', '冻结账号：校外人员批量转卖', 1, '系统管理员', '2026-10-01 03:03:00', '127.0.0.1');

-- 文件元数据
INSERT INTO `sys_file` (`file_name`, `file_path`, `file_size`, `file_type`, `biz_type`, `biz_id`, `uploader_id`, `create_time`) VALUES
('goods_10.jpg', '/uploads/seed/goods/10.jpg', 209920, 'jpg', 'goods', 1, 4, '2026-09-30 18:47:00'),
('goods_17.jpg', '/uploads/seed/goods/17.jpg', 196608, 'jpg', 'goods', 2, 5, '2026-09-07 15:21:00'),
('goods_06.jpg', '/uploads/seed/goods/06.jpg', 158720, 'jpg', 'goods', 3, 6, '2026-08-05 15:48:00'),
('goods_08.jpg', '/uploads/seed/goods/08.jpg', 361472, 'jpg', 'goods', 4, 7, '2026-09-19 14:45:00'),
('goods_01.jpg', '/uploads/seed/goods/01.jpg', 307200, 'jpg', 'goods', 5, 8, '2026-08-14 11:35:00'),
('goods_08.jpg', '/uploads/seed/goods/08.jpg', 378880, 'jpg', 'goods', 6, 4, '2026-09-23 03:52:00'),
('goods_13.jpg', '/uploads/seed/goods/13.jpg', 159744, 'jpg', 'goods', 7, 5, '2026-07-29 00:11:00'),
('goods_21.jpg', '/uploads/seed/goods/21.jpg', 234496, 'jpg', 'goods', 8, 6, '2026-10-21 03:06:00'),
('goods_13.jpg', '/uploads/seed/goods/13.jpg', 285696, 'jpg', 'goods', 9, 7, '2026-08-19 10:01:00'),
('goods_21.jpg', '/uploads/seed/goods/21.jpg', 274432, 'jpg', 'goods', 10, 8, '2026-08-16 08:02:00');

SET FOREIGN_KEY_CHECKS = 1;

-- 数据规模：商品 40 条 / 订单 34 条 / 评价 22 条 / 举报 12 条 / 信用流水 46 条 / 评分记录 32 条
