package com.grad.secondhand.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.entity.SysMessage;
import com.grad.secondhand.entity.SysOperLog;
import com.grad.secondhand.mapper.SysMessageMapper;
import com.grad.secondhand.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 留痕服务：关键写操作写操作日志，业务事件写站内消息。
 * 需求 5.8 要求审核、冻结、信用调整、人工改价等操作日志不可由普通管理员删除，
 * 因此这里只提供写入，不提供删除入口。
 */
@Service
@RequiredArgsConstructor
public class TraceService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysOperLogMapper logMapper;
    private final SysMessageMapper messageMapper;

    /** 写操作日志：模块、类型、业务编号、内容 */
    public void log(String module, String operType, String bizNo, String content) {
        SysOperLog l = new SysOperLog();
        l.setModule(module);
        l.setOperType(operType);
        l.setBizNo(bizNo);
        l.setContent(content);
        l.setOperId(UserContext.userId());
        l.setOperName(UserContext.realName().isEmpty() ? "系统" : UserContext.realName());
        l.setOperTime(LocalDateTime.now().format(FMT));
        l.setIp("127.0.0.1");
        l.setCreateTime(LocalDateTime.now().format(FMT));
        l.setDeleted(0);
        l.setVersion(0);
        logMapper.insert(l);
    }

    /** 站内消息：审核结果、订单超时、纠纷处理、信用变动都通过这里通知 */
    public void notify(Long receiverId, String title, String content, String bizType, Long bizId, String msgType) {
        if (receiverId == null) {
            return;
        }
        SysMessage m = new SysMessage();
        m.setReceiverId(receiverId);
        m.setTitle(title);
        m.setContent(content);
        m.setBizType(bizType);
        m.setBizId(bizId);
        m.setMsgType(msgType);
        m.setReadFlag(0);
        m.setCreateTime(LocalDateTime.now().format(FMT));
        m.setDeleted(0);
        m.setVersion(0);
        messageMapper.insert(m);
    }

    /** 未读数，顶栏红点使用 */
    public long unread(Long userId) {
        return messageMapper.selectCount(Wrappers.<SysMessage>lambdaQuery()
                .eq(SysMessage::getReceiverId, userId)
                .eq(SysMessage::getReadFlag, 0));
    }
}
