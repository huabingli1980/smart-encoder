package com.rsmart;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import com.fazecast.jSerialComm.SerialPort;

public class Rx2 {

	public static void main(String[] args) {

		new Rx2().readBarcode(new IData() {
			
			@Override
			public void onSuccessData(String p0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onReadFailed() {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private void readBarcode(IData mdeferred) {

		final SerialPort comPort = SerialPort.getCommPort("com3");
		comPort.setBaudRate(115200);
		comPort.setParity(1);
		comPort.setNumDataBits(8);
		comPort.setNumStopBits(1);

		comPort.openPort();
		BarcodeListener barcodeListener = new BarcodeListener(comPort, mdeferred);
		comPort.addDataListener(barcodeListener);
	}

}