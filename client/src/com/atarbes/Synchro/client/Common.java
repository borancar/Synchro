package com.atarbes.Synchro.client;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public abstract class Common {

	public static final String CONFIG_FILE = "config_synchro.xml";
	public static final String DEFAULT_FILENAME = "synced.file";
	public static final String DEFAULT_APP_URL = "http://localhost/Synchro";
	
	public static final int SUCESS = 0;
	public static final int INVALID_URL = 1;
	public static final int IO_ERROR = 2;
	public static final int TRANSFER_ERROR = 3;
	
	public void commonMain(String[] args) {
		Authenticator.setDefault(new LoginDialog());
		
		HttpURLConnection connection = null;
		
		int returnCode = 0;
	
		String message = "";
		
		try {
			Properties properties = new Properties();
			properties.loadFromXML(new BufferedInputStream(new FileInputStream(Common.CONFIG_FILE)));

			doWork(properties, connection);
			
			returnCode = SUCESS;
		} catch(MalformedURLException e) {
			returnCode = INVALID_URL;
			message = e.getMessage();
		} catch(FileNotFoundException e) {
			returnCode = IO_ERROR;
			message = e.getMessage();
		} catch(IOException e) {
			returnCode = IO_ERROR;
			message = e.getMessage();
		} catch(TransferException e) {
			returnCode = TRANSFER_ERROR;
			message = e.getMessage();
		}
		
		if(returnCode != SUCESS) {
			System.err.println(message);
			MessageBox mbox = new MessageBox(new Shell(new Display()), SWT.ICON_ERROR);
			mbox.setText("Error");
			mbox.setMessage(message);
			mbox.open();
		} else {
			System.out.println(message);
		}
		
		System.exit(returnCode);
	}
	
	protected abstract void doWork(Properties properties, HttpURLConnection connection) throws  TransferException, IOException;
}
