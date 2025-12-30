package com.campus.water.service;

import com.campus.water.entity.Area;
import com.campus.water.entity.Device;
import com.campus.water.mapper.AreaRepository;
import com.campus.water.entity.Admin;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.DeviceRepository;
import com.campus.water.mapper.DeviceTerminalMappingRepository;
import com.campus.water.security.RoleConstants;
import com.campus.water.entity.vo.AreaDeviceStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 区域管理服务类
 * 完全适配你的 Area 实体类（areaId 主键、新增 address/manager 等字段）
 * 核心规则：市区为根节点（无父级），校园必须以市区为父节点
 */
@Service
public class AreaService {

    private final AreaRepository areaRepository;
    // 新增：管理员仓库注入
    private final AdminRepository adminRepository;

    // 新增：注入现有设备、终端映射Repository（无新增Mapper方法）
    private final DeviceRepository deviceRepository;
    private final DeviceTerminalMappingRepository deviceTerminalMappingRepository;

    public AreaService(AreaRepository areaRepository, AdminRepository adminRepository,
                       DeviceRepository deviceRepository,
                       DeviceTerminalMappingRepository deviceTerminalMappingRepository) {
        this.areaRepository = areaRepository;
        this.adminRepository = adminRepository;
        this.deviceRepository = deviceRepository;
        this.deviceTerminalMappingRepository = deviceTerminalMappingRepository;
    }

    /**
     * 新增区域
     * @param area 区域对象（包含名称、类型、父级ID、地址、管理员等）
     * @return 保存后的区域对象
     */
    @Transactional(rollbackFor = Exception.class)
    public Area addArea(Area area) {
        // 1. 基础参数校验
        validateBaseParams(area);

        // 2. 层级规则校验（核心）
        validateAreaHierarchy(area);

        // 3. 新增：管理员关联校验（若传入manager（管理员ID），则校验并预绑定）
        validateAndPrepareAdmin(area);

        // 4. 补充基础字段（createdTime/updatedTime 已默认赋值，可手动刷新）
        area.setCreatedTime(LocalDateTime.now());
        area.setUpdatedTime(LocalDateTime.now());

        // 5. 保存数据
        Area savedArea = areaRepository.save(area);

        // 6. 新增：完成区域与管理员的最终绑定（需使用保存后的区域ID）
        bindAdminToArea(area.getManager(), savedArea.getAreaId());

        // 7. 返回保存后的区域对象
        return savedArea;
    }

    /**
     * 修改区域
     * @param areaId 区域ID
     * @param area   待修改的区域信息
     * @return 修改后的区域对象
     */
    @Transactional(rollbackFor = Exception.class)
    public Area updateArea(String areaId, Area area) {
        // 1. 校验区域是否存在
        Area existingArea = areaRepository.findByAreaId(areaId)
                .orElseThrow(() -> new RuntimeException("区域不存在，ID：" + areaId));

        // 2. 基础参数校验
        validateBaseParams(area);

        // 3. 层级规则校验（修改时需保持层级规则）
        validateAreaHierarchy(area);

        // 4. 覆盖可修改字段（适配你的实体类所有字段）
        existingArea.setAreaName(area.getAreaName());
        existingArea.setAreaType(area.getAreaType());
        // 父级ID：市区不允许修改，校园必须指向市区
        if (Area.AreaType.campus.equals(area.getAreaType())) {
            existingArea.setParentAreaId(area.getParentAreaId());
        }
        // 新增字段赋值
        existingArea.setAddress(area.getAddress());
        existingArea.setManager(area.getManager());
        existingArea.setManagerPhone(area.getManagerPhone());
        // 更新时间
        existingArea.setUpdatedTime(LocalDateTime.now());

        // 5. 保存修改
        return areaRepository.save(existingArea);
    }

    /**
     * 删除区域
     * @param areaId 区域ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteArea(String areaId) {
        // 1. 校验区域是否存在
        Area existingArea = areaRepository.findByAreaId(areaId)
                .orElseThrow(() -> new RuntimeException("区域不存在，ID：" + areaId));

        // 2. 校验删除规则：若为市区，需先删除其下所有校园
        if (Area.AreaType.zone.equals(existingArea.getAreaType())) {
            long campusCount = areaRepository.countByParentAreaId(areaId);
            if (campusCount > 0) {
                throw new RuntimeException("该市区下仍有 " + campusCount + " 个校园，无法删除，请先删除下属校园");
            }
        }

        // 3. 物理删除（若需逻辑删除，可参考后续说明添加 isDeleted 字段）
        areaRepository.delete(existingArea);
    }

    /**
     * 查询所有市区（根节点）
     * @return 市区列表
     */
    public List<Area> getAllCities() {
        return areaRepository.findByAreaTypeAndParentAreaIdIsNull(Area.AreaType.zone);
    }

    /**
     * 修改：仅查询无负责人的校区，排除市区
     */
    public List<Area> getAreasWithoutManager() {
        // 调用仓库新增方法，限定：区域类型=campus，负责人=null 或 空字符串
        return areaRepository.findByAreaTypeAndManagerIsNullOrManagerEquals(
                Area.AreaType.campus,  // 仅筛选校区
                ""                      // 匹配空字符串的负责人
        );
    }

    /**
     * 根据市区ID查询下属校园
     * @param cityId 市区ID（areaId）
     * @return 该市区下的校园列表
     */
    public List<Area> getCampusesByCityId(String cityId) {
        // 校验市区是否存在
        if (!areaRepository.existsByAreaId(cityId)) {
            throw new RuntimeException("市区不存在，ID：" + cityId);
        }
        return areaRepository.findByParentAreaIdAndAreaType(cityId, Area.AreaType.campus );
    }

    /**
     * 基础参数校验（名称、类型不能为空）
     * @param area 区域对象
     */
    private void validateBaseParams(Area area) {
        if (area.getAreaName() == null || area.getAreaName().trim().isEmpty()) {
            throw new RuntimeException("区域名称不能为空");
        }
        if (area.getAreaType() == null) {
            throw new RuntimeException("区域类型不能为空（市区/校园）");
        }
    }

    /**
     * 区域层级规则校验（核心）
     * 规则1：市区必须是根节点，无父级ID（parentAreaId=null）
     * 规则2：校园必须有父级ID，且父级必须是市区
     */
    private void validateAreaHierarchy(Area area) {
        if (Area.AreaType.zone.equals(area.getAreaType())) {
            // 市区不允许设置父级ID
            if (area.getParentAreaId() != null && !area.getParentAreaId().trim().isEmpty()) {
                throw new RuntimeException("市区为根节点，不允许设置父级区域");
            }
        } else if (Area.AreaType.campus .equals(area.getAreaType())) {
            // 校园必须设置父级ID
            if (area.getParentAreaId() == null || area.getParentAreaId().trim().isEmpty()) {
                throw new RuntimeException("校园必须关联市区作为父级区域");
            }
            // 校验父级区域是否存在且类型为市区
            Optional<Area> parentAreaOpt = areaRepository.findByAreaId(area.getParentAreaId());
            if (parentAreaOpt.isEmpty()) {
                throw new RuntimeException("父级市区不存在，ID：" + area.getParentAreaId());
            }
            Area parentArea = parentAreaOpt.get();
            if (!Area.AreaType.zone.equals(parentArea.getAreaType())) {
                throw new RuntimeException("校园的父级区域必须是市区，当前父级类型为：" + parentArea.getAreaType().getDesc());
            }
        }
    }

    public Area getAreaById(String areaId) {
        return areaRepository.findByAreaId(areaId)
                .orElseThrow(() -> new RuntimeException("区域不存在，ID：" + areaId));
    }

    /**
     * 辅助方法：校验管理员合法性（存在+角色正确）
     * @param area 区域对象（提取manager字段：管理员ID）
     */
    private void validateAndPrepareAdmin(Area area) {
        String adminId = area.getManager();
        if (adminId != null && !adminId.trim().isEmpty()) {
            // 校验管理员是否存在
            Admin admin = adminRepository.findById(adminId)
                    .orElseThrow(() -> new RuntimeException("区域管理员不存在，ID：" + adminId));

            // 校验管理员角色是否为区域管理员
            if (!RoleConstants.ROLE_AREA_ADMIN.equals(admin.getRole().name())) {
                throw new RuntimeException("指定用户不是区域管理员角色，无法绑定区域");
            }
        }
    }

    /**
     * 辅助方法：将管理员绑定到指定区域
     * @param adminId 管理员ID
     * @param areaId  区域ID（已保存的有效区域ID）
     */
    private void bindAdminToArea(String adminId, String areaId) {
        if (adminId != null && !adminId.trim().isEmpty() && areaId != null) {
            Admin admin = adminRepository.findById(adminId).get(); // 已在前序校验，无需再次处理空值
            // 新增：校验该管理员是否已绑定其他校区
            if (admin.getAreaId() != null) {
                throw new RuntimeException("该区域管理员已绑定校区【" + admin.getAreaId() + "】，无法重复绑定");
            }
            admin.setAreaId(areaId); // 给管理员设置关联的区域ID（Admin实体需有areaId字段）
            adminRepository.save(admin);
        }
    }


    /**
     * 获取指定片区的设备统计（校区：自身数据；市区：自身+下属所有校区汇总数据）
     * @param areaId 片区ID（市区/校园）
     * @return 片区设备统计结果（适配现有实体）
     */
    @Transactional(readOnly = true) // 只读事务提升查询性能，不修改数据
    public AreaDeviceStatsVO getAreaDeviceStats(String areaId) {
        // 1. 校验片区是否存在，获取片区基础信息
        Area targetArea = getAreaById(areaId);
        AreaDeviceStatsVO statsVO = new AreaDeviceStatsVO();

        // 2. 填充片区基础信息（复用Area实体字段）
        statsVO.setAreaId(targetArea.getAreaId());
        statsVO.setAreaName(targetArea.getAreaName());
        statsVO.setAreaTypeDesc(targetArea.getAreaType().getDesc());

        // 3. 区分片区类型执行统计（核心逻辑，复用现有Mapper方法）
        if (Area.AreaType.campus.equals(targetArea.getAreaType())) {
            // 校区：仅统计自身关联的设备/终端
            fillSelfDeviceStats(statsVO, areaId);
        } else if (Area.AreaType.zone.equals(targetArea.getAreaType())) {
            // 市区：统计自身 + 下属所有校区的汇总数据
            fillCityTotalDeviceStats(statsVO, areaId);
        }

        return statsVO;
    }

    /**
     * 填充单个片区（校区/市区自身）的设备统计数据（复用现有Mapper的count方法）
     * @param statsVO 统计结果VO
     * @param areaId  片区ID
     */
    private void fillSelfDeviceStats(AreaDeviceStatsVO statsVO, String areaId) {
        // 复用DeviceRepository.countByAreaIdAndDeviceType（已存在，无需新增）
        long waterMakerCount = deviceRepository.countByAreaIdAndDeviceType(
                areaId, Device.DeviceType.water_maker); // 假设Device枚举包含WATER_MAKER/WATER_SUPPLY
        long waterSupplyCount = deviceRepository.countByAreaIdAndDeviceType(
                areaId, Device.DeviceType.water_supply);

        // 复用DeviceTerminalMappingRepository.countByAreaId（已存在，无需新增）
        long terminalCount = deviceTerminalMappingRepository.countByAreaId(areaId);

        // 赋值到VO（强转int，若数量过大可改为long，兼容现有数据）
        statsVO.setWaterMakerCount((int) waterMakerCount);
        statsVO.setWaterSupplyCount((int) waterSupplyCount);
        statsVO.setTerminalCount((int) terminalCount);
    }

    /**
     * 填充市区的总设备统计数据（仅汇总下属所有校区，市区自身无设备/终端）
     * @param statsVO 统计结果VO
     * @param cityId  市区ID
     */
    private void fillCityTotalDeviceStats(AreaDeviceStatsVO statsVO, String cityId) {
        // 第一步：获取市区下属所有校区的ID列表（复用现有getCampusesByCityId方法）
        List<Area> campusList = getCampusesByCityId(cityId);
        List<String> campusIds = campusList.stream()
                .map(Area::getAreaId)
                .collect(Collectors.toList());

        // 第二步：汇总下属所有校区的设备/终端数量（循环复用现有count方法，适配现有Mapper）
        long campusWaterMaker = 0;
        long campusWaterSupply = 0;
        long campusTerminal = 0;
        if (!campusIds.isEmpty()) {
            for (String campusId : campusIds) {
                campusWaterMaker += deviceRepository.countByAreaIdAndDeviceType(campusId, Device.DeviceType.water_maker);
                campusWaterSupply += deviceRepository.countByAreaIdAndDeviceType(campusId, Device.DeviceType.water_supply);
                campusTerminal += deviceTerminalMappingRepository.countByAreaId(campusId);
            }
        }

        // 第三步：直接赋值校区汇总数据（市区自身无数据）
        statsVO.setWaterMakerCount((int) campusWaterMaker);
        statsVO.setWaterSupplyCount((int) campusWaterSupply);
        statsVO.setTerminalCount((int) campusTerminal);
    }

    /**
     * 获取指定市区下所有校区的单独设备统计（每个校区各自的统计结果，不汇总）
     * @param cityId 市区ID
     * @return 校区统计列表（复用现有方法，无新增依赖）
     */
    @Transactional(readOnly = true)
    public List<AreaDeviceStatsVO> getCampusDeviceStatsUnderCity(String cityId) {
        // 1. 校验市区存在且类型正确
        Area cityArea = getAreaById(cityId);
        if (!Area.AreaType.zone.equals(cityArea.getAreaType())) {
            throw new RuntimeException("指定区域不是市区，无法查询下属校区统计");
        }

        // 2. 获取下属所有校区（复用现有方法）
        List<Area> campusList = getCampusesByCityId(cityId);

        // 3. 逐个生成校区统计结果（复用getAreaDeviceStats方法）
        return campusList.stream()
                .map(campus -> getAreaDeviceStats(campus.getAreaId()))
                .collect(Collectors.toList());
    }

}