package socket.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SocketTest implements Comparable<SocketTest>{
	
	/*public static void main(String[] args) throws UnknownHostException, IOException {
		Socket socket = new Socket("169.254.1.1", 5084);
		
		System.out.println(socket.isConnected());
		OutputStream os = socket.getOutputStream();
		InputStream is = socket.getInputStream();
		BufferedOutputStream bos = new BufferedOutputStream(os);
		
		byte[] bs = new byte[] { (byte)0x80};
		bos.write(bs);
		
		BufferedInputStream bis = new BufferedInputStream(is);
		int number = -1;
		while((number= bis.read())> 0){
			System.out.println(number);
		}
		
		
		
		
		//socket.getInputStream();
	}*/
	
	public static void main(String[] args) {
		
		
		for (int i = 0; i < 1000; i++) {
			System.out.println("<p>dfsd</p>");
		}
		
		String epc = "E280689000000001543AEF83";
		String tid = "E280689000000001543AEF83";
		
		System.out.println(epc.equals(tid));
		
	}
	
	//500+40, 
	// 1,2,3,3,4,5,6
	// 
	// 1,2,3,4
	// 3,1,5,6
	public Set<String> intersect(Set<String> set1, Set<String> set2){
		
		Set<String> ret = new HashSet<String>();
		for (String string : set2) {
			
			
		}
		set1.retainAll(set2);
		
		Collections.disjoint(c1, c2)
		
		return set2;
	}

	@Override
	public int compareTo(SocketTest o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
