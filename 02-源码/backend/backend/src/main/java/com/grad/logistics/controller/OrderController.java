package com.grad.logistics.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grad.logistics.common.BizException;
import com.grad.logistics.common.R;
import com.grad.logistics.common.UserContext;
import com.grad.logistics.dto.Dtos;
import com.grad.logistics.entity.*;
import com.grad.logistics.mapper.*;
import com.grad.logistics.service.AuthService;
import com.grad.logistics.service.ScoreService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 运单（核心主数据）。
 *
 * 主流程：运单创建 CREATED → 调度配车 DISPATCHED → 装货发车 LOADED → 在途节点 SHIPPING
 *        → 到达签收 ARRIVED → 签收回单 SIGNED → 费用核对 FEE_CHECK → 已完成 DONE。
 * 所有状态变更由后端校验：不允许越级、退回/取消必须填写不少于 5 个字的原因，
 * 未完成签收复核的运单不能进入最终对账完成状态（需求文档 5.3）。
 */
@RestController
@RequestMapping("/api/order")
public class OrderController extends BaseCrudController<LgOrder> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> FLOW = List.of(
            "CREATED", "DISPATCHED", "LOADED", "SHIPPING", "ARRIVED", "SIGNED", "FEE_CHECK", "DONE");
    private static final Map<String, String> STATUS_CN = Map.of(
            "CREATED", "运单创建", "DISPATCHED", "调度配车", "LOADED", "装货发车", "SHIPPING", "在途节点",
            "ARRIVED", "到达签收", "SIGNED", "签收回单", "FEE_CHECK", "费用核对", "DONE", "已完成",
            "EXCEPTION", "异常", "CANCELED", "已取消");

    private final LgOrderMapper mapper;
    private final LgNodeMapper nodeMapper;
    private final LgFeeMapper feeMapper;
    private final LgReceiptMapper receiptMapper;
    private final LgDispatchMapper dispatchMapper;
    private final LgSignMapper signMapper;
    private final LgLoadMapper loadMapper;
    private final LgVehicleMapper vehicleMapper;
    private final LgDriverMapper driverMapper;
    private final LgExceptionMapper exceptionMapper;
    private final LgReviewMapper reviewMapper;
    private final ScoreService scoreService;
    private final AuthService authService;

    public OrderController(LgOrderMapper mapper, LgNodeMapper nodeMapper, LgFeeMapper feeMapper,
                           LgReceiptMapper receiptMapper, LgDispatchMapper dispatchMapper, LgSignMapper signMapper,
                           LgLoadMapper loadMapper, LgVehicleMapper vehicleMapper, LgDriverMapper driverMapper,
                           LgExceptionMapper exceptionMapper, LgReviewMapper reviewMapper,
                           ScoreService scoreService, AuthService authService) {
        this.mapper = mapper;
        this.nodeMapper = nodeMapper;
        this.feeMapper = feeMapper;
        this.receiptMapper = receiptMapper;
        this.dispatchMapper = dispatchMapper;
        this.signMapper = signMapper;
        this.loadMapper = loadMapper;
        this.vehicleMapper = vehicleMapper;
        this.driverMapper = driverMapper;
        this.exceptionMapper = exceptionMapper;
        this.reviewMapper = reviewMapper;
        this.scoreService = scoreService;
        this.authService = authService;
    }

    @Override
    protected BaseMapper<LgOrder> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgOrder> type() {
        return LgOrder.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("status", "customer_id", "goods_type", "plate_no", "driver_name",
                "vehicle_type_req", "exception_flag", "demand_id", "create_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("order_no", "customer_name", "goods_name", "driver_name");
    }

    /**
     * 数据范围：客户只看本单位运单，驾驶员只看本人任务，调度/财务按部门，经理全域。
     */
    @Override
    protected void applyScope(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LgOrder> w) {
        if (UserContext.isCustomer()) {
            w.eq("customer_name", UserContext.orgName());
        } else if (UserContext.isDriver()) {
            w.eq("driver_name", UserContext.realName());
        }
    }

    /** 新建运单：编号唯一、必填校验，自动生成运单编号与状态 */
    @Override
    protected void validBeforeSave(LgOrder o, boolean create) {
        if (create) {
            if (o.getCustomerName() == null || o.getCustomerName().isBlank()) {
                throw new BizException(400, "发货方不能为空");
            }
            if (o.getReceiverName() == null || o.getReceiverName().isBlank()) {
                throw new BizException(400, "收货方不能为空");
            }
            if (o.getWeight() == null || o.getWeight().doubleValue() < 0) {
                throw new BizException(400, "重量不得为负数");
            }
            if (o.getOrderNo() == null || o.getOrderNo().isBlank()) {
                o.setOrderNo("WB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            }
            o.setOrderNo(o.getOrderNo().trim());
            Long dup = mapper.selectCount(Wrappers.<LgOrder>lambdaQuery().eq(LgOrder::getOrderNo, o.getOrderNo()));
            if (dup != null && dup > 0) {
                throw new BizException(400, "运单编号已存在：" + o.getOrderNo());
            }
            if (o.getStatus() == null) {
                o.setStatus("CREATED");
            }
            if (o.getCreateBy() == null) {
                o.setCreateBy(UserContext.realName());
            }
            if (o.getCreateTime() == null) {
                o.setCreateTime(LocalDateTime.now().format(FMT));
            }
            if (o.getExceptionFlag() == null) {
                o.setExceptionFlag("NORMAL");
            }
        }
        o.setUpdateTime(LocalDateTime.now().format(FMT));
    }

    /** 运单详情：基础信息 + 节点 + 费用 + 回单 + 调度 + 签收 + 评分 + 异常 */
    @GetMapping("/{id}/detail")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        LgOrder o = mapper.selectById(id);
        if (o == null) {
            throw new BizException(404, "运单不存在");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("order", o);
        data.put("status_name", STATUS_CN.getOrDefault(o.getStatus(), o.getStatus()));
        data.put("nodes", nodeMapper.selectList(Wrappers.<LgNode>lambdaQuery()
                .eq(LgNode::getOrderId, id).orderByDesc(LgNode::getNodeTime)));
        data.put("fees", feeMapper.selectList(Wrappers.<LgFee>lambdaQuery()
                .eq(LgFee::getOrderId, id).orderByDesc(LgFee::getId)));
        data.put("dispatches", dispatchMapper.selectList(Wrappers.<LgDispatch>lambdaQuery()
                .eq(LgDispatch::getOrderId, id).orderByDesc(LgDispatch::getId)));
        data.put("receipts", receiptMapper.selectList(Wrappers.<LgReceipt>lambdaQuery()
                .eq(LgReceipt::getOrderId, id).orderByDesc(LgReceipt::getId)));
        data.put("signs", signMapper.selectList(Wrappers.<LgSign>lambdaQuery()
                .eq(LgSign::getOrderId, id).orderByDesc(LgSign::getId)));
        data.put("loads", loadMapper.selectList(Wrappers.<LgLoad>lambdaQuery()
                .eq(LgLoad::getOrderId, id).orderByDesc(LgLoad::getId)));
        data.put("exceptions", exceptionMapper.selectList(Wrappers.<LgException>lambdaQuery()
                .eq(LgException::getOrderNo, o.getOrderNo()).orderByDesc(LgException::getId)));
        data.put("reviews", reviewMapper.selectList(Wrappers.<LgReview>lambdaQuery()
                .eq(LgReview::getOrderId, id)));
        data.put("score", scoreService.latestScore(id));
        return R.ok(data);
    }

    /** 状态流转：只允许推进一级，退回/取消/标记异常必须填原因（≥5 字） */
    @PostMapping("/{id}/transition")
    public R<Map<String, Object>> transition(@PathVariable Long id, @RequestBody Dtos.TransitionReq req) {
        LgOrder o = mapper.selectById(id);
        if (o == null) {
            throw new BizException(404, "运单不存在");
        }
        String target = req.target();
        if (target == null || target.isBlank()) {
            throw new BizException(400, "请指定目标状态");
        }
        String cur = o.getStatus();
        int ci = FLOW.indexOf(cur);
        int ti = FLOW.indexOf(target);
        String reason = req.reason() == null ? "" : req.reason().trim();

        if ("CANCELED".equals(target) || "EXCEPTION".equals(target)) {
            if (reason.length() < 5) {
                throw new BizException(400, "取消或标记异常必须填写原因，且不少于 5 个字");
            }
        } else if (ti < 0) {
            throw new BizException(400, "非法的目标状态：" + target);
        } else if (ti < ci) {
            if (reason.length() < 5) {
                throw new BizException(400, "退回上一阶段必须填写原因，且不少于 5 个字");
            }
        } else if (ti > ci + 1) {
            throw new BizException(400, "不允许越级流转：当前「" + STATUS_CN.getOrDefault(cur, cur)
                    + "」，只能推进到「" + STATUS_CN.getOrDefault(FLOW.get(Math.min(ci + 1, FLOW.size() - 1)), "") + "」");
        }

        if ("DONE".equals(target)) {
            Long ok = receiptMapper.selectCount(Wrappers.<LgReceipt>lambdaQuery()
                    .eq(LgReceipt::getOrderId, id).in(LgReceipt::getStatus, List.of("CHECKED", "ARCHIVED")));
            if (ok == null || ok == 0) {
                throw new BizException(400, "未完成签收复核的运单不能进入最终对账完成状态");
            }
        }

        LgOrder upd = new LgOrder();
        upd.setId(id);
        upd.setStatus(target);
        upd.setUpdateTime(LocalDateTime.now().format(FMT));
        if ("ARRIVED".equals(target)) {
            upd.setActualArriveTime(LocalDateTime.now().format(FMT));
        }
        if ("EXCEPTION".equals(target)) {
            upd.setExceptionFlag("OTHER");
        }
        if ("DONE".equals(target)) {
            upd.setExceptionFlag("NORMAL");
        }
        mapper.updateById(upd);

        // 流程图：装车、在途、到达、签收都要留节点，客户门户据此展示进度
        String nodeType = switch (target) {
            case "LOADED" -> "LOAD";
            case "SHIPPING" -> "TRANSIT";
            case "ARRIVED" -> "ARRIVE";
            case "SIGNED" -> "SIGN";
            default -> null;
        };
        if (nodeType != null) {
            LgNode n = new LgNode();
            n.setNodeNo("JD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            n.setOrderId(id);
            n.setOrderNo(o.getOrderNo());
            n.setNodeType(nodeType);
            n.setNodeName(STATUS_CN.getOrDefault(target, target));
            n.setPlace(req.place() == null ? o.getToAddress() : req.place());
            n.setNodeTime(LocalDateTime.now().format(FMT));
            n.setDescription(req.description() == null ? req.reason() : req.description());
            n.setReporter(UserContext.realName());
            n.setCreateTime(LocalDateTime.now().format(FMT));
            nodeMapper.insert(n);
        }

        // 同步调度任务状态
        LgDispatch d = dispatchMapper.selectOne(Wrappers.<LgDispatch>lambdaQuery()
                .eq(LgDispatch::getOrderId, id).orderByDesc(LgDispatch::getId).last("limit 1"));
        if (d != null) {
            String ds = switch (target) {
                case "EXCEPTION" -> "EXCEPTION";
                case "DONE", "FEE_CHECK" -> "DONE";
                case "CANCELED" -> "CANCELED";
                case "CREATED" -> "TODO";
                default -> "PROCESSING";
            };
            LgDispatch du = new LgDispatch();
            du.setId(d.getId());
            du.setStatus(ds);
            du.setRemark(STATUS_CN.getOrDefault(target, target));
            du.setUpdateTime(LocalDateTime.now().format(FMT));
            dispatchMapper.updateById(du);
        }

        authService.writeLog(UserContext.username(), "运单状态流转", "lg_order:" + id,
                o.getOrderNo() + " " + STATUS_CN.getOrDefault(cur, cur) + " → " + STATUS_CN.getOrDefault(target, target)
                        + (reason.isEmpty() ? "" : "，原因：" + reason), null);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", target);
        res.put("status_name", STATUS_CN.getOrDefault(target, target));
        return R.ok(res);
    }

    /** 执行「容量与时间窗联合配车」计算并落库，返回明细依据 */
    @PostMapping("/{id}/evaluate")
    public R<Map<String, Object>> evaluate(@PathVariable Long id) {
        LgOrder o = mapper.selectById(id);
        if (o == null) {
            throw new BizException(404, "运单不存在");
        }
        if (!UserContext.isDispatcher()) {
            throw new BizException(403, "只有调度员或车队经理可以执行配车计算");
        }
        Map<String, Object> r = scoreService.evaluate(o);
        authService.writeLog(UserContext.username(), "执行配车计算", "lg_order:" + id,
                "规则 " + r.get("rule_version") + "，总分 " + r.get("total_score") + "，结果 " + r.get("result_level"), null);
        return R.ok(r);
    }

    /** 人工修正配车结果，必须填写原因；原始计算结果保留 */
    @PostMapping("/score/{scoreId}/manual")
    public R<Void> manual(@PathVariable Long scoreId, @RequestBody Dtos.ManualReq req) {
        if (!UserContext.isManager()) {
            throw new BizException(403, "只有车队经理可以人工修正配车结果");
        }
        scoreService.manual(scoreId, req.plateNo(), req.reason(), UserContext.realName());
        authService.writeLog(UserContext.username(), "人工修正配车", "lg_score:" + scoreId,
                "改为 " + req.plateNo() + "，原因：" + req.reason(), null);
        return R.ok();
    }

    /** 运单下拉选项（调度新建时选择车辆/驾驶员） */
    @GetMapping("/options")
    public R<Map<String, Object>> options() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("vehicles", vehicleMapper.selectList(Wrappers.<LgVehicle>lambdaQuery()
                .ne(LgVehicle::getStatus, "STOP").ne(LgVehicle::getStatus, "MAINT")));
        m.put("drivers", driverMapper.selectList(Wrappers.<LgDriver>lambdaQuery()
                .ne(LgDriver::getStatus, "LEAVE")));
        return R.ok(m);
    }
}
