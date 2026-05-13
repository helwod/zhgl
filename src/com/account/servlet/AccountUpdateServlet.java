package com.account.servlet;

import com.account.dao.AccountDAO;
import com.account.dao.AccountLogDAO;
import com.account.dao.AccountManagerDAO;
import com.account.dao.PasswordHistoryDAO;
import com.account.model.Account;
import com.account.model.AccountLog;
import com.account.model.PasswordHistory;
import com.account.model.User;
import com.account.util.CryptoUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 更新账号信息Servlet。
 * <p>功能：</p>
 * <ul>
 *   <li>接收表单提交的账号修改信息</li>
 *   <li>对每个字段进行旧值/新值比对，记录字段级变更日志</li>
 *   <li>支持密码修改：将旧密码存入密码历史表，新密码重新AES加密</li>
 *   <li>记录更新操作日志</li>
 * </ul>
 *
 * <p><b>权限要求：</b>仅admin和manager角色可操作，非admin需验证对目标账号的管理权限。</p>
 * <p><b>安全要点：</b>密码变更时旧密码被自动记录到历史表，支持密码溯源审计。</p>
 */
public class AccountUpdateServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();
    private AccountLogDAO accountLogDAO = new AccountLogDAO();
    private PasswordHistoryDAO passwordHistoryDAO = new PasswordHistoryDAO();
    private AccountManagerDAO accountManagerDAO = new AccountManagerDAO();

    /**
     * 处理更新账号的POST请求。
     * <ol>
     *   <li>权限校验：仅admin和manager可操作，非admin需验证对账号的管理权限</li>
     *   <li>加载现有账号数据，若不存在则重定向</li>
     *   <li>对每个字段逐一比对旧值和新值，记录字段级变更日志（writeFieldLog）</li>
     *   <li>更新Account对象的各字段值</li>
     *   <li>若提供了新密码：</li>
     *   <ul>
     *     <li>将当前加密密码、IV、认证标签保存到 PasswordHistory 表</li>
     *     <li>使用 CryptoUtil 对新密码重新AES加密</li>
     *     <li>记录密码变更日志</li>
     *   </ul>
     *   <li>调用 accountDAO.update() 持久化修改</li>
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

        Account acc = accountDAO.findById(id);
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/accounts");
            return;
        }

        // Compare and log changes field by field
        writeFieldLog(id, user, "名称", acc.getName(), name);
        writeFieldLog(id, user, "平台类型", acc.getPlatformType(), platformType);
        writeFieldLog(id, user, "运维子类型", acc.getSubType(), subType);
        writeFieldLog(id, user, "所属项目", acc.getProject(), project);
        writeFieldLog(id, user, "部门", acc.getDepartment(), department);
        writeFieldLog(id, user, "到期时间", acc.getExpiryDate(), expiryDate);
        writeFieldLog(id, user, "状态", acc.getStatus(), status);
        writeFieldLog(id, user, "连接信息", acc.getLoginUrl(), loginUrl);
        writeFieldLog(id, user, "备注", acc.getNotes(), notes);

        acc.setName(name.trim());
        acc.setPlatformType(platformType);
        acc.setSubType(subType != null ? subType : "");
        acc.setProject(project != null ? project : "");
        acc.setDepartment(department != null ? department : "");
        acc.setExpiryDate(expiryDate);
        acc.setStatus(status != null ? status : Account.STATUS_AVAILABLE);
        acc.setLoginUrl(loginUrl != null ? loginUrl.trim() : "");
        acc.setNotes(notes != null ? notes : "");

        // If password is provided, re-encrypt and save old password to history
        if (password != null && !password.trim().isEmpty()) {
            // Save old password to history before changing
            PasswordHistory ph = new PasswordHistory();
            ph.setAccountId(id);
            ph.setPasswordEncrypted(acc.getPasswordEncrypted());
            ph.setPasswordIv(acc.getPasswordIv());
            ph.setPasswordTag(acc.getPasswordTag());
            ph.setChangedBy(user.getId());
            ph.setChangedName(user.getDisplayName());
            passwordHistoryDAO.insert(ph);

            CryptoUtil crypto = new CryptoUtil();
            String[] encrypted = crypto.encrypt(password);
            acc.setPasswordEncrypted(encrypted[0]);
            acc.setPasswordIv(encrypted[1]);
            acc.setPasswordTag(encrypted[2]);

            writeFieldLog(id, user, "密码", "[已加密]", "[已修改]");
        }

        accountDAO.update(acc);

        System.out.println("[UPDATE] Account '" + name + "' updated by " + user.getDisplayName()
                + " at " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

        resp.sendRedirect(req.getContextPath() + "/accounts");
    }

    /**
     * 比较字段旧值和新值，若有变化则写入操作日志。
     * <p>该方法在更新账号时对每个字段逐一调用，实现字段级变更审计。</p>
     *
     * @param accountId 账号ID
     * @param user      操作人（当前登录用户）
     * @param fieldName 中文字段名称（如"名称"、"密码"等）
     * @param oldValue  旧值
     * @param newValue  新值
     */
    private void writeFieldLog(int accountId, User user, String fieldName,
                                String oldValue, String newValue) {
        String ov = oldValue != null ? oldValue : "";
        String nv = newValue != null ? newValue : "";
        if (!ov.equals(nv)) {
            AccountLog log = new AccountLog();
            log.setAccountId(accountId);
            log.setActionType("update");
            log.setFieldName(fieldName);
            log.setOldValue(ov.isEmpty() ? "(空)" : ov);
            log.setNewValue(nv.isEmpty() ? "(空)" : nv);
            log.setOperatorId(user.getId());
            log.setOperatorName(user.getDisplayName());
            accountLogDAO.insert(log);
        }
    }
}
