package org.apache.http2;

public interface DownloadListener {
	public void onConnect(int code);

	public void onStart(long pos, long length);

	public void onProgress(long pos);

	public void onFinish(int err, String msg);
}
