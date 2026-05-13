<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.account.model.User, com.account.model.Account, com.account.model.Application, com.account.model.AccountManager, com.account.model.AccountGroupManager, com.account.model.PasswordHistory" %>
<%
    User user = (User) session.getAttribute("user");
    Account acc = (Account) request.getAttribute("account");
    List<Application> apps = (List<Application>) request.getAttribute("applications");
    List<AccountManager> managers = (List<AccountManager>) request.getAttribute("accountManagers");
    List<PasswordHistory> passwordHistory = (List<PasswordHistory>) request.getAttribute("passwordHistory");
    Boolean hasApprovedApp = (Boolean) request.getAttribute("hasApprovedApp");
    Boolean hasPendingApp = (Boolean) request.getAttribute("hasPendingApp");
    Boolean isManager = (Boolean) request.getAttribute("isManager");
    if (hasApprovedApp == null) hasApprovedApp = false;
    if (hasPendingApp == null) hasPendingApp = false;
    if (isManager == null) isManager = false;
    if (managers == null) managers = java.util.Collections.emptyList();
    if (passwordHistory == null) passwordHistory = java.util.Collections.emptyList();
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>账号详情 - 企业外部账号管理</title>
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
            <div class="topbar-title">账号详情</div>
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
            <!-- Basic Info -->
            <div class="form-card" style="max-width: 100%;">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;">
                    <div>
                        <h3 style="font-size: 18px; font-weight: 600;"><%= acc.getName() %></h3>
                        <span class="badge badge-primary"><%= acc.getPlatformType() %></span>
                        <% if (acc.getSubType() != null && !acc.getSubType().isEmpty()) { %>
                            <span class="badge badge-info" style="margin-left: 8px;"><%= acc.getSubType() %></span>
                        <% } %>
                        <% if ("可用".equals(acc.getStatus())) { %>
                            <span class="badge badge-success" style="margin-left: 8px;">可用</span>
                        <% } else if ("已分配".equals(acc.getStatus())) { %>
                            <span class="badge badge-warning" style="margin-left: 8px;">已分配</span>
                        <% } else { %>
                            <span class="badge badge-danger" style="margin-left: 8px;">已过期</span>
                        <% } %>
                    </div>
                    <div style="display: flex; gap: 8px;">
                        <% if (user.isAdmin() || hasApprovedApp || (user.isManager() && isManager)) { %>
                        <button onclick="viewPassword()" class="btn btn-primary">🔑 查看密码</button>
                        <% } %>
                        <% if (user.isAdmin() || (user.isManager() && isManager)) { %>
                        <a href="<%= ctx %>/accounts/edit?id=<%= acc.getId() %>" class="btn btn-outline">编辑</a>
                        <a href="<%= ctx %>/accounts/assign?account_id=<%= acc.getId() %>" class="btn btn-info">👥 分配管理</a>
                        <% } %>
                        <% if (user.isAdmin() && passwordHistory != null && !passwordHistory.isEmpty()) { %>
                        <button onclick="viewPasswordHistory()" class="btn btn-outline">📜 历史密码</button>
                        <% } %>
                        <a href="<%= ctx %>/accounts" class="btn btn-outline">返回列表</a>
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; padding: 16px 0; border-top: 1px solid #e8e8e8;">
                    <div>
                        <div style="color: #999; font-size: 12px;">所属项目</div>
                        <div style="font-weight: 500;"><%= acc.getProject() != null && !acc.getProject().isEmpty() ? acc.getProject() : "-" %></div>
                    </div>
                    <div>
                        <div style="color: #999; font-size: 12px;">部门</div>
                        <div style="font-weight: 500;"><%= acc.getDepartment() != null && !acc.getDepartment().isEmpty() ? acc.getDepartment() : "-" %></div>
                    </div>
                    <div>
                        <div style="color: #999; font-size: 12px;">到期时间</div>
                        <div style="font-weight: 500;"><%= acc.getExpiryDate() != null ? acc.getExpiryDate() : "-" %></div>
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: 1fr; gap: 16px; padding: 16px 0; border-top: 1px solid #e8e8e8;">
                    <div>
                        <div style="color: #999; font-size: 12px;">连接信息</div>
                        <div style="font-weight: 500;">
                            <% if (acc.getLoginUrl() != null && !acc.getLoginUrl().isEmpty()) { %>
                                <code style="background: #f5f5f5; padding: 2px 8px; border-radius: 3px; font-size: 13px;"><%= acc.getLoginUrl() %></code>
                            <% } else { %>
                                -
                            <% } %>
                        </div>
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; padding: 16px 0; border-top: 1px solid #e8e8e8;">
                    <div>
                        <div style="color: #999; font-size: 12px;">创建时间</div>
                        <div style="font-weight: 500;"><%= acc.getCreatedAt() != null ? acc.getCreatedAt() : "-" %></div>
                    </div>
                    <div>
                        <div style="color: #999; font-size: 12px;">更新时间</div>
                        <div style="font-weight: 500;"><%= acc.getUpdatedAt() != null ? acc.getUpdatedAt() : "-" %></div>
                    </div>
                </div>

                <% if (acc.getNotes() != null && !acc.getNotes().isEmpty()) { %>
                <div style="padding: 12px 0; border-top: 1px solid #e8e8e8;">
                    <div style="color: #999; font-size: 12px; margin-bottom: 4px;">备注</div>
                    <div><%= acc.getNotes() %></div>
                </div>
                <% } %>

                <!-- Password display area -->
                <div id="passwordArea" style="display:none; padding: 12px 0; border-top: 1px solid #e8e8e8; margin-top: 12px;">
                    <div style="color: #999; font-size: 12px; margin-bottom: 4px;">账号密码</div>
                    <div id="passwordValue" style="font-size: 16px; font-weight: 600; color: #1a73e8; font-family: 'Courier New', monospace; background: #f5f8ff; padding: 8px 12px; border-radius: 4px; display: inline-block;"></div>
                    <span id="passwordLoading" style="color: #999; font-size: 13px;">正在解密...</span>
                </div>
            </div>

            <!-- Application Section (non-admin users) -->
            <% if (!user.isAdmin() && !hasApprovedApp) { %>
            <div style="margin-top: 20px;">
                <div class="form-card" style="max-width: 100%;">
                    <h3 class="section-title">申请使用此账号</h3>
                    <% if (hasPendingApp) { %>
                        <div class="empty-state"><p>⏳ 您已提交申请，请等待管理员审批</p></div>
                    <% } else { %>
                    <form method="post" action="<%= ctx %>/applications/create" onsubmit="return confirm('确认提交申请？')">
                        <input type="hidden" name="account_id" value="<%= acc.getId() %>">
                        <div class="form-group">
                            <label for="reason">申请原因</label>
                            <textarea id="reason" name="reason" required placeholder="请输入申请使用此账号的原因..." style="min-height: 60px;"></textarea>
                        </div>
                        <div class="form-group">
                            <label for="valid_days">使用天数</label>
                            <input type="number" id="valid_days" name="valid_days" value="7" min="1" max="365" style="width:120px;">
                            <span style="color:#999;font-size:12px;margin-left:8px;">默认7天</span>
                        </div>
                        <button type="submit" class="btn btn-primary btn-lg">📝 提交申请</button>
                    </form>
                    <% } %>
                </div>
            </div>
            <% } %>

            <!-- Account Managers Section -->
            <% if (user.isAdmin() || (user.isManager() && isManager)) {
                List<AccountGroupManager> groupMgrs = (List<AccountGroupManager>) request.getAttribute("accountGroupManagers"); %>
            <div style="margin-top: 20px;">
                <div class="form-card" style="max-width: 100%;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                        <h3 class="section-title" style="margin:0;">👥 当前管理人</h3>
                        <a href="<%= ctx %>/accounts/assign?account_id=<%= acc.getId() %>" class="btn btn-sm btn-outline">管理分配</a>
                    </div>
                    <% if (managers != null && !managers.isEmpty()) { %>
                    <div style="margin-bottom: 8px; font-size: 12px; color: #666;">个人管理人：</div>
                    <div style="display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px;">
                    <% for (AccountManager am : managers) { %>
                        <span class="badge badge-primary" style="padding: 4px 12px; font-size: 13px;">
                            <%= am.getUserDisplayName() != null ? am.getUserDisplayName() : "用户#" + am.getUserId() %>
                        </span>
                    <% } %>
                    </div>
                    <% } %>
                    <% if (groupMgrs != null && !groupMgrs.isEmpty()) { %>
                    <div style="margin-bottom: 8px; font-size: 12px; color: #666;">用户组管理人：</div>
                    <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                    <% for (AccountGroupManager gm : groupMgrs) { %>
                        <span class="badge badge-info" style="padding: 4px 12px; font-size: 13px;">
                            <%= gm.getGroupName() != null ? gm.getGroupName() : "组#" + gm.getGroupId() %>
                            <span style="font-weight:normal;margin-left:4px;">(<%= gm.getMemberCount() %>人)</span>
                        </span>
                    <% } %>
                    </div>
                    <% } %>
                    <% if ((managers == null || managers.isEmpty()) && (groupMgrs == null || groupMgrs.isEmpty())) { %>
                    <div class="empty-state" style="padding: 8px;"><p style="font-size: 13px;">暂无管理人，点击"管理分配"进行设置</p></div>
                    <% } %>
                </div>
            </div>
            <% } %>

            <!-- Application History -->
            <div style="margin-top: 20px;">
                <div class="table-container">
                    <div style="padding: 12px 16px; border-bottom: 1px solid #e8e8e8; font-weight: 600; font-size: 14px;">
                        申请记录
                    </div>
                    <table>
                        <thead>
                            <tr>
                                <th>申请人</th>
                                <th>申请原因</th>
                                <th>使用天数</th>
                                <th>状态</th>
                                <th>审批意见</th>
                                <th>申请时间</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% if (apps != null && !apps.isEmpty()) {
                            for (Application a : apps) { %>
                            <tr>
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
                                <td><%= a.getReviewComment() != null ? a.getReviewComment() : "-" %></td>
                                <td style="color:#999;font-size:12px;"><%= a.getCreatedAt() != null ? a.getCreatedAt().substring(0, 16) : "" %></td>
                            </tr>
                        <% }
                        } else { %>
                            <tr><td colspan="6"><div class="empty-state"><p>暂无申请记录</p></div></td></tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Password View Modal -->
<div id="passwordModal" class="modal-overlay">
    <div class="modal-box">
        <div class="modal-header">
            <h3>🔐 密码查看确认</h3>
            <button class="modal-close" onclick="closeModal()">&times;</button>
        </div>
        <div class="modal-body">
            <p style="margin-bottom: 12px;">您正在查看账号 <strong><%= acc.getName() %></strong> 的密码。</p>
            <p style="color: #ff4d4f; font-size: 13px;">⚠️ 此操作将被记录到审计日志。</p>
        </div>
        <div class="modal-footer">
            <button class="btn btn-outline" onclick="closeModal()">取消</button>
            <button class="btn btn-primary" onclick="confirmViewPassword()">确认查看</button>
        </div>
    </div>
</div>

<!-- Password History Modal (admin only) -->
<div id="passwordHistoryModal" class="modal-overlay">
    <div class="modal-box" style="width: 600px;">
        <div class="modal-header">
            <h3>📜 历史密码记录</h3>
            <button class="modal-close" onclick="closeHistoryModal()">&times;</button>
        </div>
        <div class="modal-body">
            <div id="historyLoading" style="text-align:center;padding:20px;color:#999;">正在加载...</div>
            <div id="historyContent" style="display:none;"></div>
        </div>
        <div class="modal-footer">
            <button class="btn btn-outline" onclick="closeHistoryModal()">关闭</button>
        </div>
    </div>
</div>

<script>
function closeModal() {
    document.getElementById('passwordModal').classList.remove('show');
}

function viewPassword() {
    document.getElementById('passwordModal').classList.add('show');
}

function confirmViewPassword() {
    closeModal();
    document.getElementById('passwordArea').style.display = 'block';
    document.getElementById('passwordLoading').style.display = 'inline';
    document.getElementById('passwordValue').textContent = '';

    fetch('<%= ctx %>/accounts/password/view', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'account_id=<%= acc.getId() %>'
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        document.getElementById('passwordLoading').style.display = 'none';
        if (data.success) {
            document.getElementById('passwordValue').textContent = data.password;
        } else {
            document.getElementById('passwordValue').textContent = '解密失败：' + data.message;
            document.getElementById('passwordValue').style.color = '#ff4d4f';
        }
    })
    .catch(function(err) {
        document.getElementById('passwordLoading').style.display = 'none';
        document.getElementById('passwordValue').textContent = '请求失败，请重试';
        document.getElementById('passwordValue').style.color = '#ff4d4f';
    });
}

function closeHistoryModal() {
    document.getElementById('passwordHistoryModal').classList.remove('show');
}

function viewPasswordHistory() {
    document.getElementById('passwordHistoryModal').classList.add('show');
    document.getElementById('historyLoading').style.display = 'block';
    document.getElementById('historyContent').style.display = 'none';

    fetch('<%= ctx %>/accounts/password/history', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'account_id=<%= acc.getId() %>'
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        document.getElementById('historyLoading').style.display = 'none';
        if (data.success && data.data && data.data.length > 0) {
            var html = '<table><thead><tr><th>编号</th><th>历史密码</th><th>修改人</th><th>修改时间</th></tr></thead><tbody>';
            for (var i = 0; i < data.data.length; i++) {
                var h = data.data[i];
                html += '<tr><td>' + h.id + '</td><td style="font-family:monospace;font-weight:600;color:#1a73e8;">' + h.password + '</td><td>' + h.changedBy + '</td><td style="color:#999;font-size:12px;">' + h.changedAt + '</td></tr>';
            }
            html += '</tbody></table>';
            document.getElementById('historyContent').innerHTML = html;
            document.getElementById('historyContent').style.display = 'block';
        } else {
            document.getElementById('historyContent').innerHTML = '<div class="empty-state"><p>暂无历史密码记录</p></div>';
            document.getElementById('historyContent').style.display = 'block';
        }
    })
    .catch(function(err) {
        document.getElementById('historyLoading').style.display = 'none';
        document.getElementById('historyContent').innerHTML = '<div style="color:#ff4d4f;padding:20px;">加载失败，请重试</div>';
        document.getElementById('historyContent').style.display = 'block';
    });
}
function closeModal() {
    document.getElementById('passwordModal').classList.remove('show');
}

function viewPassword() {
    document.getElementById('passwordModal').classList.add('show');
}

function confirmViewPassword() {
    closeModal();
    document.getElementById('passwordArea').style.display = 'block';
    document.getElementById('passwordLoading').style.display = 'inline';
    document.getElementById('passwordValue').textContent = '';

    fetch('<%= ctx %>/accounts/password/view', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'account_id=<%= acc.getId() %>'
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        document.getElementById('passwordLoading').style.display = 'none';
        if (data.success) {
            document.getElementById('passwordValue').textContent = data.password;
        } else {
            document.getElementById('passwordValue').textContent = '解密失败：' + data.message;
            document.getElementById('passwordValue').style.color = '#ff4d4f';
        }
    })
    .catch(function(err) {
        document.getElementById('passwordLoading').style.display = 'none';
        document.getElementById('passwordValue').textContent = '请求失败，请重试';
        document.getElementById('passwordValue').style.color = '#ff4d4f';
    });
}
</script>
</body>
</html>
