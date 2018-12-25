/**
 * 
 */
package com.ningze.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ningze.constant.ApiSettings;
import com.ningze.entity.FieldEntity;
import com.ningze.entity.StatEntity;
import com.ningze.secret.RSAUtil;

/**
 * @author ptero
 *
 */
public final class MethodUtil {

	/**
	 * 校验是否为空
	 * 
	 * @param object
	 * @return
	 */
	public static <T> boolean isNull(T object) {
		if (object == null) {
			return true;
		}
		if (object.toString().trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 获取UUID
	 * 
	 * @return UUID
	 */
	public static final String getUUID() {
		UUIDHexGenerator gen = new UUIDHexGenerator();
		return (String) gen.generate();
	}

	public static final String getRandom16() {
		String uuid = getUUID();
		return uuid.substring(0, 16);
	}

	/**
	 * 消息体非空校验
	 * 
	 * @param requestJSON 请求json数据
	 * @param apiName     接口名称
	 * @param channelNo   渠道编码
	 * @return true合法
	 */
	public static StatEntity checkRequestEntity(JSONObject requestJSON, String apiName, String channelId) {
		StatEntity statEntity = new StatEntity();
		statEntity.setStat(true);
		Map<String, FieldEntity> apiFieldMap = ApiSettings.getApiFieldChannelMap().get(channelId + apiName);
		if (null == apiFieldMap) {
			statEntity.setStat(false);
			statEntity.setMessage("该渠道" + apiName + "接口内存中模板为空");
			return statEntity;
		}
		// 获取所有参数的key-value对
		Map<String, Object> paraKeyValueMap = new HashMap<>();
		Map<String, Integer> arraySizeMap = new HashMap<>();
		Set<Entry<String, Object>> entrySet = requestJSON.entrySet();
		for (Entry<String, Object> oneEntry : entrySet) {
			genKeyValueMapFromJson(oneEntry, "", paraKeyValueMap, arraySizeMap);
		}
		Set<Entry<String, FieldEntity>> apiEntrySet = apiFieldMap.entrySet();
		// 校验key-value
		for (Entry<String, FieldEntity> oneEntry : apiEntrySet) {
			String key = oneEntry.getKey();
			FieldEntity value = oneEntry.getValue();
			// 首先检验必输项
			if (!value.isNullAble()) {
				// 过滤掉节点名
				String prefix = key.substring(0, key.lastIndexOf("_"));
				// 看是否为jsonarray元素
				Integer size = arraySizeMap.get(prefix);
				if (!Objects.isNull(size)) {
					int j = 0;
					Pattern pattern = Pattern.compile(key + "((_\\S{32})|())");
					Matcher matcher = null;
					Set<String> paraKeySet = paraKeyValueMap.keySet();
					for (String oneKey : paraKeySet) {
						matcher = pattern.matcher(oneKey);
						if (matcher.matches()) {
							j++;
						}
					}
					if (size != j) {
						statEntity.setStat(false);
						statEntity.setMessage("该数组字段" + key + "不存在或者为空!");
						return statEntity;
					}
				}
				// 非array节点下是否存在及为空校验
				if (!paraKeyValueMap.containsKey(key) || MethodUtil.isNull(paraKeyValueMap.get(key))) {
					statEntity.setStat(false);
					statEntity.setMessage("该字段" + key + "不存在或者为空!");
					return statEntity;
				}
				// 非array节点校验格式
				if (!MethodUtil.isNull(value.getPattern())) {
					Pattern pattern = Pattern.compile(value.getPattern());
					Matcher matcher = pattern.matcher(String.valueOf(paraKeyValueMap.get(key)));
					if (!matcher.matches()) {
						statEntity.setStat(false);
						statEntity.setMessage(key + "参数格式非法");
						return statEntity;
					}
				}
				// 针对JSONArray数据特殊处理
				Set<String> paraKeySet = paraKeyValueMap.keySet();
				for (String oneKey : paraKeySet) {
					// 能进入该流程的都是array下面的节点
					if (oneKey.startsWith(key + "_")) {
						// 空串校验
						if (MethodUtil.isNull(paraKeyValueMap.get(oneKey))) {
							statEntity.setStat(false);
							statEntity.setMessage("该字段" + key + "不存在或者为空!");
							return statEntity;
						}
						// 校验格式
						if (!MethodUtil.isNull(value.getPattern())) {
							Pattern pattern = Pattern.compile(value.getPattern());
							Matcher matcher = pattern.matcher(String.valueOf(paraKeyValueMap.get(oneKey)));
							if (!matcher.matches()) {
								statEntity.setStat(false);
								statEntity.setMessage(key + "参数格式非法");
								return statEntity;
							}
						}
					}
				}
			} else {
				// 可以为空的校验格式
				if (!MethodUtil.isNull(paraKeyValueMap.get(key))) {
					// 校验格式
					if (!MethodUtil.isNull(value.getPattern())) {
						Pattern pattern = Pattern.compile(value.getPattern());
						Matcher matcher = pattern.matcher(String.valueOf(paraKeyValueMap.get(key)));
						if (!matcher.matches()) {
							statEntity.setStat(false);
							statEntity.setMessage(key + "参数格式非法");
							return statEntity;
						}
					}
					Set<String> paraKeySet = paraKeyValueMap.keySet();
					for (String oneKey : paraKeySet) {
						if (oneKey.startsWith(key + "_")) {
							// 校验格式
							if (!MethodUtil.isNull(value.getPattern())) {
								Pattern pattern = Pattern.compile(value.getPattern());
								Matcher matcher = pattern.matcher(String.valueOf(paraKeyValueMap.get(oneKey)));
								if (!matcher.matches()) {
									statEntity.setStat(false);
									statEntity.setMessage(key + "参数格式非法");
									return statEntity;
								}
							}
						}
					}
				}
			}
		}

		return statEntity;

	}

	/**
	 * 生成Api接口校验MAP
	 * 
	 * @param parentElemnet
	 * @param initValue
	 * @param apiConfigMap
	 */
	public static final void genApiCheckMapFromXml(Element parentElemnet, String initValue,
			Map<String, FieldEntity> apiConfigMap) {
		List<Element> subElements = parentElemnet.getChildren("field");
		if (null != subElements && subElements.size() > 0) {
			initValue += "_" + parentElemnet.getAttributeValue("name");
			for (Element oneElement : subElements) {
				genApiCheckMapFromXml(oneElement, initValue, apiConfigMap);
			}
		} else {
			String name = initValue + "_" + parentElemnet.getAttributeValue("name");
			FieldEntity fieldEntity = new FieldEntity();
			fieldEntity.setComment(parentElemnet.getAttributeValue("comment"));
			fieldEntity.setName(parentElemnet.getAttributeValue("name"));
			fieldEntity.setNullAble(Boolean.valueOf(parentElemnet.getAttributeValue("nullAble")));
			fieldEntity.setPattern(parentElemnet.getAttributeValue("pattern"));
			fieldEntity.setType(parentElemnet.getAttributeValue("type"));
			apiConfigMap.put(name, fieldEntity);
//			System.out.println("name = " + name);
		}
	}

	public static final void genKeyValueMapFromJson(Entry<String, Object> parentElemnet, String initValue,
			Map<String, Object> keyValueMap, Map<String, Integer> arraySizeMap) {
		String key = parentElemnet.getKey();
		Object value = parentElemnet.getValue();
		if (value instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) value;
			Set<Entry<String, Object>> entrySet = jsonObject.entrySet();
			initValue += "_" + key;
			for (Entry<String, Object> oneEntry : entrySet) {
				genKeyValueMapFromJson(oneEntry, initValue, keyValueMap, arraySizeMap);
			}
		} else if (value instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) value;
			if (jsonArray.size() > 0) {
				initValue += "_" + key;
				arraySizeMap.put(initValue, jsonArray.size());
				for (Object oneObject : jsonArray) {
					JSONObject jsonObject = (JSONObject) oneObject;
					Set<Entry<String, Object>> entrySet = jsonObject.entrySet();
					for (Entry<String, Object> oneEntry : entrySet) {
						genKeyValueMapFromJson(oneEntry, initValue, keyValueMap, arraySizeMap);
					}
				}
			}
		} else {
			String name = initValue + "_" + key;
			if (keyValueMap.containsKey(name)) {
				name += "_" + getUUID();
			}
			keyValueMap.put(name, value);
//			System.out.println("name = " + name + " value = " + value);
		}
	}

	public static final void genTestJsonFromXml(Element parentElemnet, String initValue, Object jsonObject,
			Map<String, JSONObject> apiConfigMap) {
		List<Element> subElements = parentElemnet.getChildren("field");
		if (null != subElements && subElements.size() > 0) {
			initValue += "_" + parentElemnet.getAttributeValue("name");
			String type = parentElemnet.getAttributeValue("type");
			Object jsonObjectSub = null;
			if ("JsonObject".equals(type)) {
				jsonObjectSub = new JSONObject();
				if (jsonObject instanceof JSONObject) {
					JSONObject jsonObject3 = (JSONObject) jsonObject;
					jsonObject3.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
				} else if (jsonObject instanceof JSONArray) {
					JSONArray jsonObject3 = (JSONArray) jsonObject;
					JSONObject jsonObject4 = new JSONObject();
					jsonObject4.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
					jsonObject3.add(jsonObject4);
				}
			} else if ("JsonArray".equals(type)) {
				jsonObjectSub = new JSONArray();
				if (jsonObject instanceof JSONObject) {
					JSONObject jsonObject3 = (JSONObject) jsonObject;
					jsonObject3.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
				} else if (jsonObject instanceof JSONArray) {
					JSONArray jsonObject3 = (JSONArray) jsonObject;
					JSONObject jsonObject4 = new JSONObject();
					jsonObject4.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
					jsonObject3.add(jsonObject4);
				}
			}
			for (Element oneElement : subElements) {
				genTestJsonFromXml(oneElement, initValue, jsonObjectSub, apiConfigMap);
			}
		} else {
			String name = initValue + "_" + parentElemnet.getAttributeValue("name");
			if (jsonObject instanceof JSONObject) {
				JSONObject jsonObject2 = (JSONObject) jsonObject;
				jsonObject2.put(parentElemnet.getAttributeValue("name"),
						MethodUtil.isNull(parentElemnet.getAttributeValue("defaultValue")) ? ""
								: parentElemnet.getAttributeValue("defaultValue"));
			} else if (jsonObject instanceof JSONArray) {
				// 过滤节点名称
				String prefix = name.substring(0, name.lastIndexOf("_"));
				JSONArray jsonObject2 = (JSONArray) jsonObject;
				JSONObject jsonObject3 = null;
				if (!MethodUtil.isNull(prefix)) {
					if (apiConfigMap.containsKey(prefix)) {
						jsonObject3 = apiConfigMap.get(prefix);
					} else {
						jsonObject3 = new JSONObject();
						jsonObject2.add(jsonObject3);
						apiConfigMap.put(prefix, jsonObject3);
					}
				}
				jsonObject3.put(parentElemnet.getAttributeValue("name"),
						MethodUtil.isNull(parentElemnet.getAttributeValue("defaultValue")) ? ""
								: parentElemnet.getAttributeValue("defaultValue"));
			}
		}
	}

	public static final void genTestJsonFromXmlEncode(Element parentElemnet, String initValue, Object jsonObject,
			Map<String, JSONObject> apiConfigMap) {
		List<Element> subElements = parentElemnet.getChildren("field");
		if (null != subElements && subElements.size() > 0) {
			initValue += "_" + parentElemnet.getAttributeValue("name");
			String type = parentElemnet.getAttributeValue("type");
			Object jsonObjectSub = null;
			if ("JsonObject".equals(type)) {
				jsonObjectSub = new JSONObject();
				if (jsonObject instanceof JSONObject) {
					JSONObject jsonObject3 = (JSONObject) jsonObject;
					jsonObject3.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
				} else if (jsonObject instanceof JSONArray) {
					JSONArray jsonObject3 = (JSONArray) jsonObject;
					JSONObject jsonObject4 = new JSONObject();
					jsonObject4.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
					jsonObject3.add(jsonObject4);
				}
			} else if ("JsonArray".equals(type)) {
				jsonObjectSub = new JSONArray();
				if (jsonObject instanceof JSONObject) {
					JSONObject jsonObject3 = (JSONObject) jsonObject;
					jsonObject3.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
				} else if (jsonObject instanceof JSONArray) {
					JSONArray jsonObject3 = (JSONArray) jsonObject;
					JSONObject jsonObject4 = new JSONObject();
					jsonObject4.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
					jsonObject3.add(jsonObject4);
				}
			}
			for (Element oneElement : subElements) {
				genTestJsonFromXmlEncode(oneElement, initValue, jsonObjectSub, apiConfigMap);
			}
		} else {
			String name = initValue + "_" + parentElemnet.getAttributeValue("name");
			if (jsonObject instanceof JSONObject) {
				JSONObject jsonObject2 = (JSONObject) jsonObject;
				jsonObject2.put(parentElemnet.getAttributeValue("name"),
						MethodUtil.isNull(parentElemnet.getAttributeValue("defaultValue")) ? ""
								: RSAUtil.rsaEncodeFromCerFile("/Users/ptero/keystore/fd.cer",
										parentElemnet.getAttributeValue("defaultValue")));
			} else if (jsonObject instanceof JSONArray) {
				// 过滤节点名称
				String prefix = name.substring(0, name.lastIndexOf("_"));
				JSONArray jsonObject2 = (JSONArray) jsonObject;
				JSONObject jsonObject3 = null;
				if (!MethodUtil.isNull(prefix)) {
					if (apiConfigMap.containsKey(prefix)) {
						jsonObject3 = apiConfigMap.get(prefix);
					} else {
						jsonObject3 = new JSONObject();
						jsonObject2.add(jsonObject3);
						apiConfigMap.put(prefix, jsonObject3);
					}
				}
				jsonObject3.put(parentElemnet.getAttributeValue("name"),
						MethodUtil.isNull(parentElemnet.getAttributeValue("defaultValue")) ? ""
								: RSAUtil.rsaEncodeFromCerFile("/Users/ptero/keystore/fd.cer",
										parentElemnet.getAttributeValue("defaultValue")));
			}
		}
	}

	public static final void genApiComment(Element parentElemnet, String initValue, Object jsonObject,
			Map<String, JSONObject> apiConfigMap) {
		List<Element> subElements = parentElemnet.getChildren("field");
		if (null != subElements && subElements.size() > 0) {
			initValue += "_" + parentElemnet.getAttributeValue("name");
			String type = parentElemnet.getAttributeValue("type");
			Object jsonObjectSub = null;
			if ("JsonObject".equals(type)) {
				jsonObjectSub = new JSONObject();
				if (jsonObject instanceof JSONObject) {
					JSONObject jsonObject3 = (JSONObject) jsonObject;
					jsonObject3.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
				} else if (jsonObject instanceof JSONArray) {
					JSONArray jsonObject3 = (JSONArray) jsonObject;
					JSONObject jsonObject4 = new JSONObject();
					jsonObject4.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
					jsonObject3.add(jsonObject4);
				}
			} else if ("JsonArray".equals(type)) {
				jsonObjectSub = new JSONArray();
				if (jsonObject instanceof JSONObject) {
					JSONObject jsonObject3 = (JSONObject) jsonObject;
					jsonObject3.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
				} else if (jsonObject instanceof JSONArray) {
					JSONArray jsonObject3 = (JSONArray) jsonObject;
					JSONObject jsonObject4 = new JSONObject();
					jsonObject4.put(parentElemnet.getAttributeValue("name"), jsonObjectSub);
					jsonObject3.add(jsonObject4);
				}
			}
			for (Element oneElement : subElements) {
				genApiComment(oneElement, initValue, jsonObjectSub, apiConfigMap);
			}
		} else {
			String name = initValue + "_" + parentElemnet.getAttributeValue("name");
			if (jsonObject instanceof JSONObject) {
				JSONObject jsonObject2 = (JSONObject) jsonObject;
				jsonObject2.put(parentElemnet.getAttributeValue("name"),
						MethodUtil.isNull(parentElemnet.getAttributeValue("comment")) ? ""
								: parentElemnet.getAttributeValue("comment"));
			} else if (jsonObject instanceof JSONArray) {
				// 过滤节点名称
				String prefix = name.substring(0, name.lastIndexOf("_"));
				JSONArray jsonObject2 = (JSONArray) jsonObject;
				JSONObject jsonObject3 = null;
				if (!MethodUtil.isNull(prefix)) {
					if (apiConfigMap.containsKey(prefix)) {
						jsonObject3 = apiConfigMap.get(prefix);
					} else {
						jsonObject3 = new JSONObject();
						jsonObject2.add(jsonObject3);
						apiConfigMap.put(prefix, jsonObject3);
					}
				}
				jsonObject3.put(parentElemnet.getAttributeValue("name"),
						MethodUtil.isNull(parentElemnet.getAttributeValue("comment")) ? ""
								: parentElemnet.getAttributeValue("comment"));
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static void main(String[] args) throws JDOMException, IOException {
//		Map<String, Object> apiConfigMap = new HashMap<>();
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("name", "ptero");
//		jsonObject.put("address", "beijing");
//		JSONObject subJsonObject = new JSONObject();
//		jsonObject.put("sub", subJsonObject);
//		subJsonObject.put("lisa", "12313");
//		JSONObject subsubJsonObject = new JSONObject();
//		subsubJsonObject.put("ben", "12312");
//		subJsonObject.put("subsubben", subsubJsonObject);
//		JSONArray jsonArray = new JSONArray();
//		JSONObject jsonObject2 = new JSONObject();
//		jsonObject2.put("tttt", "kkkk");
//		jsonArray.add(jsonObject2);
//		JSONObject jsonObject3 = new JSONObject();
//		jsonObject3.put("tttt", "wwww");
//		jsonArray.add(jsonObject3);
//		jsonObject.put("array", jsonArray);
//		System.out.println(jsonObject.toJSONString());
//		Set<Entry<String, Object>> entrySet = jsonObject.entrySet();
//		Map<String, Integer> arraySizeMap = new HashMap<>();
//		for (Entry<String, Object> oneEntry : entrySet) {
//			genKeyValueMapFromJson(oneEntry, "", apiConfigMap, arraySizeMap);
//		}

		Map<String, FieldEntity> fieldSettingMap = new HashMap<>();
		SAXBuilder builder = new SAXBuilder();
		Document document = builder
				.build("/Users/ptero/workspace/OneByOne/WebContent/WEB-INF/config/onebyone/testApiName.xml");
		Element rootElement = document.getRootElement();
		Element fieldsElement = rootElement.getChild("fields");
		List<Element> elements = fieldsElement.getChildren("field");
		JSONObject jsonObject = new JSONObject();
		Map<String, JSONObject> apiConfigMap = new HashMap<>();
		for (Element oneElement : elements) {
			genTestJsonFromXml(oneElement, "", jsonObject, apiConfigMap);
		}
		System.out.println(jsonObject);

	}

}
