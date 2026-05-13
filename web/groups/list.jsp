<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.account.model.User, com.account.model.UserGroup" %>
<%
    User user = (User) session.getAttribute("user");
    List<UserGroup> groups = (List<UserGroup>) request.getAttribute("groups");
    if (groups == null) groups = java.util.Collections.emptyList();
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户组管理 - 企业外部账号管理</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="admin-layout">
    <div class="sidebar">
        <div class="sidebar-header"><h2>📋 账号管理</h2><p>Enterprise Account Manager</p></div>
        <nav class="sidebar-nav">
            <a href="${pageContext.request.contextPath}/dashboard"><span class="nav-icon">📊</span><span>仪表盘</span></a>
            <a href="${pageContext.request.contextPath}/accounts"><span class="nav-icon">📁</span><span>账号管理</span></a>
            <a href="${pageContext.request.contextPath}/users"><span class="nav-icon">👤</span><span>用户管理</span></a>
            <a href="${pageContext.request.contextPath}/groups" class="active"><span class="nav-icon">👥</span><span>用户组管理</span></a>
            <a href="${pageContext.request.contextPath}/platforms"><span class="nav-icon">🖥️</span><span>平台管理</span></a>
            <a href="${pageContext.request.contextPath}/applications/pending"><span class="nav-icon">📝</span><span>审批管理</span></a>
            <a href="${pageContext.request.contextPath}/logs"><span class="nav-icon">📋</span><span>操作日志</span></a>
        </nav>
        <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/logout">🚪 <span>退出登录</span></a></div>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">用户组管理</div>
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
            <div class="form-card" style="max-width: 700px;">
                <h3 style="font-size: 18px; font-weight: 600; margin-bottom: 16px;">创建用户组</h3>
                <div style="display: flex; gap: 8px; align-items: flex-end; flex-wrap: wrap;">
                    <div class="form-group" style="flex:2;min-width:150px;margin-bottom:0;">
                        <label>组名称 *</label>
                        <input type="text" id="groupName" placeholder="如：运维组、开发组">
                    </div>
                    <div class="form-group" style="flex:2;min-width:150px;margin-bottom:0;">
                        <label>描述</label>
                        <input type="text" id="groupDesc" placeholder="描述信息（可选）">
                    </div>
                    <button onclick="createGroup()" class="btn btn-success">➕ 创建</button>
                </div>
            </div>

            <div class="table-container" style="margin-top: 20px;">
                <div style="padding: 12px 16px; border-bottom: 1px solid #e8e8e8; font-weight: 600; font-size: 14px;">
                    用户组列表
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>编号</th>
                            <th>组名称</th>
                            <th>描述</th>
                            <th>成员数</th>
                            <th>创建时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (!groups.isEmpty()) {
                        for (UserGroup g : groups) { %>
                        <tr>
                            <td><%= g.getId() %></td>
                            <td><strong><%= g.getName() %></strong></td>
                            <td><%= g.getDescription() != null && !g.getDescription().isEmpty() ? g.getDescription() : "-" %></td>
                            <td><span class="badge badge-info"><%= g.getMemberCount() %></span></td>
                            <td style="color:#999;font-size:12px;"><%= g.getCreatedAt() != null ? g.getCreatedAt().substring(0, 16) : "" %></td>
                            <td>
                                <a href="<%= ctx %>/groups/members?group_id=<%= g.getId() %>" class="btn btn-sm btn-outline">管理成员</a>
                                <button onclick="deleteGroup(<%= g.getId() %>)" class="btn btn-sm btn-danger">删除</button>
                            </td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="6"><div class="empty-state"><p>暂无用户组</p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
function createGroup() {
    var name = document.getElementById('groupName').value.trim();
    if (!name) { alert('请输入用户组名称'); return; }
    var desc = document.getElementById('groupDesc').value.trim();

    fetch('<%= ctx %>/groups', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=create&name=' + encodeURIComponent(name) + '&description=' + encodeURIComponent(desc)
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        if (data.success) {
            location.reload();
        } else {
            alert(data.message);
        }
    })
    .catch(function() { alert('请求失败'); });
}

function deleteGroup(id) {
    if (!confirm('确认删除该用户组？')) return;
    fetch('<%= ctx %>/groups', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=delete&id=' + id
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        if (data.success) {
            location.reload();
        } else {
            alert(data.message);
        }
    })
    .catch(function() { alert('请求失败'); });
}
</script>
</body>
</html>
