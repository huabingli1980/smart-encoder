package com.impinj.octane;

public class JReader extends ImpinjReader{
	
	public void startRospect(){
		try {
			super.startRoSpec();
		} catch (OctaneSdkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
