<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.account.model.User, com.account.model.UserGroup, java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    User userEdit = (User) request.getAttribute("userEdit");
    boolean isEdit = (userEdit != null);
    String ctx = request.getContextPath();
    List<UserGroup> allGroups = (List<UserGroup>) request.getAttribute("allGroups");
    List<Integer> userGroupIds = (List<Integer>) request.getAttribute("userGroupIds");
    if (allGroups == null) allGroups = java.util.Collections.emptyList();
    if (userGroupIds == null) userGroupIds = java.util.Collections.emptyList();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isEdit ? "编辑用户" : "新增用户" %> - 企业外部账号管理</title>
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
            <div class="topbar-title"><%= isEdit ? "编辑用户" : "新增用户" %></div>
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
            <div class="form-card">
                <form method="post" action="<%= ctx %><%= isEdit ? "/users/update" : "/users/create" %>" onsubmit="return validateForm()">
                    <% if (isEdit) { %>
                    <input type="hidden" name="id" value="<%= userEdit.getId() %>">
                    <% } %>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="username">用户名 *</label>
                            <input type="text" id="username" name="username" required value="<%= isEdit ? userEdit.getUsername() : "" %>" placeholder="请输入用户名">
                        </div>
                        <div class="form-group">
                            <label for="display_name">显示名 *</label>
                            <input type="text" id="display_name" name="display_name" required value="<%= isEdit ? userEdit.getDisplayName() : "" %>" placeholder="请输入显示名">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="password"><%= isEdit ? "密码（留空不修改）" : "密码 *" %></label>
                            <input type="password" id="password" name="password" <%= isEdit ? "" : "required" %> placeholder="<%= isEdit ? "留空则不修改密码" : "请输入密码" %>">
                        </div>
                        <div class="form-group">
                            <label for="role">角色</label>
                            <select id="role" name="role" <%= isEdit && "admin".equals(userEdit.getUsername()) ? "disabled" : "" %>>
                                <option value="user" <%= isEdit && "user".equals(userEdit.getRole()) ? "selected" : "" %>>普通用户</option>
                                <option value="manager" <%= isEdit && "manager".equals(userEdit.getRole()) ? "selected" : "" %>>管理员</option>
                                <option value="admin" <%= isEdit && "admin".equals(userEdit.getRole()) ? "selected" : "" %>>超级管理员</option>
                            </select>
                            <% if (isEdit && "admin".equals(userEdit.getUsername())) { %>
                            <input type="hidden" name="role" value="admin">
                            <small style="color:#999;display:block;margin-top:4px;">内置超级管理员账号不可修改角色</small>
                            <% } %>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="department">部门</label>
                        <input type="text" id="department" name="department" value="<%= isEdit && userEdit.getDepartment() != null ? userEdit.getDepartment() : "" %>" placeholder="如：IT部、财务部">
                    </div>

                    <% if (!allGroups.isEmpty()) { %>
                    <div class="form-group">
                        <label>所属用户组</label>
                        <div style="display: flex; flex-wrap: wrap; gap: 8px; padding: 8px 0;">
                        <% for (UserGroup g : allGroups) {
                            boolean checked = isEdit && userGroupIds.contains(g.getId());
                        %>
                            <label style="display: flex; align-items: center; gap: 4px; cursor: pointer; padding: 4px 10px; border: 1px solid #d9d9d9; border-radius: 4px; background: <%= checked ? "#e6f7ff" : "#fff" %>;">
                                <input type="checkbox" name="group_ids" value="<%= g.getId() %>" <%= checked ? "checked" : "" %>>
                                <%= g.getName() %>
                            </label>
                        <% } %>
                        </div>
                    </div>
                    <% } %>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary btn-lg"><%= isEdit ? "保存修改" : "创建用户" %></button>
                        <a href="<%= ctx %>/users" class="btn btn-outline btn-lg">取消</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
function validateForm() {
    var username = document.getElementById('username').value.trim();
    var displayName = document.getElementById('display_name').value.trim();
    var password = document.getElementById('password').value;
    if (!username) { alert('请输入用户名'); return false; }
    if (!displayName) { alert('请输入显示名'); return false; }
    <% if (!isEdit) { %>
    if (!password) { alert('请输入密码'); return false; }
    <% } %>
    return true;
}
</script>
</body>
</html>
