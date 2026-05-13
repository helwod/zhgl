package com.account.servlet;

import com.account.dao.PlatformDAO;
import com.account.model.Platform;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 平台类型列表 Servlet。
 * <p>
 * 查询并展示所有平台类型。该接口无权限限制，
 * 所有已登录用户均可查看平台列表。
 * </p>
 */
public class PlatformListServlet extends HttpServlet {
    private PlatformDAO platformDAO = new PlatformDAO();

    /**
     * 处理 GET 请求，列出所有平台类型。
     * <p>
     * 调用 platformDAO.findAll() 获取全部平台记录，
     * 存入 request 属性后转发至 /platforms/list.jsp 进行渲染。
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
        List<Platform> platforms = platformDAO.findAll();
        req.setAttribute("platforms", platforms);
        req.getRequestDispatcher("/platforms/list.jsp").forward(req, resp);
    }
}
