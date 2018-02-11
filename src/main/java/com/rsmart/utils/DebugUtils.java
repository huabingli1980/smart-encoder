package com.rsmart.utils;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class DebugUtils {
	
	public static void record(String string) {
		System.out.println("record:" + string + " - " + System.currentTimeMillis());
	}
	
	public static void disableLog() {
		Logger minaLogger = (Logger) LoggerFactory.getLogger("org.apache.mina");
		if (minaLogger != null) {
			minaLogger.setLevel(Level.WARN);
		}
	}

}
