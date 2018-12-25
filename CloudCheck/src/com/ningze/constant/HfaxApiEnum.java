/**
 * 
 */
package com.ningze.constant;

/**
 * @author ptero
 *
 */
public enum HfaxApiEnum {

	getToken("获取token"), collateralNotity("房抵押品信息提交");

	/**
	 * 接口名称
	 */
	private String apiName;

	/**
	 * Construtor
	 * 
	 * @param apiName
	 */
	private HfaxApiEnum(String apiName) {
		this.apiName = apiName;
	}

	/**
	 * @return the apiName
	 */
	public String getApiName() {
		return apiName;
	}
}
