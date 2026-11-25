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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 车辆调度任务（对应需求文档 logistics_dispatch_order）。
 *
 * 配车时必须由后端完成冲突校验：车辆核载/容积、车型适配、驾驶员资质与停用状态、车辆时间冲突。
 * 调度变更（改派）必须填写原因，并保留原车辆、原驾驶员和调整时间。
 */
@RestController
@RequestMapping("/api/dispatch")
public class DispatchController extends BaseCrudController<LgDispatch> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LgDispatchMapper mapper;
    private final LgOrderMapper orderMapper;
    private final LgVehicleMapper vehicleMapper;
    private final LgDriverMapper driverMapper;
    private final LgRuleMapper ruleMapper;
    private final ScoreService scoreService;
    private final AuthService authService;

    public DispatchController(LgDispatchMapper mapper, LgOrderMapper orderMapper, LgVehicleMapper vehicleMapper,
                              LgDriverMapper driverMapper, LgRuleMapper ruleMapper,
                              ScoreService scoreService, AuthService authService) {
        this.mapper = mapper;
        this.orderMapper = orderMapper;
        this.vehicleMapper = vehicleMapper;
        this.driverMapper = driverMapper;
        this.ruleMapper = ruleMapper;
        this.scoreService = scoreService;
        this.authService = authService;
    }

    @Override
    protected BaseMapper<LgDispatch> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgDispatch> type() {
        return LgDispatch.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("status", "order_no", "vehicle_id", "driver_id", "plate_no", "dispatcher",
                "plan_load_time", "create_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("dispatch_no", "order_no", "customer_name", "plate_no", "driver_name");
    }

    @Override
    protected String defaultOrder() {
        return "update_time desc, id desc";
    }

    /** 候选车辆评分列表（不落库），调度页用于按推荐分排序 */
    @GetMapping("/candidates")
    public R<List<Map<String, Object>>> candidates(@RequestParam Long orderId) {
        LgOrder o = orderMapper.selectById(orderId);
        if (o == null) {
            throw new BizException(404, "运单不存在");
        }
        return R.ok(scoreService.candidates(o));
    }

    /**
     * 配车：校验核载、容积、车型、驾驶员资质与时间冲突，通过后创建/更新调度任务并把运单推进到「调度配车」。
     */
    @PostMapping("/assign")
    public R<Map<String, Object>> assign(@RequestBody Dtos.AssignReq req) {
        if (!UserContext.isDispatcher()) {
            throw new BizException(403, "只有调度员或车队经理可以配车");
        }
        if (req.orderId() == null || req.vehicleId() == null || req.driverId() == null) {
            throw new BizException(400, "请选择车辆与驾驶员");
        }
        LgOrder o = orderMapper.selectById(req.orderId());
        if (o == null) {
            throw new BizException(404, "运单不存在");
        }
        LgVehicle v = vehicleMapper.selectById(req.vehicleId());
        LgDriver d = driverMapper.selectById(req.driverId());
        if (v == null || d == null) {
            throw new BizException(400, "车辆或驾驶员不存在");
        }

        List<String> errors = new ArrayList<>();
        if ("MAINT".equals(v.getStatus()) || "STOP".equals(v.getStatus())) {
            errors.add("车辆处于维修停用状态，不可派车");
        }
        if (v.getLoadWeight() == null || o.getWeight() == null
                || v.getLoadWeight().doubleValue() < o.getWeight().doubleValue()) {
            errors.add("超载：货物 " + o.getWeight() + " t 超过车辆核载 " + v.getLoadWeight() + " t");
        }
        if (v.getVolume() == null || o.getVolume() == null
                || v.getVolume().doubleValue() < o.getVolume().doubleValue()) {
            errors.add("容积不足：货物 " + o.getVolume() + " m³ 超过车厢 " + v.getVolume() + " m³");
        }
        if (o.getVehicleTypeReq() != null && v.getVehicleType() != null
                && !v.getVehicleType().equals(o.getVehicleTypeReq())) {
            errors.add("车型不符：要求 " + o.getVehicleTypeReq() + "，车辆为 " + v.getVehicleType());
        }
        if ("LEAVE".equals(d.getStatus())) {
            errors.add("驾驶员休假中，不可派车");
        }
        if (d.getLicenseExpire() != null && d.getLicenseExpire().compareTo(LocalDate.now().toString()) < 0) {
            errors.add("驾驶员驾驶证已过期（" + d.getLicenseExpire() + "）");
        }
        // 车辆时间冲突：同车其他未完成任务且计划装货时间在同一天内视为冲突
        List<LgDispatch> busy = mapper.selectList(Wrappers.<LgDispatch>lambdaQuery()
                .eq(LgDispatch::getVehicleId, v.getId())
                .in(LgDispatch::getStatus, List.of("TODO", "PROCESSING"))
                .ne(LgDispatch::getOrderId, o.getId()));
        for (LgDispatch b : busy) {
            if (b.getPlanLoadTime() != null && o.getPlanLoadTime() != null
                    && b.getPlanLoadTime().substring(0, 10).equals(o.getPlanLoadTime().substring(0, 10))) {
                errors.add("时间冲突：车辆 " + v.getPlateNo() + " 在 " + b.getPlanLoadTime()
                        + " 已有任务 " + b.getDispatchNo());
            }
        }
        Long ruleCount = ruleMapper.selectCount(Wrappers.<LgRule>lambdaQuery().eq(LgRule::getStatus, "ACTIVE"));
        if (ruleCount == null || ruleCount == 0) {
            errors.add("未配置生效的配车规则版本");
        }
        if (!errors.isEmpty()) {
            throw new BizException(400, "配车校验未通过：" + String.join("；", errors));
        }

        // 评分留痕
        LgRule rule = scoreService.activeRule();
        Map<String, Object> score = scoreService.scoreVehicle(o, v, rule, true);

        LgDispatch last = mapper.selectOne(Wrappers.<LgDispatch>lambdaQuery()
                .eq(LgDispatch::getOrderId, o.getId()).orderByDesc(LgDispatch::getId).last("limit 1"));
        String now = LocalDateTime.now().format(FMT);
        Long dispatchId;
        if (last != null && "TODO".equals(last.getStatus())) {
            last.setVehicleId(v.getId());
            last.setPlateNo(v.getPlateNo());
            last.setDriverId(d.getId());
            last.setDriverName(d.getName());
            last.setPlanLoadTime(req.planLoadTime() == null ? o.getPlanLoadTime() : req.planLoadTime());
            last.setPlanArriveTime(req.planArriveTime() == null ? o.getPlanArriveTime() : req.planArriveTime());
            last.setStatus("PROCESSING");
            last.setDispatcher(UserContext.realName());
            last.setScoreId((Long) score.get("score_id"));
            last.setRemark("已指派，等待装货");
            last.setUpdateTime(now);
            mapper.updateById(last);
            dispatchId = last.getId();
        } else {
            LgDispatch nd = new LgDispatch();
            nd.setDispatchNo("DD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            nd.setOrderId(o.getId());
            nd.setOrderNo(o.getOrderNo());
            nd.setCustomerName(o.getCustomerName());
            nd.setFromCity(o.getFromCity());
            nd.setToCity(o.getToCity());
            nd.setGoodsDesc(o.getVehicleTypeReq() + " · " + o.getWeight() + "t");
            nd.setVehicleId(v.getId());
            nd.setPlateNo(v.getPlateNo());
            nd.setDriverId(d.getId());
            nd.setDriverName(d.getName());
            nd.setPlanLoadTime(req.planLoadTime() == null ? o.getPlanLoadTime() : req.planLoadTime());
            nd.setPlanArriveTime(req.planArriveTime() == null ? o.getPlanArriveTime() : req.planArriveTime());
            nd.setStatus("PROCESSING");
            nd.setDispatcher(UserContext.realName());
            nd.setScoreId((Long) score.get("score_id"));
            nd.setDeadline(o.getPlanLoadTime());
            nd.setRemark("已指派，等待装货");
            nd.setDeleted(0);
            nd.setCreateTime(now);
            nd.setUpdateTime(now);
            mapper.insert(nd);
            dispatchId = nd.getId();
        }

        LgOrder ou = new LgOrder();
        ou.setId(o.getId());
        ou.setVehicleId(v.getId());
        ou.setPlateNo(v.getPlateNo());
        ou.setDriverId(d.getId());
        ou.setDriverName(d.getName());
        if ("CREATED".equals(o.getStatus())) {
            ou.setStatus("DISPATCHED");
        }
        ou.setUpdateTime(now);
        orderMapper.updateById(ou);

        LgVehicle vu = new LgVehicle();
        vu.setId(v.getId());
        vu.setStatus("RUNNING");
        vu.setUpdateTime(now);
        vehicleMapper.updateById(vu);

        authService.writeLog(UserContext.username(), "调度配车", "lg_dispatch:" + dispatchId,
                o.getOrderNo() + " 指派 " + v.getPlateNo() + " / " + d.getName()
                        + "，评分 " + score.get("total_score"), null);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("dispatch_id", dispatchId);
        res.put("score", score);
        return R.ok(res);
    }

    /** 改派：必须填写原因，保留原车辆原驾驶员与调整时间 */
    @PostMapping("/{id}/change")
    public R<Void> change(@PathVariable Long id, @RequestBody Dtos.AssignReq req) {
        if (!UserContext.isDispatcher()) {
            throw new BizException(403, "只有调度员或车队经理可以改派");
        }
        if (req.reason() == null || req.reason().trim().length() < 5) {
            throw new BizException(400, "调度变更必须填写原因，且不少于 5 个字");
        }
        LgDispatch d = mapper.selectById(id);
        if (d == null) {
            throw new BizException(404, "调度任务不存在");
        }
        LgVehicle v = vehicleMapper.selectById(req.vehicleId());
        if (v == null) {
            throw new BizException(400, "车辆不存在");
        }
        LgDispatch upd = new LgDispatch();
        upd.setId(id);
        upd.setOriginVehicle(d.getPlateNo());
        upd.setOriginDriver(d.getDriverName());
        upd.setChangeReason(req.reason().trim());
        upd.setChangeTime(LocalDateTime.now().format(FMT));
        upd.setVehicleId(v.getId());
        upd.setPlateNo(v.getPlateNo());
        if (req.driverId() != null) {
            LgDriver dr = driverMapper.selectById(req.driverId());
            if (dr != null) {
                upd.setDriverId(dr.getId());
                upd.setDriverName(dr.getName());
            }
        }
        upd.setUpdateTime(LocalDateTime.now().format(FMT));
        mapper.updateById(upd);

        LgOrder ou = new LgOrder();
        ou.setId(d.getOrderId());
        ou.setVehicleId(v.getId());
        ou.setPlateNo(v.getPlateNo());
        if (req.driverId() != null) {
            ou.setDriverId(req.driverId());
        }
        ou.setUpdateTime(LocalDateTime.now().format(FMT));
        orderMapper.updateById(ou);

        authService.writeLog(UserContext.username(), "调度改派", "lg_dispatch:" + id,
                "原车 " + d.getPlateNo() + " → " + v.getPlateNo() + "，原因：" + req.reason(), null);
        return R.ok();
    }
}
