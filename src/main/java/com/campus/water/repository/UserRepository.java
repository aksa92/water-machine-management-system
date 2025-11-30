package com.campus.water.repository;

import com.campus.water.entity.po.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserPO, String> {
    // 登录核心：通过用户名查询
    Optional<UserPO> findByUsername(String username);

    // 可补充其他业务方法（如按学号查询）
    Optional<UserPO> findByStudentNo(String studentNo);
    boolean existsByUsername(String username);
    boolean existsByStudentNo(String studentNo);
}