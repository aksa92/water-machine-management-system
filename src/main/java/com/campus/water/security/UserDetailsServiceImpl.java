package com.campus.water.security;

import com.campus.water.entity.Admin;  // 改为引用Admin实体下的Admin实体类
import com.campus.water.entity.po.RepairerAuthPO;
import com.campus.water.entity.po.UserPO;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.RepairerAuthRepository;
import com.campus.water.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 加载用户权限信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;  // 学生用户仓库

    @Autowired
    private AdminRepository adminRepository;  // 管理员仓库

    @Autowired
    private RepairerAuthRepository repairerAuthRepository;  // 维修人员仓库

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 尝试查询学生用户
        UserPO student = userRepository.findByUsername(username).orElse(null);
        if (student != null) {
            return createUserDetails(
                    student.getUsername(),
                    student.getPassword(),
                    RoleConstants.ROLE_STUDENT
            );
        }

        // 2. 尝试查询管理员用户（适配Admin实体类的adminName字段）
        Admin admin = adminRepository.findByAdminName(username).orElse(null);  // 方法名从findByUsername改为findByAdminName
        if (admin != null) {
            return createUserDetails(
                    admin.getAdminName(),  // 字段名从getUsername改为getAdminName
                    admin.getPassword(),
                    RoleConstants.ROLE_ADMIN
            );
        }

        // 3. 尝试查询维修人员用户
        RepairerAuthPO repairer = repairerAuthRepository.findByUsername(username).orElse(null);
        if (repairer != null) {
            return createUserDetails(
                    repairer.getUsername(),
                    repairer.getPassword(),
                    RoleConstants.ROLE_REPAIRMAN
            );
        }

        // 所有类型用户都不存在
        throw new UsernameNotFoundException("用户不存在: " + username);
    }

    // 构建UserDetails对象的工具方法
    private UserDetails createUserDetails(String username, String password, String role) {
        return new User(
                username,
                password,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}