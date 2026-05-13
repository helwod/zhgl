package com.account.servlet;

import com.account.dao.UserGroupDAO;
import com.account.dao.UserDAO;
import com.account.model.User;
import com.account.model.UserGroup;
import com.account.model.UserGroupMember;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用户组成员管理 Servlet。
 * <p>
 * GET 请求：展示指定用户组的成员列表和可添加的用户候选列表。
 * POST 请求：处理成员添加（单条/批量）和移除操作（仅超级管理员可操作，返回 JSON 结果）。
 * 批量添加通过 addBatch 操作实现，参数 user_ids 为逗号分隔的用户ID列表。
 * </p>
 */
/**
 * 用户组成员管理 Servlet。
 * <p>
 * 管理指定用户组中的成员关系，包括查看组成员、添加成员（单个/批量）、移除成员。
 * GET 请求用于展示组成员页面；POST 请求由管理员执行增删成员操作，以 JSON 格式返回结果。
 * </p>
 */
public class UserGroupMemberServlet extends HttpServlet {
    private UserGroupDAO userGroupDAO = new UserGroupDAO();
    private UserDAO userDAO = new UserDAO();

    /**
     * 处理 GET 请求，展示指定用户组的成员管理页面。
     * <p>
     * 加载三类数据：
     * <ul>
     *   <li>group - 当前用户组信息</li>
     *   <li>members - 当前组成员列表</li>
     *   <li>availableUsers - 可添加的用户（当前不在组中的用户），用于多选添加</li>
     * </ul>
     * 如果指定 group_id 不存在，重定向至用户组列表页。
     * 最终转发至 /groups/members.jsp。
     * </p>
     *
     * @param req  HttpServletRequest，包含参数 group_id
     * @param resp HttpServletResponse
     * @throws ServletException 转发异常
     * @throws IOException      输入/输出异常
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int groupId = Integer.parseInt(req.getParameter("group_id"));
        UserGroup group = userGroupDAO.findById(groupId);
        if (group == null) {
            resp.sendRedirect(req.getContextPath() + "/groups");
            return;
        }

        List<UserGroupMember> members = userGroupDAO.findMembersByGroup(groupId);
        // Only show users NOT already in the group (multi-select add candidates)
        List<User> availableUsers = userGroupDAO.findUsersNotInGroup(groupId);

        req.setAttribute("group", group);
        req.setAttribute("members", members);
        req.setAttribute("availableUsers", availableUsers);
        req.getRequestDispatcher("/groups/members.jsp").forward(req, resp);
    }

    /**
     * 处理 POST 请求，执行用户组成员的添加或移除操作。
     * <p>
     * 操作流程：
     * <ol>
     *   <li><b>权限检查</b>：仅管理员可操作，否则返回 JSON 错误</li>
     *   <li><b>单个添加</b>（action=add）：通过 user_id 参数添加一个用户到组</li>
     *   <li><b>批量添加</b>（action=addBatch）：通过 user_ids 参数（逗号分隔格式 "1,2,3"）批量添加多个用户，
     *       调用 userGroupDAO.addMembers() 批量插入</li>
     *   <li><b>移除</b>（action=remove）：通过 user_id 参数从组中移除一个用户</li>
     * </ol>
     * 所有操作返回 application/json 格式的响应。
     * </p>
     *
     * @param req  HttpServletRequest，包含参数 action/group_id/user_id/user_ids
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
        int groupId = Integer.parseInt(req.getParameter("group_id"));

        try {
            if ("add".equals(action)) {
                // Single add (backward compatibility)
                int userId = Integer.parseInt(req.getParameter("user_id"));
                userGroupDAO.addMember(groupId, userId);
                out.write("{\"success\":true,\"message\":\"添加成功\"}");
            } else if ("addBatch".equals(action)) {
                // Batch add: user_ids = "1,2,3"
                String userIdsStr = req.getParameter("user_ids");
                if (userIdsStr == null || userIdsStr.isEmpty()) {
                    out.write("{\"success\":false,\"message\":\"请选择用户\"}");
                    return;
                }
                List<Integer> userIds = new ArrayList<>();
                for (String s : userIdsStr.split(",")) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty()) userIds.add(Integer.parseInt(trimmed));
                }
                userGroupDAO.addMembers(groupId, userIds);
                out.write("{\"success\":true,\"message\":\"添加成功（" + userIds.size() + "人）\"}");
            } else if ("remove".equals(action)) {
                int userId = Integer.parseInt(req.getParameter("user_id"));
                userGroupDAO.removeMember(groupId, userId);
                out.write("{\"success\":true,\"message\":\"移除成功\"}");
            } else {
                out.write("{\"success\":false,\"message\":\"未知操作\"}");
            }
        } catch (Exception e) {
            out.write("{\"success\":false,\"message\":\"操作失败：" + e.getMessage() + "\"}");
        }
    }
}
