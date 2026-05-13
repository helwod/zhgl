package com.account.model;

/**
 * 用户登录审计日志实体 - 对应 login_logs 表。
 * 记录用户的登录行为，包括登录用户信息及来源IP地址。
 */
public class LoginLog {
    /** 日志ID */
    private int id;
    /** 用户ID */
    private int userId;
    /** 用户名 */
    private String username;
    /** 用户显示名 */
    private String displayName;
    /** 登录IP地址 */
    private String ipAddress;
    /** 登录时间 */
    private String loginTime;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getLoginTime() { return loginTime; }
    public void setLoginTime(String loginTime) { this.loginTime = loginTime; }
}
