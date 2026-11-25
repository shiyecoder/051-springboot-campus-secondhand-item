package com.grad.secondhand.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grad.secondhand.common.R;
import com.grad.secondhand.entity.Category;
import com.grad.secondhand.entity.Goods;
import com.grad.secondhand.entity.Report;
import com.grad.secondhand.entity.SysNotice;
import com.grad.secondhand.mapper.CategoryMapper;
import com.grad.secondhand.mapper.GoodsMapper;
import com.grad.secondhand.mapper.ReportMapper;
import com.grad.secondhand.mapper.SysNoticeMapper;
import com.grad.secondhand.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 看板与门户首页数据。门户首页四个区块（轮播/今日上新/低价专区/同校好物）
 * 与运营看板共用同一数据源，保证统计总数与明细一致。
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;
    private final SysNoticeMapper noticeMapper;
    private final ReportMapper reportMapper;

    /** 运营首页：支持近 7 天 / 30 天 / 本学期切换 */
    @GetMapping("/overview")
    public R<Map<String, Object>> overview(@RequestParam(defaultValue = "30") int days) {
        return R.ok(dashboardService.overview(days));
    }

    /** 运营统计页 */
    @GetMapping("/analysis")
    public R<Map<String, Object>> analysis() {
        return R.ok(dashboardService.analysis());
    }

    /** 门户首页：公告、热门分类、今日上新、低价专区、同校好物 */
    @GetMapping("/portal")
    public R<Map<String, Object>> portal(@RequestParam(required = false) Integer campusId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("notices", noticeMapper.selectList(Wrappers.<SysNotice>lambdaQuery()
                .eq(SysNotice::getStatus, "已发布").orderByDesc(SysNotice::getPublishTime).last("limit 3")));

        List<Map<String, Object>> hotCats = new ArrayList<>();
        for (Category c : categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .eq(Category::getParentId, 0).eq(Category::getStatus, 1).orderByAsc(Category::getSortNo))) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("count", goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery()
                    .eq(Goods::getStatus, "在售")
                    .inSql(Goods::getCategoryId, "select id from category where parent_id = " + c.getId())));
            hotCats.add(m);
        }
        data.put("categories", hotCats);

        data.put("todayNew", goodsMapper.selectList(Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getStatus, "在售")
                .ge(Goods::getCreateTime, java.time.LocalDate.now().minusDays(2).toString())
                .orderByDesc(Goods::getCreateTime).last("limit 8")));
        data.put("lowPrice", goodsMapper.selectList(Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getStatus, "在售").le(Goods::getPrice, 60)
                .orderByAsc(Goods::getPrice).last("limit 8")));
        data.put("sameCampus", campusId == null ? goodsMapper.selectList(Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getStatus, "在售").orderByDesc(Goods::getViewCount).last("limit 8"))
                : goodsMapper.selectList(Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getStatus, "在售").eq(Goods::getCampusId, campusId)
                .orderByDesc(Goods::getViewCount).last("limit 8")));
        data.put("stats", dashboardService.siteStats());
        return R.ok(data);
    }

    /** 待办：待审核商品、待受理举报、纠纷中订单，运营首页右侧使用 */
    @GetMapping("/todo")
    public R<Map<String, Object>> todo() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("pendingGoods", goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery().eq(Goods::getStatus, "待审核")));
        m.put("pendingReports", reportMapper.selectCount(Wrappers.<Report>lambdaQuery().eq(Report::getStatus, "待受理")));
        m.put("disputes", reportMapper.selectCount(Wrappers.<Report>lambdaQuery().eq(Report::getStatus, "处理中")));
        m.put("appeals", reportMapper.selectCount(Wrappers.<Report>lambdaQuery().eq(Report::getAppealStatus, "申诉中")));
        return R.ok(m);
    }
}
