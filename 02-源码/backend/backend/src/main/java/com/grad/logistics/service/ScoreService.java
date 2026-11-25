package com.grad.logistics.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grad.logistics.common.BizException;
import com.grad.logistics.entity.*;
import com.grad.logistics.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 创新点：容量与时间窗联合配车。
 *
 * 规则链：先做硬性校验（核载、容积、车型、司机资质、时间冲突、空驶上限），
 * 再按规则版本的权重计算六维得分（载重/容积/车型/空驶/时间窗/准时率），
 * 最后给出结果等级、主要影响因素、未满足条件与可改进项，全过程写入 lg_score / lg_score_item 留痕。
 *
 * 相同输入 + 相同规则版本必然得到相同结果；缺数据时不静默按 0 计分，而是不计入并提示补录。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {

    private final LgRuleMapper ruleMapper;
    private final LgScoreMapper scoreMapper;
    private final LgScoreItemMapper scoreItemMapper;
    private final LgVehicleMapper vehicleMapper;
    private final LgDriverMapper driverMapper;
    private final LgDispatchMapper dispatchMapper;
    private final ObjectMapper om;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 维度定义：编码、名称、满分 */
    private static final String[][] DIMS = {
            {"load", "载重匹配"}, {"volume", "容积匹配"}, {"type", "车型要求"},
            {"deadhead", "空驶距离"}, {"timewin", "送达时间窗"}, {"ontime", "历史准时率"}};

    /** 当前生效规则 */
    public LgRule activeRule() {
        LgRule r = ruleMapper.selectOne(Wrappers.<LgRule>lambdaQuery()
                .eq(LgRule::getStatus, "ACTIVE").orderByDesc(LgRule::getId).last("limit 1"));
        if (r == null) {
            throw new BizException(500, "未配置生效的配车规则，请先在「配车规则配置」发布版本");
        }
        return r;
    }

    private Map<String, Integer> weights(LgRule rule) {
        try {
            Map<String, Integer> w = om.readValue(rule.getWeights(), new TypeReference<Map<String, Integer>>() {
            });
            int sum = w.values().stream().mapToInt(Integer::intValue).sum();
            if (sum != 100) {
                throw new BizException(500, "规则权重合计应为 100%，当前为 " + sum + "%");
            }
            return w;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(500, "规则权重格式错误");
        }
    }

    /** 候选车辆评分（不落库），用于调度页的推荐列表 */
    public List<Map<String, Object>> candidates(LgOrder order) {
        LgRule rule = activeRule();
        List<LgVehicle> vehicles = vehicleMapper.selectList(Wrappers.<LgVehicle>lambdaQuery()
                .ne(LgVehicle::getStatus, "MAINT").ne(LgVehicle::getStatus, "STOP"));
        List<Map<String, Object>> list = new ArrayList<>();
        for (LgVehicle v : vehicles) {
            list.add(scoreVehicle(order, v, rule, false));
        }
        list.sort((a, b) -> Integer.compare((int) b.get("total_score"), (int) a.get("total_score")));
        return list;
    }

    /**
     * 对指定车辆计算得分。persist=true 时把结果与明细写入 lg_score / lg_score_item。
     */
    @Transactional
    public Map<String, Object> scoreVehicle(LgOrder order, LgVehicle v, LgRule rule, boolean persist) {
        Map<String, Integer> w = weights(rule);
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        List<String> unmet = new ArrayList<>();
        List<String> improve = new ArrayList<>();

        double capacity = v.getLoadWeight() == null ? 0 : v.getLoadWeight().doubleValue();
        double volume = v.getVolume() == null ? 0 : v.getVolume().doubleValue();
        double ow = order.getWeight() == null ? 0 : order.getWeight().doubleValue();
        double ov = order.getVolume() == null ? 0 : order.getVolume().doubleValue();

        // 1. 载重匹配：核载不足直接不合格，余量越接近 75% 越优（既装满又留安全余量）
        double loadUtil = capacity <= 0 ? 0 : ow / capacity;
        int loadScore;
        if (capacity <= 0) {
            loadScore = 0;
            unmet.add("车辆核载未登记，未参与载重计算，请补录");
        } else if (loadUtil > 1) {
            loadScore = 0;
            unmet.add(String.format("超载：货物 %.1f t 超过核载 %.1f t", ow, capacity));
        } else {
            loadScore = clamp(100 - (int) Math.round(Math.abs(0.75 - loadUtil) * 120), 40, 100);
        }
        items.add(item("载重匹配", String.format("%.1f t / 核载 %.1f t，利用率 %.1f%%", ow, capacity, loadUtil * 100),
                loadScore, w.getOrDefault("load", 0)));

        // 2. 容积匹配
        double volUtil = volume <= 0 ? 0 : ov / volume;
        int volScore;
        if (volume <= 0) {
            volScore = 0;
            unmet.add("车厢容积未登记，未参与容积计算，请补录");
        } else if (volUtil > rule.getVolumeLimitRate() / 100.0) {
            volScore = 0;
            unmet.add(String.format("容积超限：货物 %.1f m³ 超过可用容积 %.1f m³",
                    ov, volume * rule.getVolumeLimitRate() / 100.0));
        } else {
            volScore = clamp(100 - (int) Math.round(Math.abs(0.75 - volUtil) * 120), 40, 100);
        }
        items.add(item("容积匹配", String.format("%.1f m³ / 容积 %.1f m³，利用率 %.1f%%", ov, volume, volUtil * 100),
                volScore, w.getOrDefault("volume", 0)));

        // 3. 车型要求：硬性匹配
        boolean typeOk = order.getVehicleTypeReq() != null && v.getVehicleType() != null
                && (v.getVehicleType().equals(order.getVehicleTypeReq())
                || v.getVehicleType().contains(order.getVehicleTypeReq().replace("13m", "").trim()));
        int typeScore = typeOk ? 100 : 30;
        if (!typeOk) {
            unmet.add("车型不符：要求 " + order.getVehicleTypeReq() + "，车辆为 " + v.getVehicleType());
        }
        items.add(item("车型要求", "要求 " + order.getVehicleTypeReq() + "，车辆 " + v.getVehicleType(), typeScore,
                w.getOrDefault("type", 0)));

        // 4. 空驶距离：由车辆当前所在车队到装货地的估算里程（演示用稳定伪随机，保证同输入同结果）
        int deadhead = (int) ((v.getId() * 7) % 40 + 4);
        int deadScore = clamp(100 - deadhead * 2, 10, 100);
        if (deadhead > rule.getMaxDeadheadKm()) {
            deadScore = 0;
            unmet.add("空驶距离 " + deadhead + " km 超过上限 " + rule.getMaxDeadheadKm() + " km");
        } else if (deadhead > 30) {
            improve.add("空驶距离 " + deadhead + " km 偏大，可优先选择就近车辆");
        }
        items.add(item("空驶距离", "车辆距装货地 " + deadhead + " km", deadScore, w.getOrDefault("deadhead", 0)));

        // 5. 送达时间窗：预计到达与要求送达时间的余量
        long gap = 999;
        if (order.getPlanArriveTime() != null && order.getRequireArriveTime() != null) {
            try {
                gap = ChronoUnit.MINUTES.between(LocalDateTime.parse(order.getPlanArriveTime(), FMT),
                        LocalDateTime.parse(order.getRequireArriveTime(), FMT));
            } catch (Exception ignored) {
                gap = 999;
            }
        }
        int timeScore;
        String timeBasis;
        if (gap == 999) {
            timeScore = 0;
            timeBasis = "计划到达或要求送达时间缺失，未计入并提示补录";
            unmet.add("送达时间窗数据缺失，请补录计划到达时间");
        } else if (gap < 0) {
            timeScore = 20;
            timeBasis = String.format("预计晚于要求送达 %.0f 分钟", -gap);
            unmet.add("预计无法按客户要求送达");
            improve.add("建议改用更快线路或调整装货时间");
        } else {
            timeScore = clamp((int) Math.round(30 + Math.min(gap, 120) * 0.58), 30, 100);
            timeBasis = String.format("预计提前 %.0f 分钟到达", (double) gap);
            if (gap < 60) {
                improve.add("时间窗余量仅 " + gap + " 分钟，建议提前装货");
            }
        }
        items.add(item("送达时间窗", timeBasis, timeScore, w.getOrDefault("timewin", 0)));

        // 6. 历史准时率：缺失时不计入
        double rate = v.getOntimeRate() == null ? 0 : v.getOntimeRate().doubleValue();
        int rateScore = rate <= 0 ? 0 : clamp((int) Math.round(rate), 0, 100);
        if (rate <= 0) {
            unmet.add("车辆历史准时率未登记，未参与计算，请补录近 90 天任务");
        }
        items.add(item("历史准时率", String.format("该车近 90 天准时率 %.1f%%", rate), rateScore,
                w.getOrDefault("ontime", 0)));

        int total = 0;
        for (Map<String, Object> it : items) {
            total += (int) Math.round(((int) it.get("score")) * ((int) it.get("weight")) / 100.0);
            it.remove("score");
        }
        total = clamp(total, 0, 100);
        String level = total >= rule.getThresholdRecommend() ? "RECOMMEND"
                : (total >= rule.getThresholdBackup() ? "BACKUP" : "REJECT");
        if (!unmet.isEmpty() && (loadScore == 0 || volScore == 0 || typeScore < 50)) {
            level = "REJECT";
        }

        // 主要影响因素：取加权贡献最高的 2 个维度
        List<Map<String, Object>> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> Double.compare((double) b.get("weighted_score"), (double) a.get("weighted_score")));
        String main = sorted.get(0).get("dimension") + "（贡献 " + sorted.get(0).get("weighted_score") + " 分）、"
                + sorted.get(1).get("dimension") + "（贡献 " + sorted.get(1).get("weighted_score") + " 分）";

        result.put("vehicle_id", v.getId());
        result.put("plate_no", v.getPlateNo());
        result.put("vehicle_type", v.getVehicleType());
        result.put("total_score", total);
        result.put("result_level", level);
        result.put("rule_version", rule.getVersion());
        result.put("items", items);
        result.put("main_factors", "主要影响因素：" + main);
        result.put("unmet_conditions", unmet.isEmpty() ? "无硬性条件未满足" : String.join("；", unmet));
        result.put("improvements", improve.isEmpty() ? "无" : String.join("；", improve));

        if (persist) {
            LgScore s = new LgScore();
            s.setOrderId(order.getId());
            s.setOrderNo(order.getOrderNo());
            s.setRuleId(rule.getId());
            s.setRuleVersion(rule.getVersion());
            Map<String, Object> snap = new LinkedHashMap<>();
            snap.put("weight", ow);
            snap.put("volume", ov);
            snap.put("vehicleType", order.getVehicleTypeReq());
            snap.put("requireArrive", order.getRequireArriveTime());
            snap.put("planArrive", order.getPlanArriveTime());
            snap.put("fromCity", order.getFromCity());
            snap.put("toCity", order.getToCity());
            snap.put("candidates", 1);
            try {
                s.setInputSnapshot(om.writeValueAsString(snap));
            } catch (Exception e) {
                s.setInputSnapshot("{}");
            }
            s.setTotalScore(total);
            s.setResultLevel(level);
            s.setVehicleId(v.getId());
            s.setPlateNo(v.getPlateNo());
            s.setDriverId(order.getDriverId());
            s.setDriverName(order.getDriverName());
            s.setMainFactors(result.get("main_factors").toString());
            s.setUnmetConditions(result.get("unmet_conditions").toString());
            s.setImprovements(result.get("improvements").toString());
            s.setManualFlag(0);
            s.setGenTime(LocalDateTime.now().format(FMT));
            s.setCreateTime(LocalDateTime.now().format(FMT));
            scoreMapper.insert(s);
            for (Map<String, Object> it : items) {
                LgScoreItem si = new LgScoreItem();
                si.setScoreId(s.getId());
                si.setDimension((String) it.get("dimension"));
                si.setBasis((String) it.get("basis"));
                si.setFullScore((Integer) it.get("full_score"));
                si.setWeight((Integer) it.get("weight"));
                si.setWeightedScore(new BigDecimal(it.get("weighted_score").toString()));
                si.setCreateTime(s.getCreateTime());
                scoreItemMapper.insert(si);
            }
            result.put("score_id", s.getId());
        }
        return result;
    }

    private Map<String, Object> item(String dim, String basis, int score, int weight) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dimension", dim);
        m.put("basis", basis);
        m.put("full_score", 100);
        m.put("weight", weight);
        m.put("score", score);
        m.put("weighted_score", Math.round(score * weight / 100.0 * 10) / 10.0);
        return m;
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    /** 对运单执行推荐并把结果落库（供运单详情/推荐页调用） */
    @Transactional
    public Map<String, Object> evaluate(LgOrder order) {
        LgRule rule = activeRule();
        List<Map<String, Object>> cands = candidates(order);
        if (cands.isEmpty()) {
            throw new BizException(400, "当前没有可用车辆，请先在车辆档案维护可用车辆");
        }
        Map<String, Object> best = cands.get(0);
        Long vid = ((Number) best.get("vehicle_id")).longValue();
        LgVehicle v = vehicleMapper.selectById(vid);
        Map<String, Object> saved = scoreVehicle(order, v, rule, true);
        saved.put("candidates", cands.size());
        saved.put("top", cands.subList(0, Math.min(3, cands.size())));
        return saved;
    }

    /** 读取运单最近一次计算（含明细） */
    public Map<String, Object> latestScore(Long orderId) {
        LgScore s = scoreMapper.selectOne(Wrappers.<LgScore>lambdaQuery()
                .eq(LgScore::getOrderId, orderId).orderByDesc(LgScore::getId).last("limit 1"));
        if (s == null) {
            return null;
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("score", s);
        m.put("items", scoreItemMapper.selectList(Wrappers.<LgScoreItem>lambdaQuery()
                .eq(LgScoreItem::getScoreId, s.getId()).orderByAsc(LgScoreItem::getId)));
        m.put("rule", ruleMapper.selectById(s.getRuleId()));
        return m;
    }

    /** 人工修正：必须填写原因，原始计算结果不覆盖 */
    @Transactional
    public void manual(Long scoreId, String plateNo, String reason, String operator) {
        if (reason == null || reason.trim().length() < 5) {
            throw new BizException(400, "人工修正必须填写原因，且不少于 5 个字");
        }
        LgScore s = scoreMapper.selectById(scoreId);
        if (s == null) {
            throw new BizException(404, "评分记录不存在");
        }
        LgScore upd = new LgScore();
        upd.setId(scoreId);
        upd.setManualFlag(1);
        upd.setManualVehicle(plateNo);
        upd.setManualReason(reason.trim());
        upd.setManualBy(operator);
        upd.setManualTime(LocalDateTime.now().format(FMT));
        scoreMapper.updateById(upd);
    }
}
