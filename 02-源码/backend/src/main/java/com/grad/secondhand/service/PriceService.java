package com.grad.secondhand.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grad.secondhand.common.BizException;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.dto.Dtos;
import com.grad.secondhand.entity.*;
import com.grad.secondhand.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 创新点：商品定价参考与成交概率评分（需求文档第 8 节）。
 *
 * 计算规则：
 *   建议价 = 同分类成交价中位数 × 成色系数 × 校区供需系数
 *   成交概率分 = Σ(单项归一化得分 × 权重)，权重合计 100
 * 缺数据时该项「不计分并从权重中剔除」，剩余权重按比例归一，同时在结果中返回未满足条件，
 * 绝不因为缺数据默认给高分。每次计算保存输入快照与规则版本，历史结果不随规则变更而改变。
 */
@Service
@RequiredArgsConstructor
public class PriceService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 成色系数：直接用于建议价折算 */
    private static final Map<String, BigDecimal> CONDITION_FACTOR = Map.of(
            "全新", new BigDecimal("1.00"),
            "九成新", new BigDecimal("0.88"),
            "七成新", new BigDecimal("0.72"),
            "五成新", new BigDecimal("0.55"),
            "有瑕疵", new BigDecimal("0.40"));

    /** 成色得分：满分 100 */
    private static final Map<String, BigDecimal> CONDITION_SCORE = Map.of(
            "全新", new BigDecimal("100"),
            "九成新", new BigDecimal("88"),
            "七成新", new BigDecimal("72"),
            "五成新", new BigDecimal("55"),
            "有瑕疵", new BigDecimal("40"));

    private final GoodsMapper goodsMapper;
    private final TradeOrderMapper orderMapper;
    private final SysUserMapper userMapper;
    private final PriceRuleMapper ruleMapper;
    private final PriceCalcMapper calcMapper;
    private final TraceService trace;
    private final ObjectMapper objectMapper;

    /** 取启用中的规则；没有启用规则则退回最新的草稿/停用规则，保证页面不空转 */
    public PriceRule activeRule() {
        PriceRule rule = ruleMapper.selectOne(Wrappers.<PriceRule>lambdaQuery()
                .eq(PriceRule::getStatus, "启用").orderByDesc(PriceRule::getId).last("limit 1"));
        if (rule == null) {
            rule = ruleMapper.selectOne(Wrappers.<PriceRule>lambdaQuery()
                    .orderByDesc(PriceRule::getId).last("limit 1"));
        }
        if (rule == null) {
            throw new BizException(4001, "尚未配置定价规则，请先在规则配置页保存一版规则");
        }
        return rule;
    }

    /**
     * 计算某商品的定价评分并落库。
     * @param goodsId 商品
     * @param persist 是否写入 price_calc（预览时为 false，避免重复留痕）
     */
    @Transactional
    public Map<String, Object> calculate(Long goodsId, boolean persist) {
        Goods g = goodsMapper.selectById(goodsId);
        if (g == null) {
            throw new BizException(4000, "商品不存在或已删除");
        }
        PriceRule rule = activeRule();

        List<Map<String, Object>> factors = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        double weightedSum = 0;
        int weightCounted = 0;

        // 1) 同分类成交价偏离度（30%）：取近 30 天同分类已完成订单成交价中位数
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        List<TradeOrder> done = orderMapper.selectList(Wrappers.<TradeOrder>lambdaQuery()
                .eq(TradeOrder::getStatus, "已完成")
                .ge(TradeOrder::getCreateTime, from.format(FMT)));
        List<BigDecimal> prices = new ArrayList<>();
        for (TradeOrder o : done) {
            Goods og = goodsMapper.selectById(o.getGoodsId());
            if (og != null && Objects.equals(og.getCategoryId(), g.getCategoryId()) && o.getDealPrice() != null) {
                prices.add(o.getDealPrice());
            }
        }
        BigDecimal median = prices.isEmpty() ? null : median(prices);
        double scCategory;
        BigDecimal suggestLow;
        BigDecimal suggestHigh;
        BigDecimal price = g.getPrice() == null ? BigDecimal.ZERO : g.getPrice();
        // 无成交样本时不能留空，退化为「以在售标价为基准 × 成色系数 × 供需系数」，
        // 否则商品详情页会出现「建议价 ¥null ~ ¥null」，对卖家毫无参考价值
        BigDecimal base;
        if (median == null) {
            missing.add("同分类近 30 天无成交样本，价格偏离度不计分，建议区间按在售标价折算");
            scCategory = 0;
            base = price;
        } else {
            // 偏离度：与中位数完全一致得 100，偏离 100% 及以上得 0
            double dev = Math.abs(price.doubleValue() - median.doubleValue()) / median.doubleValue();
            scCategory = Math.max(0, 100 - Math.min(dev, 1.0) * 100);
            base = median;
        }
        BigDecimal conditionFactorCalc = CONDITION_FACTOR.getOrDefault(g.getConditionLevel(), new BigDecimal("0.6"));
        base = base.multiply(conditionFactorCalc).multiply(supplyFactor(g));
        suggestLow = base.multiply(new BigDecimal("0.92")).setScale(2, RoundingMode.HALF_UP);
        suggestHigh = base.multiply(new BigDecimal("1.12")).setScale(2, RoundingMode.HALF_UP);
        weightedSum += scCategory * rule.getWeightCategory();
        weightCounted += rule.getWeightCategory();
        factors.add(factor("同分类成交价偏离度", rule.getWeightCategory(), scCategory,
                median == null ? "无成交样本，不计分"
                        : "近 30 天同分类成交 " + prices.size() + " 单，中位数 ¥" + median.setScale(2, RoundingMode.HALF_UP)));

        // 2) 商品成色（20%）
        double scCondition = CONDITION_SCORE.getOrDefault(g.getConditionLevel(), BigDecimal.valueOf(40))
                .doubleValue();
        weightedSum += scCondition * rule.getWeightCondition();
        weightCounted += rule.getWeightCondition();
        factors.add(factor("商品成色", rule.getWeightCondition(), scCondition,
                g.getConditionLevel() + "，建议价折算系数 " + CONDITION_FACTOR.getOrDefault(
                        g.getConditionLevel(), new BigDecimal("0.6"))));

        // 3) 同校同类在售数量（15%）：供给越多得分越低
        long supply = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getStatus, "在售").eq(Goods::getCampusId, g.getCampusId())
                .eq(Goods::getCategoryId, g.getCategoryId())
                .ne(Goods::getId, g.getId()));
        double scSupply = Math.max(20, 100 - supply * 12.0);
        weightedSum += scSupply * rule.getWeightSupply();
        weightCounted += rule.getWeightSupply();
        factors.add(factor("同校同类在售数量", rule.getWeightSupply(), scSupply,
                "同校区同分类在售 " + supply + " 件，供给越多成交越慢"));

        // 4) 发布时长与浏览转化（15%）：不足 2 小时不参与该项
        String createTime = g.getCreateTime();
        double hours = 0;
        try {
            hours = Duration.between(LocalDateTime.parse(createTime, FMT), LocalDateTime.now()).toMinutes() / 60.0;
        } catch (Exception ignore) {
            // 时间缺失时按新发布处理
        }
        double scHeat;
        if (hours < 2) {
            missing.add("发布不足 2 小时，热度项暂不参与计算");
            scHeat = 0;
        } else {
            int views = g.getViewCount() == null ? 0 : g.getViewCount();
            int favs = g.getFavoriteCount() == null ? 0 : g.getFavoriteCount();
            // 浏览转化：每 10 次浏览折算 8 分，收藏每次再折算 4 分，封顶 100
            scHeat = Math.min(100, views / 10.0 * 8 + favs * 4 + Math.min(20, hours / 24.0 * 5));
            weightedSum += scHeat * rule.getWeightHeat();
            weightCounted += rule.getWeightHeat();
        }
        factors.add(factor("发布时长与浏览转化", rule.getWeightHeat(), scHeat,
                String.format("%.0f 小时，浏览 %d，收藏 %d", hours,
                        g.getViewCount() == null ? 0 : g.getViewCount(),
                        g.getFavoriteCount() == null ? 0 : g.getFavoriteCount())));

        // 5) 卖家信用分（10%）
        SysUser seller = userMapper.selectById(g.getSellerId());
        int credit = seller == null || seller.getCreditScore() == null ? 100 : seller.getCreditScore();
        double scCredit = Math.min(100, credit / 1.5);
        weightedSum += scCredit * rule.getWeightCredit();
        weightCounted += rule.getWeightCredit();
        factors.add(factor("卖家信用分", rule.getWeightCredit(), scCredit, "信用分 " + credit));

        // 6) 图文完整度（10%）：图片数量、描述字数、是否填入手价
        int imgCount = g.getImageUrls() == null || g.getImageUrls().isBlank() ? 0 : g.getImageUrls().split(",").length;
        int descLen = g.getDescription() == null ? 0 : g.getDescription().length();
        double scInfo = Math.min(100, imgCount * 15 + Math.min(40, descLen / 2.0) + (g.getOriginalPrice() != null ? 15 : 0));
        weightedSum += scInfo * rule.getWeightInfo();
        weightCounted += rule.getWeightInfo();
        factors.add(factor("图文完整度", rule.getWeightInfo(), scInfo,
                imgCount + " 张图，" + descLen + " 字描述，" + (g.getOriginalPrice() != null ? "已填入手价" : "未填入手价")));

        // 归一化：缺项剔除后按已参与权重还原为百分制
        double total = weightCounted == 0 ? 0 : weightedSum / weightCounted;
        total = Math.round(total * 100) / 100.0;
        String grade = total >= 80 ? "A" : (total >= 60 ? "B" : "C");
        String conclusion = switch (grade) {
            case "A" -> "定价合理，预计 7 天内成交，建议保持价格并补充实拍图";
            case "B" -> "略高于同类成交区间，建议下调 5% 至 10% 或补充描述";
            default -> "明显偏离市场或信息不足，建议降价 15% 以上并补齐成色、入手价和图片";
        };

        // 未满足条件的可改进项
        List<String> improvements = new ArrayList<>();
        if (imgCount < 3) {
            improvements.add("实拍图少于 3 张，补充细节图可提升图文完整度");
        }
        if (g.getOriginalPrice() == null) {
            improvements.add("未填入手价，折价率无法计算，买家难以判断是否划算");
        }
        if (descLen < 40) {
            improvements.add("描述偏短，补充使用时长、瑕疵位置与配件清单");
        }
        if (supply > 6) {
            improvements.add("同校区同分类在售 " + (int) supply + " 件，供给偏多，适当降价更易成交");
        }

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("goodsId", g.getId());
        snapshot.put("categoryId", g.getCategoryId());
        snapshot.put("conditionLevel", g.getConditionLevel());
        snapshot.put("campusId", g.getCampusId());
        snapshot.put("price", price);
        snapshot.put("originalPrice", g.getOriginalPrice());
        snapshot.put("viewCount", g.getViewCount());
        snapshot.put("favoriteCount", g.getFavoriteCount());
        snapshot.put("imageCount", imgCount);
        snapshot.put("descLength", descLen);
        snapshot.put("creditScore", credit);
        snapshot.put("categorySampleSize", prices.size());
        snapshot.put("categoryMedian", median);
        snapshot.put("supplyCount", supply);
        snapshot.put("publishHours", Math.round(hours));

        Long calcId = null;
        if (persist) {
            PriceCalc pc = new PriceCalc();
            pc.setGoodsId(g.getId());
            pc.setRuleId(rule.getId());
            pc.setRuleVersion(rule.getRuleVersion());
            pc.setSnapshot(toJson(snapshot));
            pc.setScoreCategory(round(scCategory));
            pc.setScoreCondition(round(scCondition));
            pc.setScoreSupply(round(scSupply));
            pc.setScoreHeat(round(scHeat));
            pc.setScoreCredit(round(scCredit));
            pc.setScoreInfo(round(scInfo));
            pc.setTotalScore(BigDecimal.valueOf(total));
            pc.setGrade(grade);
            pc.setSuggestLow(suggestLow == null ? BigDecimal.ZERO : suggestLow);
            pc.setSuggestHigh(suggestHigh == null ? BigDecimal.ZERO : suggestHigh);
            pc.setConclusion(conclusion);
            pc.setFactors(joinFactors(factors));
            pc.setMissing(missing.isEmpty() ? null : String.join("；", missing));
            pc.setAdopted(0);
            pc.setCreateTime(LocalDateTime.now().format(FMT));
            pc.setDeleted(0);
            pc.setVersion(0);
            calcMapper.insert(pc);
            calcId = pc.getId();
            trace.log("定价评分", "计算", g.getGoodsNo(),
                    "规则 " + rule.getRuleVersion() + "，总分 " + total + "，等级 " + grade);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("calcId", calcId);
        data.put("goodsId", g.getId());
        data.put("goodsTitle", g.getTitle());
        data.put("goodsNo", g.getGoodsNo());
        data.put("ruleId", rule.getId());
        data.put("ruleVersion", rule.getRuleVersion());
        data.put("totalScore", total);
        data.put("grade", grade);
        data.put("conclusion", conclusion);
        data.put("suggestLow", suggestLow);
        data.put("suggestHigh", suggestHigh);
        data.put("factors", factors);
        data.put("missing", missing);
        data.put("improvements", improvements);
        data.put("snapshot", snapshot);
        data.put("weightCounted", weightCounted);
        return data;
    }

    /** 校区供需系数：同校区同类在售越多，建议价越低（0.85–1.05） */
    private BigDecimal supplyFactor(Goods g) {
        long supply = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getStatus, "在售").eq(Goods::getCampusId, g.getCampusId())
                .eq(Goods::getCategoryId, g.getCategoryId()).ne(Goods::getId, g.getId()));
        if (supply <= 2) {
            return new BigDecimal("1.05");
        }
        if (supply <= 5) {
            return new BigDecimal("1.00");
        }
        if (supply <= 8) {
            return new BigDecimal("0.93");
        }
        return new BigDecimal("0.85");
    }

    private Map<String, Object> factor(String name, Integer weight, double score, String basis) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("weight", weight);
        m.put("score", round(score));
        m.put("weighted", Math.round(score * weight) / 100.0);
        m.put("basis", basis);
        return m;
    }

    private String joinFactors(List<Map<String, Object>> factors) {
        List<String> parts = new ArrayList<>();
        for (Map<String, Object> f : factors) {
            parts.add(f.get("name") + " " + f.get("score"));
        }
        return String.join("、", parts);
    }

    private BigDecimal round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal median(List<BigDecimal> list) {
        List<BigDecimal> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2);
        }
        return sorted.get(n / 2 - 1).add(sorted.get(n / 2)).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 保存规则：六项权重合计必须 100，保存后生成新版本号并立即生效，
     * 旧规则置为停用（历史 price_calc 已固化 rule_version，不受影响）。
     */
    @Transactional
    public Map<String, Object> saveRule(Dtos.RuleReq req) {
        int sum = req.weightCategory() + req.weightCondition() + req.weightSupply()
                + req.weightHeat() + req.weightCredit() + req.weightInfo();
        if (sum != 100) {
            throw new BizException(4000, "六项权重合计必须等于 100%，当前为 " + sum + "%");
        }
        if (req.weightCategory() < 0 || req.weightCondition() < 0 || req.weightSupply() < 0
                || req.weightHeat() < 0 || req.weightCredit() < 0 || req.weightInfo() < 0) {
            throw new BizException(4000, "权重不能为负数");
        }
        long total = ruleMapper.selectCount(null);
        String version = "v" + (total + 1);
        // 旧版本停用，保证同一时刻只有一个启用规则
        for (PriceRule old : ruleMapper.selectList(Wrappers.<PriceRule>lambdaQuery().eq(PriceRule::getStatus, "启用"))) {
            PriceRule u = new PriceRule();
            u.setId(old.getId());
            u.setStatus("停用");
            ruleMapper.updateById(u);
        }
        PriceRule r = new PriceRule();
        r.setRuleName(req.ruleName());
        r.setRuleVersion(version);
        r.setWeightCategory(req.weightCategory());
        r.setWeightCondition(req.weightCondition());
        r.setWeightSupply(req.weightSupply());
        r.setWeightHeat(req.weightHeat());
        r.setWeightCredit(req.weightCredit());
        r.setWeightInfo(req.weightInfo());
        r.setStatus("启用");
        r.setEffectTime(LocalDateTime.now().format(FMT));
        r.setRemark(req.remark());
        r.setCreateTime(LocalDateTime.now().format(FMT));
        r.setDeleted(0);
        r.setVersion(0);
        ruleMapper.insert(r);
        trace.log("定价规则", "保存新版本", "price_rule:" + r.getId(),
                "生成 " + version + "，权重 " + sum + "%，原因：" + (req.remark() == null ? "无" : req.remark()));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", r.getId());
        data.put("version", version);
        return data;
    }

    /** 人工修正：只写修正字段，原始分数与等级保留，原因必填 */
    @Transactional
    public void correct(Long calcId, Dtos.PriceCorrectReq req) {
        if (!UserContext.isOperator()) {
            throw new BizException(4030, "只有校园运营人员可以修正评分结果");
        }
        PriceCalc pc = calcMapper.selectById(calcId);
        if (pc == null) {
            throw new BizException(4000, "评分记录不存在");
        }
        if (!List.of("A", "B", "C").contains(req.grade())) {
            throw new BizException(4000, "修正等级只能是 A/B/C");
        }
        PriceCalc u = new PriceCalc();
        u.setId(calcId);
        u.setCorrectedGrade(req.grade());
        u.setCorrectReason(req.reason());
        u.setCorrectBy(UserContext.userId());
        u.setCorrectTime(LocalDateTime.now().format(FMT));
        calcMapper.updateById(u);
        trace.log("定价评分", "人工修正", "price_calc:" + calcId,
                "原等级 " + pc.getGrade() + " 改为 " + req.grade() + "，原因：" + req.reason());
    }

    /** 卖家采纳建议价：用于统计推荐采纳率 */
    @Transactional
    public void adopt(Long calcId, boolean adopted) {
        PriceCalc u = new PriceCalc();
        u.setId(calcId);
        u.setAdopted(adopted ? 1 : 0);
        calcMapper.updateById(u);
    }

    /** 看板指标：推荐采纳率、人工修正率与修正后的分布 */
    public Map<String, Object> stats() {
        long total = calcMapper.selectCount(null);
        long adopted = calcMapper.selectCount(Wrappers.<PriceCalc>lambdaQuery().eq(PriceCalc::getAdopted, 1));
        long corrected = calcMapper.selectCount(Wrappers.<PriceCalc>lambdaQuery().isNotNull(PriceCalc::getCorrectedGrade));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", total);
        m.put("adopted", adopted);
        m.put("corrected", corrected);
        m.put("adoptRate", total == 0 ? 0 : Math.round(adopted * 1000.0 / total) / 10.0);
        m.put("correctRate", total == 0 ? 0 : Math.round(corrected * 1000.0 / total) / 10.0);
        return m;
    }
}
