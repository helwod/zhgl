package com.account.servlet;

import com.account.dao.AccountDAO;
import com.account.dao.AccountManagerDAO;
import com.account.dao.ApplicationDAO;
import com.account.dao.PasswordHistoryDAO;
import com.account.model.Account;
import com.account.model.AccountGroupManager;
import com.account.model.AccountManager;
import com.account.model.Application;
import com.account.model.PasswordHistory;
import com.account.model.User;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 账号详情展示Servlet。
 * <p>功能：</p>
 * <ul>
 *   <li>展示指定账号的详细信息</li>
 *   <li>加载关联数据：账号的申请记录、用户级管理员列表、组级管理员列表、密码变更历史</li>
 *   <li>判断当前用户与该账号的关系：是否有已获批申请、是否有待审批申请、是否为管理员</li>
 * </ul>
 *
 * <p><b>权限逻辑：</b></p>
 * <ul>
 *   <li>所有人均可访问（需登录），但详细功能的可见性由前端根据 isManager / hasApprovedApp 控制</li>
 *   <li>管理员关系通过 accountManagerDAO.isManagerWithGroupExpansion() 判断，包含用户级和组级扩展</li>
 * </ul>
 */
public class AccountDetailServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();
    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private AccountManagerDAO accountManagerDAO = new AccountManagerDAO();
    private PasswordHistoryDAO passwordHistoryDAO = new PasswordHistoryDAO();

    /**
     * 处理账号详情展示请求。
     * <ol>
     *   <li>解析账号ID参数，查询账号基本信息</li>
     *   <li>若账号不存在，重定向到账号列表</li>
     *   <li>加载关联数据：申请列表、用户级管理员、组级管理员、密码历史</li>
     *   <li>检查当前用户是否已获批申请该账号（hasApprovedApplicationValid）</li>
     *   <li>检查当前用户是否有待审批申请</li>
     *   <li>检查当前用户是否是该账号的管理员（含用户级和组级扩展判断）</li>
     *   <li>将所有数据设置为请求属性，转发至 /accounts/detail.jsp 渲染</li>
     * </ol>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int id = Integer.parseInt(req.getParameter("id"));
        Account account = accountDAO.findById(id);

        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/accounts");
            return;
        }

        List<Application> apps = applicationDAO.findByAccountId(id);
        List<AccountManager> managers = accountManagerDAO.findByAccountId(id);
        List<AccountGroupManager> groupManagers = accountManagerDAO.findGroupManagersByAccount(id);
        List<PasswordHistory> passwordHistory = passwordHistoryDAO.findByAccountId(id);

        // Check if current user has approved application or pending application
        boolean hasApprovedApp = false;
        boolean hasPendingApp = false;
        boolean isManager = false;
        HttpSession session = req.getSession(false);
        if (session != null) {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser != null) {
                hasApprovedApp = applicationDAO.hasApprovedApplicationValid(id, currentUser.getId());
                for (Application a : apps) {
                    if (a.getApplicantId() == currentUser.getId() && "pending".equals(a.getStatus())) {
                        hasPendingApp = true;
                        break;
                    }
                }
                // Check if current user is a manager of this account (user-level or via group)
                isManager = accountManagerDAO.isManagerWithGroupExpansion(id, currentUser.getId());
            }
        }

        req.setAttribute("account", account);
        req.setAttribute("applications", apps);
        req.setAttribute("accountManagers", managers);
        req.setAttribute("accountGroupManagers", groupManagers);
        req.setAttribute("passwordHistory", passwordHistory);
        req.setAttribute("hasApprovedApp", hasApprovedApp);
        req.setAttribute("hasPendingApp", hasPendingApp);
        req.setAttribute("isManager", isManager);

        req.getRequestDispatcher("/accounts/detail.jsp").forward(req, resp);
    }
}
