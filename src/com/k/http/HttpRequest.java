package com.k.http;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HttpRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1928983646222272206L;

	public static final String GET = "GET";
	public static final String POST = "POST";
	static final String DEF_ENCODING = "utf-8";
	static final int DEF_TIMEOUT = 15 * 1000;
	static final int DEF_READ_TIMEOUT = 15 * 1000;
	private String url;
	/** 参数 */
	List<NameValuePairEx> datas;
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
		this(url, DEF_READ_TIMEOUT);
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
			String data = NameValuePairEx.toString(datas);
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

	public HttpRequest setDatas(List<NameValuePairEx> list) {
		this.datas = list;
		return this;
	}

	public HttpRequest addData(NameValuePairEx arg) {
		if (this.datas == null) {
			this.datas = new ArrayList<NameValuePairEx>();
		}
		this.datas.add(arg);
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