package io.github.alanpeng899.shiro.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import io.github.alanpeng899.shiro.dao.RedisSessionDAO;

/**
 * @author pengyq
 * @time 创建时间：2019年5月30日
 * @description 类说明：自定义ThreadLocal session删除过滤器，每次请求结束删除
 */
@Order(1)
public class MyRemoveSessionFilter implements Filter {

	@Autowired
	private RedisSessionDAO redisSessionDAO;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} finally {
			if (redisSessionDAO.getSessionInMemoryEnabled() && redisSessionDAO.getCleanSessionStrategy() == 1) {
				redisSessionDAO.removeSessionInMemory();
			}
		}
	}

	@Override
	public void destroy() {
		
	}

}
