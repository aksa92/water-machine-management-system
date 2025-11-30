package com.campus.water.mapper;

import com.campus.water.entity.MaintenancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, Long> {
    // 根据设备ID查询维护计划
    List<MaintenancePlan> findByDeviceId(String deviceId);

    // 根据计划状态查询维护计划
    List<MaintenancePlan> findByPlanStatus(MaintenancePlan.PlanStatus planStatus);

    // 根据维护类型查询维护计划
    List<MaintenancePlan> findByMaintenanceType(String maintenanceType);

    // 查询指定日期前的维护计划
    List<MaintenancePlan> findByNextMaintenanceDateBefore(LocalDate date);

    // 根据下次维护日期范围查询维护计划
    List<MaintenancePlan> findByNextMaintenanceDateBetween(LocalDate start, LocalDate end);

    // 查询设备特定状态的维护计划
    List<MaintenancePlan> findByDeviceIdAndPlanStatus(String deviceId, MaintenancePlan.PlanStatus planStatus);

    // 查询到期需要执行的维护计划
    @Query("SELECT mp FROM MaintenancePlan mp WHERE mp.nextMaintenanceDate <= ?1 AND mp.planStatus = 'active'")
    List<MaintenancePlan> findDueMaintenancePlans(LocalDate date);
}