package com.jinzay.JsoupT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jinzay.JsoupT.util.JsonUtil;

public class TaobaoShopInfoGraber {

	private static final String RED = "tb-rank-red";
	private static final String BLUE = "tb-rank-blue";
	private static final String CAP = "tb-rank-cap";
	private static final String CROWN = "tb-rank-crown";

	public Set<String> dupSet = new HashSet<String>();

	private Logger logger = Logger.getLogger(TaobaoShopInfoGraber.class);

	private ReadParam param;

	public TaobaoShopInfoGraber(ReadParam param) {
		this.param = param;
	}

	public void run() {
		for (int i = 0, l = param.urls.size(); i < l; i++) {
			String url = param.urls.get(i);
			logger.info("============ ��ʼ��ȡ��" + (i + 1) + "������: " + url);
			analyzeURL(url);
			logger.info("============ ��ɻ�ȡ��" + (i + 1) + "������ ");
		}
		logger.info("*******ץȡ���!!!*******");

	}

	public void analyzeURL(String url) {
		int length = param.pages; // ץȡҳ��
		int start = param.startPage; // ��ʼҳ��
		int pageSize = 60; // ÿ������

		lp: while (start <= length) {
			logger.info("*********��ȡ��" + start + "ҳ����**********");
			String urlStr = url;
			// ����ҳ��
			if (url.indexOf("&s=") > -1) {
				urlStr = url.substring(0, url.indexOf("&s=")) + "&s=" + pageSize * (start - 1);
			} else {
				urlStr = url + "&s=" + 60 * (start - 1);
			}
			Document doc = getDoc(urlStr, null);
			Elements elements = doc.select("script");
			List<Object> auctions = new ArrayList<Object>();
			for (Element e : elements) {
				if (e.html().indexOf("g_page_config") > -1) {
					String text = e.html();
					Map<String, Object> map = JsonUtil.parseJSON2Map(text.substring(text.indexOf("{"), text.indexOf("g_srp_loadCss") - 6));
					Map<String, Object> map1 = (Map<String, Object>) map.get("mods");
					Map<String, Object> map2 = (Map<String, Object>) map1.get("itemlist");
					if ("show".equals(map2.get("status"))) {
						Map<String, Object> map3 = (Map<String, Object>) map2.get("data");
						auctions = (List<Object>) map3.get("auctions");
					} else {
						break lp;
					}
					break;
				}
			}
			start++;
			getData(auctions, urlStr);
		}
	}

	/**
	 * ��ȡ����ҳ�������
	 * 
	 * @param ss ÿҳ�е����е��̣�����list
	 * @param referer
	 */
	private void getData(List<Object> ss, String referer) {
		while (ss.size() > 0) {
			ss.remove(0);
			Map<String, Object> map4 = (Map<String, Object>) ss.get(0);
			String url = map4.get("shopLink").toString();
			String nick = map4.get("nick").toString();
			// ����ظ�
			if (dupSet.contains(url)) {
				logger.info("�ظ����ݣ�" + url);
				continue;
			}
			dupSet.add(url);
			Document doc1 = getDoc(url, referer);
			try {
				getShopInfo(doc1, url, nick);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
			}
			sleepThread(6542, 16080);
		}
	}

	/**
	 * ��ȡ����ҳ�������
	 * 
	 * @param ss ÿҳ�е����е��̣�����list
	 * @param referer
	 */
	private void getData(String url, String referer) {
		Document doc1 = getDoc(url, referer);
		try {
			getShopInfo(doc1, url, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		sleepThread(6542, 16080);
	}

	/**
	 * ƴװ������Ϣ
	 * 
	 * @param map4
	 * @param doc1
	 */
	private void getShopInfo(Document doc1, String url, String nick) {
		if (doc1.select(".service-content") != null) {
			Element e1 = doc1.select(".service-content").last();
			ShopInfo info = new ShopInfo();
			info.nick = nick;

			if (e1 != null && e1.text().indexOf("��ϵ") > -1) {
				//��ȡ������Ϣ���ȼ�
				//Element stor = doc1.select(".summary-line").first();
				Element shopNameEl = doc1.select(".shop-name").first();

				// �Ա� ��ҳ��
				if (shopNameEl != null && doc1.select("#shopExtra").first() == null) {
					String shopName = shopNameEl.text();
					String attrScore = doc1.select(".shop-rank a").first().attr("class");
					int star = doc1.select(".shop-rank i").size();
					attrScore = attrScore.substring(attrScore.indexOf("tb-rank"), attrScore.indexOf("J_TGoldlog") - 1);
					String storSore = "";
					if (CROWN.equals(attrScore)) {
						storSore = star + "���";
					} else if (CAP.equals(attrScore)) {
						storSore = star + "�ʹ�";
					} else if (BLUE.equals(attrScore)) {
						storSore = star + "��";
					} else if (RED.equals(attrScore)) {
						storSore = star + "��";
					}
					shopName = shopName.replace("�������", "");
					info.phone = e1.text();
					info.shopName = shopName;
					info.storSore = storSore;
					info.link = url;

				} else if (doc1.select(".shop-info-simple").first() != null) {
					// �Ա� ��ҳ��
					Element shopNameEl1 = doc1.select(".shop-info-simple").first();
					String shopName = shopNameEl1.select("a").text();
					String attrScore = doc1.select(".shop-rank a img").first().attr("src");
					attrScore = attrScore.substring(attrScore.lastIndexOf("/"));
					String attrScoreName = attrScore.substring(attrScore.indexOf("_") + 1, attrScore.lastIndexOf("_"));
					String star = attrScore.substring(attrScore.lastIndexOf("_") + 1, attrScore.lastIndexOf("."));
					String storSore = "";
					if ("crown".equals(attrScoreName)) {
						storSore = star + "���";
					} else if ("cap".equals(attrScoreName)) {
						storSore = star + "�ʹ�";
					} else if ("blue".equals(attrScoreName)) {
						storSore = star + "��";
					} else if ("red".equals(attrScoreName)) {
						storSore = star + "��";
					}

					info.phone = e1.text();
					info.shopName = shopName.replace("����ղ�", "");
					info.storSore = storSore;
					info.link = url;

				} else if (doc1.select("#shopExtra").first() != null) {
					// ��è
					Element shopNameEl1 = doc1.select("#shopExtra").first();
					String shopName = shopNameEl1.select(".slogo a").text();
					info.phone = e1.text();
					info.shopName = shopName;
					info.link = url;
				}

				logger.info("*** ������Ϣ��" + info);

				// ��������
				store2File(info);
			}

		}
	}

	/**
	 * �����ļ�
	 * 
	 * @param info
	 */
	private void store2File(ShopInfo info) {
		StringBuffer sBuffer = new StringBuffer();
		String sep = ",";
		sBuffer.append(info.shopName);
		sBuffer.append(sep).append(info.nick);
		sBuffer.append(sep).append(info.phone);
		sBuffer.append(sep).append(info.storSore);
		sBuffer.append(sep).append(info.link);
		sBuffer.append("\r\n");

		try {
			FileUtils.write(new File(param.fileName), sBuffer.toString(), "GBK", true);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("����ʧ��", e);
		}
	}

	/**
	 * ��ȡ���ӵ�����
	 * 
	 * @param url
	 * @param referer
	 * @return
	 */
	private Document getDoc(String url, String referer) {
		Document doc1 = null;
		if (url.indexOf("https") < 0) {
			url = "https:" + url;
		}
		for (int times = 0; times < 3; times++) {
			logger.info("��" + times + "�λ�ȡ�������ݣ�" + url);
			try {
				doc1 = Jsoup.connect(url).timeout(0).data(assHeader(referer)).get();
				break;
			} catch (IOException e) {
				if (times == 2) {
					throw new RuntimeException("��γ����޹�", e);
				} else {
					sleepThread(4869, 10241);
				}
			}
		}
		return doc1;
	}

	/**
	 * �����αװ
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
	 * ����ʱ�䣨��λ�����룩
	 * 
	 * @param min ��Сʱ��
	 * @param max ���ʱ��
	 */
	private void sleepThread(int min, int max) {
		try {
			int interval = new Random().nextInt(max) % (max - min + 1) + min;
			logger.info("��Ϣ" + interval + "s");
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

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{����: ").append(nick);
			sb.append(", ��ϵ��ʽ: ").append(phone);
			sb.append(", ��������: ").append(shopName);
			sb.append(", �����Ǽ�:  ").append(storSore);
			sb.append(", ���ӵ�ַ: ").append(link);
			sb.append("}");
			return sb.toString();
		}
	}

}
