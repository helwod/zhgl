package com.account.servlet;

import com.account.dao.ApplicationDAO;
import com.account.model.Application;
import com.account.model.User;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 审批管理列表 Servlet。
 * <p>
 * 根据当前用户角色展示账号申请列表：
 * <ul>
 *   <li><b>管理员</b>：查看所有申请（待审批、已通过、已驳回）</li>
 *   <li><b>经理</b>：仅查看自己管理范围内的账号申请</li>
 *   <li><b>普通用户</b>：无权限，重定向至仪表盘</li>
 * </ul>
 * 申请按状态分为三个类别展示：pendingApps（待审批）、reviewedApps（已通过）、rejectedApps（已驳回）。
 * </p>
 */
public class AppPendingServlet extends HttpServlet {
    private ApplicationDAO applicationDAO = new ApplicationDAO();

    /**
     * 处理 GET 请求，加载并展示账号申请列表。
     * <p>
     * 权限判断流程：
     * <ol>
     *   <li>用户未登录或无管理权限 → 重定向至 /dashboard</li>
     *   <li>经理（非管理员）→ 通过 applicationDAO.search() 传入当前用户 ID 进行范围限定，
     *       仅查询自己管理的账号相关申请</li>
     *   <li>管理员 → 通过 applicationDAO.findByStatus() 查询所有状态的申请</li>
     * </ol>
     * 最终将数据放入 request 属性后转发至 /applications/pending.jsp。
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

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Non-admin users must have management privileges to access approval page
        if (user != null && !user.isAdmin() && !user.isManager()) {
            // Regular user without manager role, redirect to dashboard
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        if (user != null && !user.isAdmin()) {
            // Manager: only see applications for accounts they manage
            int userId = user.getId();
            List<Application> pending = applicationDAO.search(null, "pending", null, null, 1, 9999, userId);
            List<Application> reviewed = applicationDAO.search(null, "approved", null, null, 1, 9999, userId);
            List<Application> rejected = applicationDAO.search(null, "rejected", null, null, 1, 9999, userId);
            req.setAttribute("pendingApps", pending);
            req.setAttribute("reviewedApps", reviewed);
            req.setAttribute("rejectedApps", rejected);
        } else {
            // Admin sees all
            List<Application> pending = applicationDAO.findByStatus("pending");
            List<Application> reviewed = applicationDAO.findByStatus("approved");
            List<Application> rejected = applicationDAO.findByStatus("rejected");
            req.setAttribute("pendingApps", pending);
            req.setAttribute("reviewedApps", reviewed);
            req.setAttribute("rejectedApps", rejected);
        }

        req.getRequestDispatcher("/applications/pending.jsp").forward(req, resp);
    }
}
