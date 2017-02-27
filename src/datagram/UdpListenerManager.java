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
	
	synchronized public int cleanup() {
		int cleanupCount = 0;
		for( UdpListener listener : listeners ) {
			cleanupCount += listener.cleanup();
		}
		return cleanupCount;
	}
	
	@Override
	public void run() {
		while( true ) {
			int cleanupCount = cleanup();
			if( cleanupCount > 0 ) {
				trace("cleanup " + cleanupCount + " udp connections");
			}
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
