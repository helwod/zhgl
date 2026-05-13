package com.account.servlet;

import com.account.dao.UserDAO;
import com.account.model.User;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户列表 Servlet。
 * <p>
 * 查询并展示所有系统用户。该接口本身无权限限制，
 * 但通常管理员通过此页面管理用户，普通用户仅查看列表。
 * </p>
 */
public class UserListServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    /**
     * 处理 GET 请求，列出所有用户。
     * <p>
     * 调用 userDAO.findAll() 获取全部用户记录，
     * 存入 request 属性后转发至 /users/list.jsp 进行渲染。
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
        List<User> users = userDAO.findAll();
        req.setAttribute("users", users);
        req.getRequestDispatcher("/users/list.jsp").forward(req, resp);
    }
}
