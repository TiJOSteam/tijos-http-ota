
HTTP OTA 应用

HTTP OTA应用支持通过HTTP URL的方式下载指定位置的tapk应用文件并更新到支持钛极OS的设备中达到OTA的目的


使用方式：

1. 在设备中预置tijos-http-ota应用
2. 在代码中加入OTA处理逻辑： 设备收到平台下发OTA升级指令，指令中可包含升级所需要的URL以及其它信息
3. 在代码中使用参数如下执行tijos-http-ota应用：
	oldAppId - 旧应用ID, 升级成功后旧应用将被删除， 如果为-1，则不删除原有应用 
	serverUrl - 新版本应用文件 http url
	
	 

