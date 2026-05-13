<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.account.model.User, com.account.model.Platform" %>
<%
    User user = (User) session.getAttribute("user");
    List<Platform> platforms = (List<Platform>) request.getAttribute("platforms");
    String error = request.getParameter("error");
    String count = request.getParameter("count");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>平台管理 - 企业外部账号管理</title>
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
            <a href="${pageContext.request.contextPath}/groups"><span class="nav-icon">👥</span><span>用户组管理</span></a>
            <a href="${pageContext.request.contextPath}/platforms" class="active"><span class="nav-icon">🖥️</span><span>平台管理</span></a>
            <a href="${pageContext.request.contextPath}/applications/pending"><span class="nav-icon">📝</span><span>审批管理</span></a>
            <a href="${pageContext.request.contextPath}/logs"><span class="nav-icon">📋</span><span>操作日志</span></a>
        </nav>
        <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/logout">🚪 <span>退出登录</span></a></div>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title">平台管理</div>
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
                <% if ("linked".equals(error)) { %>
                <div style="padding: 12px 16px; background: #fff2f0; border: 1px solid #ffccc7; color: #ff4d4f; border-radius: 4px; margin-bottom: 12px;">
                    ⚠️ 该平台下还有 <%= count %> 个关联账号，无法删除
                </div>
                <% } %>

                <div class="table-toolbar">
                    <div></div>
                    <form method="post" action="<%= ctx %>/platforms/create" class="table-filters" onsubmit="return validateForm()">
                        <input type="text" name="name" id="platformName" placeholder="输入平台名称" required>
                        <button type="submit" class="btn btn-success">➕ 新增平台</button>
                    </form>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>编号</th>
                            <th>平台名称</th>
                            <th>创建时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (platforms != null && !platforms.isEmpty()) {
                        for (Platform p : platforms) { %>
                        <tr>
                            <td><%= p.getId() %></td>
                            <td><span class="badge badge-primary"><%= p.getName() %></span></td>
                            <td style="color:#999;font-size:12px;"><%= p.getCreatedAt() != null ? p.getCreatedAt() : "" %></td>
                            <td>
                                <form method="post" action="<%= ctx %>/platforms/delete" style="display:inline;" onsubmit="return confirm('确认删除平台 <%= p.getName() %>？')">
                                    <input type="hidden" name="id" value="<%= p.getId() %>">
                                    <input type="hidden" name="name" value="<%= p.getName() %>">
                                    <button type="submit" class="btn btn-sm btn-danger">删除</button>
                                </form>
                            </td>
                        </tr>
                    <% }
                    } else { %>
                        <tr><td colspan="4"><div class="empty-state"><p>暂无平台数据</p></div></td></tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
function validateForm() {
    var name = document.getElementById('platformName').value.trim();
    if (!name) { alert('请输入平台名称'); return false; }
    return true;
}
</script>
</body>
</html>
