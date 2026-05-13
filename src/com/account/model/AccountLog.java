package com.account.model;

/**
 * 账号操作审计日志实体 - 对应 account_logs 表。
 * 记录账号的创建、更新、删除等变更操作，包括变更前后的字段值及操作人信息。
 */
public class AccountLog {
    /** 日志ID */
    private int id;
    /** 账号ID */
    private int accountId;
    /** 操作类型：create（创建）/ update（更新）/ delete（删除） */
    private String actionType;
    /** 变更字段名 */
    private String fieldName;
    /** 变更前值 */
    private String oldValue;
    /** 变更后值 */
    private String newValue;
    /** 操作人ID */
    private int operatorId;
    /** 操作人姓名 */
    private String operatorName;
    /** 操作时间 */
    private String createdAt;

    // 联表查询字段
    /** 账号名称（联表查询） */
    private String accountName;

    // 账号联表字段
    /** 所属项目（联表查询） */
    private String project;
    /** 平台类型（联表查询） */
    private String platformType;
    /** 运维子类型（联表查询） */
    private String subType;
    /** 登录地址（联表查询） */
    private String loginUrl;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public int getOperatorId() { return operatorId; }
    public void setOperatorId(int operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
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
