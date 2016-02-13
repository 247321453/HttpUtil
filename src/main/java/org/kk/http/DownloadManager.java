package org.kk.http;

import org.kk.http.bean.DownloadError;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {
    ExecutorService mPool;
    public int MAX_POOL = 8;
    /** 0 则是单线程下载 */
    public int Cache_size = 0;
    static DownloadManager sDownloadManager = null;
    final static HashMap<String, DownloadThread> sStatus = new HashMap<String, DownloadThread>();

    public DownloadManager(int num, int cache_size) {
        MAX_POOL = num;
        Cache_size = cache_size;
        mPool = Executors.newFixedThreadPool(MAX_POOL);
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
    public boolean download(String url, String file, DownloadListener listener) {
        if (url == null || file == null)
            return false;
        //变量
        DownloadThread thread = sStatus.get(file);
        if (thread != null) {
            thread.addListener(listener);
            return false;
        }
        thread = new DownloadThread(mPool, url, MAX_POOL / 2, file, Cache_size,
                new MultiDownloadListener(listener));
        sStatus.put(file, thread);
        mPool.execute(thread);
        return true;
    }

    /***
     * 关闭
     */
    public void close() {
        int count = sStatus.size();
        Collection<DownloadThread> threads = sStatus.values();
        for (DownloadThread thread : threads) {
            if (thread != null && thread.isDownloading()) {
                thread.interrupt();
            }
        }
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
