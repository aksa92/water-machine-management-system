package com.campus.water.mapper;

import com.campus.water.entity.Repairman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepairmanRepository extends JpaRepository<Repairman, String> {
    // 根据区域ID查询维修人员
    List<Repairman> findByAreaId(String areaId);

    // 根据状态查询维修人员
    List<Repairman> findByStatus(Repairman.RepairmanStatus status);

    // 按技能关键词查询维修人员
    List<Repairman> findBySkillsContaining(String skill);

    // 按区域和状态查询维修人员
    List<Repairman> findByAreaIdAndStatus(String areaId, Repairman.RepairmanStatus status);

    // 查询评分高于阈值的维修人员
    List<Repairman> findByRatingGreaterThanEqual(Double minRating);
}