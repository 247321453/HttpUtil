package com.k.http.util;

import java.io.Closeable;
import java.io.File;
import java.net.HttpURLConnection;

public class FileUtil {

	public static File getFile(String path) {
		File file = null;
		try {
			file = new File(path);
		} catch (Exception e) {

		}
		return file;
	}

	public static boolean exists(String file) {
		boolean b = false;
		try {
			b = new File(file).exists();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}

	public static void delete(File file) {
		try {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) {
					delete(f);
				}
			}
			file.delete();
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

	public static void close(HttpURLConnection conn) {
		if (conn != null) {
			conn.disconnect();
		}
	}

	public static void close(Closeable close) {
		if (close == null) {
			return;
		}
		try {
			close.close();
		} catch (Exception e) {

		}
	}
}
