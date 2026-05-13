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
 * 登录认证过滤器。
 * <p>
 * 拦截大部分 URL 请求，检查用户是否已登录。
 * 对于放行路径（如登录页、初始化页、静态资源）直接通过，不做登录检查。
 * 若用户未登录，重定向到登录页面。
 * 通常映射到 {@code /*}，但通过白名单排除无需认证的路径。
 * </p>
 *
 * @author team
 * @version 1.0
 */
public class AuthFilter implements Filter {

    /** 无需登录认证的白名单路径前缀 */
    private String[] excludedPaths = {"/login", "/init", "/css/"};

    /**
     * 过滤器初始化方法。
     * <p>
     * 当前实现为空，可在需要时从 web.xml 加载排除路径配置。
     * </p>
     *
     * @param filterConfig 过滤器配置对象
     */
    @Override
    public void init(FilterConfig filterConfig) {}

    /**
     * 执行登录认证过滤。
     * <p>
     * 处理流程：
     * <ol>
     *   <li>获取请求的 Servlet 路径</li>
     *   <li>判断路径是否在白名单中（如 /login、/init、/css/），若是则直接放行</li>
     *   <li>从会话中获取用户对象，检查登录状态</li>
     *   <li>若未登录则重定向到登录页，否则放行请求</li>
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
        String path = req.getServletPath();

        // Check if path is excluded
        for (String excluded : excludedPaths) {
            if (path.startsWith(excluded)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // Check login status
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
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
