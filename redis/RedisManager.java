/**
 *  Copyright (c)  2016-2020 CCS, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of CCS,
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with CCS.
 */
package com.ccs.core.util.redis;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ccs.ssmis.bs.dto.UserInfoDto;
import com.ccs.ssmis.bs.pojo.BsSysDic;
import com.ccs.ssmis.bs.pojo.BsSysParam;
import com.ccs.ssmis.bs.pojo.BsSysUser;
import com.google.gson.Gson;

/**
 * Manage life cycle of redis pool
 *
 * @author sfli
 * @date Aug 25, 2016
 */
public class RedisManager {
	private static final Log LOG = LogFactory.getLog(RedisManager.class);

	private static Redis redisCache = null;

	/**
	 * Constructor with redis configure file.
	 *
	 * @param conf
	 *            file of redis
	 */
	public RedisManager() {
		/*Preconditions.checkArgument(null != conf,
				"Redis conf object cannot be null.");*/
		redisCache = new Redis();
		this.start();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.dropwizard.lifecycle.Managed#start()
	 */
	public void start() {
		if (null != redisCache) {
			redisCache.initialize();
			LOG.info("Succsessfully initialize redis pool.");
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.dropwizard.lifecycle.Managed#stop()
	 */
	public void stop() throws Exception {
		if (null != redisCache) {
			redisCache.destroy();
			LOG.info("Succsessfully destory redis pool.");
		}
	}

	/**
	 * Get redis cache instance
	 *
	 * @return single instance of redis cache
	 * @author sfli
	 * @date Aug 25, 2016
	 */
	public static Redis getRedis() {
		if(redisCache==null){
			redisCache = new Redis();
			redisCache.initialize();
		}
		return redisCache;
	}
	/**
	 * 获取当前登录用户
	 * @param token
	 * @return
	 * @date Aug 25, 2016
	 */
	public static BsSysUser getUser(HttpServletRequest request){
		//TODO
		return null;
	}

	/**
	 * 通过系统参数的key值可获取redis中key值所对应的该系统参数信息
	 * @param token
	 * @return
	 * @date Aug 25, 2016
	 */
	public static Map<String,Object> getParam(String key){
		Map<String,Object> param = redisCache.get(key+"param");
		return param;
	}

	/**
	 * 通过系统参数的key值可获取redis中key值所对应的该系统参数的value值
	 * @param token
	 * @return
	 * @date Aug 25, 2016
	 */
	public static String getValueBykey(String key){
		Map<String,Object> param = redisCache.get(key+"param");
		return (String) param.get("PARAM_VALUE");
	}

	/**
	 * 通过键值对的type(分类名称)获取redis的信息
	 * @param request
	 * @param value
	 * @param type
	 * @return
	 * @date Aug 25, 2016
	 */
	public static List<Map<String,Object>> getDic(String type){
		List<Map<String,Object>> dic=redisCache.get(type+"classify");
		return dic;
	}

	/**
	 * 获取当前登录用户的相关信息 
	 * Gets information about the current logged-in user
	 * @param code 用户code
	 * @return user infe
	 * @date Aug 26, 2016
	 */
	public static UserInfoDto getUserInfo(String code){
		Gson gson = new Gson();
		UserInfoDto userInfo = gson.fromJson(redisCache.getString(code), UserInfoDto.class);
		return userInfo;
	}

	public static void main(String [] a){
		RedisManager red = new RedisManager();
		red.start();
	}

}