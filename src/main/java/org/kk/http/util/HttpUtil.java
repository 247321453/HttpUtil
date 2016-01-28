package org.kk.http.util;

import org.kk.http.bean.HttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String HEADER_CONNECTION = "Connection";
    public static final String HEADER_COOKIE = "Cookie";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_MD5 = "Content-MD5";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_DATE = "Date";
    public static final String HEADER_EXPECT = "Expect";
    public static final String HEADER_FROM = "From";
    public static final String HEADER_IF_MATCH = "If-Match";
    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
    public static final String HEADER_IF_RANGE = "If-Range";
    public static final String HEADER_IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    public static final String HEADER_MAX_FORWARDS = "Max-Forwards";
    public static final String HEADER_PRAGMA = "Pragma";
    public static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";
    public static final String HEADER_RANGE = "Range";
    public static final String HEADER_REFERER = "Referer";
    public static final String HEADER_TARNSFER_ENCODING = "TE";
    public static final String HEADER_UPGRADE = "Upgrade";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_VIA = "Via";
    public static final String HEADER_WARNING = "Warning";
    public static final String HEADER_LOCATION = "Location";

    /**
     * @param request   请求
     * @param propertys propertys
     * @return 连接
     * @throws IOException 异常
     */
    public static HttpURLConnection connect(HttpRequest request, Map<String, String> propertys) throws IOException {
        if (request == null) {
            return null;
        }
        String url = request.getUrl();
        URL _url = new URL(url);
        HttpURLConnection url_con = (HttpURLConnection) _url.openConnection();
        if (request.getTimeout() > 0) {
            url_con.setConnectTimeout(request.getTimeout());
            url_con.setReadTimeout(request.getTimeout());
        }
        if (request.getUserAngent() != null) {
            url_con.setRequestProperty("User-agent", request.getUserAngent());
        }
        if (request.getEncoding() != null) {
            url_con.setRequestProperty("Accept-Charset", request.getEncoding());
        }
        if (request.getRedirects() != null) {
            url_con.setInstanceFollowRedirects(request.getRedirects());
        }
        // 设置session
        if (request.isSavecookies()) {
            List<HttpCookie> cookies = CookiesUtils.getCookies(url);
            if (cookies.size() > 0) {
                url_con.setRequestProperty("Cookie", cookies.toString());
            }
        }
        if (propertys != null) {
            for (Map.Entry<String, String> entry : propertys.entrySet()) {
                url_con.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (request.isPost()) {
            // 输入参数
            url_con.setRequestMethod(HttpRequest.POST);
            url_con.setDoOutput(true);
            String str = request.getDataString();
            if (str != null)
                url_con.getOutputStream().write(str.getBytes());
        } else {
            url_con.setRequestMethod(HttpRequest.GET);
        }
        return url_con;

    }

    /***
     * 读取网址的内容
     *
     * @param request   请求参数
     * @param propertys propertys
     * @return 内容
     */
    public byte[] readHttpContent(HttpRequest request, Map<String, String> propertys) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] result = null;
        if (read(request, propertys, outputStream)) {
            result = outputStream.toByteArray();
        }
        IOUtil.close(outputStream);
        return result;
    }

    public static boolean isStatusOK(int code) {
        return (code >= 200) && (code <= 299);
    }

    public static boolean isStatusRedirect(int code) {
        return (code >= 300) && (code <= 399);
    }

    public static boolean isStatusClientError(int code) {
        return (code >= 400) && (code <= 499);
    }

    public static boolean isStatusServerError(int code) {
        return (code >= 500) && (code <= 599);
    }

    private boolean read(HttpRequest request, Map<String, String> propertys, OutputStream outputStream) {
        if (request == null) {
            return false;
        }
        boolean connect = true;
        HttpURLConnection url_con = null;
        InputStream inputStream = null;
        try {
            url_con = connect(request, propertys);
            int code = url_con.getResponseCode();
            if (isStatusOK(code)) {
                if (request.isSavecookies()) {
                    Map<String, List<String>> headers = url_con.getHeaderFields();
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
            } else {
                connect = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(inputStream);
            IOUtil.close(url_con);
        }
        return connect;
    }
}
