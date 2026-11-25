#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""NEW-051 校园二手物品交易平台 · init.sql 生成器

按需求文档第 6 节表结构生成建库建表语句与测试数据。
改动表结构只需改本文件的 TABLES 定义后重跑：
    python3 sql/gen_sql.py
输出：sql/init.sql（可重复执行，先 DROP DATABASE 再重建）
"""
import os
import random

random.seed(20261101)
BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(BASE, "sql", "init.sql")
DB = "new_051"

# 统一标准字段（业务表统一带，不加物理外键）
STD = [
    ("create_by", "bigint DEFAULT NULL COMMENT '创建人'"),
    ("create_time", "datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'"),
    ("update_by", "bigint DEFAULT NULL COMMENT '更新人'"),
    ("update_time", "datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'"),
    ("deleted", "tinyint NOT NULL DEFAULT 0 COMMENT '0正常 1删除'"),
    ("version", "int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'"),
]

TABLES = {}

TABLES["category"] = dict(comment="商品分类", cols=[
    ("parent_id", "bigint NOT NULL DEFAULT 0 COMMENT '0表示一级分类，最多两级'"),
    ("category_code", "varchar(30) NOT NULL COMMENT '分类编码，大写字母与下划线'"),
    ("name", "varchar(50) NOT NULL COMMENT '分类名称'"),
    ("icon", "varchar(100) DEFAULT NULL COMMENT '图标相对路径'"),
    ("sort_no", "int NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前'"),
    ("status", "tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用'"),
    ("is_hot", "tinyint NOT NULL DEFAULT 0 COMMENT '1首页热门 0普通'"),
    ("remark", "varchar(200) DEFAULT NULL COMMENT '运营备注'"),
], keys=[
    "UNIQUE KEY uk_category_code (category_code)",
    "KEY idx_parent_sort (parent_id, sort_no)",
])

TABLES["campus_place"] = dict(comment="校区面交点", cols=[
    ("campus_id", "int NOT NULL COMMENT '校区编号1东2西3北'"),
    ("campus_name", "varchar(50) NOT NULL COMMENT '校区名称'"),
    ("place_name", "varchar(80) NOT NULL COMMENT '面交点名称'"),
    ("address_desc", "varchar(200) NOT NULL COMMENT '位置说明'"),
    ("open_start", "time NOT NULL DEFAULT '08:00:00' COMMENT '开放开始时间'"),
    ("open_end", "time NOT NULL DEFAULT '21:00:00' COMMENT '开放结束时间'"),
    ("manager_name", "varchar(30) DEFAULT NULL COMMENT '现场联系人，仅运营可见'"),
    ("manager_phone", "varchar(20) DEFAULT NULL COMMENT '联系电话，前台脱敏'"),
    ("status", "tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用'"),
], keys=[
    "KEY idx_campus_status (campus_name, status)",
    "KEY idx_place_name (place_name)",
])

TABLES["goods"] = dict(comment="闲置商品", cols=[
    ("goods_no", "varchar(32) NOT NULL COMMENT 'XY+yyMMdd+4位序号'"),
    ("seller_id", "bigint NOT NULL COMMENT '卖家 sys_user.id'"),
    ("category_id", "bigint NOT NULL COMMENT '二级分类 category.id'"),
    ("title", "varchar(120) NOT NULL COMMENT '标题'"),
    ("description", "text NOT NULL COMMENT '商品描述，10-1000字'"),
    ("condition_level", "varchar(20) NOT NULL DEFAULT '九成新' COMMENT '全新/九成新/七成新/五成新/有瑕疵'"),
    ("price", "decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '出售价'"),
    ("original_price", "decimal(10,2) DEFAULT NULL COMMENT '入手价，用于折价率'"),
    ("campus_id", "int NOT NULL COMMENT '校区编号'"),
    ("campus_name", "varchar(50) NOT NULL COMMENT '校区名称（冗余便于列表展示）'"),
    ("trade_place_id", "bigint DEFAULT NULL COMMENT '面交点 campus_place.id'"),
    ("trade_type", "varchar(20) NOT NULL DEFAULT '面交' COMMENT '面交/自提/可送'"),
    ("status", "varchar(20) NOT NULL DEFAULT '待审核' COMMENT '待审核/在售/已下架/已售出/审核驳回/冻结'"),
    ("audit_remark", "varchar(200) DEFAULT NULL COMMENT '审核意见，驳回时必填'"),
    ("view_count", "int NOT NULL DEFAULT 0 COMMENT '浏览量'"),
    ("favorite_count", "int NOT NULL DEFAULT 0 COMMENT '收藏量，用于热度评分'"),
    ("cover_url", "varchar(255) NOT NULL COMMENT '封面图相对路径'"),
    ("image_urls", "varchar(1000) DEFAULT NULL COMMENT '实拍图，相对路径逗号分隔，最多6张'"),
], keys=[
    "UNIQUE KEY uk_goods_no (goods_no)",
    "KEY idx_category_status (category_id, status)",
    "KEY idx_seller_status (seller_id, status)",
    "KEY idx_campus_status (campus_id, status)",
])

TABLES["trade_order"] = dict(comment="交易订单", cols=[
    ("order_no", "varchar(32) NOT NULL COMMENT 'DD+yyMMdd+6位序号'"),
    ("goods_id", "bigint NOT NULL COMMENT '商品 goods.id'"),
    ("goods_title", "varchar(120) NOT NULL COMMENT '商品标题快照'"),
    ("goods_cover", "varchar(255) NOT NULL COMMENT '商品封面快照'"),
    ("buyer_id", "bigint NOT NULL COMMENT '买家 sys_user.id'"),
    ("seller_id", "bigint NOT NULL COMMENT '卖家，下单时快照'"),
    ("deal_price", "decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '成交价，卖家确认时填写'"),
    ("pay_type", "varchar(20) NOT NULL DEFAULT '现金' COMMENT '现金/转账/其他'"),
    ("appoint_time", "datetime NOT NULL COMMENT '面交时间'"),
    ("appoint_place", "varchar(120) NOT NULL COMMENT '面交点'"),
    ("status", "varchar(20) NOT NULL DEFAULT '待卖家确认' COMMENT '待卖家确认/待面交/已完成/已取消/纠纷中'"),
    ("cancel_reason", "varchar(200) DEFAULT NULL COMMENT '取消原因'"),
    ("buyer_confirm_time", "datetime DEFAULT NULL COMMENT '买家确认收货时间'"),
    ("seller_confirm_time", "datetime DEFAULT NULL COMMENT '卖家接单时间'"),
    ("lock_expire_time", "datetime DEFAULT NULL COMMENT '商品锁定到期时间'"),
], keys=[
    "UNIQUE KEY uk_order_no (order_no)",
    "KEY idx_buyer_status (buyer_id, status)",
    "KEY idx_goods_status (goods_id, status)",
    "KEY idx_appoint_time (appoint_time)",
])

TABLES["review"] = dict(comment="交易评价", cols=[
    ("order_id", "bigint NOT NULL COMMENT '订单 trade_order.id'"),
    ("from_user_id", "bigint NOT NULL COMMENT '评价人'"),
    ("to_user_id", "bigint NOT NULL COMMENT '被评人'"),
    ("score", "tinyint NOT NULL DEFAULT 5 COMMENT '1至5星'"),
    ("tags", "varchar(200) DEFAULT NULL COMMENT '描述一致/准时/好沟通/包装完好'"),
    ("content", "varchar(500) DEFAULT NULL COMMENT '评价内容'"),
    ("anonymous", "tinyint NOT NULL DEFAULT 0 COMMENT '0显示昵称 1匿名'"),
    ("append_content", "varchar(500) DEFAULT NULL COMMENT '追评，30天内一次'"),
    ("status", "varchar(20) NOT NULL DEFAULT '待审核' COMMENT '待审核/已展示/已隐藏'"),
], keys=[
    "UNIQUE KEY uk_order_from (order_id, from_user_id)",
    "KEY idx_to_user (to_user_id, status)",
])

TABLES["report"] = dict(comment="举报与纠纷", cols=[
    ("report_no", "varchar(32) NOT NULL COMMENT 'JB+yyMMdd+4位序号'"),
    ("biz_type", "varchar(20) NOT NULL COMMENT '商品/订单/用户'"),
    ("biz_id", "bigint NOT NULL COMMENT '按类型关联 goods/trade_order/sys_user'"),
    ("biz_title", "varchar(200) DEFAULT NULL COMMENT '关联对象标题快照'"),
    ("reporter_id", "bigint NOT NULL COMMENT '举报人'"),
    ("reason_type", "varchar(30) NOT NULL COMMENT '违禁品/虚假描述/诈骗/骚扰/校外人员'"),
    ("description", "varchar(500) NOT NULL COMMENT '问题描述，不少于10字'"),
    ("evidence_urls", "varchar(1000) DEFAULT NULL COMMENT '证据图片，最多6张'"),
    ("status", "varchar(20) NOT NULL DEFAULT '待受理' COMMENT '待受理/处理中/已处理/已驳回/申诉中'"),
    ("handler_id", "bigint DEFAULT NULL COMMENT '处理人'"),
    ("handler_name", "varchar(30) DEFAULT NULL COMMENT '处理人姓名'"),
    ("handle_result", "varchar(500) DEFAULT NULL COMMENT '处理结果'"),
    ("appeal_status", "varchar(20) NOT NULL DEFAULT '无' COMMENT '无/申诉中/申诉通过/申诉驳回'"),
], keys=[
    "UNIQUE KEY uk_report_no (report_no)",
    "KEY idx_status_create (status, create_time)",
    "KEY idx_biz (biz_type, biz_id)",
])

TABLES["credit_log"] = dict(comment="信用分流水", cols=[
    ("user_id", "bigint NOT NULL COMMENT '用户 sys_user.id'"),
    ("change_value", "int NOT NULL DEFAULT 0 COMMENT '正数加分，负数扣分'"),
    ("before_score", "int NOT NULL DEFAULT 100 COMMENT '变化前分值'"),
    ("after_score", "int NOT NULL DEFAULT 100 COMMENT '变化后分值'"),
    ("reason_type", "varchar(30) NOT NULL COMMENT '交易好评/差评/放鸽子/违规下架/人工调整'"),
    ("biz_no", "varchar(32) DEFAULT NULL COMMENT '订单号或举报编号'"),
    ("remark", "varchar(200) DEFAULT NULL COMMENT '说明'"),
    ("operator_id", "bigint DEFAULT NULL COMMENT '操作人，系统自动为空'"),
], keys=[
    "KEY idx_user_create (user_id, create_time)",
    "KEY idx_biz_no (biz_no)",
])

TABLES["price_rule"] = dict(comment="定价评分规则版本", cols=[
    ("rule_name", "varchar(60) NOT NULL COMMENT '规则名称'"),
    ("rule_version", "varchar(20) NOT NULL COMMENT '规则版本号，如 v1/v2'"),
    ("weight_category", "int NOT NULL DEFAULT 30 COMMENT '同分类成交价偏离度权重%'"),
    ("weight_condition", "int NOT NULL DEFAULT 20 COMMENT '成色权重%'"),
    ("weight_supply", "int NOT NULL DEFAULT 15 COMMENT '同校同类在售数量权重%'"),
    ("weight_heat", "int NOT NULL DEFAULT 15 COMMENT '发布时长与浏览转化权重%'"),
    ("weight_credit", "int NOT NULL DEFAULT 10 COMMENT '卖家信用分权重%'"),
    ("weight_info", "int NOT NULL DEFAULT 10 COMMENT '图文完整度权重%'"),
    ("status", "varchar(20) NOT NULL DEFAULT '草稿' COMMENT '草稿/启用/停用'"),
    ("effect_time", "datetime DEFAULT NULL COMMENT '生效时间'"),
    ("remark", "varchar(200) DEFAULT NULL COMMENT '变更说明'"),
], keys=[
    "KEY idx_status (status)",
])

TABLES["price_calc"] = dict(comment="定价评分记录", cols=[
    ("goods_id", "bigint NOT NULL COMMENT '商品 goods.id'"),
    ("rule_id", "bigint NOT NULL COMMENT '规则 price_rule.id'"),
    ("rule_version", "varchar(20) NOT NULL COMMENT '计算时使用的规则版本'"),
    ("snapshot", "text COMMENT '输入快照 JSON'"),
    ("score_category", "decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '分类价格偏离得分'"),
    ("score_condition", "decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '成色得分'"),
    ("score_supply", "decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '校区供需得分'"),
    ("score_heat", "decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '热度得分'"),
    ("score_credit", "decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '信用得分'"),
    ("score_info", "decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '图文完整度得分'"),
    ("total_score", "decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '加权总分'"),
    ("grade", "varchar(5) NOT NULL COMMENT 'A/B/C'"),
    ("suggest_low", "decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '建议价下限'"),
    ("suggest_high", "decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '建议价上限'"),
    ("conclusion", "varchar(300) DEFAULT NULL COMMENT '推荐结论'"),
    ("factors", "varchar(1000) DEFAULT NULL COMMENT '主要影响因素'"),
    ("missing", "varchar(500) DEFAULT NULL COMMENT '未满足条件'"),
    ("corrected_grade", "varchar(5) DEFAULT NULL COMMENT '人工修正后的等级'"),
    ("correct_reason", "varchar(200) DEFAULT NULL COMMENT '修正原因，不少于5字'"),
    ("correct_by", "bigint DEFAULT NULL COMMENT '修正人'"),
    ("correct_time", "datetime DEFAULT NULL COMMENT '修正时间'"),
    ("adopted", "tinyint NOT NULL DEFAULT 0 COMMENT '卖家是否采纳建议价 0未采纳1采纳'"),
], keys=[
    "KEY idx_goods (goods_id)",
    "KEY idx_rule (rule_id)",
])

TABLES["sys_user"] = dict(comment="用户表", cols=[
    ("username", "varchar(50) NOT NULL COMMENT '登录名'"),
    ("password", "varchar(100) NOT NULL COMMENT 'BCrypt 密码'"),
    ("real_name", "varchar(50) NOT NULL COMMENT '姓名/昵称'"),
    ("student_no", "varchar(30) DEFAULT NULL COMMENT '学号，学生账号导入时绑定'"),
    ("phone", "varchar(20) DEFAULT NULL COMMENT '手机号'"),
    ("email", "varchar(80) DEFAULT NULL COMMENT '邮箱'"),
    ("avatar", "varchar(255) DEFAULT NULL COMMENT '头像相对路径'"),
    ("gender", "varchar(10) DEFAULT NULL COMMENT '男/女'"),
    ("dept_id", "bigint DEFAULT NULL COMMENT '所属部门（运营人员）'"),
    ("campus_id", "int DEFAULT NULL COMMENT '绑定校区编号'"),
    ("campus_name", "varchar(50) DEFAULT NULL COMMENT '绑定校区名称'"),
    ("credit_score", "int NOT NULL DEFAULT 100 COMMENT '信用分 0-150'"),
    ("status", "tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用'"),
    ("login_fail_count", "int NOT NULL DEFAULT 0 COMMENT '连续登录失败次数'"),
    ("lock_until", "varchar(30) DEFAULT NULL COMMENT '锁定至'"),
    ("last_login", "varchar(30) DEFAULT NULL COMMENT '最近登录时间'"),
], keys=[
    "UNIQUE KEY uk_username (username)",
    "KEY idx_campus (campus_id)",
])

TABLES["sys_role"] = dict(comment="角色表", cols=[
    ("role_name", "varchar(50) NOT NULL COMMENT '角色名称'"),
    ("role_code", "varchar(50) NOT NULL COMMENT '角色编码'"),
    ("data_scope", "varchar(20) NOT NULL DEFAULT 'self' COMMENT 'all/dept/self'"),
    ("status", "tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用'"),
    ("remark", "varchar(200) DEFAULT NULL COMMENT '备注'"),
], keys=["UNIQUE KEY uk_role_code (role_code)"])

TABLES["sys_user_role"] = dict(comment="用户角色表", cols=[
    ("user_id", "bigint NOT NULL COMMENT '用户ID'"),
    ("role_id", "bigint NOT NULL COMMENT '角色ID'"),
], keys=["KEY idx_user (user_id)"])

TABLES["sys_menu"] = dict(comment="菜单权限表", cols=[
    ("parent_id", "bigint NOT NULL DEFAULT 0 COMMENT '上级菜单'"),
    ("menu_name", "varchar(50) NOT NULL COMMENT '菜单名称'"),
    ("path", "varchar(120) DEFAULT NULL COMMENT '路由地址'"),
    ("component", "varchar(120) DEFAULT NULL COMMENT '组件路径'"),
    ("perm", "varchar(100) DEFAULT NULL COMMENT '权限标识'"),
    ("menu_type", "varchar(20) NOT NULL DEFAULT 'menu' COMMENT 'catalog/menu/button'"),
    ("icon", "varchar(50) DEFAULT NULL COMMENT '图标键'"),
    ("sort", "int NOT NULL DEFAULT 0 COMMENT '排序号'"),
    ("visible", "tinyint NOT NULL DEFAULT 1 COMMENT '1显示 0隐藏'"),
], keys=["KEY idx_parent (parent_id)"])

TABLES["sys_role_menu"] = dict(comment="角色菜单表", cols=[
    ("role_id", "bigint NOT NULL COMMENT '角色ID'"),
    ("menu_id", "bigint NOT NULL COMMENT '菜单ID'"),
], keys=["KEY idx_role (role_id)"])

TABLES["sys_dict_type"] = dict(comment="字典类型表", cols=[
    ("dict_name", "varchar(60) NOT NULL COMMENT '字典名称'"),
    ("dict_type", "varchar(60) NOT NULL COMMENT '字典类型编码'"),
    ("status", "tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用'"),
    ("remark", "varchar(200) DEFAULT NULL COMMENT '备注'"),
], keys=["UNIQUE KEY uk_dict_type (dict_type)"])

TABLES["sys_dict_data"] = dict(comment="字典数据表", cols=[
    ("dict_type", "varchar(60) NOT NULL COMMENT '字典类型编码'"),
    ("dict_label", "varchar(60) NOT NULL COMMENT '字典标签'"),
    ("dict_value", "varchar(60) NOT NULL COMMENT '字典值'"),
    ("sort", "int NOT NULL DEFAULT 0 COMMENT '排序号'"),
    ("status", "tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用'"),
    ("remark", "varchar(200) DEFAULT NULL COMMENT '备注'"),
], keys=["KEY idx_type (dict_type)"])

TABLES["sys_file"] = dict(comment="文件表", cols=[
    ("file_name", "varchar(120) NOT NULL COMMENT '原始文件名'"),
    ("file_path", "varchar(255) NOT NULL COMMENT '相对路径'"),
    ("file_size", "bigint NOT NULL DEFAULT 0 COMMENT '字节数'"),
    ("file_type", "varchar(50) DEFAULT NULL COMMENT '扩展名'"),
    ("biz_type", "varchar(30) DEFAULT NULL COMMENT '业务类型 goods/report/avatar'"),
    ("biz_id", "bigint DEFAULT NULL COMMENT '业务ID'"),
    ("uploader_id", "bigint DEFAULT NULL COMMENT '上传人'"),
], keys=["KEY idx_biz (biz_type, biz_id)"])

TABLES["sys_notice"] = dict(comment="公告表", cols=[
    ("title", "varchar(120) NOT NULL COMMENT '标题'"),
    ("content", "text NOT NULL COMMENT '内容'"),
    ("notice_type", "varchar(20) NOT NULL DEFAULT '平台公告' COMMENT '平台公告/交易提醒/规则变更'"),
    ("publish_time", "datetime DEFAULT NULL COMMENT '发布时间'"),
    ("publisher", "varchar(50) DEFAULT NULL COMMENT '发布人'"),
    ("status", "varchar(20) NOT NULL DEFAULT '已发布' COMMENT '草稿/已发布/已下线'"),
], keys=["KEY idx_status (status)"])

TABLES["sys_message"] = dict(comment="站内消息表", cols=[
    ("receiver_id", "bigint NOT NULL COMMENT '接收人'"),
    ("title", "varchar(120) NOT NULL COMMENT '标题'"),
    ("content", "varchar(500) NOT NULL COMMENT '内容'"),
    ("biz_type", "varchar(30) DEFAULT NULL COMMENT 'goods/order/report/credit'"),
    ("biz_id", "bigint DEFAULT NULL COMMENT '业务ID'"),
    ("msg_type", "varchar(20) NOT NULL DEFAULT '系统通知' COMMENT '审核/订单/纠纷/信用/系统通知'"),
    ("read_flag", "tinyint NOT NULL DEFAULT 0 COMMENT '0未读 1已读'"),
], keys=["KEY idx_receiver (receiver_id, read_flag)"])

TABLES["sys_oper_log"] = dict(comment="操作日志表", cols=[
    ("module", "varchar(50) NOT NULL COMMENT '模块'"),
    ("oper_type", "varchar(30) NOT NULL COMMENT '操作类型'"),
    ("biz_no", "varchar(40) DEFAULT NULL COMMENT '业务编号'"),
    ("content", "varchar(500) NOT NULL COMMENT '操作内容'"),
    ("oper_id", "bigint DEFAULT NULL COMMENT '操作人ID'"),
    ("oper_name", "varchar(50) DEFAULT NULL COMMENT '操作人'"),
    ("oper_time", "datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'"),
    ("ip", "varchar(40) DEFAULT NULL COMMENT 'IP'"),
], keys=["KEY idx_time (oper_time)"])


def ddl(name, spec):
    lines = [f"CREATE TABLE `{name}` (", "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',"]
    for col, typ in spec["cols"]:
        lines.append(f"  `{col}` {typ},")
    for col, typ in STD:
        lines.append(f"  `{col}` {typ},")
    lines.append("  PRIMARY KEY (`id`)")
    for k in spec["keys"]:
        lines.append(f"  ,{k}" if not k.startswith("UNIQUE") and not k.startswith("KEY") else f"  ,{k}")
    lines.append(f") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{spec['comment']}';")
    return "\n".join(lines).replace(",  ,", "  ,")


def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, (int, float)):
        return str(v)
    return "'" + str(v).replace("'", "''") + "'"


def insert(table, cols, rows):
    out = []
    for r in rows:
        out.append("(" + ", ".join(q(x) for x in r) + ")")
    body = ",\n".join(out)
    return f"INSERT INTO `{table}` ({', '.join('`'+c+'`' for c in cols)}) VALUES\n{body};"


def ts(days_ago, hour=None, minute=None):
    """相对当前时间的偏移，保证测试数据时间分散在近 6 个月"""
    import datetime
    base = datetime.datetime(2026, 11, 1, 20, 0, 0)
    d = base - datetime.timedelta(days=days_ago, hours=random.randint(0, 20), minutes=random.randint(0, 59))
    if hour is not None:
        d = d.replace(hour=hour, minute=minute or 0)
    return d.strftime("%Y-%m-%d %H:%M:%S")


parts = []
parts.append(f"""-- ============================================================================
-- NEW-051 校园二手物品交易平台 · 数据库初始化脚本
-- MySQL 8 / utf8mb4 ｜ 执行：mysql -uroot -p123456 < sql/init.sql
-- 生成器：sql/gen_sql.py（改表结构请改生成器后重跑，勿手改本文件）
-- ============================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
DROP DATABASE IF EXISTS {DB};
CREATE DATABASE {DB} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE {DB};
""")

for name, spec in TABLES.items():
    parts.append(f"\n-- {spec['comment']} {name}\n" + ddl(name, spec))

# ---------------------------------------------------------------- 分类
cats = [
    (1, 0, "ELEC", "电子产品", 1, 1), (2, 0, "BOOK", "图书教材", 2, 1),
    (3, 0, "LIFE", "生活用品", 3, 1), (4, 0, "SPORT", "运动户外", 4, 1),
    (5, 0, "CLOTH", "服饰鞋包", 5, 0), (6, 0, "STUDY", "学习文具", 6, 0),
]
subs = {
    1: [("ELEC_PHONE", "手机平板"), ("ELEC_PC", "电脑配件"), ("ELEC_AUDIO", "耳机音响"), ("ELEC_OTHER", "数码周边")],
    2: [("BOOK_TEXT", "教材教辅"), ("BOOK_EXAM", "考研考证"), ("BOOK_NOVEL", "文学小说")],
    3: [("LIFE_DORM", "宿舍用品"), ("LIFE_APPL", "小家电"), ("LIFE_BIKE", "代步工具")],
    4: [("SPORT_FIT", "健身器材"), ("SPORT_BALL", "球类装备"), ("SPORT_OUT", "户外露营")],
    5: [("CLOTH_TOP", "上装"), ("CLOTH_SHOE", "鞋靴")],
    6: [("STUDY_DESK", "桌面文具"), ("STUDY_DRAW", "画材乐器")],
}
rows, cid, pid2real = [], 0, {}
for pid, parent, code, name, sort, hot in cats:
    cid += 1
    # 一级分类的 id 不是序号（中间穿插了二级分类），先记下真实 id，供二级分类挂树用
    pid2real[pid] = cid
    rows.append((cid, 0, code, name, None, sort, 1, hot, None))
    for code2, name2 in subs[pid]:
        cid += 1
        rows.append((cid, pid2real[pid], code2, name2, None, 10, 1, 0, None))
parts.append("\n-- 商品分类：6 个一级 + 18 个二级\n" + insert(
    "category", ["id", "parent_id", "category_code", "name", "icon", "sort_no", "status", "is_hot", "remark"], rows))

# ---------------------------------------------------------------- 校区面交点
places = [
    (1, "东校区", "图书馆一层大厅", "图书馆正门进，服务台右侧休息区", "08:00:00", "21:30:00", "李文", "13800001001", 1),
    (1, "东校区", "一食堂门口广场", "一食堂南门，靠近快递驿站", "07:00:00", "20:00:00", "王倩", "13800001002", 1),
    (1, "东校区", "三号宿舍楼下", "三号楼门厅，宿管值班室旁", "08:30:00", "22:00:00", "张磊", "13800001003", 1),
    (2, "西校区", "体育馆东门", "体育馆东侧入口，雨天改到馆内前厅", "09:00:00", "21:00:00", "陈晨", "13800001004", 1),
    (2, "西校区", "西区快递中心", "快递中心取件口外侧长椅区", "08:00:00", "20:30:00", "赵敏", "13800001005", 1),
    (2, "西校区", "实验楼A座大厅", "A 座一层门禁外侧", "08:00:00", "18:00:00", None, None, 0),
    (3, "北校区", "北门警卫室旁", "北门内 20 米，警务室窗口", "07:30:00", "22:00:00", "孙浩", "13800001006", 1),
    (3, "北校区", "北区食堂二楼", "二楼靠窗自习区", "10:00:00", "20:00:00", "周琳", "13800001007", 1),
]
parts.append("\n-- 校区与面交点\n" + insert(
    "campus_place", ["campus_id", "campus_name", "place_name", "address_desc", "open_start", "open_end",
                     "manager_name", "manager_phone", "status"], places))

# ---------------------------------------------------------------- 角色
roles = [
    (1, "平台管理员", "ROLE_ADMIN", "all", 1, "管理用户、角色、菜单、字典、公告、参数和操作日志"),
    (2, "校园运营人员", "ROLE_OPERATOR", "dept", 1, "审核商品、维护分类与面交点、受理举报、调整信用分"),
    (3, "学生卖家", "ROLE_SELLER", "self", 1, "发布闲置、回复咨询、确认面交、下架商品、查看信用分"),
    (4, "学生买家", "ROLE_BUYER", "self", 1, "浏览检索、收藏商品、下单、确认收货、评价和举报"),
]
parts.append("\n-- 角色\n" + insert("sys_role", ["id", "role_name", "role_code", "data_scope", "status", "remark"], roles))

# ---------------------------------------------------------------- 用户
PWD = "$2a$10$4YQdDPK0Ry6y6uEezovclug/65r0jyxYgxzsEUGAoWVuXe.fPJU.u"  # 明文 123456，BCrypt cost=10
users = [
    (1, "admin", PWD, "系统管理员", None, "13900000001", "admin@campus.edu", "/uploads/seed/avatar/01.jpg", "男", 1, 1, "东校区", 100, 1),
    (2, "operator", PWD, "林晓运营", "OP2026001", "13900000002", "linxiao@campus.edu", "/uploads/seed/avatar/02.jpg", "女", 2, 1, "东校区", 100, 1),
    (3, "user", PWD, "陈同学", "2023010101", "13900000003", "chen@campus.edu", "/uploads/seed/avatar/03.jpg", "男", None, 1, "东校区", 104, 1),
    (4, "seller01", PWD, "周子航", "2023010203", "13900000004", "zhouzh@campus.edu", "/uploads/seed/avatar/04.jpg", "男", None, 1, "东校区", 128, 1),
    (5, "seller02", PWD, "苏明玉", "2023020108", "13900000005", "sumy@campus.edu", "/uploads/seed/avatar/05.jpg", "女", None, 2, "西校区", 116, 1),
    (6, "seller03", PWD, "何嘉禾", "2022030412", "13900000006", "hejh@campus.edu", "/uploads/seed/avatar/06.jpg", "女", None, 3, "北校区", 92, 1),
    (7, "seller04", PWD, "郑一鸣", "2023040517", "13900000007", "zhengyiming@campus.edu", "/uploads/seed/avatar/07.jpg", "男", None, 1, "东校区", 78, 1),
    (8, "seller05", PWD, "许安然", "2023050122", "13900000008", "xuar@campus.edu", "/uploads/seed/avatar/08.jpg", "女", None, 2, "西校区", 46, 1),
    (9, "buyer01", PWD, "李思远", "2023010511", "13900000009", "lisy@campus.edu", "/uploads/seed/avatar/03.jpg", "男", None, 1, "东校区", 110, 1),
    (10, "buyer02", PWD, "黄雨桐", "2023020525", "13900000010", "huangyt@campus.edu", "/uploads/seed/avatar/05.jpg", "女", None, 2, "西校区", 100, 1),
    (11, "buyer03", PWD, "马骁", "2023030533", "13900000011", "maxiao@campus.edu", "/uploads/seed/avatar/07.jpg", "男", None, 3, "北校区", 88, 1),
    (12, "buyer04", PWD, "邓佳怡", "2023040541", "13900000012", "dengjy@campus.edu", "/uploads/seed/avatar/06.jpg", "女", None, 1, "东校区", 132, 1),
    (13, "buyer05", PWD, "吴桐", "2023050549", "13900000013", "wutong@campus.edu", "/uploads/seed/avatar/04.jpg", "男", None, 2, "西校区", 96, 0),
    (14, "operator02", PWD, "钱立运营", "OP2026002", "13900000014", "qianli@campus.edu", "/uploads/seed/avatar/02.jpg", "男", 2, 2, "西校区", 100, 1),
]
parts.append("\n-- 用户：默认账号 admin/operator/user，密码统一 123456（BCrypt）\n" + insert(
    "sys_user", ["id", "username", "password", "real_name", "student_no", "phone", "email", "avatar",
                 "gender", "dept_id", "campus_id", "campus_name", "credit_score", "status"], users))

user_roles = []
for uid in (1,):
    user_roles.append((uid, 1))
for uid in (2, 14):
    user_roles.append((uid, 2))
user_roles += [(3, 4), (4, 3), (5, 3), (6, 3), (7, 3), (8, 3)]
user_roles += [(9, 4), (10, 4), (11, 4), (12, 4), (13, 4)]
parts.append("\n-- 用户角色\n" + insert("sys_user_role", ["user_id", "role_id"], user_roles))

# ---------------------------------------------------------------- 菜单
# (id, parent, name, path, component, perm, type, icon, sort)
menus = [
    (1, 0, "运营中心", None, None, None, "catalog", "dash", 1),
    (2, 1, "运营首页", "/dashboard/index", "Dashboard", "dashboard:view", "menu", "dash", 1),
    (3, 1, "商品审核台", "/admin/audit", "GoodsAudit", "goods:audit", "menu", "check", 2),
    (4, 1, "商品管理", "/admin/goods", "Goods", "goods:list", "menu", "box", 3),
    (5, 1, "订单管理", "/admin/orders", "Orders", "order:list", "menu", "order", 4),
    (6, 1, "评价管理", "/admin/reviews", "Reviews", "review:list", "menu", "star", 5),
    (7, 1, "举报与纠纷", "/admin/reports", "Reports", "report:list", "menu", "alert", 6),
    (8, 1, "信用分流水", "/admin/credit", "CreditLogs", "credit:list", "menu", "medal", 7),
    (9, 1, "定价评分记录", "/admin/price", "PriceCalcs", "price:list", "menu", "chart", 8),
    (10, 1, "运营统计", "/admin/analysis", "Analysis", "analysis:view", "menu", "chart", 9),
    (11, 0, "基础配置", None, None, None, "catalog", "gear", 2),
    (12, 11, "分类管理", "/admin/categories", "Categories", "category:list", "menu", "tag", 1),
    (13, 11, "校区面交点", "/admin/campus", "Campus", "campus:list", "menu", "pin", 2),
    (14, 11, "定价规则配置", "/admin/rules", "Rules", "rule:list", "menu", "rule", 3),
    (15, 11, "公告管理", "/admin/notices", "Notices", "notice:list", "menu", "bell", 4),
    (16, 0, "系统管理", None, None, None, "catalog", "gear", 3),
    (17, 16, "用户管理", "/admin/users", "Users", "user:list", "menu", "user", 1),
    (18, 16, "角色管理", "/admin/roles", "Roles", "role:list", "menu", "shield", 2),
    (19, 16, "菜单管理", "/admin/menus", "Menus", "menu:list", "menu", "list", 3),
    (20, 16, "字典管理", "/admin/dicts", "Dicts", "dict:list", "menu", "book", 4),
    (21, 16, "操作日志", "/admin/logs", "Logs", "log:list", "menu", "doc", 5),
    (22, 16, "定价参数", "/admin/params", "Params", "param:list", "menu", "gear", 6),
    (23, 0, "消息与个人", None, None, None, "catalog", "bell", 4),
    (24, 23, "消息中心", "/admin/messages", "Messages", "message:list", "menu", "bell", 1),
    (25, 23, "个人信息", "/admin/profile", "Profile", "profile:view", "menu", "user", 2),
]
parts.append("\n-- 菜单（目录/菜单二级，按钮权限单独授权）\n" + insert(
    "sys_menu", ["id", "parent_id", "menu_name", "path", "component", "perm", "menu_type", "icon", "sort"], menus))
buttons = [
    (26, 3, "商品审核通过", None, None, "goods:audit:pass", "button", None, 1),
    (27, 3, "商品驳回", None, None, "goods:audit:reject", "button", None, 2),
    (28, 7, "受理举报", None, None, "report:handle", "button", None, 1),
    (29, 8, "调整信用分", None, None, "credit:adjust", "button", None, 1),
    (30, 9, "人工修正评分", None, None, "price:correct", "button", None, 1),
    (31, 4, "下架商品", None, None, "goods:offshelf", "button", None, 1),
    (32, 15, "发布公告", None, None, "notice:add", "button", None, 1),
]
parts.append(insert("sys_menu", ["id", "parent_id", "menu_name", "path", "component", "perm", "menu_type", "icon", "sort"], buttons))

role_menus = []
for mid in [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25] + list(range(26, 33)):
    role_menus.append((1, mid))
for mid in [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 23, 24, 25, 26, 27, 28, 30, 31, 32]:
    role_menus.append((2, mid))
for mid in [2, 3, 4, 5, 9, 12, 23, 24, 25]:
    role_menus.append((3, mid))
for mid in [2, 4, 5, 7, 9, 23, 24, 25, 28]:
    role_menus.append((4, mid))
parts.append("\n-- 角色菜单授权\n" + insert("sys_role_menu", ["role_id", "menu_id"], role_menus))

# ---------------------------------------------------------------- 字典
dict_types = [
    ("商品状态", "goods_status", 1, "待审核/在售/已下架/已售出/审核驳回/冻结"),
    ("订单状态", "order_status", 1, "交易订单状态机"),
    ("商品成色", "condition_level", 1, "成色等级"),
    ("交易方式", "trade_type", 1, "面交/自提/可送"),
    ("支付方式", "pay_type", 1, "线下支付方式记录"),
    ("举报类型", "report_reason", 1, "举报原因分类"),
    ("举报状态", "report_status", 1, "举报处理状态"),
    ("评价状态", "review_status", 1, "评价审核状态"),
    ("信用变动原因", "credit_reason", 1, "信用分变动来源"),
    ("定价等级", "price_grade", 1, "A/B/C 三级"),
    ("申诉状态", "appeal_status", 1, "被举报人申诉"),
    ("校区", "campus", 1, "三个校区"),
    ("审核时限", "audit_limit", 1, "参数配置展示"),
]
parts.append("\n-- 字典类型\n" + insert("sys_dict_type", ["dict_name", "dict_type", "status", "remark"], dict_types))
dict_data = [
    ("goods_status", "待审核", "待审核", 1), ("goods_status", "在售", "在售", 2),
    ("goods_status", "已下架", "已下架", 3), ("goods_status", "已售出", "已售出", 4),
    ("goods_status", "审核驳回", "审核驳回", 5), ("goods_status", "冻结", "冻结", 6),
    ("order_status", "待卖家确认", "待卖家确认", 1), ("order_status", "待面交", "待面交", 2),
    ("order_status", "已完成", "已完成", 3), ("order_status", "已取消", "已取消", 4),
    ("order_status", "纠纷中", "纠纷中", 5),
    ("condition_level", "全新", "全新", 1), ("condition_level", "九成新", "九成新", 2),
    ("condition_level", "七成新", "七成新", 3), ("condition_level", "五成新", "五成新", 4),
    ("condition_level", "有瑕疵", "有瑕疵", 5),
    ("trade_type", "面交", "面交", 1), ("trade_type", "自提", "自提", 2), ("trade_type", "可送", "可送", 3),
    ("pay_type", "现金", "现金", 1), ("pay_type", "转账", "转账", 2), ("pay_type", "其他", "其他", 3),
    ("report_reason", "违禁品", "违禁品", 1), ("report_reason", "虚假描述", "虚假描述", 2),
    ("report_reason", "诈骗", "诈骗", 3), ("report_reason", "骚扰", "骚扰", 4),
    ("report_reason", "校外人员", "校外人员", 5),
    ("report_status", "待受理", "待受理", 1), ("report_status", "处理中", "处理中", 2),
    ("report_status", "已处理", "已处理", 3), ("report_status", "已驳回", "已驳回", 4),
    ("report_status", "申诉中", "申诉中", 5),
    ("review_status", "待审核", "待审核", 1), ("review_status", "已展示", "已展示", 2),
    ("review_status", "已隐藏", "已隐藏", 3),
    ("credit_reason", "交易好评", "交易好评", 1), ("credit_reason", "差评", "差评", 2),
    ("credit_reason", "放鸽子", "放鸽子", 3), ("credit_reason", "违规下架", "违规下架", 4),
    ("credit_reason", "中评", "中评", 5), ("credit_reason", "人工调整", "人工调整", 6),
    ("price_grade", "A 定价合理", "A", 1), ("price_grade", "B 略偏高", "B", 2),
    ("price_grade", "C 偏离市场", "C", 3),
    ("appeal_status", "无", "无", 1), ("appeal_status", "申诉中", "申诉中", 2),
    ("appeal_status", "申诉通过", "申诉通过", 3), ("appeal_status", "申诉驳回", "申诉驳回", 4),
    ("campus", "东校区", "1", 1), ("campus", "西校区", "2", 2), ("campus", "北校区", "3", 3),
    ("audit_limit", "审核时限（小时）", "48", 1), ("audit_limit", "订单锁定时长（小时）", "24", 2),
]
parts.append(insert("sys_dict_data", ["dict_type", "dict_label", "dict_value", "sort"], dict_data))

# ---------------------------------------------------------------- 商品
goods_src = [
    ("九成新 iPad Air 4 64G 蓝色", "ELEC_PHONE", 2350, 3999, "九成新", "2025 年入学买的，用了一年少，屏幕无划痕，电池健康 92%，配原装充电头和数据线，可当面验机。"),
    ("红米 K70 12+256 白色 保修内", "ELEC_PHONE", 1299, 2199, "九成新", "换了新手机出掉，屏幕无磕碰，边框有一处轻微氧化，功能全部正常，保修到明年三月。"),
    ("罗技 G102 二代 有线鼠标", "ELEC_PC", 55, 129, "七成新", "用了一个学期，左键有轻微使用痕迹，滚轮和按键都正常，适合宿舍打游戏写作业用。"),
    ("机械革命 32G 内存条 DDR4 3200", "ELEC_PC", 260, 399, "全新", "买错型号了，全新未拆封，3200 频率单条 32G，笔记本通用，可现场验货。"),
    ("索尼 WH-1000XM4 头戴降噪耳机", "ELEC_AUDIO", 899, 1899, "九成新", "降噪效果很好，耳罩无掉皮，附带原装收纳包和音频线，考研自习室神器。"),
    ("小米蓝牙音箱 便携款", "ELEC_AUDIO", 45, 99, "七成新", "宿舍用了一段时间，音质不错，充电正常，外壳有些许使用痕迹，不影响使用。"),
    ("高数教材 同济第七版 上下册", "BOOK_TEXT", 25, 89, "七成新", "有少量笔记和划线，重点章节标注完整，适合期末复习用，两本一起出。"),
    ("数据结构与算法分析 C语言版", "BOOK_TEXT", 18, 59, "九成新", "只翻过前面几章，几乎全新，无笔记，计算机专业考研常用教材。"),
    ("考研英语黄皮书 真题全套", "BOOK_EXAM", 60, 168, "七成新", "2025 版真题，做题痕迹较多，答案册干净，适合二刷使用，共四本。"),
    ("教资考试 中学综合素质教材", "BOOK_EXAM", 22, 68, "九成新", "备考用过一遍，重点已用荧光笔标注，配套网课已过期不影响看书。"),
    ("三毛 撒哈拉的故事 正版", "BOOK_NOVEL", 12, 39, "九成新", "正版书籍，无折页无笔记，看完一遍出掉，书脊完好。"),
    ("宿舍可折叠收纳箱 大号两只", "LIFE_DORM", 30, 78, "九成新", "搬宿舍买的，用过一学期，折叠无破损，可装衣物和被褥，两只一起出。"),
    ("宿舍小风扇 静音落地款", "LIFE_APPL", 38, 119, "七成新", "三档风速，静音效果可以，夏天宿舍必备，扇叶已清洁。"),
    ("美的电热水壶 1.5L", "LIFE_APPL", 45, 139, "九成新", "宿舍烧水用，无结垢，自动断电正常，含原装底座。"),
    ("捷安特 ATX 山地自行车 26寸", "LIFE_BIKE", 480, 1580, "七成新", "骑了两年，刚换过刹车片和链条，变速顺畅，带 U 型锁，仅限校内面交自提。"),
    ("折叠自行车 20寸 通勤款", "LIFE_BIKE", 210, 599, "九成新", "折叠方便放宿舍阳台，轮胎新换，骑行无异常，附赠车灯。"),
    ("哑铃一对 可调节 15kg", "SPORT_FIT", 85, 260, "九成新", "宿舍健身用的，铸铁片无锈，锁扣正常，太重不方便寄，只能自提。"),
    ("瑜伽垫 加厚防滑 183x61", "SPORT_FIT", 28, 89, "全新", "买多了一条，全新未拆，加厚 10mm，防滑面，附绑带。"),
    ("羽毛球拍 尤尼克斯 双拍套装", "SPORT_BALL", 120, 380, "七成新", "打了一学期，拍框无变形，已重新拉线 24 磅，附拍包和三个球。"),
    ("斯伯丁 7号篮球 室外款", "SPORT_BALL", 65, 189, "七成新", "室外场地打的，表皮有磨损但不影响手感，气足。"),
    ("户外露营帐篷 双人自动", "SPORT_OUT", 150, 429, "九成新", "去过两次露营，无破损无漏水，自动开合，收纳袋齐全。"),
    ("登山杖 铝合金 一对", "SPORT_OUT", 40, 128, "九成新", "可伸缩，锁扣牢固，爬山用过两次，握把干净。"),
    ("优衣库 男士羽绒服 M码 黑色", "CLOTH_TOP", 130, 499, "九成新", "冬天买的，穿过一季，无破损无污渍，已干洗，M 码偏宽松。"),
    ("匡威帆布鞋 42码 白色", "CLOTH_SHOE", 95, 369, "七成新", "穿了一学期，鞋底有磨损，已刷洗干净，无开胶。"),
    ("考研数学 复习全书 全套", "BOOK_EXAM", 55, 149, "九成新", "全套三本，做题主要在草稿纸上，书内笔记较少，适合基础阶段。"),
    ("机械键盘 87键 青轴", "ELEC_PC", 105, 299, "九成新", "宿舍用了一年，键帽无打油，灯效正常，青轴声音偏大已换静音轴故出。"),
    ("台灯 护眼可调光 LED", "LIFE_APPL", 42, 129, "九成新", "三档色温，无极调光，宿舍书桌用，附原装电源适配器。"),
    ("画架 实木折叠 含画板", "STUDY_DRAW", 78, 220, "七成新", "美院课程用过，木架稳固，画板有铅笔痕迹，可擦拭。"),
    ("桌面收纳架 三层 白色", "STUDY_DESK", 25, 79, "九成新", "宿舍桌面收纳，组装简单，无变形，螺丝齐全。"),
    ("吉他 41寸 民谣 入门款", "STUDY_DRAW", 260, 680, "七成新", "自学半年，琴颈无变形，刚换琴弦，附琴包、变调夹和拨片。"),
    ("充电宝 20000mAh 双向快充", "ELEC_OTHER", 58, 169, "九成新", "容量真实，双向快充正常，外壳有轻微划痕，附原装线。"),
    ("显示器 24寸 1080P IPS", "ELEC_PC", 320, 799, "九成新", "外接笔记本用的，无坏点无亮点，附 HDMI 线和支架。"),
    ("AirPods 二代 有线充电盒", "ELEC_AUDIO", 320, 999, "九成新", "左右耳电池都正常，续航约三小时，充电盒有轻微划痕，已消毒。"),
    ("线性代数 同济第六版", "BOOK_TEXT", 15, 42, "七成新", "课上用过的教材，习题部分有铅笔演算，可擦除，配套答案册齐全。"),
    ("床上小桌板 可折叠", "LIFE_DORM", 22, 69, "九成新", "宿舍床上用，桌面无划痕，折叠支架牢固。"),
    ("跳绳 钢丝竞速款", "SPORT_FIT", 12, 39, "九成新", "买来减脂用的，跳了不到十次，钢丝无打结，附收纳袋。"),
    ("双肩包 电脑仓 15.6寸", "CLOTH_TOP", 68, 219, "七成新", "上课背了一年，拉链顺滑，肩带无断裂，内衬有轻微磨损。"),
    ("针管笔套装 0.1-0.8 六支", "STUDY_DRAW", 26, 78, "全新", "买重了，全新未拆封，六种粗细，绘图和手绘都能用。"),
    ("投影仪 便携 1080P", "ELEC_OTHER", 420, 1299, "九成新", "宿舍看电影用的，亮度够用，灯泡使用不到 100 小时，附幕布和三脚架。"),
    ("运动水壶 750ml 保温", "SPORT_OUT", 32, 98, "九成新", "保温效果正常，杯盖无渗漏，内胆已清洗。"),
]
# 分类编码 -> id
code2id = {r[2]: r[0] for r in rows}

# ---------------------------------------------------------------- 商品实拍图
# uploads/seed/goods 下共 21 张实拍图，按“图与商品语义相符”的原则逐条指定主图，
# 保证同一行数据不会出现「标题写耳机、配图是自行车」这种明显穿帮。
# 实际图号对应的物品（生成后按文件名字典序重命名，已逐张核对）：
# 01 头戴耳机 02 双肩包 03 保温水壶 04 电热水壶 05 电煮锅 06 鼠标 07 显示器
# 08 笔记本电脑 09 小风扇 10 平板 11 吉他 12 机械键盘 13 书册 14 瑜伽垫
# 15 台灯 16 山地自行车 17 智能手机 18 露营帐篷 19 收纳箱 20 羽毛球拍 21 笔记本册
N_IMG = 21
IMG_MAP = [
    10, 17, 6, 8, 1, 8, 13, 21, 13, 21,    # 1-10
    13, 19, 9, 4, 16, 18, 20, 14, 20, 16,  # 11-20
    18, 16, 19, 2, 13, 12, 15, 21, 19, 11,  # 21-30
    17, 7, 1, 13, 19, 20, 2, 21, 7, 3,     # 31-40
]

# 语义簇：同一件商品的多张实拍图只从这个簇里取，保证详情图与标题不打架
IMG_CLUSTER = [
    [1, 6, 7, 8, 9, 10, 12, 17],   # 数码：耳机、鼠标、显示器、笔记本、风扇、平板、键盘、手机
    [3, 4, 5, 15, 19],             # 生活：保温壶、电热水壶、电煮锅、台灯、收纳箱
    [13, 21],                      # 图书文具：书册、笔记本册
    [14, 16, 20],                  # 运动：瑜伽垫、自行车、球拍
    [18, 16, 20],                  # 户外：帐篷、自行车、球拍
    [2, 19],                       # 箱包收纳：双肩包、收纳箱
    [11],                          # 乐器：吉他
]


def detail_imgs(cover_no):
    """详情图取主图 + 同一语义簇里的其他图。
    早先按固定步长 5 偏移取图，会出现「画架」详情里混进电煮锅和平板的情况，
    审核台的「图文对照」一眼就能看出穿帮，所以改成按语义簇挑选，簇内不足 4 张就有几张放几张。"""
    cluster = next((v for v in IMG_CLUSTER if cover_no in v), [cover_no])
    picks = [cover_no]
    for n in cluster:
        if len(picks) >= 4:
            break
        if n != cover_no:
            picks.append(n)
    return ",".join(f"/uploads/seed/goods/{n:02d}.jpg" for n in picks)
# 40 条商品：16 在售 / 16 已售出 / 4 待审核 / 2 已下架 / 1 驳回 / 1 冻结，覆盖文档要求的全部状态
status_plan = (["在售"] * 16) + (["已售出"] * 16) + (["待审核"] * 4) + (["已下架"] * 2) + (["审核驳回"] * 1) + (["冻结"] * 1)
random.shuffle(status_plan)
sellers = [4, 5, 6, 7, 8]
campus_of_seller = {4: (1, "东校区"), 5: (2, "西校区"), 6: (3, "北校区"), 7: (1, "东校区"), 8: (2, "西校区")}
place_of_campus = {1: [1, 2, 3], 2: [4, 5], 3: [7, 8]}
grows = []
for i, (title, cat, price, orig, cond, desc) in enumerate(goods_src):
    seller = sellers[i % 5]
    camp_id, camp_name = campus_of_seller[seller]
    pl = place_of_campus[camp_id][i % len(place_of_campus[camp_id])]
    img_n = IMG_MAP[i]
    imgs = detail_imgs(img_n)
    st = status_plan[i]
    if i % 8 == 2:
        # 今天发布的这几件固定为在售，门户「今日上新」区块才有内容
        st = "在售"
    remark = "图片含联系方式水印，请重新上传实拍图" if st == "审核驳回" else None
    if i % 7 == 5:
        remark = "描述与实物不符，疑似批量转卖，请补充细节图"
    grow = dict(
        no=f"XY{(260401 + i):06d}{i+1:04d}",
        seller=seller, cat=code2id[cat], title=title, desc=desc, cond=cond,
        price=price, orig=orig, camp_id=camp_id, camp_name=camp_name, place=pl,
        trade=["面交", "面交", "自提", "可送"][i % 4], st=st, remark=remark,
        view=random.randint(12, 860), fav=random.randint(0, 46),
        cover=f"/uploads/seed/goods/{img_n:02d}.jpg", imgs=imgs,
        days=random.randint(1, 172),
    )
    # 每 8 条留一条发布在今天，运营首页「今日新增」和 30 天趋势图才有数据
    grow["create"] = (f"2026-11-01 {9 + (i % 10):02d}:{(i * 13) % 60:02d}:00" if i % 8 == 2
                      else ts(grow["days"]))
    grows.append(grow)

parts.append("\n-- 闲置商品：30 条，覆盖待审核/在售/已下架/已售出/审核驳回/冻结六种状态\n" + insert(
    "goods", ["goods_no", "seller_id", "category_id", "title", "description", "condition_level", "price",
              "original_price", "campus_id", "campus_name", "trade_place_id", "trade_type", "status",
              "audit_remark", "view_count", "favorite_count", "cover_url", "image_urls", "create_time"],
    [(g["no"], g["seller"], g["cat"], g["title"], g["desc"], g["cond"], g["price"], g["orig"], g["camp_id"],
      g["camp_name"], g["place"], g["trade"], g["st"], g["remark"], g["view"], g["fav"], g["cover"],
      g["imgs"], g["create"]) for g in grows]))

# ---------------------------------------------------------------- 订单
# 已售出商品对应已完成订单；在售商品派生待卖家确认/待面交/已取消/纠纷中
sold = [i for i, g in enumerate(grows) if g["st"] == "已售出"]
onsale = [i for i, g in enumerate(grows) if g["st"] == "在售"]
buyers = [3, 9, 10, 11, 12]
orders = []
oid = 0


def mk_order(gi, buyer_id, status, days, deal=None, cancel=None):
    global oid
    oid += 1
    g = grows[gi]
    d = days
    ap = ts(max(0, d - random.randint(1, 3)), random.randint(9, 20), random.choice([0, 30]))
    seller_ok = ts(max(0, d - 1)) if status in ("待面交", "已完成", "纠纷中") else None
    buyer_ok = ts(max(0, d - 2)) if status == "已完成" else None
    return dict(
        no=f"DD{(260401 + d % 170):06d}{oid:06d}", gid=gi + 1, gtitle=g["title"], gcover=g["cover"],
        buyer=buyer_id, seller=g["seller"], deal=deal if deal is not None else g["price"],
        pay=["现金", "转账", "现金", "其他"][oid % 4], appoint=ap, place=f"{g['camp_name']}·面交点{((oid % 3) + 1)}",
        st=status, cancel=cancel, buyer_ok=buyer_ok, seller_ok=seller_ok, create=ts(d),
        lock=ts(max(0, d - 1)),
    )


# 已完成 16 单：每个已售出商品对应一单，形成「商品已售出 ↔ 订单已完成」的闭环
# 前 8 单落在最近 4 天，保证运营首页「本周成交」与近 30 天趋势有数据
for k, gi in enumerate(sold):
    d = (k % 4) + 1 if k < 8 else random.randint(8, 165)
    orders.append(mk_order(gi, buyers[k % 5], "已完成", d,
                           deal=grows[gi]["price"] - random.choice([0, 0, 5, 10, 20])))
# 待面交 6 单：商品处于锁定状态（订单 lock_expire_time 未到）
for k in range(6):
    orders.append(mk_order(onsale[k], buyers[k % 5], "待面交", random.randint(1, 20)))
# 待卖家确认 5 单：卖家尚未接单。创建时间必须落在 12 小时锁定期内，
# 否则会被后端的超时自动取消定时任务扫走，导致这个状态在演示时消失
for k in range(6, 11):
    o = mk_order(onsale[k], buyers[(k + 2) % 5], "待卖家确认", 0)
    # 固定落在今天 15:00-19:00 之间，距 12 小时锁定期上限还有余量，不会被定时任务立刻回收
    o["create"] = f"2026-11-01 {15 + (k - 6):02d}:{(k * 11) % 60:02d}:00"
    o["lock"] = "2026-11-02 03:30:00"
    orders.append(o)
# 已取消 8 单：取消后商品释放回在售，因此允许同一商品多次下单再取消
cancels = ["临时有事，改天再约", "和卖家协商后取消", "面交时间冲突，重新约", "商品与描述不符，取消",
           "临时决定不买了", "卖家超时未确认，系统自动取消", "面交点临时关闭，改约失败", "重复下单，取消多余订单"]
for k in range(8):
    src = onsale[11 + (k % 5)]
    orders.append(mk_order(src, buyers[(k + 1) % 5], "已取消", random.randint(2, 40), cancel=cancels[k]))
# 纠纷中 2 单：冻结商品与订单，等待运营调解
for k in range(2):
    orders.append(mk_order(onsale[13 + k], buyers[(k + 3) % 5], "纠纷中", random.randint(3, 30),
                           cancel="面交未见到卖家，申请纠纷调解"))

parts.append("\n-- 交易订单：32 条，覆盖待卖家确认/待面交/已完成/已取消/纠纷中五种状态\n" + insert(
    "trade_order", ["order_no", "goods_id", "goods_title", "goods_cover", "buyer_id", "seller_id", "deal_price",
                    "pay_type", "appoint_time", "appoint_place", "status", "cancel_reason",
                    "buyer_confirm_time", "seller_confirm_time", "lock_expire_time", "create_time"],
    [(o["no"], o["gid"], o["gtitle"], o["gcover"], o["buyer"], o["seller"], o["deal"], o["pay"], o["appoint"],
      o["place"], o["st"], o["cancel"], o["buyer_ok"], o["seller_ok"], o["lock"], o["create"]) for o in orders]))

# ---------------------------------------------------------------- 评价（双盲：部分只有一方提交）
done = [(i + 1, o) for i, o in enumerate(orders) if o["st"] == "已完成"]
reviews = []
tags_pool = ["描述一致,准时", "准时,好沟通", "描述一致,好沟通,包装完好", "准时", "好沟通"]
for k, (ord_id, o) in enumerate(done):
    score = [5, 5, 4, 5, 3, 5, 4, 5][k % 8]
    reviews.append((ord_id, o["buyer"], o["seller"], score, tags_pool[k % len(tags_pool)],
                    ["东西和描述一样，卖家很准时", "面交很顺利，物品成色不错", "沟通很愉快，推荐",
                     "价格实在，下次还找他", "有点小瑕疵但卖家提前说了"][k % 5],
                    k % 4 == 0, "已展示" if k % 6 != 5 else "待审核", o["create"]))
    if k % 3 != 2:
        reviews.append((ord_id, o["seller"], o["buyer"], [5, 4, 5, 5][k % 4], "准时,好沟通",
                        ["买家很爽快，当面确认后立刻付款", "沟通顺畅，交易愉快", "很守时，谢谢支持"][k % 3],
                        1 if k % 5 == 0 else 0, "已展示", o["create"]))
parts.append("\n-- 交易评价：完成订单的双向互评，部分仅有单方提交用于验证双盲\n" + insert(
    "review", ["order_id", "from_user_id", "to_user_id", "score", "tags", "content", "anonymous", "status", "create_time"],
    reviews))

# ---------------------------------------------------------------- 举报
report_rows = []
rs = [("商品", "违禁品", "疑似出售违规电器，宿舍不允许使用大功率电热设备", "待受理", "无", 0),
      ("商品", "虚假描述", "标题写九成新，实际边框有明显磕碰，图片是网图", "处理中", "无", 1),
      ("商品", "诈骗", "要求先转账后发货，不肯当面交易，感觉是骗子", "已处理", "无", 2),
      ("订单", "骚扰", "下单后多次在非面交时间联系，要求送到宿舍楼下", "已处理", "申诉中", 3),
      ("商品", "校外人员", "卖家自称校外人员，长期批量转卖二手手机", "已处理", "申诉通过", 4),
      ("订单", "虚假描述", "面交时商品与描述严重不符，拒绝退款", "待受理", "无", 5),
      ("商品", "违禁品", "出售考试作弊器材，影响考试公平", "已处理", "无", 6),
      ("商品", "虚假描述", "标价与详情页价格不一致，引导私下加价", "已驳回", "无", 7),
      ("订单", "诈骗", "付款后卖家失联，订单一直不确认", "处理中", "无", 8),
      ("用户", "骚扰", "对方在评价里公开我的手机号", "已处理", "申诉驳回", 9),
      ("商品", "校外人员", "商品图片带商铺水印，疑似商家冒充学生", "待受理", "无", 10),
      ("订单", "虚假描述", "到手发现缺少配件，卖家称已说明但页面没写", "已处理", "无", 11)]
for k, (bt, reason, desc, st, appeal, gi) in enumerate(rs):
    handler = 2 or 14
    report_rows.append((
        f"JB{(260501 + k % 6):06d}{k+1:04d}", bt, (gi + 1) if bt == "商品" else (k + 1),
        grows[gi]["title"] if bt == "商品" else f"订单 DD{(260401 + k):06d}{k+1:06d}",
        buyers[k % 5] if k % 2 == 0 else [9, 10, 11, 12][k % 4], reason, desc,
        f"{grows[gi]['cover']},{detail_imgs(IMG_MAP[gi]).split(',')[1]}",
        st, (2 if st != "待受理" else None), ("林晓运营" if st != "待受理" else None),
        ("已按平台规则处理：核实后下架商品并对卖家扣 10 分" if st == "已处理" else
         ("证据不足，驳回举报" if st == "已驳回" else ("已受理，正在联系双方核实" if st == "处理中" else None))),
        appeal, ts(random.randint(2, 60)),
    ))
parts.append("\n-- 举报与纠纷：12 条，覆盖待受理/处理中/已处理/已驳回与申诉\n" + insert(
    "report", ["report_no", "biz_type", "biz_id", "biz_title", "reporter_id", "reason_type", "description",
               "evidence_urls", "status", "handler_id", "handler_name", "handle_result", "appeal_status", "create_time"],
    report_rows))

# ---------------------------------------------------------------- 信用分流水（按时间累加还原分值）
credit_rows = []
score_now = {u: 100 for u in range(3, 14)}
for uid in range(3, 14):
    target = [104, 128, 116, 92, 78, 46, 110, 100, 88, 132, 96][uid - 3]
    events = []
    remaining = target - 100
    for k in range(random.randint(2, 5)):
        delta = random.choice([2, 2, -5, 10, -10, -15, 2])
        events.append((delta, ["交易好评", "差评", "放鸽子", "违规下架", "手动调整"][
            0 if delta == 2 else (1 if delta == -5 else (2 if delta == -10 else (3 if delta == -15 else 4)))]))
    events.append((remaining - sum(e[0] for e in events), "人工调整"))
    cur = 100
    for delta, reason in events:
        after = max(0, min(150, cur + delta))
        credit_rows.append((uid, after - cur, cur, after, reason,
                            f"DD{(260401 + random.randint(0, 150)):06d}{random.randint(1, 999999):06d}",
                            "运营人工校准信用分" if reason == "人工调整" else None,
                            2 if reason == "人工调整" else None,
                            ts(random.randint(1, 170))))
        cur = after
parts.append("\n-- 信用分流水：变动前后值连续，可回溯到 sys_user.credit_score\n" + insert(
    "credit_log", ["user_id", "change_value", "before_score", "after_score", "reason_type", "biz_no",
                   "remark", "operator_id", "create_time"], credit_rows))

# ---------------------------------------------------------------- 定价规则与评分记录
rules = [
    ("商品定价成交概率评分规则", "v1", 30, 20, 15, 15, 10, 10, "停用",
     ts(120), "初版权重，同分类成交样本不足时惩罚过重，已停用"),
    ("商品定价成交概率评分规则", "v2", 30, 20, 15, 15, 10, 10, "启用",
     ts(30), "调整供给系数与热度口径，样本不足改为不计分并在结果页提示"),
]
parts.append("\n-- 定价规则：v1 停用、v2 启用（权重合计 100%）\n" + insert(
    "price_rule", ["rule_name", "rule_version", "weight_category", "weight_condition", "weight_supply", "weight_heat",
                   "weight_credit", "weight_info", "status", "effect_time", "remark"], rules))

cond_factor = {"全新": 1.0, "九成新": 0.88, "七成新": 0.72, "五成新": 0.55, "有瑕疵": 0.4}
calc_rows = []
for i, g in enumerate(grows[:32]):
    base = g["price"]
    sc_cat = round(min(100, max(20, 100 - abs(g["price"] - base * 1.05) / max(base, 1) * 120)), 2)
    sc_cond = round({"全新": 100, "九成新": 88, "七成新": 72, "五成新": 55, "有瑕疵": 40}[g["cond"]], 2)
    sc_supply = round(min(100, 55 + (i * 7) % 40), 2)
    sc_heat = round(min(100, 30 + (g["view"] % 60)), 2)
    sc_credit = round(min(100, [100, 128, 116, 92, 78, 46][g["seller"] - 3] / 1.5), 2)
    sc_info = round(70 + (i % 4) * 10, 2)
    total = round(sc_cat * .3 + sc_cond * .2 + sc_supply * .15 + sc_heat * .15 + sc_credit * .1 + sc_info * .1, 2)
    grade = "A" if total >= 80 else ("B" if total >= 60 else "C")
    mid = base * cond_factor[g["cond"]]
    low, high = round(mid * 0.92, 2), round(mid * 1.12, 2)
    concl = {"A": "定价合理，预计7天内成交，建议保持价格并补充实拍图",
             "B": "略高于同类成交区间，建议下调5%至10%或补充描述",
             "C": "明显偏离市场或信息不足，建议降价15%以上并补齐成色、入手价和图片"}[grade]
    snap = ('{"categoryId":%d,"condition":"%s","campusId":%d,"viewCount":%d,"favoriteCount":%d,'
            '"creditScore":%d,"imageCount":4,"descLength":%d,"originalPrice":%d,"price":%d}') % (
        g["cat"], g["cond"], g["camp_id"], g["view"], g["fav"],
        [100, 128, 116, 92, 78, 46][g["seller"] - 3], len(g["desc"]), g["orig"], g["price"])
    corrected = "B" if grade == "A" and i % 7 == 3 else None
    calc_rows.append((i + 1, 2, "v2", snap, sc_cat, sc_cond, sc_supply, sc_heat, sc_credit, sc_info, total, grade,
                      low, high, concl,
                      "同分类成交价、成色、校区供给、浏览转化、卖家信用、图文完整度六项加权",
                      "同分类近30天成交样本 3 条，样本偏少，价格项参考性有限" if i % 5 == 0 else None,
                      corrected, "人工复核：该商品为开学季刚需，实际成交价高于模型建议" if corrected else None,
                      2 if corrected else None, ts(random.randint(1, 20)) if corrected else None,
                      1 if i % 3 == 0 else 0, ts(random.randint(0, 25))))
parts.append("\n-- 定价评分记录：32 条，含输入快照、六项得分、总分与人工修正留痕\n" + insert(
    "price_calc", ["goods_id", "rule_id", "rule_version", "snapshot", "score_category", "score_condition",
                   "score_supply", "score_heat", "score_credit", "score_info", "total_score", "grade",
                   "suggest_low", "suggest_high", "conclusion", "factors", "missing",
                   "corrected_grade", "correct_reason", "correct_by", "correct_time", "adopted", "create_time"],
    calc_rows))

# ---------------------------------------------------------------- 公告、消息、日志、文件
notices = [
    ("关于春季学期二手交易专场面交点调整的通知", "为配合校园活动，4 月 12 日至 4 月 15 日东校区图书馆一层大厅面交点暂停使用，请改约一食堂门口广场，带来不便敬请谅解。", "平台公告", ts(9, 10, 0), "林晓运营", "已发布"),
    ("二手交易防诈骗提示：一律当面验货", "近期出现要求先转账后发货的案例，平台不提供在线支付，也不支持预付定金。请务必当面验货、当面交付，遇到可疑账号请立即举报。", "交易提醒", ts(22, 9, 30), "系统管理员", "已发布"),
    ("商品发布规范更新：实拍图不少于 2 张", "自本月起，发布闲置需上传不少于 2 张实拍图，禁止使用网图或带商铺水印的图片，违规商品将被驳回并计入信用记录。", "规则变更", ts(45, 15, 0), "林晓运营", "已发布"),
    ("毕业季跳蚤市场线上专区开放", "毕业季专区已开放，教材、宿舍用品、代步工具集中展示，支持按楼栋筛选，欢迎毕业生发布闲置。", "平台公告", ts(70, 11, 0), "钱立运营", "已发布"),
]
parts.append("\n-- 公告\n" + insert(
    "sys_notice", ["title", "content", "notice_type", "publish_time", "publisher", "status"], notices))

msgs = []
for k in range(14):
    uid = [4, 5, 9, 10, 3, 6][k % 6]
    msgs.append((uid, ["商品审核通过", "订单待确认提醒", "举报处理结果", "信用分变动提醒", "评价邀请"][k % 5]
                 + f"（{k+1}）",
                 ["你发布的商品已通过审核，现已上架展示", "有买家下单，请在12小时内确认接单，超时订单将自动取消",
                  "你提交的举报已处理完成，处理结果可在举报记录中查看", "你的信用分发生变动，可在我的信用中查看明细",
                  "订单已完成，请在7天内完成评价"][k % 5],
                 ["goods", "order", "report", "credit", "order"][k % 5], (k % 30) + 1,
                 ["审核", "订单", "纠纷", "信用", "系统通知"][k % 5], k % 3 == 0, ts(random.randint(0, 40))))
parts.append("\n-- 站内消息\n" + insert(
    "sys_message", ["receiver_id", "title", "content", "biz_type", "biz_id", "msg_type", "read_flag", "create_time"], msgs))

logs = []
for k in range(18):
    logs.append((["商品审核", "举报处理", "信用调整", "规则配置", "用户管理", "订单管理"][k % 6],
                 ["审核通过", "审核驳回", "受理", "人工调整", "修改权重", "冻结账号"][k % 6],
                 f"XY{(260401 + k % 30):06d}{k+1:04d}",
                 ["通过商品审核并上架", "驳回商品：图片含联系方式水印", "受理举报并冻结商品",
                  "信用分人工调整 +5，原因：核实后补偿", "调整定价规则权重并生成 v2 版本",
                  "冻结账号：校外人员批量转卖"][k % 6],
                 [1, 2, 2, 2, 1, 1][k % 6], ["系统管理员", "林晓运营", "林晓运营", "钱立运营", "系统管理员", "系统管理员"][k % 6],
                 ts(random.randint(0, 60)), "127.0.0.1"))
parts.append("\n-- 操作日志：审核/冻结/信用调整/改权重等关键写操作留痕\n" + insert(
    "sys_oper_log", ["module", "oper_type", "biz_no", "content", "oper_id", "oper_name", "oper_time", "ip"], logs))

files = []
for k in range(10):
    n = IMG_MAP[k]
    files.append((f"goods_{n:02d}.jpg", f"/uploads/seed/goods/{n:02d}.jpg",
                  random.randint(120, 380) * 1024, "jpg", "goods", k + 1, sellers[k % 5], ts(random.randint(5, 100))))
parts.append("\n-- 文件元数据\n" + insert(
    "sys_file", ["file_name", "file_path", "file_size", "file_type", "biz_type", "biz_id", "uploader_id", "create_time"], files))

parts.append("\nSET FOREIGN_KEY_CHECKS = 1;\n")
parts.append(f"-- 数据规模：商品 {len(grows)} 条 / 订单 {len(orders)} 条 / 评价 {len(reviews)} 条 / "
             f"举报 {len(report_rows)} 条 / 信用流水 {len(credit_rows)} 条 / 评分记录 {len(calc_rows)} 条\n")

os.makedirs(os.path.dirname(OUT), exist_ok=True)
with open(OUT, "w", encoding="utf-8") as f:
    f.write("\n".join(parts))
print("written", OUT, os.path.getsize(OUT), "bytes")
