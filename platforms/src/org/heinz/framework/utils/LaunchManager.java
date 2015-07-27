package org.heinz.framework.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LaunchManager {
	public final int portNr;
	private ServerSocket socket;
	private List listeners = new ArrayList();
	
	public LaunchManager(int portNr) {
		this.portNr = portNr;
	}
	
	public void addLaunchManagerListener(LaunchManagerListener listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeLaunchManagerListener(LaunchManagerListener listener) {
		listeners.remove(listener);
	}
	
	public void startServer() throws IOException {
		socket = new ServerSocket(portNr);
		
		Thread listenThread = new Thread() {
			public void run() {
				while(true) {
					Socket s = null;
					try {
						s = socket.accept();
						ObjectInputStream os = new ObjectInputStream(s.getInputStream());
						String[] args = (String[]) os.readObject();
						os.close();
						s = null;
						fireProgramLaunched(args);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if(s != null) {
						try {
							s.close();
						} catch (IOException e) {
						}
					}
				}
			}
		};
		listenThread.start();
	}
	
	private void fireProgramLaunched(String[] args) {
		for(Iterator it=listeners.iterator(); it.hasNext();) {
			LaunchManagerListener l = (LaunchManagerListener) it.next();
			l.programLaunched(args);
		}
	}

	public boolean delegateLaunch(String[] args) {
		try {
			Socket s = new Socket();
			s.connect(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), portNr));
			ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
			os.writeObject(args);
			os.flush();
			os.close();
			s.close();
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}

	public void startServer(LaunchManagerListener launchManagerListener) throws IOException {
		addLaunchManagerListener(launchManagerListener);
		startServer();
	}
}
