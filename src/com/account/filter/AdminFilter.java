package com.account.filter;

import com.account.model.User;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 管理员权限检查过滤器。
 * <p>
 * 限制对后台管理功能的访问，仅允许具有超级管理员权限的用户通过。
 * 若用户未登录，将重定向到登录页面；若已登录但不是管理员，则返回 403 Forbidden 错误。
 * 通常在 web.xml 中映射到 {@code /admin/*} 路径。
 * </p>
 *
 * @author team
 * @version 1.0
 */
public class AdminFilter implements Filter {

    /**
     * 过滤器初始化方法。
     * <p>
     * 当前实现为空，可在需要时加载过滤器配置参数。
     * </p>
     *
     * @param filterConfig 过滤器配置对象
     */
    @Override
    public void init(FilterConfig filterConfig) {}

    /**
     * 执行权限检查过滤。
     * <p>
     * 验证流程：
     * <ol>
     *   <li>检查当前请求是否存在会话（session），若不存在则重定向到登录页</li>
     *   <li>从会话中获取用户对象，若用户为空或非管理员，返回 403 Forbidden</li>
     *   <li>通过验证后放行请求，进入目标资源</li>
     * </ol>
     * </p>
     *
     * @param request  Servlet 请求对象
     * @param response Servlet 响应对象
     * @param chain    Filter 链对象，用于将请求传递给下一个过滤器或目标资源
     * @throws IOException      输入/输出异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足，仅超级管理员可访问");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * 过滤器销毁方法。
     * <p>
     * 当前实现为空，可在需要时释放过滤器占用的资源。
     * </p>
     */
    @Override
    public void destroy() {}
}
