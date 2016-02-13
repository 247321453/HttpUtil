package org.kk.http;

import org.kk.http.bean.DownloadError;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    ExecutorService mPool;
    public int MAX_POOL = 8;
    /** 0 则是单线程下载 */
    public int Cache_size = 0;
    static DownloadManager sDownloadManager = null;
    final static HashMap<String, DownloadThread> sStatus = new HashMap<String, DownloadThread>();

    public DownloadManager(int num, int cache_size) {
        MAX_POOL = Math.max(1, num);
        Cache_size = cache_size;
        mPool = new ThreadPoolExecutor(num, num * 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    /**
     * @return 下载管理器
     */
    public static DownloadManager getInstance() {
        if (sDownloadManager == null) {
            sDownloadManager = new DownloadManager(4, 512 * 1024);
        }
        return sDownloadManager;
    }

    /***
     * 下载线程
     *
     * @param filepath 保存文件路径
     * @return 线程
     */
    public DownloadThread getDownloadThread(String filepath) {
        if (filepath == null)
            return null;
        return sStatus.get(filepath);
    }

    /***
     * 停止下载
     *
     * @param filepath 保存文件路径
     */
    public void stopDownload(String filepath) {
        if (filepath == null)
            return;
        DownloadThread thread = sStatus.get(filepath);
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        sStatus.remove(filepath);
    }

    /**
     * 下载
     *
     * @param url      链接
     * @param file     保存文件路径
     * @param listener 监听
     * @return 是否能下载
     */
    public DownloadThread download(String url, String file, DownloadListener listener) {
        if (url == null || file == null || mPool.isShutdown())
            return null;
        //变量
        DownloadThread thread = sStatus.get(file);
        if (thread != null) {
            thread.addListener(listener);
            return thread;
        }
        thread = new DownloadThread(mPool, url, MAX_POOL / 2, file, Cache_size,
                new MultiDownloadListener(file, listener));
        sStatus.put(file, thread);
        mPool.submit(thread);
        return thread;
    }

    /***
     * 关闭
     */
    public void close() {
        mPool.shutdown();
    }

    class MultiDownloadListener implements DownloadListener {
        DownloadListener parent;
        String file;

        public MultiDownloadListener(String file, DownloadListener parent) {
            this.file = file;
            this.parent = parent;
        }

        @Override
        public void onFinish(DownloadError err) {
            if (parent != null) {
                parent.onFinish(err);
            }
            sStatus.remove(file);
        }

        @Override
        public void onStart(float p, long length) {
            if (parent != null) {
                parent.onStart(p, length);
            }
        }

        @Override
        public void onProgress(float p) {
            if (parent != null) {
                parent.onProgress(p);
            }
        }
    }
}
