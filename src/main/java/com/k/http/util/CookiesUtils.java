package com.k.http.util;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CookiesUtils {
	static CookieManager sManager;

	public static void init() {
		if (sManager == null) {
			sManager = new CookieManager();
			//
			sManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
			// 保存这个定制的 CookieManager
			CookieHandler.setDefault(sManager);
		}
	}

	/***
	 * 设置cookies
	 * 
	 * @param url
	 * @param domain
	 * @param path
	 * @param name
	 * @param value
	 * @return
	 */
	public static boolean setCookie(String url, String domain, String path, String name,
			String value) {
		init();
		try {
			CookieStore store = sManager.getCookieStore();
			URI uri = URI.create(url);
			List<HttpCookie> cookies = store.get(uri);
			int version = -999;
			for (HttpCookie cookie : cookies) {
				if (domain == null) {
					domain = cookie.getDomain();
				}
				if (version == -999) {
					version = cookie.getVersion();
				}
				if (path == null) {
					path = cookie.getPath();
				}
				String k = cookie.getName();
				if (k != null && k.equalsIgnoreCase(name)) {
					cookie.setValue(value);
					store.add(uri, cookie);
					return true;
				}
			}
			HttpCookie cookie = new HttpCookie(name, value);
			cookie.setDomain(domain);
			cookie.setMaxAge(30 * 24 * 3600);
			cookie.setPath(path);
			cookie.setVersion(version == -999 ? 0 : version);
			store.add(uri, cookie);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static List<HttpCookie> getCookies(URI url) {
		init();
		return sManager.getCookieStore().get(url);
	}

	public static List<HttpCookie> getCookies(String url) {
		List<HttpCookie> list=new ArrayList<HttpCookie>();
		try{
			list.addAll(getCookies(URI.create(url)));
		}catch(Exception e){
			
		}
		return list;
	}

	public static void updateCookies(String url, Map<String, List<String>> headers) {
		if (url != null && url.length() > 0) {
			try {
				updateCookies(URI.create(url), headers);
			} catch (Exception e) {

			}
		}
	}

	public static void updateCookies(URI uri, Map<String, List<String>> headers) {
		init();
		try {
			sManager.put(uri, headers);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 * 获取
	 * 
	 * @param url
	 * @param key
	 *            cookies的某个name
	 * @return
	 */
	public static String getCookie(String url, String name) {
		init();
		try {
			List<HttpCookie> cookies = sManager.getCookieStore().get(URI.create(url));
			for (HttpCookie cookie : cookies) {
				String k = cookie.getName();
				if (k != null && k.equalsIgnoreCase(name)) {
					return cookie.getValue();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 清除所有cookies
	 */
	public static void clearAllCookies() {
		init();
		sManager.getCookieStore().removeAll();
	}

}
