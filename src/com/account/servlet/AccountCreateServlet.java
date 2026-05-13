package com.account.servlet;

import com.account.dao.AccountDAO;
import com.account.dao.AccountLogDAO;
import com.account.dao.AccountManagerDAO;
import com.account.model.Account;
import com.account.model.AccountLog;
import com.account.model.AccountManager;
import com.account.model.User;
import com.account.util.CryptoUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 创建账号Servlet。
 * <p>功能：</p>
 * <ul>
 *   <li>接收表单提交的账号信息（名称、密码、平台类型、子类型、项目、部门等）</li>
 *   <li>使用AES加密（CryptoUtil）对密码进行加密存储</li>
 *   <li>自动将创建者设为该账号的管理员（AccountManager）</li>
 *   <li>记录创建操作日志</li>
 * </ul>
 *
 * <p><b>权限要求：</b>仅admin和manager角色可以创建账号。</p>
 * <p><b>安全要点：</b>密码在数据库中始终以AES加密形式存储，任何场景均不存储明文。</p>
 */
public class AccountCreateServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();
    private AccountLogDAO accountLogDAO = new AccountLogDAO();
    private AccountManagerDAO accountManagerDAO = new AccountManagerDAO();

    /**
     * 处理创建账号的POST请求。
     * <ol>
     *   <li>权限校验：仅admin和manager可操作</li>
     *   <li>参数校验：账号名称和平台类型不能为空</li>
     *   <li>使用CryptoUtil对密码进行AES加密，获得密文、IV、认证标签</li>
     *   <li>组装Account对象并写入数据库</li>
     *   <li>记录创建操作日志（actionType=create, fieldName=all）</li>
     *   <li>在 AccountManager 表中插入一条记录，将创建者自动设为该账号的管理员</li>
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

        String name = req.getParameter("name");
        String password = req.getParameter("password");
        String platformType = req.getParameter("platform_type");
        String subType = req.getParameter("sub_type");
        String project = req.getParameter("project");
        String department = req.getParameter("department");
        String expiryDate = req.getParameter("expiry_date");
        String status = req.getParameter("status");
        String loginUrl = req.getParameter("login_url");
        String notes = req.getParameter("notes");

        if (name == null || name.trim().isEmpty() || platformType == null || platformType.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/accounts/new?error=1");
            return;
        }

        // AES encrypt the password
        CryptoUtil crypto = new CryptoUtil();
        String[] encrypted = crypto.encrypt(password != null ? password : "");

        Account acc = new Account();
        acc.setName(name.trim());
        acc.setPasswordEncrypted(encrypted[0]);
        acc.setPasswordIv(encrypted[1]);
        acc.setPasswordTag(encrypted[2]);
        acc.setPlatformType(platformType);
        acc.setSubType(subType != null ? subType : "");
        acc.setProject(project != null ? project : "");
        acc.setDepartment(department != null ? department : "");
        acc.setExpiryDate(expiryDate);
        acc.setStatus(status != null ? status : Account.STATUS_AVAILABLE);
        acc.setLoginUrl(loginUrl != null ? loginUrl.trim() : "");
        acc.setNotes(notes != null ? notes : "");
        acc.setCreatedBy(user.getId());

        int accountId = accountDAO.insert(acc);

        // Write create log
        if (accountId > 0) {
            AccountLog log = new AccountLog();
            log.setAccountId(accountId);
            log.setActionType("create");
            log.setFieldName("all");
            log.setNewValue(name.trim());
            log.setOperatorId(user.getId());
            log.setOperatorName(user.getDisplayName());
            accountLogDAO.insert(log);

            // Auto-assign the creator as a manager of this account
            AccountManager am = new AccountManager();
            am.setAccountId(accountId);
            am.setUserId(user.getId());
            am.setAssignedBy(user.getId());
            accountManagerDAO.insert(am);
        }

        System.out.println("[CREATE] Account '" + name + "' created by " + user.getDisplayName()
                + " at " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

        resp.sendRedirect(req.getContextPath() + "/accounts");
    }
}
