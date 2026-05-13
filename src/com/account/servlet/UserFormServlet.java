package com.account.servlet;

import com.account.dao.UserDAO;
import com.account.dao.UserGroupDAO;
import com.account.model.User;
import com.account.model.UserGroup;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户创建/编辑表单 Servlet。
 * <p>
 * 根据是否提供 id 参数决定是"新建用户"模式还是"编辑用户"模式。
 * 加载所有可用用户组信息供前端勾选，在编辑模式下还会加载用户已关联的组 ID。
 * </p>
 */
public class UserFormServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private UserGroupDAO groupDAO = new UserGroupDAO();

    /**
     * 处理 GET 请求，加载用户表单页面。
     * <p>
     * 行为模式：
     * <ul>
     *   <li><b>新建模式</b>（无 id 参数）：userEdit 属性为 null，表单表现为空白新建页</li>
     *   <li><b>编辑模式</b>（有 id 参数）：
     *     <ul>
     *       <li>通过 userDAO.findById() 加载用户信息，放入 userEdit 属性</li>
     *       <li>通过 groupDAO.findGroupsByUser() 获取用户已关联的用户组 ID 列表</li>
     *     </ul>
     *   </li>
     * </ul>
     * 两种模式均加载所有用户组列表（allGroups），用于多选控件的渲染。
     * 最终转发至 /users/form.jsp。
     * </p>
     *
     * @param req  HttpServletRequest，可选参数 id（编辑模式时传入）
     * @param resp HttpServletResponse
     * @throws ServletException 转发异常
     * @throws IOException      输入/输出异常
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String editId = req.getParameter("id");
        User userEdit = null;

        if (editId != null && !editId.isEmpty()) {
            try {
                userEdit = userDAO.findById(Integer.parseInt(editId));
            } catch (NumberFormatException e) {}
        }

        // Load all groups
        List<UserGroup> allGroups = groupDAO.findAll();
        req.setAttribute("allGroups", allGroups);

        // Load user's existing groups
        if (userEdit != null) {
            List<Integer> userGroupIds = groupDAO.findGroupsByUser(userEdit.getId())
                .stream().map(m -> m.getGroupId()).collect(Collectors.toList());
            req.setAttribute("userGroupIds", userGroupIds);
        }

        req.setAttribute("userEdit", userEdit);
        req.getRequestDispatcher("/users/form.jsp").forward(req, resp);
    }
}
