package com.account.servlet;

import com.account.dao.AccountDAO;
import com.account.dao.AccountManagerDAO;
import com.account.dao.ApplicationDAO;
import com.account.dao.PasswordLogDAO;
import com.account.model.Account;
import com.account.model.PasswordLog;
import com.account.model.User;
import com.account.util.CryptoUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 密码查看 Servlet。
 * <p>
 * 处理密码查看请求：对加密密码进行解密并返回 JSON 结果。
 * 在返回密码前进行严格的权限检查，并记录密码查看审计日志，
 * 用于后续的安全审计和追溯。
 * </p>
 */
public class PasswordViewServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();
    private PasswordLogDAO passwordLogDAO = new PasswordLogDAO();
    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private AccountManagerDAO accountManagerDAO = new AccountManagerDAO();

    /**
     * 处理 POST 请求，查看指定账号的密码。
     * <p>
     * 操作流程：
     * <ol>
     *   <li><b>登录检查</b>：用户必须已登录，否则返回 JSON 错误</li>
     *   <li><b>参数校验</b>：解析 account_id 参数，无效则返回 JSON 错误</li>
     *   <li><b>账号存在性检查</b>：查询账号是否存在</li>
     *   <li><b>权限检查</b>：满足以下任一条件可查看密码：
     *     <ul>
     *       <li>管理员</li>
     *       <li>申请已获批且在有效期内的用户（{@link ApplicationDAO#hasApprovedApplicationValid}）</li>
     *       <li>该账号的管理者（{@link AccountManagerDAO#isManagerWithGroupExpansion}）</li>
     *     </ul>
     *   </li>
     *   <li><b>解密</b>：使用 {@link CryptoUtil} 对加密密码进行 AES-GCM 解密</li>
     *   <li><b>审计日志</b>：记录 {@link PasswordLog}，包含账号 ID、查看人 ID 和查看人名称</li>
     *   <li><b>返回</b>：返回包含解密后密码的 JSON 响应</li>
     * </ol>
     * </p>
     *
     * @param req  HttpServletRequest，包含参数 account_id
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

        if (user == null) {
            out.write("{\"success\":false,\"message\":\"未登录\"}");
            return;
        }

        int accountId;
        try {
            accountId = Integer.parseInt(req.getParameter("account_id"));
        } catch (NumberFormatException e) {
            out.write("{\"success\":false,\"message\":\"参数错误\"}");
            return;
        }

        Account acc = accountDAO.findById(accountId);
        if (acc == null) {
            out.write("{\"success\":false,\"message\":\"账号不存在\"}");
            return;
        }

        // Check permission: admin, approved applicant (with expiry check), or manager of this account
        boolean canView = user.isAdmin()
            || applicationDAO.hasApprovedApplicationValid(accountId, user.getId())
            || (user.isManager() && accountManagerDAO.isManagerWithGroupExpansion(accountId, user.getId()));
        if (!canView) {
            out.write("{\"success\":false,\"message\":\"权限不足\"}");
            return;
        }

        try {
            CryptoUtil crypto = new CryptoUtil();
            String decrypted = crypto.decrypt(
                acc.getPasswordEncrypted(),
                acc.getPasswordIv(),
                acc.getPasswordTag()
            );

            // Record audit log
            PasswordLog log = new PasswordLog();
            log.setAccountId(accountId);
            log.setViewerId(user.getId());
            log.setViewerName(user.getDisplayName());
            passwordLogDAO.insert(log);

            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date());
            System.out.println("[PASSWORD_VIEW] User '" + user.getDisplayName()
                + "' viewed password of account '" + acc.getName()
                + "' (ID: " + accountId + ") at " + timestamp);

            // Escape JSON special characters
            String escapedPassword = decrypted
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

            out.write("{\"success\":true,\"password\":\"" + escapedPassword + "\"}");
        } catch (Exception e) {
            out.write("{\"success\":false,\"message\":\"解密失败：" + e.getMessage() + "\"}");
        }
    }
}
