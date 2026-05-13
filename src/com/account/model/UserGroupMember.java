package com.account.model;

/**
 * 用户组成员映射实体 - 对应 user_group_members 表。
 * 记录用户与用户组之间的多对多关系，一个用户可属于多个组，一个组可包含多个用户。
 */
public class UserGroupMember {
    /** 记录ID */
    private int id;
    /** 用户组ID */
    private int groupId;
    /** 用户ID */
    private int userId;

    // 联表查询字段
    /** 用户名（联表查询） */
    private String userName;
    /** 用户显示名（联表查询） */
    private String userDisplayName;
    /** 用户组名称（联表查询） */
    private String groupName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
}
