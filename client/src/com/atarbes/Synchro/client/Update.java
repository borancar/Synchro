package com.atarbes.Synchro.client;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Update extends Common {

	public Update() {
		super();
	}
	
	public static void main(String[] args) {
		new Update().commonMain(args);
	}
	
	@Override
	protected void doWork(Properties properties, HttpURLConnection connection) throws TransferException, IOException {
		URL url = new URL(properties.getProperty("updateUrl", Common.DEFAULT_APP_URL + "/request.php"));
		connection = (HttpURLConnection) url.openConnection();
		
		try {
			Synchro.update(connection, new File(properties.getProperty("filename", Common.DEFAULT_FILENAME)));
		} catch(IOException e) {
			throw new TransferException(Synchro.streamToString(connection.getErrorStream()) + "\n" + e.getStackTrace());
		}
	}
}
