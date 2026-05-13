<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.account.model.User, com.account.model.PasswordLog, com.account.model.Application, com.account.model.LoginLog, com.account.model.AccountLog" %>
<%
    User user = (User) session.getAttribute("user");
    String type = (String) request.getAttribute("type");
    if (type == null) type = "password";
    String keyword = (String) request.getAttribute("keyword");
    String dateFrom = (String) request.getAttribute("dateFrom");
    String dateTo = (String) request.getAttribute("dateTo");
    String statusFilter = (String) request.getAttribute("statusFilter");
    Integer total = (Integer) request.getAttribute("total");
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    if (total == null) total = 0;
    if (currentPage == null) currentPage = 1;
    if (totalPages == null) totalPages = 1;
    String ctx = request.getContextPath();

    // Data for each type
    List<PasswordLog> logs = (List<PasswordLog>) request.getAttribute("logs");
    List<Application> appLogs = (List<Application>) request.getAttribute("appLogs");
    List<LoginLog> loginLogs = (List<LoginLog>) request.getAttribute("loginLogs");
    List<AccountLog> accountLogs = (List<AccountLog>) request.getAttribute("accountLogs");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>操作日志 - 企业外部账号管理</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
    .search-form { display: flex; gap: 8px; flex-wrap: wrap; align-items: flex-end; padding: 12px 16px; border-bottom: 1px solid #e8e8e8; }
    .search-form .form-group { margin-bottom: 0; }
    .search-form label { font-size: 12px; color: #666; display: block; margin-bottom: 2px; }
    .search-form input, .search-form select { padding: 6px 10px; border: 1px solid #d9d9d9; border-radius: 4px; font-size: 13px; }
    .search-form input[type="text"] { width: 160px; }
    .search-form input[type="date"] { width: 140px; }
    </style>
</head>
<body>
<div class="admin-layout">
    <div class="sidebar">
        <div class="sidebar-header"><h2>📋 账号管理</h2><p>Enterprise Account Manager</p></div>
        <nav class="sidebar-nav">
            <a href="${pageContext.request.contextPath}/dashboard"><span class="nav-icon">📊</span><span>仪表盘</span></a>
            <a href="${pageContext.request.contextPath}/accounts"><span class="nav-icon">📁</span><span>账号管理</span></a>
            <% if (user.isAdmin()) { %>
            <a href="${pageContext.request.contextPath}/users"><span class="nav-icon">👤</span><span>用户管理</span></a>
            <a href="${pageContext.request.contextPath}/groups"><span class="nav-icon">👥</span><span>用户组管理</span></a>
            <a href="${pageContext.request.contextPath}/platforms"><span class="nav-icon">🖥️</span><span>平台管理</span></a>
            <% } %>
            <% if (user.isAdmin() || user.isManager()) { %>
            <a href="${pageContext.request.contextPath}/applications/pending"><span class="nav-icon">📝</span><span>审批管理</span></a>
            <% } %>
            <a href="${pageContext.request.contextPath}/logs" class="active"><span class="nav-icon">📋</span><span>操作日志</span></a>
        </nav>
        <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/logout">🚪 <span>退出登录</span></a></div>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">操作日志</div>
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
                        <a href="?type=password" class="tab-item <%= "password".equals(type) ? "active" : "" %>">🔑 密码查看记录</a>
                        <a href="?type=application" class="tab-item <%= "application".equals(type) ? "active" : "" %>">📝 申请记录</a>
                        <% if (user.isAdmin()) { %>
                        <a href="?type=login" class="tab-item <%= "login".equals(type) ? "active" : "" %>">🔐 登录记录</a>
                        <% } %>
                        <a href="?type=account" class="tab-item <%= "account".equals(type) ? "active" : "" %>">📋 修改日志</a>
                    </div>
                </div>

                <!-- Search Form -->
                <form class="search-form" method="get" action="<%= ctx %>/logs">
                    <input type="hidden" name="type" value="<%= type %>">
                    <div class="form-group">
                        <label>关键词</label>
                        <input type="text" name="keyword" placeholder="搜索..." value="<%= keyword != null ? keyword : "" %>">
                    </div>
                    <% if ("account".equals(type)) { %>
                    <div class="form-group">
                        <label>操作类型</label>
                        <select name="status">
                            <option value="all" <%= "all".equals(statusFilter) || statusFilter == null ? "selected" : "" %>>全部</option>
                            <option value="create" <%= "create".equals(statusFilter) ? "selected" : "" %>>创建</option>
                            <option value="update" <%= "update".equals(statusFilter) ? "selected" : "" %>>修改</option>
                            <option value="delete" <%= "delete".equals(statusFilter) ? "selected" : "" %>>删除</option>
                        </select>
                    </div>
                    <% } %>
                    <% if ("application".equals(type)) { %>
                    <div class="form-group">
                        <label>状态</label>
                        <select name="status">
                            <option value="all" <%= "all".equals(statusFilter) || statusFilter == null ? "selected" : "" %>>全部</option>
                            <option value="pending" <%= "pending".equals(statusFilter) ? "selected" : "" %>>待审批</option>
                            <option value="approved" <%= "approved".equals(statusFilter) ? "selected" : "" %>>已通过</option>
                            <option value="rejected" <%= "rejected".equals(statusFilter) ? "selected" : "" %>>已驳回</option>
                        </select>
                    </div>
                    <% } %>
                    <div class="form-group">
                        <label>开始日期</label>
                        <input type="date" name="date_from" value="<%= dateFrom != null ? dateFrom : "" %>">
                    </div>
                    <div class="form-group">
                        <label>结束日期</label>
                        <input type="date" name="date_to" value="<%= dateTo != null ? dateTo : "" %>">
                    </div>
                    <div class="form-group">
                        <label>&nbsp;</label>
                        <button type="submit" class="btn btn-primary btn-sm">🔍 搜索</button>
                        <a href="<%= ctx %>/logs?type=<%= type %>" class="btn btn-outline btn-sm">清除</a>
                    </div>
                </form>

                <div style="padding: 8px 16px; font-size: 13px; color: #999; border-bottom: 1px solid #e8e8e8; display: flex; justify-content: space-between;">
                    <span>
                        <% if ("password".equals(type)) { %>密码查看审计日志
                        <% } else if ("application".equals(type)) { %>申请记录日志
                        <% } else if ("login".equals(type)) { %>登录记录日志
                        <% } else { %>账号修改日志
                        <% } %>
                    </span>
                    <span style="font-weight:500;">共 <%= total %> 条记录</span>
                </div>

                <!-- Password Logs Table -->
                <% if ("password".equals(type)) { %>
                <table>
                    <thead><tr><th>编号</th><th>操作类型</th><th>账号名称</th><th>所属项目</th><th>平台类型</th><th>运维子类型</th><th>操作人</th><th>操作时间</th></tr></thead>
                    <tbody>
                    <% if (logs != null && !logs.isEmpty()) {
                        for (PasswordLog log : logs) { %>
                        <tr>
                            <td><%= log.getId() %></td>
                            <td>
                                <% if ("export".equals(log.getActionType())) { %>
                                    <span class="badge badge-info"><%= log.getViewerName() %> 导出了所有账号数据</span>
                                <% } else { %>
                                    <span class="badge badge-primary">查看密码</span>
                                <% } %>
                            </td>
                            <td>
                                <% if (log.getAccountId() > 0) { %>
                                    <a href="<%= ctx %>/accounts/detail?id=<%= log.getAccountId() %>"><%= log.getAccountName() != null ? log.getAccountName() : "#" + log.getAccountId() %></a>
                                <% } else { %>
                                    <span style="color:#999;">-</span>
                                <% } %>
                            </td>
                            <td><%= log.getProject() != null ? log.getProject() : "-" %></td>
                            <td><%= log.getPlatformType() != null ? log.getPlatformType() : "-" %></td>
                            <td><%= log.getSubType() != null ? log.getSubType() : "-" %></td>
                            <td><%= log.getViewerName() %></td>
                            <td style="color:#999;font-size:12px;"><%= log.getViewedAt() != null ? log.getViewedAt() : "" %></td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="8"><div class="empty-state"><p>暂无操作记录</p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
                <% } %>

                <!-- Application Logs Table -->
                <% if ("application".equals(type)) { %>
                <table>
                    <thead><tr><th>编号</th><th>账号名称</th><th>连接信息</th><th>所属项目</th><th>平台类型</th><th>运维子类型</th><th>申请人</th><th>申请原因</th><th>使用天数</th><th>状态</th><th>审批人</th><th>审批意见</th><th>申请时间</th></tr></thead>
                    <tbody>
                    <% if (appLogs != null && !appLogs.isEmpty()) {
                        for (Application a : appLogs) { %>
                        <tr>
                            <td><%= a.getId() %></td>
                            <td><a href="<%= ctx %>/accounts/detail?id=<%= a.getAccountId() %>"><%= a.getAccountName() != null ? a.getAccountName() : "#" + a.getAccountId() %></a></td>
                            <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;"><%= a.getLoginUrl() != null && !a.getLoginUrl().isEmpty() ? a.getLoginUrl() : "-" %></td>
                            <td><%= a.getProject() != null ? a.getProject() : "-" %></td>
                            <td><%= a.getPlatformType() != null ? a.getPlatformType() : "-" %></td>
                            <td><%= a.getSubType() != null ? a.getSubType() : "-" %></td>
                            <td><%= a.getApplicantName() != null ? a.getApplicantName() : "用户#" + a.getApplicantId() %></td>
                            <td><%= a.getReason() != null ? a.getReason() : "-" %></td>
                            <td><%= a.getValidDays() != null ? a.getValidDays() + "天" : "7天" %></td>
                            <td>
                                <% if ("pending".equals(a.getStatus())) { %>
                                    <span class="badge badge-warning">待审批</span>
                                <% } else if ("approved".equals(a.getStatus())) { %>
                                    <span class="badge badge-success">已通过</span>
                                <% } else { %>
                                    <span class="badge badge-danger">已驳回</span>
                                <% } %>
                            </td>
                            <td><%= a.getReviewerName() != null ? a.getReviewerName() : "-" %></td>
                            <td><%= a.getReviewComment() != null && !a.getReviewComment().isEmpty() ? a.getReviewComment() : "-" %></td>
                            <td style="color:#999;font-size:12px;"><%= a.getCreatedAt() != null ? a.getCreatedAt().substring(0, 16) : "" %></td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="13"><div class="empty-state"><p>暂无申请记录</p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
                <% } %>

                <!-- Login Logs Table -->
                <% if ("login".equals(type)) { %>
                <table>
                    <thead><tr><th>编号</th><th>用户名</th><th>显示名</th><th>IP地址</th><th>登录时间</th></tr></thead>
                    <tbody>
                    <% if (loginLogs != null && !loginLogs.isEmpty()) {
                        for (LoginLog log : loginLogs) { %>
                        <tr>
                            <td><%= log.getId() %></td>
                            <td><%= log.getUsername() %></td>
                            <td><%= log.getDisplayName() %></td>
                            <td style="font-family:monospace;font-size:12px;"><%= log.getIpAddress() != null ? log.getIpAddress() : "-" %></td>
                            <td style="color:#999;font-size:12px;"><%= log.getLoginTime() != null ? log.getLoginTime() : "" %></td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="5"><div class="empty-state"><p>暂无登录记录</p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
                <% } %>

                <!-- Account Logs Table -->
                <% if ("account".equals(type)) { %>
                <table>
                    <thead><tr><th>编号</th><th>操作类型</th><th>账号名称</th><th>连接信息</th><th>所属项目</th><th>平台类型</th><th>运维子类型</th><th>变更字段</th><th>旧值</th><th>新值</th><th>操作人</th><th>操作时间</th></tr></thead>
                    <tbody>
                    <% if (accountLogs != null && !accountLogs.isEmpty()) {
                        for (AccountLog log : accountLogs) { %>
                        <tr>
                            <td><%= log.getId() %></td>
                            <td>
                                <% if ("create".equals(log.getActionType())) { %>
                                    <span class="badge badge-success">创建</span>
                                <% } else if ("update".equals(log.getActionType())) { %>
                                    <span class="badge badge-primary">修改</span>
                                <% } else if ("delete".equals(log.getActionType())) { %>
                                    <span class="badge badge-danger">删除</span>
                                <% } else { %>
                                    <span class="badge badge-info"><%= log.getActionType() %></span>
                                <% } %>
                            </td>
                            <td>
                                <% if (log.getAccountId() > 0) { %>
                                    <a href="<%= ctx %>/accounts/detail?id=<%= log.getAccountId() %>"><%= log.getAccountName() != null ? log.getAccountName() : "#" + log.getAccountId() %></a>
                                <% } else { %>
                                    <span style="color:#999;">-</span>
                                <% } %>
                            </td>
                            <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;"><%= log.getLoginUrl() != null && !log.getLoginUrl().isEmpty() ? log.getLoginUrl() : "-" %></td>
                            <td><%= log.getProject() != null ? log.getProject() : "-" %></td>
                            <td><%= log.getPlatformType() != null ? log.getPlatformType() : "-" %></td>
                            <td><%= log.getSubType() != null ? log.getSubType() : "-" %></td>
                            <td><%= log.getFieldName() != null ? log.getFieldName() : "-" %></td>
                            <td style="max-width:120px;overflow:hidden;text-overflow:ellipsis;"><%= log.getOldValue() != null ? log.getOldValue() : "-" %></td>
                            <td style="max-width:120px;overflow:hidden;text-overflow:ellipsis;"><%= log.getNewValue() != null ? log.getNewValue() : "-" %></td>
                            <td><%= log.getOperatorName() %></td>
                            <td style="color:#999;font-size:12px;"><%= log.getCreatedAt() != null ? log.getCreatedAt() : "" %></td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="12"><div class="empty-state"><p>暂无修改日志</p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
                <% } %>

                <!-- Pagination -->
                <% if (totalPages > 1) { %>
                <div class="pagination">
                    <% if (currentPage > 1) { %>
                    <a href="<%= ctx %>/logs?type=<%= type %>&page=<%= currentPage - 1 %><%= keyword != null && !keyword.isEmpty() ? "&keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8") : "" %><%= dateFrom != null && !dateFrom.isEmpty() ? "&date_from=" + dateFrom : "" %><%= dateTo != null && !dateTo.isEmpty() ? "&date_to=" + dateTo : "" %><%= statusFilter != null && !statusFilter.isEmpty() && !"all".equals(statusFilter) ? "&status=" + statusFilter : "" %>">上一页</a>
                    <% } %>
                    <% for (int i = 1; i <= totalPages; i++) { %>
                        <% if (i == currentPage) { %>
                        <span class="current"><%= i %></span>
                        <% } else { %>
                        <a href="<%= ctx %>/logs?type=<%= type %>&page=<%= i %><%= keyword != null && !keyword.isEmpty() ? "&keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8") : "" %><%= dateFrom != null && !dateFrom.isEmpty() ? "&date_from=" + dateFrom : "" %><%= dateTo != null && !dateTo.isEmpty() ? "&date_to=" + dateTo : "" %><%= statusFilter != null && !statusFilter.isEmpty() && !"all".equals(statusFilter) ? "&status=" + statusFilter : "" %>"><%= i %></a>
                        <% } %>
                    <% } %>
                    <% if (currentPage < totalPages) { %>
                    <a href="<%= ctx %>/logs?type=<%= type %>&page=<%= currentPage + 1 %><%= keyword != null && !keyword.isEmpty() ? "&keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8") : "" %><%= dateFrom != null && !dateFrom.isEmpty() ? "&date_from=" + dateFrom : "" %><%= dateTo != null && !dateTo.isEmpty() ? "&date_to=" + dateTo : "" %><%= statusFilter != null && !statusFilter.isEmpty() && !"all".equals(statusFilter) ? "&status=" + statusFilter : "" %>">下一页</a>
                    <% } %>
                </div>
                <% } %>
            </div>
        </div>
    </div>
</div>
</body>
</html>
