/**
 * 
 */
package com.ningze.constant;

import com.google.common.cache.LoadingCache;

/**
 * @author ptero
 *
 */
public final class RuntimeEnv {

	/**
	 * 测试标志
	 */
	public static boolean TEST_FLAG = false;

	/**
	 * token缓存
	 */
	public static LoadingCache<String, String> BEIJING_BANK_TOKEN_CACHE = null;

}
