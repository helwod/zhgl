<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.account.model.User, com.account.model.Application, com.account.dao.AccountAssignmentDAO" %>
<%
    User user = (User) session.getAttribute("user");
    List<Application> pending = (List<Application>) request.getAttribute("pendingApps");
    List<Application> reviewed = (List<Application>) request.getAttribute("reviewedApps");
    List<Application> rejected = (List<Application>) request.getAttribute("rejectedApps");
    String ctx = request.getContextPath();
    String tab = request.getParameter("tab");
    if (tab == null) tab = "pending";
    String validOnly = request.getParameter("valid_only");
    boolean filterValid = "1".equals(validOnly);
    AccountAssignmentDAO aaDAO = new AccountAssignmentDAO();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>审批管理 - 企业外部账号管理</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="admin-layout">
    <div class="sidebar">
        <div class="sidebar-header"><h2>📋 账号管理</h2><p>Enterprise Account Manager</p></div>
        <nav class="sidebar-nav">
            <a href="${pageContext.request.contextPath}/dashboard"><span class="nav-icon">📊</span><span>仪表盘</span></a>
            <% if (user.isAdmin() || user.isManager()) { %>
            <a href="${pageContext.request.contextPath}/accounts"><span class="nav-icon">📁</span><span>账号管理</span></a>
            <% } %>
            <% if (user.isAdmin()) { %>
            <a href="${pageContext.request.contextPath}/users"><span class="nav-icon">👤</span><span>用户管理</span></a>
            <a href="${pageContext.request.contextPath}/groups"><span class="nav-icon">👥</span><span>用户组管理</span></a>
            <a href="${pageContext.request.contextPath}/platforms"><span class="nav-icon">🖥️</span><span>平台管理</span></a>
            <% } %>
            <a href="${pageContext.request.contextPath}/applications/pending" class="active"><span class="nav-icon">📝</span><span>审批管理</span></a>
            <a href="${pageContext.request.contextPath}/logs"><span class="nav-icon">📋</span><span>操作日志</span></a>
        </nav>
        <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/logout">🚪 <span>退出登录</span></a></div>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">审批管理</div>
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
                <!-- Tabs -->
                <div style="padding: 12px 16px 0; border-bottom: 1px solid #e8e8e8;">
                    <div class="tabs" style="margin-bottom:0;">
                        <a href="?tab=pending" class="tab-item <%= "pending".equals(tab) ? "active" : "" %>">待审批 (<%= pending != null ? pending.size() : 0 %>)</a>
                        <a href="?tab=approved" class="tab-item <%= "approved".equals(tab) ? "active" : "" %>">已通过 (<%= reviewed != null ? reviewed.size() : 0 %>)</a>
                        <a href="?tab=rejected" class="tab-item <%= "rejected".equals(tab) ? "active" : "" %>">已驳回 (<%= rejected != null ? rejected.size() : 0 %>)</a>
                    </div>
                    <% if ("approved".equals(tab)) { %>
                    <div style="padding: 8px 0;">
                        <label style="font-size:13px;cursor:pointer;">
                            <input type="checkbox" onchange="toggleValidOnly(this.checked)" <%= filterValid ? "checked" : "" %>> 仅显示可用（未过期）
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
                    <% } %>
                </div>

                <!-- Pending Tab -->
                <% if ("pending".equals(tab)) { %>
                <table>
                    <thead>
                        <tr>
                            <th>账号名称</th>
                            <th>连接信息</th>
                            <th>所属项目</th>
                            <th>平台类型</th>
                            <th>运维子类型</th>
                            <th>申请人</th>
                            <th>申请原因</th>
                            <th>使用天数</th>
                            <th>申请时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (pending != null && !pending.isEmpty()) {
                        for (Application a : pending) { %>
                        <tr>
                            <td><a href="<%= ctx %>/accounts/detail?id=<%= a.getAccountId() %>"><%= a.getAccountName() != null ? a.getAccountName() : "#" + a.getAccountId() %></a></td>
                            <td style="font-size:12px;"><%= a.getLoginUrl() != null && !a.getLoginUrl().isEmpty() ? a.getLoginUrl() : "-" %></td>
                            <td><%= a.getProject() != null && !a.getProject().isEmpty() ? a.getProject() : "-" %></td>
                            <td><%= a.getPlatformType() != null ? a.getPlatformType() : "-" %></td>
                            <td><%= a.getSubType() != null && !a.getSubType().isEmpty() ? a.getSubType() : "-" %></td>
                            <td><%= a.getApplicantName() != null ? a.getApplicantName() : "用户#" + a.getApplicantId() %></td>
                            <td><%= a.getReason() != null ? a.getReason() : "-" %></td>
                            <td><%= a.getValidDays() != null ? a.getValidDays() + "天" : "7天" %></td>
                            <td style="color:#999;font-size:12px;"><%= a.getCreatedAt() != null ? a.getCreatedAt().substring(0, 16) : "" %></td>
                            <td>
                                <div class="flex gap-2" style="align-items: center;">
                                    <form method="post" action="<%= ctx %>/applications/review" style="display:inline;" onsubmit="return confirm('确认审批通过？')">
                                        <input type="hidden" name="id" value="<%= a.getId() %>">
                                        <input type="hidden" name="action" value="approve">
                                        <input type="text" name="comment" placeholder="审批意见（可选）" style="padding:4px 8px;border:1px solid #d9d9d9;border-radius:4px;font-size:12px;width:90px;">
                                        <button type="submit" class="btn btn-sm btn-success">✅ 通过</button>
                                    </form>
                                    <form method="post" action="<%= ctx %>/applications/review" style="display:inline;" onsubmit="return confirm('确认驳回该申请？')">
                                        <input type="hidden" name="id" value="<%= a.getId() %>">
                                        <input type="hidden" name="action" value="reject">
                                        <input type="text" name="comment" placeholder="驳回原因（必填）" style="padding:4px 8px;border:1px solid #d9d9d9;border-radius:4px;font-size:12px;width:90px;" required>
                                        <button type="submit" class="btn btn-sm btn-danger">❌ 驳回</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="10"><div class="empty-state"><p>🎉 暂无待审批的申请</p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
                <% } %>

                <!-- Approved Tab -->
                <% if ("approved".equals(tab)) { %>
                <table>
                    <thead>
                        <tr>
                            <th>账号名称</th>
                            <th>连接信息</th>
                            <th>所属项目</th>
                            <th>平台类型</th>
                            <th>运维子类型</th>
                            <th>申请人</th>
                            <th>申请原因</th>
                            <th>使用天数</th>
                            <th>状态</th>
                            <th>审批人</th>
                            <th>意见</th>
                            <th>审批时间</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (reviewed != null && !reviewed.isEmpty()) {
                        for (Application a : reviewed) {
                            // Check validity via account_assignments table
                            boolean isValid = aaDAO.hasValidAssignment(a.getAccountId(), a.getApplicantId());
                            // Skip expired entries when "valid only" filter is active
                            if (filterValid && !isValid) continue;
                        %>
                        <tr>
                            <td><a href="<%= ctx %>/accounts/detail?id=<%= a.getAccountId() %>"><%= a.getAccountName() != null ? a.getAccountName() : "#" + a.getAccountId() %></a></td>
                            <td style="font-size:12px;"><%= a.getLoginUrl() != null && !a.getLoginUrl().isEmpty() ? a.getLoginUrl() : "-" %></td>
                            <td><%= a.getProject() != null && !a.getProject().isEmpty() ? a.getProject() : "-" %></td>
                            <td><%= a.getPlatformType() != null ? a.getPlatformType() : "-" %></td>
                            <td><%= a.getSubType() != null && !a.getSubType().isEmpty() ? a.getSubType() : "-" %></td>
                            <td><%= a.getApplicantName() != null ? a.getApplicantName() : "用户#" + a.getApplicantId() %></td>
                            <td><%= a.getReason() != null ? a.getReason() : "-" %></td>
                            <td><%= a.getValidDays() != null ? a.getValidDays() + "天" : "7天" %></td>
                            <td>
                                <% if (isValid) { %>
                                    <span class="badge badge-success">可用</span>
                                <% } else { %>
                                    <span class="badge badge-danger">已过期</span>
                                <% } %>
                            </td>
                            <td><%= a.getReviewerName() != null ? a.getReviewerName() : "-" %></td>
                            <td><%= a.getReviewComment() != null && !a.getReviewComment().isEmpty() ? a.getReviewComment() : "-" %></td>
                            <td style="color:#999;font-size:12px;"><%= a.getReviewedAt() != null ? a.getReviewedAt().substring(0, 16) : "" %></td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="12"><div class="empty-state"><p><%= filterValid ? "暂无有效（未过期）的已通过申请" : "暂无已通过的申请" %></p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
                <% } %>

                <!-- Rejected Tab -->
                <% if ("rejected".equals(tab)) { %>
                <table>
                    <thead>
                        <tr>
                            <th>账号名称</th>
                            <th>连接信息</th>
                            <th>所属项目</th>
                            <th>平台类型</th>
                            <th>运维子类型</th>
                            <th>申请人</th>
                            <th>申请原因</th>
                            <th>使用天数</th>
                            <th>审批人</th>
                            <th>驳回原因</th>
                            <th>审批时间</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (rejected != null && !rejected.isEmpty()) {
                        for (Application a : rejected) { %>
                        <tr>
                            <td><a href="<%= ctx %>/accounts/detail?id=<%= a.getAccountId() %>"><%= a.getAccountName() != null ? a.getAccountName() : "#" + a.getAccountId() %></a></td>
                            <td style="font-size:12px;"><%= a.getLoginUrl() != null && !a.getLoginUrl().isEmpty() ? a.getLoginUrl() : "-" %></td>
                            <td><%= a.getProject() != null && !a.getProject().isEmpty() ? a.getProject() : "-" %></td>
                            <td><%= a.getPlatformType() != null ? a.getPlatformType() : "-" %></td>
                            <td><%= a.getSubType() != null && !a.getSubType().isEmpty() ? a.getSubType() : "-" %></td>
                            <td><%= a.getApplicantName() != null ? a.getApplicantName() : "用户#" + a.getApplicantId() %></td>
                            <td><%= a.getReason() != null ? a.getReason() : "-" %></td>
                            <td><%= a.getValidDays() != null ? a.getValidDays() + "天" : "7天" %></td>
                            <td><%= a.getReviewerName() != null ? a.getReviewerName() : "-" %></td>
                            <td style="color:#ff4d4f;"><%= a.getReviewComment() != null ? a.getReviewComment() : "-" %></td>
                            <td style="color:#999;font-size:12px;"><%= a.getReviewedAt() != null ? a.getReviewedAt().substring(0, 16) : "" %></td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="11"><div class="empty-state"><p>暂无已驳回的申请</p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
                <% } %>
            </div>
        </div>
    </div>
</div>
</body>
</html>
