package com.account.servlet;

import com.account.dao.UserDAO;
import com.account.model.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用户删除 Servlet。
 * <p>
 * 处理删除用户请求。包含多层保护机制以防止误删关键账号：
 * <ul>
 *   <li>仅超级管理员可执行删除操作</li>
 *   <li>禁止删除当前登录用户自身</li>
 *   <li>禁止删除内置超级管理员账号（admin）</li>
 * </ul>
 * </p>
 */
public class UserDeleteServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    /**
     * 处理 POST 请求，删除指定用户。
     * <p>
     * 操作流程：
     * <ol>
     *   <li><b>解析参数</b>：获取待删除用户的 ID</li>
     *   <li><b>权限检查</b>：当前用户必须已登录且是管理员（isAdmin()），否则返回 403</li>
     *   <li><b>自删除保护</b>：检查待删除用户 ID 是否等于当前用户 ID，是则重定向并报错 error=self</li>
     *   <li><b>保护内置管理员</b>：查询目标用户，如果用户名为 "admin" 则拒绝删除，重定向并报错 error=admin_delete</li>
     *   <li><b>执行删除</b>：通过 userDAO.delete() 删除用户记录</li>
     * </ol>
     * </p>
     *
     * @param req  HttpServletRequest，包含参数 id（待删除用户的 ID）
     * @param resp HttpServletResponse
     * @throws ServletException 转发异常
     * @throws IOException      输入/输出异常
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int id = Integer.parseInt(req.getParameter("id"));

        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");

        // Only super admin can delete users
        if (currentUser == null || !currentUser.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足，仅超级管理员可操作");
            return;
        }

        // Prevent self-deletion
        if (currentUser.getId() == id) {
            resp.sendRedirect(req.getContextPath() + "/users?error=self");
            return;
        }

        // Prevent deleting the built-in super admin account
        User targetUser = userDAO.findById(id);
        if (targetUser != null && "admin".equals(targetUser.getUsername())) {
            resp.sendRedirect(req.getContextPath() + "/users?error=admin_delete");
            return;
        }

        userDAO.delete(id);
        resp.sendRedirect(req.getContextPath() + "/users");
    }
}
