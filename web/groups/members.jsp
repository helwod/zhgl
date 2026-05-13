<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.account.model.User, com.account.model.UserGroup, com.account.model.UserGroupMember" %>
<%
    User user = (User) session.getAttribute("user");
    UserGroup group = (UserGroup) request.getAttribute("group");
    List<UserGroupMember> members = (List<UserGroupMember>) request.getAttribute("members");
    List<User> availableUsers = (List<User>) request.getAttribute("availableUsers");
    if (members == null) members = java.util.Collections.emptyList();
    if (availableUsers == null) availableUsers = java.util.Collections.emptyList();
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>组成员管理 - <%= group.getName() %> - 企业外部账号管理</title>
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
            <div class="topbar-title"><%= group.getName() %> - 组成员管理</div>
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
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                <div>
                    <a href="<%= ctx %>/groups" class="btn btn-outline btn-sm">← 返回用户组</a>
                </div>
            </div>

            <!-- Add users section (multi-select) -->
            <div class="form-card" style="max-width: 100%; margin-bottom: 20px;">
                <h4 style="margin: 0 0 12px 0; font-size: 15px;">➕ 添加成员（多选）</h4>
                <% if (availableUsers.isEmpty()) { %>
                <div class="empty-state" style="padding: 8px;"><p style="font-size: 13px;">所有用户均已添加到此组</p></div>
                <% } else { %>
                <div style="margin-bottom: 12px;">
                    <label style="font-size: 13px; cursor: pointer;">
                        <input type="checkbox" id="selectAll" onchange="toggleSelectAll()"> 全选
                    </label>
                </div>
                <div id="userCheckboxList" style="display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 16px; max-height: 260px; overflow-y: auto; padding: 8px; border: 1px solid #e8e8e8; border-radius: 6px;">
                    <% for (User u : availableUsers) { %>
                    <label style="display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 13px; min-width: 180px; padding: 4px 8px; border-radius: 4px; background: #fafafa;">
                        <input type="checkbox" class="user-checkbox" value="<%= u.getId() %>">
                        <%= u.getDisplayName() %> (<%= u.getUsername() %>)
                    </label>
                    <% } %>
                </div>
                <button onclick="addSelectedMembers()" class="btn btn-primary">➕ 添加选中成员</button>
                <% } %>
            </div>

            <!-- Current members -->
            <div class="table-container">
                <div style="padding: 12px 16px; border-bottom: 1px solid #e8e8e8; font-weight: 600; font-size: 14px;">
                    成员列表（共 <%= members.size() %> 人）
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>编号</th>
                            <th>用户名</th>
                            <th>显示名</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (!members.isEmpty()) {
                        for (UserGroupMember m : members) { %>
                        <tr>
                            <td><%= m.getId() %></td>
                            <td><%= m.getUserName() != null ? m.getUserName() : "用户#" + m.getUserId() %></td>
                            <td><%= m.getUserDisplayName() != null ? m.getUserDisplayName() : "-" %></td>
                            <td>
                                <button onclick="removeMember(<%= m.getGroupId() %>, <%= m.getUserId() %>)" class="btn btn-sm btn-danger">移除</button>
                            </td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="4"><div class="empty-state"><p>暂无成员</p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div id="msg" style="display:none;"></div>

<script>
function toggleSelectAll() {
    var checked = document.getElementById('selectAll').checked;
    var checkboxes = document.querySelectorAll('.user-checkbox');
    for (var i = 0; i < checkboxes.length; i++) {
        checkboxes[i].checked = checked;
    }
}

function addSelectedMembers() {
    var checkboxes = document.querySelectorAll('.user-checkbox:checked');
    if (checkboxes.length === 0) {
        alert('请至少选择一个用户');
        return;
    }

    var userIds = [];
    for (var i = 0; i < checkboxes.length; i++) {
        userIds.push(checkboxes[i].value);
    }

    fetch('<%= ctx %>/groups/members', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=addBatch&group_id=<%= group.getId() %>&user_ids=' + userIds.join(',')
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

function removeMember(groupId, userId) {
    if (!confirm('确认移除该成员？')) return;

    fetch('<%= ctx %>/groups/members', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=remove&group_id=' + groupId + '&user_id=' + userId
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
