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
            if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_PARTIAL) {
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
