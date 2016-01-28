package org.kk.http;

import org.kk.http.bean.DownloadError;

/***
 * 下载监听
 */
public interface DownloadListener {
    /***
     * 下载开始
     * @param url 链接
     * @param file 文件
     */
	void onStart(String url, String file);

    /**
     * 下载中
     * @param url 链接
     * @param file 文件
     * @param pos  当前位置
     * @param total 目标位置
     * @param writed 是否写入文件
     */
	void onProgress(String url, String file, long pos, long total, boolean writed);

    /***
     * 完成
     * @param url 链接
     * @param file 文件
     * @param err 错误
     */
	void onFinish(String url, String file, DownloadError err);
}