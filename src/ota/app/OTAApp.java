package ota.app;

import java.io.IOException;
import java.io.OutputStream;

import tijos.framework.appcenter.TiAPP;
import tijos.framework.appcenter.TiAPPManager;
import tijos.framework.networkcenter.http.HttpClient;
import tijos.framework.networkcenter.http.HttpMessage;
import tijos.framework.networkcenter.http.IHttpMessageListener;
import tijos.framework.util.SyncObject;
import tijos.framework.util.logging.Logger;

public class OTAApp implements IHttpMessageListener {

	OutputStream appOutStream = null;

	int originalAppId = -1;

	SyncObject sync = new SyncObject();

	OTAProgress otaProgress;

	int otaResult = 0;
	String otaResultMsg = "";

	public OTAApp(int oldAppId) {
		this.originalAppId = oldAppId;
	}

	/**
	 * 等待结果
	 * 
	 * @param timeout
	 * @throws OTAException
	 */
	public int waitResult(int timeout) {
		long duration = this.sync.await(timeout);

		Logger.info("ota", "time " + duration);
		// timeout
		if (duration <= 0) {
			return OTAException.ERROR_TIMEOUT;
		}

		return this.otaResult;
	}
	
	/**
	 * 升级结果
	 * @return
	 */
	public String getResultMessage() {
		 return this.otaResultMsg;
	}

	/**
	 * create an application
	 * 
	 * @param fileSize
	 * @throws OTAException
	 * @throws IOException
	 */
	private void createApp(int fileSize) throws OTAException {

		Logger.info("ota", "enter createApp file size " + fileSize);

		try {
			TiAPPManager manager = TiAPPManager.getInstance();

			if (fileSize <= 0 || fileSize > manager.getFreeSize())
				throw new IllegalArgumentException("the APP size error = " + fileSize);

			otaProgress = new OTAProgress(fileSize);

			appOutStream = manager.create(0, fileSize);

		} catch (Exception ex) {
			throw new OTAException(OTAException.ERROR_CREATE_APP, ex.getMessage());
		}
	}

	/**
	 * 更新中
	 * 
	 * @param payload
	 */
	private void updateApp(byte[] payload) throws OTAException {

		Logger.info("ota", "enter update, payload size " + (payload == null ? 0 : payload.length));

		try {
			if (payload != null && payload.length > 0) {
				appOutStream.write(payload);
				this.otaProgress.updateOtaProgress(payload.length);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new OTAException(OTAException.ERROR_UPDATE_APP, ex.getMessage());
		}

	}

	/**
	 * 完成并通知调用者
	 * 
	 * @param error
	 */
	private void finish(int error, String errMsg) {

		Logger.info("ota", "enter finish, error " + error + " " + errMsg);

		// release stream
		if (appOutStream != null) {
			try {
				appOutStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			appOutStream = null;
		}

		this.otaResult = error;
		this.otaResultMsg = errMsg;

		// finish notify
		this.sync.trigger();
	}

	/**
	 * OTA 结束 激活新应用，删除旧应用
	 * 
	 * @throws OTAException
	 */
	private void otaComplete() throws OTAException {
		// remove the original application
		try {
			Logger.info("ota", "OTA finished.");

			this.appOutStream.close();
			this.appOutStream = null;

			// set the new app to auto run
			TiAPP app = TiAPPManager.getInstance().activate(0);
			app.enableAutorun();

			Logger.info("ota", "Active the new app and set it to autorun.");

			// ignore the old app
			if (originalAppId != -1) {
				app = TiAPPManager.getInstance().getAPP(originalAppId);
				if (app != null)
					app.delete();
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new OTAException(OTAException.ERROR_DELETE_APP, e.getMessage());
		}

	}

	@Override
	public void onGetResponseArrived(HttpMessage httpMessage) {

		Logger.info("ota", "onGetResponseArrived result " + httpMessage.result + " status " + httpMessage.statusCode
				+ " seq " + httpMessage.sequence + " payload len " + httpMessage.payload.length);

		try {
			if (httpMessage.result < 0) {
				throw new OTAException(OTAException.ERROR_HTTP, "http: " + httpMessage.result);
			}

			// http error
			if (httpMessage.statusCode != 200) {
				throw new OTAException(OTAException.ERROR_HTTP, "code " + httpMessage.statusCode);
			}

			// 1st packet, create application
			if (httpMessage.sequence == 0) {
				createApp(httpMessage.response_len);
			}

			// update application
			this.updateApp(httpMessage.payload);

			// finish
			if (httpMessage.result == HttpClient.ERROR_OK) {

				this.otaComplete();

				this.finish(OTAException.ERROR_OK, "OK");
			}

		} catch (OTAException ex) {
			this.finish(ex.getCode(), ex.getMessage());
		}

	}

	@Override
	public void onPostResponseArrvied(HttpMessage arg0) {
	}

	@Override
	public void onPutResponseArrived(HttpMessage arg0) {
	}

	@Override
	public void onDeleteResponseArrived(HttpMessage arg0) {
	}

}
