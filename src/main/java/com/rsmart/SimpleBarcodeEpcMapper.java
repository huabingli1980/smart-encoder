package com.rsmart;

import java.util.HashMap;
import java.util.Map;

public class SimpleBarcodeEpcMapper implements IBarcodeEpcMappable
{	
	static final Map mp;
	
	static{
		mp = new HashMap();
		mp.put("0123456831223", "00B07A135403A988" + String.valueOf(System.currentTimeMillis()).substring(0, 8));
		mp.put("0123456831223", "00B07A135403A988" + String.valueOf(System.currentTimeMillis()).substring(0, 8));
		mp.put("0123456831223", "00B07A135403A988" + String.valueOf(System.currentTimeMillis()).substring(0, 8));
		mp.put("0123456831223", "00B07A135403A988" + String.valueOf(System.currentTimeMillis()).substring(0, 8));
				
	}
	
	public static void main(String[] args) {
		System.out.println("00B07A135403A988" + String.valueOf(System.currentTimeMillis()).substring(0, 8));
	}
	
   /* @Override
    public String barcodeToEpc(final String barcode) {
        return (String) mp.get(barcode);
    }*/
    
    @Override
    public String barcodeToEpc(final String barcode) {
        String valueOf = String.valueOf(System.currentTimeMillis()+54321);
		String testdata = "00B07A135403A988" + valueOf.substring(valueOf.length()-8);
		return testdata;
    }
}
