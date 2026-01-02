// com/campus/water/config/MD5PasswordEncoder.java
package main.java.com.campus.water.config;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 自定义MD5密码编码器，实现Spring Security的PasswordEncoder接口
 */
public class MD5PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        // 对原始密码进行MD5加密（与注册时的加密方式保持一致）
        return DigestUtils.md5DigestAsHex(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // 验证时：将原始密码MD5加密后与数据库中存储的加密密码对比
        String rawEncoded = encode(rawPassword);
        return rawEncoded.equals(encodedPassword);
    }
}