package com.account.servlet;

import com.account.dao.PlatformDAO;
import com.account.model.Platform;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 平台类型创建 Servlet。
 * <p>
 * 处理新增平台类型的请求。接收平台名称参数，创建新的 {@link Platform} 记录。
 * 平台名称不能为空，否则重定向至平台列表页并附带 error 参数。
 * </p>
 */
public class PlatformCreateServlet extends HttpServlet {
    private PlatformDAO platformDAO = new PlatformDAO();

    /**
     * 处理 POST 请求，创建新的平台类型。
     * <p>
     * 操作流程：
     * <ol>
     *   <li>校验 name 参数，为空则重定向至 /platforms?error=1</li>
     *   <li>创建 Platform 对象并设置名称</li>
     *   <li>调用 platformDAO.insert() 持久化</li>
     *   <li>重定向至平台列表页</li>
     * </ol>
     * </p>
     *
     * @param req  HttpServletRequest，包含参数 name（平台名称）
     * @param resp HttpServletResponse
     * @throws ServletException 转发异常
     * @throws IOException      输入/输出异常
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String name = req.getParameter("name");
        if (name == null || name.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/platforms?error=1");
            return;
        }
        Platform platform = new Platform();
        platform.setName(name.trim());
        platformDAO.insert(platform);
        resp.sendRedirect(req.getContextPath() + "/platforms");
    }
}
