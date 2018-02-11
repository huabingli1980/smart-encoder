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
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}

	public void serialEvent(final SerialPortEvent event) {
		
		System.out.println("in....");
		/*if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
			return;
		}*/

		final byte[] newData = new byte[this.comPort.bytesAvailable()];
		final int numRead = this.comPort.readBytes(newData, (long) newData.length);
		final String dataStr = new String(newData);
		//System.out.println(dataStr);
		if (numRead <= 6) {
			this.mdeferred.onReadFailed();
		} else {
			try {
				this.mdeferred.onSuccessData(dataStr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
