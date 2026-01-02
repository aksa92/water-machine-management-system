package com.campus.water.controller.app;

import com.campus.water.entity.WorkOrder;
import com.campus.water.service.app.RepairmanAppService;
import com.campus.water.util.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app/repairman") // 维修人员APP端接口前缀
public class RepairmanAppController {

    @Autowired
    private RepairmanAppService repairmanAppService;

    // 获取可抢工单列表
    @GetMapping("/available-orders")
    public ResultVO<List<WorkOrder>> getAvailableOrders(@RequestParam String areaId) {
        return repairmanAppService.getAvailableOrders(areaId);
    }

    // 抢单
    @PostMapping("/grab-order")
    public ResultVO<Boolean> grabOrder(@RequestBody Map<String, String> request) {
        return repairmanAppService.grabOrder(request);
    }

    // 拒单
    @PostMapping("/reject-order")
    public ResultVO<Boolean> rejectOrder(@RequestBody Map<String, String> request) {
        return repairmanAppService.rejectOrder(request);
    }

    // 提交维修结果
    @PostMapping("/submit-result")
    public ResultVO<Boolean> submitRepairResult(@RequestBody Map<String, String> request) {
        return repairmanAppService.submitRepairResult(request);
    }

    // 获取我的工单
    @GetMapping("/my-orders")
    public ResultVO<List<WorkOrder>> getMyOrders(@RequestParam String repairmanId) {
        return repairmanAppService.getMyOrders(repairmanId);
    }
}