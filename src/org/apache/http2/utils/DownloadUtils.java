package org.apache.http2.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http2.DownloadListener;
import org.apache.http2.HttpClinetEx;
import org.apache.http2.HttpRequest;

public class DownloadUtils {
	public static final int ERR_NONE = 0;
	public static final int ERR_NETWORK_OR_FILE = ERR_NONE + 1;
	public static final int ERR_SERVER = ERR_NONE + 2;
	public static final int ERR_OTHER = ERR_NONE + 3;

	/**
	 * 负数，则不支持断点续传
	 * @param request
	 * @return
	 */
	public static long getDownloadlength(HttpClinetEx client, String url) {
		HttpURLConnection url_con = null;
		long length = 0;
		try {
			Map<String, String> args = new HashMap<String, String>();
			args.put("Connection", "Keep-Alive");
			args.put("Range", "bytes=" + 1 + "-");
			HttpRequest request = client.getHttpRequestByDefault(url);
			request.setNeedContent(false);
			url_con = client.connect(request, args);
			int code = url_con.getResponseCode();
			length = url_con.getContentLengthLong();
			if (code == HttpURLConnection.HTTP_PARTIAL) {
				length += 1;
			} else {
				length = -length;
			}
		} catch (IOException e) {
		} catch (Exception e) {
		} finally {
			HttpClinetEx.close(url_con);
		}
		return length;
	}

	public static boolean download(HttpClinetEx client, String url, File file,
			long pos, long length, DownloadListener listener) {
		if (url == null || file == null || file.isDirectory()) {
			return false;
		}
		Map<String, String> args = new HashMap<String, String>();
		args.put("Connection", "Keep-Alive");
		if (pos > 0) {
			if (length > 0) {
				args.put("Range", "bytes=" + pos + "-" + length);
			} else {
				args.put("Range", "bytes=" + pos + "-");
			}
		}
		boolean connect = false;
		HttpURLConnection url_con = null;
		InputStream inputStream = null;
		RandomAccessFile outputStream = null;
		long compeleteSize = 0;
		try {
			HttpRequest request = client.getHttpRequestByDefault(url);
			url_con = client.connect(request, args);
			int code = url_con.getResponseCode();
			if (listener != null) {
				listener.onConnect(code);
			}
			if (pos > 0 && code == HttpURLConnection.HTTP_OK) {
				//不支持断点续传
				pos = 0;
				HttpClinetEx.log("don't resume.");
				args.remove("Range");
				url_con.disconnect();
				url_con = HttpClinetEx.getInstance().connect(request, args);
				code = url_con.getResponseCode();
				if (listener != null) {
					listener.onConnect(code);
				}
			}
			if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_PARTIAL) {
				long size = url_con.getContentLengthLong();
				if (request.savecookies) {
					Map<String, List<String>> headers = url_con.getHeaderFields();
					HttpClinetEx.log("headers:" + headers);
					CookiesUtils.updateCookies(request.getInitUrl(), headers);
				}
				inputStream = url_con.getInputStream();
				if (listener != null) {
					listener.onStart(pos, size);
				}
				if (!file.exists()) {
					file.createNewFile();
				}
				outputStream = new RandomAccessFile(file, "rw");
				//指定大小的空白文件
				//outputStream.setLength(size);
				HttpClinetEx.log("resume="+(pos > 0)+",seek:" + pos);
				if (pos > 0) {
					outputStream.seek(pos);
				}
				byte[] data = new byte[8192];
				int len = 0;
				while ((len = inputStream.read(data)) != -1) {
					outputStream.write(data, 0, len);
					compeleteSize += len;
					if (listener != null) {
						listener.onProgress(compeleteSize);
					}
				}
				connect = true;
			} else {
				HttpClinetEx.log("err:" + code);
				if (listener != null) {
					listener.onFinish(ERR_SERVER, compeleteSize, "code=" + code);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onFinish(ERR_NETWORK_OR_FILE, compeleteSize, e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onFinish(ERR_OTHER, compeleteSize, e.getMessage());
			}
		} finally {
			HttpClinetEx.close(outputStream);
			HttpClinetEx.close(inputStream);
			HttpClinetEx.close(url_con);
		}
		if (connect) {
			if (listener != null) {
				listener.onFinish(ERR_NONE, compeleteSize, null);
			}
		}
		return connect;
	}

	public static File getFile(String path) {
		File file = null;
		try {
			file = new File(path);
		} catch (Exception e) {

		}
		return file;
	}

	public static void deleteFile(File file) {
		try {
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void renameTo(File file, File newFile) {
		try {
			file.renameTo(newFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createDirByFile(File file) {
		if (file == null) {
			return;
		}
		File pdir = file.getParentFile();
		if (pdir != null && !pdir.exists()) {
			try {
				pdir.mkdirs();
			} catch (Exception e) {

			}
		}
	}

}
