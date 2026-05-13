package com.account.model;

/**
 * 用户级别账号管理分配实体 - 对应 account_managers 表。
 * 记录将账号分配给具体用户的操作，包含分配人和分配时间。
 */
public class AccountManager {
    /** 记录ID */
    private int id;
    /** 账号ID */
    private int accountId;
    /** 用户ID */
    private int userId;
    /** 分配人ID */
    private int assignedBy;
    /** 分配时间 */
    private String assignedAt;

    // 联表查询字段
    /** 用户显示名（联表查询） */
    private String userDisplayName;
    /** 用户名（联表查询） */
    private String userName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getAssignedBy() { return assignedBy; }
    public void setAssignedBy(int assignedBy) { this.assignedBy = assignedBy; }
    public String getAssignedAt() { return assignedAt; }
    public void setAssignedAt(String assignedAt) { this.assignedAt = assignedAt; }
    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
