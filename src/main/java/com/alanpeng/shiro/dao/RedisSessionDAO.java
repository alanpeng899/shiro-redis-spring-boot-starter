package com.alanpeng.shiro.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alanpeng.shiro.entity.SessionInMemory;
import com.alanpeng.shiro.util.ISimpleRedisUtil;

/**
 * @author pengyq
 * @time 创建时间：2019年5月29日
 * @description 类说明：重写shiro抽象类AbstractSessionDAO，实现redis读写session
 */
public class RedisSessionDAO extends AbstractSessionDAO {

	private static Logger logger = LoggerFactory.getLogger(RedisSessionDAO.class);

	private static final String DEFAULT_SESSION_KEY_PREFIX = "shiro:session:";
	private String keyPrefix = DEFAULT_SESSION_KEY_PREFIX;

	private static final long DEFAULT_SESSION_IN_MEMORY_TIMEOUT = 1000L;
	/**
	 * doReadSession be called about 10 times when login. Save Session in
	 * ThreadLocal to resolve this problem. sessionInMemoryTimeout is expiration of
	 * Session in ThreadLocal. The default value is 1000 milliseconds (1s). Most of
	 * time, you don't need to change it.
	 */
	private long sessionInMemoryTimeout = DEFAULT_SESSION_IN_MEMORY_TIMEOUT;

	private static final boolean DEFAULT_SESSION_IN_MEMORY_ENABLED = true;

	private boolean sessionInMemoryEnabled = DEFAULT_SESSION_IN_MEMORY_ENABLED;

	private ISimpleRedisUtil redisUtil;

	private static ThreadLocal<Map<Serializable, SessionInMemory>> sessionsInThread = new ThreadLocal<>();

	private static final int DEFAULT_CLEAN_SESSION_IN_MEMORY = 1;

	/**
	 * Clean up all session info from current thread after per request end. It will
	 * be effectived by setting "sessionInMemoryEnabled" is true,and
	 * "sessionInMemoryTimeout" will be invalid. It can set values are either 1 or
	 * 2,default is 1. if set to 2,it will check all session info of current thread
	 * whether is timeout(live time greater than sessionInMemoryTimeout) and
	 * removed.
	 */
	private int cleanSessionStrategy = DEFAULT_CLEAN_SESSION_IN_MEMORY;

	private static final String SESSION_NULL_ERROR = "session or session id is null";

	@Override
	public void update(Session session) {
		this.saveSession(session);
	}

	/**
	 * save session
	 * 
	 * @param session
	 * @throws UnknownSessionException
	 */
	private void saveSession(Session session) {
		if (session == null || session.getId() == null) {
			logger.error(SESSION_NULL_ERROR);
			throw new UnknownSessionException(SESSION_NULL_ERROR);
		}
		
		redisUtil.setForTimeMS(getRedisSessionKey(session.getId()), session, session.getTimeout());
		
		if (this.sessionInMemoryEnabled) {
			this.setSessionToThreadLocal(session.getId(), session);
		}
	}

	@Override
	public void delete(Session session) {
		if (session == null || session.getId() == null) {
			logger.error(SESSION_NULL_ERROR);
			return;
		}
		try {
			redisUtil.delete(getRedisSessionKey(session.getId()));
		} catch (Exception e) {
			logger.error("delete session error. session id={}", session.getId());
		}
	}

	@Override
	public Collection<Session> getActiveSessions() {
		Set<Session> sessions = new HashSet<>();
		try {
			Set<Object> keys = redisUtil.hashKeys(keyPrefix + "*");
			if (keys != null && !keys.isEmpty()) {
				for (Object key : keys) {
					Session s = (Session) redisUtil.get(key);
					sessions.add(s);
				}
			}
		} catch (Exception e) {
			logger.error("get active sessions error.");
		}
		return sessions;
	}

	@Override
	protected Serializable doCreate(Session session) {
		if (session == null) {
			logger.error("session is null");
			throw new UnknownSessionException("session is null");
		}
		Serializable sessionId = this.generateSessionId(session);
		this.assignSessionId(session, sessionId);
		this.saveSession(session);
		return sessionId;
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		if (sessionId == null) {
			logger.warn("session id is null");
			return null;
		}

		if (this.sessionInMemoryEnabled) {
			Session session = getSessionFromThreadLocal(sessionId);
			if (session != null) {
				return session;
			}
		}

		Session session = null;
		logger.debug("read session from redis");
		try {
			session = (Session) redisUtil.get(getRedisSessionKey(sessionId));
			if (this.sessionInMemoryEnabled) {
				setSessionToThreadLocal(sessionId, session);
			}
		} catch (Exception e) {
			logger.error("read session error. session id={}", sessionId);
		}
		return session;
	}

	private void setSessionToThreadLocal(Serializable sessionId, Session s) {
		Map<Serializable, SessionInMemory> sessionMap = sessionsInThread.get();
		if (sessionMap == null) {
			sessionMap = new HashMap<>();
			sessionsInThread.set(sessionMap);
		}

		if (cleanSessionStrategy != DEFAULT_CLEAN_SESSION_IN_MEMORY) {
			removeExpiredSessionInMemory(sessionMap);
		}

		SessionInMemory sessionInMemory = new SessionInMemory();
		sessionInMemory.setCreateTime(new Date());
		sessionInMemory.setSession(s);
		sessionMap.put(sessionId, sessionInMemory);
	}

	private void removeExpiredSessionInMemory(Map<Serializable, SessionInMemory> sessionMap) {
		Iterator<Serializable> it = sessionMap.keySet().iterator();
		while (it.hasNext()) {
			Serializable sessionId = it.next();
			SessionInMemory sessionInMemory = sessionMap.get(sessionId);
			if (sessionInMemory == null) {
				it.remove();
				continue;
			}
			long liveTime = getSessionInMemoryLiveTime(sessionInMemory);
			if (liveTime > sessionInMemoryTimeout) {
				it.remove();
			}
		}
	}

	private Session getSessionFromThreadLocal(Serializable sessionId) {

		if (sessionsInThread.get() == null) {
			return null;
		}

		Map<Serializable, SessionInMemory> sessionMap = sessionsInThread.get();
		SessionInMemory sessionInMemory = sessionMap.get(sessionId);
		if (sessionInMemory == null) {
			return null;
		}

		if (cleanSessionStrategy != DEFAULT_CLEAN_SESSION_IN_MEMORY) {
			long liveTime = getSessionInMemoryLiveTime(sessionInMemory);
			if (liveTime > sessionInMemoryTimeout) {
				sessionMap.remove(sessionId);
				return null;
			}
		}

		logger.debug("read session from memory");
		return sessionInMemory.getSession();
	}

	private long getSessionInMemoryLiveTime(SessionInMemory sessionInMemory) {
		Date now = new Date();
		return now.getTime() - sessionInMemory.getCreateTime().getTime();
	}

	private String getRedisSessionKey(Serializable sessionId) {
		return this.keyPrefix + sessionId;
	}

	public ISimpleRedisUtil getRedisUtil() {
		return redisUtil;
	}

	public void setRedisUtil(ISimpleRedisUtil redisUtil) {
		this.redisUtil = redisUtil;
	}

	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public long getSessionInMemoryTimeout() {
		return sessionInMemoryTimeout;
	}

	public void setSessionInMemoryTimeout(long sessionInMemoryTimeout) {
		this.sessionInMemoryTimeout = sessionInMemoryTimeout;
	}

	public boolean getSessionInMemoryEnabled() {
		return sessionInMemoryEnabled;
	}

	public void setSessionInMemoryEnabled(boolean sessionInMemoryEnabled) {
		this.sessionInMemoryEnabled = sessionInMemoryEnabled;
	}

	public static ThreadLocal<Map<Serializable, SessionInMemory>> getSessionsInThread() {
		return sessionsInThread;
	}

	public int getCleanSessionStrategy() {
		return cleanSessionStrategy;
	}

	public void setCleanSessionStrategy(int cleanSessionStrategy) {
		this.cleanSessionStrategy = cleanSessionStrategy;
	}

	public void removeSessionInMemory() {
		sessionsInThread.remove();
		logger.debug("已经清除线程【{}】中的session数据。。。。", Thread.currentThread().getName());
	}

}
