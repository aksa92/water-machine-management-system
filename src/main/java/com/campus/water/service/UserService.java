package com.campus.water.service;

import com.campus.water.entity.User;
import com.campus.water.mapper.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 获取学生用户列表（支持按姓名和状态筛选）
     */
    public List<User> getUserList(String studentName, User.UserStatus status) {
        if (studentName != null && status != null) {
            // 按姓名模糊查询和状态筛选
            return userRepository.findByStudentNameContainingAndStatus(studentName, status);
        } else if (studentName != null) {
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
}