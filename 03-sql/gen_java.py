#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""从 sql/init.sql 解析表结构，生成 MyBatis-Plus 实体与 Mapper。

实体与库表一一对应，避免手写字段名偏差；标准字段（create_by…version）一并生成。
"""
import os
import re

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DDL = os.path.join(BASE, "sql", "init.sql")
PKG = os.path.join(BASE, "backend/src/main/java/com/grad/secondhand")
os.makedirs(os.path.join(PKG, "entity"), exist_ok=True)
os.makedirs(os.path.join(PKG, "mapper"), exist_ok=True)

TYPE_MAP = [
    ("bigint", "Long"), ("int", "Integer"), ("tinyint", "Integer"), ("smallint", "Integer"),
    ("decimal", "BigDecimal"), ("double", "Double"), ("varchar", "String"), ("text", "String"),
    ("datetime", "String"), ("date", "String"), ("time", "String"), ("char", "String"),
]


def java_type(sql_type):
    low = sql_type.lower()
    for key, jt in TYPE_MAP:
        if low.startswith(key):
            return jt
    return "String"


def camel(name):
    parts = name.split("_")
    return parts[0] + "".join(p.capitalize() for p in parts[1:])


def cls(name):
    return "".join(p.capitalize() for p in name.split("_"))


def parse():
    sql = open(DDL, encoding="utf-8").read()
    tables = {}
    for m in re.finditer(r"CREATE TABLE `(\w+)` \((.*?)\n\) ENGINE", sql, re.S):
        name, body = m.group(1), m.group(2)
        cols = []
        for line in body.split("\n"):
            line = line.strip().rstrip(",")
            cm = re.match(r"`(\w+)` (\w+(?:\([\d,]+\))?)", line)
            if not cm:
                continue
            col, typ = cm.group(1), cm.group(2)
            comment = ""
            cc = re.search(r"COMMENT '([^']*)'", line)
            if cc:
                comment = cc.group(1)
            cols.append((col, typ, comment))
        tables[name] = cols
    return tables


# 手写补充说明：仅用于类注释
TABLE_CN = {
    "goods": "闲置商品", "trade_order": "交易订单", "review": "交易评价", "report": "举报与纠纷",
    "credit_log": "信用分流水", "category": "商品分类", "campus_place": "校区面交点",
    "price_rule": "定价评分规则版本", "price_calc": "定价评分记录",
    "sys_user": "用户", "sys_role": "角色", "sys_user_role": "用户角色", "sys_menu": "菜单权限",
    "sys_role_menu": "角色菜单", "sys_dict_type": "字典类型", "sys_dict_data": "字典数据",
    "sys_file": "文件元数据", "sys_notice": "公告", "sys_message": "站内消息", "sys_oper_log": "操作日志",
}

tables = parse()
count = 0
for table, cols in tables.items():
    entity = cls(table)
    body = []
    for col, typ, comment in cols:
        jt = java_type(typ)
        field = camel(col)
        ann = "    @TableId(type = IdType.AUTO)\n" if col == "id" else ""
        body.append(f"    /** {comment} */\n{ann}    private {jt} {field};")
    imports = ["import com.baomidou.mybatisplus.annotation.IdType;",
               "import com.baomidou.mybatisplus.annotation.TableId;",
               "import com.baomidou.mybatisplus.annotation.TableName;",
               "import lombok.Data;"]
    if any(java_type(t) == "BigDecimal" for _, t, _ in cols):
        imports.append("\nimport java.math.BigDecimal;")
    src = f"""package com.grad.secondhand.entity;

{chr(10).join(imports)}

/** {TABLE_CN.get(table, table)}（表 {table}） */
@Data
@TableName("{table}")
public class {entity} {{
{chr(10).join(body)}
}}
"""
    open(os.path.join(PKG, "entity", f"{entity}.java"), "w", encoding="utf-8").write(src)
    mapper = f"""package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.{entity};
import org.apache.ibatis.annotations.Mapper;

/** {TABLE_CN.get(table, table)} Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface {entity}Mapper extends BaseMapper<{entity}> {{
}}
"""
    open(os.path.join(PKG, "mapper", f"{entity}Mapper.java"), "w", encoding="utf-8").write(mapper)
    count += 1

print("entities/mappers:", count)
