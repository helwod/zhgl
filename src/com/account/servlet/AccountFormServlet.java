package com.account.servlet;

import com.account.dao.AccountDAO;
import com.account.dao.AccountManagerDAO;
import com.account.dao.PlatformDAO;
import com.account.model.Account;
import com.account.model.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 账号创建/编辑表单展示Servlet。
 * <p>功能：</p>
 * <ul>
 *   <li>GET请求：展示账号创建或编辑表单</li>
 *   <li>编辑模式：根据id参数加载已有账号数据，填充到表单中</li>
 *   <li>权限校验：仅admin和manager角色可访问</li>
 *   <li>非admin用户编辑时，需验证其对目标账号的管理权限</li>
 *   <li>提供平台类型、子类型、状态的选项列表供表单使用</li>
 * </ul>
 */
public class AccountFormServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();
    private PlatformDAO platformDAO = new PlatformDAO();
    private AccountManagerDAO accountManagerDAO = new AccountManagerDAO();

    /**
     * 处理表单展示请求。
     * <ol>
     *   <li>权限校验：仅admin和manager角色可访问，否则返回403</li>
     *   <li>解析id参数：</li>
     *   <ul>
     *     <li>若无id参数 → 创建模式，account对象为null</li>
     *     <li>若有id参数 → 编辑模式，从数据库加载对应账号</li>
     *   </ul>
     *   <li>编辑模式下，非admin用户需通过 isManagerWithGroupExpansion 验证管理权限</li>
     *   <li>设置请求属性：account（编辑数据）、platformTypes、subTypes、statuses</li>
     *   <li>转发至 /accounts/form.jsp 渲染表单页面</li>
     * </ol>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || (!user.isAdmin() && !user.isManager())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
            return;
        }

        String editId = req.getParameter("id");
        Account account = null;

        if (editId != null && !editId.isEmpty()) {
            try {
                int id = Integer.parseInt(editId);
                account = accountDAO.findById(id);
                
                // Non-admin: check management permission for editing
                if (account != null && !user.isAdmin()) {
                    boolean canManage = accountManagerDAO.isManagerWithGroupExpansion(id, user.getId());
                    if (!canManage) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有此账号的管理权限");
                        return;
                    }
                }
            } catch (NumberFormatException e) {}
        }

        req.setAttribute("account", account);
        req.setAttribute("platformTypes", getPlatformNames());
        req.setAttribute("subTypes", Account.SUB_TYPES);
        req.setAttribute("statuses", new String[]{
            Account.STATUS_AVAILABLE, Account.STATUS_ASSIGNED, Account.STATUS_EXPIRED
        });

        req.getRequestDispatcher("/accounts/form.jsp").forward(req, resp);
    }

    /**
     * 获取所有平台类型的名称数组，供表单下拉框使用。
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
