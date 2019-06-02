package io.github.alanpeng899.shiro.util;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author pengyq
 * @time 创建时间：2019年5月29日
 * @description 类说明：简易redis工具类接口
 */
public interface ISimpleRedisUtil {

	/**
	 * 设置 Object 类型 key-value
	 * 
	 * @param key
	 * @param value
	 */
	public void set(Object key, Object value);

	/**
	 * 获取 Object 类型 key-value
	 * 
	 * @param key
	 * @return
	 */
	public Object get(Object key);

	/**
	 * 设置 Object 类型 key-value 并添加过期时间 (毫秒单位)
	 * 
	 * @param key
	 * @param value
	 * @param time  过期时间,毫秒单位
	 */
	public void setForTimeMS(Object key, Object value, long time);

	/**
	 * 设置 Object 类型 key-value 并添加过期时间 (分钟单位)
	 * 
	 * @param key
	 * @param value
	 * @param time  过期时间,分钟单位
	 */
	public void setForTimeMIN(Object key, Object value, long time);

	/**
	 * 设置 Object 类型 key-value 并添加过期时间
	 * 
	 * @param key
	 * @param value
	 * @param time  过期时间
	 * @param type	时间单位
	 */
	public void setForTimeCustom(Object key, Object value, long time, TimeUnit type);

	/**
	 * 给一个指定的 key 值附加过期时间
	 * 
	 * @param key
	 * @param time	过期时间
	 * @param type	时间单位
	 * @return
	 */
	public boolean expire(Object key, long time, TimeUnit type);

	/**
	 * 移除指定key 的过期时间
	 * 
	 * @param key
	 * @return
	 */
	public boolean persist(Object key);

	/**
	 * 获取指定key 的过期时间
	 * 
	 * @param key
	 * @return
	 */
	public Long getExpire(Object key);

	/**
	 * 删除 key-value
	 * 
	 * @param key
	 * @return
	 */
	public boolean delete(Object key);
	
	/**
	 * 删除指定 hash 的 HashKey
	 * 
	 * @param key
	 * @param hashKeys
	 * @return 删除成功的 数量
	 */
	public Long delete(Object key, Object... hashKeys);

	// hash操作

	/**
	 * 获取 key 下的 所有 hashkey 字段名
	 * 
	 * @param key
	 * @return
	 */
	public Set<Object> hashKeys(Object key);

}
