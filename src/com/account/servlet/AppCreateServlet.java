package com.account.servlet;

import com.account.dao.ApplicationDAO;
import com.account.model.Application;
import com.account.model.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 创建账号访问申请Servlet。
 * <p>功能：</p>
 * <ul>
 *   <li>允许用户提交申请以获取某个账号的访问权限</li>
 *   <li>申请需包含申请原因和有效天数</li>
 *   <li>默认有效期7天</li>
 *   <li>申请提交后需由账号管理员审批</li>
 * </ul>
 *
 * <p><b>业务流程：</b>用户提交申请 → 管理员审批 → 获批后用户可查看密码</p>
 */
public class AppCreateServlet extends HttpServlet {
    private ApplicationDAO applicationDAO = new ApplicationDAO();

    /**
     * 处理创建访问申请的POST请求。
     * <ol>
     *   <li>获取当前登录用户、目标账号ID、申请原因、有效天数</li>
     *   <li>有效天数默认7天，若参数非法则忽略（使用默认值）</li>
     *   <li>组装Application对象：设置账号ID、申请人ID、原因、有效期</li>
     *   <li>写入数据库</li>
     *   <li>重定向到账号详情页</li>
     * </ol>
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");

        int accountId = Integer.parseInt(req.getParameter("account_id"));
        String reason = req.getParameter("reason");
        int validDays = 7;
        if (req.getParameter("valid_days") != null) {
            try { validDays = Integer.parseInt(req.getParameter("valid_days")); } catch (NumberFormatException ignored) {}
        }

        Application app = new Application();
        app.setAccountId(accountId);
        app.setApplicantId(user.getId());
        app.setReason(reason != null ? reason.trim() : "");
        app.setValidDays(validDays);

        applicationDAO.insert(app);

        System.out.println("[APPLICATION] User '" + user.getDisplayName()
                + "' applied for account ID " + accountId
                + " at " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

        resp.sendRedirect(req.getContextPath() + "/accounts/detail?id=" + accountId);
    }
}
