package test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http2.DownloadListener;
import org.apache.http2.DownloadMulti;
import org.apache.http2.HttpClinetEx;

import crypto.DESUtils;

public class Main {

	static void test() {
		String teString = "            <tr>"
				+ "                <td>112.193.142.236</td>"
				+ "                <td>8090</td>"
				+ "                <td>高匿名</td>";
		Pattern pattern = Pattern.compile("<tr>\\s+<td>([0-9|.]+?)</td>\\s+<td>([0-9]+?)</td>");
		// System.out.println(html);
		Matcher m = pattern.matcher(teString);
		if (m.find()) {
			System.out.println(m.group(1) + ":" + m.group(2));
		} else {
			System.out.println("fail");
		}
	}

	public static void main(String[] args) {
		System.setProperty("http.agent", "Mozilla/5.0 (Linux; "
				+ "Android 4.2.2; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) "
				+ "Chrome/18.0.1025.166 Safari/535.19");
		testmultidownload();
	}

	static void testmultidownload() {
		new DownloadMulti("http://bcscdn.baidu.com/netdisk/BaiduYunGuanjia_5.3.4.exe",
				"D:\\BaiduYunGuanjia_5.3.4.exe", 4, new HttpClinetEx(), new DownloadListener() {

					@Override
					public void onStart(long pos, long length) {

					}

					@Override
					public void onProgress(long pos) {

					}

					@Override
					public void onFinish(int err, String msg) {
						System.out.println(err + "\n" + msg);
					}

					@Override
					public void onConnect(int code) {

					}
				}).run();
	}

}
