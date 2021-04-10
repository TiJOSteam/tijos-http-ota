package ota.app;
import java.io.IOException;

public class OTAProgress {

	/**
	 * current position
	 */
	int position = 0;

	/**
	 * OTA Progress
	 */
	int otaProgressNotify = 20;

	/**
	 * Firmware size
	 */
	int firmwareSize = 0;
	
	public OTAProgress(int totalSize)
	{
		this.firmwareSize = totalSize;
	}
	
	/**
	 * Report progress of OTA upgrade
	 * 
	 * @param payloadSize the current packet size
	 * @throws IOException
	 */
	public void updateOtaProgress(int payloadSize) throws IOException {

		this.position += payloadSize;
		
		int progress = (int) ((position / (float) this.firmwareSize) * 100);

		System.out.println("ota progress " + progress);

		if (progress < otaProgressNotify) {
			return;
		}

		otaProgressNotify += 20;
	}

	
}
