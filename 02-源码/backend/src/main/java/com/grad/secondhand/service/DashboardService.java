package com.grad.secondhand.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grad.secondhand.entity.*;
import com.grad.secondhand.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统计看板（需求第 9 节）：核心指标 + 图表数据。
 * 指标口径统一为「按 create_time 落在区间内」，与明细列表同一数据源，保证图表总数与列表总数一致。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final GoodsMapper goodsMapper;
    private final TradeOrderMapper orderMapper;
    private final SysUserMapper userMapper;
    private final ReportMapper reportMapper;
    private final CategoryMapper categoryMapper;
    private final ReviewMapper reviewMapper;
    private final PriceCalcMapper calcMapper;

    /** 登录页公开数据：不暴露具体业务明细，只展示规模感 */
    public Map<String, Object> siteStats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("goods", goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery().eq(Goods::getStatus, "在售")));
        m.put("deals", orderMapper.selectCount(Wrappers.<TradeOrder>lambdaQuery().eq(TradeOrder::getStatus, "已完成")));
        m.put("users", userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getStatus, 1)));
        m.put("campuses", 3);
        return m;
    }

    /** 运营首页：8 个核心指标 + 7 组图表数据 */
    public Map<String, Object> overview(int days) {
        LocalDateTime from = LocalDate.now().minusDays(days - 1L).atStartOfDay();
        String fromStr = from.format(FMT);

        List<Goods> goodsAll = goodsMapper.selectList(Wrappers.<Goods>lambdaQuery());
        List<TradeOrder> orderAll = orderMapper.selectList(Wrappers.<TradeOrder>lambdaQuery());

        long todayNew = goodsAll.stream().filter(g -> g.getCreateTime() != null
                && g.getCreateTime().startsWith(LocalDate.now().format(DAY))).count();
        long pending = goodsAll.stream().filter(g -> "待审核".equals(g.getStatus())).count();
        long onSale = goodsAll.stream().filter(g -> "在售".equals(g.getStatus())).count();
        List<TradeOrder> done = orderAll.stream().filter(o -> "已完成".equals(o.getStatus())).toList();
        long weekDeal = done.stream().filter(o -> o.getCreateTime() != null
                && o.getCreateTime().compareTo(LocalDateTime.now().minusDays(7).format(FMT)) >= 0).count();
        long dispute = orderAll.stream().filter(o -> "纠纷中".equals(o.getStatus())).count();
        BigDecimal avgPrice = done.isEmpty() ? BigDecimal.ZERO
                : done.stream().map(TradeOrder::getDealPrice).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(done.size()), 2, RoundingMode.HALF_UP);
        long activeSellers = goodsAll.stream().filter(g -> g.getCreateTime() != null
                        && g.getCreateTime().compareTo(LocalDateTime.now().minusDays(7).format(FMT)) >= 0)
                .map(Goods::getSellerId).distinct().count();
        long totalOrders = orderAll.size();

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("todayNew", todayNew);
        kpi.put("pendingAudit", pending);
        kpi.put("onSale", onSale);
        kpi.put("weekDeal", weekDeal);
        kpi.put("conversionRate", goodsAll.isEmpty() ? 0
                : Math.round(done.size() * 1000.0 / goodsAll.size()) / 10.0);
        kpi.put("avgDealPrice", avgPrice);
        kpi.put("disputeRate", totalOrders == 0 ? 0 : Math.round(dispute * 1000.0 / totalOrders) / 10.0);
        kpi.put("activeSellers", activeSellers);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("kpi", kpi);
        data.put("priceStats", priceStats());
        data.put("trend", trend(days));
        data.put("categorySupply", categorySupply());
        data.put("campusDeal", campusDeal());
        data.put("funnel", funnel(goodsAll, orderAll));
        data.put("creditDistribution", creditDistribution());
        data.put("reportTypes", reportTypes());
        data.put("supplyWarning", supplyWarning());
        return data;
    }

    /** 近 N 天发布量与成交量双折线 */
    public List<Map<String, Object>> trend(int days) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            String day = LocalDate.now().minusDays(i).format(DAY);
            long publish = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery().likeRight(Goods::getCreateTime, day));
            long deal = orderMapper.selectCount(Wrappers.<TradeOrder>lambdaQuery()
                    .eq(TradeOrder::getStatus, "已完成").likeRight(TradeOrder::getCreateTime, day));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("day", day.substring(5));
            m.put("publish", publish);
            m.put("deal", deal);
            list.add(m);
        }
        return list;
    }

    /** 分类供给分布（一级分类下在售数量） */
    public List<Map<String, Object>> categorySupply() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Category c : categoryMapper.selectList(Wrappers.<Category>lambdaQuery().eq(Category::getParentId, 0))) {
            Long count = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery()
                    .eq(Goods::getStatus, "在售")
                    .inSql(Goods::getCategoryId, "select id from category where parent_id = " + c.getId()));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", c.getName());
            m.put("value", count);
            list.add(m);
        }
        return list;
    }

    /** 各校区成交量 */
    public List<Map<String, Object>> campusDeal() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String campus : List.of("东校区", "西校区", "北校区")) {
            Long count = orderMapper.selectCount(Wrappers.<TradeOrder>lambdaQuery()
                    .eq(TradeOrder::getStatus, "已完成")
                    .inSql(TradeOrder::getGoodsId, "select id from goods where campus_name = '" + campus + "'"));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", campus);
            m.put("value", count);
            list.add(m);
        }
        return list;
    }

    /** 发布 → 审核通过 → 下单 → 成交 漏斗 */
    public List<Map<String, Object>> funnel(List<Goods> goodsAll, List<TradeOrder> orderAll) {
        long published = goodsAll.size();
        long audited = goodsAll.stream().filter(g -> !"待审核".equals(g.getStatus())
                && !"审核驳回".equals(g.getStatus())).count();
        long ordered = orderAll.stream().map(TradeOrder::getGoodsId).distinct().count();
        long completed = orderAll.stream().filter(o -> "已完成".equals(o.getStatus())).map(TradeOrder::getGoodsId).distinct().count();
        List<Map<String, Object>> list = new ArrayList<>();
        String[][] stages = {{"发布", String.valueOf(published)}, {"审核通过", String.valueOf(audited)},
                {"产生订单", String.valueOf(ordered)}, {"完成成交", String.valueOf(completed)}};
        for (String[] s : stages) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", s[0]);
            m.put("value", Long.valueOf(s[1]));
            list.add(m);
        }
        return list;
    }

    /** 用户信用分分布直方图 */
    public List<Map<String, Object>> creditDistribution() {
        String[][] buckets = {{"0-39", "0", "39"}, {"40-59", "40", "59"}, {"60-79", "60", "79"},
                {"80-99", "80", "99"}, {"100-119", "100", "119"}, {"120-150", "120", "150"}};
        List<Map<String, Object>> list = new ArrayList<>();
        for (String[] b : buckets) {
            Long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .ge(SysUser::getCreditScore, Integer.parseInt(b[1]))
                    .le(SysUser::getCreditScore, Integer.parseInt(b[2]))
                    .inSql(SysUser::getId, "select user_id from sys_user_role where role_id in (3,4)"));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", b[0]);
            m.put("value", count);
            list.add(m);
        }
        return list;
    }

    /** 举报类型占比 */
    public List<Map<String, Object>> reportTypes() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String type : List.of("违禁品", "虚假描述", "诈骗", "骚扰", "校外人员")) {
            Long count = reportMapper.selectCount(Wrappers.<Report>lambdaQuery().eq(Report::getReasonType, type));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", type);
            m.put("value", count);
            list.add(m);
        }
        return list;
    }

    /** 供大于求提示：在售多而成交换少的分类 */
    public List<Map<String, Object>> supplyWarning() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Category c : categoryMapper.selectList(Wrappers.<Category>lambdaQuery().eq(Category::getParentId, 0))) {
            long onSale = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery().eq(Goods::getStatus, "在售")
                    .inSql(Goods::getCategoryId, "select id from category where parent_id = " + c.getId()));
            long sold = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery().eq(Goods::getStatus, "已售出")
                    .inSql(Goods::getCategoryId, "select id from category where parent_id = " + c.getId()));
            if (onSale >= 3 && onSale > sold) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", c.getName());
                m.put("onSale", onSale);
                m.put("sold", sold);
                m.put("tip", "在售 " + onSale + " 件、已成交 " + sold + " 件，建议做专题清理");
                list.add(m);
            }
        }
        return list;
    }

    /** 定价建议采纳率与人工修正率 */
    public Map<String, Object> priceStats() {
        long total = calcMapper.selectCount(null);
        long adopted = calcMapper.selectCount(Wrappers.<PriceCalc>lambdaQuery().eq(PriceCalc::getAdopted, 1));
        long corrected = calcMapper.selectCount(Wrappers.<PriceCalc>lambdaQuery().isNotNull(PriceCalc::getCorrectedGrade));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", total);
        m.put("adoptRate", total == 0 ? 0 : Math.round(adopted * 1000.0 / total) / 10.0);
        m.put("correctRate", total == 0 ? 0 : Math.round(corrected * 1000.0 / total) / 10.0);
        return m;
    }

    /** 运营统计页：按分类/校区/成色的供给与成交对比 */
    public Map<String, Object> analysis() {
        List<Map<String, Object>> byCategory = new ArrayList<>();
        for (Category c : categoryMapper.selectList(Wrappers.<Category>lambdaQuery().eq(Category::getParentId, 0)
                .orderByAsc(Category::getSortNo))) {
            long onSale = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery().eq(Goods::getStatus, "在售")
                    .inSql(Goods::getCategoryId, "select id from category where parent_id = " + c.getId()));
            long sold = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery().eq(Goods::getStatus, "已售出")
                    .inSql(Goods::getCategoryId, "select id from category where parent_id = " + c.getId()));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", c.getName());
            m.put("onSale", onSale);
            m.put("sold", sold);
            m.put("rate", onSale + sold == 0 ? 0 : Math.round(sold * 100.0 / (onSale + sold)));
            byCategory.add(m);
        }
        List<Map<String, Object>> byCondition = new ArrayList<>();
        for (String cond : List.of("全新", "九成新", "七成新", "五成新", "有瑕疵")) {
            long onSale = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery()
                    .eq(Goods::getConditionLevel, cond).eq(Goods::getStatus, "在售"));
            long sold = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery()
                    .eq(Goods::getConditionLevel, cond).eq(Goods::getStatus, "已售出"));
            BigDecimal avg = BigDecimal.ZERO;
            List<Goods> soldGoods = goodsMapper.selectList(Wrappers.<Goods>lambdaQuery()
                    .eq(Goods::getConditionLevel, cond).eq(Goods::getStatus, "已售出"));
            if (!soldGoods.isEmpty()) {
                avg = soldGoods.stream().map(Goods::getPrice).filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(soldGoods.size()), 2, RoundingMode.HALF_UP);
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", cond);
            m.put("onSale", onSale);
            m.put("sold", sold);
            m.put("avgPrice", avg);
            byCondition.add(m);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("byCategory", byCategory);
        data.put("byCondition", byCondition);
        data.put("byCampus", campusDeal());
        data.put("reportTypes", reportTypes());
        data.put("reviewCount", reviewMapper.selectCount(null));
        return data;
    }
}
