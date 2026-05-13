package com.account.servlet;

import com.account.dao.AccountDAO;
import com.account.dao.AccountLogDAO;
import com.account.dao.AccountManagerDAO;
import com.account.model.Account;
import com.account.model.AccountLog;
import com.account.model.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 删除账号 Servlet。
 * <p>
 * 只有 admin 或 manager 可以删除账号。
 * 删除前记录操作日志，并清理用户级和组级的管理员分配关系。
 * 非 admin 用户需要具有该账号的管理权限才能删除。
 * </p>
 */
public class AccountDeleteServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();
    private AccountLogDAO accountLogDAO = new AccountLogDAO();
    private AccountManagerDAO accountManagerDAO = new AccountManagerDAO();

    /**
     * 处理删除账号的POST请求。
     * <ol>
     *   <li>权限校验：仅admin和manager可操作，非admin验证管理权限</li>
     *   <li>加载账号信息用于日志记录</li>
     *   <li>记录删除操作日志（actionType=delete, fieldName=all, oldValue=账号名称）</li>
     *   <li>清理关联的管理员分配记录：</li>
     *   <ul>
     *     <li>删除 user-level 管理关系（accountManagerDAO.deleteByAccount）</li>
     *     <li>删除 group-level 管理关系（accountManagerDAO.deleteGroupManagersByAccount）</li>
     *   </ul>
     *   <li>执行账号删除（accountDAO.delete）</li>
     *   <li>重定向到账号列表页</li>
     * </ol>
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || (!user.isAdmin() && !user.isManager())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
            return;
        }

        int id = Integer.parseInt(req.getParameter("id"));

        // Non-admin: check management permission
        if (!user.isAdmin()) {
            boolean canManage = accountManagerDAO.isManagerWithGroupExpansion(id, user.getId());
            if (!canManage) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有此账号的管理权限");
                return;
            }
        }

        // Get account info before delete for logging
        Account acc = accountDAO.findById(id);

        if (acc != null && user != null) {
            // Write delete log
            AccountLog log = new AccountLog();
            log.setAccountId(id);
            log.setActionType("delete");
            log.setFieldName("all");
            log.setOldValue(acc.getName());
            log.setOperatorId(user.getId());
            log.setOperatorName(user.getDisplayName());
            accountLogDAO.insert(log);

            // Clean up manager assignments (user-level + group-level)
            accountManagerDAO.deleteByAccount(id);
            accountManagerDAO.deleteGroupManagersByAccount(id);
        }

        accountDAO.delete(id);
        resp.sendRedirect(req.getContextPath() + "/accounts");
    }
}
