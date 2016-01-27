package com.k.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import com.k.http.util.ByteUtils;
import com.k.http.util.FileUtil;

public class DownloadInfo {

	private static final int HEAD_LENGTH = 3 * 8;
	private static final long CHACHE_SIZE = 512 * 1024;

	public DownloadInfo(String cfgfile) {
		this(cfgfile, CHACHE_SIZE);
	}

	public DownloadInfo(String cfgfile, long cache_size) {
		this.cfgfile = cfgfile;
		this.cache_size = cache_size;
	}

	private String cfgfile;
	private volatile long length;
	private volatile long cache_size;
	private volatile int block_count;
	private volatile long[][] blocks;
	private volatile boolean[] status;

	public int getBlockCount() {
		return block_count;
	}

	public long getLength() {
		return length;
	}

	public long getCacheSize() {
		return cache_size;
	}

	public boolean isDownload(int i) {
		boolean b = false;
		if (i >= 0 && i < status.length)
			b = status[i];
		return b;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public void setCache(long cache_size) {
		this.cache_size = cache_size;
	}

	@Override
	public String toString() {
		return "DownloadInfo [cfgfile=" + cfgfile + ", length=" + length + ", cache_size=" + cache_size
				+ ", block_count=" + block_count + "]";
	}

	/***
	 * 查找下一块，如果为null则判断为下载完成
	 * 
	 * @param blocks
	 * @param pos
	 * @return
	 */
	public int findblock(int pos, long[] b) {
		if (blocks == null) {
			System.err.println("blocks is null");
			return -1;
		}
		for (int i = pos; i < block_count; i++) {
			b[0] = blocks[i][0];
			b[1] = blocks[i][1];
			if (b[0] >= b[1]) {
				continue;
			}
			return i;
		}
		for (int i = 0; i < pos; i++) {
			b[0] = blocks[i][0];
			b[1] = blocks[i][1];
			if (b[0] >= b[1]) {
				continue;
			}
			return i;
		}
		return -1;
	}

	public boolean isOk() {
		return findblock(0, new long[2]) < 0;
	}

	public void updateStatu(int i, boolean s) {
		if (i >= 0 && i < status.length)
			status[i] = s;
	}

	public void readBlock(int i) {
		RandomAccessFile outputStream = null;
		try {
			outputStream = new RandomAccessFile(cfgfile, "rws");
			outputStream.seek((i * 2) * 8 + HEAD_LENGTH);
			byte[] tmp = new byte[8];
			outputStream.read(tmp);
			blocks[i][0] = ByteUtils.bytesToLong(tmp);
			outputStream.read(tmp);
			blocks[i][1] = ByteUtils.bytesToLong(tmp);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.close(outputStream);
		}
	}

	public void updateBlock(int i, long[] b) {
		RandomAccessFile outputStream = null;
		try {
			blocks[i][0] = b[0];
			blocks[i][1] = b[1];
			outputStream = new RandomAccessFile(cfgfile, "rws");
			outputStream.seek((i * 2) * 8 + HEAD_LENGTH);
			outputStream.write(ByteUtils.longToBytes(b[0]));
			outputStream.write(ByteUtils.longToBytes(b[1]));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.close(outputStream);
		}
	}

	public boolean read() {
		boolean ok = false;
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(cfgfile);
			// 第一个长度，根据长度可以获取
			byte[] b = new byte[8];
			// 总长度
			inputStream.read(b);
			length = ByteUtils.bytesToLong(b);
			// 缓存大小
			inputStream.read(b);
			cache_size = ByteUtils.bytesToLong(b);
			// 缓存数量
			inputStream.read(b);
			block_count = (int) ByteUtils.bytesToLong(b);
			status = new boolean[block_count];
			blocks = new long[block_count][2];
			// 剩下就是每块的进度/每块的大小
			for (int i = 0; i < block_count; i++) {
				inputStream.read(b);
				blocks[i][0] = ByteUtils.bytesToLong(b);
				inputStream.read(b);
				blocks[i][1] = ByteUtils.bytesToLong(b);
			}
			ok = true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			FileUtil.close(inputStream);
		}
		return ok;
	}

	public boolean createOrRead() {
		if (FileUtil.exists(cfgfile)) {
			if (read()) {
				return true;
			}
		}
		createNew();
		return false;
	}

	public void createNew() {
		FileUtil.delete(new File(cfgfile));
		// 第一个长度，根据长度可以获取
		if (cache_size <= 0) {
			block_count = 1;
			cache_size = length;
		} else {
			block_count = (int) (length / cache_size);
			if (length % cache_size > 0) {
				block_count++;
			}
		}
		status = new boolean[block_count];
		blocks = new long[block_count][2];
		// 剩下就是每块的进度/每块的大小
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(cfgfile);
			// 总长度
			outputStream.write(ByteUtils.longToBytes(length), 0, 8);
			// 缓存大小
			outputStream.write(ByteUtils.longToBytes(cache_size), 0, 8);
			// 缓存数量
			outputStream.write(ByteUtils.longToBytes(block_count), 0, 8);
			for (int i = 0; i < block_count; i++) {
				if (i == block_count - 1) {
					blocks[i][0] = i * cache_size;
					blocks[i][1] = length;
				} else {
					blocks[i][0] = i * cache_size;
					blocks[i][1] = (i + 1) * cache_size;
				}
				// System.out.println("write:"+i+"="+Arrays.toString(bs[i]));
				outputStream.write(ByteUtils.longToBytes(blocks[i][0]), 0, 8);
				outputStream.write(ByteUtils.longToBytes(blocks[i][1]), 0, 8);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.close(outputStream);
		}
	}
}