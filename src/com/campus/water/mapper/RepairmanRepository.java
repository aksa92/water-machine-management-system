package com.campus.water.mapper;

import datebaseclass.system.Repairman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepairmanRepository extends JpaRepository<Repairman, String> {
    List<Repairman> findByAreaId(String areaId);
    List<Repairman> findByStatus(Repairman.RepairmanStatus status);
    List<Repairman> findBySkillsContaining(String skill);
    List<Repairman> findByAreaIdAndStatus(String areaId, Repairman.RepairmanStatus status);
    List<Repairman> findByRatingGreaterThanEqual(Double minRating);
}