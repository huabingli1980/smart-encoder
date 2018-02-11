package com.rsmart;

import com.fazecast.jSerialComm.SerialPort;
import com.impinj.octane.OctaneSdkException;

public class Rx2 {

	public static void main(String[] args) throws OctaneSdkException {
		/*System.setSecurityManager(null);
		ReaderHandler.disableLog();
		String valueOf = String.valueOf(System.currentTimeMillis()+54321);
		String epcToWrite = 
					"00B07A135403A988"
						+ valueOf.substring(valueOf.length() - 8);
		
		ReaderHandler.currentEpc = epcToWrite;*/
		//ReaderHandler rh = new ReaderHandler();
		//rh.writeIt();
		
		new Rx2().readBarcode(new IData() {
			
			@Override
			public void onSuccessData(String p0) throws OctaneSdkException {
				System.out.println("Receive barcode - " + p0);
				String valueOf = String.valueOf(System.currentTimeMillis()+54321);
				String epcToWrite = 
							"00B07A135403A988"
								+ valueOf.substring(valueOf.length() - 8);
				
				ReaderM.currentEpc = epcToWrite;
				ReaderM rh = new ReaderM();
				rh.writeEpcAndLock();
			}
			
			@Override
			public void onReadFailed() {
				// TODO Auto-generated method stub
				System.out.println("failed");
			}
		});
	}

	private void readBarcode(IData mdeferred) {

		final SerialPort comPort = SerialPort.getCommPort("com3");
		comPort.setBaudRate(115200);
		//comPort.setParity(0);
		comPort.setNumDataBits(8);
		comPort.setNumStopBits(1);

		boolean openOk = comPort.openPort();
		if(!openOk){
			throw new RuntimeException("Cant open port");
		}
		
		BarcodeListener barcodeListener = new BarcodeListener(comPort, mdeferred);
		comPort.addDataListener(barcodeListener);
	}

}