package com.campus.water.service;

import com.campus.water.entity.Area;
import com.campus.water.mapper.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 区域管理服务层
 * 处理校园/楼宇/区域的增删改查业务逻辑
 */
@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;
    private final AdminService adminService; // 用于删除时校验管理员关联

    /**
     * 新增区域（校园/楼宇/区域）
     * 校验规则：
     * 1. 名称和类型不能为空
     * 2. 楼宇必须关联校园作为父级
     * 3. 区域必须关联楼宇作为父级
     * 4. 校园无需父级（顶级节点）
     */
    public Area addArea(Area area) {
        // 基础字段校验
        if (area.getAreaName() == null || area.getAreaName().trim().isEmpty()) {
            throw new RuntimeException("区域名称不能为空");
        }
        if (area.getAreaType() == null) {
            throw new RuntimeException("区域类型不能为空（校园/楼宇/区域）");
        }

        // 层级关联校验
        handleAreaLevelCheck(area);

        // 初始化时间字段（兜底，防止手动修改）
        area.setCreatedTime(LocalDateTime.now());
        area.setUpdatedTime(LocalDateTime.now());

        return areaRepository.save(area);
    }

    /**
     * 删除区域
     * 校验规则：
     * 1. 区域必须存在
     * 2. 无管理员关联该区域
     * 3. 无下级子区域
     */
    public void deleteArea(String areaId) {
        // 1. 校验区域是否存在
        Area existArea = areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("区域不存在：" + areaId));

        // 2. 校验是否有管理员关联该区域
        List<com.campus.water.entity.Admin> relatedAdmins = adminService.getAdminsByAreaId(areaId);
        if (!relatedAdmins.isEmpty()) {
            throw new RuntimeException("该区域关联了" + relatedAdmins.size() + "个管理员，无法删除");
        }

        // 3. 校验是否有下级子区域
        List<Area> childAreas = areaRepository.findByParentAreaId(areaId);
        if (!childAreas.isEmpty()) {
            throw new RuntimeException("该区域包含" + childAreas.size() + "个子区域，无法删除（请先删除子区域）");
        }

        // 执行删除
        areaRepository.delete(existArea);
    }

    /**
     * 修改区域信息
     * 支持修改：名称、父级、地址、负责人、联系电话
     * 不允许修改：区域类型（避免层级混乱）
     */
    public Area updateArea(Area area) {
        // 1. 校验区域是否存在
        Area existArea = areaRepository.findById(area.getAreaId())
                .orElseThrow(() -> new RuntimeException("区域不存在：" + area.getAreaId()));

        // 2. 基础字段校验
        if (area.getAreaName() == null || area.getAreaName().trim().isEmpty()) {
            throw new RuntimeException("区域名称不能为空");
        }

        // 3. 层级关联校验（如果修改了父级ID）
        if (!equalsWithNull(existArea.getParentAreaId(), area.getParentAreaId())) {
            handleAreaLevelCheck(area);
        }

        // 4. 赋值（仅更新允许修改的字段）
        existArea.setAreaName(area.getAreaName());
        existArea.setParentAreaId(area.getParentAreaId());
        existArea.setAddress(area.getAddress());
        existArea.setManager(area.getManager());
        existArea.setManagerPhone(area.getManagerPhone());
        existArea.setUpdatedTime(LocalDateTime.now());

        // 5. 保存修改
        return areaRepository.save(existArea);
    }

    /**
     * 按ID查询区域详情
     */
    public Area getAreaById(String areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("区域不存在：" + areaId));
    }

    /**
     * 条件查询区域列表
     * 支持筛选条件：父级ID、区域类型、名称关键词
     */
    public List<Area> listAreas(String parentAreaId, Area.AreaType areaType, String keyword) {
        if (parentAreaId != null && !parentAreaId.trim().isEmpty()) {
            // 按父级ID查询
            if (areaType != null) {
                // 父级ID + 类型
                return areaRepository.findByParentAreaIdAndAreaType(parentAreaId, areaType);
            } else {
                // 仅父级ID
                return areaRepository.findByParentAreaId(parentAreaId);
            }
        } else if (areaType != null) {
            // 按类型查询
            return areaRepository.findByAreaTypeOrderByCreatedTimeDesc(areaType);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 按名称模糊查询
            return areaRepository.findByAreaNameContaining(keyword);
        } else {
            // 查询所有（按创建时间倒序）
            return areaRepository.findAllByOrderByCreatedTimeDesc();
        }
    }

    /**
     * 辅助方法：校验区域层级关联规则
     */
    private void handleAreaLevelCheck(Area area) {
        Area.AreaType type = area.getAreaType();
        String parentId = area.getParentAreaId();

        // 1. 校园（顶级节点）：不允许设置父级
        if (type == Area.AreaType.campus) {
            if (parentId != null && !parentId.trim().isEmpty()) {
                throw new RuntimeException("校园作为顶级节点，不允许关联父级区域");
            }
            return;
        }

        // 2. 楼宇/区域：必须设置父级
        if (parentId == null || parentId.trim().isEmpty()) {
            throw new RuntimeException(type.getDesc() + "必须关联父级区域");
        }

        // 3. 校验父级区域是否存在且类型匹配
        Area parentArea = areaRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("父级区域不存在：" + parentId));

        if (type == Area.AreaType.building && parentArea.getAreaType() != Area.AreaType.campus) {
            throw new RuntimeException("楼宇的父级必须是校园");
        }
        if (type == Area.AreaType.zone && parentArea.getAreaType() != Area.AreaType.building) {
            throw new RuntimeException("区域的父级必须是楼宇");
        }
    }

    /**
     * 辅助方法：判断两个值是否相等（兼容null）
     */
    private boolean equalsWithNull(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }
}