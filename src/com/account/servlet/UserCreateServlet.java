package com.account.servlet;

import com.account.dao.UserDAO;
import com.account.dao.UserGroupDAO;
import com.account.model.User;
import com.account.util.PasswordUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户创建 Servlet。
 * <p>
 * 处理新用户注册请求，使用 BCrypt（通过 {@link PasswordUtil#hash}）对密码进行加密存储。
 * 创建用户后可同时分配用户组，实现用户与组的初始关联。
 * </p>
 */
public class UserCreateServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private UserGroupDAO groupDAO = new UserGroupDAO();

    /**
     * 处理 POST 请求，创建新用户。
     * <p>
     * 操作流程：
     * <ol>
     *   <li><b>必填校验</b>：用户名、密码、显示名不可为空，否则重定向至 /users/new?error=1</li>
     *   <li><b>用户名唯一性校验</b>：通过 userDAO.findByUsername() 检查用户名是否已存在，
     *       已存在则重定向至 /users/new?error=2</li>
     *   <li><b>创建用户</b>：
     *     <ul>
     *       <li>密码使用 {@link PasswordUtil#hash}（BCrypt）加密后存储</li>
     *       <li>角色默认 "user"</li>
     *       <li>部门可选，默认为空</li>
     *     </ul>
     *   </li>
     *   <li><b>组关联</b>：如果勾选了用户组，通过 groupDAO.addMember() 逐条添加用户到组</li>
     *   <li>完成后重定向至用户列表页</li>
     * </ol>
     * </p>
     *
     * @param req  HttpServletRequest，包含参数 username/password/display_name/role/department/group_ids
     * @param resp HttpServletResponse
     * @throws ServletException 转发异常
     * @throws IOException      输入/输出异常
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String displayName = req.getParameter("display_name");
        String role = req.getParameter("role");
        String department = req.getParameter("department");

        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            displayName == null || displayName.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/users/new?error=1");
            return;
        }

        User existing = userDAO.findByUsername(username.trim());
        if (existing != null) {
            resp.sendRedirect(req.getContextPath() + "/users/new?error=2");
            return;
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(PasswordUtil.hash(password.trim()));
        user.setDisplayName(displayName.trim());
        user.setRole(role != null ? role : "user");
        user.setDepartment(department != null ? department.trim() : "");

        int newUserId = userDAO.insertAndReturnId(user);

        // Add to selected groups
        String[] groupIds = req.getParameterValues("group_ids");
        if (groupIds != null && newUserId > 0) {
            for (String gid : groupIds) {
                try {
                    groupDAO.addMember(Integer.parseInt(gid), newUserId);
                } catch (NumberFormatException e) {}
            }
        }

        resp.sendRedirect(req.getContextPath() + "/users");
    }
}
