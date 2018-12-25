package com.ningze.api;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * 用户类相关API设置
 * 
 * @author zhanght.fnst
 * 
 */
@ApplicationPath(value = "setting")
public class MyApplicationSetting extends ResourceConfig {

	/**
	 * 构造器
	 */
	public MyApplicationSetting() {
		register(MultiPartFeature.class);
		packages(true, "com.ningze.rest.setting");
	}
}
