package com.atarbes.Synchro.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Observable;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Synchro extends Observable {

	public static String streamToString(InputStream is) throws IOException {
		InputStream input = new BufferedInputStream(is);
		StringBuilder sb = new StringBuilder();
		
		int receivedByte;
		
		while ((receivedByte = input.read()) != -1) {
			sb.append((char) receivedByte);
		}
		
		return sb.toString();
	}
		
	public static String byteArrayAsHex(byte[] array) {
		StringBuilder sb = new StringBuilder();
		
		for (byte digit : array) {
			sb.append(Character.forDigit((digit >> 4) & 0xF, 16));
			sb.append(Character.forDigit(digit & 0xF, 16));
		}
		
		return sb.toString();
	}
	
	public static int connectStreams(InputStream in, OutputStream out) throws IOException {				
		int data;
		int transfered = 0;
		
		while((data = in.read()) != -1) {
			out.write(data);
			transfered++;
		}
		
		return transfered;
	}

	public static MessageDigest getMD5() {
		MessageDigest md5 = null;
		
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException ignorable) {
		}
		
		return md5;
	}
	
	public static void update(HttpURLConnection connection, File outputFile) throws TransferException, IOException {
		if(connection.getHeaderFields().containsKey("Content-Disposition")) {
			MessageDigest md5 = getMD5();
			ProgressReport progress = new ProgressReport();
			Thread progressThread = new Thread(progress);
			progressThread.start();
			progress.setTitle("Update");

			int length = Integer.parseInt(connection.getHeaderField("Content-Length"));
			progress.setFilesize(length);
			
			InputStream in = new DigestInputStream(
					new BufferedInputStream(
							new InputStreamMeter(connection.getInputStream(), progress)), md5);
			
			OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
			
			int received = connectStreams(in, out);
			
			out.close();
			
			progress.close();

			int expected = length;
			
			if(received != expected) {
				throw new TransferException("File length not OK! Transfer was interrupted. Expected: " + expected + " bytes, received: " + received + " bytes.");
			}

			if(!connection.getHeaderFields().containsKey("Content-MD5")) {
				throw new TransferException("MD5 hash not received from server. File integrity cannot be checked!");
			} else {
				try {
					String actualMD5 = byteArrayAsHex(md5.digest());
					String expectedMD5 = new String(Base64.decode(connection.getHeaderField("Content-MD5")));
					
					if(!expectedMD5.equals(actualMD5)) {
						throw new TransferException("File corrupt! MD5 hashes do not match!\r\nExpected: " + expectedMD5 + "\r\nActual: " + actualMD5);
					}
				} catch(Base64DecodingException e) {
					throw new TransferException("Received MD5 hash corrupt!", e);
				}
			}
			
			in.close();
		} else {
			throw new TransferException(streamToString(connection.getInputStream()));
		}
	}
	
	public static void commit(HttpURLConnection connection, File inputFile) throws TransferException, IOException {
				
		try {			
			MessageDigest md5 = getMD5();
			
			MimeMultipart multipart = new MimeMultipart();
		
			ByteArrayDataSource dataSource = new ByteArrayDataSource(
					new DigestInputStream(
							new BufferedInputStream(
									new FileInputStream(inputFile)),
					md5), "application/octet-stream");
			
			MimeBodyPart syncedFile = new MimeBodyPart();
			syncedFile.setDataHandler(new DataHandler(dataSource));
			syncedFile.setHeader("Content-Type", "application/octet-stream");
			syncedFile.setHeader("Content-Disposition", "form-data; name=\"userfile\"; filename=\"synced.file\"");
			multipart.addBodyPart(syncedFile);
			
			MimeBodyPart hashPart = new MimeBodyPart();
			hashPart.setText(byteArrayAsHex(md5.digest()));
			hashPart.setHeader("Content-Disposition", "form-data; name=\"md5\"");
			multipart.addBodyPart(hashPart);
			
			String contentType = multipart.getContentType().replaceAll("mixed", "form-data").replaceAll("(\r|\r\n|\n|\t| )+", " ");
			
			connection.setRequestMethod("POST");
			connection.addRequestProperty("Content-Type", contentType);
						
			ProgressReport progress = new ProgressReport();
			Thread progressThread = new Thread(progress);
			progressThread.start();
			progress.setFilesize((int) inputFile.length());
			progress.setTitle("Commit");			
						
			OutputStream out = connection.getOutputStream();
			out = new BufferedOutputStream(new OutputStreamMeter(out, progress));
			multipart.writeTo(out);			
			out.close();
			
			progress.close();
						
			InputStream resultStream = new BufferedInputStream(connection.getInputStream());
			String result = streamToString(resultStream);
			resultStream.close();
			
			if(!result.equals("File sent successfully!")) {
				throw new TransferException(result);
			}
		} catch (MessagingException e) {
			throw new TransferException(e);
		}
	}
}
