package com.rsmart;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.junit.Test;

import com.impinj.octane.Tag;

import ch.qos.logback.core.net.SyslogOutputStream;

public class SimpleReaderTest {

	@Test
	public void test_writeEpc() {
		IReader reader = new SimpleReader();
		reader.writeEpc("00B07A135403A9880806C38B").done(new DoneCallback<String>() {

			@Override
			public void onDone(String arg0) {
				// TODO Auto-generated method stub
				System.out.println(arg0);
			}
		}).fail(new FailCallback<String>() {

			@Override
			public void onFail(String arg0) {
				// TODO Auto-generated method stub
				System.out.println("failed" + arg0);
			}
		});
	}
	
	@Test
	public void test_readEpc(){
		IReader reader = new SimpleReader();
		reader.readEpc().done(new DoneCallback<List<Tag>>() {

			@Override
			public void onDone(List<Tag> arg0) {
			    System.out.println("done ...");
				for (Tag tag : arg0) {
					System.out.println(tag.getEpc().toHexString());
				}
			}
		}).fail(new FailCallback<String>() {

			@Override
			public void onFail(String arg0) {
				// TODO Auto-generated method stub
				System.out.println("Failed ... " + arg0);
			}
		});
	}

}
