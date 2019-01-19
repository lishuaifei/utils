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

		import java.io.InputStream;
		import java.nio.charset.Charset;
		import java.util.ArrayList;
		import java.util.List;
		import java.util.Properties;

		import redis.clients.jedis.JedisPoolConfig;
		import redis.clients.jedis.JedisShardInfo;
		import redis.clients.jedis.ShardedJedis;
		import redis.clients.jedis.ShardedJedisPool;
		import redis.clients.util.Hashing;
		import redis.clients.util.Sharded;

		import com.ccs.core.util.StringUtil;
		import com.ccs.core.util.TSerUtils;
		import com.google.gson.Gson;

/**
 * Use Redis to implement our own cache utility.
 *
 * @author sfli
 * @date Jul 22, 2016
 */
public final class Redis {
	//private static final Logger LOG = LoggerFactory.getLogger(App.class);

	private List<JedisShardInfo> shards = null;
	private ShardedJedisPool pool;
	Gson gson = new Gson();
	/**
	 * Default constructor with shard information
	 *
	 * @param conf
	 *            redis conf
	 */
	public Redis() {
		Properties properties = new Properties();
		try {
			//读取配置文件
			InputStream is = Redis.class.getResourceAsStream(
					"/conf/cfg.properties");
			properties.load(is);
			String address = properties.getProperty("redis.address");
			String postRedis = properties.getProperty("redis.port");

			String[] hosts = address.split(",");
			String[] ports = postRedis.split(",");

			this.shards = new ArrayList<JedisShardInfo>();
			for (int i = 0; i < hosts.length; i++) {
				this.shards.add(new JedisShardInfo(hosts[i], ports[i]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//		this.shards.add(new JedisShardInfo("10.1.4.29", 6379));
	}

	/**
	 * Do some redis initialize work,such as idle time...
	 *
	 * @author sfli
	 * @date Aug 25, 2016
	 */
	public void initialize() {
		JedisPoolConfig config = new JedisPoolConfig();
		this.pool = new ShardedJedisPool(config, shards, Hashing.MURMUR_HASH,
				Sharded.DEFAULT_KEY_TAG_PATTERN);
	}

	/**
	 * Destroy the pool.
	 *
	 * @author sfli
	 */
	public void destroy() {
		if (null != pool) {
			this.pool.destroy();
		}
	}

	/**
	 * Get from cache
	 *
	 * @param key
	 *            key of pair
	 * @return value of pair
	 * @author sfli
	 * @date Aug 25, 2016
	 */
	public <T> T get(final String key) {
		T value = null;
		ShardedJedis jds = null;

		/*Preconditions.checkArgument(pool != null,
				"Please initializa redis pool before using it");*/

		try {

			if (!StringUtil.isEmpty(key)) {

				jds = pool.getResource();
				byte[] values = jds.get(key.getBytes(Charset.forName("utf-8")));
				if (values == null || (values != null && values.length <= 0)) {
					/*LOG.error("Oops get key=" + key
							+ " from cache value is null or length<=0");*/
					return null;
				}
				value = TSerUtils.byteArr2T(values);
			}

		} catch (Throwable ex) {
			/*LOG.error("Oops get key=" + key + " from cache error", ex);
			throw new KkbException(ex);*/
		} finally {
			if (jds != null) {
				this.pool.returnResource(jds);
			}
		}

		return value;
	}

	/**
	 * Put pair into cache
	 *
	 * @param key
	 *            key of pair
	 * @param value
	 *            value of pair
	 * @author sfli
	 * @date Aug 25, 2016
	 */
	public <T> void set(final String key, final T value) {
		/*Preconditions.checkArgument(pool != null,
				"Please initializa redis pool before using it");*/
		ShardedJedis jds = null;

		try {
			if (!StringUtil.isEmpty(key) && null != value) {

				jds = pool.getResource();
				jds.set(key.getBytes(Charset.forName("utf-8")),
						TSerUtils.serial(value));

			}
		} catch (Throwable ex) {
			/*LOG.error("Oops set key=" + key + ",value=" + value
					+ " from cache error", ex);
			throw new KkbException(ex);*/
		} finally {
			if (jds != null) {
				this.pool.returnResource(jds);
			}
		}
	}
	/**
	 * 获取字符串
	 * @param key
	 * @return
	 */
	public String getString(final String key){
		String value = null;
		ShardedJedis jds = null;
		try {
			if (!StringUtil.isEmpty(key)) {
				jds = pool.getResource();
				value = jds.get(key);
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		} finally {
			if (jds != null) {
				this.pool.returnResource(jds);
			}
		}

		return value;
	}
	/**
	 * 放入字符串类型
	 *
	 * @param key
	 *            key of pair
	 * @param value
	 *            value of pair
	 * @author sfli
	 * @date Aug 25, 2016
	 */
	public void setString(final String key, String value) {
		ShardedJedis jds = null;
		try {
			if (!StringUtil.isEmpty(key) && null != value) {
				jds = pool.getResource();
				jds.set(key,value);
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		} finally {
			if (jds != null) {
				this.pool.returnResource(jds);
			}
		}
	}
	/**
	 * 获取字符串
	 * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 stop 指定。
	 * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
	 * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
	 * 例：getList(key,0,-1)表示取出所有list 
	 * @param key
	 * @return
	 */
	public List<String> getList(String key,Integer start,Integer end){
		List<String> value = null;
		ShardedJedis jds = null;
		try {
			if (!StringUtil.isEmpty(key)) {
				jds = pool.getResource();
				value = jds.lrange(key, start, end);
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		} finally {
			if (jds != null) {
				this.pool.returnResource(jds);
			}
		}

		return value;
	}
	/**
	 * 放入字符串类型
	 *
	 * @param key
	 *            key of pair
	 * @param value
	 *            value of pair
	 * @author sfli
	 * @date Aug 25, 2016
	 */
	public void setList(final String key, String value) {
		ShardedJedis jds = null;
		try {
			if (!StringUtil.isEmpty(key) && null != value) {
				jds = pool.getResource();
				jds.lpush(key, value);
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		} finally {
			if (jds != null) {
				this.pool.returnResource(jds);
			}
		}
	}
	/**
	 * Delete from cache
	 *
	 * @param key
	 *            key of pair
	 * @return boolean
	 * @author lishuaifei
	 * @date Aug 27, 2016
	 */
	public <T> boolean delete(final String key) {
		ShardedJedis jds = null;

		/*Preconditions.checkArgument(pool != null,
				"Please initializa redis pool before using it");*/
		try {
			if (!StringUtil.isEmpty(key)) {

				jds = pool.getResource();
				jds.del(key);
				return true;
			}

		} catch (Throwable ex) {
			/*LOG.error("Oops delete key=" + key + " from cache error", ex);
			throw new KkbException(ex);*/
		} finally {
			if (jds != null) {
				this.pool.returnResource(jds);
			}
		}
		return false;
	}
}