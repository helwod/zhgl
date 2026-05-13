package com.account.model;

/**
 * 用户-账号分配记录实体 - 对应 account_assignments 表。
 * 记录用户与账号之间的分配关系，包含分配到期时间（用于审批到期追踪）。
 */
public class AccountAssignment {
    /** 记录ID */
    private int id;
    /** 账号ID */
    private int accountId;
    /** 用户ID */
    private int userId;
    /** 分配到期日期（用于审批到期追踪） */
    private String expiryDate;
    /** 创建时间 */
    private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
