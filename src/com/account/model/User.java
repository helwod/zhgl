package com.account.model;

/**
 * 系统用户实体 - 对应 users 表。
 * 支持三种角色：admin（超级管理员）、manager（管理员）、user（普通用户）。
 * 权限控制基于 role 字段，但超级管理员通过 username="admin" 识别。
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String displayName;
    private String role;
    private String department;
    private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /**
     * 判断是否为管理员角色
     * @return 如果是 admin 角色返回 true
     */
    public boolean isAdmin() {
        return "admin".equals(role);
    }

    /**
     * 判断是否为管理者角色
     * @return 如果是 manager 角色返回 true
     */
    public boolean isManager() {
        return "manager".equals(role);
    }

    /**
     * 判断是否至少拥有管理者权限（包含管理员和管理者）
     * @return 如果是 admin 或 manager 角色返回 true
     */
    public boolean isAtLeastManager() {
        return isAdmin() || isManager();
    }
}
