package com.account.servlet;

import com.account.dao.AccountDAO;
import com.account.model.Account;
import com.account.model.User;
import com.account.util.CryptoUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 * 账号数据CSV导入Servlet。
 * <p>功能：</p>
 * <ul>
 *   <li>接收上传的CSV文件，逐行解析并批量创建账号</li>
 *   <li>支持UTF-8编码，自动处理BOM头</li>
 *   <li>密码字段自动使用AES加密（CryptoUtil）后存储</li>
 *   <li>支持逗号转义（双引号包裹的字段内允许逗号）</li>
 *   <li>导入结果统计成功数和失败详情</li>
 * </ul>
 *
 * <p><b>CSV格式要求（10列）：</b></p>
 * <ul>
 *   <li>账号名称（必填）、密码、平台类型（必填）、运维子类型、所属项目</li>
 *   <li>部门、到期时间、状态、连接信息、备注</li>
 * </ul>
 *
 * <p><b>文件大小限制：</b>5MB（通过 @MultipartConfig 设置）</p>
 */
@MultipartConfig(maxFileSize = 1024 * 1024 * 5, fileSizeThreshold = 1024 * 1024)
public class AccountImportServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();

    /**
     * 处理CSV文件上传并批量导入账号。
     * <ol>
     *   <li>校验上传文件非空</li>
     *   <li>使用UTF-8编码逐行读取CSV</li>
     *   <li>跳过首行（表头）和空行</li>
     *   <li>自动去除UTF-8 BOM头（\ufeff）</li>
     *   <li>使用 parseCsvLine 解析每行，支持引号内逗号</li>
     *   <li>校验必填字段（账号名称、平台类型）</li>
     *   <li>使用 CryptoUtil.encrypt() 对密码进行AES加密</li>
     *   <li>组装Account对象并写入数据库，创建人为当前登录用户</li>
     *   <li>统计成功/失败数量，错误信息逐行记录</li>
     *   <li>导入结果存入session（importMsg），重定向到账号列表页展示</li>
     * </ol>
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");

        Part filePart = req.getPart("csvFile");
        if (filePart == null || filePart.getSize() == 0) {
            req.getSession().setAttribute("importMsg", "请选择文件上传");
            resp.sendRedirect(req.getContextPath() + "/accounts");
            return;
        }

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int lineNum = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(filePart.getInputStream(), "UTF-8"))) {

            String line;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (lineNum == 1) continue; // Skip header row

                line = line.trim();
                if (line.isEmpty()) continue;

                // Remove BOM if present
                if (line.charAt(0) == '\ufeff') line = line.substring(1);

                String[] cols = parseCsvLine(line);
                if (cols.length < 3) {
                    errors.add("第" + lineNum + "行: 列数不足");
                    continue;
                }

                try {
                    String name = cols[0].trim();
                    String password = cols[1].trim();
                    String platformType = cols[2].trim();
                    String subType = cols.length > 3 ? cols[3].trim() : "";
                    String project = cols.length > 4 ? cols[4].trim() : "";
                    String department = cols.length > 5 ? cols[5].trim() : "";
                    String expiryDate = cols.length > 6 ? cols[6].trim() : "";
                    String status = cols.length > 7 ? cols[7].trim() : Account.STATUS_AVAILABLE;
                    String loginUrl = cols.length > 8 ? cols[8].trim() : "";
                    String notes = cols.length > 9 ? cols[9].trim() : "";

                    if (name.isEmpty() || platformType.isEmpty()) {
                        errors.add("第" + lineNum + "行: 账号名称和平台类型不能为空");
                        continue;
                    }

                    CryptoUtil crypto = new CryptoUtil();
                    String[] encrypted = crypto.encrypt(password);

                    Account acc = new Account();
                    acc.setName(name);
                    acc.setPasswordEncrypted(encrypted[0]);
                    acc.setPasswordIv(encrypted[1]);
                    acc.setPasswordTag(encrypted[2]);
                    acc.setPlatformType(platformType);
                    acc.setSubType(subType);
                    acc.setProject(project);
                    acc.setDepartment(department);
                    acc.setExpiryDate(expiryDate.isEmpty() ? null : expiryDate);
                    acc.setStatus(status);
                    acc.setLoginUrl(loginUrl);
                    acc.setNotes(notes);
                    acc.setCreatedBy(user.getId());

                    accountDAO.insert(acc);
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + lineNum + "行: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("文件读取失败: " + e.getMessage());
        }

        StringBuilder msg = new StringBuilder();
        msg.append("导入完成：成功 ").append(successCount).append(" 条");
        if (!errors.isEmpty()) {
            msg.append("，失败 ").append(errors.size()).append(" 条<br>");
            for (String err : errors) {
                msg.append("&nbsp;&nbsp;❌ ").append(err).append("<br>");
            }
        }
        req.getSession().setAttribute("importMsg", msg.toString());
        resp.sendRedirect(req.getContextPath() + "/accounts");
    }

    /**
     * 解析CSV一行数据，支持双引号包裹的字段和转义双引号（""）。
     * <p>该解析器处理以下场景：</p>
     * <ul>
     *   <li>普通字段直接按逗号分割</li>
     *   <li>双引号包裹的字段内允许包含逗号</li>
     *   <li>连续两个双引号（""）转义为一个双引号</li>
     * </ul>
     *
     * @param line CSV行文本
     * @return 解析后的字段字符串数组
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(field.toString());
                    field = new StringBuilder();
                } else {
                    field.append(c);
                }
            }
        }
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }
}
