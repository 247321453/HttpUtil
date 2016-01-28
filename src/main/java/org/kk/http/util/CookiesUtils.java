package org.kk.http.util;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CookiesUtils {
    static CookieManager sManager;

    /***
     * 初始化
     */
    static {
        sManager = new CookieManager();
        //
        sManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        // 保存这个定制的 CookieManager
        CookieHandler.setDefault(sManager);
    }

    /***
     * 设置cookies
     *
     * @param url    链接
     * @param domain 作用域
     * @param path   路径
     * @param name   名字
     * @param value  值
     * @return 是否成功
     */
    public static boolean setCookie(String url, String domain, String path, String name,
                                    String value) {
        try {
            CookieStore store = sManager.getCookieStore();
            URI uri = URI.create(url);
            List<HttpCookie> cookies = store.get(uri);
            int version = -999;
            for (HttpCookie cookie : cookies) {
                if (domain == null) {
                    domain = cookie.getDomain();
                }
                if (version == -999) {
                    version = cookie.getVersion();
                }
                if (path == null) {
                    path = cookie.getPath();
                }
                String k = cookie.getName();
                if (k != null && k.equalsIgnoreCase(name)) {
                    cookie.setValue(value);
                    store.add(uri, cookie);
                    return true;
                }
            }
            HttpCookie cookie = new HttpCookie(name, value);
            cookie.setDomain(domain);
            cookie.setMaxAge(30 * 24 * 3600);
            cookie.setPath(path);
            cookie.setVersion(version == -999 ? 0 : version);
            store.add(uri, cookie);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /***
     * 获取coockies
     *
     * @param url 网址
     * @return coockies
     */
    public static List<HttpCookie> getCookies(URI url) {
        return sManager.getCookieStore().get(url);
    }

    /***
     * 获取coockies
     *
     * @param url 网址
     * @return coockies
     */
    public static List<HttpCookie> getCookies(String url) {
        List<HttpCookie> list = new ArrayList<HttpCookie>();
        try {
            list.addAll(getCookies(URI.create(url)));
        } catch (Exception e) {

        }
        return list;
    }

    /***
     * 更新某个cookies
     *
     * @param url     网址
     * @param headers 头
     */
    public static void updateCookies(String url, Map<String, List<String>> headers) {
        if (url != null && url.length() > 0) {
            try {
                sManager.put(URI.create(url), headers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /***
     * 获取
     *
     * @param url  链接
     * @param name cookies的某个name
     * @return 值
     */
    public static String getCookie(String url, String name) {
        try {
            List<HttpCookie> cookies = sManager.getCookieStore().get(URI.create(url));
            for (HttpCookie cookie : cookies) {
                String k = cookie.getName();
                if (k != null && k.equalsIgnoreCase(name)) {
                    return cookie.getValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 清除所有cookies
     */
    public static void clearAllCookies() {
        sManager.getCookieStore().removeAll();
    }

}
