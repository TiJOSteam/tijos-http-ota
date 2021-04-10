package ota.http;

import java.io.IOException;

import ota.app.OTAApp;
import tijos.framework.networkcenter.http.HttpClient;
import tijos.framework.platform.TiPower;
import tijos.framework.platform.network.NetworkInterfaceManager;

public class OTAHttp {

	private static final int OTA_TIMEOUT = 1000 * 60 * 1; // max 1 minutes

	OTAApp otaApp;

	HttpClient httpClient = HttpClient.getInstance();

	/**
	 * start fetch application and write to local application
	 * 
	 * @throws IOException
	 */
	public void start(String serverUrl, int oldAppId) throws IOException {

		System.out.println("enter start");

		this.otaApp = new OTAApp(oldAppId);

		this.httpClient.start(otaApp);
		this.httpClient.get(serverUrl, HttpClient.APPLICATION_OCTET_STREAM);
	}

	/**
	 * 等待结果
	 * 
	 * @return
	 */
	public int waitResult() {
		int result = this.otaApp.waitResult(OTA_TIMEOUT);

		// cancel requests in progress
		this.httpClient.cancel();

		return result;
	}

	/**
	 * 获取结果字符串
	 */
	public String getResultMessage() {
		return this.otaApp.getResultMessage();
	}

	/**
	 * 释放资源
	 * 
	 * @param timeOut
	 */
	public void release(int timeOut) {
		try {
			this.httpClient.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		OTAHttp otaHttp = null;

		try {			
			NetworkInterfaceManager.getInstance().startup(30);

			System.out.println("TiJOS OTA Application(HTTP) V1.0");

			System.out.println("Parameters: ");
			for (String param : args) {
				System.out.println(param);
			}

			int oldAppId = Integer.parseInt(args[0]);
			String serverUrl = args[1];

			otaHttp = new OTAHttp();

			// Create application and start connect to the server
			otaHttp.start(serverUrl, oldAppId);

			// Wait result (1 minutes)
			int err = otaHttp.waitResult();

			System.out.println("OTA result = " + err + " " + otaHttp.getResultMessage());

		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {

			// 重新启动
			try {
				// Wait finish and release connection
				if (otaHttp != null)
					otaHttp.release(20);

				TiPower.getInstance().reboot(0);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
