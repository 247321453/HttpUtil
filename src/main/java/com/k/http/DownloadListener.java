package com.k.http;

public interface DownloadListener {
	void onStart(String url, String file);

	/**
	 * @param p
	 *            0-1
	 */
	void onProgress(String url, String file, long pos, long total, boolean writed);

	void onFinish(String url, String file, DownloadError err);
}