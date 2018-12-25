package com.ningze.constant;

import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLContext;

/**
 * 常量
 * 
 * @author ptero
 *
 */
public interface Constant {

	/**
	 * 应用标识ID
	 * 
	 */
	public static final String WEBAPP_ROOT_KEY = "baofu";

	/**
	 * For response json
	 */
	public static final String OK = "ok";
	public static final String ERROR = "error";
	public static final String STATUS = "status";
	public static final String ERROR_MSG = "error_msg";

	public static final String NOT_APPLY = "0";
	public static final String APPLYED = "1";
	public static final String BJ_BANK_SUCCESS = "0";
	public static final String BJ_BANK_FAILED = "1";

	/**
	 * 安全环境map
	 */
	public static final ConcurrentHashMap<String, SSLContext> SSL_MAP = new ConcurrentHashMap<>();

}
