package com.campus.water.mapper;

import com.campus.water.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // 根据学号查找用户
    Optional<User> findByStudentId(String studentId);

    // 根据学生姓名模糊查询
    List<User> findByStudentNameContaining(String studentName);

    // 根据用户状态查询
    List<User> findByStatus(User.UserStatus status);

    // 根据手机号查询用户
    Optional<User> findByPhone(String phone);

    // 根据邮箱查询用户
    Optional<User> findByEmail(String email);

    // 按姓名模糊查询和状态筛选
    @Query("SELECT u FROM User u WHERE u.studentName LIKE %?1% AND u.status = ?2")
    List<User> findByStudentNameContainingAndStatus(String studentName, User.UserStatus status);

    // 检查学号是否已存在
    boolean existsByStudentId(String studentId);

    // 检查手机号是否已存在
    boolean existsByPhone(String phone);

    // 检查邮箱是否已存在
    boolean existsByEmail(String email);
}