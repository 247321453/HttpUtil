package test;

import org.kk.http.DownloadListener;
import org.kk.http.bean.DownloadError;
import org.kk.http.util.UriUtils;

public class Main {

	public static void main(String[] args) {
		System.setProperty("http.agent",
				"Mozilla/5.0 (Linux; " + "Android 4.2.2; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) "
						+ "Chrome/18.0.1025.166 Safari/535.19");
//		DownloadManager.init(8, 512 * 1024);
//		DownloadManager.getInstance().download(
//				"https://github.com/247321453/YgoServer/raw/master/lib/System.Data.SQLite.dll",
//				"D:\\a.dll",
//				new DefaultDownloadListener());
        System.out.print(UriUtils.removeQuerys("http://127.0.0.1/index.php?a=b"));
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
        public void onStart(float pos, long length) {

        }

        @Override
        public void onProgress(float progress) {

        }

        @Override
        public void onFinish(DownloadError err) {

        }
    };
}
