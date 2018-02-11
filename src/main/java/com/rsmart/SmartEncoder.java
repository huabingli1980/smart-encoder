
package com.rsmart;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.sqlite.utils.ReaderManager;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SmartEncoder {

	private static final int sleepTimeBeforeBarcodeReading = 200;
	private static final int sleepTimeBeforeEpcWriting = 0;
	protected static final long TIMEOUT_MS = 680;
	public static int count;
	static IBarcodeEpcMappable mapper = new SimpleBarcodeEpcMapper();
	IContentValidate preWriteChecker = new SimpleContentValidator();
	public static BlockingDeque<String> bq = new LinkedBlockingDeque<>();
	
	static Queue<String> epcs = new LinkedList<String>();
	IReader reader;
	
	public SmartEncoder(){
		try {
			reader = new SimpleReader();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger minaLogger = (Logger) LoggerFactory.getLogger("org.apache.mina");
		if (minaLogger != null) {
			minaLogger.setLevel(Level.WARN);
		}
		System.out.println("<!!! Start encoding using timeout - " + TIMEOUT_MS + " ms !!!>");
		
		SmartEncoder smartEncoder = new SmartEncoder();
	
		
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
	
		for (int i = 0; i < 200; i++) {
			String valueOf = String.valueOf(System.currentTimeMillis()+54321);
			String testdata = "00B07A135403A988" + valueOf.substring(valueOf.length()-8);
			final String epcToWrite = testdata;//mapper.barcodeToEpc("");
			
			Thread.sleep(TIMEOUT_MS);
			
			/*smartEncoder.readBarcode(new IData() {

				@SuppressWarnings("unchecked")
				public void onSuccessData(String barcode) throws Exception {
					String epcToWrite = mapper.barcodeToEpc(barcode);
					smartEncoder.startEncode(epcToWrite);
				}
	
				public void onReadFailed() {
					System.out.println("Barcode scanner scaned sth that is not a barcode ...");
				}
			});*/
			SmartEncoder.count++;
			smartEncoder.startEncode(epcToWrite);
		}	
		
		
		
		//smartEncoder.startEncode(epcToWrite);
	}

	private void startEncode(String epcToWrite) throws Exception{
		if(epcs.isEmpty()){
			epcs.add(epcToWrite);
		}
		execute(epcs.poll());
	}
	
	private void execute(String epcToWrite) throws Exception {
		
		start(epcToWrite).done(new DoneCallback<String>() {

			@Override
			public void onDone(String result) {
				
				System.out.println("Done within " + TIMEOUT_MS + " ms - " + result);
				try {
					if(epcs.size() != 1){
						return;
					}
					execute(epcs.poll());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}).fail(new FailCallback<Result>() {

			@Override
			public void onFail(Result result) {
				System.out.println(result);
				try {
					if(epcs.size() != 1){
						return;
					}
					execute(epcs.poll());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public Promise start(String epcToWrite) throws Exception {
		final Deferred deferred = new DeferredObject();
		final Promise promise = deferred.promise();
		
		if (preWriteChecker.check(epcToWrite)) {
			//Thread.sleep(sleepTimeBeforeEpcWriting);
			
			reader.writeEpc(epcToWrite).done(new DoneCallback<String>() {

				public void onDone(String epc) {
					postWriteChecking(epcToWrite, deferred);
				}
				
			}).fail(new FailCallback<Result>() {

				@Override
				public void onFail(Result result) {
					deferred.reject(result);
				}
				
			});
			
			//timeout(deferred, TIMEOUT_MS);
		} else {
			deferred.reject(Result.newReasonOnlyResult("Invalid epc!"));
		}
		
		return promise;
	}
	
	private void postWriteChecking(String epcToWrite, final Deferred deferred) {
		try {
			reader.readSingleEpc().done(new DoneCallback<Tag>() {
				
				@Override
				public void onDone(Tag result) {
					String epcWritten = result.getEpc().toHexString();
					System.out.println(epcWritten);
					verify(epcToWrite,  epcWritten, deferred);
				}

			}).fail(new FailCallback<Result>() {

				@Override
				public void onFail(Result result) {
					deferred.reject(result);
				}
			});
		} catch (Exception e) {
			deferred.reject(Result.newResult("read failure", e));
		}
	}
	
	private void timeout(final Deferred deferred, long ms) {
		Timer tm = new Timer();
		tm.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if(!(deferred.isResolved() || deferred.isRejected())){
					deferred.reject(Result.newTimeoutResult());
				}
			}
		}, ms);
	}
	

	private void verify(String epcToWrite, String epcWritten, Deferred deferred) {
		if (epcToWrite.equals(epcWritten)) {

			new CountTryer(new Executor() {

				public void execute(final CountTryer me) {
					System.out.println("reading margin...");
					reader.readMargin(epcWritten).done(new DoneCallback<Integer>() {
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