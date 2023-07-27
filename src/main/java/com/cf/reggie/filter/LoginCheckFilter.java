package com.cf.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.cf.reggie.common.BaseContext;
import com.cf.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * decription: 过滤器
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @return void
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1.获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}", requestURI);

        // 定义放行的URI
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };

        // 2.判断是否是放行资源
        boolean check = check(urls, requestURI);

        // 3.无需处理则放行
        if(check){
            log.info("本次请求无需处理，{}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 4-1.判断是否登录，登录则放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，id为{}", request.getSession().getAttribute("employee"));
            Long id = (Long) request.getSession().getAttribute("employee");
            // 存储id到ThreadLocal中
            BaseContext.setId(id);
            filterChain.doFilter(request, response);
            return;
        }

        // 4-2.判断前台是否登录，登录则放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，id为{}", request.getSession().getAttribute("user"));
            Long userId = (Long) request.getSession().getAttribute("user");
            // 存储id到ThreadLocal中
            BaseContext.setId(userId);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户未登录");
        // 5.未登录，则通过输出流响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * decription: 匹配URI是否放行
     * @param urls
     * @param requestURI
     * @return boolean
     */
    private boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            if(PATH_MATCHER.match(url, requestURI))
                return true;
        }
        return false;
    }
}
