package com.account.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用户退出登录 Servlet。
 * <p>
 * 处理退出登录请求：使当前用户会话失效并重定向到登录页面。
 * 无论是否存在会话，最终都会跳转到登录页。
 * </p>
 */
public class LogoutServlet extends HttpServlet {
    /**
     * 处理退出登录请求。
     * <ol>
     *   <li>获取当前会话（若无会话则不创建新会话）</li>
     *   <li>若会话存在，调用 invalidate() 销毁会话</li>
     *   <li>重定向到登录页面</li>
     * </ol>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
