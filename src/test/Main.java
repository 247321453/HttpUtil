package test;

import com.k.http.DownloadError;
import com.k.http.DownloadListener;
import com.k.http.DownloadManager;

public class Main {

	public static void main(String[] args) {
		System.setProperty("http.agent",
				"Mozilla/5.0 (Linux; " + "Android 4.2.2; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) "
						+ "Chrome/18.0.1025.166 Safari/535.19");
		DownloadManager.init(8, 512 * 1024);
		DownloadManager.getInstance().download(
				"https://github.com/247321453/YgoServer/raw/master/lib/System.Data.SQLite.dll", 
				"D:\\a.dll", 
				new DefaultDownloadListener());
		// new
		// DownloadSingle("http://bcscdn.baidu.com/netdisk/BaiduYunGuanjia_5.3.4.exe",
		// "D:\\BaiduYunGuanjia_5.3.4_single.exe", new HttpClinetEx(),
		// new DefaultDownloadListener()).run();
	}

	static class DefaultDownloadListener implements DownloadListener {
		long t1 = 0;

		public DefaultDownloadListener() {
			t1 = System.currentTimeMillis();
		}

		@Override
		public void onStart(String url, String file) {
			
		}

		@Override
		public void onProgress(String url, String file, long pos, long total, boolean writed) {
		}

		@Override
		public void onFinish(String url, String file, DownloadError err) {
			System.out.println((System.currentTimeMillis() - t1) + "," + err + "：" + err);			
		}
	};
}
