<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.account.model.User" %>
<%
    User user = (User) session.getAttribute("user");
    List<User> users = (List<User>) request.getAttribute("users");
    String error = request.getParameter("error");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户管理 - 企业外部账号管理</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="admin-layout">
    <div class="sidebar">
        <div class="sidebar-header"><h2>📋 账号管理</h2><p>Enterprise Account Manager</p></div>
        <nav class="sidebar-nav">
            <a href="${pageContext.request.contextPath}/dashboard"><span class="nav-icon">📊</span><span>仪表盘</span></a>
            <a href="${pageContext.request.contextPath}/accounts"><span class="nav-icon">📁</span><span>账号管理</span></a>
            <a href="${pageContext.request.contextPath}/users" class="active"><span class="nav-icon">👤</span><span>用户管理</span></a>
            <a href="${pageContext.request.contextPath}/groups"><span class="nav-icon">👥</span><span>用户组管理</span></a>
            <a href="${pageContext.request.contextPath}/platforms"><span class="nav-icon">🖥️</span><span>平台管理</span></a>
            <a href="${pageContext.request.contextPath}/applications/pending"><span class="nav-icon">📝</span><span>审批管理</span></a>
            <a href="${pageContext.request.contextPath}/logs"><span class="nav-icon">📋</span><span>操作日志</span></a>
        </nav>
        <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/logout">🚪 <span>退出登录</span></a></div>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">用户管理</div>
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
                <% if ("self".equals(error)) { %>
                <div style="padding: 12px 16px; background: #fff2f0; border: 1px solid #ffccc7; color: #ff4d4f; border-radius: 4px; margin-bottom: 12px;">
                    ⚠️ 不能删除当前登录的用户
                </div>
                <% } %>
                <% if ("admin_delete".equals(error)) { %>
                <div style="padding: 12px 16px; background: #fff2f0; border: 1px solid #ffccc7; color: #ff4d4f; border-radius: 4px; margin-bottom: 12px;">
                    ⚠️ 不能删除内置超级管理员账号
                </div>
                <% } %>
                <% if ("admin_role".equals(error)) { %>
                <div style="padding: 12px 16px; background: #fff2f0; border: 1px solid #ffccc7; color: #ff4d4f; border-radius: 4px; margin-bottom: 12px;">
                    ⚠️ 不能修改内置超级管理员账号的角色
                </div>
                <% } %>
                <div class="table-toolbar">
                    <div></div>
                    <a href="<%= ctx %>/users/new" class="btn btn-success">➕ 新增用户</a>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>编号</th>
                            <th>用户名</th>
                            <th>显示名</th>
                            <th>角色</th>
                            <th>部门</th>
                            <th>创建时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (users != null && !users.isEmpty()) {
                        for (User u : users) { %>
                        <tr>
                            <td><%= u.getId() %></td>
                            <td><%= u.getUsername() %></td>
                            <td><%= u.getDisplayName() %></td>
                            <td>
                                <% if ("admin".equals(u.getRole())) { %>
                                    <span class="badge badge-primary">超级管理员</span>
                                <% } else if ("manager".equals(u.getRole())) { %>
                                    <span class="badge badge-info">管理员</span>
                                <% } else { %>
                                    <span class="badge badge-outline">普通用户</span>
                                <% } %>
                            </td>
                            <td><%= u.getDepartment() != null ? u.getDepartment() : "" %></td>
                            <td style="color:#999;font-size:12px;"><%= u.getCreatedAt() != null ? u.getCreatedAt() : "" %></td>
                            <td>
                                <a href="<%= ctx %>/users/edit?id=<%= u.getId() %>" class="btn btn-sm btn-primary">编辑</a>
                                <% if (user.getId() != u.getId()) { %>
                                <form method="post" action="<%= ctx %>/users/delete" style="display:inline;" onsubmit="return confirm('确认删除用户 <%= u.getDisplayName() %>？')">
                                    <input type="hidden" name="id" value="<%= u.getId() %>">
                                    <button type="submit" class="btn btn-sm btn-danger">删除</button>
                                </form>
                                <% } %>
                            </td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="7"><div class="empty-state"><p>暂无用户数据</p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>
