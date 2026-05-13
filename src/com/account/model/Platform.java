package com.account.model;

/**
 * 平台类型字典实体 - 对应 platforms 表。
 * 定义账号所属的平台类型，如阿里云、腾讯云、GitHub 等。
 */
public class Platform {
    /** 平台ID */
    private int id;
    /** 平台名称 */
    private String name;
    /** 创建时间 */
    private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
