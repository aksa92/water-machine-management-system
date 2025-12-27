package com.campus.water.mapper;

import com.campus.water.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<Area, String> {

    // 修正：将findByIdAndIsDeletedFalse改为findByAreaIdAndIsDeletedFalse（如果有isDeleted字段）
    // 注意：你的实体类目前没有isDeleted字段，先删除该方法，或补充字段后再用
    Optional<Area> findByAreaId(String areaId);

    // 查询所有市区（根节点：areaType=CITY 且 parentAreaId=null）
    List<Area> findByAreaTypeAndParentAreaIdIsNull(Area.AreaType areaType);

    // 根据父级ID和区域类型查询（指定市区下的校园）
    List<Area> findByParentAreaIdAndAreaType(String parentAreaId, Area.AreaType areaType);

    // 统计指定父级ID下的区域数量
    long countByParentAreaId(String parentAreaId);

    // 校验区域ID是否存在
    boolean existsByAreaId(String areaId);
}