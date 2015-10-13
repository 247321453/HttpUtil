package org.apache.http2;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http2.utils.CookiesUtils;

public class HttpClinetEx {
	public static boolean Log = false;
	static HttpClinetEx sInstance;
	HttpRequest DefaultHttpRequest;

	public static interface ReadListener {
		public OutputStream write(int code, InputStream inputStream);
	}

	public static void log(String str) {
		if (Log)
			System.out.println(str);
	}

	public HttpClinetEx() {
		CookiesUtils.init();
	}

	public static HttpClinetEx getInstance() {
		if (sInstance == null) {
			sInstance = new HttpClinetEx();
		}
		return sInstance;
	}

	public HttpURLConnection connect(HttpRequest request)
			throws IOException {
		return connect(request, null);
	}

	public HttpURLConnection connect(HttpRequest request, Map<String, String> args)
			throws IOException {
		if (request == null) {
			return null;
		}
		String url = request.getUrl();
		log("url:" + url);
		URL _url = new URL(url);
		HttpURLConnection url_con;
		Proxy proxy = request.getProxy();
		if (proxy == null) {
			url_con = (HttpURLConnection) _url.openConnection();
		} else {
			url_con = (HttpURLConnection) _url.openConnection(proxy);
			if (request.proxyToken != null) {
				url_con.setRequestProperty("Proxy-Authorization", request.proxyToken);
			}
		}
		if (request.timeout > 0) {
			url_con.setConnectTimeout(request.timeout);
		}
		if (request.timeout_read > 0) {
			url_con.setReadTimeout(request.timeout_read);
		}
		if (request.userAngent != null) {
			log(request.userAngent);
			url_con.setRequestProperty("User-agent", request.userAngent);
		}
		if (request.encoding != null) {
			url_con.setRequestProperty("Accept-Charset", request.encoding);
		}
		if (args != null) {
			for (Entry<String, String> entry : args.entrySet()) {
				url_con.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		if (request.redirects != null) {
			url_con.setInstanceFollowRedirects(request.redirects);
		}
		// 设置session
		if (request.savecookies) {
			List<HttpCookie> cookies = CookiesUtils.getCookies(url);
			log("cookies:" + cookies.toString());
			if (cookies.size() > 0) {
				url_con.setRequestProperty("Cookie", cookies.toString());
			}
		}
		if (request.isPost()) {
			// 输入参数
			url_con.setRequestMethod(HttpRequest.POST);
			url_con.setDoOutput(true);
			url_con.getOutputStream().write(NameValuePairEx.toString(request.datas).getBytes());
		} else {
			url_con.setRequestMethod(HttpRequest.GET);
		}
		//	url_con.connect();
		return url_con;
	}

	public byte[] readHttpContent(HttpRequest request) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] result = null;
		if (read(request, outputStream)) {
			result = outputStream.toByteArray();
		}
		close(outputStream);
		return result;
	}

	public boolean read(HttpRequest request, OutputStream outputStream) {
		if (request == null) {
			return false;
		}
		boolean connect = true;
		HttpURLConnection url_con = null;
		InputStream inputStream = null;
		try {
			url_con = connect(request, null);
			int code = url_con.getResponseCode();
			if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_PARTIAL) {
				if (request.savecookies) {
					Map<String, List<String>> headers = url_con.getHeaderFields();
					log("headers:" + headers);
					CookiesUtils.updateCookies(request.getInitUrl(), headers);
				}
				if (request.needContent) {
					inputStream = url_con.getInputStream();
					byte[] data = new byte[4096];
					int len = 0;
					while ((len = inputStream.read(data)) != -1) {
						outputStream.write(data, 0, len);
					}
				}
			} else if (code < HttpURLConnection.HTTP_OK) {
				log("other:" + code);
			} else {
				connect = false;
				log("err:" + code);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(inputStream);
			close(url_con);
		}
		return connect;
	}

	public String openHttpContent(HttpRequest request) {
		if (request == null) {
			return null;
		}
		byte[] datas = readHttpContent(request);
		String content = null;
		try {
			content = new String(datas);
		} catch (Exception e) {
		}
		return content;
	}

	public void setDefaultHttpRequest(HttpRequest request) {
		this.DefaultHttpRequest = request;
	}

	public HttpRequest getHttpRequestByDefault(String url) {
		return getHttpRequestByDefault(url, HttpRequest.DEF_TIMEOUT, HttpRequest.DEF_READ_TIMEOUT);
	}

	/***
	 * 获取默认
	 * @param url
	 * @param timeout
	 * @param read_timtout
	 * @return
	 */
	public HttpRequest getHttpRequestByDefault(String url, int timeout, int read_timtout) {
		if (this.DefaultHttpRequest != null) {
			return DefaultHttpRequest.clone().setUrl(url).setTimeout(timeout)
					.setReadTimeout(read_timtout);
		}
		HttpRequest request = new HttpRequest(url, timeout, read_timtout);
		request.setMethod(HttpRequest.GET);
		request.setNeedContent(true);
		return request;
	}

	public static String getHttpContent(String url) {
		return getHttpContent(url, 0, 0);
	}

	public static String getHttpContent(String url, int timeout, int read_timtout) {
		HttpRequest request = getInstance().getHttpRequestByDefault(url, timeout, read_timtout);
		return getInstance().openHttpContent(request);
	}

	public static String postHttpContent(String url, List<NameValuePairEx> datas) {
		return postHttpContent(url, datas, 0, 0);
	}

	public static String postHttpContent(String url, List<NameValuePairEx> datas, int timeout,
			int read_timtout) {
		HttpRequest request = getInstance().getHttpRequestByDefault(url, timeout, read_timtout);
		request.setMethod(HttpRequest.POST);
		request.setDatas(datas);
		request.setNeedContent(true);
		return getInstance().openHttpContent(request);
	}

	public static void close(HttpURLConnection conn) {
		if (conn != null) {
			conn.disconnect();
		}
	}

	public static void close(Closeable close) {
		if (close == null) {
			return;
		}
		try {
			close.close();
		} catch (Exception e) {

		}
	}
}
