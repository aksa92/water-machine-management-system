package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.Area;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final AreaRepository areaRepository;  // 新增注入


    @Autowired
    private PasswordEncoder passwordEncoder;
    /**
     * 获取管理员列表（支持按姓名/角色筛选）
     */
    public List<Admin> getAdminList(String name, Admin.AdminRole role) {
        if (name != null && !name.isEmpty() && role != null) {
            // 按姓名+角色组合查询
            return adminRepository.findByAdminNameContainingAndRole(name, role);
        } else if (role != null) {
            // 仅按角色查询
            return adminRepository.findByRole(role);
        } else if (name != null && !name.isEmpty()) {
            // 仅按姓名查询
            return adminRepository.findByAdminNameContaining(name);
        } else {
            // 查询全部
            return adminRepository.findAll();
        }
    }

    /**
     * 新增：查询可分配校区的区域管理员（未负责任何片区的区域管理员）
     * 用于前端片区选择负责人时的下拉框数据源
     */
    public List<Admin> getAvailableAreaAdmins() {
        return adminRepository.findByRoleAndAreaIdIsNull(Admin.AdminRole.ROLE_AREA_ADMIN);
    }

    /**
     * 新增：获取指定区域的管理员列表
     */
    public List<Admin> getAdminsByAreaId(String areaId) {
        // 校验区域是否存在
        if (!areaRepository.existsById(areaId)) {
            throw new RuntimeException("区域不存在：" + areaId);
        }
        return adminRepository.findByAreaId(areaId);
    }

    /**
     * 按ID查询管理员
     */
    public Optional<Admin> getAdminById(String adminId) {
        return adminRepository.findByAdminId(adminId);
    }

    /**
     * 新增/修改管理员（支持指定角色）
     * 重写保存方法，增加区域校验（区域管理员必须关联区域）
     */
    public Admin saveAdmin(Admin admin) {
        admin.setUpdatedTime(LocalDateTime.now());
        if (admin.getCreatedTime() == null) {
            admin.setCreatedTime(LocalDateTime.now());
        }

        // 区域管理员（ROLE_AREA_ADMIN）的专属校验逻辑
        if (admin.getRole() == Admin.AdminRole.ROLE_AREA_ADMIN) {
            // 1. 若未填写区域ID（null/空字符串），直接放行（支持先创建管理员，后续补填）
            if (admin.getAreaId() == null || admin.getAreaId().trim().isEmpty()) {
                admin.setAreaId(null); // 统一置为null，避免空字符串冗余数据
                // 无需校验，直接允许保存
            } else {
                // 2. 若填写了区域ID，进行严格校验：区域存在 + 类型为校区（禁止市区）
                String areaId = admin.getAreaId().trim();
                // 校验区域是否存在
                Area targetArea = areaRepository.findById(areaId)
                        .orElseThrow(() -> new RuntimeException("关联的区域不存在：" + areaId));
                // 核心校验：仅允许关联校区，禁止关联市区
                if (Area.AreaType.zone.equals(targetArea.getAreaType())) {
                    throw new RuntimeException("区域管理员仅允许关联校区，不能关联市区，请重新选择");
                }
                // 校验通过，保留填写的合法校区ID
                admin.setAreaId(areaId);
            }
        } else {
            // 非区域管理员，清空区域ID，避免冗余数据
            admin.setAreaId(null);
        }

        return adminRepository.save(admin);
    }

    /**
     * 删除管理员
     */
    public void deleteAdmin(String adminId) {
        adminRepository.deleteById(adminId);
    }

    /**
     * 管理员登录验证
     */
    public Optional<Admin> login(String adminName, String password) {
        Optional<Admin> admin = adminRepository.findByAdminName(adminName);
        // 使用MD5加密器验证密码
        return admin.filter(a -> passwordEncoder.matches(password, a.getPassword()));
    }
    /**
     * 获取所有角色枚举（供前端下拉框使用）
     */
    public Admin.AdminRole[] getAllRoles() {
        return Admin.AdminRole.values();
    }

    public AreaRepository getAreaRepository() {
        return areaRepository;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 个人信息更新（限制可修改字段）
     */
    public Admin updateProfile(Admin profile) {
        // 1. 获取数据库中原始信息
        Admin existingAdmin = adminRepository.findByAdminId(profile.getAdminId())
                .orElseThrow(() -> new RuntimeException("管理员不存在"));

        // 2. 仅更新允许修改的字段（排除角色、区域等敏感信息）
        existingAdmin.setAdminName(profile.getAdminName());
        existingAdmin.setPhone(profile.getPhone());
        existingAdmin.setUpdatedTime(LocalDateTime.now());

        // 3. 密码修改单独处理（如果有密码更新需求）
        if (profile.getPassword() != null && !profile.getPassword().isEmpty()) {
            existingAdmin.setPassword(passwordEncoder.encode(profile.getPassword()));
        }

        return adminRepository.save(existingAdmin);
    }

    /**
     * 辅助方法：通过用户名查询管理员
     */
    public Optional<Admin> getAdminByName(String username) {
        return adminRepository.findByAdminName(username);
    }

    /**
     * 管理员密码修改（验证原密码，校验新密码，更新密码）
     * @param username 登录用户名
     * @param oldPassword 原密码（明文）
     * @param newPassword 新密码（明文）
     * @return 密码修改是否成功
     */
    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        // 1. 校验参数合法性
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("原密码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("新密码不能与原密码一致");
        }
        // 可选：新密码复杂度校验（增强安全性，根据项目需求调整）
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new IllegalArgumentException("新密码长度必须在6-20位之间");
        }

        // 2. 根据用户名查询当前管理员信息
        Admin existingAdmin = adminRepository.findByAdminName(username)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));

        // 3. 验证原密码是否正确（使用项目已有的 PasswordEncoder 进行匹配）
        boolean oldPasswordMatch = passwordEncoder.matches(oldPassword, existingAdmin.getPassword());
        if (!oldPasswordMatch) {
            return false; // 原密码错误，返回修改失败
        }

        // 4. 加密新密码并更新管理员信息
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        existingAdmin.setPassword(encodedNewPassword);
        existingAdmin.setUpdatedTime(LocalDateTime.now()); // 更新修改时间，保持与其他方法一致

        // 5. 保存到数据库
        adminRepository.save(existingAdmin);
        return true;
    }

}