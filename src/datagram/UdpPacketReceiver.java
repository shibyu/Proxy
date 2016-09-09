package datagram;

import static base.LogManager.*;
import static base.Config.*;

import java.io.*;
import java.net.*;

public class UdpPacketReceiver {
	
	private boolean isActive;
	
	public UdpPacketReceiver() {
		setActive(true);
	}
	
	synchronized public boolean isActive() {
		return isActive;
	}

	synchronized public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	synchronized public void terminate() {
		setActive(false);
	}
	
	public boolean receive(DatagramSocket socket, DatagramPacket packet) {
		
		while( isActive() ) {
			try {
				socket.receive(packet);
				return true;
			}
			catch( SocketTimeoutException e ) {
				try {
					Thread.sleep(udpConfig.getTimeout());
					continue;
				}
				catch( InterruptedException e2 ) {
					trace(e);
					break;
				}
			}
			catch( IOException e ) {
				trace(e);
				break;
			}
		}
		
		return false;
		
	}

}
