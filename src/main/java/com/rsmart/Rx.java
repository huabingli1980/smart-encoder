package com.rsmart;

import java.util.List;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;
import com.impinj.octane.Tag;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Rx {

	private static final int sleepTimeBeforeBarcodeReading = 200;
	private static final int sleepTimeBeforeEpcWriting = 0;
	public static void main(String[] args) {
		Logger minaLogger = (Logger) LoggerFactory.getLogger("org.apache.mina");
		if (minaLogger != null) {
			minaLogger.setLevel(Level.WARN);
		}
		new Rx().start();
	}
	
	@Test
	public void testWrite(){
		Logger minaLogger = (Logger) LoggerFactory.getLogger("org.apache.mina");
		if (minaLogger != null) {
			minaLogger.setLevel(Level.WARN);
		}
		
		new Rx().start();
	}
	
	@SuppressWarnings("unchecked")
	public void start() {
		IBarcodeEpcMappable mapper = new SimpleBarcodeEpcMapper();
		final IReader rd = new SimpleReader();
		IContentValidate cv = new SimpleContentValidator();
		//System.out.println("Barcode ... " + barcode);
		final String epcToWrite = mapper.barcodeToEpc("");
		if (true && cv.validate(epcToWrite)) {
			//System.out.println("validating ... ");
			/*try {
				Thread.sleep(sleepTimeBeforeEpcWriting);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			long start = System.currentTimeMillis();
			rd.writeEpc(epcToWrite).done(new DoneCallback<String>() {

				public void onDone(final String epc) {
					System.out.println("Write done with EPC - " + epc + " within " + (System.currentTimeMillis() - start) + " ms");
					
					/*rd.readEpc().done(new DoneCallback<List<Tag>>() {

						@Override
						public void onDone(List<Tag> result) {
							String epcWritten = result.iterator().next().getEpc().toHexString();
							// TODO Auto-generated method stub
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
						
					});*/
					
				}
			}).fail(new FailCallback<String>() {

				@Override
				public void onFail(String arg0) {
					// TODO Auto-generated method stub
					System.out.println("Write error ... " + arg0);
				}
				
			});

		} else {
			System.out.println("Invalid epc!");
		}
		
		
		
		try {
			Thread.sleep(sleepTimeBeforeBarcodeReading);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/*new Rx().readBarcode(new IData() {

			IBarcodeEpcMappable mapper = new SimpleBarcodeEpcMapper();
			final IReader rd = new SimpleReader();
			IContentValidate cv = new SimpleContentValidator();

			@SuppressWarnings("unchecked")
			public void onSuccessData(String barcode) {
				
			}

			public void onReadFailed() {
				System.out.println("Barcode scanner scaned sth that is not a barcode ...");
			}
		});*/
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