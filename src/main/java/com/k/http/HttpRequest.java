package com.k.http;

import com.k.http.util.UriUtils;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1928983646222272206L;

	public static final String GET = "GET";
	public static final String POST = "POST";
	static final int DEF_TIMEOUT = 15 * 1000;
	private String url;
	/** 参数 */
	Map<String, String> datas;
	/** 方法 */
	String method = GET;
	/** 是否读取 */
	public boolean needContent = true;
	/** 连接超时（毫秒） */
	int timeout = 0;
	/** 保存cookies */
	public boolean savecookies = false;
	/** 编码 */
	String encoding = null;
	/** 浏览器标识 */
	String userAngent = null;
	/** 是否允许跳转 */
	Boolean redirects = true;

	public boolean isPost() {
		return POST.equalsIgnoreCase(method);
	}

	public boolean isGet() {
		return GET.equalsIgnoreCase(method);
	}

	public HttpRequest(String url) {
		this(url, DEF_TIMEOUT);
	}

	public HttpRequest clone() {
		HttpRequest request = new HttpRequest(url, timeout);
		request.setDatas(datas);
		request.setMethod(method);
		request.setNeedContent(needContent);
		request.setNeedCookies(savecookies);
		request.setEncoding(encoding);
		request.userAngent = userAngent;
		request.redirects = redirects;
		return request;
	}

	public HttpRequest setDefaultAngent() {
		this.userAngent = System.getProperty("http.agent");
		return this;
	}

	public HttpRequest setAngent(String userAngent) {
		this.userAngent = userAngent;
		return this;
	}

	public String getUrl() {
		if (url != null) {
			String data = UriUtils.toString(datas);
			if (isGet() && data != null && data.length() > 0) {
				if (url.contains("?")) {
					return url + data;
				} else {
					return url + "?" + data;
				}
			}
		}
		return url;
	}

	public HttpRequest(String url, int timeout) {
		super();
		this.url = url;
		this.timeout = timeout;
	}

	public String getInitUrl() {
		return this.url;
	}

	public HttpRequest setUrl(String url) {
		this.url = url;
		return this;
	}

	public HttpRequest setCanRedirects(boolean redirects) {
		this.redirects = redirects;
		return this;
	}

	public HttpRequest setDatas(Map<String, String> list) {
		this.datas = list;
		return this;
	}

	public HttpRequest addData(String key, String value) {
		if (this.datas == null) {
			this.datas = new HashMap<>();
		}
		this.datas.put(key, value);
		return this;
	}

	public HttpRequest setMethod(String method) {
		this.method = method;
		return this;
	}

	public HttpRequest setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public HttpRequest setNeedContent(boolean needContent) {
		this.needContent = needContent;
		return this;
	}

	public HttpRequest setNeedCookies(boolean savecookies) {
		this.savecookies = savecookies;
		return this;
	}

	public HttpRequest setEncoding(String encoding) {
		try {
			Charset.forName(encoding);
			this.encoding = encoding;
		} catch (Exception e) {

		}
		return this;
	}
}
