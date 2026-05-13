<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.account.model.User, com.account.model.PasswordLog, com.account.model.Application" %>
<%
    User user = (User) session.getAttribute("user");
    Integer totalAccounts = (Integer) request.getAttribute("totalAccounts");
    Integer pendingApps = (Integer) request.getAttribute("pendingApps");
    Integer totalApps = (Integer) request.getAttribute("totalApps");
    Integer totalLogs = (Integer) request.getAttribute("totalLogs");
    Integer totalUsers = (Integer) request.getAttribute("totalUsers");
    List<PasswordLog> recentLogs = (List<PasswordLog>) request.getAttribute("recentLogs");
    List<Application> recentApps = (List<Application>) request.getAttribute("recentApps");
    if (totalAccounts == null) totalAccounts = 0;
    if (pendingApps == null) pendingApps = 0;
    if (totalApps == null) totalApps = 0;
    if (totalLogs == null) totalLogs = 0;
    if (totalUsers == null) totalUsers = 0;
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>仪表盘 - 企业外部账号管理</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="admin-layout">
    <!-- Sidebar -->
    <div class="sidebar">
        <div class="sidebar-header">
            <h2>📋 账号管理</h2>
            <p>Enterprise Account Manager</p>
        </div>
        <nav class="sidebar-nav">
            <a href="${pageContext.request.contextPath}/dashboard" class="active">
                <span class="nav-icon">📊</span><span>仪表盘</span>
            </a>
            <a href="${pageContext.request.contextPath}/accounts">
                <span class="nav-icon">📁</span><span>账号管理</span>
            </a>
            <% if (user.isAdmin()) { %>
            <a href="${pageContext.request.contextPath}/users">
                <span class="nav-icon">👤</span><span>用户管理</span>
            </a>
            <a href="${pageContext.request.contextPath}/groups">
                <span class="nav-icon">👥</span><span>用户组管理</span>
            </a>
            <a href="${pageContext.request.contextPath}/platforms">
                <span class="nav-icon">🖥️</span><span>平台管理</span>
            </a>
            <a href="${pageContext.request.contextPath}/applications/pending">
                <span class="nav-icon">📝</span><span>审批管理</span>
            </a>
            <a href="${pageContext.request.contextPath}/logs">
                <span class="nav-icon">📋</span><span>操作日志</span>
            </a>
            <% } else if (user.isManager()) { %>
            <a href="${pageContext.request.contextPath}/applications/pending">
                <span class="nav-icon">📝</span><span>审批管理</span>
            </a>
            <a href="${pageContext.request.contextPath}/logs">
                <span class="nav-icon">📋</span><span>操作日志</span>
            </a>
            <% } else { %>
            <a href="${pageContext.request.contextPath}/applications/my">
                <span class="nav-icon">📝</span><span>我的申请</span>
            </a>
            <% } %>
        </nav>
        <div class="sidebar-footer">
            <a href="${pageContext.request.contextPath}/logout">🚪 <span>退出登录</span></a>
        </div>
    </div>

    <!-- Main Content -->
    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">仪表盘</div>
            <div class="topbar-right">
                <div class="topbar-user">
                    <div class="user-avatar"><%= user.getDisplayName().charAt(0) %></div>
                    <div class="user-info">
                        <div class="user-name"><%= user.getDisplayName() %></div>
                        <div class="user-role"><%= user.isAdmin() ? "超级管理员" : user.isManager() ? "管理员" : "普通用户" %></div>
                    </div>
                </div>
            </div>
        </div>
        <div class="content-area">
            <!-- Stats Cards -->
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-icon">📁</div>
                    <div class="stat-value"><%= totalAccounts %></div>
                    <div class="stat-label">账号总数</div>
                </div>
                <% if (user.isAdmin() || user.isManager()) { %>
                <div class="stat-card">
                    <div class="stat-icon">⏳</div>
                    <div class="stat-value" style="color: #faad14;"><%= pendingApps %></div>
                    <div class="stat-label">待审批申请</div>
                </div>
                <% } %>
                <% if (user.isAdmin()) { %>
                <div class="stat-card">
                    <div class="stat-icon">👤</div>
                    <div class="stat-value"><%= totalUsers %></div>
                    <div class="stat-label">用户总数</div>
                </div>
                <% } %>
                <div class="stat-card">
                    <div class="stat-icon">📋</div>
                    <div class="stat-value"><%= totalApps %></div>
                    <div class="stat-label">申请总数</div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon">🔍</div>
                    <div class="stat-value"><%= totalLogs %></div>
                    <div class="stat-label">密码查看记录</div>
                </div>
            </div>

            <!-- Quick Actions -->
            <h3 class="section-title">快捷操作</h3>
            <div style="display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap;">
                <a href="${pageContext.request.contextPath}/accounts" class="btn btn-primary">📁 查看账号列表</a>
                <% if (user.isAdmin()) { %>
                <a href="${pageContext.request.contextPath}/accounts/new" class="btn btn-success">➕ 新增账号</a>
                <a href="${pageContext.request.contextPath}/applications/pending" class="btn btn-warning">📝 审批申请</a>
                <% } else if (user.isManager()) { %>
                <a href="${pageContext.request.contextPath}/applications/pending" class="btn btn-warning">📝 审批申请</a>
                <a href="${pageContext.request.contextPath}/accounts/new" class="btn btn-success">➕ 新增账号</a>
                <% } else { %>
                <a href="${pageContext.request.contextPath}/applications/my" class="btn btn-primary">📝 我的申请</a>
                <% } %>
            </div>

            <!-- Recent Activity -->
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                <!-- Recent Applications -->
                <div class="table-container">
                    <div style="padding: 12px 16px; border-bottom: 1px solid #e8e8e8; font-weight: 600; font-size: 14px;">
                        最近申请
                    </div>
                    <% if (recentApps != null && !recentApps.isEmpty()) { %>
                    <table>
                        <thead>
                            <tr>
                                <th>账号</th>
                                <th>申请人</th>
                                <th>连接信息</th>
                                <th>所属项目</th>
                                <th>平台类型</th>
                                <th>运维子类型</th>
                                <th>状态</th>
                                <th>时间</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% for (Application a : recentApps) { %>
                            <tr>
                                <td><%= a.getAccountName() != null ? a.getAccountName() : "#" + a.getAccountId() %></td>
                                <td><%= a.getApplicantName() != null ? a.getApplicantName() : "用户#" + a.getApplicantId() %></td>
                                <td style="font-size:12px;"><%= a.getLoginUrl() != null && !a.getLoginUrl().isEmpty() ? a.getLoginUrl() : "-" %></td>
                                <td><%= a.getProject() != null && !a.getProject().isEmpty() ? a.getProject() : "-" %></td>
                                <td><%= a.getPlatformType() != null ? a.getPlatformType() : "-" %></td>
                                <td><%= a.getSubType() != null && !a.getSubType().isEmpty() ? a.getSubType() : "-" %></td>
                                <td>
                                    <% if ("pending".equals(a.getStatus())) { %>
                                        <span class="badge badge-warning">待审批</span>
                                    <% } else if ("approved".equals(a.getStatus())) { %>
                                        <span class="badge badge-success">已通过</span>
                                    <% } else { %>
                                        <span class="badge badge-danger">已驳回</span>
                                    <% } %>
                                </td>
                                <td style="color: #999; font-size: 12px;"><%= a.getCreatedAt() != null ? a.getCreatedAt().substring(0, 16) : "" %></td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                    <% } else { %>
                    <div class="empty-state"><p>暂无申请记录</p></div>
                    <% } %>
                </div>

                <!-- Recent Password Logs (admin only) -->
                <% if (user.isAdmin()) { %>
                <div class="table-container">
                    <div style="padding: 12px 16px; border-bottom: 1px solid #e8e8e8; font-weight: 600; font-size: 14px;">
                        最近密码查看记录
                    </div>
                    <% if (recentLogs != null && !recentLogs.isEmpty()) { %>
                    <table>
                        <thead>
                            <tr>
                                <th>账号</th>
                                <th>查看人</th>
                                <th>连接信息</th>
                                <th>所属项目</th>
                                <th>平台类型</th>
                                <th>运维子类型</th>
                                <th>时间</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% for (PasswordLog log : recentLogs) { %>
                            <tr>
                                <td><%= log.getAccountName() != null ? log.getAccountName() : "#" + log.getAccountId() %></td>
                                <td><%= log.getViewerName() %></td>
                                <td style="font-size:12px;"><%= log.getLoginUrl() != null && !log.getLoginUrl().isEmpty() ? log.getLoginUrl() : "-" %></td>
                                <td><%= log.getProject() != null && !log.getProject().isEmpty() ? log.getProject() : "-" %></td>
                                <td><%= log.getPlatformType() != null ? log.getPlatformType() : "-" %></td>
                                <td><%= log.getSubType() != null && !log.getSubType().isEmpty() ? log.getSubType() : "-" %></td>
                                <td style="color: #999; font-size: 12px;"><%= log.getViewedAt() != null ? log.getViewedAt().substring(0, 16) : "" %></td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                    <% } else { %>
                    <div class="empty-state"><p>暂无查看记录</p></div>
                    <% } %>
                </div>
                <% } %>
            </div>
        </div>
    </div>
</div>
</body>
</html>
