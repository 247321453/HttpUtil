package org.apache.http2.utils;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http2.HttpClinetEx;
import org.apache.http2.HttpRequest;

public class UriUtils {

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
		HttpRequest request = new HttpRequest(httpurl, 60 * 1000, 60 * 1000);
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
			HttpClinetEx.close(connection);
		}
		return newurl;
	}

	public static String getHostwithProt(String url) {
		if (url != null) {
			int i = url.indexOf("://");
			if (i > 0) {
				int j = url.indexOf("/", i + 3);
				String host = (j > 0) ? url.substring(0, j) : url;
				return host;
			}
		}
		return url;
	}

	public static HashMap<String, String> getUriData(String uri) {
		HashMap<String, String> args = new HashMap<String, String>();
		if (uri != null) {
			int index = uri.indexOf('?');
			if (index > 0 && index < uri.length()) {
				uri = uri.substring(index + 1);
				String[] tmps = uri.split("[\\?|&]");
				for (String tmp : tmps) {
					String[] m = tmp.split("=");
					if (m.length == 2) {
						args.put(m[0], m[1]);
					}
				}
			}
		}
		return args;
	}

	public static String getHost(String url) {
		if (url != null) {
			int i = url.indexOf("://");
			if (i > 0) {
				int j = url.indexOf(":", i + 3);
				String host = (j > 0) ? url.substring(0, j) : url;
				return host;
			}
		}
		return url;
	}

	public static String decode(String str) {
		String nstr = "";
		try {
			nstr = URLDecoder.decode(str, "UTF-8");
		} catch (Exception e) {

		}
		return nstr;
	}

	public static String encode(String str) {
		String nstr = "";
		try {
			nstr = URLEncoder.encode(str, "UTF-8");
		} catch (Exception e) {

		}
		return nstr;
	}

	public static Proxy getProxy(String proxyHost, int proxyPort) {
		Proxy proxy = null;
		try {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
		} catch (Exception e) {

		}
		return proxy;
	}
}
