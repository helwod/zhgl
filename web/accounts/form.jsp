<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.account.model.User, com.account.model.Account" %>
<%
    User user = (User) session.getAttribute("user");
    Account account = (Account) request.getAttribute("account");
    boolean isEdit = (account != null);
    String[] platformTypes = (String[]) request.getAttribute("platformTypes");
    String[] subTypes = (String[]) request.getAttribute("subTypes");
    String[] statuses = (String[]) request.getAttribute("statuses");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isEdit ? "编辑账号" : "新增账号" %> - 企业外部账号管理</title>
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
            <a href="${pageContext.request.contextPath}/logs"><span class="nav-icon">📋</span><span>操作日志</span></a>
            <% } else if (user.isManager()) { %>
            <a href="${pageContext.request.contextPath}/logs"><span class="nav-icon">📋</span><span>操作日志</span></a>
            <% } %>
            <% if (user.isAdmin() || user.isManager()) { %>
            <a href="${pageContext.request.contextPath}/applications/pending"><span class="nav-icon">📝</span><span>审批管理</span></a>
            <% } %>
        </nav>
        <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/logout">🚪 <span>退出登录</span></a></div>
    </div>

    <div class="main-content">
        <div class="topbar">
            <div class="topbar-title"><%= isEdit ? "编辑账号" : "新增账号" %></div>
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
                <form method="post" action="<%= ctx %><%= isEdit ? "/accounts/update" : "/accounts/create" %>" onsubmit="return validateForm()">
                    <% if (isEdit) { %>
                    <input type="hidden" name="id" value="<%= account.getId() %>">
                    <% } %>

                    <!-- Row 1: 所属项目 + 平台类型 -->
                    <div class="form-row">
                        <div class="form-group">
                            <label for="project">所属项目</label>
                            <input type="text" id="project" name="project" value="<%= isEdit && account.getProject() != null ? account.getProject() : "" %>" placeholder="如：电商平台、OA系统">
                        </div>
                        <div class="form-group">
                            <label for="platform_type">平台类型 *</label>
                            <select id="platform_type" name="platform_type" required onchange="toggleSubType()">
                                <option value="">-- 请选择 --</option>
                                <% if (platformTypes != null) for (String pt : platformTypes) { %>
                                <option value="<%= pt %>" <%= isEdit && pt.equals(account.getPlatformType()) ? "selected" : "" %>><%= pt %></option>
                                <% } %>
                            </select>
                        </div>
                    </div>

                    <!-- Row 2: 运维子类型（仅运维服务器时显示） -->
                    <div class="form-row" id="subTypeRow" style="display:none;">
                        <div class="form-group">
                            <label for="sub_type">运维子类型</label>
                            <select id="sub_type" name="sub_type">
                                <option value="">-- 请选择 --</option>
                                <% if (subTypes != null) for (String st : subTypes) { %>
                                <option value="<%= st %>" <%= isEdit && st.equals(account.getSubType()) ? "selected" : "" %>><%= st %></option>
                                <% } %>
                            </select>
                        </div>
                    </div>

                    <!-- Row 3: 账号名称 + 密码 -->
                    <div class="form-row">
                        <div class="form-group">
                            <label for="name">账号名称 *</label>
                            <input type="text" id="name" name="name" required value="<%= isEdit ? account.getName() : "" %>" placeholder="请输入账号名称">
                        </div>
                        <div class="form-group">
                            <label for="password"><%= isEdit ? "密码（留空不修改）" : "密码 *" %></label>
                            <input type="password" id="password" name="password" <%= isEdit ? "" : "required" %> placeholder="<%= isEdit ? "留空则不修改密码" : "请输入账号密码" %>">
                        </div>
                    </div>

                    <!-- Row 4: 连接信息 + 关联部门 -->
                    <div class="form-row">
                        <div class="form-group">
                            <label for="login_url">连接信息</label>
                            <input type="text" id="login_url" name="login_url" value="<%= isEdit && account.getLoginUrl() != null ? account.getLoginUrl() : "" %>" placeholder="URL / IP:端口 / SSH 连接串">
                        </div>
                        <div class="form-group">
                            <label for="department">关联部门</label>
                            <input type="text" id="department" name="department" value="<%= isEdit && account.getDepartment() != null ? account.getDepartment() : "" %>" placeholder="如：IT部、财务部">
                        </div>
                    </div>

                    <!-- Row 5: 到期时间 + 状态 -->
                    <div class="form-row">
                        <div class="form-group">
                            <label for="expiry_date">到期时间</label>
                            <input type="date" id="expiry_date" name="expiry_date" value="<%= isEdit && account.getExpiryDate() != null ? account.getExpiryDate() : "" %>">
                        </div>
                        <div class="form-group">
                            <label for="status">状态</label>
                            <select id="status" name="status">
                                <% if (statuses != null) for (String s : statuses) { %>
                                <option value="<%= s %>" <%= isEdit && s.equals(account.getStatus()) ? "selected" : "" %>><%= s %></option>
                                <% } %>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="notes">备注</label>
                        <textarea id="notes" name="notes" placeholder="备注信息（可选）"><%= isEdit && account.getNotes() != null ? account.getNotes() : "" %></textarea>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary btn-lg"><%= isEdit ? "保存修改" : "创建账号" %></button>
                        <a href="<%= ctx %>/accounts" class="btn btn-outline btn-lg">取消</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
function toggleSubType() {
    var pt = document.getElementById('platform_type').value;
    var row = document.getElementById('subTypeRow');
    row.style.display = (pt === '运维服务器') ? 'flex' : 'none';
}
window.addEventListener('DOMContentLoaded', toggleSubType);

function validateForm() {
    var name = document.getElementById('name').value.trim();
    var platform = document.getElementById('platform_type').value;
    if (!name) { alert('请输入账号名称'); return false; }
    if (!platform) { alert('请选择平台类型'); return false; }
    return true;
}
</script>
</body>
</html>
