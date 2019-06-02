package io.github.alanpeng899.shiro.util;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author pengyq
 * @time 创建时间：2019年5月31日
 * @description 类说明：简易redis工具类接口实现
 */
public class SimpleRedisUtil implements ISimpleRedisUtil {

	private RedisTemplate<Object, Object> redisTemplate;

	public RedisTemplate<Object, Object> getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate<Object, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void set(Object key, Object value) {
		redisTemplate.opsForValue().set(key, value);
	}

	@Override
	public Object get(Object key) {
		return redisTemplate.opsForValue().get(key);
	}

	@Override
	public void setForTimeMS(Object key, Object value, long time) {
		redisTemplate.opsForValue().set(key, value, time, TimeUnit.MILLISECONDS);
	}

	@Override
	public void setForTimeMIN(Object key, Object value, long time) {
		redisTemplate.opsForValue().set(key, value, time, TimeUnit.MINUTES);
	}

	@Override
	public void setForTimeCustom(Object key, Object value, long time, TimeUnit type) {
		redisTemplate.opsForValue().set(key, value, time, type);
	}


	@Override
	public boolean expire(Object key, long time, TimeUnit type) {
		return redisTemplate.boundValueOps(key).expire(time, type);
	}

	@Override
	public boolean persist(Object key) {
		return redisTemplate.boundValueOps(key).persist();
	}

	@Override
	public Long getExpire(Object key) {
		return redisTemplate.boundValueOps(key).getExpire();
	}

	@Override
	public boolean delete(Object key) {
		redisTemplate.delete(key);
		return true;
	}
	
	@Override
	public Long delete(Object key, Object... hashKeys) {
		return redisTemplate.opsForHash().delete(key, hashKeys);
	}

	@Override
	public Set<Object> hashKeys(Object key) {
		return redisTemplate.opsForHash().keys(key);
	}

}
