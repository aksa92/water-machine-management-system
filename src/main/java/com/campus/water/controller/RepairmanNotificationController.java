package main.java.com.campus.water.controller;

import main.java.com.campus.water.entity.Notification;
import main.java.com.campus.water.service.NotificationService;
import main.java.com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 维修人员通知专属控制器
 * 负责APP端通知相关接口，与原有维修人员控制器解耦
 */
@RestController
@RequestMapping("/api/app/repairman/notification")
@RequiredArgsConstructor
@Tag(name = "维修人员通知接口", description = "维修人员APP端通知查询/已读标记接口")
public class RepairmanNotificationController {

    private final NotificationService notificationService;

    /**
     * 获取维修人员未读通知
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读通知", description = "查询维修人员所有未读的派单/系统通知")
    public ResultVO<List<Notification>> getUnreadNotifications(@RequestParam String repairmanId) {
        try {
            List<Notification> unreadNotifications = notificationService.getUnreadNotifications(repairmanId);
            return ResultVO.success(unreadNotifications, "获取未读通知成功");
        } catch (Exception e) {
            return ResultVO.error(500, "获取未读通知失败：" + e.getMessage());
        }
    }

    /**
     * 获取维修人员所有通知
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有通知", description = "查询维修人员所有通知（已读+未读）")
    public ResultVO<List<Notification>> getAllNotifications(@RequestParam String repairmanId) {
        try {
            List<Notification> allNotifications = notificationService.getAllNotifications(repairmanId);
            return ResultVO.success(allNotifications, "获取所有通知成功");
        } catch (Exception e) {
            return ResultVO.error(500, "获取所有通知失败：" + e.getMessage());
        }
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/read")
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读状态")
    public ResultVO<Boolean> markNotificationAsRead(@RequestParam Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResultVO.success(true, "标记通知为已读成功");
        } catch (Exception e) {
            return ResultVO.error(500, "标记通知为已读失败：" + e.getMessage());
        }
    }
}