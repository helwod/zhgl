package com.account.servlet;

import com.account.dao.PlatformDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 平台类型删除 Servlet。
 * <p>
 * 处理删除平台类型的请求。在删除前会检查该平台下是否有关联的账号，
 * 如果存在关联账号则拒绝删除并提示用户，以防止数据不一致。
 * </p>
 */
public class PlatformDeleteServlet extends HttpServlet {
    private PlatformDAO platformDAO = new PlatformDAO();

    /**
     * 处理 POST 请求，删除指定的平台类型。
     * <p>
     * 操作流程：
     * <ol>
     *   <li>接收 id 和 name 参数</li>
     *   <li>调用 platformDAO.countAccountsByPlatform(name) 检查关联账号数量</li>
     *   <li>如果存在关联账号（count > 0），拒绝删除并重定向至 /platforms?error=linked&count=数量</li>
     *   <li>如果无关联账号，调用 platformDAO.delete(id) 执行删除</li>
     *   <li>重定向至平台列表页</li>
     * </ol>
     * 此保护机制确保不会误删正在使用的平台类型，避免账号数据丢失。
     * </p>
     *
     * @param req  HttpServletRequest，包含参数 id（平台ID）和 name（平台名称）
     * @param resp HttpServletResponse
     * @throws ServletException 转发异常
     * @throws IOException      输入/输出异常
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        String name = req.getParameter("name");

        int accountCount = platformDAO.countAccountsByPlatform(name);
        if (accountCount > 0) {
            resp.sendRedirect(req.getContextPath() + "/platforms?error=linked&count=" + accountCount);
            return;
        }

        platformDAO.delete(id);
        resp.sendRedirect(req.getContextPath() + "/platforms");
    }
}
