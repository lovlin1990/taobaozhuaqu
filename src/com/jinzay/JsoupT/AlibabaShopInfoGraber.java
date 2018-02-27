package com.jinzay.JsoupT;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlObject;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class AlibabaShopInfoGraber {

	private static final String RED = "tb-rank-red";
	private static final String BLUE = "tb-rank-blue";
	private static final String CAP = "tb-rank-cap";
	private static final String CROWN = "tb-rank-crown";
	private static final WebClient webClient = new WebClient(BrowserVersion.CHROME);

	public Set<String> dupSet = new HashSet<String>();

	private Logger logger = Logger.getLogger(AlibabaShopInfoGraber.class);

	private ReadParam param;

	public AlibabaShopInfoGraber(ReadParam param) {
		this.param = param;
	}

	public void run() {
		for (int i = 0, l = param.urls.size(); i < l; i++) {
			String url = param.urls.get(i);
			logger.info("============ 开始获取第" + (i + 1) + "个链接: " + url);
			analyzeURL(url);
			logger.info("============ 完成获取第" + (i + 1) + "个链接 ");
		}
		logger.info("*******抓取完成!!!*******");

	}

	public void analyzeURL(String url) {
		int length = param.pages; // 抓取页数
		int start = param.startPage; // 开始页数
		int pageSize = 60; // 每天条数

		lp: while (start <= length) {
			logger.info("*********获取第" + start + "页店铺**********");
			String urlStr = url;
			// 加上页码
			/*if (url.indexOf("&s=") > -1) {
				urlStr = url.substring(0, url.indexOf("&s=")) + "&s=" + pageSize * (start - 1);
			} else {
				urlStr = url + "&s=" + 60 * (start - 1);
			}*/
			List<Object> auctions=null;
			try {
				auctions = getDoc(urlStr, null);
				//List<String> hbList = (List<String>) doc.querySelector(".item-box");
				
				//Elements elements = doc.select(".wrapper-main").first().select(".item-box");
				
				start++;
				getData(auctions, urlStr);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 获取具体页面的数据
	 * 
	 * @param ss 每页中的所有店铺，传入list
	 * @param referer
	 */
	private void getData(List<Object> ss, String referer) {
		while (ss.size() > 0) {
			ss.remove(0);
			Map<String, Object> map4 = (Map<String, Object>) ss.get(0);
			String url = map4.get("shopLink").toString();
			String nick = map4.get("nick").toString();
			// 检查重复
			if (dupSet.contains(url)) {
				logger.info("重复数据：" + url);
				continue;
			}
			dupSet.add(url);
			String doc1;
			try {
				doc1 = getDoc1(url, referer);
				getShopInfo(doc1, url, nick);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
			}
			sleepThread(6542, 16080);
		}
	}

	/**
	 * 获取具体页面的数据
	 * 
	 * @param ss 每页中的所有店铺，传入list
	 * @param referer
	 */
	/*private void getData(String url, String referer) {
		Document doc1 = getDoc(url, referer);
		try {
			getShopInfo(doc1, url, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		sleepThread(6542, 16080);
	}*/

	/**
	 * 拼装店铺信息
	 * 
	 * @param map4
	 * @param doc1
	 * @throws Exception 
	 */
	private void getShopInfo(String imgUrl, String url, String nick) throws Exception {
		ShopInfo info = new ShopInfo();
		info.nick = nick;
		info.link = url;
		
		if(StringUtils.isNotBlank(imgUrl.replaceAll(" ", "")) && imgUrl.indexOf(".swf") < 0){
			imgUrl = imgUrl.substring(imgUrl.indexOf("(")+1, imgUrl.indexOf(")"));
		}
		info.imgUrl = imgUrl.replaceAll(" ", "");
		
		logger.info("*** 店铺信息：" + info);

		// 保存数据
		store2File(info);
		
		/*if (doc1.select(".service-content") != null) {
			Element e1 = doc1.select(".service-content").last();
			ShopInfo info = new ShopInfo();
			info.nick = nick;

			if (e1 != null && e1.text().indexOf("联系") > -1) {
				//获取店铺信息，等级
				//Element stor = doc1.select(".summary-line").first();
				Element shopNameEl = doc1.select(".shop-name").first();

				// 淘宝 新页面
				if (shopNameEl != null && doc1.select("#shopExtra").first() == null) {
					String shopName = shopNameEl.text();
					String attrScore = doc1.select(".shop-rank a").first().attr("class");
					int star = doc1.select(".shop-rank i").size();
					attrScore = attrScore.substring(attrScore.indexOf("tb-rank"), attrScore.indexOf("J_TGoldlog") - 1);
					String storSore = "";
					if (CROWN.equals(attrScore)) {
						storSore = star + "金冠";
					} else if (CAP.equals(attrScore)) {
						storSore = star + "皇冠";
					} else if (BLUE.equals(attrScore)) {
						storSore = star + "钻";
					} else if (RED.equals(attrScore)) {
						storSore = star + "心";
					}
					shopName = shopName.replace("进入店铺", "");
					info.phone = e1.text();
					info.shopName = shopName;
					info.storSore = storSore;
					info.link = url;

				} else if (doc1.select(".shop-info-simple").first() != null) {
					// 淘宝 旧页面
					Element shopNameEl1 = doc1.select(".shop-info-simple").first();
					String shopName = shopNameEl1.select("a").text();
					String attrScore = doc1.select(".shop-rank a img").first().attr("src");
					attrScore = attrScore.substring(attrScore.lastIndexOf("/"));
					String attrScoreName = attrScore.substring(attrScore.indexOf("_") + 1, attrScore.lastIndexOf("_"));
					String star = attrScore.substring(attrScore.lastIndexOf("_") + 1, attrScore.lastIndexOf("."));
					String storSore = "";
					if ("crown".equals(attrScoreName)) {
						storSore = star + "金冠";
					} else if ("cap".equals(attrScoreName)) {
						storSore = star + "皇冠";
					} else if ("blue".equals(attrScoreName)) {
						storSore = star + "钻";
					} else if ("red".equals(attrScoreName)) {
						storSore = star + "心";
					}

					info.phone = e1.text();
					info.shopName = shopName.replace("点击收藏", "");
					info.storSore = storSore;
					info.link = url;

				} else if (doc1.select("#shopExtra").first() != null) {
					// 天猫
					Element shopNameEl1 = doc1.select("#shopExtra").first();
					String shopName = shopNameEl1.select(".slogo a").text();
					info.phone = e1.text();
					info.shopName = shopName;
					info.link = url;
				}

				logger.info("*** 店铺信息：" + info);

				// 保存数据
				store2File(info);
			}

		}*/
	}

	/**
	 * 保存文件
	 * 
	 * @param info
	 * @throws Exception 
	 */
	private void store2File(ShopInfo info) throws Exception {
		StringBuffer sBuffer = new StringBuffer();
		String sep = ",";
		sBuffer.append(sep).append(info.nick);
		sBuffer.append(sep).append(info.link);
		sBuffer.append("\r\n");

		try {
			FileUtils.write(new File(param.fileName), sBuffer.toString(), "GBK", true);
			File file=new File(param.imgFile);
			if(!file.exists() && !file.isDirectory()){
				file.mkdir();
			}
			if(StringUtils.isNotBlank(info.imgUrl)){
				if(info.imgUrl.indexOf(".swf") < 0){
					downloadPicture(info.imgUrl,param.imgFile+info.nick+".jpg");
				} else {
					downloadPicture(info.imgUrl,param.imgFile+info.nick+".swf");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("保存失败", e);
		}
	}

	/**
	 * 获取链接的内容
	 * 
	 * @param url
	 * @param referer
	 * @return
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws FailingHttpStatusCodeException 
	 */
	private List<Object> getDoc(String url, String referer) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage doc1 = null;
		List<Object> auctions = new ArrayList<Object>();
		//创建webclient
        
        //htmlunit 对css和javascript的支持不好，所以请关闭之
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        for (int times = 0; times < 3; times++) {
			logger.info("第" + times + "次获取链接内容：" + url);
			try {
				doc1 = (HtmlPage)webClient.getPage(url);
				DomNodeList<DomNode> hbList = doc1.querySelectorAll(".sm-offer-companyName");
				
				for (DomNode e : hbList) {
					HtmlAnchor a = (HtmlAnchor)(e);
					
					String shopLink = a.getAttribute("href");
					String nick = a.asText();
					
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("shopLink", shopLink);
					map.put("nick", nick);
					auctions.add(map);
					
				}
				break;
			} catch (IOException e) {
				if (times == 2) {
					throw new RuntimeException("多次尝试无果", e);
				} else {
					sleepThread(4869, 10241);
				}
			}
		}
	
        //关闭webclient
        webClient.closeAllWindows();
        
    	return auctions;
		
		/*Document doc1 = null;
		if (url.indexOf("https") < 0) {
			url = "https:" + url;
		}
		for (int times = 0; times < 3; times++) {
			logger.info("第" + times + "次获取链接内容：" + url);
			try {
				doc1 = Jsoup.connect(url).timeout(0).data(assHeader(referer)).get();
				break;
			} catch (IOException e) {
				if (times == 2) {
					throw new RuntimeException("多次尝试无果", e);
				} else {
					sleepThread(4869, 10241);
				}
			}
		}
		return doc1;*/
	}
	private String getDoc1(String url, String referer) {
		HtmlPage doc1 = null;
		String imgUrl = null;
		List<Object> auctions = new ArrayList<Object>();
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
				
				if(domNode1 != null){
					HtmlDivision division1 = (HtmlDivision)domNode1;
					imgUrl = division1.getAttribute("data-url");
				} else {
					HtmlDivision division = (HtmlDivision)domNode;
					imgUrl = division.getAttribute("style");
				}
				break;
			} catch (IOException e) {
				if (times == 2) {
					throw new RuntimeException("多次尝试无果", e);
				} else {
					sleepThread(4869, 10241);
				}
			}
		}
	
        //关闭webclient
        webClient.closeAllWindows();
		
		return imgUrl;
	}

	/**
	 * 浏览器伪装
	 */
	private Map<String, String> assHeader(String referer) {
		Map<String, String> header = new HashMap<String, String>();
		header.put("Host", "https://www.taobao.com");
		header.put("User-Agent", "	Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
		header.put("Accept", "	text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		header.put("Accept-Language", "zh-cn,zh;q=0.5");
		header.put("Accept-Charset", "	GB2312,utf-8;q=0.7,*;q=0.7");
		header.put("Connection", "keep-alive");
		if (referer == null)
			referer = "https://www.taobao.com/";
		header.put("referer", referer);
		return header;
	}

	/**
	 * 休眠时间（单位：毫秒）
	 * 
	 * @param min 最小时间
	 * @param max 最大时间
	 */
	private void sleepThread(int min, int max) {
		try {
			int interval = new Random().nextInt(max) % (max - min + 1) + min;
			logger.info("休息" + interval + "s");
			Thread.sleep(interval);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	class ShopInfo {
		String nick;
		String phone;
		String shopName;
		String storSore;
		String link;
		String imgUrl;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{名称: ").append(nick);
			sb.append(", 链接地址: ").append(link);
			sb.append("}");
			return sb.toString();
		}
	}
	
	 /** 
     * 传入要下载的图片的imgUrl，将imgUrl所对应的图片下载到本地 imgDownAddr
     * @param imgUrl ,imgDownAddr
     */  
    private void downloadPicture(String imgUrl,String imgDownAddr) throws Exception {
    	sleepThread(2000, 3000);
    	HttpURLConnection connection = (HttpURLConnection) new URL(imgUrl).openConnection();
		
    	connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
    	connection.setRequestProperty("Accept","image/webp,image/*,*/*;q=0.8");
    	connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
        connection.setRequestProperty("Accept-Language", "zh-cn,zh;q=0.5");
        //connection.setRequestProperty("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
        connection.setRequestProperty("Connection", "keep-alive"); //keep-Alive，有什么用呢，你不是在访问网站，你是在采集。嘿嘿。减轻别人的压力，也是减轻自己。

        for (int times = 0; times < 3; times++) {
			logger.info("第" + times + "次下载图片：" + imgUrl);
			try {
				InputStream stream = connection.getInputStream();
				
				BufferedInputStream bis = new BufferedInputStream(stream);
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imgDownAddr));
				
				byte[] buffer = new byte[1024];
				int len = -1;
				while((len = bis.read(buffer)) != -1) {
					bos.write(buffer, 0, len);
				}
				bos.flush();
				bos.close();
				bis.close();
				logger.info("======下载图片成功======");
				break;
			} catch (IOException e) {
				if (times == 2) {
					throw new RuntimeException("====多次尝试下载图片失败===", e);
				} else {
					sleepThread(4869, 10241);
				}
			}
		}
		
		connection.disconnect();
    } 

}
