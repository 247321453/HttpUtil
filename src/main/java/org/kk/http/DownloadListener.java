package org.kk.http;

import org.kk.http.bean.DownloadError;

/***
 * 下载监听
 */
public interface DownloadListener {
    /***
     * 下载开始
     * @param pos 进度
     * @param length 文件
     */
	void onStart(float pos, long length);

    /**
     * 下载中
     * @param progress  当前位置
     */
	void onProgress(float progress);

    /***
     * 完成
     * @param err 错误
     */
	void onFinish(DownloadError err);
}