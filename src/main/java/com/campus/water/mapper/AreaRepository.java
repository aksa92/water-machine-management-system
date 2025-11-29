package com.campus.water.mapper;

import com.campus.water.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, String> {
    List<Area> findByAreaType(Area.AreaType areaType);
    List<Area> findByParentAreaId(String parentAreaId);
    List<Area> findByManager(String manager);

    // 修复：使用正确的字段名 managerPhone
    List<Area> findByManagerPhone(String managerPhone);

    @Query("SELECT a FROM Area a WHERE a.areaType = ?1 AND a.parentAreaId IS NULL")
    List<Area> findRootAreasByType(Area.AreaType areaType);
}