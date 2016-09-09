package datagram;

import static base.LogManager.*;
import static base.Config.*;

import java.util.*;

public class UdpListenerManager extends Thread {

	private List<UdpListener> listeners;
	
	private static final UdpListenerManager self = new UdpListenerManager();
	public static final UdpListenerManager getInstance() { return self; }
	
	private UdpListenerManager() {
		listeners = new ArrayList<UdpListener>();
		start();
	}
	
	synchronized public void register(UdpListener listener) {
		listeners.add(listener);
	}
	
	synchronized public void cleanup() {
		for( UdpListener listener : listeners ) {
			listener.cleanup();
		}
	}
	
	@Override
	public void run() {
		while( true ) {
			trace("cleanup udp connections");
			cleanup();
			try {
				sleep(udpConfig.getIntProperty(KEY_CLEANUP));
			}
			catch (InterruptedException e) {
				trace(e);
				break;
			}
		}
	}
	
}
