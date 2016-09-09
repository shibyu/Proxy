package datagram;

import static base.LogManager.*;

import java.io.*;
import java.net.*;

import base.SocketManager;

public class UdpPeer extends Thread {

	private UdpListener clientConnection;
	// 同じソケットを使い回すことで、port を固定する;
	// client ごとに異なる port を使用することにより、UDP packet の振り分けを実現する;
	private DatagramSocket peerConnection;
	private UdpPacketReceiver peerReceiver;
	
	private SocketAddress clientAddress;

	// UDP の socket を破棄するために、定期的に接続を切る処理を入れておく;
	// client の port が変わったときとか、そもそも通信が終わったときとか;
	private boolean isDirty;
	
	public UdpPeer(UdpListener listener, SocketAddress clientAddress) {
		clientConnection = listener;
		peerConnection = SocketManager.udpConnect();
		peerReceiver = new UdpPacketReceiver();
		this.clientAddress = clientAddress;
		clean();
	}
	
	synchronized public void clean() {
		setDirty(false);
	}
	
	synchronized public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
	
	synchronized public boolean isDirty() {
		return isDirty;
	}
	
	synchronized public void terminate() {
		peerReceiver.terminate();
	}
	
	public void send(DatagramPacket packet) {
		try {
			peerConnection.send(packet);
		}
		catch( IOException e ) {
			trace(e);
		}
	}
	
	// peer からの返信を receive して、client に send することを延々と繰り返すスレッド;
	@Override
	public void run() {
		
		int bufferSize = clientConnection.getBufferSize();
		byte buffer[] = new byte[bufferSize];
		DatagramPacket packet = new DatagramPacket(buffer, bufferSize);
		
		while( true ) {
			
			if( peerReceiver.receive(peerConnection, packet) == false ) {
				trace("failed to receive peer packet @ " + clientConnection);
				// continue でもいいのかも知れないが、ここを無限ループしそうなので break しておく;
				break;
			}
			
			setDirty(true);

			clientConnection.peerReceived(clientAddress, packet);
			
		}
		
		// 終了したので、クリアしてしまう;
		clientConnection.remove(clientAddress.toString());
		
	}

}
