package datagram;

import static base.Config.*;

import java.net.*;

import util.Host;

public class PortMapListener extends UdpListener {
	
	private int fromPort;
	private SocketAddress host;
	
	public PortMapListener(int fromPort, Host target) {
		super(CATEGORY_PORTMAP);
		this.fromPort = fromPort;
		host = new InetSocketAddress(target.getHost(), target.getPort());
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
