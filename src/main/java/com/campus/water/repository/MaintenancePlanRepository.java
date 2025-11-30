package com.campus.water.repository;

import com.campus.water.entity.MaintenancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, Long> {
    List<MaintenancePlan> findByDeviceId(String deviceId);
    List<MaintenancePlan> findByPlanStatus(MaintenancePlan.PlanStatus planStatus);
    List<MaintenancePlan> findByMaintenanceType(String maintenanceType);
    List<MaintenancePlan> findByNextMaintenanceDateBefore(LocalDate date);
    List<MaintenancePlan> findByNextMaintenanceDateBetween(LocalDate start, LocalDate end);
    List<MaintenancePlan> findByDeviceIdAndPlanStatus(String deviceId, MaintenancePlan.PlanStatus planStatus);

    @Query("SELECT mp FROM MaintenancePlan mp WHERE mp.nextMaintenanceDate <= ?1 AND mp.planStatus = 'active'")
    List<MaintenancePlan> findDueMaintenancePlans(LocalDate date);
}