package com.grad.secondhand.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 入参 DTO 集合。所有写接口的必填项、长度与取值范围在此声明，
 * 由 Jakarta Validation 在进入 Service 前拦截，避免脏数据落库（需求 12 节分层要求）。
 */
public class Dtos {

    /** 登录：登录名 + 密码 */
    public record LoginReq(@NotBlank(message = "登录名不能为空") String username,
                           @NotBlank(message = "密码不能为空") String password) {
    }

    /** 个人资料：姓名、手机、头像 */
    public record ProfileReq(@NotBlank(message = "姓名不能为空") String realName,
                             String phone, String avatar) {
    }

    /** 修改密码 */
    public record PasswordReq(@NotBlank(message = "原密码不能为空") String oldPassword,
                              @NotBlank(message = "新密码不能为空")
                              @Size(min = 6, max = 32, message = "新密码长度 6-32 位") String newPassword) {
    }

    /** 商品审核：通过/驳回 + 原因（驳回必填且不少于 5 字）+ 是否冻结 */
    public record AuditReq(Boolean pass, Boolean freeze, String reason) {
    }

    /** 卖家确认接单：实际成交价允许低于标价 */
    public record OrderConfirmReq(@DecimalMin(value = "0.01", message = "成交价必须大于 0")
                                  @DecimalMax(value = "99999.99", message = "成交价超出范围") BigDecimal dealPrice,
                                  String payType) {
    }

    /** 取消 / 纠纷 / 申诉等动作：原因必填 */
    public record ActionReq(@NotBlank(message = "原因不能为空")
                            @Size(min = 5, max = 200, message = "原因需 5-200 字") String reason,
                            String targetStatus, List<String> attachments) {
    }

    /** 交易评价：1-5 星，标签逗号分隔 */
    /** 订单 id 由路径参数给出，请求体里可不传 */
    public record ReviewReq(Long orderId,
                            @NotNull(message = "评分不能为空")
                            @Min(value = 1, message = "评分最低 1 星")
                            @Max(value = 5, message = "评分最高 5 星") Integer score,
                            String tags,
                            @Size(max = 500, message = "评价内容最多 500 字") String content,
                            Integer anonymous) {
    }

    /** 举报：类型、对象、描述（不少于 10 字）、证据图 */
    public record ReportReq(@NotBlank(message = "举报对象类型不能为空") String bizType,
                            @NotNull(message = "举报对象不能为空") Long bizId,
                            @NotBlank(message = "举报类型不能为空") String reasonType,
                            @NotBlank(message = "问题描述不能为空")
                            @Size(min = 10, max = 500, message = "问题描述需 10-500 字") String description,
                            String evidenceUrls) {
    }

    /** 举报处理：处理结果必填，可同时冻结商品或限制账号 */
    public record ReportHandleReq(@NotBlank(message = "处理结果不能为空")
                                  @Size(min = 5, max = 500, message = "处理结果需 5-500 字") String result,
                                  String targetStatus, Boolean freezeGoods, Boolean limitUser) {
    }

    /** 发布/编辑闲置：标题禁联系方式、描述不少于 10 字、金额范围校验 */
    public record GoodsReq(@NotNull(message = "分类不能为空") Long categoryId,
                           @NotBlank(message = "标题不能为空")
                           @Size(min = 4, max = 120, message = "标题需 4-120 字") String title,
                           @NotBlank(message = "描述不能为空")
                           @Size(min = 10, max = 1000, message = "描述需 10-1000 字") String description,
                           @NotBlank(message = "成色不能为空") String conditionLevel,
                           @NotNull(message = "售价不能为空")
                           @DecimalMin(value = "0.01", message = "售价必须大于 0")
                           @DecimalMax(value = "99999.99", message = "售价超出范围") BigDecimal price,
                           BigDecimal originalPrice,
                           @NotNull(message = "校区不能为空") Integer campusId,
                           Long tradePlaceId,
                           String tradeType,
                           @NotEmpty(message = "至少上传 1 张实拍图") List<String> imageUrls) {
    }

    /** 信用分人工调整 */
    public record CreditAdjustReq(@NotNull(message = "用户不能为空") Long userId,
                                  @NotNull(message = "变动值不能为空") Integer changeValue,
                                  @NotBlank(message = "调整原因不能为空")
                                  @Size(min = 5, max = 200, message = "原因需 5-200 字") String remark) {
    }

    /** 定价规则保存：六项权重，后端再次校验合计 100 */
    public record RuleReq(@NotBlank(message = "规则名称不能为空") String ruleName,
                          @NotNull Integer weightCategory, @NotNull Integer weightCondition,
                          @NotNull Integer weightSupply, @NotNull Integer weightHeat,
                          @NotNull Integer weightCredit, @NotNull Integer weightInfo,
                          String remark) {
    }

    /** 定价结果人工修正：必须有原因，原始结果保留 */
    public record PriceCorrectReq(@NotBlank(message = "修正等级不能为空") String grade,
                                  @NotBlank(message = "修正原因不能为空")
                                  @Size(min = 5, max = 200, message = "修正原因需 5-200 字") String reason) {
    }

    /** 通用状态流转（含 targetStatus 与 reason 的业务动作） */
    @Data
    public static class FlowReq {
        private String targetStatus;
        private String reason;
    }
}
