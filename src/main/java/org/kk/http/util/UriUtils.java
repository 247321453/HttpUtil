package org.kk.http.util;

import org.kk.http.bean.HttpRequest;

import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class UriUtils {
    private static final String UTF_8 = "UTF-8";

    /**
     * @param httpurl 网址
     * @return 重定向一次的url
     */
    public static String getLocationUrl(String httpurl) {
        HttpRequest request = new HttpRequest(httpurl, 60 * 1000);
        request.setMethod(HttpRequest.GET);
        request.setCanRedirects(false);
        request.setDefaultAngent();
        String newurl = httpurl;
        HttpURLConnection connection = null;
        try {
            connection = HttpUtil.connect(request, null);
            int code = connection.getResponseCode();
            if (HttpUtil.isStatusRedirect(code)) {
                newurl = connection.getHeaderField(HttpUtil.HEADER_LOCATION);
            } else {
            }
        } catch (Exception e) {

        } finally {
            IOUtil.close(connection);
        }
        return newurl;
    }

    /**
     * 数据转query
     *
     * @param list 数据
     * @return query字符串，不带?
     */
    public static String toQueryString(Map<String, String> list) {
        if (list == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> e : list.entrySet()) {
            sb.append(e.getKey() + "=" + encode(e.getValue()));
            sb.append("&");
        }
        String args = sb.toString();
        if (args.endsWith("&")) {
            args = args.substring(0, args.length() - 1);
        }
        return args;
    }

    /***
     * 从连接获取query数据
     *
     * @param uri 链接
     * @return 数据集合
     */
    public static Map<String, String> getQuerys(String uri) {
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

    /**
     * @param uri 网址
     * @return 移除?以及后面的数据
     */
    public static String removeQuerys(String uri) {
        if (uri != null) {
            int index = uri.indexOf('?');
            if (index > 0 && index < uri.length()) {
                uri = uri.substring(0, index);
            }
        }
        return uri;
    }

    /***
     * url解码
     *
     * @param str 字符串
     * @return 解码后字符串
     */
    public static String decode(String str) {
        String nstr = "";
        try {
            nstr = URLDecoder.decode(str, UTF_8);
        } catch (Exception e) {

        }
        return nstr;
    }

    /***
     * url加密
     *
     * @param str 字符串
     * @return 加密后
     */
    public static String encode(String str) {
        String nstr = "";
        try {
            nstr = URLEncoder.encode(str, UTF_8);
        } catch (Exception e) {

        }
        return nstr;
    }
}
