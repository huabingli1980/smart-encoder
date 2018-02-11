package com.rsmart.utils;

public class IDGenerator {
	private static int ID_SEQ = 1234;
	
	public static int genSeqId() {
		return ID_SEQ++;
	}

}
