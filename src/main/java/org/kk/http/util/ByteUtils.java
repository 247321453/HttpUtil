package org.kk.http.util;

import java.nio.ByteBuffer;

public class ByteUtils {

	private static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /***
     * byte数组转十六进制
     * @param b 数组
     * @return 十六进制
     */
	public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}

    /**
     *
     * @param b 4位byte数组
     * @return int
     */
	public static int bytesToInt(byte[] b) {
		return b[3] & 0xFF |
				(b[2] & 0xFF) << 8 |
				(b[1] & 0xFF) << 16 |
				(b[0] & 0xFF) << 24;
	}

    /**
     *
     * @param a 数字
     * @return 4位byte数组
     */
	public static byte[] intToBytes(int a) {
		return new byte[] {
				(byte) ((a >> 24) & 0xFF),
				(byte) ((a >> 16) & 0xFF),
				(byte) ((a >> 8) & 0xFF),
				(byte) (a & 0xFF)
		};
	}

    /**
     *
     * @param x long
     * @return 8位byte数组
     */
	public static byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(0, x);
		return buffer.array();
	}

    /**
     *
     * @param bytes 8位byte数组
     * @return long
     */
	public static long bytesToLong(byte[] bytes) {
		long l = 0;
		try {
			ByteBuffer buffer = ByteBuffer.allocate(8);
			buffer.put(bytes, 0, bytes.length);
			buffer.flip();//need flip
			l = buffer.getLong();
		} catch (Exception e) {

		}
		return l;
	}
}
