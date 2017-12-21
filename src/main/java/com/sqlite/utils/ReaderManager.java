package com.sqlite.utils;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;

import boot.ApplicationConfig;

//10.0.0.89
public class ReaderManager {
	private static ImpinjReader reader;

	public static ImpinjReader getReader() {
		//final String hostname = ApplicationConfig.get("host", "169.254.1.2");
		final String hostname = ApplicationConfig.get("host", "10.0.0.89");
		if (ReaderManager.reader != null) {
			if (!ReaderManager.reader.isConnected()) {
				try {
					ReaderManager.reader.setAddress(hostname);
					ReaderManager.reader.connect();
				} catch (OctaneSdkException e) {
					e.printStackTrace();
					throw new RuntimeException("Cant connect to reader on ip - " + hostname + "!");
				}
			}
			return ReaderManager.reader;
		}
		(ReaderManager.reader = new ImpinjReader()).setAddress(hostname);
		System.out.println("Connecting to " + hostname);
		try {
			ReaderManager.reader.connect();
		} catch (OctaneSdkException e3) {
			if (!ReaderManager.reader.isConnected()) {
				try {
					ReaderManager.reader.connect();
				} catch (OctaneSdkException e2) {
					e2.printStackTrace();
					throw new RuntimeException("Cant connect to reader on ip - " + hostname + "!");
				}
			}
		}
		System.out.println("Connected");
		return ReaderManager.reader;
	}

	public static void stopPrinter(final long mille) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				int gpoPort = Integer.valueOf(ApplicationConfig.get("gpo.port", "1"));
				try {
					reader.setGpo(gpoPort, true);
					Thread.sleep(mille);
					reader.setGpo(gpoPort, false);
				} catch (OctaneSdkException | InterruptedException e) {
					e.printStackTrace();
				}
			}

		}).start();
	}

	public static void stop() throws OctaneSdkException {
		if(!ReaderManager.reader.queryStatus().getIsSingulating()){
			ReaderManager.reader.stop();
		}
	}

	public static void disconnect() {
		if (ReaderManager.reader != null) {
			ReaderManager.reader.disconnect();
			ReaderManager.reader.setReaderStopListener(null);
		}
	}
}
