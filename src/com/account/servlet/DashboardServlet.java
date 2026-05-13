package com.account.servlet;

import com.account.dao.AccountDAO;
import com.account.dao.AccountManagerDAO;
import com.account.dao.ApplicationDAO;
import com.account.dao.PasswordLogDAO;
import com.account.dao.UserDAO;
import com.account.model.PasswordLog;
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
 * 仪表盘Servlet，根据用户角色展示不同的统计数据。
 * <p>功能：</p>
 * <ul>
 *   <li>根据角色（admin/manager/user）统计账号总数、待审批申请、应用总数、操作日志数、用户数</li>
 *   <li>展示最近5条密码查看日志（按权限范围过滤）</li>
 *   <li>展示最近5条应用申请记录（按权限范围过滤）</li>
 * </ul>
 *
 * <p><b>权限差异：</b></p>
 * <ul>
 *   <li><b>管理员（admin）：</b>所有全局统计数据，查看所有日志和申请</li>
 *   <li><b>经理（manager）：</b>仅显示其管辖范围内的账号统计和申请</li>
 *   <li><b>普通用户（user）：</b>显示已获批账号数、本人提交的申请数、本人相关的日志</li>
 *   <li><b>通过组分配的经理：</b>与经理角色同级别权限</li>
 * </ul>
 */
public class DashboardServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();
    private AccountManagerDAO accountManagerDAO = new AccountManagerDAO();
    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private PasswordLogDAO passwordLogDAO = new PasswordLogDAO();
    private UserDAO userDAO = new UserDAO();

    /**
     * 处理仪表盘数据展示请求。
     * <ol>
     *   <li>获取当前登录用户及其角色信息</li>
     *   <li>根据角色计算不同维度的统计数据：</li>
     *   <ul>
     *     <li>管理员：accountDAO.countAll(), applicationDAO.countPending(), userDAO.count() 等全局统计</li>
     *     <li>经理：accountDAO.countManaged(), applicationDAO.searchCount() 等管辖范围内统计</li>
     *     <li>普通用户：通过 applicationDAO 查询已获批账号数和本人申请数</li>
     *   </ul>
     *   <li>获取最近5条密码操作日志（按角色范围过滤）</li>
     *   <li>获取最近5条应用申请记录（按角色范围过滤）</li>
     *   <li>将数据作为请求属性设置，转发至 dashboard.jsp 渲染</li>
     * </ol>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;
        int userId = (currentUser != null) ? currentUser.getId() : 0;

        int totalAccounts;
        int pendingApps;
        int totalApps;
        int totalLogs;
        int totalUsers;

        if (currentUser != null && currentUser.isAdmin()) {
            // Admin sees global counts
            totalAccounts = accountDAO.countAll();
            pendingApps = applicationDAO.countPending();
            totalApps = applicationDAO.countAll();
            totalLogs = passwordLogDAO.countAll();
            totalUsers = userDAO.count();
        } else if (currentUser != null && currentUser.isManager()) {
            // Manager sees only managed counts
            totalAccounts = accountDAO.countManaged(userId, null, null, null);
            pendingApps = applicationDAO.searchCount(null, "pending", null, null, userId);
            totalApps = applicationDAO.searchCount(null, null, null, null, userId);
            totalLogs = passwordLogDAO.count(null, null, null, userId);
            totalUsers = 0;
        } else if (currentUser != null) {
            // Check if user has management privileges via group assignment
            List<Integer> managedIds = accountManagerDAO.findManagedAccountIdsWithGroups(userId);
            if (!managedIds.isEmpty()) {
                // User has management privileges via group, use managed counts
                totalAccounts = accountDAO.countManaged(userId, null, null, null);
                pendingApps = applicationDAO.searchCount(null, "pending", null, null, userId);
                totalApps = applicationDAO.searchCount(null, null, null, null, userId);
                totalLogs = passwordLogDAO.count(null, null, null, userId);
                totalUsers = 0;
            } else {
                // Regular user sees their own counts
                totalAccounts = applicationDAO.findApprovedAccountIdsByApplicant(userId).size();
                pendingApps = applicationDAO.countByApplicant(userId);
                totalApps = applicationDAO.countByApplicant(userId);
                totalLogs = passwordLogDAO.count(null, null, null, userId);
                totalUsers = 0;
            }
        } else {
            totalAccounts = 0;
            pendingApps = 0;
            totalApps = 0;
            totalLogs = 0;
            totalUsers = 0;
        }

        req.setAttribute("totalAccounts", totalAccounts);
        req.setAttribute("pendingApps", pendingApps);
        req.setAttribute("totalApps", totalApps);
        req.setAttribute("totalLogs", totalLogs);
        req.setAttribute("totalUsers", totalUsers);

        // Recent logs (scope-appropriate)
        List<PasswordLog> recentLogs;
        if (currentUser != null && currentUser.isAdmin()) {
            recentLogs = passwordLogDAO.findAll();
        } else if (currentUser != null) {
            recentLogs = passwordLogDAO.search(null, null, null, 1, 5, userId);
        } else {
            recentLogs = java.util.Collections.emptyList();
        }
        if (recentLogs.size() > 5) recentLogs = recentLogs.subList(0, 5);
        req.setAttribute("recentLogs", recentLogs);

        // Recent applications (scope-appropriate)
        List<Application> recentApps;
        if (currentUser != null && currentUser.isAdmin()) {
            recentApps = applicationDAO.findAll();
        } else if (currentUser != null && currentUser.isManager()) {
            recentApps = applicationDAO.search(null, null, null, null, 1, 5, userId);
        } else if (currentUser != null) {
            recentApps = applicationDAO.findByApplicant(userId);
        } else {
            recentApps = java.util.Collections.emptyList();
        }
        if (recentApps.size() > 5) recentApps = recentApps.subList(0, 5);
        req.setAttribute("recentApps", recentApps);

        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}
