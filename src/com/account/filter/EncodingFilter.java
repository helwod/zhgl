package com.account.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * 字符编码过滤器。
 * <p>
 * 对所有进入应用的请求和响应设置统一的 UTF-8 字符编码，
 * 解决中文等非 ASCII 字符的乱码问题。
 * 通过 {@code @WebFilter("/*")} 注解自动注册，拦截所有 URL 路径。
 * 编码格式可通过 filter 初始化参数 {@code encoding} 进行配置，默认值为 UTF-8。
 * </p>
 *
 * @author team
 * @version 1.0
 */
@WebFilter("/*")
public class EncodingFilter implements Filter {

    /** 默认字符编码，UTF-8 */
    private String encoding = "UTF-8";

    /**
     * 过滤器初始化方法。
     * <p>
     * 从 web.xml 的 filter 配置中读取 {@code encoding} 初始化参数。
     * 若未配置或为空，则保持默认的 UTF-8 编码。
     * </p>
     *
     * @param filterConfig 过滤器配置对象
     * @throws ServletException 初始化异常
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String configEncoding = filterConfig.getInitParameter("encoding");
        if (configEncoding != null && !configEncoding.isEmpty()) {
            this.encoding = configEncoding;
        }
    }

    /**
     * 执行编码设置过滤。
     * <p>
     * 在请求到达目标资源前，为请求和响应分别设置字符编码。
     * 确保 Controller 和 JSP 页面在处理请求参数和输出内容时使用正确的编码格式，
     * 避免中文乱码问题。
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
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);
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
