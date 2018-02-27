package com.jinzay.JsoupT;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jinzay.JsoupT.util.ReadProp;

/**
 * <p>
 * Title:
 * </p>
 *
 * <p>
 * Description:
 * </p>
 * @author ZHSL
 *
 */
public class JsoupTest {

	private static Logger logger = Logger.getLogger(JsoupTest.class);

	public static void main(String[] args) throws Exception {
		ReadParam readParam = new ReadParam();
		List<String> ls = ReadProp.setSensitiveLs();
		List<String> urlList = new ArrayList<String>();
		for (String l : ls) {
			if (l.startsWith("http") || l.startsWith("https")) {
				urlList.add(l);
			} else if (l.startsWith("pages")) {
				readParam.pages = Integer.parseInt(l.substring(l.indexOf("=") + 1));
			} else if (l.startsWith("saveUrl")) {
				readParam.fileName = l.substring(l.indexOf("=") + 1);
			} else if (l.startsWith("imgFile")) {
				readParam.imgFile = l.substring(l.indexOf("=") + 1);
			}
		}
		
		readParam.urls = urlList;
		
		//TaobaoShopInfoGraber grabe = new TaobaoShopInfoGraber(readParam);
		AlibabaShopInfoGraber grabe = new AlibabaShopInfoGraber(readParam);
		grabe.run();
	}
	
	/*private static String getDoc1(String url, String referer) {
		HtmlPage doc1 = null;
		String imgUrl = null;
		List<Object> auctions = new ArrayList<Object>();
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		//创建webclient
        //htmlunit 对css和javascript的支持不好，所以请关闭之
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        for (int times = 0; times < 3; times++) {
			logger.info("第" + times + "次获取链接内容：" + url);
			try {
				doc1 = (HtmlPage)webClient.getPage(url);
				DomNode domNode = doc1.querySelector(".simple");
				DomNode domNode1 = doc1.querySelector(".flash");
				HtmlDivision division = (HtmlDivision)domNode;
				HtmlDivision division1 = (HtmlDivision)domNode1;
				
				if(division1 != null){
					imgUrl = division1.getAttribute("data-url");
				} else {
					imgUrl = division.getAttribute("style");
				}
				break;
			} catch (IOException e) {
				if (times == 2) {
					throw new RuntimeException("多次尝试无果", e);
				} else {
					//sleepThread(4869, 10241);
				}
			}
		}
	
        //关闭webclient
        webClient.closeAllWindows();
		
		return imgUrl;
	}*/
	
}
