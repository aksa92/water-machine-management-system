package com.campus.water.security;

import com.campus.water.entity.Admin;
import com.campus.water.entity.RepairerAuth;
import com.campus.water.entity.User; // 自定义User实体类（保留）
import com.campus.water.Repository.AdminRepository;
import com.campus.water.Repository.RepairerAuthRepository;
import com.campus.water.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RepairerAuthRepository repairerAuthRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询自定义User实体（学生用户）
        User student = userRepository.findByStudentName(username).orElse(null);
        if (student != null) {
            return createUserDetails(student.getStudentName(), student.getPassword(), RoleConstants.ROLE_STUDENT);
        }

        // 2. 查询管理员用户
        Admin admin = adminRepository.findByAdminName(username).orElse(null);
        if (admin != null) {
            // ========== 关键改动：替换硬编码的RoleConstants.ROLE_ADMIN为admin.getRole().name() ==========
            return createUserDetails(
                    admin.getAdminName(),
                    admin.getPassword(),
                    admin.getRole().name() // 取Admin实体中实际的角色（如ROLE_SUPER_ADMIN/ROLE_AREA_ADMIN）
            );
        }

        // 3. 查询维修人员用户
        RepairerAuth repairer = repairerAuthRepository.findByUsername(username).orElse(null);
        if (repairer != null) {
            return createUserDetails(repairer.getUsername(), repairer.getPassword(), RoleConstants.ROLE_REPAIRMAN);
        }

        throw new UsernameNotFoundException("用户不存在: " + username);
    }

    private UserDetails createUserDetails(String username, String password, String role) {
        return new org.springframework.security.core.userdetails.User(
                username,
                password,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}