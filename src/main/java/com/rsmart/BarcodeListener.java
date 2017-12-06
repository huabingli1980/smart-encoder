package com.rsmart;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

class BarcodeListener implements SerialPortDataListener {
	SerialPort comPort;
	IData mdeferred;

	BarcodeListener(final SerialPort comPort, final IData mdeferred2) {
		this.comPort = comPort;
		this.mdeferred = mdeferred2;
	}

	public int getListeningEvents() {
		return 1;
	}

	public void serialEvent(final SerialPortEvent event) {
		if (event.getEventType() != 1) {
			return;
		}

		final byte[] newData = new byte[this.comPort.bytesAvailable()];
		final int numRead = this.comPort.readBytes(newData, (long) newData.length);
		final String dataStr = new String(newData);
		if (numRead <= 6) {
			this.mdeferred.onReadFailed();
		} else {
			this.mdeferred.onSuccessData(dataStr);
		}
	}
}
