package com.campus.water.mapper; // 核心修正：从mapper改为repository（JPA规范）

import com.campus.water.entity.po.UserPO; // 替换为PO包下的实体类
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserPO, String> {
    // 根据学号查找用户（适配PO类，字段名保持studentId不变）
    Optional<UserPO> findByStudentId(String studentId);

    // 根据学生姓名模糊查询（适配PO的username字段，原studentName对应PO的username）
    List<UserPO> findByUsernameContaining(String username);

    // 根据用户状态查询（引用PO内的UserStatus枚举）
    List<UserPO> findByStatus(UserPO.UserStatus status);

    // 根据手机号查询用户
    Optional<UserPO> findByPhone(String phone);

    // 根据邮箱查询用户
    Optional<UserPO> findByEmail(String email);

    // 按姓名模糊查询和状态筛选（JPQL实体类名改为UserPO，studentName改为username）
    @Query("SELECT u FROM UserPO u WHERE u.username LIKE %?1% AND u.status = ?2")
    List<UserPO> findByUsernameContainingAndStatus(String username, UserPO.UserStatus status);

    // 检查学号是否已存在
    boolean existsByStudentId(String studentId);

    // 检查手机号是否已存在
    boolean existsByPhone(String phone);

    // 检查邮箱是否已存在
    boolean existsByEmail(String email);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // ========== 新增：登录核心方法（必须） ==========
    Optional<UserPO> findByUsername(String username);
}