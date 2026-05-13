<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.account.model.User, com.account.model.Application, com.account.dao.AccountAssignmentDAO" %>
<%
    User user = (User) session.getAttribute("user");
    List<Application> apps = (List<Application>) request.getAttribute("applications");
    String ctx = request.getContextPath();
    String validOnly = request.getParameter("valid_only");
    boolean filterValid = "1".equals(validOnly);
    AccountAssignmentDAO aaDAO = new AccountAssignmentDAO();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>我的申请 - 企业外部账号管理</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="admin-layout">
    <div class="sidebar">
        <div class="sidebar-header"><h2>📋 账号管理</h2><p>Enterprise Account Manager</p></div>
        <nav class="sidebar-nav">
            <a href="${pageContext.request.contextPath}/dashboard"><span class="nav-icon">📊</span><span>仪表盘</span></a>
            <a href="${pageContext.request.contextPath}/accounts"><span class="nav-icon">📁</span><span>账号管理</span></a>
            <a href="${pageContext.request.contextPath}/applications/my" class="active"><span class="nav-icon">📝</span><span>我的申请</span></a>
        </nav>
        <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/logout">🚪 <span>退出登录</span></a></div>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">我的申请</div>
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
            <div class="table-container">
                <div style="padding: 12px 16px; border-bottom: 1px solid #e8e8e8; font-weight: 600; font-size: 14px; display: flex; justify-content: space-between; align-items: center;">
                    <span>我的申请记录</span>
                    <label style="font-size:13px;cursor:pointer;font-weight:normal;">
                        <input type="checkbox" onchange="toggleValidOnly(this.checked)" <%= filterValid ? "checked" : "" %>> 仅显示未过期
                    </label>
                </div>
                <script>
                function toggleValidOnly(checked) {
                    var url = new URL(window.location.href);
                    if (checked) url.searchParams.set('valid_only', '1');
                    else url.searchParams.delete('valid_only');
                    window.location.href = url.toString();
                }
                </script>
                <table>
                    <thead>
                        <tr>
                            <th>账号名称</th>
                            <th>申请原因</th>
                            <th>状态</th>
                            <th>审批意见</th>
                            <th>申请时间</th>
                            <th>审批时间</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (apps != null && !apps.isEmpty()) {
                        for (Application a : apps) {
                            // Check validity for approved applications
                            boolean isValid = "approved".equals(a.getStatus()) && aaDAO.hasValidAssignment(a.getAccountId(), a.getApplicantId());
                            // Skip expired entries when "valid only" filter is active
                            if (filterValid && "approved".equals(a.getStatus()) && !isValid) continue;
                        %>
                        <tr>
                            <td><a href="<%= ctx %>/accounts/detail?id=<%= a.getAccountId() %>"><%= a.getAccountName() != null ? a.getAccountName() : "#" + a.getAccountId() %></a></td>
                            <td><%= a.getReason() != null ? a.getReason() : "-" %></td>
                            <td>
                                <% if ("pending".equals(a.getStatus())) { %>
                                    <span class="badge badge-warning">⏳ 待审批</span>
                                <% } else if ("approved".equals(a.getStatus())) { %>
                                    <% if (isValid) { %>
                                        <span class="badge badge-success">✅ 已通过</span>
                                    <% } else { %>
                                        <span class="badge badge-danger">⚠️ 已过期</span>
                                    <% } %>
                                <% } else { %>
                                    <span class="badge badge-danger">❌ 已驳回</span>
                                <% } %>
                            </td>
                            <td><%= a.getReviewComment() != null && !a.getReviewComment().isEmpty() ? a.getReviewComment() : "-" %></td>
                            <td style="color:#999;font-size:12px;"><%= a.getCreatedAt() != null ? a.getCreatedAt().substring(0, 16) : "" %></td>
                            <td style="color:#999;font-size:12px;"><%= a.getReviewedAt() != null ? a.getReviewedAt().substring(0, 16) : "-" %></td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="6"><div class="empty-state"><p><%= filterValid ? "暂无未过期的申请记录" : "暂无申请记录" %></p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>
