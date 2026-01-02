package com.campus.water.service;

import com.campus.water.entity.Alert;
import com.campus.water.entity.MessagePush;
import com.campus.water.entity.Repairman;
import com.campus.water.mapper.MessagePushRepository;
import com.campus.water.mapper.RepairmanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertPushService {

    private final MessagePushRepository messagePushRepository;
    private final RepairmanRepository repairmanRepository; // 维修人员数据访问

    /**
     * 推送告警消息给目标用户（根据告警级别和区域分配）
     * @param alert 告警实体
     */
    @Transactional
    public void pushAlertMessage(Alert alert) {
        // 1. 确定推送目标（紧急告警推送给管理员+区域维修人员；一般告警仅推送给区域维修人员）
        String alertType = alert.getAlertType();
        String areaId = alert.getAreaId();
        boolean isEmergency = alert.getAlertLevel().getPriority() >= 3; // 紧急级别（error/critical）

        // 2. 构建消息内容
        MessagePush message = new MessagePush();
        message.setTitle(String.format("[%s告警] %s",
                alert.getAlertLevel().getLevelName(), alertType));
        message.setContent(alert.getAlertMessage());
        message.setMessageType("ALERT");
        message.setRelatedId(alert.getAlertId().toString()); // 关联告警ID
        message.setPushTime(LocalDateTime.now());

        // 3. 推送区域维修人员（所有级别都推送）
        List<Repairman> areaRepairmen = repairmanRepository.findByAreaId(areaId);
        for (Repairman repairman : areaRepairmen) {
            MessagePush repairmanMsg = copyMessage(message);
            repairmanMsg.setRepairmanId(repairman.getRepairmanId());
            repairmanMsg.setUserId(repairman.getRepairmanId());
            repairmanMsg.setUserType("REPAIRMAN");
            messagePushRepository.save(repairmanMsg);
        }
        log.info("告警推送区域维修人员完成 | 告警ID：{} | 区域：{} | 人数：{}",
                alert.getAlertId(), areaId, areaRepairmen.size());

        // 4. 紧急告警额外推送管理员（假设管理员userType为"ADMIN"，可扩展查询逻辑）
        if (isEmergency) {
            MessagePush adminMsg = copyMessage(message);
            adminMsg.setAdminId("ADMIN001"); // 实际应从管理员表查询
            adminMsg.setUserId("ADMIN001");
            adminMsg.setUserType("ADMIN");
            messagePushRepository.save(adminMsg);
            log.info("紧急告警推送管理员完成 | 告警ID：{}", alert.getAlertId());
        }
    }

    // 复制消息基础信息（避免重复代码）
    private MessagePush copyMessage(MessagePush source) {
        MessagePush target = new MessagePush();
        target.setTitle(source.getTitle());
        target.setContent(source.getContent());
        target.setMessageType(source.getMessageType());
        target.setRelatedId(source.getRelatedId());
        target.setPushTime(source.getPushTime());
        return target;
    }
}