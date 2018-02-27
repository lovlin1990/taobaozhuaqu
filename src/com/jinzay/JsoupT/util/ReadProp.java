package com.jinzay.JsoupT.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.jinzay.JsoupT.JsoupTest;


public class ReadProp {
	private static List<String> ls = null;

	public static void destory() {
		if (ls != null) {
			ls.clear();
		}
	}

	public static List<String> setSensitiveLs() {
		/*
		 * ls = new ArrayList<String>(); Map<String,Object> map = new
		 * HashMap<String,Object>();
		 * map.put("configListCode","blackSensitivelistKey"); //配置文件编码
		 * configService =
		 * (ConfigService)SpringContext.getBean("configService");
		 * List<ConfigValue> dataList = configService.findConfigValue(map); for
		 * (ConfigValue configValue : dataList) {
		 * ls.add(configValue.getValue()); }
		 */
		ls = new ArrayList<String>();
		InputStream in = new JsoupTest().getClass().getClassLoader().getResourceAsStream("common.txt");
		//File file = new File(ReadProp.class.getResource("/common.text").getPath());
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				if(tempString.indexOf("##") < 0 && !tempString.trim().isEmpty()){
					ls.add(tempString);
				}
			}
			reader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return ls;
	}

	public static boolean isRight(String word) {
		boolean result = true;
		if (StringUtils.isBlank(word)) {
			return result;
		}
		if (ls == null) {
			setSensitiveLs();
		}
		for (String temp : ls) {
			if (word.indexOf(temp) >= 0) {
				result = false;
			}
		}
		return result;
	}
}