package com.account.servlet;

import com.account.dao.AccountManagerDAO;
import com.account.dao.UserDAO;
import com.account.dao.UserGroupDAO;
import com.account.model.AccountGroupManager;
import com.account.model.AccountManager;
import com.account.model.User;
import com.account.model.UserGroup;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 账号管理权限分配Servlet。
 * <p>功能：</p>
 * <ul>
 *   <li>GET请求：展示分配管理页面，加载用户级和组级管理员列表</li>
 *   <li>POST请求：以JSON格式响应，支持添加/移除用户级管理员和组级管理员</li>
 * </ul>
 *
 * <p><b>权限要求：</b></p>
 * <ul>
 *   <li>仅admin和manager角色可操作</li>
 *   <li>非admin需验证对目标账号的管理权限</li>
 * </ul>
 *
 * <p><b>管理维度：</b></p>
 * <ul>
 *   <li>用户级（user-level）：直接指定某个用户为账号管理员</li>
 *   <li>组级（group-level）：指定某个用户组，组内所有用户自动成为管理员</li>
 * </ul>
 */
public class AccountAssignServlet extends HttpServlet {
    private AccountManagerDAO accountManagerDAO = new AccountManagerDAO();
    private UserDAO userDAO = new UserDAO();
    private UserGroupDAO userGroupDAO = new UserGroupDAO();

    /**
     * 处理GET请求，展示账号管理权限分配页面。
     * <ol>
     *   <li>权限校验：仅admin和manager可访问</li>
     *   <li>非admin需验证对账号的管理权限</li>
     *   <li>加载当前用户级管理员列表（accountManagerDAO.findByAccountId）</li>
     *   <li>加载当前组级管理员列表（accountManagerDAO.findGroupManagersByAccount）</li>
     *   <li>加载所有非admin用户列表供选择</li>
     *   <li>加载所有用户组列表供选择</li>
     *   <li>设置请求属性，转发至 /accounts/assign.jsp 渲染</li>
     * </ol>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || (!currentUser.isAdmin() && !currentUser.isManager())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
            return;
        }

        int accountId = Integer.parseInt(req.getParameter("account_id"));

        // Non-admin managers can only access accounts they manage
        if (!currentUser.isAdmin()) {
            boolean canManage = accountManagerDAO.isManagerWithGroupExpansion(accountId, currentUser.getId());
            if (!canManage) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有此账号的管理权限");
                return;
            }
        }

        // Get current user-level managers
        List<AccountManager> managers = accountManagerDAO.findByAccountId(accountId);
        // Get current group-level managers
        List<AccountGroupManager> groupManagers = accountManagerDAO.findGroupManagersByAccount(accountId);
        // Get all non-admin users
        List<User> allUsers = userDAO.findAll();
        // Get all groups
        List<UserGroup> allGroups = userGroupDAO.findAll();

        req.setAttribute("accountId", accountId);
        req.setAttribute("managers", managers);
        req.setAttribute("groupManagers", groupManagers);
        req.setAttribute("allUsers", allUsers);
        req.setAttribute("allGroups", allGroups);

        req.getRequestDispatcher("/accounts/assign.jsp").forward(req, resp);
    }

    /**
     * 处理POST请求，执行管理权限的添加/移除操作。
     * <p>请求类型为AJAX异步请求，响应格式为JSON。</p>
     *
     * <p><b>支持的操作类型（action参数）：</b></p>
     * <ul>
     *   <li>add - 添加用户级管理员（参数：user_id）</li>
     *   <li>remove - 移除用户级管理员（参数：user_id）</li>
     *   <li>addGroup - 添加组级管理员（参数：group_id）</li>
     *   <li>removeGroup - 移除组级管理员（参数：group_id）</li>
     * </ul>
     *
     * <ol>
     *   <li>权限校验：仅admin和manager可操作，非admin验证管理权限</li>
     *   <li>根据action参数执行对应操作</li>
     *   <li>返回JSON：{"success": true/false, "message": "操作结果"}</li>
     * </ol>
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession();
        User admin = (User) session.getAttribute("user");
        if (admin == null || (!admin.isAdmin() && !admin.isManager())) {
            out.write("{\"success\":false,\"message\":\"权限不足\"}");
            return;
        }

        String action = req.getParameter("action");
        int accountId = Integer.parseInt(req.getParameter("account_id"));

        // Non-admin managers: check they manage this account
        if (!admin.isAdmin()) {
            boolean canManage = accountManagerDAO.isManagerWithGroupExpansion(accountId, admin.getId());
            if (!canManage) {
                out.write("{\"success\":false,\"message\":\"您没有此账号的管理权限\"}");
                return;
            }
        }

        try {
            if ("add".equals(action)) {
                int userId = Integer.parseInt(req.getParameter("user_id"));
                AccountManager am = new AccountManager();
                am.setAccountId(accountId);
                am.setUserId(userId);
                am.setAssignedBy(admin.getId());
                accountManagerDAO.insert(am);
                out.write("{\"success\":true,\"message\":\"添加成功\"}");
            } else if ("remove".equals(action)) {
                int userId = Integer.parseInt(req.getParameter("user_id"));
                accountManagerDAO.delete(accountId, userId);
                out.write("{\"success\":true,\"message\":\"移除成功\"}");
            } else if ("addGroup".equals(action)) {
                int groupId = Integer.parseInt(req.getParameter("group_id"));
                accountManagerDAO.insertGroupManager(accountId, groupId, admin.getId());
                out.write("{\"success\":true,\"message\":\"添加成功\"}");
            } else if ("removeGroup".equals(action)) {
                int groupId = Integer.parseInt(req.getParameter("group_id"));
                accountManagerDAO.deleteGroupManager(accountId, groupId);
                out.write("{\"success\":true,\"message\":\"移除成功\"}");
            } else {
                out.write("{\"success\":false,\"message\":\"未知操作\"}");
            }
        } catch (Exception e) {
            out.write("{\"success\":false,\"message\":\"操作失败：" + e.getMessage() + "\"}");
        }
    }
}
