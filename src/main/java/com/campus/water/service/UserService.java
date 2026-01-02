package com.campus.water.service;

import com.campus.water.entity.User;
import com.campus.water.mapper.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 获取学生用户列表（支持按姓名和状态筛选）
     */
    public List<User> getUserList(String studentName, User.UserStatus status) {
        if (studentName != null && !studentName.isEmpty() && status != null) {
            // 按姓名模糊查询和状态筛选
            return userRepository.findByStudentNameContainingAndStatus(studentName, status);
        } else if (studentName != null && !studentName.isEmpty()) {
            // 仅按姓名模糊查询
            return userRepository.findByStudentNameContaining(studentName);
        } else if (status != null) {
            // 仅按状态筛选
            return userRepository.findByStatus(status);
        } else {
            // 查询所有学生
            return userRepository.findAll();
        }
    }

    /**
     * 新增/编辑学生
     */
    public User saveUser(User user) {
        // 新增时设置默认值
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        // 新增学生默认状态为激活
        if (user.getStudentId() == null && user.getStatus() == null) {
            user.setStatus(User.UserStatus.active);
        }
        user.setUpdatedTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * 删除学生
     */
    public void deleteUser(String studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new RuntimeException("学生不存在：" + studentId);
        }
        userRepository.deleteById(studentId);
    }

    /**
     * 根据学号查询学生
     */
    public Optional<User> getUserById(String studentId) {
        return userRepository.findById(studentId);
    }
}