package com.k.http;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class NameValuePairEx implements Serializable {
	private static final String UTF_8 = "UTF-8";
	/**
	 * 
	 */
	private static final long serialVersionUID = 3284464313750147707L;

	public NameValuePairEx(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String toUriString() {
		String str = "";
		try {
			str = URLEncoder.encode(name, UTF_8) + "=" + URLEncoder.encode(value, UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			str = name + "=" + value;
		}
		return str;
	}

	public static String toString(List<NameValuePairEx> list) {
		if (list == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (NameValuePairEx p : list) {
			sb.append(p.toUriString());
			sb.append("&");
		}
		String args = sb.toString();
		if (args != null && args.endsWith("&")) {
			args = args.substring(0, args.length() - 1);
		}
		return args;
	}

	public static List<NameValuePairEx> toDatas(NameValuePairEx... args) {
		List<NameValuePairEx> list = new ArrayList<NameValuePairEx>();
		for (NameValuePairEx nameValuePair : args) {
			list.add(nameValuePair);
		}
		return list;
	}

	public static List<NameValuePairEx> toDatas(String uri) {
		List<NameValuePairEx> list = new ArrayList<NameValuePairEx>();
		if (uri != null) {
			int index = uri.indexOf('?');
			if (index > 0 && index < uri.length()) {
				uri = uri.substring(index + 1);
			}
			String[] tmps = uri.split("[\\?|&]");
			for (String tmp : tmps) {
				String[] m = tmp.split("=");
				if (m.length == 2) {
					list.add(new NameValuePairEx(m[0], m[1]));
				}
			}
		}
		return list;
	}

	@Override
	public String toString() {
		return name + "=" + value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	private String name;
	private String value;
}
