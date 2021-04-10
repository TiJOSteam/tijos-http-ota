package ota.app;

public class OTAException extends Exception {

	/**
	 * 成功
	 */
	public static final int ERROR_OK = 0;
	/**
	 * HTTP 通讯错误
	 */
	public static final int ERROR_HTTP = -1;
	/**
	 * OTA超时
	 */
	public static final int ERROR_TIMEOUT = -2;
	/**
	 * 创建应用失败
	 */
	public static final int ERROR_CREATE_APP = -3;
	/**
	 * 应用更新失败
	 */
	public static final int ERROR_UPDATE_APP = -4;
	/**
	 * 旧应用删除失败
	 */
	public static final int ERROR_DELETE_APP = -5;

	public static final int ERROR_UNKNOWN  = -10;

	int code = 0;
	
	public OTAException(int code) {
		this.code = code;		
	}
	
	public OTAException(int code, String message) {
		super(message);
		this.code = code;		
	}
	
	public int getCode() {
		return this.code;
	}
	
}
