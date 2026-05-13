<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>企业外部账号管理系统 - 登录</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="login-page">
    <div class="login-card">
        <div class="login-header">
            <h1>🔐 企业外部账号管理</h1>
            <p>Enterprise External Account Manager</p>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="login-error">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <form class="login-form" method="post" action="${pageContext.request.contextPath}/login">
            <div class="form-group">
                <label for="username">用户名</label>
                <input type="text" id="username" name="username" placeholder="请输入用户名" required autofocus>
            </div>
            <div class="form-group">
                <label for="password">密码</label>
                <input type="password" id="password" name="password" placeholder="请输入密码" required>
            </div>
            <button type="submit" class="login-btn">登 录</button>
        </form>

        <% Boolean sysInit = (Boolean) request.getAttribute("systemInitialized");
           if (sysInit == null || !sysInit) { %>
        <div style="margin-top: 20px; padding-top: 16px; border-top: 1px solid #e8e8e8; font-size: 12px; color: #999; text-align: center;">
            首次使用请访问 <a href="${pageContext.request.contextPath}/init">初始化页面</a> 创建管理员账号
        </div>
        <% } %>
    </div>
</div>
</body>
</html>
