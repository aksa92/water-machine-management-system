package main.java.com.campus.water.mapper;

import main.java.com.campus.water.entity.Area;
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

    // 新增按区域类型查询（按创建时间倒序）
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

    // 移除所有含isDeleted的方法（实体无该属性）
    // Optional<Area> findByIdAndIsDeletedFalse(String id); // 已移除
    // List<Area> findByAreaTypeAndParentAreaIdIsNullAndIsDeletedFalse(Area.AreaType areaType); // 已移除
    // List<Area> findByParentAreaIdAndAreaTypeAndIsDeletedFalse(String parentAreaId, Area.AreaType areaType); // 已移除
    // long countByParentAreaIdAndIsDeletedFalse(String parentAreaId); // 已移除
    // boolean existsByIdAndIsDeletedFalse(String id); // 已移除

    // 保留原有正确方法
    Optional<Area> findByAreaId(String areaId);
    long countByParentAreaId(String areaId);
    List<Area> findByAreaTypeAndParentAreaIdIsNull(Area.AreaType areaType);
    boolean existsByAreaId(String cityId);

    /**
     * 查询没有负责人的片区（manager为null或空字符串）
     * 覆盖未设置负责人的所有场景
     */
    List<Area> findByAreaTypeAndManagerIsNullOrManagerEquals(Area.AreaType areaType, String emptyStr);

}