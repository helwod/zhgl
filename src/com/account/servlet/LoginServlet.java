package com.account.servlet;

import com.account.dao.LoginLogDAO;
import com.account.dao.UserDAO;
import com.account.model.LoginLog;
import com.account.model.User;
import com.account.util.PasswordUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用户登录 Servlet。
 * <p>
 * GET 请求显示登录页面，如果已登录则跳转到仪表盘。
 * 同时检查系统是否已初始化（admin 用户是否存在），用于控制登录页初始化按钮的显示。
 * POST 请求处理登录：使用 BCrypt 验证密码，成功后创建会话并记录登录日志。
 * </p>
 */
public class LoginServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private LoginLogDAO loginLogDAO = new LoginLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        // Check if system is already initialized (admin user exists)
        User adminCheck = userDAO.findByUsername("admin");
        req.setAttribute("systemInitialized", adminCheck != null);

        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    /**
     * 处理用户登录请求（POST方式）。
     * <ol>
     *   <li>获取用户名和密码参数，校验非空</li>
     *   <li>查询数据库获取用户信息，若用户不存在则返回错误</li>
     *   <li>使用 BCrypt（PasswordUtil.verify）比对明文密码和数据库中加密密码</li>
     *   <li>验证通过后，创建HttpSession并将User对象存入session</li>
     *   <li>记录登录日志（用户ID、用户名、显示名、IP地址）</li>
     *   <li>跳转到仪表盘页面</li>
     * </ol>
     *
     * <p><b>安全要点：</b></p>
     * <ul>
     *   <li>密码比对使用BCrypt算法，数据库中不存储明文密码</li>
     *   <li>登录日志记录包含用户IP地址，便于安全审计</li>
     * </ul>
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || password == null || username.trim().isEmpty()) {
            req.setAttribute("error", "请输入用户名和密码");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null || !PasswordUtil.verify(password, user.getPassword())) {
            req.setAttribute("error", "用户名或密码错误");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession();
        session.setAttribute("user", user);

        // Record login log
        try {
            LoginLog log = new LoginLog();
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
            log.setDisplayName(user.getDisplayName());
            log.setIpAddress(req.getRemoteAddr());
            loginLogDAO.insert(log);
        } catch (Exception e) {
            System.out.println("[LOGIN] Failed to record login log: " + e.getMessage());
        }

        System.out.println("[LOGIN] User '" + user.getDisplayName() + "' logged in at "
                + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
