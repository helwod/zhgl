<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Set, com.account.model.User, com.account.model.Account" %>
<%
    User user = (User) session.getAttribute("user");
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    String[] platformTypes = (String[]) request.getAttribute("platformTypes");
    String selPlatform = (String) request.getAttribute("platformType");
    String selStatus = (String) request.getAttribute("status");
    String keyword = (String) request.getAttribute("keyword");
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    Integer total = (Integer) request.getAttribute("total");
    Set<Integer> pendingAccountIds = (Set<Integer>) request.getAttribute("pendingAccountIds");
    if (currentPage == null) currentPage = 1;
    if (totalPages == null) totalPages = 1;
    if (total == null) total = 0;
    if (pendingAccountIds == null) pendingAccountIds = new java.util.HashSet<Integer>();
    String ctx = request.getContextPath();

    String importMsg = (String) session.getAttribute("importMsg");
    if (importMsg != null) { session.removeAttribute("importMsg"); }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>账号管理 - 企业外部账号管理</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
    .import-form { display: inline-flex; gap: 8px; align-items: center; }
    .import-form input[type=file] { font-size: 12px; }
    #importMsg { padding: 10px 16px; margin-bottom: 12px; border-radius: 6px; font-size: 13px; }
    #importMsg.success { background: #f6ffed; border: 1px solid #b7eb8f; color: #389e0d; }
    #importMsg.error { background: #fff2f0; border: 1px solid #ffccc7; color: #cf1322; }
    </style>
</head>
<body>
<div class="admin-layout">
    <div class="sidebar">
        <div class="sidebar-header"><h2>📋 账号管理</h2><p>Enterprise Account Manager</p></div>
        <nav class="sidebar-nav">
            <a href="${pageContext.request.contextPath}/dashboard"><span class="nav-icon">📊</span><span>仪表盘</span></a>
            <a href="${pageContext.request.contextPath}/accounts" class="active"><span class="nav-icon">📁</span><span>账号管理</span></a>
            <% if (user.isAdmin()) { %>
            <a href="${pageContext.request.contextPath}/users"><span class="nav-icon">👤</span><span>用户管理</span></a>
            <a href="${pageContext.request.contextPath}/groups"><span class="nav-icon">👥</span><span>用户组管理</span></a>
            <a href="${pageContext.request.contextPath}/platforms"><span class="nav-icon">🖥️</span><span>平台管理</span></a>
            <a href="${pageContext.request.contextPath}/applications/pending"><span class="nav-icon">📝</span><span>审批管理</span></a>
            <a href="${pageContext.request.contextPath}/logs"><span class="nav-icon">📋</span><span>操作日志</span></a>
            <% } else if (user.isManager()) { %>
            <a href="${pageContext.request.contextPath}/applications/pending"><span class="nav-icon">📝</span><span>审批管理</span></a>
            <a href="${pageContext.request.contextPath}/logs"><span class="nav-icon">📋</span><span>操作日志</span></a>
            <% } else { %>
            <a href="${pageContext.request.contextPath}/applications/my"><span class="nav-icon">📝</span><span>我的申请</span></a>
            <% } %>
        </nav>
        <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/logout">🚪 <span>退出登录</span></a></div>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">账号管理</div>
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
            <% if (importMsg != null && !importMsg.isEmpty()) { %>
            <div id="importMsg" class="<%= importMsg.contains("失败") ? "error" : "success" %>"><%= importMsg %></div>
            <% } %>

            <div class="table-container">
                <div class="table-toolbar">
                    <form class="table-filters" method="get" action="<%= ctx %>/accounts">
                        <select name="platform_type">
                            <option value="">全部平台</option>
                            <% if (platformTypes != null) for (String pt : platformTypes) { %>
                            <option value="<%= pt %>" <%= pt.equals(selPlatform) ? "selected" : "" %>><%= pt %></option>
                            <% } %>
                        </select>
                        <select name="status">
                            <option value="">全部状态</option>
                            <option value="可用" <%= "可用".equals(selStatus) ? "selected" : "" %>>可用</option>
                            <option value="已分配" <%= "已分配".equals(selStatus) ? "selected" : "" %>>已分配</option>
                            <option value="已过期" <%= "已过期".equals(selStatus) ? "selected" : "" %>>已过期</option>
                        </select>
                        <input type="text" name="keyword" placeholder="搜索名称/项目/部门/备注" value="<%= keyword != null ? keyword : "" %>">
                        <button type="submit" class="btn btn-primary btn-sm">🔍 搜索</button>
                        <% if ((selPlatform != null && !selPlatform.isEmpty()) || (selStatus != null && !selStatus.isEmpty()) || (keyword != null && !keyword.isEmpty())) { %>
                        <a href="<%= ctx %>/accounts" class="btn btn-outline btn-sm">清除</a>
                        <% } %>
                    </form>
                    <div style="display:flex;gap:8px;align-items:center;">
                        <% if (user.isAdmin() || user.isManager()) { %>
                        <a href="<%= ctx %>/accounts/new" class="btn btn-success">➕ 新增</a>
                        <% } %>
                        <% if (user.isAdmin()) { %>
                        <a href="<%= ctx %>/accounts/export" class="btn btn-outline">📥 导出</a>
                        <a href="<%= ctx %>/accounts/export?template=true" class="btn btn-outline">📋 模板</a>
                        <form class="import-form" method="post" action="<%= ctx %>/accounts/import" enctype="multipart/form-data">
                            <input type="file" name="csvFile" accept=".csv" required>
                            <button type="submit" class="btn btn-outline">📤 导入</button>
                        </form>
                        <% } %>
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>编号</th>
                            <th>所属项目</th>
                            <th>账号名称</th>
                            <th>平台类型</th>
                            <th>运维子类型</th>
                            <th>部门</th>
                            <th>连接信息</th>
                            <th>到期时间</th>
                            <th>状态</th>
                            <th>创建时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (accounts != null && !accounts.isEmpty()) {
                        for (Account a : accounts) { %>
                        <tr>
                            <td><%= a.getId() %></td>
                            <td><%= a.getProject() != null && !a.getProject().isEmpty() ? a.getProject() : "-" %></td>
                            <td><a href="<%= ctx %>/accounts/detail?id=<%= a.getId() %>"><%= a.getName() %></a></td>
                            <td><span class="badge badge-primary"><%= a.getPlatformType() %></span></td>
                            <td><%= a.getSubType() != null && !a.getSubType().isEmpty() ? a.getSubType() : "-" %></td>
                            <td><%= a.getDepartment() != null ? a.getDepartment() : "" %></td>
                            <td style="font-size:12px;">
                                <% if (a.getLoginUrl() != null && !a.getLoginUrl().isEmpty()) { %>
                                    <%= a.getLoginUrl() %>
                                <% } else { %>
                                    <span style="color:#999;">-</span>
                                <% } %>
                            </td>
                            <td><%= a.getExpiryDate() != null ? a.getExpiryDate() : "-" %></td>
                            <td>
                                <% if ("可用".equals(a.getStatus())) { %>
                                    <span class="badge badge-success">可用</span>
                                <% } else if ("已分配".equals(a.getStatus())) { %>
                                    <span class="badge badge-warning">已分配</span>
                                <% } else { %>
                                    <span class="badge badge-danger">已过期</span>
                                <% } %>
                            </td>
                            <td style="color:#999;font-size:12px;"><%= a.getCreatedAt() != null ? a.getCreatedAt().substring(0, 10) : "" %></td>
                            <td>
                                <a href="<%= ctx %>/accounts/detail?id=<%= a.getId() %>" class="btn btn-sm btn-outline">详情</a>
                                <% if (user.isAdmin()) { %>
                                <a href="<%= ctx %>/accounts/edit?id=<%= a.getId() %>" class="btn btn-sm btn-primary">编辑</a>
                                <a href="<%= ctx %>/accounts/assign?account_id=<%= a.getId() %>" class="btn btn-sm btn-info">分配</a>
                                <form method="post" action="<%= ctx %>/accounts/delete" style="display:inline;" onsubmit="return confirm('确认删除该账号？')">
                                    <input type="hidden" name="id" value="<%= a.getId() %>">
                                    <button type="submit" class="btn btn-sm btn-danger">删除</button>
                                </form>
                                <% } else if (user.isManager()) { %>
                                <a href="<%= ctx %>/accounts/edit?id=<%= a.getId() %>" class="btn btn-sm btn-primary">编辑</a>
                                <a href="<%= ctx %>/accounts/assign?account_id=<%= a.getId() %>" class="btn btn-sm btn-info">分配</a>
                                <form method="post" action="<%= ctx %>/accounts/delete" style="display:inline;" onsubmit="return confirm('确认删除该账号？')">
                                    <input type="hidden" name="id" value="<%= a.getId() %>">
                                    <button type="submit" class="btn btn-sm btn-danger">删除</button>
                                </form>
                                <% } else { %>
                                    <% if (pendingAccountIds.contains(a.getId())) { %>
                                        <span class="badge badge-warning" style="font-size:12px;">⏳ 申请中</span>
                                    <% } else { %>
                                        <a href="<%= ctx %>/accounts/detail?id=<%= a.getId() %>" class="btn btn-sm btn-primary">申请</a>
                                    <% } %>
                                <% } %>
                            </td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="11"><div class="empty-state"><p>暂无账号数据</p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>

                <% if (totalPages > 1) { %>
                <div class="pagination">
                    <% if (currentPage > 1) { %>
                    <a href="<%= ctx %>/accounts?page=<%= currentPage - 1 %><%= selPlatform != null && !selPlatform.isEmpty() ? "&platform_type=" + selPlatform : "" %><%= selStatus != null && !selStatus.isEmpty() ? "&status=" + selStatus : "" %><%= keyword != null && !keyword.isEmpty() ? "&keyword=" + keyword : "" %>">上一页</a>
                    <% } %>
                    <% for (int i = 1; i <= totalPages; i++) { %>
                        <% if (i == currentPage) { %>
                        <span class="current"><%= i %></span>
                        <% } else { %>
                        <a href="<%= ctx %>/accounts?page=<%= i %><%= selPlatform != null && !selPlatform.isEmpty() ? "&platform_type=" + selPlatform : "" %><%= selStatus != null && !selStatus.isEmpty() ? "&status=" + selStatus : "" %><%= keyword != null && !keyword.isEmpty() ? "&keyword=" + keyword : "" %>"><%= i %></a>
                        <% } %>
                    <% } %>
                    <% if (currentPage < totalPages) { %>
                    <a href="<%= ctx %>/accounts?page=<%= currentPage + 1 %><%= selPlatform != null && !selPlatform.isEmpty() ? "&platform_type=" + selPlatform : "" %><%= selStatus != null && !selStatus.isEmpty() ? "&status=" + selStatus : "" %><%= keyword != null && !keyword.isEmpty() ? "&keyword=" + keyword : "" %>">下一页</a>
                    <% } %>
                </div>
                <% } %>
                <div style="padding: 8px 16px; font-size: 12px; color: #999; border-top: 1px solid #e8e8e8;">
                    共 <%= total %> 条记录
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
