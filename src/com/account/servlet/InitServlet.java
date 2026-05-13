package com.account.servlet;

import com.account.dao.DBUtil;
import com.account.dao.UserDAO;
import com.account.model.User;
import com.account.util.PasswordUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 系统初始化 Servlet。
 * <p>
 * 在应用启动时自动初始化数据库（创建表结构），
 * 并可通过 GET 请求手动触发初始数据创建（admin 管理员账号和测试用户）。
 * 系统只会初始化一次，如果管理员账号已存在则跳过。
 * </p>
 */
public class InitServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        // Initialize database on startup
        String dbPath = getServletContext().getRealPath("/WEB-INF/accountant.db");
        DBUtil.getInstance(dbPath).initDatabase();

        // Set crypto key as system property for CryptoUtil to use
        String cryptoKey = getServletContext().getInitParameter("cryptoKey");
        if (cryptoKey != null && !cryptoKey.isEmpty()) {
            System.setProperty("crypto.key", cryptoKey);
            System.out.println("[INIT] Crypto key set from web.xml context-param");
        } else {
            System.out.println("[INIT] WARNING: cryptoKey context-param not found in web.xml");
        }

        System.out.println("[INIT] Database path: " + dbPath);
    }

    /**
     * 处理GET请求，执行手动初始化操作。
     * <ol>
     *   <li>确保数据库表已创建（处理空数据库文件的情况）</li>
     *   <li>检查admin账号是否已存在，若存在则提示跳转到登录页</li>
     *   <li>若admin不存在，创建管理员账号（admin/admin123，BCrypt加密）</li>
     *   <li>创建演示用户账号（user/user123）</li>
     *   <li>HTML页面展示初始化结果及登录入口</li>
     * </ol>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // Ensure database tables exist (handle empty DB file from deploy)
        String dbPath = getServletContext().getRealPath("/WEB-INF/accountant.db");
        DBUtil.getInstance(dbPath).initDatabase();

        UserDAO userDAO = new UserDAO();

        // Check if admin already exists
        User existingAdmin = userDAO.findByUsername("admin");
        if (existingAdmin != null) {
            out.println("<html><body><h3>管理员账号已存在，无需重复初始化。</h3>");
            out.println("<a href='" + req.getContextPath() + "/login'>前往登录</a></body></html>");
            return;
        }

        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(PasswordUtil.hash("admin123"));
        admin.setDisplayName("系统管理员");
        admin.setRole("admin");
        admin.setDepartment("IT部");
        userDAO.insert(admin);

        // Create a demo user
        User demoUser = new User();
        demoUser.setUsername("user");
        demoUser.setPassword(PasswordUtil.hash("user123"));
        demoUser.setDisplayName("测试用户");
        demoUser.setRole("user");
        demoUser.setDepartment("业务部");
        userDAO.insert(demoUser);

        out.println("<html><body style='font-family:Microsoft YaHei; padding:40px;'>");
        out.println("<h2>初始化成功！</h2>");
        out.println("<h3>管理员账号</h3>");
        out.println("<p>用户名：admin</p>");
        out.println("<p>密码：admin123</p>");
        out.println("<h3>普通用户账号</h3>");
        out.println("<p>用户名：user</p>");
        out.println("<p>密码：user123</p>");
        out.println("<br/><a href='" + req.getContextPath() + "/login' style='padding:10px 24px; background:#1a73e8; color:white; text-decoration:none; border-radius:4px;'>前往登录</a>");
        out.println("</body></html>");
    }
}
