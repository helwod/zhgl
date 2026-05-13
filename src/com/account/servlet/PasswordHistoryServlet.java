package com.account.servlet;

import com.account.dao.PasswordHistoryDAO;
import com.account.model.PasswordHistory;
import com.account.model.User;
import com.account.util.CryptoUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 密码历史记录 Servlet（仅管理员可用）。
 * <p>
 * 查询指定账号的历史密码记录，将加密的密码进行解密后以 JSON 格式返回。
 * 此接口用于审计场景，允许管理员追溯某个账号的密码变更历史。
 * 每次密码变更时都会记录一条 {@link PasswordHistory}，包含加密后的密码、变更人和变更时间。
 * </p>
 */
public class PasswordHistoryServlet extends HttpServlet {
    private PasswordHistoryDAO passwordHistoryDAO = new PasswordHistoryDAO();

    /**
     * 处理 POST 请求，查询并返回账号的历史密码列表。
     * <p>
     * 操作流程：
     * <ol>
     *   <li><b>权限检查</b>：仅管理员可调用，非管理员返回 JSON 错误</li>
     *   <li><b>参数校验</b>：解析 account_id 参数</li>
     *   <li><b>查询历史</b>：通过 {@link PasswordHistoryDAO#findByAccountId} 获取所有历史记录</li>
     *   <li><b>解密</b>：逐条使用 {@link CryptoUtil} 解密历史密码（AES-GCM）</li>
     *   <li><b>JSON 构建</b>：组装包含 id、password（已解密）、changedBy、changedAt 的 JSON 数组</li>
     * </ol>
     * 响应格式示例：{"success":true,"data":[{"id":1,"password":"xxx","changedBy":"admin","changedAt":"2024-01-01 12:00:00"}]}
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
        if (user == null || !user.isAdmin()) {
            out.write("{\"success\":false,\"message\":\"权限不足\"}");
            return;
        }

        int accountId;
        try {
            accountId = Integer.parseInt(req.getParameter("account_id"));
        } catch (NumberFormatException e) {
            out.write("{\"success\":false,\"message\":\"参数错误\"}");
            return;
        }

        try {
            List<PasswordHistory> history = passwordHistoryDAO.findByAccountId(accountId);
            CryptoUtil crypto = new CryptoUtil();

            StringBuilder json = new StringBuilder("{\"success\":true,\"data\":[");
            boolean first = true;

            for (PasswordHistory ph : history) {
                if (!first) json.append(",");
                first = false;

                String decrypted = crypto.decrypt(
                    ph.getPasswordEncrypted(), ph.getPasswordIv(), ph.getPasswordTag()
                );
                String escapedPwd = decrypted
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
                String escapedName = ph.getChangedName() != null ?
                    ph.getChangedName().replace("\\", "\\\\").replace("\"", "\\\"") : "";

                json.append("{");
                json.append("\"id\":").append(ph.getId()).append(",");
                json.append("\"password\":\"").append(escapedPwd).append("\",");
                json.append("\"changedBy\":\"").append(escapedName).append("\",");
                json.append("\"changedAt\":\"").append(ph.getChangedAt() != null ? ph.getChangedAt() : "").append("\"");
                json.append("}");
            }

            json.append("]}");
            out.write(json.toString());
        } catch (Exception e) {
            out.write("{\"success\":false,\"message\":\"查询失败：" + e.getMessage() + "\"}");
        }
    }
}
