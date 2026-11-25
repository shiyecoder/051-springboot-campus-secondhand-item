package com.grad.logistics.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grad.logistics.common.BizException;
import com.grad.logistics.common.PageResult;
import com.grad.logistics.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 通用 CRUD 控制器：服务端分页 + 关键字模糊 + 白名单字段精确筛选 + 排序 + 增删改。
 * 子类只需要声明实体类型、Mapper、可筛选列与可模糊列，避免 33 张表重复写同样的代码。
 * 所有筛选列走白名单，杜绝前端传任意列名拼 SQL。
 */
public abstract class BaseCrudController<T> {

    @Autowired
    protected ObjectMapper objectMapper;

    protected abstract BaseMapper<T> mapper();

    protected abstract Class<T> type();

    /** 允许做等值筛选的列（数据库列名） */
    protected abstract List<String> filterColumns();

    /** 允许做关键字模糊的列 */
    protected abstract List<String> likeColumns();

    /** 默认排序 */
    protected String defaultOrder() {
        return "create_time desc, id desc";
    }

    protected QueryWrapper<T> buildWrapper(Map<String, String> params) {
        QueryWrapper<T> w = new QueryWrapper<>();
        String keyword = trimToNull(params.get("keyword"));
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            String v = trimToNull(e.getValue());
            if (v == null || List.of("page", "size", "keyword", "sort", "order").contains(k)) {
                continue;
            }
            if (filterColumns().contains(k)) {
                if ("status_in".equals(k)) {
                    w.in(k.replace("_in", ""), Arrays.asList(v.split(",")));
                } else {
                    w.eq(k, v);
                }
            } else if (k.endsWith("_from") && filterColumns().contains(k.substring(0, k.length() - 5))) {
                w.ge(k.substring(0, k.length() - 5), v);
            } else if (k.endsWith("_to") && filterColumns().contains(k.substring(0, k.length() - 3))) {
                w.le(k.substring(0, k.length() - 3), v);
            }
        }
        if (keyword != null && !likeColumns().isEmpty()) {
            w.and(x -> {
                for (int i = 0; i < likeColumns().size(); i++) {
                    if (i == 0) {
                        x.like(likeColumns().get(i), keyword);
                    } else {
                        x.or().like(likeColumns().get(i), keyword);
                    }
                }
            });
        }
        applyScope(w);
        String sort = trimToNull(params.get("sort"));
        if (sort != null && filterColumns().contains(sort)) {
            boolean asc = "asc".equalsIgnoreCase(params.getOrDefault("order", "desc"));
            w.orderBy(true, asc, sort);
        } else if (!defaultOrder().isEmpty()) {
            for (String seg : defaultOrder().split(",")) {
                String[] kv = seg.trim().split(" ");
                w.orderBy(true, kv.length > 1 && "asc".equalsIgnoreCase(kv[1]), kv[0]);
            }
        }
        return w;
    }

    /** 数据范围钩子：默认不限制，运单类子类按角色限本人/本单位 */
    protected void applyScope(QueryWrapper<T> wrapper) {
    }

    protected static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @GetMapping("/page")
    public R<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam Map<String, String> params) {
        Page<Map<String, Object>> p = new Page<>(Math.max(1, page), Math.min(Math.max(1, size), 100));
        IPage<Map<String, Object>> result = mapper().selectMapsPage(p, buildWrapper(params));
        return R.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> get(@PathVariable Long id) {
        T entity = mapper().selectById(id);
        if (entity == null) {
            throw new BizException(404, "记录不存在或已删除");
        }
        return R.ok(toMap(entity));
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> toMap(T entity) {
        return objectMapper.convertValue(entity, Map.class);
    }

    @PostMapping
    public R<Long> save(@RequestBody Map<String, Object> body) {
        body.remove("id");
        T entity = objectMapper.convertValue(body, type());
        validBeforeSave(entity, true);
        mapper().insert(entity);
        afterSave(entity, true);
        return R.ok(null);
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        body.remove("id");
        T entity = objectMapper.convertValue(body, type());
        setId(entity, id);
        validBeforeSave(entity, false);
        mapper().updateById(entity);
        afterSave(entity, false);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        mapper().deleteById(id);
        return R.ok();
    }

    /** 由子类通过反射写入 ID（实体统一有 id 字段） */
    protected void setId(T entity, Long id) {
        try {
            entity.getClass().getMethod("setId", Long.class).invoke(entity, id);
        } catch (Exception e) {
            throw new BizException(500, "主键写入失败");
        }
    }

    /** 保存前校验钩子 */
    protected void validBeforeSave(T entity, boolean create) {
    }

    /** 保存后钩子（例如写日志、回填冗余字段） */
    protected void afterSave(T entity, boolean create) {
    }
}
