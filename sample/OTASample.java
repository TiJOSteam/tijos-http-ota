
import java.io.IOException;

import tijos.framework.appcenter.TiAPP;
import tijos.framework.appcenter.TiAPPManager;


public static void main(String[] args) {

		try {

			//获取当前应用ID
			TiAPP currApp = TiAPPManager.getInstance().getRunningAPP();
			int oldAppId = currApp.getId();

			//预置OTA 应用名称
			String otaAppName = "tijos-http-ota";

			//获取OTA应用
			TiAPP otaApp = TiAPPManager.getInstance().getAPP(otaAppName);
			if (otaApp == null) {
				throw new IOException("OTA App is not found: " + otaAppName);
			}

			String otaAppUrl = "http://img.tijos.net/img/tiwl-aep.tapk";
			//应用参数
			String appArgs = oldAppId + " " + otaAppUrl;
			
			//启动OTA应用
			otaApp.execute(true, appArgs);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}