package com.atarbes.Synchro.client;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Observer;

public class OutputStreamMeter extends OutputStream {

	private OutputStream out;
	
	private int transfered = 0;
	
	private Observer observer;
	
	public OutputStreamMeter(OutputStream out, Observer observer) {
		this.out = out;
		this.observer = observer;
	}
	
	@Override
	public void write(int b) throws IOException {
		if(b != -1) {
			transfered++;
			observer.update(null, transfered);
		}
		
		out.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		
		transfered += len;
		observer.update(null, transfered);
	}
}
