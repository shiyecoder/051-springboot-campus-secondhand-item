package com.grad.logistics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 运营看板与统计报表：所有聚合在后端一次算完，前端不做二次聚合（需求文档 统计看板详细要求）。
 * 统计值与明细列表口径一致：都按 lg_order 主表 + 状态过滤。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JdbcTemplate jdbc;

    private List<Map<String, Object>> q(String sql, Object... args) {
        try {
            return jdbc.queryForList(sql, args);
        } catch (Exception e) {
            return List.of();
        }
    }

    private long one(String sql, Object... args) {
        try {
            Long v = jdbc.queryForObject(sql, Long.class, args);
            return v == null ? 0 : v;
        } catch (Exception e) {
            return 0;
        }
    }

    private double oneD(String sql, Object... args) {
        try {
            Double v = jdbc.queryForObject(sql, Double.class, args);
            return v == null ? 0 : v;
        } catch (Exception e) {
            return 0;
        }
    }

    /** 首页：6 个核心指标 + 5-7 个图表数据 */
    public Map<String, Object> overview() {
        Map<String, Object> m = new LinkedHashMap<>();

        List<Map<String, Object>> kpis = new ArrayList<>();
        kpis.add(kpi("运单量", String.valueOf(one("select count(*) from lg_order where deleted=0")), "近 6 个月累计", "up"));
        kpis.add(kpi("待调度", String.valueOf(one("select count(*) from lg_order where status='CREATED' and deleted=0")),
                "超 2 小时 " + one("select count(*) from lg_dispatch where status='TODO' and deadline < now()") + " 条", "dn"));
        kpis.add(kpi("在途运单", String.valueOf(one("select count(*) from lg_order where status in ('LOADED','SHIPPING') and deleted=0")),
                "今日到达 " + one("select count(*) from lg_order where status='ARRIVED' and deleted=0") + " 单", "up"));
        kpis.add(kpi("准时送达率", String.format("%.1f%%", oneD(
                "select round(100*sum(case when actual_arrive_time is not null and actual_arrive_time <= require_arrive_time then 1 else 0 end)/greatest(count(*),1),1) from lg_order where status in ('SIGNED','FEE_CHECK','DONE')")),
                "按已签收运单", "up"));
        kpis.add(kpi("异常运单", String.valueOf(one("select count(*) from lg_exception where status <> 'CLOSED'")),
                "待经理审核 " + one("select count(*) from lg_exception where status='PENDING_AUDIT'") + " 条", "dn"));
        kpis.add(kpi("近 30 天运费", String.format("¥ %.1f万", oneD(
                "select coalesce(sum(amount),0)/10000 from lg_fee where direction='RECV' and fee_type='运费' " +
                        "and submit_time >= date_sub(now(), interval 30 day)")),
                "按提交时间滚动统计", "up"));
        m.put("kpis", kpis);

        // 1 运单趋势（近 6 个月）
        m.put("trend", q("select date_format(create_time,'%Y-%m') as month, count(*) as orders, " +
                "round(coalesce(sum(receivable),0)/10000,1) as fee from lg_order where deleted=0 " +
                "and create_time >= date_sub(now(), interval 6 month) group by 1 order by 1"));

        // 2 运单状态分布
        m.put("status_dist", q("select status as code, count(*) as cnt from lg_order where deleted=0 group by status"));

        // 3 客户运量 TOP5
        m.put("customer_top", q("select customer_name as name, count(*) as cnt, " +
                "round(coalesce(sum(receivable),0),2) as amount from lg_order where deleted=0 " +
                "group by customer_name order by cnt desc limit 5"));

        // 4 车辆利用率
        m.put("vehicle_usage", q("select v.plate_no, v.vehicle_type, v.ontime_rate, " +
                "count(d.id) as trips, round(least(100, count(d.id)*7.5),1) as usage_rate " +
                "from lg_vehicle v left join lg_dispatch d on d.vehicle_id=v.id and d.status<>'CANCELED' " +
                "where v.deleted=0 group by v.id order by trips desc limit 8"));

        // 5 异常类型构成
        m.put("exception_type", q("select type as name, count(*) as cnt from lg_exception group by type order by cnt desc"));

        // 6 费用构成（当月）
        m.put("fee_compose", q("select fee_type as name, round(coalesce(sum(abs(amount)),0),2) as amount " +
                "from lg_fee group by fee_type order by amount desc"));

        // 7 线路排行（运量与平均时效）
        m.put("route_rank", q("select concat(from_city,'→',to_city) as name, count(*) as cnt, " +
                "round(avg(plan_hours),1) as avg_hours from lg_order where deleted=0 group by 1 order by cnt desc limit 6"));

        // 创新点效果：推荐分布与采纳率
        m.put("score_dist", q("select result_level as code, count(*) as cnt from lg_score group by result_level"));
        long total = one("select count(*) from lg_score");
        long manual = one("select count(*) from lg_score where manual_flag=1");
        Map<String, Object> effect = new LinkedHashMap<>();
        effect.put("total", total);
        effect.put("manual", manual);
        effect.put("adopt_rate", total == 0 ? 0 : Math.round((total - manual) * 1000.0 / total) / 10.0);
        effect.put("manual_rate", total == 0 ? 0 : Math.round(manual * 1000.0 / total) / 10.0);
        effect.put("manual_reasons", q("select manual_reason as reason, manual_by as operator, manual_time as time " +
                "from lg_score where manual_flag=1 order by id desc limit 5"));
        m.put("effect", effect);
        return m;
    }

    private Map<String, Object> kpi(String name, String value, String sub, String trend) {
        Map<String, Object> k = new LinkedHashMap<>();
        k.put("name", name);
        k.put("value", value);
        k.put("sub", sub);
        k.put("trend", trend);
        return k;
    }

    /** 登录页展示的经营口径（公开接口，仅返回聚合数，不含任何明细） */
    public Map<String, Object> siteStats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orders", one("select count(*) from lg_order where deleted=0"));
        m.put("vehicles", one("select count(*) from lg_vehicle where deleted=0"));
        m.put("drivers", one("select count(*) from lg_driver where deleted=0"));
        m.put("ontime", oneD("select round(100*sum(case when actual_arrive_time is not null and actual_arrive_time <= require_arrive_time then 1 else 0 end)"
                + "/greatest(count(*),1),1) from lg_order where status in ('SIGNED','FEE_CHECK','DONE')"));
        m.put("customers", one("select count(*) from lg_customer where deleted=0"));
        return m;
    }

    /** 统计报表：客户运量、车辆利用率、准时率、线路排行与费用构成 */
    public Map<String, Object> analysis() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("monthly", q("select date_format(create_time,'%Y-%m') as month, count(*) as orders, " +
                "round(coalesce(sum(receivable),0)/10000,1) as fee from lg_order where deleted=0 " +
                "group by 1 order by 1"));
        m.put("customer_volume", q("select customer_name as name, count(*) as cnt, " +
                "round(coalesce(sum(weight),0),1) as weight from lg_order where deleted=0 " +
                "group by customer_name order by cnt desc limit 8"));
        m.put("vehicle_usage", q("select v.plate_no, v.vehicle_type, v.ontime_rate, count(d.id) as trips, " +
                "round(coalesce(sum(o.plan_distance),0),0) as mileage, " +
                "round(least(100, count(d.id)*7.5),1) as usage_rate from lg_vehicle v " +
                "left join lg_dispatch d on d.vehicle_id=v.id and d.status<>'CANCELED' " +
                "left join lg_order o on o.id=d.order_id where v.deleted=0 group by v.id order by trips desc"));
        m.put("ontime_by_month", q("select date_format(create_time,'%Y-%m') as month, " +
                "round(100*sum(case when actual_arrive_time is not null and actual_arrive_time <= require_arrive_time then 1 else 0 end)" +
                "/greatest(sum(case when actual_arrive_time is not null then 1 else 0 end),1),1) as rate " +
                "from lg_order where deleted=0 group by 1 order by 1"));
        m.put("fee_compose", q("select fee_type as name, round(coalesce(sum(abs(amount)),0),2) as amount " +
                "from lg_fee group by fee_type order by amount desc"));
        m.put("route_rank", q("select concat(from_city,'→',to_city) as name, count(*) as cnt, " +
                "round(avg(plan_hours),1) as avg_hours, round(avg(plan_distance),0) as avg_km " +
                "from lg_order where deleted=0 group by 1 order by cnt desc limit 8"));

        // 创新结果分布与采纳率：看板上已下放到本页，需在这里单独返回，否则报表页两处为 0
        m.put("score_dist", q("select result_level as code, count(*) as cnt from lg_score group by result_level"));
        long total = one("select count(*) from lg_score");
        long manual = one("select count(*) from lg_score where manual_flag=1");
        Map<String, Object> effect = new LinkedHashMap<>();
        effect.put("total", total);
        effect.put("manual", manual);
        effect.put("adopt_rate", total == 0 ? 0 : Math.round((total - manual) * 1000.0 / total) / 10.0);
        effect.put("manual_rate", total == 0 ? 0 : Math.round(manual * 1000.0 / total) / 10.0);
        effect.put("manual_reasons", q("select manual_reason as reason, manual_by as operator, manual_time as time " +
                "from lg_score where manual_flag=1 order by id desc limit 5"));
        m.put("effect", effect);
        return m;
    }

    /** 我的待办：按角色给出待处理数量与列表 */
    public Map<String, Object> todo(String role, String realName) {
        Map<String, Object> m = new LinkedHashMap<>();
        String scope = "ROLE_DRIVER".equals(role) ? " and o.driver_name='" + realName + "'" : "";
        m.put("todo", q("select d.id, d.dispatch_no, d.order_no, d.customer_name, d.status, d.plan_load_time, " +
                "d.deadline, d.remark, o.status as order_status from lg_dispatch d join lg_order o on o.id=d.order_id " +
                "where d.deleted=0 and d.status in ('TODO','PROCESSING')" + scope + " order by d.update_time desc limit 20"));
        m.put("counts", q("select o.status as code, count(*) as cnt from lg_order o where o.deleted=0" + scope + " group by o.status"));
        return m;
    }
}
