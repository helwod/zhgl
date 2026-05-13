package com.account.model;

/**
 * 密码历史记录实体 - 对应 password_history 表。
 * 记录账号密码的历史变更情况，每次修改密码时保存一份加密快照用于回溯。
 */
public class PasswordHistory {
    /** 记录ID */
    private int id;
    /** 账号ID */
    private int accountId;
    /** 历史加密密码内容 */
    private String passwordEncrypted;
    /** 历史加密IV（初始化向量） */
    private String passwordIv;
    /** 历史加密认证标签 */
    private String passwordTag;
    /** 修改人ID */
    private int changedBy;
    /** 修改人姓名 */
    private String changedName;
    /** 修改时间 */
    private String changedAt;

    // 临时字段，仅用于展示
    /** 解密后的密码（临时字段，仅用于展示） */
    private String decryptedPassword;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getPasswordEncrypted() { return passwordEncrypted; }
    public void setPasswordEncrypted(String passwordEncrypted) { this.passwordEncrypted = passwordEncrypted; }
    public String getPasswordIv() { return passwordIv; }
    public void setPasswordIv(String passwordIv) { this.passwordIv = passwordIv; }
    public String getPasswordTag() { return passwordTag; }
    public void setPasswordTag(String passwordTag) { this.passwordTag = passwordTag; }
    public int getChangedBy() { return changedBy; }
    public void setChangedBy(int changedBy) { this.changedBy = changedBy; }
    public String getChangedName() { return changedName; }
    public void setChangedName(String changedName) { this.changedName = changedName; }
    public String getChangedAt() { return changedAt; }
    public void setChangedAt(String changedAt) { this.changedAt = changedAt; }
    public String getDecryptedPassword() { return decryptedPassword; }
    public void setDecryptedPassword(String decryptedPassword) { this.decryptedPassword = decryptedPassword; }
}
