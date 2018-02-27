package com.jinzay.JsoupT.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonUtil {

	private static Gson gson = null;

	public static final String DATE_FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

	/***
	 * ����ת�����ַ�����ʱ���ʽΪ��yyyyMMddHHmmss
	 * 
	 * @param obj ��ת���Ķ���
	 * @return
	 */
	public static String toJsonStr(Object obj) {
		return getGson(DATE_FORMAT_YYYYMMDDHHMMSS).toJson(obj);
	}

	/***
	 * ����ת�����ַ���
	 * 
	 * @param obj ��ת���Ķ���
	 * @param dateFormat ���ڸ�ʽ
	 * @return
	 */
	public static String toJsonStr(Object obj, String dateFormat) {
		return getGson(dateFormat).toJson(obj);
	}

	private static Gson getGson(String dateFormat) {
		if (gson == null) {
			synchronized (JsonUtil.class) {
				if (gson == null) {
					// ����JSON
					gson = new GsonBuilder().setDateFormat("yyyyMMddHHmmss").registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
						@Override
						public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
							return new JsonPrimitive(String.format("%.4f", src));
						}
					}).create();
				}

			}
		}
		return gson;
	}

	/**
	 * ��JSON�ַ���ת����Map
	 */
	public static Map<String, Object> parseJSON2Map(String jsonStr) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject json = JSONObject.fromObject(jsonStr);
		for (Object k : json.keySet()) {
			Object v = json.get(k);
			map.put(k.toString(), parseJSONValue(v));
		}
		return map;
	}

	// ��JSON����ת����java���󣺻������� -> �������ͣ����� -> List������ -> Map
	private static Object parseJSONValue(Object val) {
		// nullֵ
		if (val == null)
			return null;
		// ��������
		if (ClassUtils.isPrimitiveOrWrapper(val.getClass()))
			return val;
		//�ַ���
		if (ClassUtils.isAssignable(val.getClass(), String.class))
			return StringUtils.trimToNull((String) val); // ������ַ���
		// json��nullֵ
		if (val instanceof JSONNull)
			return null;
		// �����json���飬��ת����list
		if (val instanceof JSONArray) {
			List<Object> list = new ArrayList<Object>();
			JSONArray array = (JSONArray) val;
			Iterator it = array.iterator();
			while (it.hasNext()) {
				Object next = it.next();
				list.add(parseJSONValue(next));
			}
			return list;
		}
		return parseJSON2Map(val.toString());
	}

	public static void main(String[] args) {
		String json = "{\"a\":\"xyz\",\"b\":12,\"c\":[\"1\",\"2\",\"3\"],\"d\":[{\"d11\":\"658\",\"d12\":\"poi\",\"d13\":{\"d131\":\"ddddd\"},\"d14\":[\"d5\",\"d6\"]},{\"d21\":\"554\",\"d22\":\"xml\"}],\"e\":{\"e1\":\"12\",\"e2\":\"21\"}}";
		Map<String, Object> map = parseJSON2Map(json);
		Map<String, Object> e = (Map<String, Object>) map.get("e");
		System.out.println(e.get("e2"));//21
		List<String> c = (List) map.get("c");
		System.out.println(c.get(1));// 2
		List<Map<String, Object>> d = (List<Map<String, Object>>) map.get("d");
		Map<String, Object> d1 = d.get(0);
		System.out.println(d1.get("d11"));// 658
		Map<String, Object> d13 = (Map<String, Object>) d1.get("d13");
		System.out.println(d13.get("d131"));// ddddd
		List<Object> d14 = (List<Object>) d1.get("d14");
		System.out.println(d14.get(1));//d6
	}
}
