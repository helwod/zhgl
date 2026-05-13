package com.account.servlet;

import com.account.dao.AccountDAO;
import com.account.dao.AccountManagerDAO;
import com.account.dao.ApplicationDAO;
import com.account.dao.PlatformDAO;
import com.account.model.Account;
import com.account.model.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 账号列表展示Servlet，支持分页、筛选和角色权限控制。
 * <p>功能：</p>
 * <ul>
 *   <li>按平台类型、状态、关键词搜索筛选账号</li>
 *   <li>分页展示，每页10条数据</li>
 *   <li>根据用户角色（admin/manager/user）控制可见的账号范围</li>
 *   <li>对普通用户标记其已提交申请但待审批的账号（禁用申请按钮）</li>
 *   <li>加载所有平台类型名称供筛选下拉框使用</li>
 * </ul>
 *
 * <p><b>权限控制：</b></p>
 * <ul>
 *   <li><b>admin：</b>查看所有账号</li>
 *   <li><b>manager：</b>仅查看由其管辖的账号；若无管辖账号则跳转到仪表盘</li>
 *   <li><b>user（通过组/个人分配为管理员）：</b>与manager同级别</li>
 *   <li><b>user（普通）：</b>仅查看已获批申请对应的账号</li>
 * </ul>
 */
public class AccountListServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();
    private AccountManagerDAO accountManagerDAO = new AccountManagerDAO();
    private PlatformDAO platformDAO = new PlatformDAO();
    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private static final int PAGE_SIZE = 10;

    /**
     * 处理账号列表查询请求。
     * <ol>
     *   <li>从session获取当前登录用户</li>
     *   <li>解析请求参数：platform_type（平台类型）、status（状态）、keyword（关键词）、page（页码）</li>
     *   <li>根据用户角色执行不同的数据查询逻辑：</li>
     *   <ul>
     *     <li>admin：直接查询全部账号（accountDAO.findPage）</li>
     *     <li>manager：查询管辖范围内的账号（accountDAO.findPageManaged）</li>
     *     <li>user（有管理权限）：同manager处理逻辑</li>
     *     <li>user（普通）：通过已获批申请ID列表查询对应账号</li>
     *   </ul>
     *   <li>计算总页数，处理页码越界</li>
     *   <li>查询当前用户待审批的账号ID列表，用于前端禁用申请按钮</li>
     *   <li>设置请求属性，转发至 /accounts/list.jsp 渲染</li>
     * </ol>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        String platformType = req.getParameter("platform_type");
        String status = req.getParameter("status");
        String keyword = req.getParameter("keyword");
        int page = 1;
        if (req.getParameter("page") != null) {
            try { page = Integer.parseInt(req.getParameter("page")); } catch (NumberFormatException e) {}
        }

        List<Account> accounts;
        int total;

        if (currentUser != null && currentUser.isAdmin()) {
            // Admin sees all accounts
            total = accountDAO.count(platformType, status, keyword);
            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
            if (totalPages < 1) totalPages = 1;
            if (page > totalPages) page = totalPages;
            accounts = accountDAO.findPage(platformType, status, keyword, page, PAGE_SIZE);
        } else if (currentUser != null && currentUser.isManager()) {
            // Manager only sees managed accounts
            total = accountDAO.countManaged(currentUser.getId(), platformType, status, keyword);
            // If manager has no assigned accounts, redirect to dashboard
            if (total == 0) {
                resp.sendRedirect(req.getContextPath() + "/dashboard");
                return;
            }
            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
            if (totalPages < 1) totalPages = 1;
            if (page > totalPages) page = totalPages;
            accounts = accountDAO.findPageManaged(currentUser.getId(), platformType, status, keyword, page, PAGE_SIZE);
        } else if (currentUser != null) {
            // Check if user has management privileges via group/individual assignment
            List<Integer> managedIds = accountManagerDAO.findManagedAccountIdsWithGroups(currentUser.getId());
            if (!managedIds.isEmpty()) {
                // User has management privileges via group assignment, use managed path
                total = accountDAO.countManaged(currentUser.getId(), platformType, status, keyword);
                if (total == 0) {
                    resp.sendRedirect(req.getContextPath() + "/dashboard");
                    return;
                }
                int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
                if (totalPages < 1) totalPages = 1;
                if (page > totalPages) page = totalPages;
                accounts = accountDAO.findPageManaged(currentUser.getId(), platformType, status, keyword, page, PAGE_SIZE);
            } else {
                // Regular users only see accounts they have approved applications for
                List<Integer> approvedIds = applicationDAO.findApprovedAccountIdsByApplicant(currentUser.getId());
                total = approvedIds.size();
                int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
                if (totalPages < 1) totalPages = 1;
                if (page > totalPages) page = totalPages;
                accounts = new ArrayList<>();
                if (!approvedIds.isEmpty()) {
                    accounts = accountDAO.findByIds(approvedIds, platformType, status, keyword, page, PAGE_SIZE);
                }
            }
        } else {
            accounts = new ArrayList<>();
            total = 0;
        }

        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;

        // Check which accounts the current user has pending applications for
        Set<Integer> pendingAccountIds = new HashSet<>();
        if (currentUser != null && !currentUser.isAtLeastManager()) {
            List<Integer> ids = applicationDAO.findPendingAccountIdsByApplicant(currentUser.getId());
            pendingAccountIds.addAll(ids);
        }

        req.setAttribute("accounts", accounts);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("total", total);
        req.setAttribute("platformType", platformType);
        req.setAttribute("status", status);
        req.setAttribute("keyword", keyword);
        req.setAttribute("platformTypes", getPlatformNames());
        req.setAttribute("pendingAccountIds", pendingAccountIds);

        req.getRequestDispatcher("/accounts/list.jsp").forward(req, resp);
    }

    /**
     * 获取所有平台类型的名称数组，供前端筛选下拉框使用。
     *
     * @return 平台名称字符串数组
     */
    private String[] getPlatformNames() {
        java.util.List<com.account.model.Platform> list = platformDAO.findAll();
        String[] names = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            names[i] = list.get(i).getName();
        }
        return names;
    }
}
