package com.campus.water.mapper;

import com.campus.water.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<Area, String> {
    // 根据区域类型查询
    List<Area> findByAreaType(Area.AreaType areaType);

    // 根据父区域ID查询子区域
    List<Area> findByParentAreaId(String parentAreaId);

    // 新增按区域类型查询
    List<Area> findByAreaTypeOrderByCreatedTimeDesc(Area.AreaType areaType);

    // 按父级ID+类型查询（如查询某校园下的所有楼宇）
    List<Area> findByParentAreaIdAndAreaType(String parentAreaId, Area.AreaType areaType);



    // 按名称模糊查询
    List<Area> findByAreaNameContaining(String keyword);

    // 查询所有（按创建时间倒序）
    List<Area> findAllByOrderByCreatedTimeDesc();

    // 根据管理员姓名查询区域
    List<Area> findByManager(String manager);

    // 根据管理员手机号查询区域
    List<Area> findByManagerPhone(String managerPhone);

    // 查询指定类型的根级区域
    @Query("SELECT a FROM Area a WHERE a.areaType = ?1 AND a.parentAreaId IS NULL")
    List<Area> findRootAreasByType(Area.AreaType areaType);

    // 根据ID和未删除状态查询
    Optional<Area> findByIdAndIsDeletedFalse(String id);
    // 查询指定类型、无父级、未删除的区域（所有市区）
    List<Area> findByAreaTypeAndParentAreaIdIsNullAndIsDeletedFalse(Area.AreaType areaType);
    // 根据父级ID、类型、未删除状态查询（市区下的校园）
    List<Area> findByParentAreaIdAndAreaTypeAndIsDeletedFalse(String parentAreaId, Area.AreaType areaType);
    // 统计父级ID下未删除的区域数量（删除市区时校验）
    long countByParentAreaIdAndIsDeletedFalse(String parentAreaId);
    // 校验ID是否存在且未删除
    boolean existsByIdAndIsDeletedFalse(String id);

    Optional<Area> findByAreaId(String areaId);

    long countByParentAreaId(String areaId);

    List<Area> findByAreaTypeAndParentAreaIdIsNull(Area.AreaType areaType);

    boolean existsByAreaId(String cityId);
}