#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""NEW-051 design 阶段生成器：可点击的 HTML 原型 + 设计系统 + 组件规范 + 图标集。

页面共用 design/assets/app.css，内置假数据，页间可跳转，覆盖 4 个角色的全部主要页面。
"""
import os

BASE = os.path.dirname(os.path.abspath(__file__))
PAGES = os.path.join(BASE, "pages")
os.makedirs(PAGES, exist_ok=True)
os.makedirs(os.path.join(BASE, "assets"), exist_ok=True)

CSS = """/* NEW-051 校园二手物品交易平台 · 设计系统实现（校园清新绿） */
:root{
  --p:#146B5B; --p2:#3E8E7E; --a:#D9A441; --bg:#F5F6F7; --ink:#1F2933; --ink2:#5B6770;
  --ok:#2B7955; --bad:#B02A37; --line:#E4E7EA; --white:#fff; --r:10px;
  --shadow:0 1px 2px rgba(31,41,51,.06),0 8px 24px rgba(31,41,51,.06);
}
*{box-sizing:border-box;margin:0;padding:0}
body{font:14px/1.6 -apple-system,"PingFang SC","Microsoft YaHei",sans-serif;color:var(--ink);background:var(--bg)}
a{color:inherit;text-decoration:none}
.num{font-variant-numeric:tabular-nums}
.mono{font-family:ui-monospace,Menlo,monospace;font-size:12.5px;color:var(--p2)}
/* 后台骨架：220px 侧栏 + 56px 顶栏 */
.shell{display:flex;min-height:100vh}
.side{width:220px;flex:0 0 220px;background:var(--p);color:#E9F3F0;position:sticky;top:0;height:100vh;display:flex;flex-direction:column}
.brand{height:56px;display:flex;align-items:center;gap:9px;padding:0 18px;border-bottom:1px solid rgba(255,255,255,.12);font-size:14px;font-weight:600;letter-spacing:.4px}
.brand span{font-size:10px;opacity:.6;letter-spacing:1.4px;font-weight:400}
.nav{padding:10px 0;overflow:auto;flex:1}
.nav .grp{padding:12px 18px 6px;font-size:11px;opacity:.55;letter-spacing:1px}
.nav a{display:flex;align-items:center;gap:9px;padding:9px 18px;font-size:13px;color:#DCEBE7;border-left:3px solid transparent}
.nav a:hover{background:rgba(255,255,255,.08);color:#fff}
.nav a.on{background:rgba(255,255,255,.14);color:#fff;border-left-color:var(--a);font-weight:600}
.main{flex:1;min-width:0;display:flex;flex-direction:column}
.top{height:56px;background:#fff;border-bottom:1px solid var(--line);display:flex;align-items:center;gap:14px;padding:0 24px;position:sticky;top:0;z-index:9}
.crumb{font-size:12px;color:var(--ink2)}.crumb b{color:var(--ink)}
.sp{flex:1}
.who{display:flex;align-items:center;gap:8px;padding-left:14px;border-left:1px solid var(--line);font-size:12px}
.ava{width:28px;height:28px;border-radius:50%;background:var(--p2);color:#fff;display:grid;place-items:center;font-size:11px}
.content{padding:24px;flex:1}
/* 门户骨架 */
.pshell{min-height:100vh;display:flex;flex-direction:column}
.pnav{height:64px;background:#fff;border-bottom:1px solid var(--line);display:flex;align-items:center;gap:22px;padding:0 32px;position:sticky;top:0;z-index:9}
.pnav .logo{font-weight:700;color:var(--p);font-size:16px;display:flex;align-items:center;gap:8px}
.pnav .lk{font-size:14px;color:var(--ink2);padding:6px 2px}
.pnav .lk.on{color:var(--p);font-weight:600;border-bottom:2px solid var(--p)}
.pnav .search{display:flex;align-items:center;gap:8px;background:var(--bg);border:1px solid var(--line);border-radius:999px;padding:7px 14px;width:340px;color:var(--ink2);font-size:13px}
.pbody{padding:24px 32px;max-width:1280px;margin:0 auto;width:100%}
/* 通用块 */
.page-h{display:flex;align-items:flex-end;gap:14px;margin-bottom:16px}
.page-h h2{font-size:18px;letter-spacing:.2px}
.page-h p{font-size:12.5px;color:var(--ink2);margin-top:3px}
.card{background:#fff;border:1px solid var(--line);border-radius:var(--r);padding:16px;box-shadow:var(--shadow)}
.card+.card{margin-top:16px}
.card h3{font-size:14px;margin-bottom:12px}
.btn{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 14px;border-radius:6px;background:var(--p);color:#fff;font-size:13px;border:1px solid var(--p);cursor:pointer}
.btn.ghost{background:#fff;color:var(--ink);border-color:var(--line)}
.btn.warn{background:#fff;color:var(--bad);border-color:#E3B4B9}
.btn.acc{background:var(--a);border-color:var(--a);color:#3A2A05}
.tag{display:inline-flex;align-items:center;gap:4px;height:22px;padding:0 9px;border-radius:999px;font-size:12px;background:#EEF4F2;color:var(--p)}
.tag.ok{background:#E8F3ED;color:var(--ok)}
.tag.run{background:#E7F1EF;color:var(--p2)}
.tag.warn{background:#FBF2DF;color:#8A6118}
.tag.bad{background:#F9E8EA;color:var(--bad)}
.tag.gray{background:#F0F2F3;color:var(--ink2)}
.grid{display:grid;gap:14px}
.g4{grid-template-columns:repeat(4,1fr)}
.g3{grid-template-columns:repeat(3,1fr)}
.g2{grid-template-columns:repeat(2,1fr)}
.kpi{background:#fff;border:1px solid var(--line);border-radius:var(--r);padding:14px 16px;box-shadow:var(--shadow)}
.kpi small{font-size:12px;color:var(--ink2)}
.kpi b{display:block;font-size:24px;margin-top:4px;font-variant-numeric:tabular-nums;color:var(--p)}
.kpi i{font-style:normal;font-size:11.5px;color:var(--ink2)}
.filter{display:flex;flex-wrap:wrap;gap:10px;align-items:center;background:#fff;border:1px solid var(--line);border-radius:var(--r);padding:12px 14px;margin-bottom:14px}
.filter label{font-size:12.5px;color:var(--ink2);margin-right:6px}
.filter .fi{display:flex;align-items:center}
.inp{height:32px;border:1px solid var(--line);border-radius:6px;padding:0 10px;font-size:13px;color:var(--ink);background:#fff;width:180px}
table{width:100%;border-collapse:collapse;font-size:13px}
th{text-align:left;font-weight:600;color:var(--ink2);font-size:12.5px;background:#FAFBFB;padding:10px 12px;border-bottom:1px solid var(--line);white-space:nowrap}
td{padding:11px 12px;border-bottom:1px solid var(--line);vertical-align:middle}
tbody tr:hover{background:#FBFDFC}
.thumb{width:48px;height:36px;object-fit:cover;border-radius:6px;background:#EEF1F2;display:block}
.prod{display:flex;gap:10px;align-items:center}
.prod b{font-weight:600;font-size:13px;display:block;max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.prod small{color:var(--ink2);font-size:11.5px}
.pager{display:flex;align-items:center;gap:10px;justify-content:flex-end;padding-top:12px;font-size:12.5px;color:var(--ink2)}
.pager .pg{display:inline-grid;place-items:center;min-width:28px;height:28px;border:1px solid var(--line);border-radius:6px;background:#fff}
.pager .pg.on{background:var(--p);color:#fff;border-color:var(--p)}
/* 商品卡片 */
.pgrid{display:grid;grid-template-columns:repeat(4,1fr);gap:16px}
.pcard{background:#fff;border:1px solid var(--line);border-radius:var(--r);overflow:hidden;box-shadow:var(--shadow)}
.pcard img{width:100%;aspect-ratio:4/3;object-fit:cover;display:block;background:#EDF1F0}
.pcard .pd{padding:10px 12px 12px}
.pcard .pt{font-size:13.5px;line-height:1.45;height:40px;overflow:hidden}
.pcard .pp{color:var(--bad);font-size:16px;font-weight:700;margin-top:6px}
.pcard .pp s{color:var(--ink2);font-size:11.5px;font-weight:400;margin-left:6px}
.pcard .pm{display:flex;align-items:center;gap:6px;margin-top:8px;font-size:11.5px;color:var(--ink2)}
.sect{margin:22px 0 10px;display:flex;align-items:center;gap:10px}
.sect h3{font-size:15px}
.sect .more{margin-left:auto;font-size:12.5px;color:var(--p2)}
.notice{display:flex;gap:10px;align-items:center;background:#FBF7EC;border:1px solid #EFE3C6;color:#6B5218;border-radius:var(--r);padding:10px 14px;font-size:12.5px;margin-bottom:16px}
.foot{padding:22px 32px;color:var(--ink2);font-size:12px;text-align:center;border-top:1px solid var(--line);background:#fff}
.split{display:grid;grid-template-columns:1.6fr 1fr;gap:16px}
.gallery{display:grid;grid-template-columns:repeat(3,1fr);gap:8px}
.gallery img{width:100%;aspect-ratio:4/3;object-fit:cover;border-radius:8px;background:#EDF1F0}
.steps{display:flex;gap:8px;align-items:center;margin-bottom:14px;font-size:12.5px;color:var(--ink2)}
.steps b{color:var(--p)}
.progress{height:8px;background:#EDF1F0;border-radius:999px;overflow:hidden}
.progress i{display:block;height:100%;background:var(--p2)}
"""

NAV_ADMIN = [
    ("运营中心", [("运营首页", "dashboard.html"), ("商品审核台", "audit.html"), ("商品管理", "goods.html"),
                  ("订单管理", "orders.html"), ("评价管理", "reviews.html"), ("举报与纠纷", "reports.html"),
                  ("信用分流水", "credit.html"), ("定价评分记录", "price.html"), ("运营统计", "analysis.html")]),
    ("基础配置", [("分类管理", "categories.html"), ("校区面交点", "campus.html"), ("定价规则配置", "rules.html"),
                  ("公告管理", "notices.html")]),
    ("系统管理", [("用户管理", "users.html"), ("角色管理", "roles.html"), ("菜单管理", "menus.html"),
                  ("字典管理", "dicts.html")]),
    ("消息与个人", [("消息中心", "messages.html"), ("个人信息", "profile.html")]),
]
NAV_OPERATOR = [n for n in NAV_ADMIN if n[0] in ("运营中心", "基础配置", "消息与个人")]
NAV_PORTAL = [("首页", "portal-home.html"), ("逛闲置", "search.html"), ("发布闲置", "publish.html"),
              ("我的闲置", "mygoods.html"), ("我的订单", "myorders.html"), ("我的信用", "mycredit.html"),
              ("我的评价", "myreviews.html"), ("消息", "messages.html")]

GOODS = [
    ("九成新 iPad Air 4 64G 蓝色", 2350, 3999, "九成新", "东校区", "在售", 1),
    ("索尼 WH-1000XM4 头戴降噪耳机", 899, 1899, "九成新", "东校区", "在售", 2),
    ("捷安特 ATX 山地自行车 26寸", 480, 1580, "七成新", "东校区", "在售", 3),
    ("考研英语黄皮书 真题全套", 60, 168, "七成新", "西校区", "待审核", 4),
    ("机械键盘 87键 青轴", 105, 299, "九成新", "西校区", "在售", 5),
    ("户外露营帐篷 双人自动", 150, 429, "九成新", "北校区", "待审核", 6),
    ("优衣库 男士羽绒服 M码 黑色", 130, 499, "九成新", "东校区", "已售出", 7),
    ("瑜伽垫 加厚防滑 183x61", 28, 89, "全新", "西校区", "在售", 8),
    ("罗技 G102 二代 有线鼠标", 55, 129, "七成新", "东校区", "已下架", 9),
    ("吉他 41寸 民谣 入门款", 260, 680, "七成新", "北校区", "审核驳回", 10),
    ("显示器 24寸 1080P IPS", 320, 799, "九成新", "东校区", "在售", 11),
    ("宿舍小风扇 静音落地款", 38, 119, "七成新", "西校区", "冻结", 12),
]
TAG = {"在售": "ok", "待审核": "warn", "已售出": "gray", "已下架": "gray", "审核驳回": "bad", "冻结": "bad"}


def admin_shell(title, body, active, nav=NAV_ADMIN, role="平台管理员"):
    links = []
    for grp, items in nav:
        links.append(f'<div class="grp">{grp}</div>')
        for name, href in items:
            cls = " class=\"on\"" if href == active else ""
            links.append(f'<a href="{href}"{cls}>{name}</a>')
    return f"""<!DOCTYPE html>
<html lang="zh-CN"><head><meta charset="utf-8"><title>{title} · 拾光集</title>
<link rel="stylesheet" href="{'../assets/app.css' if active != 'index.html' else 'assets/app.css'}"></head>
<body><div class="shell">
<aside class="side"><div class="brand">拾光集<span>SECOND HAND</span></div><nav class="nav">{''.join(links)}</nav></aside>
<div class="main"><header class="top"><div class="crumb">校园二手 / <b>{title}</b></div><div class="sp"></div>
<a href="messages.html" style="font-size:12.5px;color:var(--ink2)">消息 <span class="tag bad">3</span></a>
<div class="who"><div class="ava">林</div><div>{role}<br><small>东校区 · 校园运营</small></div></div></header>
<section class="content">{body}</section></div></div></body></html>"""


def portal_shell(title, body, active):
    links = "".join(
        f'<a class="lk{" on" if href == active else ""}" href="{href}">{name}</a>' for name, href in NAV_PORTAL)
    return f"""<!DOCTYPE html>
<html lang="zh-CN"><head><meta charset="utf-8"><title>{title} · 拾光集</title>
<link rel="stylesheet" href="{'../assets/app.css' if active != 'index.html' else 'assets/app.css'}"></head>
<body><div class="pshell">
<header class="pnav"><div class="logo">拾光集<span style="font-weight:400;font-size:11px;color:var(--ink2)">校园闲置</span></div>
{links}<div class="sp"></div><div class="search">搜索教材、数码、代步工具…</div>
<div class="who"><div class="ava">陈</div><div>陈同学<br><small>东校区 · 信用 104</small></div></div></header>
<div class="pbody">{body}</div>
<footer class="foot">拾光集 · 校园二手物品交易平台 ｜ 仅限本校学生，面交验货，不接入在线支付</footer></div></body></html>"""


def rows_goods(items):
    out = []
    for t, price, orig, cond, campus, st, n in items:
        out.append(f"""<tr><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/{n:02d}.jpg" alt="">
<div><b>{t}</b><small>XY2026{n:04d} · {cond}</small></div></div></td>
<td>{campus}</td><td class="num">¥{price}</td><td><s style="color:var(--ink2)">¥{orig}</s></td>
<td><span class="tag {TAG[st]}">{st}</span></td><td class="num">2026-0{(n % 6) + 4}-1{n % 9} 10:2{n % 6}</td>
<td><a style="color:var(--p2)" href="goods-detail.html">查看</a> ｜ <a style="color:var(--p2)" href="audit.html">审核</a></td></tr>""")
    return "".join(out)


PAGE_BODIES = {}

# ---------------------------------------------------------------- 运营首页
PAGE_BODIES["dashboard.html"] = ("运营首页", """
<div class="page-h"><div><h2>运营首页</h2><p>2026-11-01 20:00 更新 ｜ 数据范围：全部校区</p></div><div class="sp"></div>
<span class="btn ghost">近 7 天</span><span class="btn ghost">近 30 天</span><span class="btn">本学期</span></div>
<div class="grid g4">
<div class="kpi"><small>今日新增商品</small><b>12</b><i>较昨日 +3</i></div>
<div class="kpi"><small>待审核商品</small><b>4</b><i>最长等待 9 小时</i></div>
<div class="kpi"><small>在售商品</small><b>16</b><i>覆盖 3 个校区</i></div>
<div class="kpi"><small>本周成交</small><b>9</b><i>成交率 22.5%</i></div>
<div class="kpi"><small>平均成交价</small><b>¥368</b><i>环比 -4.2%</i></div>
<div class="kpi"><small>纠纷率</small><b>5.4%</b><i>2 单待调解</i></div>
<div class="kpi"><small>活跃卖家</small><b>5</b><i>近 7 天有发布</i></div>
<div class="kpi"><small>定价建议采纳率</small><b>38%</b><i>人工修正 3 单</i></div>
</div>
<div class="grid g2" style="margin-top:16px">
<div class="card"><h3>近 30 天发布量与成交量</h3>
<svg viewBox="0 0 520 180" width="100%" height="170"><g stroke="#E4E7EA"><line x1="40" y1="20" x2="500" y2="20"/><line x1="40" y1="70" x2="500" y2="70"/><line x1="40" y1="120" x2="500" y2="120"/><line x1="40" y1="155" x2="500" y2="155"/></g>
<polyline fill="none" stroke="#146B5B" stroke-width="2" points="40,120 90,105 140,112 190,88 240,95 290,72 340,80 390,60 440,66 490,48"/>
<polyline fill="none" stroke="#D9A441" stroke-width="2" stroke-dasharray="5 4" points="40,140 90,132 140,136 190,124 240,128 290,116 340,120 390,108 440,112 490,100"/>
<text x="40" y="172" font-size="10" fill="#5B6770">10-02</text><text x="240" y="172" font-size="10" fill="#5B6770">10-17</text><text x="440" y="172" font-size="10" fill="#5B6770">11-01</text></svg>
<div style="font-size:12px;color:var(--ink2)">— 发布量 ｜ ┄ 成交量</div></div>
<div class="card"><h3>分类供给分布</h3>
<svg viewBox="0 0 200 200" width="100%" height="176"><circle cx="100" cy="100" r="70" fill="none" stroke="#146B5B" stroke-width="26" stroke-dasharray="120 320"/>
<circle cx="100" cy="100" r="70" fill="none" stroke="#3E8E7E" stroke-width="26" stroke-dasharray="90 350" stroke-dashoffset="-120"/>
<circle cx="100" cy="100" r="70" fill="none" stroke="#D9A441" stroke-width="26" stroke-dasharray="70 370" stroke-dashoffset="-210"/>
<circle cx="100" cy="100" r="70" fill="none" stroke="#B9C2C6" stroke-width="26" stroke-dasharray="60 380" stroke-dashoffset="-280"/>
<text x="100" y="96" text-anchor="middle" font-size="13" fill="#1F2933">在售 16</text><text x="100" y="114" text-anchor="middle" font-size="11" fill="#5B6770">3 校区</text></svg></div>
</div>
<div class="card"><h3>发布 → 审核 → 下单 → 成交 漏斗</h3>
<div style="display:grid;gap:8px">
<div class="progress"><i style="width:100%"></i></div><div style="font-size:12px;color:var(--ink2)">发布 40</div>
<div class="progress"><i style="width:78%"></i></div><div style="font-size:12px;color:var(--ink2)">审核通过 31</div>
<div class="progress"><i style="width:52%"></i></div><div style="font-size:12px;color:var(--ink2)">产生订单 21</div>
<div class="progress"><i style="width:40%"></i></div><div style="font-size:12px;color:var(--ink2)">完成成交 16</div></div>
<p style="margin-top:10px;font-size:12px;color:var(--ink2)">供大于求提示：图书教材在售 9 件、近 30 天成交 2 件，建议做专题清理。</p></div>
""")

# ---------------------------------------------------------------- 审核台
PAGE_BODIES["audit.html"] = ("商品审核台", """
<div class="page-h"><div><h2>商品审核台</h2><p>待审核 4 件 ｜ 按违禁品、虚假描述、图片合规三项检查，驳回需选原因并填写不少于 5 字说明</p></div></div>
<div class="split">
<div class="card"><h3>待审核队列</h3>
<table><tbody>
<tr><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/04.jpg"><div><b>考研英语黄皮书 真题全套</b><small>苏明玉 · 西校区 · 图书教材</small></div></div></td><td><span class="tag warn">待审核</span></td></tr>
<tr><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/06.jpg"><div><b>户外露营帐篷 双人自动</b><small>何嘉禾 · 北校区 · 户外露营</small></div></div></td><td><span class="tag warn">待审核</span></td></tr>
<tr><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/13.jpg"><div><b>宿舍可折叠收纳箱 大号两只</b><small>周子航 · 东校区 · 宿舍用品</small></div></div></td><td><span class="tag warn">待审核</span></td></tr>
<tr><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/15.jpg"><div><b>捷安特 ATX 山地自行车</b><small>许安然 · 西校区 · 代步工具</small></div></div></td><td><span class="tag warn">待审核</span></td></tr>
</tbody></table></div>
<div class="card"><h3>图文对照</h3>
<div class="gallery"><img src="../uploads/seed/goods/04.jpg"><img src="../uploads/seed/goods/05.jpg"><img src="../uploads/seed/goods/06.jpg"></div>
<p style="margin-top:12px;font-size:13px">考研英语黄皮书 真题全套 ｜ ¥60 ｜ 入手价 ¥168 ｜ 折价率 64%</p>
<p style="font-size:12.5px;color:var(--ink2);margin-top:6px">2025 版真题，做题痕迹较多，答案册干净，适合二刷使用，共四本。实拍图 3 张，描述 42 字。</p>
<div style="display:flex;gap:8px;margin-top:16px"><span class="btn">通过并上架</span><span class="btn warn">驳回</span><span class="btn ghost">冻结商品</span></div>
<p style="margin-top:12px;font-size:12px;color:var(--ink2)">命中敏感词：无 ｜ 同标题重复度：12% ｜ 卖家 24 小时发布：1 件</p></div>
</div>
""")

# ---------------------------------------------------------------- 商品管理
PAGE_BODIES["goods.html"] = ("商品管理", f"""
<div class="page-h"><div><h2>商品管理</h2><p>共 40 件，覆盖待审核/在售/已下架/已售出/审核驳回/冻结六种状态</p></div>
<div class="sp"></div><span class="btn ghost">导出当前筛选</span><span class="btn">新建商品</span></div>
<div class="filter"><div class="fi"><label>关键词</label><input class="inp" placeholder="标题或编号"></div>
<div class="fi"><label>状态</label><input class="inp" value="全部"></div>
<div class="fi"><label>分类</label><input class="inp" value="全部"></div>
<div class="fi"><label>校区</label><input class="inp" style="width:150px" value="全部"></div>
<div class="sp"></div><span class="btn">查询</span><span class="btn ghost">重置</span></div>
<div class="card"><table><thead><tr><th>商品</th><th>校区</th><th>售价</th><th>入手价</th><th>状态</th><th>发布时间</th><th>操作</th></tr></thead>
<tbody>{rows_goods(GOODS)}</tbody></table>
<div class="pager"><span>共 40 条</span><span class="pg">1</span><span class="pg on">2</span><span class="pg">3</span><span class="pg">›</span><select class="inp" style="width:110px"><option>10 条/页</option></select></div></div>
""")

# ---------------------------------------------------------------- 订单
orders = [
    ("DD260401000001", "九成新 iPad Air 4 64G 蓝色", "李思远", "周子航", 2350, "待面交", 1),
    ("DD260401000002", "索尼 WH-1000XM4 降噪耳机", "黄雨桐", "周子航", 890, "待卖家确认", 2),
    ("DD260401000003", "捷安特 ATX 山地自行车", "马骁", "周子航", 470, "已完成", 3),
    ("DD260401000004", "机械键盘 87键 青轴", "邓佳怡", "苏明玉", 105, "已完成", 5),
    ("DD260401000005", "户外露营帐篷 双人自动", "吴桐", "何嘉禾", 150, "已取消", 6),
    ("DD260401000006", "显示器 24寸 1080P IPS", "陈同学", "郑一鸣", 310, "纠纷中", 11),
]
orows = "".join(f"""<tr><td class="mono">{no}</td><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/{img:02d}.jpg"><div><b>{t}</b></div></div></td>
<td>{b}</td><td>{s}</td><td class="num">¥{p}</td><td><span class="tag {'ok' if st == '已完成' else ('warn' if st in ('待卖家确认', '待面交') else ('bad' if st == '纠纷中' else 'gray'))}">{st}</span></td>
<td><a style="color:var(--p2)" href="order-detail.html">详情</a></td></tr>""" for no, t, b, s, p, st, img in orders)
PAGE_BODIES["orders.html"] = ("订单管理", f"""
<div class="page-h"><div><h2>订单管理</h2><p>共 37 单 ｜ 状态机：待卖家确认→待面交→已完成，等待/纠纷分支均留痕</p></div></div>
<div class="filter"><div class="fi"><label>订单号</label><input class="inp" placeholder="DD 开头"></div>
<div class="fi"><label>状态</label><input class="inp" value="全部"></div>
<div class="fi"><label>下单时间</label><input class="inp" style="width:200px" value="2026-05-01 至 2026-11-01"></div>
<div class="sp"></div><span class="btn">查询</span></div>
<div class="card"><table><thead><tr><th>订单号</th><th>商品</th><th>买家</th><th>卖家</th><th>成交价</th><th>状态</th><th>操作</th></tr></thead><tbody>{orows}</tbody></table>
<div class="pager"><span>共 37 条</span><span class="pg on">1</span><span class="pg">2</span><span class="pg">3</span><span class="pg">4</span></div></div>
""")

PAGE_BODIES["order-detail.html"] = ("订单详情", """
<div class="page-h"><div><h2>订单 DD260401000001</h2><p>待面交 ｜ 下单 2026-10-28 14:22 ｜ 商品锁定至 2026-10-29 14:22</p></div><div class="sp"></div>
<span class="btn ghost">打印面交凭证</span></div>
<div class="steps"><b>下单</b> → <b>卖家接单</b> → 面交确认 → 双方评价</div>
<div class="split">
<div class="card"><h3>商品快照</h3>
<div class="prod"><img class="thumb" style="width:88px;height:66px" src="../uploads/seed/goods/01.jpg">
<div><b>九成新 iPad Air 4 64G 蓝色</b><small>成交价 ¥2350 ｜ 支付方式：现金 ｜ 状态：待面交</small></div></div>
<table style="margin-top:14px"><tbody>
<tr><td style="color:var(--ink2)">面交时间</td><td>2026-10-30 15:30</td><td style="color:var(--ink2)">面交点</td><td>东校区·图书馆一层大厅</td></tr>
<tr><td style="color:var(--ink2)">买家</td><td>李思远（信用 110）</td><td style="color:var(--ink2)">卖家</td><td>周子航（信用 128）</td></tr>
<tr><td style="color:var(--ink2)">卖家接单</td><td>2026-10-28 15:04</td><td style="color:var(--ink2)">锁定到期</td><td>2026-10-29 14:22</td></tr>
</tbody></table></div>
<div class="card"><h3>状态流水</h3>
<table><tbody>
<tr><td>2026-10-28 14:22</td><td>买家下单，商品锁定</td></tr>
<tr><td>2026-10-28 15:04</td><td>卖家确认接单，成交价 2350</td></tr>
<tr><td>—</td><td>等待买家确认收货</td></tr>
</tbody></table>
<div style="display:flex;gap:8px;margin-top:14px"><span class="btn">确认收货</span><span class="btn warn">发起纠纷</span></div></div></div>
""")

PAGE_BODIES["reviews.html"] = ("评价管理", """
<div class="page-h"><div><h2>评价管理</h2><p>双盲机制：双方都提交后才互相可见；评价内容经敏感词过滤</p></div></div>
<div class="card"><table><thead><tr><th>订单号</th><th>评价人 → 被评人</th><th>评分</th><th>标签</th><th>内容</th><th>状态</th><th>操作</th></tr></thead><tbody>
<tr><td class="mono">DD260401000003</td><td>马骁 → 周子航</td><td>★★★★★</td><td>描述一致,准时</td><td>东西和描述一样，卖家很准时</td><td><span class="tag ok">已展示</span></td><td>隐藏</td></tr>
<tr><td class="mono">DD260401000003</td><td>周子航 → 马骁</td><td>★★★★☆</td><td>准时,好沟通</td><td>买家很爽快，当面确认后立刻付款</td><td><span class="tag ok">已展示</span></td><td>隐藏</td></tr>
<tr><td class="mono">DD260401000004</td><td>邓佳怡 → 苏明玉</td><td>★★★★★</td><td>包装完好</td><td>键盘手感很好，卖家还送了拔键器</td><td><span class="tag warn">待审核</span></td><td>通过 / 隐藏</td></tr>
<tr><td class="mono">DD260401000007</td><td>匿名用户 → 何嘉禾</td><td>★★★☆☆</td><td>—</td><td>有一点划痕，卖家没提前说</td><td><span class="tag ok">已展示</span></td><td>隐藏</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["reports.html"] = ("举报与纠纷", """
<div class="page-h"><div><h2>举报与纠纷</h2><p>待受理 3 件 ｜ 48 小时内必须受理，处理结果通过站内消息通知双方</p></div></div>
<div class="filter"><div class="fi"><label>状态</label><input class="inp" value="全部"></div>
<div class="fi"><label>举报类型</label><input class="inp" value="全部"></div><div class="sp"></div><span class="btn">查询</span></div>
<div class="card"><table><thead><tr><th>举报编号</th><th>类型</th><th>关联对象</th><th>举报人</th><th>描述</th><th>状态</th><th>申诉</th><th>操作</th></tr></thead><tbody>
<tr><td class="mono">JB260501001</td><td>违禁品</td><td>宿舍小风扇</td><td>李思远</td><td>疑似出售违规电器，宿舍不允许使用大功率电热设备</td><td><span class="tag warn">待受理</span></td><td>无</td><td>受理</td></tr>
<tr><td class="mono">JB260501002</td><td>虚假描述</td><td>吉他 41寸 民谣</td><td>黄雨桐</td><td>标题写九成新，实际边框有明显磕碰，图片是网图</td><td><span class="tag run">处理中</span></td><td>无</td><td>处理</td></tr>
<tr><td class="mono">JB260501003</td><td>诈骗</td><td>订单 DD260401000006</td><td>马骁</td><td>要求先转账后发货，不肯当面交易，感觉是骗子</td><td><span class="tag ok">已处理</span></td><td>无</td><td>查看</td></tr>
<tr><td class="mono">JB260501004</td><td>骚扰</td><td>订单 DD260401000003</td><td>邓佳怡</td><td>下单后多次在非面交时间联系，要求送到宿舍楼下</td><td><span class="tag ok">已处理</span></td><td><span class="tag warn">申诉中</span></td><td>复核</td></tr>
<tr><td class="mono">JB260501005</td><td>校外人员</td><td>AirPods 二代</td><td>吴桐</td><td>卖家自称校外人员，长期批量转卖二手手机</td><td><span class="tag ok">已处理</span></td><td><span class="tag ok">申诉通过</span></td><td>查看</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["credit.html"] = ("信用分流水", """
<div class="page-h"><div><h2>信用分流水</h2><p>初始 100，好评 +2、差评 -5、放鸽子 -10、违规下架 -15，上下限 0–150</p></div>
<div class="sp"></div><span class="btn">人工调整信用分</span></div>
<div class="card"><table><thead><tr><th>用户</th><th>变动</th><th>变动前</th><th>变动后</th><th>原因</th><th>关联单号</th><th>操作人</th><th>时间</th></tr></thead><tbody>
<tr><td>周子航</td><td style="color:var(--ok)">+2</td><td class="num">126</td><td class="num">128</td><td>交易好评</td><td class="mono">DD260401000003</td><td>系统</td><td class="num">2026-10-28 16:02</td></tr>
<tr><td>许安然</td><td style="color:var(--bad)">-10</td><td class="num">56</td><td class="num">46</td><td>放鸽子</td><td class="mono">DD260401000005</td><td>系统</td><td class="num">2026-10-21 19:30</td></tr>
<tr><td>郑一鸣</td><td style="color:var(--bad)">-15</td><td class="num">93</td><td class="num">78</td><td>违规下架</td><td class="mono">JB260501002</td><td>林晓运营</td><td class="num">2026-10-18 11:12</td></tr>
<tr><td>何嘉禾</td><td style="color:var(--ok)">+5</td><td class="num">87</td><td class="num">92</td><td>人工调整</td><td>—</td><td>钱立运营</td><td class="num">2026-10-11 09:40</td></tr>
</tbody></table></div>
<div class="card"><h3>信用分与限制规则</h3>
<p style="font-size:13px;color:var(--ink2)">低于 60 分：同时在线商品限制为 3 件（许安然当前 46 分，在线 2 件）；低于 40 分：禁止发布新商品 30 天。</p></div>
""")

PAGE_BODIES["price.html"] = ("定价评分记录", """
<div class="page-h"><div><h2>定价评分记录</h2><p>32 次计算 ｜ 规则版本 v2 ｜ 每次保留输入快照、六项得分与人工修正痕迹</p></div></div>
<div class="card"><h3>本次计算：九成新 iPad Air 4 64G 蓝色（¥2350）</h3>
<div class="grid g4" style="margin-bottom:14px">
<div class="kpi"><small>成交概率总分</small><b>82.4</b><i>等级 A</i></div>
<div class="kpi"><small>建议价区间</small><b>¥2068–2516</b><i>同分类成交中位数 ¥2400</i></div>
<div class="kpi"><small>规则版本</small><b>v2</b><i>生效 2026-10-02</i></div>
<div class="kpi"><small>是否采纳</small><b>已采纳</b><i>卖家按建议价下调 2%</i></div></div>
<table><thead><tr><th>因素</th><th>权重</th><th>得分</th><th>加权</th><th>依据</th></tr></thead><tbody>
<tr><td>同分类成交价偏离度</td><td>30%</td><td class="num">86.0</td><td class="num">25.8</td><td>近 30 天同分类成交 7 单，中位数 ¥2400</td></tr>
<tr><td>商品成色</td><td>20%</td><td class="num">88.0</td><td class="num">17.6</td><td>九成新，成色系数 0.88</td></tr>
<tr><td>同校同类在售数量</td><td>15%</td><td class="num">62.0</td><td class="num">9.3</td><td>东校区同类在售 5 件，供给偏多</td></tr>
<tr><td>发布时长与浏览转化</td><td>15%</td><td class="num">78.0</td><td class="num">11.7</td><td>发布 56 小时，浏览 412，收藏 23</td></tr>
<tr><td>卖家信用分</td><td>10%</td><td class="num">85.3</td><td class="num">8.5</td><td>信用 128，历史成交 6 单</td></tr>
<tr><td>图文完整度</td><td>10%</td><td class="num">95.0</td><td class="num">9.5</td><td>4 张实拍图，描述 78 字，已填入手价</td></tr>
</tbody></table>
<p style="margin-top:12px;font-size:13px">结论：<b>定价合理，预计 7 天内成交</b>，建议保持价格并补充实拍图</p>
<p style="font-size:12.5px;color:var(--ink2);margin-top:6px">未满足条件：无 ｜ 可改进项：补充背面细节图可提升图文完整度至 100</p>
<div style="display:flex;gap:8px;margin-top:14px"><span class="btn ghost">查看输入快照</span><span class="btn warn">人工修正结果</span></div></div>
<div class="card"><h3>历史计算记录</h3>
<table><thead><tr><th>商品</th><th>规则版本</th><th>总分</th><th>等级</th><th>人工修正</th><th>修正原因</th><th>时间</th></tr></thead><tbody>
<tr><td>九成新 iPad Air 4</td><td>v2</td><td class="num">82.4</td><td><span class="tag ok">A</span></td><td>—</td><td>—</td><td class="num">2026-10-30 10:12</td></tr>
<tr><td>捷安特 ATX 山地自行车</td><td>v2</td><td class="num">74.1</td><td><span class="tag warn">B</span></td><td><span class="tag warn">A</span></td><td>开学季刚需，实际成交价高于模型建议</td><td class="num">2026-10-26 15:40</td></tr>
<tr><td>吉他 41寸 民谣入门款</td><td>v2</td><td class="num">56.8</td><td><span class="tag bad">C</span></td><td>—</td><td>—</td><td class="num">2026-10-22 09:05</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["analysis.html"] = ("运营统计", """
<div class="page-h"><div><h2>运营统计</h2><p>供给与成交按分类、校区、成色对比，识别供大于求的分类</p></div><div class="sp"></div>
<span class="btn ghost">本月</span><span class="btn">本季度</span><span class="btn ghost">本年</span></div>
<div class="grid g2"><div class="card"><h3>各校区成交量</h3>
<svg viewBox="0 0 480 160" width="100%" height="150"><g font-size="11" fill="#5B6770">
<rect x="60" y="20" width="300" height="18" fill="#146B5B"/><text x="370" y="34">东校区 14</text>
<rect x="60" y="60" width="214" height="18" fill="#3E8E7E"/><text x="284" y="74">西校区 10</text>
<rect x="60" y="100" width="128" height="18" fill="#D9A441"/><text x="198" y="114">北校区 6</text></g></svg></div>
<div class="card"><h3>成色与成交价</h3>
<table><thead><tr><th>成色</th><th>在售</th><th>成交</th><th>均价</th></tr></thead><tbody>
<tr><td>全新</td><td class="num">3</td><td class="num">2</td><td class="num">¥276</td></tr>
<tr><td>九成新</td><td class="num">8</td><td class="num">9</td><td class="num">¥542</td></tr>
<tr><td>七成新</td><td class="num">4</td><td class="num">4</td><td class="num">¥193</td></tr>
<tr><td>五成新 / 有瑕疵</td><td class="num">1</td><td class="num">1</td><td class="num">¥86</td></tr>
</tbody></table></div></div>
<div class="card"><h3>举报类型占比</h3>
<table><thead><tr><th>类型</th><th>数量</th><th>占比</th><th>处理情况</th></tr></thead><tbody>
<tr><td>虚假描述</td><td class="num">4</td><td>33%</td><td>3 已处理，1 处理中</td></tr>
<tr><td>诈骗</td><td class="num">2</td><td>17%</td><td>1 已处理，1 处理中</td></tr>
<tr><td>违禁品</td><td class="num">2</td><td>17%</td><td>全部已处理</td></tr>
<tr><td>骚扰 / 校外人员</td><td class="num">4</td><td>33%</td><td>3 已处理，1 待受理</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["categories.html"] = ("分类管理", """
<div class="page-h"><div><h2>分类管理</h2><p>两级分类 ｜ 删除前校验是否被商品引用，停用不影响历史商品展示</p></div><div class="sp"></div><span class="btn">新建分类</span></div>
<div class="card"><table><thead><tr><th>分类名称</th><th>编码</th><th>层级</th><th>排序</th><th>热门</th><th>关联商品</th><th>状态</th><th>操作</th></tr></thead><tbody>
<tr><td>电子产品</td><td class="mono">ELEC</td><td>一级</td><td class="num">1</td><td><span class="tag acc">热门</span></td><td class="num">12</td><td><span class="tag ok">启用</span></td><td>编辑 / 停用</td></tr>
<tr><td>　└ 手机平板</td><td class="mono">ELEC_PHONE</td><td>二级</td><td class="num">10</td><td>—</td><td class="num">4</td><td><span class="tag ok">启用</span></td><td>编辑 / 停用</td></tr>
<tr><td>　└ 电脑配件</td><td class="mono">ELEC_PC</td><td>二级</td><td class="num">10</td><td>—</td><td class="num">3</td><td><span class="tag ok">启用</span></td><td>编辑 / 停用</td></tr>
<tr><td>图书教材</td><td class="mono">BOOK</td><td>一级</td><td class="num">2</td><td><span class="tag acc">热门</span></td><td class="num">9</td><td><span class="tag ok">启用</span></td><td>编辑 / 停用</td></tr>
<tr><td>生活用品</td><td class="mono">LIFE</td><td>一级</td><td class="num">3</td><td><span class="tag acc">热门</span></td><td class="num">8</td><td><span class="tag ok">启用</span></td><td>编辑 / 停用</td></tr>
<tr><td>服饰鞋包</td><td class="mono">CLOTH</td><td>一级</td><td class="num">5</td><td>—</td><td class="num">2</td><td><span class="tag gray">停用</span></td><td>编辑 / 启用</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["campus.html"] = ("校区面交点", """
<div class="page-h"><div><h2>校区面交点</h2><p>停用的面交点不能在新订单中选择；联系人电话仅运营可见，前台脱敏</p></div><div class="sp"></div><span class="btn">新建面交点</span></div>
<div class="card"><table><thead><tr><th>校区</th><th>面交点</th><th>位置说明</th><th>开放时段</th><th>联系人</th><th>电话</th><th>状态</th><th>操作</th></tr></thead><tbody>
<tr><td>东校区</td><td>图书馆一层大厅</td><td>图书馆正门进，服务台右侧休息区</td><td class="num">08:00–21:30</td><td>李文</td><td class="num">138****1001</td><td><span class="tag ok">启用</span></td><td>编辑</td></tr>
<tr><td>东校区</td><td>一食堂门口广场</td><td>一食堂南门，靠近快递驿站</td><td class="num">07:00–20:00</td><td>王倩</td><td class="num">138****1002</td><td><span class="tag ok">启用</span></td><td>编辑</td></tr>
<tr><td>西校区</td><td>体育馆东门</td><td>体育馆东侧入口，雨天改到馆内前厅</td><td class="num">09:00–21:00</td><td>陈晨</td><td class="num">138****1004</td><td><span class="tag ok">启用</span></td><td>编辑</td></tr>
<tr><td>西校区</td><td>实验楼A座大厅</td><td>A 座一层门禁外侧</td><td class="num">08:00–18:00</td><td>—</td><td>—</td><td><span class="tag gray">停用</span></td><td>启用</td></tr>
<tr><td>北校区</td><td>北门警卫室旁</td><td>北门内 20 米，警务室窗口</td><td class="num">07:30–22:00</td><td>孙浩</td><td class="num">138****1006</td><td><span class="tag ok">启用</span></td><td>编辑</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["rules.html"] = ("定价规则配置", """
<div class="page-h"><div><h2>定价规则配置</h2><p>权重合计必须等于 100% ｜ 修改后生成新版本号与生效时间，历史计算不受影响</p></div><div class="sp"></div><span class="btn">保存并生成新版本</span></div>
<div class="split"><div class="card"><h3>当前版本 v2（启用中，生效 2026-10-02 10:00）</h3>
<table><thead><tr><th>因素</th><th>权重</th><th>说明</th></tr></thead><tbody>
<tr><td>同分类成交价偏离度</td><td class="num">30%</td><td>近 30 天同分类已完成订单中位数</td></tr>
<tr><td>商品成色</td><td class="num">20%</td><td>按成色系数折算</td></tr>
<tr><td>同校同类在售数量</td><td class="num">15%</td><td>供给越多得分越低</td></tr>
<tr><td>发布时长与浏览转化</td><td class="num">15%</td><td>发布不足 2 小时不参与</td></tr>
<tr><td>卖家信用分</td><td class="num">10%</td><td>新用户按 100 分</td></tr>
<tr><td>图文完整度</td><td class="num">10%</td><td>图片数、描述字数、是否填入手价</td></tr>
</tbody><tfoot><tr><td><b>合计</b></td><td class="num"><b>100%</b></td><td>校验通过</td></tr></tfoot></table>
<p style="margin-top:12px;font-size:12.5px;color:var(--ink2)">等级阈值：A ≥ 80 ｜ B 60–79 ｜ C &lt; 60。修改后仅影响新计算或主动重算的数据。</p></div>
<div class="card"><h3>版本历史</h3>
<table><thead><tr><th>版本</th><th>状态</th><th>生效时间</th><th>变更说明</th></tr></thead><tbody>
<tr><td class="mono">v2</td><td><span class="tag ok">启用</span></td><td class="num">2026-10-02 10:00</td><td>调整供给系数与热度口径，样本不足改为不计分</td></tr>
<tr><td class="mono">v1</td><td><span class="tag gray">停用</span></td><td class="num">2026-07-04 09:00</td><td>初版权重，样本不足时惩罚过重</td></tr>
</tbody></table></div></div>
""")

PAGE_BODIES["notices.html"] = ("公告管理", """
<div class="page-h"><div><h2>公告管理</h2><p>后台发布、门户展示 ｜ 支持草稿、已发布、已下线</p></div><div class="sp"></div><span class="btn">发布公告</span></div>
<div class="card"><table><thead><tr><th>标题</th><th>类型</th><th>发布人</th><th>发布时间</th><th>状态</th><th>操作</th></tr></thead><tbody>
<tr><td>关于春季学期二手交易专场面交点调整的通知</td><td>平台公告</td><td>林晓运营</td><td class="num">2026-10-23 10:00</td><td><span class="tag ok">已发布</span></td><td>编辑 / 下线</td></tr>
<tr><td>二手交易防诈骗提示：一律当面验货</td><td>交易提醒</td><td>系统管理员</td><td class="num">2026-10-10 09:30</td><td><span class="tag ok">已发布</span></td><td>编辑 / 下线</td></tr>
<tr><td>商品发布规范更新：实拍图不少于 2 张</td><td>规则变更</td><td>林晓运营</td><td class="num">2026-09-17 15:00</td><td><span class="tag ok">已发布</span></td><td>编辑 / 下线</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["users.html"] = ("用户管理", """
<div class="page-h"><div><h2>用户管理</h2><p>14 个账号 ｜ 学生账号通过学号导入并绑定校区 ｜ 密码字段不下发前端</p></div><div class="sp"></div><span class="btn">新增用户</span></div>
<div class="filter"><div class="fi"><label>关键字</label><input class="inp" placeholder="登录名或姓名"></div>
<div class="fi"><label>角色</label><input class="inp" value="全部"></div><div class="sp"></div><span class="btn">查询</span></div>
<div class="card"><table><thead><tr><th>登录名</th><th>姓名</th><th>学号</th><th>校区</th><th>信用分</th><th>角色</th><th>状态</th><th>操作</th></tr></thead><tbody>
<tr><td class="mono">admin</td><td>系统管理员</td><td>—</td><td>东校区</td><td class="num">100</td><td>平台管理员</td><td><span class="tag ok">启用</span></td><td>编辑 / 重置密码</td></tr>
<tr><td class="mono">operator</td><td>林晓运营</td><td>OP2026001</td><td>东校区</td><td class="num">100</td><td>校园运营人员</td><td><span class="tag ok">启用</span></td><td>编辑 / 重置密码</td></tr>
<tr><td class="mono">seller01</td><td>周子航</td><td>2023010203</td><td>东校区</td><td class="num">128</td><td>学生卖家</td><td><span class="tag ok">启用</span></td><td>编辑 / 停用</td></tr>
<tr><td class="mono">seller05</td><td>许安然</td><td>2023050122</td><td>西校区</td><td class="num">46</td><td>学生卖家</td><td><span class="tag ok">启用</span></td><td>编辑 / 停用</td></tr>
<tr><td class="mono">buyer05</td><td>吴桐</td><td>2023050549</td><td>西校区</td><td class="num">96</td><td>学生买家</td><td><span class="tag gray">停用</span></td><td>编辑 / 启用</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["roles.html"] = ("角色管理", """
<div class="page-h"><div><h2>角色管理</h2><p>菜单权限 + 按钮权限 + 数据范围三层控制，前端隐藏按钮不等于授权</p></div><div class="sp"></div><span class="btn">新增角色</span></div>
<div class="card"><table><thead><tr><th>角色名称</th><th>编码</th><th>数据范围</th><th>菜单数</th><th>用户数</th><th>状态</th><th>操作</th></tr></thead><tbody>
<tr><td>平台管理员</td><td class="mono">ROLE_ADMIN</td><td>全部</td><td class="num">32</td><td class="num">1</td><td><span class="tag ok">启用</span></td><td>分配菜单</td></tr>
<tr><td>校园运营人员</td><td class="mono">ROLE_OPERATOR</td><td>本部门</td><td class="num">24</td><td class="num">2</td><td><span class="tag ok">启用</span></td><td>分配菜单</td></tr>
<tr><td>学生卖家</td><td class="mono">ROLE_SELLER</td><td>本人</td><td class="num">9</td><td class="num">5</td><td><span class="tag ok">启用</span></td><td>分配菜单</td></tr>
<tr><td>学生买家</td><td class="mono">ROLE_BUYER</td><td>本人</td><td class="num">9</td><td class="num">5</td><td><span class="tag ok">启用</span></td><td>分配菜单</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["menus.html"] = ("菜单管理", """
<div class="page-h"><div><h2>菜单管理</h2><p>目录 / 菜单 / 按钮三级 ｜ 菜单管理项在分配菜单弹窗中隐藏</p></div><div class="sp"></div><span class="btn">新增菜单</span></div>
<div class="card"><table><thead><tr><th>菜单名称</th><th>类型</th><th>路由</th><th>权限标识</th><th>排序</th><th>可见</th><th>操作</th></tr></thead><tbody>
<tr><td>运营中心</td><td>目录</td><td>—</td><td>—</td><td class="num">1</td><td>显示</td><td>编辑</td></tr>
<tr><td>　└ 商品审核台</td><td>菜单</td><td class="mono">/admin/audit</td><td class="mono">goods:audit</td><td class="num">2</td><td>显示</td><td>编辑 / 删除</td></tr>
<tr><td>　└ 定价评分记录</td><td>菜单</td><td class="mono">/admin/price</td><td class="mono">price:list</td><td class="num">8</td><td>显示</td><td>编辑 / 删除</td></tr>
<tr><td>　└ 商品审核通过</td><td>按钮</td><td>—</td><td class="mono">goods:audit:pass</td><td class="num">1</td><td>—</td><td>编辑 / 删除</td></tr>
<tr><td>基础配置</td><td>目录</td><td>—</td><td>—</td><td class="num">2</td><td>显示</td><td>编辑</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["dicts.html"] = ("字典管理", """
<div class="page-h"><div><h2>字典管理</h2><p>状态与类型枚举统一维护，前后端统一从字典接口取值，禁止各自硬编码</p></div></div>
<div class="split"><div class="card"><h3>字典类型</h3>
<table><thead><tr><th>字典名称</th><th>类型编码</th><th>项数</th><th>状态</th></tr></thead><tbody>
<tr><td>商品状态</td><td class="mono">goods_status</td><td class="num">6</td><td><span class="tag ok">启用</span></td></tr>
<tr><td>订单状态</td><td class="mono">order_status</td><td class="num">5</td><td><span class="tag ok">启用</span></td></tr>
<tr><td>商品成色</td><td class="mono">condition_level</td><td class="num">5</td><td><span class="tag ok">启用</span></td></tr>
<tr><td>举报类型</td><td class="mono">report_reason</td><td class="num">5</td><td><span class="tag ok">启用</span></td></tr>
</tbody></table></div>
<div class="card"><h3>字典项：order_status</h3>
<table><thead><tr><th>标签</th><th>值</th><th>排序</th><th>状态</th></tr></thead><tbody>
<tr><td>待卖家确认</td><td>待卖家确认</td><td class="num">1</td><td><span class="tag ok">启用</span></td></tr>
<tr><td>待面交</td><td>待面交</td><td class="num">2</td><td><span class="tag ok">启用</span></td></tr>
<tr><td>已完成</td><td>已完成</td><td class="num">3</td><td><span class="tag ok">启用</span></td></tr>
<tr><td>已取消</td><td>已取消</td><td class="num">4</td><td><span class="tag ok">启用</span></td></tr>
<tr><td>纠纷中</td><td>纠纷中</td><td class="num">5</td><td><span class="tag ok">启用</span></td></tr>
</tbody></table></div></div>
""")

PAGE_BODIES["messages.html"] = ("消息中心", """
<div class="page-h"><div><h2>消息中心</h2><p>审核、订单、纠纷、信用四类提醒，未读 3 条</p></div><div class="sp"></div><span class="btn ghost">全部标为已读</span></div>
<div class="card"><table><thead><tr><th>类型</th><th>标题</th><th>内容</th><th>时间</th><th>状态</th></tr></thead><tbody>
<tr><td><span class="tag run">审核</span></td><td>商品审核通过</td><td>你发布的商品已通过审核，现已上架展示</td><td class="num">2026-11-01 09:20</td><td><span class="tag bad">未读</span></td></tr>
<tr><td><span class="tag warn">订单</span></td><td>订单待确认提醒</td><td>有买家下单，请在 12 小时内确认接单，超时订单将自动取消</td><td class="num">2026-10-31 18:40</td><td><span class="tag bad">未读</span></td></tr>
<tr><td><span class="tag bad">纠纷</span></td><td>举报处理结果</td><td>你提交的举报已处理完成，处理结果可在举报记录中查看</td><td class="num">2026-10-30 14:05</td><td><span class="tag gray">已读</span></td></tr>
<tr><td><span class="tag ok">信用</span></td><td>信用分变动提醒</td><td>你的信用分发生变动，可在我的信用中查看明细</td><td class="num">2026-10-28 16:02</td><td><span class="tag gray">已读</span></td></tr>
</tbody></table></div>
""")

PAGE_BODIES["profile.html"] = ("个人信息", """
<div class="page-h"><div><h2>个人信息</h2><p>改资料、改密码、上传头像，头像上传后右上角同步显示</p></div></div>
<div class="split"><div class="card"><h3>基本资料</h3>
<table><tbody>
<tr><td style="width:120px;color:var(--ink2)">头像</td><td><div class="prod"><div class="ava" style="width:48px;height:48px;font-size:16px">林</div><span class="btn ghost">上传头像</span></div></td></tr>
<tr><td style="color:var(--ink2)">姓名</td><td><input class="inp" value="林晓运营"></td></tr>
<tr><td style="color:var(--ink2)">手机号</td><td><input class="inp" value="13900000002"></td></tr>
<tr><td style="color:var(--ink2)">所属校区</td><td><input class="inp" value="东校区"></td></tr>
<tr><td style="color:var(--ink2)">角色</td><td>校园运营人员 · 数据范围 本部门</td></tr>
</tbody></table><div style="margin-top:14px"><span class="btn">保存修改</span></div></div>
<div class="card"><h3>修改密码</h3>
<table><tbody>
<tr><td style="width:120px;color:var(--ink2)">原密码</td><td><input class="inp" type="password" value="······"></td></tr>
<tr><td style="color:var(--ink2)">新密码</td><td><input class="inp" type="password" value="······"></td></tr>
<tr><td style="color:var(--ink2)">确认新密码</td><td><input class="inp" type="password" value="······"></td></tr>
</tbody></table><div style="margin-top:14px"><span class="btn">更新密码</span></div></div></div>
""")

# ---------------------------------------------------------------- 门户
PAGE_BODIES["portal-home.html"] = ("首页", """
<div class="notice"><b>公告</b> 4 月 12–15 日东校区图书馆面交点暂停使用，请改约一食堂门口广场</div>
<div class="card" style="display:flex;align-items:center;gap:18px;background:linear-gradient(90deg,#146B5B,#3E8E7E);color:#fff;border:none">
<div><h3 style="font-size:20px;margin-bottom:6px">毕业季闲置专场上线</h3>
<p style="font-size:13px;opacity:.92">教材、宿舍用品、代步工具集中展示，支持按楼栋筛选</p>
<div style="margin-top:12px"><span class="btn acc">去逛逛</span></div></div><div class="sp"></div>
<img src="../uploads/seed/goods/03.jpg" style="width:260px;height:140px;object-fit:cover;border-radius:8px" alt=""></div>
<div class="sect"><h3>今日上新</h3><span class="tag ok">12 件</span><span class="more">查看更多 ›</span></div>
<div class="pgrid">
<div class="pcard"><img src="../uploads/seed/goods/01.jpg"><div class="pd"><div class="pt">九成新 iPad Air 4 64G 蓝色</div><div class="pp">¥2350 <s>¥3999</s></div><div class="pm"><span class="tag ok">九成新</span>东校区 · 图书馆</div></div></div>
<div class="pcard"><img src="../uploads/seed/goods/02.jpg"><div class="pd"><div class="pt">索尼 WH-1000XM4 头戴降噪耳机</div><div class="pp">¥899 <s>¥1899</s></div><div class="pm"><span class="tag ok">九成新</span>东校区 · 一食堂</div></div></div>
<div class="pcard"><img src="../uploads/seed/goods/03.jpg"><div class="pd"><div class="pt">捷安特 ATX 山地自行车 26寸</div><div class="pp">¥480 <s>¥1580</s></div><div class="pm"><span class="tag ok">七成新</span>东校区 · 三号楼</div></div></div>
<div class="pcard"><img src="../uploads/seed/goods/08.jpg"><div class="pd"><div class="pt">瑜伽垫 加厚防滑 183x61</div><div class="pp">¥28 <s>¥89</s></div><div class="pm"><span class="tag ok">全新</span>西校区 · 体育馆</div></div></div>
</div>
<div class="sect"><h3>低价专区</h3><span class="tag warn">50 元以下</span><span class="more">查看更多 ›</span></div>
<div class="pgrid">
<div class="pcard"><img src="../uploads/seed/goods/09.jpg"><div class="pd"><div class="pt">罗技 G102 二代 有线鼠标</div><div class="pp">¥55 <s>¥129</s></div><div class="pm"><span class="tag ok">七成新</span>东校区</div></div></div>
<div class="pcard"><img src="../uploads/seed/goods/12.jpg"><div class="pd"><div class="pt">宿舍可折叠收纳箱 大号两只</div><div class="pp">¥30 <s>¥78</s></div><div class="pm"><span class="tag ok">九成新</span>东校区</div></div></div>
<div class="pcard"><img src="../uploads/seed/goods/07.jpg"><div class="pd"><div class="pt">高数教材 同济第七版 上下册</div><div class="pp">¥25 <s>¥89</s></div><div class="pm"><span class="tag ok">七成新</span>东校区</div></div></div>
<div class="pcard"><img src="../uploads/seed/goods/16.jpg"><div class="pd"><div class="pt">折叠自行车 20寸 通勤款</div><div class="pp">¥210 <s>¥599</s></div><div class="pm"><span class="tag ok">九成新</span>西校区</div></div></div>
</div>
""")

PAGE_BODIES["search.html"] = ("逛闲置", """
<div class="filter"><div class="fi"><label>关键词</label><input class="inp" placeholder="iPad / 教材 / 自行车"></div>
<div class="fi"><label>分类</label><input class="inp" style="width:150px" value="电子产品 / 手机平板"></div>
<div class="fi"><label>价格</label><input class="inp" style="width:90px" value="0"><span style="margin:0 6px">–</span><input class="inp" style="width:90px" value="3000"></div>
<div class="fi"><label>成色</label><input class="inp" style="width:120px" value="全部"></div>
<div class="fi"><label>校区</label><input class="inp" style="width:120px" value="东校区"></div>
<div class="sp"></div><span class="btn">查询</span></div>
<div class="page-h"><div><h2>搜索结果</h2><p>共 6 件 ｜ 排序：<b>最新发布</b> · 价格从低到高 · 信用优先</p></div></div>
<div class="pgrid">
<div class="pcard"><img src="../uploads/seed/goods/01.jpg"><div class="pd"><div class="pt">九成新 <mark>iPad</mark> Air 4 64G 蓝色</div><div class="pp">¥2350 <s>¥3999</s></div><div class="pm"><span class="tag ok">九成新</span>东校区 · 卖家信用 128</div></div></div>
<div class="pcard"><img src="../uploads/seed/goods/11.jpg"><div class="pd"><div class="pt">显示器 24寸 1080P IPS</div><div class="pp">¥320 <s>¥799</s></div><div class="pm"><span class="tag ok">九成新</span>东校区 · 卖家信用 78</div></div></div>
<div class="pcard"><img src="../uploads/seed/goods/05.jpg"><div class="pd"><div class="pt">索尼 WH-1000XM4 头戴降噪耳机</div><div class="pp">¥899 <s>¥1899</s></div><div class="pm"><span class="tag ok">九成新</span>东校区 · 卖家信用 128</div></div></div>
<div class="pcard"><img src="../uploads/seed/goods/09.jpg"><div class="pd"><div class="pt">罗技 G102 二代 有线鼠标</div><div class="pp">¥55 <s>¥129</s></div><div class="pm"><span class="tag ok">七成新</span>东校区 · 卖家信用 128</div></div></div>
</div>
""")

PAGE_BODIES["goods-detail.html"] = ("商品详情", """
<div class="split"><div class="card">
<div class="gallery"><img src="../uploads/seed/goods/01.jpg"><img src="../uploads/seed/goods/02.jpg"><img src="../uploads/seed/goods/03.jpg"><img src="../uploads/seed/goods/04.jpg"><img src="../uploads/seed/goods/05.jpg"><img src="../uploads/seed/goods/06.jpg"></div>
<h3 style="margin-top:16px;font-size:16px">九成新 iPad Air 4 64G 蓝色</h3>
<p style="color:var(--ink2);font-size:13px;margin-top:6px">2025 年入学买的，用了一年少，屏幕无划痕，电池健康 92%，配原装充电头和数据线，可当面验机。</p>
<table style="margin-top:14px"><tbody>
<tr><td style="width:110px;color:var(--ink2)">成色</td><td>九成新</td><td style="color:var(--ink2)">交易方式</td><td>面交</td></tr>
<tr><td style="color:var(--ink2)">入手价</td><td>¥3999（折价率 41%）</td><td style="color:var(--ink2)">校区</td><td>东校区</td></tr>
<tr><td style="color:var(--ink2)">面交点</td><td>图书馆一层大厅 · 08:00–21:30</td><td style="color:var(--ink2)">浏览</td><td>412 次</td></tr>
</tbody></table>
<div class="notice" style="margin-top:14px;margin-bottom:0">交易风险提示：平台不支持在线支付与快递，请当面验货后再付款，勿向陌生人转账。</div></div>
<div><div class="card"><div style="font-size:26px;color:var(--bad);font-weight:700">¥2350 <s style="font-size:13px;color:var(--ink2);font-weight:400">¥3999</s></div>
<p style="font-size:12.5px;color:var(--ink2);margin-top:4px">比同分类成交中位数低 2%</p>
<div style="display:flex;gap:8px;margin-top:14px"><span class="btn" style="flex:1;justify-content:center">立即下单</span><span class="btn ghost">收藏</span></div></div>
<div class="card"><h3>卖家信息</h3><div class="prod"><div class="ava" style="width:40px;height:40px">周</div>
<div><b>周子航</b><small>东校区 · 历史成交 6 单 · 好评率 100%</small></div></div>
<p style="margin-top:10px;font-size:12.5px;color:var(--ink2)">信用分 <b style="color:var(--p)">128</b>（优秀）｜ 近 30 天发布 4 件</p></div>
<div class="card"><h3>相似商品</h3>
<div class="prod" style="margin-bottom:10px"><img class="thumb" src="../uploads/seed/goods/11.jpg"><div><b>显示器 24寸 1080P</b><small>¥320 · 东校区</small></div></div>
<div class="prod"><img class="thumb" src="../uploads/seed/goods/05.jpg"><div><b>索尼 XM4 降噪耳机</b><small>¥899 · 东校区</small></div></div></div></div></div>
""")

PAGE_BODIES["publish.html"] = ("发布闲置", """
<div class="page-h"><div><h2>发布闲置</h2><p>最多 6 张实拍图 ｜ 提交后进入待审核，审核期间不可被下单</p></div></div>
<div class="split"><div class="card"><h3>实拍图（置顶）</h3>
<div class="gallery"><div style="border:1px dashed var(--line);border-radius:8px;aspect-ratio:4/3;display:grid;place-items:center;color:var(--ink2);font-size:12.5px">＋ 上传实拍图</div>
<div style="border:1px dashed var(--line);border-radius:8px;aspect-ratio:4/3;display:grid;place-items:center;color:var(--ink2);font-size:12.5px">＋</div>
<div style="border:1px dashed var(--line);border-radius:8px;aspect-ratio:4/3;display:grid;place-items:center;color:var(--ink2);font-size:12.5px">＋</div></div>
<table style="margin-top:16px"><tbody>
<tr><td style="width:110px;color:var(--ink2)">标题</td><td><input class="inp" style="width:100%" value="九成新 iPad Air 4 64G 蓝色"></td></tr>
<tr><td style="color:var(--ink2)">分类</td><td><input class="inp" style="width:220px" value="电子产品 / 手机平板"></td></tr>
<tr><td style="color:var(--ink2)">成色</td><td><input class="inp" style="width:180px" value="九成新"></td></tr>
<tr><td style="color:var(--ink2)">出售价</td><td><input class="inp" style="width:180px" value="2350"><span style="margin-left:12px;color:var(--ink2);font-size:12.5px">入手价</span> <input class="inp" style="width:180px" value="3999"></td></tr>
<tr><td style="color:var(--ink2)">校区 / 面交点</td><td><input class="inp" style="width:180px" value="东校区"> <input class="inp" style="width:220px;margin-left:8px" value="图书馆一层大厅"></td></tr>
<tr><td style="color:var(--ink2)">商品描述</td><td><textarea class="inp" style="width:100%;height:96px;padding:8px 10px">2025 年入学买的，用了一年少，屏幕无划痕，电池健康 92%，配原装充电头和数据线，可当面验机。</textarea></td></tr>
<tr><td style="color:var(--ink2)">交易方式</td><td><input class="inp" style="width:180px" value="面交"></td></tr>
</tbody></table>
<div class="notice" style="margin-top:14px">提交前请确认：不发布违禁品；图片为本人实拍；描述属实。命中敏感词将进入人工重点审核。</div>
<div style="display:flex;gap:8px"><span class="btn">提交审核</span><span class="btn ghost">保存草稿</span></div></div>
<div><div class="card"><h3>定价参考（创新点）</h3>
<div style="font-size:22px;font-weight:700;color:var(--p)">建议 ¥2068–2516</div>
<p style="font-size:12.5px;color:var(--ink2);margin-top:4px">成交概率 82 分 · 等级 A · 规则 v2</p>
<div class="progress" style="margin-top:12px"><i style="width:82%"></i></div>
<p style="font-size:12.5px;color:var(--ink2);margin-top:10px">主要依据：同分类近 30 天成交 7 单，中位数 ¥2400；成色系数 0.88；东校区同类在售 5 件。</p>
<p style="font-size:12.5px;color:var(--ink2);margin-top:6px">可改进：补充背面与配件实拍图可提升图文完整度。</p></div>
<div class="card"><h3>发布规则</h3>
<p style="font-size:12.5px;color:var(--ink2)">同一卖家 24 小时内最多发布 10 件；标题与描述重复度超过 80% 会提示重复发布并转人工复核。信用分低于 60 时同时在线商品限制 3 件。</p></div></div></div>
""")

PAGE_BODIES["mygoods.html"] = ("我的闲置", """
<div class="page-h"><div><h2>我的闲置</h2><p>在售 4 件 ｜ 待审核 1 件 ｜ 已售出 6 件 ｜ 信用 128 分</p></div><div class="sp"></div><span class="btn">发布闲置</span></div>
<div class="card"><table><thead><tr><th>商品</th><th>售价</th><th>状态</th><th>浏览</th><th>发布时间</th><th>操作</th></tr></thead><tbody>
<tr><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/01.jpg"><div><b>九成新 iPad Air 4 64G 蓝色</b><small>电子产品 / 手机平板</small></div></div></td><td class="num">¥2350</td><td><span class="tag ok">在售</span></td><td class="num">412</td><td class="num">2026-10-28</td><td>编辑 / 下架</td></tr>
<tr><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/02.jpg"><div><b>红米 K70 12+256 白色</b><small>电子产品 / 手机平板</small></div></div></td><td class="num">¥1299</td><td><span class="tag ok">在售</span></td><td class="num">286</td><td class="num">2026-10-24</td><td>编辑 / 下架</td></tr>
<tr><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/13.jpg"><div><b>宿舍可折叠收纳箱 大号两只</b><small>生活用品 / 宿舍用品</small></div></div></td><td class="num">¥30</td><td><span class="tag warn">待审核</span></td><td class="num">0</td><td class="num">2026-11-01</td><td>查看审核进度</td></tr>
<tr><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/07.jpg"><div><b>优衣库 男士羽绒服 M码</b><small>服饰鞋包 / 上装</small></div></div></td><td class="num">¥130</td><td><span class="tag gray">已售出</span></td><td class="num">534</td><td class="num">2026-09-18</td><td>查看订单</td></tr>
<tr><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/09.jpg"><div><b>罗技 G102 二代 有线鼠标</b><small>电子产品 / 电脑配件</small></div></div></td><td class="num">¥55</td><td><span class="tag gray">已下架</span></td><td class="num">97</td><td class="num">2026-08-30</td><td>重新上架 / 删除</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["myorders.html"] = ("我的订单", """
<div class="page-h"><div><h2>我的订单</h2><p>作为买家 3 单 ｜ 作为卖家 5 单 ｜ 待面交请按约定时间到场</p></div></div>
<div class="card"><table><thead><tr><th>订单号</th><th>商品</th><th>对方</th><th>成交价</th><th>面交时间</th><th>状态</th><th>操作</th></tr></thead><tbody>
<tr><td class="mono">DD260401000001</td><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/01.jpg"><div><b>九成新 iPad Air 4</b></div></div></td><td>卖家 周子航</td><td class="num">¥2350</td><td class="num">10-30 15:30</td><td><span class="tag run">待面交</span></td><td><a href="order-detail.html" style="color:var(--p2)">确认收货</a> ｜ 发起纠纷</td></tr>
<tr><td class="mono">DD260401000002</td><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/02.jpg"><div><b>红米 K70 12+256</b></div></div></td><td>卖家 周子航</td><td class="num">¥1290</td><td class="num">11-02 10:00</td><td><span class="tag warn">待卖家确认</span></td><td>取消订单</td></tr>
<tr><td class="mono">DD260401000008</td><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/11.jpg"><div><b>显示器 24寸</b></div></div></td><td>卖家 郑一鸣</td><td class="num">¥310</td><td class="num">10-26 16:00</td><td><span class="tag ok">已完成</span></td><td>评价</td></tr>
<tr><td class="mono">DD260401000011</td><td><div class="prod"><img class="thumb" src="../uploads/seed/goods/15.jpg"><div><b>捷安特 ATX 自行车</b></div></div></td><td>卖家 周子航</td><td class="num">¥470</td><td class="num">10-21 14:00</td><td><span class="tag ok">已完成</span></td><td>已评价</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["mycredit.html"] = ("我的信用", """
<div class="page-h"><div><h2>我的信用</h2><p>信用分决定可同时在线商品数与发布权限</p></div></div>
<div class="grid g4"><div class="kpi"><small>当前信用分</small><b>128</b><i>等级：优秀</i></div>
<div class="kpi"><small>本月变动</small><b>+6</b><i>3 次好评</i></div>
<div class="kpi"><small>历史成交</small><b>6</b><i>好评率 100%</i></div>
<div class="kpi"><small>在线商品上限</small><b>不限</b><i>低于 60 分限 3 件</i></div></div>
<div class="card"><h3>信用分变动明细</h3>
<table><thead><tr><th>时间</th><th>原因</th><th>变动</th><th>变动后</th><th>关联订单</th></tr></thead><tbody>
<tr><td class="num">2026-10-28 16:02</td><td>交易好评</td><td style="color:var(--ok)">+2</td><td class="num">128</td><td class="mono">DD260401000003</td></tr>
<tr><td class="num">2026-10-21 20:10</td><td>交易好评</td><td style="color:var(--ok)">+2</td><td class="num">126</td><td class="mono">DD260401000011</td></tr>
<tr><td class="num">2026-09-30 11:20</td><td>交易好评</td><td style="color:var(--ok)">+2</td><td class="num">124</td><td class="mono">DD260401000009</td></tr>
<tr><td class="num">2026-08-14 09:05</td><td>人工调整</td><td style="color:var(--ok)">+5</td><td class="num">122</td><td>—</td></tr>
</tbody></table></div>
""")

PAGE_BODIES["myreviews.html"] = ("我的评价", """
<div class="page-h"><div><h2>我的评价</h2><p>双方都提交后才互相可见，防止报复性评价 ｜ 完成后 7 天内可评</p></div></div>
<div class="card"><h3>待我评价（1 单）</h3>
<table><thead><tr><th>订单</th><th>商品</th><th>完成时间</th><th>剩余时间</th><th>操作</th></tr></thead><tbody>
<tr><td class="mono">DD260401000008</td><td>显示器 24寸 1080P IPS</td><td class="num">2026-10-26 16:40</td><td>还剩 4 天</td><td><span class="btn">去评价</span></td></tr>
</tbody></table></div>
<div class="card"><h3>我收到的评价</h3>
<table><thead><tr><th>评价人</th><th>评分</th><th>标签</th><th>内容</th><th>时间</th></tr></thead><tbody>
<tr><td>马骁</td><td>★★★★★</td><td>描述一致,准时</td><td>东西和描述一样，卖家很准时</td><td class="num">2026-10-21 20:10</td></tr>
<tr><td>匿名用户</td><td>★★★★☆</td><td>好沟通</td><td>沟通顺畅，包装很用心</td><td class="num">2026-09-30 11:20</td></tr>
</tbody></table></div>
""")

# 生成文件
for name, (title, body) in PAGE_BODIES.items():
    if name.startswith(("portal-home", "search", "publish", "mygoods", "myorders", "mycredit", "myreviews")):
        html = portal_shell(title, body, name)
    else:
        nav = NAV_OPERATOR if name in ("audit.html", "goods.html", "orders.html", "reports.html", "categories.html", "campus.html", "rules.html") else NAV_ADMIN
        html = admin_shell(title, body, name, nav)
    with open(os.path.join(PAGES, name), "w", encoding="utf-8") as f:
        f.write(html)

# 首页总览
cards = []
for grp, items in NAV_ADMIN:
    links = "".join(f'<a href="pages/{h}" style="display:block;padding:8px 12px;border:1px solid var(--line);'
                    f'border-radius:8px;margin:6px 0;font-size:13px">{n}</a>' for n, h in items)
    cards.append(f'<div class="card"><h3>{grp}</h3>{links}</div>')
pcards = "".join(f'<a href="pages/{h}" style="display:block;padding:8px 12px;border:1px solid var(--line);'
                 f'border-radius:8px;margin:6px 0;font-size:13px">{n}</a>' for n, h in NAV_PORTAL)
index = f"""<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8"><title>NEW-051 校园二手物品交易平台 · 设计原型</title>
<link rel="stylesheet" href="assets/app.css"></head><body><div class="pbody">
<div class="page-h"><div><h2>NEW-051 校园二手物品交易平台 · 设计原型</h2>
<p>4 个角色 ｜ {len(PAGE_BODIES)} 个页面 ｜ 校园清新绿 #146B5B ｜ 原型内置假数据、可点击跳转</p></div></div>
<div class="grid g4">{''.join(cards)}<div class="card"><h3>学生门户</h3>{pcards}</div></div>
<div class="card" style="margin-top:16px"><h3>设计系统</h3>
<p style="font-size:13px;color:var(--ink2)">颜色、字体阶梯、间距、圆角与组件尺寸见 <b>design-system.md</b>；
按钮/表单/表格/卡片/标签/弹窗/分页规格见 <b>components.md</b>；图标见 <b>icons.svg</b>。</p></div>
</div></body></html>"""
with open(os.path.join(BASE, "index.html"), "w", encoding="utf-8") as f:
    f.write(index)

# 设计系统与组件规范
with open(os.path.join(BASE, "design-system.md"), "w", encoding="utf-8") as f:
    f.write("""# NEW-051 设计系统 · 校园二手物品交易平台

## 1. 视觉方向
校园清新绿。门户轻量、卡片化，信息密度低、留白多；后台沿用同一主色的管理台布局，
不使用大面积渐变与插画，状态一律「颜色 + 中文标签 + 图标」三重表达。

## 2. 色彩
| 用途 | 色值 | 说明 |
|---|---|---|
| 主色 | `#146B5B` | 顶部栏、主按钮、当前菜单、关键数字 |
| 辅助色 | `#3E8E7E` | 次级按钮、筛选选中态、普通图表系列 |
| 强调色 | `#D9A441` | 重点提示、待办数量、图表对比系列 |
| 页面背景 | `#F5F6F7` | 内容区背景，表格与表单用白色容器 |
| 正文文字 | `#1F2933` | 标题与正文；次要信息 `#5B6770` |
| 分割线 | `#E4E7EA` | 表格、卡片描边 |
| 成功 | `#2B7955` | 已完成、已通过、正常 |
| 危险 | `#B02A37` | 驳回、超时、删除，仅小面积使用 |

图表序列：主色 → 辅助色 → 强调色 → 中性灰 `#B9C2C6`，同一含义跨页面保持一致。

## 3. 字体阶梯
| 层级 | 字号/行高 | 字重 | 用途 |
|---|---|---|---|
| 页面标题 | 18/26 | 600 | 内容区 h2 |
| 卡片标题 | 14/22 | 600 | 卡片 h3 |
| 正文 | 14/22 | 400 | 表格、表单 |
| 次要 | 12.5/20 | 400 | 说明、面包屑 |
| 数字 | 24/32 | 700 | KPI 数值，等宽数字 |

## 4. 间距与圆角
- 栅格：内容区外边距 24px；卡片间距 16px；表单行距 14px。
- 圆角：卡片 10px，按钮/输入框 6px，标签 999px，图片 8px。
- 阴影：`0 1px 2px rgba(31,41,51,.06), 0 8px 24px rgba(31,41,51,.06)`。

## 5. 布局
- 后台：220px 侧栏 + 56px 顶栏 + 自适应内容区；侧栏折叠保留图标与悬浮说明。
- 门户：64px 顶部导航（Logo + 频道 + 搜索 + 头像），内容最大宽 1280px 居中。
- 后台首页 1440×900 一屏不出现纵向滚动；图表区最小高度 170px。
- 移动端 <768px：侧栏改抽屉、筛选折叠为底部面板、表格转信息卡片、操作区固定安全区。

## 6. 角色视觉区分
| 角色 | 骨架 | 识别色 | 说明 |
|---|---|---|---|
| 平台管理员 | 后台 | 主色 #146B5B | 全域数据，系统管理入口齐全 |
| 校园运营人员 | 后台 | 主色 + 强调色标签 | 无系统管理中的用户/角色入口 |
| 学生卖家 | 门户 | 主色 | 发布、我的闲置、订单、信用 |
| 学生买家 | 门户 | 主色 | 浏览、下单、评价、举报 |
""")

with open(os.path.join(BASE, "components.md"), "w", encoding="utf-8") as f:
    f.write("""# NEW-051 组件规范

## 按钮
| 类型 | 高度 | 内边距 | 背景 | 文字 | 用途 |
|---|---|---|---|---|---|
| 主按钮 | 32px | 0 14px | `#146B5B` | #fff | 提交、确认、查询 |
| 次按钮 | 32px | 0 14px | #fff + 1px `#E4E7EA` | `#1F2933` | 重置、导出、取消 |
| 危险按钮 | 32px | 0 14px | #fff + 1px `#E3B4B9` | `#B02A37` | 驳回、删除、冻结 |
| 强调按钮 | 32px | 0 14px | `#D9A441` | `#3A2A05` | 门户主 CTA |
圆角 6px，字号 13px；禁用态透明度 0.45；行内操作使用文字链接，最多「查看 + 2 个高频操作」。

## 表单
- 标签宽 100–120px，右对齐；单列输入宽 360–520px；搜索下拉 180–240px。
- 输入框高 32px，圆角 6px，边框 `#E4E7EA`，聚焦边框主色 + 2px 外发光。
- 必填项标签前加红色星号；错误提示在输入框下方 12px，字号 12px。
- 日期区间、级联分类、字典下拉必须用对应组件，不用普通文本框替代。

## 表格
- 表头 12.5px/600，背景 `#FAFBFB`；行高 44px，首列可放 48×36 缩略图。
- 表头不固定宽度，按内容自适应，长文本 `show-overflow-tooltip`。
- 状态列使用标签组件；数字列右对齐并使用等宽数字。
- 分页固定右下：总条数 + 页码 + 每页条数（10/20/50）。

## 标签（状态）
高 22px，圆角 999px，字号 12px，语义色见设计系统；必须带中文标签，不能只用颜色。

## 卡片
白底、1px `#E4E7EA` 描边、10px 圆角、内边距 16px；卡片标题 14px/600，与内容间距 12px。

## 抽屉与弹窗
- 审批、流转、派单使用右侧抽屉（宽 520px），抽屉内顶部展示单据摘要。
- 危险操作二次确认弹窗，确认按钮为危险色；普通取消不做强提醒。
- 弹窗最大宽 640px，圆角 10px，遮罩 `rgba(31,41,51,.45)`。

## 空状态与加载
- 空状态说明原因与下一步操作（「当前筛选条件下无记录，清空筛选或新建数据」）。
- 加载超过 300ms 显示局部骨架，禁止整页白屏。

## 图片
- 列表缩略图 48×36，圆角 6px；详情主图 4:3，圆角 8px；六图轮播 3 列网格。
- 一律本地路径 `/uploads/...`，上传组件展示态渲染，禁止外链输入框。
""")

# 图标集
ICONS = [("dash", '<path d="M3 13h8V3H3v10zm10 8h8V11h-8v10zM3 21h8v-6H3v6zM13 9h8V3h-8v6z"/>'),
         ("order", '<path d="M6 2h9l5 5v15H6z" fill="none" stroke="currentColor" stroke-width="1.6"/>'),
         ("box", '<path d="M3 7l9-4 9 4v10l-9 4-9-4z" fill="none" stroke="currentColor" stroke-width="1.6"/><path d="M3 7l9 4 9-4M12 11v10" fill="none" stroke="currentColor" stroke-width="1.2"/>'),
         ("check", '<path d="M4 12l5 5L20 6" fill="none" stroke="currentColor" stroke-width="2"/>'),
         ("alert", '<path d="M12 3l9 17H3z" fill="none" stroke="currentColor" stroke-width="1.6"/><path d="M12 9v5M12 17v1" stroke="currentColor" stroke-width="1.8"/>'),
         ("star", '<path d="M12 3l2.8 5.9 6.2.8-4.6 4.3 1.2 6.2L12 17.4 6.4 20.2l1.2-6.2L3 9.7l6.2-.8z" fill="none" stroke="currentColor" stroke-width="1.5"/>'),
         ("medal", '<circle cx="12" cy="14" r="6" fill="none" stroke="currentColor" stroke-width="1.6"/><path d="M8 3l3 6M16 3l-3 6" stroke="currentColor" stroke-width="1.6"/>'),
         ("chart", '<path d="M4 20V9M10 20V4M16 20v-7M22 20H2" fill="none" stroke="currentColor" stroke-width="1.7"/>'),
         ("gear", '<circle cx="12" cy="12" r="3.2" fill="none" stroke="currentColor" stroke-width="1.6"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3" stroke="currentColor" stroke-width="1.6"/>'),
         ("tag", '<path d="M3 12l9-9 9 9-9 9z" fill="none" stroke="currentColor" stroke-width="1.6"/>'),
         ("pin", '<path d="M12 22s7-6.3 7-12A7 7 0 1 0 5 10c0 5.7 7 12 7 12z" fill="none" stroke="currentColor" stroke-width="1.6"/>'),
         ("rule", '<path d="M4 6h16M4 12h10M4 18h13" stroke="currentColor" stroke-width="1.7"/>'),
         ("bell", '<path d="M6 16V10a6 6 0 1 1 12 0v6l2 3H4z" fill="none" stroke="currentColor" stroke-width="1.6"/>'),
         ("user", '<circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" stroke-width="1.6"/><path d="M4 21c0-4.4 3.6-7 8-7s8 2.6 8 7" fill="none" stroke="currentColor" stroke-width="1.6"/>'),
         ("shield", '<path d="M12 3l8 3v6c0 5-3.4 8.2-8 9-4.6-.8-8-4-8-9V6z" fill="none" stroke="currentColor" stroke-width="1.6"/>'),
         ("list", '<path d="M4 6h16M4 12h16M4 18h16" stroke="currentColor" stroke-width="1.7"/>'),
         ("book", '<path d="M4 4h7v16H4zM13 4h7v16h-7z" fill="none" stroke="currentColor" stroke-width="1.6"/>'),
         ("doc", '<path d="M5 3h10l4 4v14H5z" fill="none" stroke="currentColor" stroke-width="1.6"/>')]
svg = ['<svg xmlns="http://www.w3.org/2000/svg" style="display:none">']
for name, body in ICONS:
    svg.append(f'<symbol id="i-{name}" viewBox="0 0 24 24">{body}</symbol>')
svg.append("</svg>")
svg.append("<!-- 用法：<svg width=16 height=16><use href=\"icons.svg#i-order\"/></svg> 覆盖 18 个业务图标 -->")
with open(os.path.join(BASE, "icons.svg"), "w", encoding="utf-8") as f:
    f.write("\n".join(svg))

print("design pages:", len(PAGE_BODIES))
