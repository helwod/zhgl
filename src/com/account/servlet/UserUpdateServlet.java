package com.account.servlet;

import com.account.dao.UserDAO;
import com.account.dao.UserGroupDAO;
import com.account.model.User;
import com.account.util.PasswordUtil;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用户更新 Servlet。
 * <p>
 * 处理用户信息修改请求，仅超级管理员可执行。
 * 支持更新基本信息（用户名、显示名、角色、部门），
 * 可选更新密码（BCrypt 加密），并同步用户组成员关系。
 * 内置保护机制：禁止降级系统内置 admin 账号的角色。
 * </p>
 */
/**
 * 用户更新 Servlet。
 * <p>
 * 处理用户信息的修改操作，仅超级管理员可执行。
 * 支持修改用户名、显示名、角色、部门，并可选择性重置密码。
 * 同时同步用户组成员关系：对比新旧组列表，移除取消的组、添加新增的组。
 * 禁止将内置超级管理员账号（admin）的角色从 admin 改为其他角色。
 * </p>
 */
public class UserUpdateServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private UserGroupDAO groupDAO = new UserGroupDAO();

    /**
     * 处理 POST 请求，更新用户信息。
     * <p>
     * 操作流程：
     * <ol>
     *   <li><b>权限检查</b>：仅管理员可操作，否则返回 403</li>
     *   <li><b>加载目标用户</b>：通过 userDAO.findById() 获取用户信息</li>
     *   <li><b>保护管理员角色</b>：如果目标用户名为 "admin" 且角色被改为非 admin，则拒绝并报错</li>
     *   <li><b>更新基本信息</b>：用户名、显示名、角色、部门</li>
     *   <li><b>更新密码</b>（可选）：如果提供了非空密码，使用 {@link PasswordUtil#hash}（BCrypt）加密更新</li>
     *   <li><b>组成员同步</b>：
     *     <ul>
     *       <li>查询用户当前组 ID 集合（oldGroupIds）</li>
     *       <li>解析前端提交的新组 ID 集合（newGroupIds）</li>
     *       <li>差集计算：移除不再选中的组，添加新勾选的组</li>
     *     </ul>
     *   </li>
     * </ol>
     * </p>
     *
     * @param req  HttpServletRequest，包含参数 id/username/password/display_name/role/department/group_ids
     * @param resp HttpServletResponse
     * @throws ServletException 转发异常
     * @throws IOException      输入/输出异常
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !currentUser.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足，仅超级管理员可操作");
            return;
        }

        int id = Integer.parseInt(req.getParameter("id"));
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String displayName = req.getParameter("display_name");
        String role = req.getParameter("role");
        String department = req.getParameter("department");

        User user = userDAO.findById(id);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/users");
            return;
        }

        // Prevent demoting the built-in super admin account
        if ("admin".equals(user.getUsername()) && !"admin".equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/users?error=admin_role");
            return;
        }

        user.setUsername(username.trim());
        user.setDisplayName(displayName.trim());
        user.setRole(role != null ? role : "user");
        user.setDepartment(department != null ? department.trim() : "");

        userDAO.update(user);

        if (password != null && !password.trim().isEmpty()) {
            userDAO.updatePassword(id, PasswordUtil.hash(password.trim()));
        }

        // Sync group memberships: remove old, add new
        Set<Integer> oldGroupIds = groupDAO.findGroupsByUser(id)
            .stream().map(m -> m.getGroupId()).collect(Collectors.toSet());
        Set<Integer> newGroupIds = new HashSet<>();
        String[] groupIdParams = req.getParameterValues("group_ids");
        if (groupIdParams != null) {
            for (String gid : groupIdParams) {
                try { newGroupIds.add(Integer.parseInt(gid)); } catch (NumberFormatException e) {}
            }
        }

        // Remove groups that are no longer selected
        for (int gid : oldGroupIds) {
            if (!newGroupIds.contains(gid)) {
                groupDAO.removeMember(gid, id);
            }
        }
        // Add newly selected groups
        for (int gid : newGroupIds) {
            if (!oldGroupIds.contains(gid)) {
                groupDAO.addMember(gid, id);
            }
        }

        resp.sendRedirect(req.getContextPath() + "/users");
    }
}
