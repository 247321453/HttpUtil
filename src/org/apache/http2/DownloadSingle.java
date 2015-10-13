package org.apache.http2;

import java.io.File;

import org.apache.http2.utils.DownloadUtils;

public class DownloadSingle implements Runnable {

	String url;
	String filepath;
	DownloadListener listener;
	int timeout;
	int read_timeout;
	HttpClinetEx client;

	public DownloadSingle(String url, String file, HttpClinetEx client) {
		this(url, file, client, null);
	}

	public DownloadSingle(String url, String file, HttpClinetEx client,
			DownloadListener listener) {
		this.url = url;
		this.filepath = file;
		this.listener = listener;
		this.client = client;
	}

	@Override
	public void run() {
		if (url == null || filepath == null) {
			return;
		}
		File file = DownloadUtils.getFile(filepath);
		DownloadUtils.createDirByFile(file);
		File tmpFile = new File(file.getAbsolutePath() + ".tmp");
		long pos = 0;
		if (tmpFile.exists()) {
			pos = tmpFile.length();
		}
		long length = DownloadUtils.getDownloadlength(client, url);
		if (length < 0) {
			//不支持断点续传
			length = -length;
		}
		HttpClinetEx.log(url + ",length=" + length);
		boolean ok = DownloadUtils.download(client, url, tmpFile, pos, length,
				listener);
		HttpClinetEx.log(url + ",download=" + ok);
		if (ok) {
			DownloadUtils.deleteFile(file);
			DownloadUtils.renameTo(tmpFile, file);
		}
	}

}
