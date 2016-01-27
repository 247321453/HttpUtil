package com.k.http.util;

import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.k.http.HttpClinetEx;
import com.k.http.HttpRequest;

public class UriUtils {
	private static final String UTF_8 = "UTF-8";
	public static String getRealUrl(String skipurl) {
		String newurl = null;
		String url = skipurl;
		boolean isSame = false;
		while (!isSame) {
			newurl = getLocationUrl(url);
			isSame = url.equals(newurl);
			url = newurl;
		}
		return newurl;
	}

	public static String getLocationUrl(String httpurl) {
		HttpRequest request = new HttpRequest(httpurl, 60 * 1000);
		request.setMethod(HttpRequest.GET);
		request.setCanRedirects(false);
		request.setDefaultAngent();
		String newurl = httpurl;
		HttpURLConnection connection = null;
		try {
			connection = HttpClinetEx.getInstance().connect(request);
			int code = connection.getResponseCode();
			if (code == HttpURLConnection.HTTP_MOVED_PERM
					|| code == HttpURLConnection.HTTP_MOVED_TEMP) {
				newurl = connection.getHeaderField("Location");
			} else {
				HttpClinetEx.log("err=" + code);
			}
		} catch (Exception e) {

		} finally {
			FileUtil.close(connection);
		}
		return newurl;
	}

	public static Map<String, String> query(String uri) {
		Map<String, String> args = new HashMap<String, String>();
		if (uri != null) {
			int index = uri.indexOf('?');
			if (index > 0 && index < uri.length()) {
				uri = uri.substring(index + 1);
				String[] tmps = uri.split("[\\?|&]");
				for (String tmp : tmps) {
					String[] m = tmp.split("=");
					if (m.length == 2) {
						args.put(m[0], decode(m[1]));
					}
				}
			}
		}
		return args;
	}

	public static String decode(String str) {
		String nstr = "";
		try {
			nstr = URLDecoder.decode(str, UTF_8);
		} catch (Exception e) {

		}
		return nstr;
	}

	public static String encode(String str) {
		String nstr = "";
		try {
			nstr = URLEncoder.encode(str, UTF_8);
		} catch (Exception e) {

		}
		return nstr;
	}
}
