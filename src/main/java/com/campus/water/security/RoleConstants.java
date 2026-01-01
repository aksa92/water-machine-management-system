package main.java.com.campus.water.security;

/**
 * 角色常量定义
 */
public class RoleConstants {
    /** 学生角色 */
    public static final String ROLE_STUDENT = "ROLE_STUDENT";
    /** 维修人员角色 */
    public static final String ROLE_REPAIRMAN = "ROLE_REPAIRMAN";


    /** 新增：细分的管理员角色（与Admin枚举一一对应） */
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";  // 超级管理员
    public static final String ROLE_AREA_ADMIN = "ROLE_AREA_ADMIN";    // 区域管理员
    public static final String ROLE_VIEWER = "ROLE_VIEWER";            // 查看者

    private RoleConstants() {}
}