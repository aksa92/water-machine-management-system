package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.Alert;
import com.campus.water.entity.MessagePush;
import com.campus.water.entity.Repairman;
import com.campus.water.Repository.AdminRepository;
import com.campus.water.Repository.MessagePushRepository;
import com.campus.water.Repository.RepairmanRepository;
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
    private final RepairmanRepository repairmanRepository;
    private final AdminRepository adminRepository;

    /**
     * 推送告警消息给目标用户（根据告警级别和区域分配）
     */
    @Transactional
    public void pushAlertMessage(Alert alert) {
        String alertType = alert.getAlertType();
        String areaId = alert.getAreaId();
        boolean isEmergency = alert.getAlertLevel().getPriority() >= 3;

        MessagePush message = new MessagePush();
        message.setTitle(String.format("[%s告警] %s",
                alert.getAlertLevel().getLevelName(), alertType));
        message.setContent(alert.getAlertMessage());
        message.setMessageType("ALERT");
        message.setRelatedId(alert.getAlertId().toString());
        message.setPushTime(LocalDateTime.now());

        // 推送区域维修人员
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

        // 紧急告警推送管理员
        if (isEmergency) {
            List<Admin> admins = adminRepository.findByRole(Admin.AdminRole.ROLE_SUPER_ADMIN);
            for (Admin admin : admins) {
                MessagePush adminMsg = copyMessage(message);
                adminMsg.setAdminId(admin.getAdminId());
                adminMsg.setUserId(admin.getAdminId());
                adminMsg.setUserType("ADMIN");
                messagePushRepository.save(adminMsg);
            }
            log.info("紧急告警推送管理员完成 | 告警ID：{} | 人数：{}", alert.getAlertId(), admins.size());
        }
    }

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
