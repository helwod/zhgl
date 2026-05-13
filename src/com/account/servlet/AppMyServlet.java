package com.account.servlet;

import com.account.dao.ApplicationDAO;
import com.account.model.Application;
import com.account.model.User;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 我的申请列表Servlet。
 * <p>功能：显示当前登录用户提交的所有账号访问申请记录。</p>
 *
 * <p><b>展示内容：</b></p>
 * <ul>
 *   <li>申请的目标账号名称</li>
 *   <li>申请原因</li>
 *   <li>申请状态（待审批/已通过/已拒绝）</li>
 *   <li>审批结果及有效期信息</li>
 * </ul>
 */
public class AppMyServlet extends HttpServlet {
    private ApplicationDAO applicationDAO = new ApplicationDAO();

    /**
     * 处理查询个人申请列表的请求。
     * <ol>
     *   <li>从session获取当前登录用户</li>
     *   <li>查询该用户的所有申请记录（applicationDAO.findByApplicant）</li>
     *   <li>设置请求属性，转发至 /applications/my.jsp 渲染</li>
     * </ol>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");

        List<Application> apps = applicationDAO.findByApplicant(user.getId());
        req.setAttribute("applications", apps);

        req.getRequestDispatcher("/applications/my.jsp").forward(req, resp);
    }
}
