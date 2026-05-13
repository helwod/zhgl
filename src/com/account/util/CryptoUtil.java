package com.account.util;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256-GCM 加密解密工具类。
 * <p>
 * 提供基于 AES-GCM 模式的对称加密与解密功能，适用于账户密码等敏感数据的加解密操作。
 * 支持通过 Base64 编码的密钥字符串初始化，也支持从系统属性 crypto.key 读取密钥。
 * 加密结果返回密文、初始向量(IV)和认证标签(TAG)三段式数据，解密时需同时提供这三个部分。
 * </p>
 *
 * @author team
 * @version 1.0
 */
public class CryptoUtil {
    /** AES-GCM 加密算法/模式/填充方式 */
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    /** GCM 模式初始向量(IV)长度，单位：字节 */
    private static final int GCM_IV_LENGTH = 12;
    /** GCM 认证标签(TAG)位长度 */
    private static final int GCM_TAG_LENGTH = 128;

    /** AES 密钥对象 */
    private SecretKey secretKey;

    /**
     * 使用指定的 Base64 编码密钥构造加密工具实例。
     *
     * @param base64Key Base64 编码的 AES-256 密钥字符串
     */
    public CryptoUtil(String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 无参构造方法，从系统属性 {@code crypto.key} 读取密钥。
     * <p>
     * 若未配置系统属性，则自动生成一个临时密钥（仅限开发环境使用），
     * 并在控制台输出警告信息。生产环境应通过 {@code web.xml} 的 context-param 配置密钥。
     * </p>
     */
    public CryptoUtil() {
        // Default: generate a key from a known string
        // In production, the key should come from web.xml context-param
        String defaultKey = System.getProperty("crypto.key");
        if (defaultKey == null || defaultKey.isEmpty()) {
            // If no key configured, generate one (for dev only)
            try {
                KeyGenerator kg = KeyGenerator.getInstance("AES");
                kg.init(256);
                this.secretKey = kg.generateKey();
                System.out.println("WARNING: Using auto-generated AES key. Set crypto.key system property for persistence.");
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate AES key", e);
            }
        } else {
            byte[] keyBytes = Base64.getDecoder().decode(defaultKey);
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        }
    }

    /**
     * 对明文进行 AES-256-GCM 加密。
     * <p>
     * 自动生成随机 IV，加密完成后将密文与 GCM 认证标签分离，
     * 返回包含 Base64 编码的【密文、IV、标签】的三元素字符串数组。
     * </p>
     *
     * @param plaintext 待加密的明文字符串
     * @return 包含三个元素的字符串数组：<br>
     *         [0] Base64 编码的密文<br>
     *         [1] Base64 编码的初始向量(IV)<br>
     *         [2] Base64 编码的 GCM 认证标签
     * @throws RuntimeException 加密过程发生异常时抛出
     */
    public String[] encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));
            byte[] tag = ciphertext;
            // In GCM, the tag is appended to the ciphertext
            // We need to extract it
            int tagLenBytes = GCM_TAG_LENGTH / 8;
            byte[] actualCiphertext = new byte[ciphertext.length - tagLenBytes];
            byte[] actualTag = new byte[tagLenBytes];
            System.arraycopy(ciphertext, 0, actualCiphertext, 0, actualCiphertext.length);
            System.arraycopy(ciphertext, ciphertext.length - tagLenBytes, actualTag, 0, tagLenBytes);

            return new String[]{
                Base64.getEncoder().encodeToString(actualCiphertext),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(actualTag)
            };
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * 对 AES-256-GCM 加密的密文进行解密。
     * <p>
     * 将 Base64 编码的密文、IV 和认证标签组合还原为完整密文后进行解密。
     * GCM 模式会在解密时自动验证认证标签，若数据被篡改则会抛出异常。
     * </p>
     *
     * @param encryptedBase64 Base64 编码的密文字符串
     * @param ivBase64        Base64 编码的初始向量(IV)字符串
     * @param tagBase64       Base64 编码的 GCM 认证标签字符串
     * @return 解密后的明文字符串
     * @throws RuntimeException 解密过程发生异常（包括认证失败）时抛出
     */
    public String decrypt(String encryptedBase64, String ivBase64, String tagBase64) {
        try {
            byte[] ciphertext = Base64.getDecoder().decode(encryptedBase64);
            byte[] iv = Base64.getDecoder().decode(ivBase64);
            byte[] tag = Base64.getDecoder().decode(tagBase64);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            // Reconstruct: ciphertext + tag
            byte[] combined = new byte[ciphertext.length + tag.length];
            System.arraycopy(ciphertext, 0, combined, 0, ciphertext.length);
            System.arraycopy(tag, 0, combined, ciphertext.length, tag.length);

            byte[] decrypted = cipher.doFinal(combined);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * 生成一个随机的 AES-256 密钥，并以 Base64 编码形式返回。
     * <p>
     * 可用于生成密钥并在应用配置中使用（如系统属性 crypto.key）。
     * </p>
     *
     * @return Base64 编码的 AES-256 密钥字符串
     * @throws RuntimeException 密钥生成失败时抛出
     */
    public static String generateBase64Key() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(256);
            SecretKey key = kg.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }
}
