package com.grad.logistics.controller;

import com.grad.logistics.entity.SysMessage;
import com.grad.logistics.mapper.SysMessageMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 站内消息 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/message")
public class SysMessageController extends BaseCrudController<SysMessage> {

    private final SysMessageMapper mapper;

    public SysMessageController(SysMessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<SysMessage> mapper() {
        return mapper;
    }

    @Override
    protected Class<SysMessage> type() {
        return SysMessage.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("msg_type", "is_read", "receiver");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("title", "biz_no");
    }
}
