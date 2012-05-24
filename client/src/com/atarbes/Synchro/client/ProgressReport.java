package com.atarbes.Synchro.client;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class ProgressReport implements Observer, Runnable {

	private ProgressBar progress = null;
	private Display display = null;
	private Shell shell = null;
	
	public void run() {		
		synchronized (this) {
			display = new Display();
			shell = new Shell(display);
			shell.setLayout(new FillLayout());
			
			progress = new ProgressBar(shell, SWT.HORIZONTAL | SWT.SMOOTH);
			progress.setMinimum(0);

			shell.pack();
			shell.setLocation(display.getClientArea().width/2 - shell.getSize().x/2, display.getClientArea().height/2 - shell.getSize().y/2);
			shell.open();

			this.notifyAll();
		}
			
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		display.dispose();
	}
	
	private void waitInit() {
		synchronized (this) {
			while(progress == null) {
				try {
					this.wait();
				} catch (InterruptedException ignorable) {
				}
			}
		}
	}
	
	public void setFilesize(final int size) {
		waitInit();
		
		display.syncExec(new Runnable() {
		
			public void run() {
				progress.setMaximum(size);
			}
		});
	}
	
	public void setTitle(final String title) {
		waitInit();
		
		display.syncExec(new Runnable() {
			
			public void run() {
				shell.setText(title);
			}
		});
	}
	
	public void close() {
		waitInit();
		
		display.syncExec(new Runnable() {
		
			public void run() {
				shell.dispose();
			}
		});
	}
	
	public void update(Observable o, final Object arg) {
		waitInit();
		
		display.syncExec(new Runnable() {
			
			public void run() {
				progress.setSelection((Integer) arg);
			}
		});
	}
}
