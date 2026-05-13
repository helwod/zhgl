package com.account.servlet;

import com.account.dao.AccountDAO;
import com.account.dao.PasswordLogDAO;
import com.account.model.Account;
import com.account.model.PasswordLog;
import com.account.model.User;
import com.account.util.CryptoUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 账号数据CSV导出Servlet。
 * <p>功能：</p>
 * <ul>
 *   <li>支持两种模式：数据导出 和 模板下载</li>
 *   <li>数据导出模式：导出所有账号信息，密码解密为明文后写入CSV</li>
 *   <li>模板下载模式：仅输出表头和一行示例数据</li>
 *   <li>记录导出操作日志（PasswordLog）用于安全审计</li>
 * </ul>
 *
 * <p><b>安全要点：</b></p>
 * <ul>
 *   <li>导出文件包含明文密码，属于敏感操作，每次导出均记录日志</li>
 *   <li>CSV添加UTF-8 BOM确保Excel正确识别编码</li>
 *   <li>密码解密失败时输出 "DECRYPT_FAILED" 标记</li>
 *   <li>CSV字段自动转义（逗号、引号、换行符）</li>
 * </ul>
 */
public class AccountExportServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();
    private PasswordLogDAO passwordLogDAO = new PasswordLogDAO();

    /**
     * 处理CSV导出请求。
     * <ol>
     *   <li>判断是否为模板模式（template=true）</li>
     *   <li>设置HTTP响应头为 text/csv，文件名含UTF-8编码</li>
     *   <li>写入UTF-8 BOM（\ufeff）确保Excel编码兼容</li>
     *   <li>写入CSV表头行</li>
     *   <li>数据导出模式下：</li>
     *   <ul>
     *     <li>记录导出日志（PasswordLog, actionType=export）</li>
     *     <li>遍历所有账号，使用 CryptoUtil.decrypt() 解密密码</li>
     *     <li>逐行写入CSV数据，每个字段经 csvEscape 处理</li>
     *   </ul>
     *   <li>模板模式下：仅写入一行示例数据</li>
     * </ol>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        boolean isTemplate = "true".equals(req.getParameter("template"));

        resp.setContentType("text/csv;charset=UTF-8");
        String filename = isTemplate ? "账号导入模板.csv" : "账号数据导出.csv";
        resp.setHeader("Content-Disposition",
            "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));

        PrintWriter out = resp.getWriter();
        // BOM for Excel UTF-8 encoding
        out.write('\ufeff');

        // CSV Header with project column
        out.println("账号名称,密码,平台类型,运维子类型,所属项目,部门,到期时间,状态,连接信息,备注");

        if (!isTemplate) {
            // Log export action before writing data
            HttpSession session = req.getSession(false);
            if (session != null) {
                User admin = (User) session.getAttribute("user");
                if (admin != null) {
                    PasswordLog log = new PasswordLog();
                    log.setAccountId(0);
                    log.setViewerId(admin.getId());
                    log.setViewerName(admin.getDisplayName());
                    log.setActionType("export");
                    passwordLogDAO.insert(log);
                }
            }

            List<Account> accounts = accountDAO.findAll();
            CryptoUtil crypto = new CryptoUtil();
            for (Account a : accounts) {
                StringBuilder line = new StringBuilder();
                line.append(csvEscape(a.getName())).append(",");
                // Decrypt password
                String decryptedPwd = "";
                if (a.getPasswordEncrypted() != null && !a.getPasswordEncrypted().isEmpty()
                        && a.getPasswordIv() != null && !a.getPasswordIv().isEmpty()
                        && a.getPasswordTag() != null && !a.getPasswordTag().isEmpty()) {
                    try {
                        decryptedPwd = crypto.decrypt(a.getPasswordEncrypted(), a.getPasswordIv(), a.getPasswordTag());
                    } catch (Exception e) {
                        decryptedPwd = "DECRYPT_FAILED";
                    }
                }
                line.append(csvEscape(decryptedPwd)).append(",");
                line.append(csvEscape(a.getPlatformType())).append(",");
                line.append(csvEscape(a.getSubType())).append(",");
                line.append(csvEscape(a.getProject())).append(",");
                line.append(csvEscape(a.getDepartment())).append(",");
                line.append(csvEscape(a.getExpiryDate())).append(",");
                line.append(csvEscape(a.getStatus())).append(",");
                line.append(csvEscape(a.getLoginUrl())).append(",");
                line.append(csvEscape(a.getNotes()));
                out.println(line.toString());
            }
        } else {
            // Template sample row (with project column)
            out.println("示例服务器,myPass123,运维服务器,服务器,电商平台,IT部,2026-12-31,可用,192.168.1.100:22,备注信息");
        }
    }

    /**
     * CSV字段转义处理。
     * <p>若字段值包含逗号、双引号或换行符，则用双引号包裹并将内部双引号转义为两个双引号。</p>
     *
     * @param value 原始字段值
     * @return 转义后的安全CSV字段值
     */
    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
