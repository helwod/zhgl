package com.account.model;

/**
 * 用户组级别账号管理分配实体 - 对应 account_group_managers 表。
 * 记录将账号分配到用户组的操作，包含分配人和分配时间。
 */
public class AccountGroupManager {
    /** 记录ID */
    private int id;
    /** 账号ID */
    private int accountId;
    /** 用户组ID */
    private int groupId;
    /** 分配人ID */
    private int assignedBy;
    /** 分配时间 */
    private String assignedAt;

    // 联表查询字段
    /** 用户组名称（联表查询） */
    private String groupName;
    /** 组成员数量（联表查询） */
    private int memberCount;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public int getAssignedBy() { return assignedBy; }
    public void setAssignedBy(int assignedBy) { this.assignedBy = assignedBy; }
    public String getAssignedAt() { return assignedAt; }
    public void setAssignedAt(String assignedAt) { this.assignedAt = assignedAt; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
}
