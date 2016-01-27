package com.k.http;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {
	ExecutorService mPool;
	public static int MAX_POOL = 8;
	public static int Cache_size = 0;
	static DownloadManager sDownloadManager = null;
	final HashMap<String, DownloadThread> sStatus = new HashMap<>();

	private DownloadManager(int num) {
		mPool = Executors.newFixedThreadPool(Math.min(num, MAX_POOL));
	}

	public static void init(int num, int cache_size) {
		MAX_POOL = num;
		Cache_size = cache_size;
	}

	public static DownloadManager getInstance() {
		if (sDownloadManager == null) {
			sDownloadManager = new DownloadManager(MAX_POOL);
		}
		return sDownloadManager;
	}

	public DownloadThread getDownloadThread(String filepath) {
		if (filepath == null)
			return null;
		return sStatus.get(filepath);
	}

	public void stopDownload(String filepath) {
		if (filepath == null)
			return;
		DownloadThread thread = sStatus.get(filepath);
		if (thread != null && !thread.isInterrupted()) {
			thread.interrupt();
		}
		sStatus.remove(filepath);
	}

	public boolean download(String url, String file, DownloadListener listener) {
		if (url == null || file == null)
			return false;
		if (sStatus.get(file) != null) {
			return false;
		}
		DownloadThread thread = new DownloadThread(mPool, url, file, Cache_size, new MultiDownloadListener(listener));
		sStatus.put(file, thread);
		mPool.execute(thread);
		return true;
	}

	public void close() {
		mPool.shutdown();
	}

	class MultiDownloadListener implements DownloadListener {
		DownloadListener parent;

		public MultiDownloadListener(DownloadListener parent) {
			this.parent = parent;
		}

		@Override
		public void onFinish(String url, String file, DownloadError err) {
			if (parent != null) {
				parent.onFinish(url, file, err);
			}
			sStatus.remove(file);
		}

		@Override
		public void onStart(String url, String file) {
			if (parent != null) {
				parent.onStart(url, file);
			}
		}

		@Override
		public void onProgress(String url, String file, long pos, long length, boolean writed) {
			if (parent != null) {
				parent.onProgress(url, file, pos, length, writed);
			}
		}
	}
}
