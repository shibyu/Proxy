package datagram;

import static base.Config.*;
import static base.LogManager.*;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.net.*;

import base.SocketManager;

abstract public class UdpListener extends Thread {

	protected int listenerId;
	
	protected DatagramSocket listenSocket;
	protected String category;
	
	protected Map<String, UdpPeer> connections;
	
	protected UdpPacketReceiver clientReceiver;
	
	protected UdpListener(String category) {
		this.category = category;
		connections = new HashMap<String, UdpPeer>();
		clientReceiver = new UdpPacketReceiver();
	}
	
	public String getCategory() {
		return category;
	}
	
	public int getPort() {
		return udpConfig.getPort( getCategory() );
	}
	
	public int getBufferSize() {
		return udpConfig.getBufferSize( getCategory() ) ;
	}
	
	public void setListenerId(int listenerId) {
		this.listenerId = listenerId;
	}
	
	public int getListenerId() { return listenerId; }
	
	// 接続元情報から、接続先情報を拾ってくる (適切に再利用する);
	synchronized protected UdpPeer getPeer(SocketAddress clientAddress) {
		String client = clientAddress.toString();
		if( connections.containsKey(client) == false ) {
			UdpPeer peer = new UdpPeer(this, clientAddress);
			connections.put(client, peer);
			peer.start();
		}
		return connections.get(client);
	}

	// 新規に登録する必要のないコンテキストでのみ実行される前提;
	synchronized protected UdpPeer getPeer(String client) {
		return connections.get(client);
	}
	
	// 接続が切れたか分からないので、一定時間を経過した peer は切ってしまう;
	synchronized public int cleanup() {
		int cleanupCount = 0;
		for( Entry<String, UdpPeer> pair : connections.entrySet() ) {
			UdpPeer peer = pair.getValue();
			if( peer.isDirty() ) {
				peer.clean();
			}
			else {
				// dirty flag が立っていないので、音信不通ということになる;
				// 終われば自分から離脱するはずなので、ここでは connections から除去しないでおく;
				peer.terminate();
				++cleanupCount;
			}
		}
		return cleanupCount;
	}
	
	synchronized public void remove(String client) {
		connections.remove(client);
	}
	
	synchronized public final boolean register() {
		if( udpConfig.isEnable( getCategory() ) == false ) { return false; }
		int port = getPort();
		listenSocket = SocketManager.udpListen(port);
		if( listenSocket == null ) {
			output("failed to listen udp port: " + port, LOG_LISTEN);
			return false;
		}
		output("listening udp port: " + port, LOG_LISTEN);
		return true;
	}
	
	public void sendClient(DatagramPacket packet) {
		try {
			listenSocket.send(packet);
		}
		catch( IOException e ) {
			trace(e);
		}
	}
	
	@Override
	public void run() {
		
		int bufferSize = udpConfig.getBufferSize(CATEGORY_PORTMAP);
		trace("udp bufferSize: " + bufferSize);
		byte buffer[] = new byte[bufferSize];
		DatagramPacket packet = new DatagramPacket(buffer, bufferSize);
		
		while( true ) {
			
			if( clientReceiver.receive(listenSocket, packet) == false ) {
				trace("failed to receive client packet @ " + this);
				break;
			}
			
			// trace("received udp packet @ port: " + getPort());
			UdpPeer peer = getPeer(packet.getSocketAddress());
			peerSend(peer, packet);

		}

	}

	// peer に送る packet をどう処理するか;
	abstract public void peerSend(UdpPeer peer, DatagramPacket packet);
	
	// peer から返ってきた reponse をどう処理するか;
	abstract public void peerReceived(SocketAddress clientAddress, DatagramPacket packet); 
	
}
