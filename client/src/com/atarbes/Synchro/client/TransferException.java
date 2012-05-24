package com.atarbes.Synchro.client;

public class TransferException extends Exception {

	private static final long serialVersionUID = 1L;

	public TransferException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransferException(String message) {
		super(message);
	}

	public TransferException(Throwable cause) {
		super(cause);
	}	
}
