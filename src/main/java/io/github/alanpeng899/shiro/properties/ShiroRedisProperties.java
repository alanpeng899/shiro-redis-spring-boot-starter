package io.github.alanpeng899.shiro.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
* @author pengyq
* @time 创建时间：2019年5月30日 
* @description 类说明：shiro-redis全局配置属性
*/
@ConfigurationProperties(prefix = "shiro-redis")
public class ShiroRedisProperties {
	
	private Boolean enabled;
	
	private Long globalSessionTimeout;
	
	private RedisSessionDAOProperties sessionDao = new RedisSessionDAOProperties();
	
	private CookieProperties cookie = new CookieProperties();

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Long getGlobalSessionTimeout() {
		return globalSessionTimeout;
	}

	public void setGlobalSessionTimeout(Long globalSessionTimeout) {
		this.globalSessionTimeout = globalSessionTimeout;
	}

	public RedisSessionDAOProperties getSessionDao() {
		return sessionDao;
	}

	public void setSessionDao(RedisSessionDAOProperties sessionDao) {
		this.sessionDao = sessionDao;
	}

	public CookieProperties getCookie() {
		return cookie;
	}

	public void setCookie(CookieProperties cookie) {
		this.cookie = cookie;
	}
	
	public class RedisSessionDAOProperties {
		
	    private String keyPrefix;
	    
	    private Long sessionInMemoryTimeout;
	    
	    private Boolean sessionInMemoryEnabled;
	    
	    private Integer cleanSessionStrategy;
	    
		public String getKeyPrefix() {
			return keyPrefix;
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}

		public Long getSessionInMemoryTimeout() {
			return sessionInMemoryTimeout;
		}

		public void setSessionInMemoryTimeout(Long sessionInMemoryTimeout) {
			this.sessionInMemoryTimeout = sessionInMemoryTimeout;
		}

		public Boolean getSessionInMemoryEnabled() {
			return sessionInMemoryEnabled;
		}

		public void setSessionInMemoryEnabled(Boolean sessionInMemoryEnabled) {
			this.sessionInMemoryEnabled = sessionInMemoryEnabled;
		}

		public Integer getCleanSessionStrategy() {
			return cleanSessionStrategy;
		}

		public void setCleanSessionStrategy(Integer cleanSessionStrategy) {
			this.cleanSessionStrategy = cleanSessionStrategy;
		}

	}
	
	public class CookieProperties {
		
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
}
