package com.rsmart;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import com.fazecast.jSerialComm.SerialPort;

public class Rx {

	public static void main(String[] args) {

		new Rx().readBarcode(new IData() {

			IBarcodeEpcMappable mapper = new SimpleBarcodeEpcMapper();
			final IReader rd = new SimpleReader();
			IContentValidate cv = new SimpleContentValidator();

			@SuppressWarnings("unchecked")
			public void onSuccessData(String barcode) {
				final String epcToWrite = mapper.barcodeToEpc(barcode);
				if (cv.validate(epcToWrite)) {
					rd.writeEpc(epcToWrite).done(new DoneCallback<String>() {

						public void onDone(final String epc) {
							
							rd.readEpc().done(new DoneCallback<String>() {

								public void onDone(final String epcWritten) {
									
									System.out.println(epcWritten);
									if (epcToWrite.equals(epcWritten)) {

										new CountTryer(new Executor() {

											public void execute(final CountTryer me) {
												System.out.println("reading margin...");
												rd.readMargin(epcWritten).done(new DoneCallback<Integer>() {
													public void onDone(Integer arg0) {
														me.onSuccess();
													}
												}).fail(new FailCallback<Integer>() {

													public void onFail(Integer errorCode) {
														if (errorCode == 1) {
															System.out.println("Insufficient power retrying...");
														} else if (errorCode == 2) {
															System.out.println("Failure");
														}
													}

												});
											}

										}).time(5).start();

									} else {
										System.out.println("Wrong epc");
									}
								}

							}).fail(new FailCallback<String>() {

								@Override
								public void onFail(String msg) {
									System.out.println(msg);
								}
							});
						}
					});

				} else {
					System.out.println("Invalid epc!");
				}
			}

			public void onReadFailed() {
				System.out.println("Barcode scanner scaned sth that is not a barcode ...");
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