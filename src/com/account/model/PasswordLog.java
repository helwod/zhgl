package com.account.model;

/**
 * 密码查看/导出审计日志实体 - 对应 password_logs 表。
 * 记录用户查看或导出密码的操作，用于安全审计追溯。
 */
public class PasswordLog {
    /** 日志ID */
    private int id;
    /** 账号ID */
    private int accountId;
    /** 查看人ID */
    private int viewerId;
    /** 查看人姓名 */
    private String viewerName;
    /** 查看/导出时间 */
    private String viewedAt;
    /** 操作类型：view（查看）/ export（导出） */
    private String actionType;

    // 联表查询字段
    /** 账号名称（联表查询） */
    private String accountName;
    /** 所属项目（联表查询） */
    private String project;
    /** 平台类型（联表查询） */
    private String platformType;
    /** 运维子类型（联表查询） */
    private String subType;
    /** 连接信息（联表查询） */
    private String loginUrl;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public int getViewerId() { return viewerId; }
    public void setViewerId(int viewerId) { this.viewerId = viewerId; }
    public String getViewerName() { return viewerName; }
    public void setViewerName(String viewerName) { this.viewerName = viewerName; }
    public String getViewedAt() { return viewedAt; }
    public void setViewedAt(String viewedAt) { this.viewedAt = viewedAt; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getPlatformType() { return platformType; }
    public void setPlatformType(String platformType) { this.platformType = platformType; }
    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }
    public String getLoginUrl() { return loginUrl; }
    public void setLoginUrl(String loginUrl) { this.loginUrl = loginUrl; }
}
