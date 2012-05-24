package com.atarbes.Synchro.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Commit extends Common {

	public Commit() {
		super();
	}
	
	public static void main(String[] args) {
		new Commit().commonMain(args);
	}

	@Override
	protected void doWork(Properties properties, HttpURLConnection connection) throws TransferException, IOException {
		URL url = new URL(properties.getProperty("commitUrl", Common.DEFAULT_APP_URL + "/submit.php"));
		
		try {
			connection = (HttpURLConnection) url.openConnection();
			InputStream statusStream = connection.getInputStream();
			String status = Synchro.streamToString(statusStream);
			if(status.startsWith("You do not own")) {
				throw new TransferException(status);
			}
			connection.disconnect();
		} catch(IOException e) {
			throw new TransferException(Synchro.streamToString(connection.getErrorStream()) + e.getStackTrace().toString());
		}
		
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Referer", url.toString());
		connection.setDoOutput(true);
		
		try {
			Synchro.commit(connection, new File(properties.getProperty("filename", Common.DEFAULT_FILENAME)));
		} catch(IOException e) {
			throw new TransferException(Synchro.streamToString(connection.getErrorStream()) + "\n" + e.getStackTrace());
		}
	}
}
