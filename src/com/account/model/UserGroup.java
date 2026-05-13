package com.account.model;

/**
 * 用户组实体 - 对应 user_groups 表。
 * 定义用户分组，用于批量管理账号分配权限。
 */
public class UserGroup {
    /** 用户组ID */
    private int id;
    /** 用户组名称 */
    private String name;
    /** 用户组描述 */
    private String description;
    /** 创建时间 */
    private String createdAt;

    // 联表查询字段
    /** 组成员数量（联表查询） */
    private int memberCount;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
}
