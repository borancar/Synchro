package com.atarbes.Synchro.client;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginDialog extends Authenticator {
	
	private PasswordAuthentication passwordAuth;
	
	private void init(final Shell shell) {		
		shell.setLayout(new GridLayout(2, false));
		
		GridData messageData = new GridData();
		messageData.horizontalSpan = 2;
		
		Label message = new Label(shell, SWT.RIGHT);
		message.setText(getRequestingURL().toString());
		message.setLayoutData(messageData);
		
		GridData textData = new GridData();
		textData.horizontalAlignment = SWT.FILL;
		
		GridData labelData = new GridData();
		labelData.horizontalAlignment = SWT.RIGHT;
				
		Label userLabel = new Label(shell, SWT.RIGHT);
		userLabel.setText("Username:");
		userLabel.setLayoutData(labelData);
		final Text userText = new Text(shell, SWT.BORDER);
		userText.setLayoutData(textData);
		
		Label passLabel = new Label(shell, SWT.RIGHT);
		passLabel.setText("Password:");
		passLabel.setLayoutData(labelData);
		final Text passText = new Text(shell, SWT.BORDER);
		passText.setLayoutData(textData);
		passText.setEchoChar('*');
		
		GridData compositeData = new GridData();
		compositeData.horizontalAlignment = SWT.RIGHT;
		compositeData.horizontalSpan = 2;
		
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayout(new FillLayout());
		buttonComposite.setLayoutData(compositeData);
		
		final Button okButton = new Button(buttonComposite, SWT.BORDER);
		okButton.setText("OK");
		okButton.addSelectionListener(new SelectionAdapter() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				passwordAuth = new PasswordAuthentication(userText.getText(), passText.getText().toCharArray());
				shell.dispose();
			}
		});
		
		final Button cancelButton = new Button(buttonComposite, SWT.BORDER);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				passwordAuth = null;
				shell.dispose();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		shell.addDisposeListener(new DisposeListener() {
		
			public void widgetDisposed(DisposeEvent e) {
				userText.setText(Integer.toHexString(new Random().hashCode()));
				passText.setText(Integer.toHexString(new Random().hashCode()));
			}
		});

		KeyListener escListener = new KeyAdapter() {	
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == SWT.ESC) {
					cancelButton.notifyListeners(SWT.Selection, new Event());
				}
			}
		};
		
		userText.addKeyListener(escListener);
		passText.addKeyListener(escListener);		
		
		shell.setDefaultButton(okButton);
		shell.setText(getRequestingPrompt());
	}
	
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		Display display = new Display();
		Shell shell = new Shell(display, SWT.DIALOG_TRIM);
		
		init(shell);
		
		shell.pack();
		shell.setLocation((display.getClientArea().width-shell.getSize().x)/2, (display.getClientArea().height-shell.getSize().y)/2);
		shell.open();		
		
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		display.dispose();
			
		return passwordAuth;
	}
}
