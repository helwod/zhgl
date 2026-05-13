package com.account.servlet;

import com.account.dao.AccountLogDAO;
import com.account.dao.ApplicationDAO;
import com.account.dao.LoginLogDAO;
import com.account.dao.PasswordLogDAO;
import com.account.model.AccountLog;
import com.account.model.Application;
import com.account.model.LoginLog;
import com.account.model.PasswordLog;
import com.account.model.User;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 统一日志查看器 Servlet。
 * <p>
 * 支持四种日志类型的查看与分页筛选：
 * <ul>
 *   <li><b>password</b>（默认）：密码查看日志，记录谁查看了哪些账号密码</li>
 *   <li><b>application</b>：申请审批日志，记录账号申请的审批流转</li>
 *   <li><b>login</b>：登录日志，仅管理员可查看</li>
 *   <li><b>account</b>：账号操作日志</li>
 * </ul>
 * 非管理员用户只能查看与自己相关的日志（通过 userId 限定查询范围）。
 * 每种日志支持关键词搜索、日期范围筛选、状态筛选及分页功能。
 * </p>
 */
public class LogListServlet extends HttpServlet {
    private PasswordLogDAO passwordLogDAO = new PasswordLogDAO();
    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private LoginLogDAO loginLogDAO = new LoginLogDAO();
    private AccountLogDAO accountLogDAO = new AccountLogDAO();
    private static final int PAGE_SIZE = 15;

    /**
     * 处理 GET 请求，加载并显示指定类型的日志列表。
     * <p>
     * 请求参数：
     * <ul>
     *   <li>type - 日志类型：password（默认）/ application / login / account</li>
     *   <li>keyword - 搜索关键词</li>
     *   <li>date_from / date_to - 日期范围筛选</li>
     *   <li>status - 状态筛选</li>
     *   <li>page - 当前页码，默认第 1 页</li>
     * </ul>
     * 权限说明：
     * <ul>
     *   <li>登录日志（login）仅限管理员查看，非管理员返回 403</li>
     *   <li>其他日志类型：管理员可查看所有记录，非管理员仅查看自己相关的记录</li>
     * </ul>
     * 所有查询结果包含分页信息（currentPage / totalPages / total）并转发至 /logs.jsp。
     * </p>
     *
     * @param req  HttpServletRequest
     * @param resp HttpServletResponse
     * @throws ServletException 转发异常
     * @throws IOException      输入/输出异常
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String type = req.getParameter("type");
        if (type == null) type = "password";

        String keyword = req.getParameter("keyword");
        String dateFrom = req.getParameter("date_from");
        String dateTo = req.getParameter("date_to");
        String statusFilter = req.getParameter("status");

        int page = 1;
        if (req.getParameter("page") != null) {
            try { page = Integer.parseInt(req.getParameter("page")); } catch (NumberFormatException e) {}
        }

        int userId = currentUser.isAdmin() ? 0 : currentUser.getId();

        if ("application".equals(type)) {
            int total = applicationDAO.searchCount(keyword, statusFilter, dateFrom, dateTo, userId);
            int totalPages = calcPages(total);
            if (page > totalPages) page = totalPages;
            List<Application> logs = applicationDAO.search(keyword, statusFilter, dateFrom, dateTo, page, PAGE_SIZE, userId);

            req.setAttribute("appLogs", logs);
            req.setAttribute("total", total);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("type", "application");

        } else if ("login".equals(type)) {
            if (!currentUser.isAdmin()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
                return;
            }
            int total = loginLogDAO.count(keyword, dateFrom, dateTo);
            int totalPages = calcPages(total);
            if (page > totalPages) page = totalPages;
            List<LoginLog> logs = loginLogDAO.search(keyword, dateFrom, dateTo, page, PAGE_SIZE);

            req.setAttribute("loginLogs", logs);
            req.setAttribute("total", total);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("type", "login");

        } else if ("account".equals(type)) {
            int total = accountLogDAO.count(statusFilter, keyword, dateFrom, dateTo, userId);
            int totalPages = calcPages(total);
            if (page > totalPages) page = totalPages;
            List<AccountLog> logs = accountLogDAO.search(statusFilter, keyword, dateFrom, dateTo, page, PAGE_SIZE, userId);

            req.setAttribute("accountLogs", logs);
            req.setAttribute("total", total);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("type", "account");

        } else {
            // Default: password view logs
            int total = passwordLogDAO.count(keyword, dateFrom, dateTo, userId);
            int totalPages = calcPages(total);
            if (page > totalPages) page = totalPages;
            List<PasswordLog> logs = passwordLogDAO.search(keyword, dateFrom, dateTo, page, PAGE_SIZE, userId);

            req.setAttribute("logs", logs);
            req.setAttribute("total", total);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("type", "password");
        }

        req.setAttribute("keyword", keyword);
        req.setAttribute("dateFrom", dateFrom);
        req.setAttribute("dateTo", dateTo);
        req.setAttribute("statusFilter", statusFilter);

        req.getRequestDispatcher("/logs.jsp").forward(req, resp);
    }

    /**
     * 计算总页数。
     * <p>根据总记录数和每页大小（{@value #PAGE_SIZE}）计算分页数，至少返回 1 页。</p>
     *
     * @param total 总记录数
     * @return 总页数，最小值为 1
     */
    private int calcPages(int total) {
        int p = (int) Math.ceil((double) total / PAGE_SIZE);
        return p < 1 ? 1 : p;
    }
}
