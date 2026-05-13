package com.account.model;

/**
 * 账号实体类 - 对应 accounts 表。
 * 存储外部账号的名称、加密密码、平台类型、到期时间等信息。
 * 密码使用 AES-256-GCM 加密存储，分为加密内容、IV、认证标签三部分。
 */
public class Account {
    /** 账号ID */
    private int id;
    /** 账号名称 */
    private String name;
    /** 加密后的密码内容 */
    private String passwordEncrypted;
    /** 加密IV（初始化向量） */
    private String passwordIv;
    /** 加密认证标签 */
    private String passwordTag;
    /** 平台类型标识 */
    private String platformType;
    /** 运维子类型：服务器/数据库/项目 */
    private String subType;
    /** 所属项目 */
    private String project;
    /** 所属部门 */
    private String department;
    /** 到期日期 */
    private String expiryDate;
    /** 状态：可用/已分配/已过期 */
    private String status;
    /** 登录地址 */
    private String loginUrl;
    /** 备注说明 */
    private String notes;
    /** 创建人ID */
    private int createdBy;
    /** 创建时间 */
    private String createdAt;
    /** 更新时间 */
    private String updatedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPasswordEncrypted() { return passwordEncrypted; }
    public void setPasswordEncrypted(String passwordEncrypted) { this.passwordEncrypted = passwordEncrypted; }
    public String getPasswordIv() { return passwordIv; }
    public void setPasswordIv(String passwordIv) { this.passwordIv = passwordIv; }
    public String getPasswordTag() { return passwordTag; }
    public void setPasswordTag(String passwordTag) { this.passwordTag = passwordTag; }
    public String getPlatformType() { return platformType; }
    public void setPlatformType(String platformType) { this.platformType = platformType; }
    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLoginUrl() { return loginUrl; }
    public void setLoginUrl(String loginUrl) { this.loginUrl = loginUrl; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    /** 运维子类型可选值 */
    public static final String[] SUB_TYPES = {"服务器", "数据库", "其他"};

    /** 状态：可用 */
    public static final String STATUS_AVAILABLE = "可用";
    /** 状态：已分配 */
    public static final String STATUS_ASSIGNED = "已分配";
    /** 状态：已过期 */
    public static final String STATUS_EXPIRED = "已过期";
}
