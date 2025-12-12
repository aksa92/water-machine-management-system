package com.campus.water.service;

import com.campus.water.entity.Repairman;
import com.campus.water.mapper.RepairmanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 维修人员服务类（合并接口+实现）
 */
@Service
@RequiredArgsConstructor
public class RepairmanService {

    private final RepairmanRepository repairmanRepository;

    /**
     * 获取维修人员列表（支持多条件筛选）
     * @param name 维修人员姓名（模糊查询，可选）
     * @param areaId 区域ID（可选）
     * @param status 维修人员状态（可选）
     * @return 符合条件的维修人员列表
     */
    public List<Repairman> getRepairmanList(String name, String areaId, Repairman.RepairmanStatus status) {
        // 组合查询条件（与原实现逻辑完全一致）
        if (name != null && !name.isEmpty() && areaId != null && !areaId.isEmpty() && status != null) {
            return repairmanRepository.findByRepairmanNameContainingAndAreaIdAndStatus(name, areaId, status);
        } else if (name != null && !name.isEmpty() && areaId != null && !areaId.isEmpty()) {
            return repairmanRepository.findByRepairmanNameContainingAndAreaId(name, areaId);
        } else if (name != null && !name.isEmpty() && status != null) {
            return repairmanRepository.findByRepairmanNameContainingAndStatus(name, status);
        } else if (areaId != null && !areaId.isEmpty() && status != null) {
            return repairmanRepository.findByAreaIdAndStatus(areaId, status);
        } else if (name != null && !name.isEmpty()) {
            return repairmanRepository.findByRepairmanNameContaining(name);
        } else if (areaId != null && !areaId.isEmpty()) {
            return repairmanRepository.findByAreaId(areaId);
        } else if (status != null) {
            return repairmanRepository.findByStatus(status);
        } else {
            // 查询全部
            return repairmanRepository.findAll();
        }
    }

    /**
     * 获取所有维修人员状态枚举
     * @return 维修人员状态枚举数组
     */
    public Repairman.RepairmanStatus[] getAllStatus() {
        return Repairman.RepairmanStatus.values();
    }
}