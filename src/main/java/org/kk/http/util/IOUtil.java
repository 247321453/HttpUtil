package org.kk.http.util;

import java.io.Closeable;
import java.io.File;
import java.net.HttpURLConnection;

/***
 * 文件操作
 */
public class IOUtil {

    /***
     * 文件对象
     * @param path 路径
     * @return 文件
     */
	public static File getFile(String path) {
		File file = null;
		try {
			file = new File(path);
		} catch (Exception e) {

		}
		return file;
	}

    /***
     * @param file 路径
     * @return 是否存在
     */
	public static boolean exists(String file) {
		boolean b = false;
		try {
			b = new File(file).exists();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}

    /***
     * 删除
     * @param file 文件/目录
     */
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

    /***
     * 重命名
     * @param file 原路径
     * @param newFile 新路径
     */
	public static void renameTo(File file, File newFile) {
		try {
			file.renameTo(newFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * 根据文件创建所在目录
     * @param file 文件
     */
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

    /***
     * 关闭
     * @param conn url连接
     */
	public static void close(HttpURLConnection conn) {
		if (conn != null) {
			conn.disconnect();
		}
	}

    /***
     * 关闭
     * @param close 流
     */
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
