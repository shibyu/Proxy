package datagram;

import static base.Config.*;

import java.net.*;

public class PortMapListener extends UdpListener {
	
	private int fromPort;
	private SocketAddress host;
	
	public PortMapListener(int fromPort, int toPort) {
		super(CATEGORY_PORTMAP);
		this.fromPort = fromPort;
		host = new InetSocketAddress("127.0.0.1", toPort);
	}
	
	@Override
	public int getPort() {
		return fromPort;
	}
	
	@Override
	public void peerSend(UdpPeer peer, DatagramPacket packet) {
		packet.setSocketAddress(host);
		peer.send(packet);
	}
	
	@Override
	public void peerReceived(SocketAddress clientAddress, DatagramPacket packet) {
		packet.setSocketAddress(clientAddress);
		sendClient(packet);
	}

}
