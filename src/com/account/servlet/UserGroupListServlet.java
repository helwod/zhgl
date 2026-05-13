package com.account.servlet;

import com.account.dao.UserGroupDAO;
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
 * 用户组列表 Servlet。
 * <p>
 * GET 请求：展示所有用户组列表（含成员数量统计）。
 * POST 请求：处理用户组的创建和删除操作（仅超级管理员可操作，返回 JSON 结果）。
 * </p>
 */
/**
 * 用户组列表及管理 Servlet。
 * <p>
 * 同时处理用户组的列表展示和 CRUD 操作（创建、删除）。
 * GET 请求用于展示所有用户组；POST 请求由管理员执行创建或删除操作，以 JSON 格式返回结果。
 * </p>
 */
public class UserGroupListServlet extends HttpServlet {
    private UserGroupDAO userGroupDAO = new UserGroupDAO();

    /**
     * 处理 GET 请求，列出所有用户组。
     * <p>
     * 调用 userGroupDAO.findAll() 获取全部用户组记录，
     * 存入 request 属性后转发至 /groups/list.jsp 进行渲染。
     * </p>
     *
     * @param req  HttpServletRequest
     * @param resp HttpServletResponse
     * @throws ServletException 转发异常
     * @throws IOException      输入/输出异常
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<UserGroup> groups = userGroupDAO.findAll();
        req.setAttribute("groups", groups);
        req.getRequestDispatcher("/groups/list.jsp").forward(req, resp);
    }

    /**
     * 处理 POST 请求，创建或删除用户组。
     * <p>
     * 操作流程：
     * <ol>
     *   <li><b>权限检查</b>：仅管理员可操作，否则返回 JSON 错误</li>
     *   <li><b>创建</b>（action=create）：接收 name 和 description 参数，必填 name，
     *       创建成功后返回 JSON 响应</li>
     *   <li><b>删除</b>（action=delete）：接收 id 参数，删除指定用户组，返回 JSON 响应</li>
     * </ol>
     * 所有操作返回 application/json 格式的响应。
     * </p>
     *
     * @param req  HttpServletRequest，包含参数 action/name/description/id
     * @param resp HttpServletResponse，返回 application/json
     * @throws ServletException 转发异常
     * @throws IOException      输入/输出异常
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            out.write("{\"success\":false,\"message\":\"权限不足\"}");
            return;
        }

        String action = req.getParameter("action");
        if ("create".equals(action)) {
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            if (name == null || name.trim().isEmpty()) {
                out.write("{\"success\":false,\"message\":\"请输入用户组名称\"}");
                return;
            }
            UserGroup g = new UserGroup();
            g.setName(name.trim());
            g.setDescription(description != null ? description.trim() : "");
            try {
                userGroupDAO.insert(g);
                out.write("{\"success\":true,\"message\":\"创建成功\"}");
            } catch (Exception e) {
                out.write("{\"success\":false,\"message\":\"创建失败：" + e.getMessage() + "\"}");
            }
        } else if ("delete".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            try {
                userGroupDAO.delete(id);
                out.write("{\"success\":true,\"message\":\"删除成功\"}");
            } catch (Exception e) {
                out.write("{\"success\":false,\"message\":\"删除失败：" + e.getMessage() + "\"}");
            }
        } else {
            out.write("{\"success\":false,\"message\":\"未知操作\"}");
        }
    }
}
