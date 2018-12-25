/**
 * 
 */
package com.ningze.constant;

import java.util.HashMap;
import java.util.Map;

import com.ningze.entity.FieldEntity;

/**
 * @author ptero
 *
 */
public interface ApiSettings {

	/**
	 * 各接口请求参数map
	 */
	public static final Map<String, Map<String, FieldEntity>> API_FIELD_CHANNEL_MAP = new HashMap<>();

	/**
	 * 各接口请求参数xml内存模板
	 */
	public static final Map<String, String> API_XML_CONFIG_MAP = new HashMap<>();

	/**
	 * 挡板应答报文
	 */
	public static final Map<String, String> API_TEST_RESPONSE_MAP = new HashMap<>();

	/**
	 * @return the API_BACKEND_URL_MAP
	 */
	public static Map<String, String> getApiTestResponseMap() {
		return API_TEST_RESPONSE_MAP;
	}

	/**
	 * @return the API_FIELD_CHANNEL_MAP
	 */
	public static Map<String, Map<String, FieldEntity>> getApiFieldChannelMap() {
		return API_FIELD_CHANNEL_MAP;
	}

	/**
	 * @return the API_XML_CONFIG_MAP
	 */
	public static Map<String, String> getApiXmlConfigMap() {
		return API_XML_CONFIG_MAP;
	}

}
