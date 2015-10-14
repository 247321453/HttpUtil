package org.apache.http2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http2.utils.ByteUtils;
import org.apache.http2.utils.DownloadUtils;

public class DownloadMulti implements Runnable {
	static final int MAX_THREADS = 8;
	/** 最小分块k */
	static final long BLOCK_NUMBER = 384 * 1024;
	String url;
	String filepath;
	DownloadListener listener;
	ExecutorService executor;
	HttpClinetEx client;
	int threads;
	boolean[] status;
	long[][] infos;

	int mErr = DownloadUtils.ERR_OTHER;
	private static final byte[] lock = {};
	String mErrmsg;

	public DownloadMulti(String url, String file, int threads, HttpClinetEx client) {
		this(url, file, threads, client, null);
	}

	public DownloadMulti(String url, String file, int threads,
			HttpClinetEx client, DownloadListener listener) {
		this.url = url;
		this.filepath = file;
		this.listener = listener;
		this.client = client;
		this.threads = threads;
		if (threads > 0) {
			executor = Executors.newFixedThreadPool(threads);
		} else {
			executor = Executors.newFixedThreadPool(4);
		}
	}

	@Override
	public void run() {
		if (url == null || filepath == null) {
			return;
		}
		File file = DownloadUtils.getFile(filepath);
		DownloadUtils.createDirByFile(file);
		File cfgFile = new File(file.getAbsolutePath() + ".cfg");
		File tmpFile = new File(file.getAbsolutePath() + ".tmp");
		if (cfgFile.exists()) {
			infos = readBlocks(cfgFile.getAbsolutePath());
		} else {
			long length = DownloadUtils.getDownloadlength(client, url);
			if (length < 0) {
				//不支持断点续传
				length = -length;
				executor.submit(new Runnable() {
					@Override
					public void run() {
						long pos = 0;
						if (tmpFile.exists()) {
							pos = tmpFile.length();
						}
						boolean ok = DownloadUtils.download(client, url, tmpFile,
								pos, 0, listener);
						HttpClinetEx.log(url + ",download=" + ok);
						if (ok) {
							DownloadUtils.deleteFile(file);
							DownloadUtils.renameTo(tmpFile, file);
						}
					}
				});
				return;
			} else if (length == 0) {
				//无法访问
				listener.onFinish(DownloadUtils.ERR_OTHER, 0, "length==0");
				return;
			} else {
				//支持断点续传
				HttpClinetEx.log(url + ",length=" + length);
				//创建空白文件
//				try {
//					RandomAccessFile randomAccessFile = new RandomAccessFile(tmpFile, "rw");
//					randomAccessFile.setLength(length);
//					randomAccessFile.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				infos = createBlocks(cfgFile.getAbsolutePath(), length);
			}
		}
//		
		status = new boolean[infos.length];
		for (int i = 0; i < infos.length; i++) {
			if (infos[i][0] >= infos[i][1]) {
				continue;
			}
			final long[] b = infos[i];
			final int pos = i;
			status[pos] = true;
			executor.submit(new Runnable() {

				@Override
				public void run() {
					download(tmpFile, pos, b);
				}
			});
			//
		}
		//检查是否下载完成
		isCompleted();
	}

	private boolean isCompleted() {
		//有一个在下载都未完成
		for (int i = 0; i < status.length; i++) {
			if (status[i]) {
				//	System.out.println("wait:" + i);
				return false;
			}
		}
		for (int i = 0; i < infos.length; i++) {
			if (infos[i][0] >= infos[i][1]) {
				continue;
			} else {
				if (listener != null) {
					listener.onFinish(mErr, 0, mErrmsg);
				}
				return false;
			}
		}
		HttpClinetEx.log(url + ",download=true");
		File file = DownloadUtils.getFile(filepath);
		DownloadUtils.createDirByFile(file);
		File tmpFile = new File(file.getAbsolutePath() + ".tmp");
		File cfgFile = new File(file.getAbsolutePath() + ".cfg");
		DownloadUtils.deleteFile(file);
		DownloadUtils.renameTo(tmpFile, file);
		DownloadUtils.deleteFile(cfgFile);
		if (listener != null) {
			listener.onFinish(DownloadUtils.ERR_NONE, 0, null);
		}
		executor.shutdown();
		return true;
	}

	private void download(File tmpFile, final int index, long[] b) {
//		System.out.println(index + "=" + Arrays.toString(b));
		final long start = b[0];
		if (b[0] > b[1]) {
			return;
		}
		DownloadUtils.download(client, url, tmpFile, b[0], b[1], new DownloadListener() {

			@Override
			public void onStart(long pos, long length) {
				if (listener != null) {
					listener.onStart(pos, length);
				}
			}

			@Override
			public void onProgress(long pos) {
				if (listener != null) {
					listener.onProgress(pos);
				}
//				synchronized (lock) {
//					infos[index][0] = start + pos;
//				}
//				updateBlock(filepath + ".cfg", index, infos[index]);
			}

			@Override
			public void onFinish(int err, long size, String msg) {
				synchronized (lock) {
					status[index] = false;
					infos[index][0] = start + size;
				}
				mErr = err;
				mErrmsg = msg;
				if (isCompleted()) {
					//从剩下的找一个去下载
//					long[] b2 = new long[2];
//					final int i = findblock(infos, index + 1, b2);
//					if (i >= 0) {
//						download(tmpFile, i, b);
//					}
				} else {
					updateBlock(filepath + ".cfg", index, infos[index]);
				}
			}

			@Override
			public void onConnect(int code) {
				if (listener != null) {
					listener.onConnect(code);
				}
			}
		});
	}

	/***
	 * 查找下一块，如果为null则判断为下载完成
	 * @param blocks
	 * @param pos
	 * @return
	 */
	int findblock(long[][] blocks, int pos, long[] b) {
		int len = blocks.length;
		for (int i = pos; i < len; i++) {
			if (blocks[i][0] >= blocks[i][1] || status[i]) {
				continue;
			}
			b[0] = blocks[i][0];
			b[1] = blocks[i][1];
			return i;
		}
		for (int i = 0; i < pos; i++) {
			if (blocks[i][0] >= blocks[i][1] || status[i]) {
				continue;
			}
			b[0] = blocks[i][0];
			b[1] = blocks[i][1];
			return i;
		}
		return -1;
	}

	private static long[][] readBlocks(String file) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			//第一个长度，根据长度可以获取
			byte[] b = new byte[8];
			inputStream.read(b);
//			long length ByteUtils.bytesToLong(b);
			//第二个每块的长度
			inputStream.read(b);
			int blocks = (int) ByteUtils.bytesToLong(b);
			long[][] bs = new long[blocks][2];
			//剩下就是每块的进度/每块的大小
			for (int i = 0; i < blocks; i++) {
				inputStream.read(b);
				bs[i][0] = ByteUtils.bytesToLong(b);
				inputStream.read(b);
				bs[i][1] = ByteUtils.bytesToLong(b);
//				System.out.println("read:"+i+"="+Arrays.toString(bs[i]));
			}
			return bs;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			HttpClinetEx.close(inputStream);
		}
		return null;
	}

	private static void updateBlock(String file, int i, long[] b) {
		RandomAccessFile outputStream = null;
		try {
			outputStream = new RandomAccessFile(file, "rws");
			outputStream.seek((i * 2 + 2) * 8);
			outputStream.write(ByteUtils.longToBytes(b[0]));
//			outputStream.write(ByteUtils.longToBytes(b[1]));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			HttpClinetEx.close(outputStream);
		}
	}

	private static long[][] createBlocks(String file, long length) {
		//第一个长度，根据长度可以获取
		int blocks = (int) (length / BLOCK_NUMBER);
		if (length % BLOCK_NUMBER > 0) {
			blocks += 1;
		}
		long[][] bs = new long[blocks][2];
		//剩下就是每块的进度/每块的大小
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(ByteUtils.longToBytes(length), 0, 8);
			outputStream.write(ByteUtils.longToBytes(bs.length), 0, 8);
			for (int i = 0; i < blocks; i++) {
				if (i == blocks - 1) {
					bs[i][0] = i * BLOCK_NUMBER;
					bs[i][1] = length;
				} else {
					bs[i][0] = i * BLOCK_NUMBER;
					bs[i][1] = (i + 1) * BLOCK_NUMBER;
				}
//				System.out.println("write:"+i+"="+Arrays.toString(bs[i]));
				outputStream.write(ByteUtils.longToBytes(bs[i][0]), 0, 8);
				outputStream.write(ByteUtils.longToBytes(bs[i][1]), 0, 8);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			HttpClinetEx.close(outputStream);
		}
		return bs;
	}
}
