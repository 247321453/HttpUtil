package com.k.http;

import com.k.http.util.CookiesUtils;
import com.k.http.util.IOUtil;
import com.k.http.util.UriUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpClinetEx {
    public static boolean Log = false;
    static HttpClinetEx sInstance;

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

    public HttpURLConnection connect(HttpRequest request, Map<String, String> propertys) throws IOException {
        if (request == null) {
            return null;
        }
        String url = request.getUrl();
        log("url:" + url);
        URL _url = new URL(url);
        HttpURLConnection url_con = (HttpURLConnection) _url.openConnection();
        if (request.timeout > 0) {
            url_con.setConnectTimeout(request.timeout);
            url_con.setReadTimeout(request.timeout);
        }
        if (request.userAngent != null) {
            log(request.userAngent);
            url_con.setRequestProperty("User-agent", request.userAngent);
        }
        if (request.encoding != null) {
            url_con.setRequestProperty("Accept-Charset", request.encoding);
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
        if (propertys != null) {
            for (Map.Entry<String, String> entry : propertys.entrySet()) {
                url_con.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (request.isPost()) {
            // 输入参数
            url_con.setRequestMethod(HttpRequest.POST);
            url_con.setDoOutput(true);
            url_con.getOutputStream().write(UriUtils.toString(request.datas).getBytes());
        } else

        {
            url_con.setRequestMethod(HttpRequest.GET);
        }
        // url_con.connect();
        return url_con;

    }

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
            IOUtil.close(inputStream);
            IOUtil.close(url_con);
        }
        return connect;
    }
}
