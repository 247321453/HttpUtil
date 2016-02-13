package org.kk.http;

import org.kk.http.bean.DownloadError;
import org.kk.http.bean.DownloadInfo;
import org.kk.http.bean.HttpRequest;
import org.kk.http.util.HttpUtil;
import org.kk.http.util.IOUtil;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class DownloadThread extends Thread {

    private final List<DownloadListener> listeners = new ArrayList<DownloadListener>();
    private volatile boolean isDownloading = false;
    private volatile boolean Lock = false;
    private static final int BUFF_SIZE = 1024 * 512;

    private String mUrl;
    private String mFile;
    private File tmpFile;
    private DownloadInfo mDownloadInfo;
    private ExecutorService executor;
    private int cache_size;
    private int thread;

    public DownloadThread(ExecutorService executor, String url, int thread, String file, int cache_size,
                          DownloadListener listener) {
        super(file);
        this.executor = executor;
        this.mUrl = url;
        this.mFile = file;
        this.thread = thread;
        if (listener != null)
            this.listeners.add(listener);
        this.cache_size = cache_size;
        mDownloadInfo = new DownloadInfo(file + ".cfg", cache_size);
        tmpFile = new File(mFile + ".tmp");
    }

    public long getContentLength() {
        return mDownloadInfo.getLength();
    }

    public void addListener(DownloadListener listener) {
        if (listener != null)
            this.listeners.add(listener);
    }

    @Override
    public void interrupt() {
        this.isDownloading = false;
        super.interrupt();
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public long getCompletedSize() {
        return mDownloadInfo.getCompletedSize();
    }

    public float getProgress() {
        return mDownloadInfo.getProgress();
    }
    @Override
    public void run() {
        super.run();
        this.isDownloading = true;
        // 创建目录
        File dir = tmpFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        boolean read = mDownloadInfo.createOrRead();
        System.out.println("pre download by read?" + read + " " + mDownloadInfo);
        // 需要下载的长度
        long alllength = getContentLength(mUrl);
        if (alllength < 0) {
            alllength = -alllength;
            mDownloadInfo.setLength(alllength);
            // 不支持断点
            if (mDownloadInfo.getLength() != alllength || (mDownloadInfo.getBlockCount() == 1 && alllength > BUFF_SIZE)
                    || mDownloadInfo.getBlockCount() == 0) {
                mDownloadInfo.setCache(cache_size);
                mDownloadInfo.createNew();
                System.out.println("createNew download..." + mDownloadInfo);
            }
        } else if (alllength > 0) {
            mDownloadInfo.setLength(alllength);
            if (mDownloadInfo.getLength() != alllength || mDownloadInfo.getBlockCount() != 1) {
                mDownloadInfo.setCache(alllength);
                mDownloadInfo.createNew();
                System.out.println("reset download..." + mDownloadInfo);
            }
        } else {
            for (DownloadListener listener : listeners) {
                if (listener != null) {
                    listener.onFinish(DownloadError.ERR_404);
                }
            }
            return;
        }
        if (!isCompleted()) {
            System.out.println("start download..." + mDownloadInfo);
            for (DownloadListener listener : listeners) {
                if (listener != null) {
                    listener.onStart(mDownloadInfo.getProgress(), mDownloadInfo.getLength());
                }
            }
            for (int i = 0; i < thread; i++) {
                if (startDownload(i)) {
                    System.out.println("start thread ok " + i);
                } else {
                    System.out.println("start thread fail " + i);
                }
            }
            System.out.println("end main thread ");
        }
    }

    private boolean startDownload(int start) {
        if (isDownloading) {
            if (isCompleted()) {
                return false;
            }
        }
        final long[] b = new long[2];
        final int pos = mDownloadInfo.findblock(start, b);
        if (pos >= 0) {
            if (mDownloadInfo.isDownload(pos)) {
                System.out.println(pos + " is downloading");
                return false;
            }
            mDownloadInfo.updateStatu(pos, true);
            System.out.println("submit " + pos + " " + b[0] + "-" + b[1]);
            executor.submit(new Runnable() {

                @Override
                public void run() {
                    System.out.println("download " + pos + " " + b[0] + "-" + b[1]);
                    download(tmpFile, pos, b);
                    mDownloadInfo.readBlock(pos);
                    mDownloadInfo.updateStatu(pos, false);
                    startDownload(pos + 1);
                }
            });
            return true;
        } else {
            System.out.println("no find null");
            return false;
        }
    }

    private boolean isCompleted() {
        // 有一个在下载都未完成
        if (!mDownloadInfo.isCompleted()) {
            return false;
        }
        System.out.println("is completed.");
        onfinish();
        executor.shutdown();
        return true;
    }

    private void download(File tmpFile, final int index, long[] b) {
        final long[] tmp = new long[2];
        tmp[0] = b[0];
        tmp[1] = b[1];
        final long total = tmp[1];
        final long start = tmp[0];
        if (start >= total) {
            mDownloadInfo.updateStatu(index, false);
            return;
        }
        HttpRequest request = new HttpRequest(mUrl, 60 * 1000);
        request.setMethod(HttpRequest.GET);
        request.setCanRedirects(false);
        request.setDefaultAngent();
        Map<String, String> datas = new HashMap<String, String>();
        datas.put(HttpUtil.HEADER_RANGE, "bytes=" + start + "-" + total);
        datas.put(HttpUtil.HEADER_CONNECTION, "Keep - Alive");
        HttpURLConnection httpURLConnection = null;
        RandomAccessFile output = null;
        InputStream input = null;
        try {
            // 断点续传测试
            httpURLConnection = HttpUtil.connect(request, datas);
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                // 支持断点续传
                input = httpURLConnection.getInputStream();
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                // 不支持断点续传
                input = httpURLConnection.getInputStream();
            }
            if (input == null) {
                for (DownloadListener listener : listeners) {
                    if (listener != null) {
                        listener.onFinish(DownloadError.ERR_404);
                    }
                }
            } else {
                if (!tmpFile.exists()) {
                    tmpFile.createNewFile();
                }
                output = new RandomAccessFile(tmpFile, "rws");
                output.seek(start);
                byte[] buffer = new byte[BUFF_SIZE];
                int length;
                long compeleteSize = start;
                while (isDownloading && (length = input.read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                    compeleteSize += length;
                    mDownloadInfo.updateBlock(index, new long[]{compeleteSize, total});
                    for (DownloadListener listener : listeners) {
                        if (listener != null) {
                            listener.onProgress(mDownloadInfo.getProgress());
                        }
                    }
                }
            }
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(input);
            IOUtil.close(httpURLConnection);
            IOUtil.close(output);
        }
    }

    private void onfinish() {
        long alllength = mDownloadInfo.getLength();
        if (mFile == null) {
            System.err.println("file is null");
            for (DownloadListener listener : listeners) {
                if (listener != null) {
                    listener.onFinish(DownloadError.ERR_FILE);
                }
            }
        } else {
            if (tmpFile.length() == alllength) {
                try {
                    File rfile = new File(mFile);
                    IOUtil.delete(rfile);
                    IOUtil.createDirByFile(rfile);
                    IOUtil.renameTo(tmpFile, rfile);
                    IOUtil.delete(new File(mFile + ".cfg"));
                    for (DownloadListener listener : listeners) {
                        if (listener != null) {
                            listener.onFinish(DownloadError.ERR_NONE);
                        }
                    }
                } catch (Exception e) {
                    for (DownloadListener listener : listeners) {
                        if (listener != null) {
                            listener.onFinish(DownloadError.ERR_OTHER);
                        }
                    }
                }
            } else {
                System.err.println("alllength is bad " + tmpFile.length() + "/" + alllength);
                for (DownloadListener listener : listeners) {
                    if (listener != null) {
                        listener.onFinish(DownloadError.ERR_FILE);
                    }
                }
            }
        }
        isDownloading = false;
    }

    private long getContentLength(String uri) {
        long length = 0;
        HttpRequest request = new HttpRequest(uri, 60 * 1000);
        request.setMethod(HttpRequest.GET);
        request.setCanRedirects(false);
        request.setDefaultAngent();
        HttpURLConnection connection = null;
        Map<String, String> datas = new HashMap<String, String>();
        datas.put(HttpUtil.HEADER_RANGE, "bytes=" + 1 + "-");
        try {
            connection = HttpUtil.connect(request, datas);
            int code = connection.getResponseCode();
            length = connection.getContentLength();
            if (code == HttpURLConnection.HTTP_PARTIAL) {
                length = -(length + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(connection);
        }
        return length;
    }
}
