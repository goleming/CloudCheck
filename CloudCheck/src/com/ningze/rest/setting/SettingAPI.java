/**
 * 
 */
package com.ningze.rest.setting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.sax.SAXSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import com.alibaba.fastjson.JSONObject;
import com.ningze.constant.ApiSettings;
import com.ningze.constant.Constant;
import com.ningze.entity.FieldEntity;
import com.ningze.entity.StatEntity;
import com.ningze.util.MethodUtil;

/**
 * 设置接口参数
 * 
 * @author ptero
 *
 */
@Singleton
@Path("")
public class SettingAPI {

	/**
	 * 设置挡板报文
	 * 
	 * @param uri
	 * @param headers
	 * @param requestJsonBody
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	@POST
	@Path("/setTestResponse")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setTestResponse(@Context UriInfo uri, @Context HttpHeaders headers, String request) {
		JSONObject reqJson = JSONObject.parseObject(request);
		String serviceName = reqJson.getString("serviceName");
		String channelId = reqJson.getString("channelId");
		LOGGER.info("setTestResponse : " + channelId + ":" + serviceName);
		// 测试报文
		JSONObject testResponse = reqJson.getJSONObject("testResponse");
		ApiSettings.getApiTestResponseMap().put(channelId + serviceName, testResponse.toJSONString());
		JSONObject response = new JSONObject();
		response.put(Constant.STATUS, Constant.OK);
		return Response.status(200).entity(response.toJSONString()).type(new MediaType("application", "json", "UTF-8"))
				.build();
	}

	/**
	 * 获得挡板报文
	 * 
	 * @param uri
	 * @param headers
	 * @param requestJsonBody
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	@POST
	@Path("/getTestResponse")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTestResponse(@Context UriInfo uri, @Context HttpHeaders headers, String request) {
		JSONObject reqJson = JSONObject.parseObject(request);
		String serviceName = reqJson.getString("serviceName");
		String channelId = reqJson.getString("channelId");
		LOGGER.info("getTestResponse : " + channelId + ":" + serviceName);
		String testResponse = ApiSettings.getApiTestResponseMap().get(channelId + serviceName);
		if (null == testResponse) {
			return Response.status(200).entity(null).type(new MediaType("application", "json", "UTF-8")).build();
		}
		return Response.status(200).entity(testResponse).type(new MediaType("application", "json", "UTF-8")).build();
	}

	/**
	 * API设置
	 * 
	 * @param uri
	 * @param headers
	 * @param requestJsonBody
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	@POST
	@Path("/setSingleApiConfig")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setApiConfig(@Context UriInfo uri, @Context HttpHeaders headers, SAXSource saxSource)
			throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(saxSource.getInputSource());
		String xmlConfig = new XMLOutputter().outputString(document);
		Element rootElement = document.getRootElement();
		String channelId = rootElement.getAttributeValue("channelId");
		String name = rootElement.getAttributeValue("name");
		LOGGER.info("setApiConfig : " + channelId + ":" + name);
		Element fieldsElement = rootElement.getChild("fields");
		List<Element> elements = fieldsElement.getChildren("field");
		Map<String, FieldEntity> fieldSettingMap = new HashMap<>();
		for (Element oneElement : elements) {
			MethodUtil.genApiCheckMapFromXml(oneElement, "", fieldSettingMap);
		}
		ApiSettings.getApiFieldChannelMap().put(channelId + name, fieldSettingMap);
		ApiSettings.getApiXmlConfigMap().put(channelId + name, xmlConfig);
		JSONObject response = new JSONObject();
		response.put(Constant.STATUS, Constant.OK);
		return Response.status(200).entity(response.toJSONString()).type(new MediaType("application", "json", "UTF-8"))
				.build();
	}

	/**
	 * API设置
	 * 
	 * @param uri
	 * @param headers
	 * @param requestJsonBody
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	@POST
	@Path("/genApiComment")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response genApiComment(@Context UriInfo uri, @Context HttpHeaders headers, String request)
			throws JDOMException, IOException {
		JSONObject reqJson = JSONObject.parseObject(request);
		String serviceName = reqJson.getString("serviceName");
		String channelId = reqJson.getString("channelId");
		SAXBuilder builder = new SAXBuilder();
		if (null == ApiSettings.getApiXmlConfigMap().get(channelId + serviceName)) {
			JSONObject responseJson = new JSONObject();
			responseJson.put(Constant.STATUS, false);
			responseJson.put(Constant.ERROR_MSG, "该渠道接口模板为空");
			LOGGER.info(" checkService response = " + responseJson);
			return Response.status(200).entity(responseJson.toJSONString())
					.type(new MediaType("application", "json", "UTF-8")).build();
		}
		Document document = builder.build(
				new ByteArrayInputStream(ApiSettings.getApiXmlConfigMap().get(channelId + serviceName).getBytes()));
		Element rootElement = document.getRootElement();
		Element fieldsElement = rootElement.getChild("fields");
		List<Element> elements = fieldsElement.getChildren("field");
		Map<String, JSONObject> apiConfigMap = new HashMap<>();
		JSONObject jsonObject = new JSONObject();
		for (Element oneElement : elements) {
			MethodUtil.genApiComment(oneElement, "", jsonObject, apiConfigMap);
		}
		JSONObject response = new JSONObject();
		response.put("serviceName", serviceName);
		response.put("channelId", channelId);
		response.put("data", jsonObject);
		return Response.status(200).entity(response.toJSONString()).type(new MediaType("application", "json", "UTF-8"))
				.build();
	}

	/**
	 * 获取测试报文
	 * 
	 * @param uri
	 * @param headers
	 * @param requestJsonBody
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	@POST
	@Path("/genTestJson")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response genTestJsonFromXml(@Context UriInfo uri, @Context HttpHeaders headers, String request)
			throws JDOMException, IOException {
		JSONObject reqJson = JSONObject.parseObject(request);
		String serviceName = reqJson.getString("serviceName");
		String channelId = reqJson.getString("channelId");
		SAXBuilder builder = new SAXBuilder();
		if (null == ApiSettings.getApiXmlConfigMap().get(channelId + serviceName)) {
			JSONObject responseJson = new JSONObject();
			responseJson.put(Constant.STATUS, false);
			responseJson.put(Constant.ERROR_MSG, "该渠道接口模板为空");
			LOGGER.info(" checkService response = " + responseJson);
			return Response.status(200).entity(responseJson.toJSONString())
					.type(new MediaType("application", "json", "UTF-8")).build();
		}
		Document document = builder.build(
				new ByteArrayInputStream(ApiSettings.getApiXmlConfigMap().get(channelId + serviceName).getBytes()));
		Element rootElement = document.getRootElement();
		Element fieldsElement = rootElement.getChild("fields");
		List<Element> elements = fieldsElement.getChildren("field");
		Map<String, JSONObject> apiConfigMap = new HashMap<>();
		JSONObject jsonObject = new JSONObject();
		for (Element oneElement : elements) {
			MethodUtil.genTestJsonFromXml(oneElement, "", jsonObject, apiConfigMap);
		}
		JSONObject response = new JSONObject();
		response.put("serviceName", serviceName);
		response.put("channelId", channelId);
		response.put("data", jsonObject);
		return Response.status(200).entity(response.toJSONString()).type(new MediaType("application", "json", "UTF-8"))
				.build();
	}

	@POST
	@Path("/checkService")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkService(@Context UriInfo uri, @Context HttpHeaders headers, String request) throws Exception {
		JSONObject reqJson = JSONObject.parseObject(request);
		JSONObject responseJson = new JSONObject();
		String apiName = reqJson.getString("serviceName");
		String channelId = reqJson.getString("channelId");
		JSONObject checkJson = reqJson.getJSONObject("data");
		LOGGER.info("checkService : " + channelId + ":" + apiName);
		StatEntity stat = MethodUtil.checkRequestEntity(checkJson, apiName, channelId);
		if (!stat.isStat()) {
			responseJson.put(Constant.STATUS, false);
			responseJson.put(Constant.ERROR_MSG, stat.getMessage());
			LOGGER.info(" checkService response = " + responseJson);
			return Response.status(200).entity(responseJson.toJSONString())
					.type(new MediaType("application", "json", "UTF-8")).build();
		}
		responseJson.put("status", true);
		LOGGER.info(" checkService response = " + responseJson);
		return Response.status(200).entity(responseJson.toString()).type(new MediaType("application", "json", "UTF-8"))
				.build();
	}

	/**
	 * LOGGER
	 */
	private final static Logger LOGGER = LogManager.getLogger(SettingAPI.class.getName());
}
