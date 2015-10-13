package crypto;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import org.apache.http2.utils.ByteUtils;

public class MD5Utils {

	public static String getStringSHA(String val) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			sha.update(val.getBytes());
			byte[] m = sha.digest();// 加密
			// return getshaString(m);
			return ByteUtils.toHexString(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getMD5ByString(String source) {
		String hash = null;
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(
					source.getBytes());
			hash = getMD5ByStream(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}

	/**
	 * 获取文件的MD5
	 * */
	public String getMD5ByFile(String file) {
		String hash = null;
		try {
			FileInputStream in = new FileInputStream(file);
			hash = getMD5ByStream(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}

	private String getMD5ByStream(InputStream stream) {
		String hash = null;
		byte[] buffer = new byte[1024];
		BufferedInputStream in = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			in = new BufferedInputStream(stream);
			int numRead = 0;
			while ((numRead = in.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}
			in.close();
			hash = ByteUtils.toHexString(md5.digest());
		} catch (Exception e) {
			if (in != null)
				try {
					in.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			e.printStackTrace();
		}
		return hash;
	}
}
