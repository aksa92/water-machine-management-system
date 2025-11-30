package com.campus.water.repository;

import com.campus.water.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, String> {
    // 根据区域类型查询
    List<Area> findByAreaType(Area.AreaType areaType);

    // 根据父区域ID查询子区域
    List<Area> findByParentAreaId(String parentAreaId);

    // 根据管理员姓名查询区域
    List<Area> findByManager(String manager);

    // 根据管理员手机号查询区域
    List<Area> findByManagerPhone(String managerPhone);

    // 查询指定类型的根级区域
    @Query("SELECT a FROM Area a WHERE a.areaType = ?1 AND a.parentAreaId IS NULL")
    List<Area> findRootAreasByType(Area.AreaType areaType);
}