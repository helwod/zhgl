package com.account.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt 密码哈希工具类。
 * <p>
 * 提供基于 BCrypt 算法的密码哈希存储与验证功能。
 * 使用随机盐值（salt）自动生成哈希，保障密码存储的安全性。
 * 验证时通过 {@link #verify(String, String)} 方法比对明文与哈希值是否匹配。
 * </p>
 *
 * @author team
 * @version 1.0
 */
public class PasswordUtil {

    /**
     * 对明文密码进行 BCrypt 哈希处理。
     * <p>
     * 自动生成随机盐值并混入哈希结果，每次调用生成的哈希值均不相同。
     * 用于用户注册或修改密码时将密码安全存入数据库。
     * </p>
     *
     * @param plainPassword 明文密码字符串
     * @return BCrypt 哈希后的密码字符串（已包含盐值信息）
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * 校验明文密码与 BCrypt 哈希值是否匹配。
     * <p>
     * 从哈希字符串中提取盐值对明文重新哈希，并与存储的哈希值进行比对。
     * 用于用户登录时验证输入的密码是否正确。
     * </p>
     *
     * @param plainPassword 待校验的明文密码字符串
     * @param hashed        数据库中存储的 BCrypt 哈希密码字符串
     * @return 若匹配返回 {@code true}，否则返回 {@code false}
     */
    public static boolean verify(String plainPassword, String hashed) {
        return BCrypt.checkpw(plainPassword, hashed);
    }
}
