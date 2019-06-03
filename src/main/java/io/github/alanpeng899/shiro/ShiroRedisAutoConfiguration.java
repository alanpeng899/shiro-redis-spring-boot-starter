package io.github.alanpeng899.shiro;

import java.util.List;

import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.config.web.autoconfigure.ShiroWebAutoConfiguration;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import io.github.alanpeng899.shiro.dao.RedisSessionDAO;
import io.github.alanpeng899.shiro.filter.MyRemoveSessionFilter;
import io.github.alanpeng899.shiro.properties.ShiroRedisProperties;
import io.github.alanpeng899.shiro.serializer.ObjectSerializer;
import io.github.alanpeng899.shiro.util.ISimpleRedisUtil;
import io.github.alanpeng899.shiro.util.SimpleRedisUtil;

/**
 * @author pengyq<br>
 *  创建时间：2019年5月29日<br>
 *  类说明：自定义shiro-redis自动配置类
 */
@Configuration
@EnableConfigurationProperties({ ShiroRedisProperties.class })
@ConditionalOnProperty(name = "shiro-redis.enabled", havingValue = "true")
@AutoConfigureBefore({ ShiroWebAutoConfiguration.class })
public class ShiroRedisAutoConfiguration {

	@Autowired
	private ShiroRedisProperties shiroRedisProperties;

	@Bean
	@ConditionalOnMissingBean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();

		// 使用自定义object序列化
		ObjectSerializer objectSerializer = new ObjectSerializer();
		// value值的序列化采用objectSerializer
		template.setValueSerializer(objectSerializer);
		template.setHashValueSerializer(objectSerializer);
		// key的序列化采用StringRedisSerializer
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setConnectionFactory(redisConnectionFactory);
		template.afterPropertiesSet();
		return template;
	}

	@Bean
	@ConditionalOnClass(RedisTemplate.class)
	@ConditionalOnMissingBean
	public ISimpleRedisUtil iSimpleRedisUtil(RedisTemplate<Object, Object> redisTemplate) {
		SimpleRedisUtil redisUtil = new SimpleRedisUtil();
		redisUtil.setRedisTemplate(redisTemplate);
		return redisUtil;
	}

	@Bean
	@ConditionalOnClass(ISimpleRedisUtil.class)
	@ConditionalOnMissingBean
	public RedisSessionDAO redisSessionDAO(ISimpleRedisUtil iSimpleRedisUtil) {
		RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
		redisSessionDAO.setRedisUtil(iSimpleRedisUtil);
		if (!StringUtils.isEmpty(shiroRedisProperties.getSessionDao().getKeyPrefix())) {
			redisSessionDAO.setKeyPrefix(shiroRedisProperties.getSessionDao().getKeyPrefix());
		}
		if (shiroRedisProperties.getSessionDao().getSessionInMemoryTimeout() != null) {
			redisSessionDAO.setSessionInMemoryTimeout(shiroRedisProperties.getSessionDao().getSessionInMemoryTimeout());
		}
		if (shiroRedisProperties.getSessionDao().getCleanSessionStrategy() != null) {
			redisSessionDAO.setCleanSessionStrategy(shiroRedisProperties.getSessionDao().getCleanSessionStrategy());
		}
		if (shiroRedisProperties.getSessionDao().getSessionInMemoryEnabled() != null) {
			redisSessionDAO.setSessionInMemoryEnabled(shiroRedisProperties.getSessionDao().getSessionInMemoryEnabled());
		}
		return redisSessionDAO;
	}

	@Bean
	@ConditionalOnMissingBean
	public SimpleCookie cookie() {
		SimpleCookie cookie = new SimpleCookie();
		if (!StringUtils.isEmpty(shiroRedisProperties.getCookie().getName())) {
			cookie.setName(shiroRedisProperties.getCookie().getName());
		}
		return cookie;
	}

	@Bean
	@ConditionalOnClass({ RedisSessionDAO.class })
	@ConditionalOnMissingBean
	public DefaultWebSessionManager sessionManager(RedisSessionDAO redisSessionDAO) {
		DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
		// 设置session超时
		if (shiroRedisProperties.getGlobalSessionTimeout() != null) {
			sessionManager.setGlobalSessionTimeout(shiroRedisProperties.getGlobalSessionTimeout());
		}
		// 删除无效session
		sessionManager.setDeleteInvalidSessions(true);
		// 设置JSESSIONID
		sessionManager.setSessionIdCookie(cookie());
		// 设置sessionDAO
		sessionManager.setSessionDAO(redisSessionDAO);
		return sessionManager;
	}

	@Bean
	@ConditionalOnClass({ Realm.class, DefaultWebSessionManager.class })
	@ConditionalOnMissingBean
	public SessionsSecurityManager securityManager(List<Realm> realms, DefaultWebSessionManager sessionManager) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(realms);
		// 设置sessionManager
		securityManager.setSessionManager(sessionManager);
		return securityManager;
	}

	@Bean
	@ConditionalOnClass(RedisSessionDAO.class)
	@ConditionalOnProperty(name = "shiro-redis.session-dao.clean-session-strategy", havingValue = "1")
	@ConditionalOnMissingBean
	public MyRemoveSessionFilter myRemoveSessionFilter() {
		return new MyRemoveSessionFilter();
	}

}
