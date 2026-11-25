package com.grad.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grad.secondhand.common.BizException;
import com.grad.secondhand.common.PageResult;
import com.grad.secondhand.common.R;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.entity.CampusPlace;
import com.grad.secondhand.entity.Category;
import com.grad.secondhand.entity.Goods;
import com.grad.secondhand.mapper.CampusPlaceMapper;
import com.grad.secondhand.mapper.CategoryMapper;
import com.grad.secondhand.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 基础数据：商品分类（两级）与校区面交点。
 * 分类删除前校验是否被商品引用；停用分类不影响历史商品展示（需求 5.6）。
 */
@RestController
@RequiredArgsConstructor
public class BaseDataController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CategoryMapper categoryMapper;
    private final CampusPlaceMapper placeMapper;
    private final GoodsMapper goodsMapper;

    // ---------------------------------------------------------------- 分类
    @GetMapping("/api/category/page")
    public R<PageResult<Category>> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "20") long size,
                                        @RequestParam(required = false) Long parent_id,
                                        @RequestParam(required = false) Integer status) {
        QueryWrapper<Category> w = new QueryWrapper<>();
        w.eq("deleted", 0);
        if (parent_id != null) {
            w.eq("parent_id", parent_id);
        }
        if (status != null) {
            w.eq("status", status);
        }
        w.orderByAsc("parent_id").orderByAsc("sort_no");
        IPage<Category> p = categoryMapper.selectPage(new Page<>(page, size), w);
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    /** 分类树：门户分类快捷入口与发布页级联共用 */
    @GetMapping("/api/category/tree")
    public R<List<Map<String, Object>>> tree() {
        List<Category> all = categoryMapper.selectList(new QueryWrapper<Category>()
                .eq("deleted", 0).eq("status", 1).orderByAsc("sort_no"));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Category root : all) {
            if (root.getParentId() != null && root.getParentId() == 0) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", root.getId());
                m.put("name", root.getName());
                m.put("icon", root.getIcon());
                List<Map<String, Object>> children = new ArrayList<>();
                for (Category c : all) {
                    if (root.getId().equals(c.getParentId())) {
                        Map<String, Object> cm = new LinkedHashMap<>();
                        cm.put("id", c.getId());
                        cm.put("name", c.getName());
                        children.add(cm);
                    }
                }
                m.put("children", children);
                list.add(m);
            }
        }
        return R.ok(list);
    }

    @PostMapping("/api/category")
    public R<Void> saveCategory(@RequestBody Map<String, Object> body) {
        if (!UserContext.isOperator()) {
            throw new BizException(4030, "只有运营人员可以维护分类");
        }
        Category c = new Category();
        Object id = body.get("id");
        c.setParentId(body.get("parent_id") == null ? 0L : Long.valueOf(body.get("parent_id").toString()));
        Long parentId = c.getParentId();
        if (parentId != 0) {
            Category parent = categoryMapper.selectById(parentId);
            if (parent == null) {
                throw new BizException(4000, "上级分类不存在");
            }
            if (parent.getParentId() != null && parent.getParentId() != 0) {
                throw new BizException(4001, "分类最多两级");
            }
        }
        c.setCategoryCode((String) body.get("category_code"));
        c.setName((String) body.get("name"));
        c.setIcon((String) body.get("icon"));
        c.setSortNo(body.get("sort_no") == null ? 0 : Integer.valueOf(body.get("sort_no").toString()));
        c.setStatus(body.get("status") == null ? 1 : Integer.valueOf(body.get("status").toString()));
        c.setIsHot(body.get("is_hot") == null ? 0 : Integer.valueOf(body.get("is_hot").toString()));
        c.setRemark((String) body.get("remark"));
        if (c.getCategoryCode() == null || !c.getCategoryCode().matches("[A-Z_]+")) {
            throw new BizException(4000, "分类编码只能使用大写字母与下划线");
        }
        if (id == null) {
            c.setCreateTime(LocalDateTime.now().format(FMT));
            c.setDeleted(0);
            c.setVersion(0);
            categoryMapper.insert(c);
        } else {
            c.setId(Long.valueOf(id.toString()));
            categoryMapper.updateById(c);
        }
        return R.ok();
    }

    @DeleteMapping("/api/category/{id}")
    public R<Void> deleteCategory(@PathVariable Long id) {
        if (!UserContext.isOperator()) {
            throw new BizException(4030, "只有运营人员可以删除分类");
        }
        Long refs = goodsMapper.selectCount(new QueryWrapper<Goods>().eq("category_id", id).eq("deleted", 0));
        if (refs != null && refs > 0) {
            throw new BizException(4001, "该分类已被 " + refs + " 件商品引用，不能删除，请改为停用");
        }
        Long child = categoryMapper.selectCount(new QueryWrapper<Category>().eq("parent_id", id));
        if (child != null && child > 0) {
            throw new BizException(4001, "存在二级分类，请先删除二级分类");
        }
        categoryMapper.deleteById(id);
        return R.ok();
    }

    // ---------------------------------------------------------------- 校区面交点
    @GetMapping("/api/campus-place/page")
    public R<PageResult<CampusPlace>> placePage(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) Integer campus_id,
                                                @RequestParam(required = false) Integer status) {
        QueryWrapper<CampusPlace> w = new QueryWrapper<>();
        w.eq("deleted", 0);
        if (campus_id != null) {
            w.eq("campus_id", campus_id);
        }
        if (status != null) {
            w.eq("status", status);
        }
        w.orderByAsc("campus_id").orderByAsc("id");
        IPage<CampusPlace> p = placeMapper.selectPage(new Page<>(page, size), w);
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    /** 下单可选面交点：只返回启用中的，且电话脱敏 */
    @GetMapping("/api/campus-place/list")
    public R<List<CampusPlace>> placeList(@RequestParam(required = false) Integer campus_id) {
        QueryWrapper<CampusPlace> w = new QueryWrapper<CampusPlace>().eq("deleted", 0).eq("status", 1);
        if (campus_id != null) {
            w.eq("campus_id", campus_id);
        }
        List<CampusPlace> list = placeMapper.selectList(w);
        for (CampusPlace p : list) {
            if (p.getManagerPhone() != null && p.getManagerPhone().length() == 11) {
                p.setManagerPhone(p.getManagerPhone().substring(0, 3) + "****" + p.getManagerPhone().substring(7));
            }
        }
        return R.ok(list);
    }

    @PostMapping("/api/campus-place")
    public R<Void> savePlace(@RequestBody Map<String, Object> body) {
        if (!UserContext.isOperator()) {
            throw new BizException(4030, "只有运营人员可以维护面交点");
        }
        CampusPlace p = new CampusPlace();
        Object id = body.get("id");
        p.setCampusId(Integer.valueOf(body.get("campus_id").toString()));
        p.setCampusName((String) body.get("campus_name"));
        p.setPlaceName((String) body.get("place_name"));
        p.setAddressDesc((String) body.get("address_desc"));
        p.setOpenStart(body.get("open_start") == null ? "08:00:00" : body.get("open_start").toString());
        p.setOpenEnd(body.get("open_end") == null ? "21:00:00" : body.get("open_end").toString());
        if (p.getOpenStart().compareTo(p.getOpenEnd()) >= 0) {
            throw new BizException(4000, "开放结束时间必须晚于开始时间");
        }
        p.setManagerName((String) body.get("manager_name"));
        p.setManagerPhone((String) body.get("manager_phone"));
        p.setStatus(body.get("status") == null ? 1 : Integer.valueOf(body.get("status").toString()));
        if (id == null) {
            p.setCreateTime(LocalDateTime.now().format(FMT));
            p.setDeleted(0);
            p.setVersion(0);
            placeMapper.insert(p);
        } else {
            p.setId(Long.valueOf(id.toString()));
            placeMapper.updateById(p);
        }
        return R.ok();
    }

    @DeleteMapping("/api/campus-place/{id}")
    public R<Void> deletePlace(@PathVariable Long id) {
        if (!UserContext.isOperator()) {
            throw new BizException(4030, "只有运营人员可以删除面交点");
        }
        Long refs = goodsMapper.selectCount(new QueryWrapper<Goods>().eq("trade_place_id", id).eq("deleted", 0));
        if (refs != null && refs > 0) {
            throw new BizException(4001, "该面交点已被 " + refs + " 件商品引用，请改为停用");
        }
        placeMapper.deleteById(id);
        return R.ok();
    }
}
