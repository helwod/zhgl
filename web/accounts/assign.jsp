<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.account.model.User, com.account.model.UserGroup, com.account.model.AccountManager, com.account.model.AccountGroupManager" %>
<%
    User user = (User) session.getAttribute("user");
    Integer accountId = (Integer) request.getAttribute("accountId");
    List<AccountManager> managers = (List<AccountManager>) request.getAttribute("managers");
    List<AccountGroupManager> groupManagers = (List<AccountGroupManager>) request.getAttribute("groupManagers");
    List<User> allUsers = (List<User>) request.getAttribute("allUsers");
    List<UserGroup> allGroups = (List<UserGroup>) request.getAttribute("allGroups");
    if (managers == null) managers = java.util.Collections.emptyList();
    if (groupManagers == null) groupManagers = java.util.Collections.emptyList();
    if (allUsers == null) allUsers = java.util.Collections.emptyList();
    if (allGroups == null) allGroups = java.util.Collections.emptyList();
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>分配管理 - 企业外部账号管理</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
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
            <% } %>
            <% if (user.isAdmin() || user.isManager()) { %>
            <a href="${pageContext.request.contextPath}/applications/pending"><span class="nav-icon">📝</span><span>审批管理</span></a>
            <% } %>
            <a href="${pageContext.request.contextPath}/logs"><span class="nav-icon">📋</span><span>操作日志</span></a>
        </nav>
        <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/logout">🚪 <span>退出登录</span></a></div>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">账号管理分配</div>
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
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <h3 style="font-size: 18px; font-weight: 600;">👥 分配管理（账号 #<%= accountId %>）</h3>
                    <a href="<%= ctx %>/accounts/detail?id=<%= accountId %>" class="btn btn-outline btn-sm">返回详情</a>
                </div>

                <!-- Current user-level managers -->
                <h4 style="margin-bottom: 8px; font-size: 14px;">个人管理人</h4>
                <% if (managers.isEmpty()) { %>
                <div class="empty-state" style="padding:8px;"><p style="font-size:13px;">暂无个人管理人</p></div>
                <% } else { %>
                <div style="display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px;">
                <% for (AccountManager am : managers) { %>
                    <div style="display: flex; align-items: center; gap: 6px; background: #f0f5ff; padding: 4px 10px; border-radius: 20px; border: 1px solid #d6e4ff;">
                        <span><%= am.getUserDisplayName() != null ? am.getUserDisplayName() : "用户#" + am.getUserId() %></span>
                        <button onclick="removeManager(<%= accountId %>, <%= am.getUserId() %>)" style="border:none;background:none;color:#ff4d4f;cursor:pointer;font-size:16px;line-height:1;">&times;</button>
                    </div>
                <% } %>
                </div>
                <% } %>

                <div style="display: flex; gap: 8px; align-items: center; margin-bottom: 20px;">
                    <select id="userSelect" style="flex:1; padding: 8px 12px; border: 1px solid #d9d9d9; border-radius: 6px;">
                        <option value="">-- 选择用户 --</option>
                        <% for (User u : allUsers) { %><%
                            String roleLabel = "admin".equals(u.getRole()) ? "超级管理员" : "manager".equals(u.getRole()) ? "管理员" : "普通用户";
                        %>
                        <option value="<%= u.getId() %>"><%= u.getDisplayName() %> (<%= u.getUsername() %>) - <%= roleLabel %></option>
                        <% } %>
                    </select>
                    <button onclick="addManager()" class="btn btn-primary">添加</button>
                </div>

                <hr style="border:none;border-top:1px solid #e8e8e8;margin:16px 0;">

                <!-- Current group-level managers -->
                <h4 style="margin-bottom: 8px; font-size: 14px;">用户组管理人</h4>
                <% if (groupManagers.isEmpty()) { %>
                <div class="empty-state" style="padding:8px;"><p style="font-size:13px;">暂无用户组管理人</p></div>
                <% } else { %>
                <div style="display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px;">
                <% for (AccountGroupManager gm : groupManagers) { %>
                    <div style="display: flex; align-items: center; gap: 6px; background: #fff7e6; padding: 4px 10px; border-radius: 20px; border: 1px solid #ffd591;">
                        <span><strong><%= gm.getGroupName() != null ? gm.getGroupName() : "组#" + gm.getGroupId() %></strong>
                        <span style="font-weight:normal;font-size:11px;color:#999;">(<%= gm.getMemberCount() %>人)</span></span>
                        <button onclick="removeGroupManager(<%= accountId %>, <%= gm.getGroupId() %>)" style="border:none;background:none;color:#ff4d4f;cursor:pointer;font-size:16px;line-height:1;">&times;</button>
                    </div>
                <% } %>
                </div>
                <% } %>

                <div style="display: flex; gap: 8px; align-items: center;">
                    <select id="groupSelect" style="flex:1; padding: 8px 12px; border: 1px solid #d9d9d9; border-radius: 6px;">
                        <option value="">-- 选择用户组 --</option>
                        <% for (UserGroup g : allGroups) { %>
                        <option value="<%= g.getId() %>"><%= g.getName() %> (组内<%= g.getMemberCount() %>人)</option>
                        <% } %>
                    </select>
                    <button onclick="addGroupManager()" class="btn btn-info">添加组分配</button>
                </div>

                <div id="msg" style="margin-top: 12px; display: none;"></div>
            </div>
        </div>
    </div>
</div>

<script>
function addManager() {
    var userId = document.getElementById('userSelect').value;
    if (!userId) { alert('请选择用户'); return; }
    var msg = document.getElementById('msg');
    msg.style.display = 'none';

    fetch('<%= ctx %>/accounts/assign', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=add&account_id=<%= accountId %>&user_id=' + userId
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        if (data.success) {
            location.reload();
        } else {
            msg.style.display = 'block';
            msg.className = 'error';
            msg.textContent = data.message;
        }
    })
    .catch(function(err) {
        msg.style.display = 'block';
        msg.className = 'error';
        msg.textContent = '请求失败';
    });
}

function removeManager(accountId, userId) {
    if (!confirm('确认移除该管理人？')) return;
    var msg = document.getElementById('msg');
    msg.style.display = 'none';

    fetch('<%= ctx %>/accounts/assign', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=remove&account_id=' + accountId + '&user_id=' + userId
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        if (data.success) {
            location.reload();
        } else {
            msg.style.display = 'block';
            msg.className = 'error';
            msg.textContent = data.message;
        }
    })
    .catch(function(err) {
        msg.style.display = 'block';
        msg.className = 'error';
        msg.textContent = '请求失败';
    });
}

function addGroupManager() {
    var groupId = document.getElementById('groupSelect').value;
    if (!groupId) { alert('请选择用户组'); return; }
    var msg = document.getElementById('msg');
    msg.style.display = 'none';

    fetch('<%= ctx %>/accounts/assign', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=addGroup&account_id=<%= accountId %>&group_id=' + groupId
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        if (data.success) {
            location.reload();
        } else {
            msg.style.display = 'block';
            msg.className = 'error';
            msg.textContent = data.message;
        }
    })
    .catch(function(err) {
        msg.style.display = 'block';
        msg.className = 'error';
        msg.textContent = '请求失败';
    });
}

function removeGroupManager(accountId, groupId) {
    if (!confirm('确认移除该用户组分配？')) return;
    var msg = document.getElementById('msg');
    msg.style.display = 'none';

    fetch('<%= ctx %>/accounts/assign', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=removeGroup&account_id=' + accountId + '&group_id=' + groupId
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        if (data.success) {
            location.reload();
        } else {
            msg.style.display = 'block';
            msg.className = 'error';
            msg.textContent = data.message;
        }
    })
    .catch(function(err) {
        msg.style.display = 'block';
        msg.className = 'error';
        msg.textContent = '请求失败';
    });
}
</script>
</body>
</html>
