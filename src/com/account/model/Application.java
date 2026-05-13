package com.account.model;

/**
 * 账号使用申请实体 - 对应 applications 表。
 * 用户申请某个账号的访问权限，经管理员审批通过后可获得账号使用权限。
 * 申请包含使用天数（valid_days），审批通过后生成 account_assignments 记录用于到期追踪。
 */
public class Application {
    private int id;
    private int accountId;
    private int applicantId;
    private String reason;
    private String status;
    private String reviewComment;
    private Integer reviewedBy;
    private String createdAt;
    private String reviewedAt;
    private Integer validDays; // 申请使用天数，默认7天

    // joined fields
    private String accountName;
    private String applicantName;
    private String reviewerName;

    // account joined fields
    private String project;
    private String platformType;
    private String subType;
    private String loginUrl;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public int getApplicantId() { return applicantId; }
    public void setApplicantId(int applicantId) { this.applicantId = applicantId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getPlatformType() { return platformType; }
    public void setPlatformType(String platformType) { this.platformType = platformType; }
    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }
    public String getLoginUrl() { return loginUrl; }
    public void setLoginUrl(String loginUrl) { this.loginUrl = loginUrl; }

    /** 状态：待审批（英文常量） */
    public static final String STATUS_PENDING = "pending";
    /** 状态：已通过（英文常量） */
    public static final String STATUS_APPROVED = "approved";
    /** 状态：已驳回（英文常量） */
    public static final String STATUS_REJECTED = "rejected";

    /** 状态：待审批（中文展示） */
    public static final String STATUS_CN_PENDING = "待审批";
    /** 状态：已通过（中文展示） */
    public static final String STATUS_CN_APPROVED = "已通过";
    /** 状态：已驳回（中文展示） */
    public static final String STATUS_CN_REJECTED = "已驳回";

    public static String toChinese(String status) {
        switch (status) {
            case STATUS_PENDING: return STATUS_CN_PENDING;
            case STATUS_APPROVED: return STATUS_CN_APPROVED;
            case STATUS_REJECTED: return STATUS_CN_REJECTED;
            default: return status;
        }
    }
}
