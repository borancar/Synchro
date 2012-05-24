package com.atarbes.Synchro.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observer;

public class InputStreamMeter extends InputStream {

	private InputStream in;
	
	private int transfered = 0;
	
	private Observer observer;
	
	public InputStreamMeter(InputStream in, Observer observer) {
		this.in = in;
		this.observer = observer;
	}
	
	public int read() throws IOException {
		int data = in.read();
		
		if(data != -1) {
			transfered++;
			observer.update(null, transfered);
		}
		
		return data;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int count = in.read(b, off, len);
		
		if(count != -1) {
			transfered += count;
			observer.update(null, transfered);
		}
		
		return count;
	}
}
