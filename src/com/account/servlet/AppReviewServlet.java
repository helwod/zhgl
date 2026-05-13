package com.account.servlet;

import com.account.dao.AccountAssignmentDAO;
import com.account.dao.AccountManagerDAO;
import com.account.dao.ApplicationDAO;
import com.account.model.AccountAssignment;
import com.account.model.Application;
import com.account.model.User;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 申请审批/驳回 Servlet。
 * <p>
 * 处理对账号使用申请的审批或驳回操作。审批通过时，除了更新申请状态，
 * 还会自动创建 {@link AccountAssignment} 记录，用于追踪用户对账号的使用期限。
 * 驳回操作仅更新申请状态并记录审批意见。
 * </p>
 */
public class AppReviewServlet extends HttpServlet {
    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private AccountAssignmentDAO accountAssignmentDAO = new AccountAssignmentDAO();
    private AccountManagerDAO accountManagerDAO = new AccountManagerDAO();

    /**
     * 处理 POST 请求，执行审批或驳回操作。
     * <p>
     * 操作流程：
     * <ol>
     *   <li><b>登录检查</b>：用户必须已登录</li>
     *   <li><b>权限检查</b>：非管理员用户必须通过
     *       {@link AccountManagerDAO#isManagerWithGroupExpansion(int, int)} 验证
     *       是否是该账号的管理者，否则返回 403</li>
     *   <li><b>审批操作</b>（action=approve）：
     *     <ul>
     *       <li>调用 applicationDAO.approve() 更新申请状态</li>
     *       <li>计算有效期：取申请时的 validDays（默认 7 天），从当前日期推算到期日</li>
     *       <li>创建 AccountAssignment 记录并持久化</li>
     *       <li>控制台输出审批日志</li>
     *     </ul>
     *   </li>
     *   <li><b>驳回操作</b>（action=reject）：
     *     <ul>
     *       <li>调用 applicationDAO.reject() 更新申请状态</li>
     *       <li>不创建账号分配记录</li>
     *     </ul>
     *   </li>
     * </ol>
     * 操作完成后重定向至审批列表页。
     * </p>
     *
     * @param req  HttpServletRequest，包含参数 id（申请ID）、action（approve/reject）、comment（审批意见）
     * @param resp HttpServletResponse
     * @throws ServletException 转发异常
     * @throws IOException      输入/输出异常
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "未登录");
            return;
        }

        int id = Integer.parseInt(req.getParameter("id"));
        String action = req.getParameter("action");
        String comment = req.getParameter("comment");

        // Get application details
        com.account.model.Application app = applicationDAO.findById(id);
        if (app == null) {
            resp.sendRedirect(req.getContextPath() + "/applications/pending");
            return;
        }

        // Permission check: non-admin users must manage the account
        if (!user.isAdmin()) {
            boolean canManage = accountManagerDAO.isManagerWithGroupExpansion(app.getAccountId(), user.getId());
            if (!canManage) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有该账号的审批权限");
                return;
            }
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(new Date());

        if ("approve".equals(action)) {

            applicationDAO.approve(id, user.getId(), comment != null ? comment.trim() : "");
            System.out.println("[APPROVE] Application ID " + id + " approved by "
                + user.getDisplayName() + " at " + timestamp);

            // Create account assignment record for user-account expiry tracking
            if (app != null) {
                int validDays = app.getValidDays() != null ? app.getValidDays() : 7;
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, validDays);
                String newExpiry = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

                AccountAssignment aa = new AccountAssignment();
                aa.setAccountId(app.getAccountId());
                aa.setUserId(app.getApplicantId());
                aa.setExpiryDate(newExpiry);
                accountAssignmentDAO.insert(aa);

                System.out.println("[APPROVE] Assignment created: account=" + app.getAccountId()
                    + " user=" + app.getApplicantId() + " until " + newExpiry);
            }
        } else if ("reject".equals(action)) {
            applicationDAO.reject(id, user.getId(), comment != null ? comment.trim() : "");
            System.out.println("[REJECT] Application ID " + id + " rejected by "
                + user.getDisplayName() + " at " + timestamp);
        }

        resp.sendRedirect(req.getContextPath() + "/applications/pending");
    }
}
