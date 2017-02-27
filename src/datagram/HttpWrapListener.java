package datagram;

import static base.LogManager.*;
import static base.Config.*;
import static base.Constant.*;

import java.io.IOException;
import java.net.*;

import contents.Content;
import util.Host;
import util.Parser;
import pipes.DelegatePipe;

public class HttpWrapListener extends UdpListener {
	
	private SocketAddress host;
	
	private DelegatePipe requestDelegatePipe;
	private DelegatePipe responseDelegatePipe;
	
	private boolean ignoreRUDP;
	
	public HttpWrapListener(String category) {
		super(category);
		host = new InetSocketAddress(udpConfig.getHost(category), udpConfig.getHostPort(category));
		if( hasRequestSlave() ) {
			Host requestSlaveHost = new Host(tcpConfig.getHost(), udpConfig.getRequestSlavePort(category));
			requestDelegatePipe = new DelegatePipe(requestSlaveHost, this, udpConfig.getRequestDataConverter(category)); 
		}
		if( hasResponseSlave() ) {
			Host responseSlaveHost = new Host(tcpConfig.getHost(), udpConfig.getResponseSlavePort(category));
			responseDelegatePipe = new DelegatePipe(responseSlaveHost, this, udpConfig.getResponseDataConverter(category)); 
		}
		ignoreRUDP = udpConfig.getBoolProperty(category, "IgnoreRUDP", true, false);
	}

	// TODO: MasterTask から複製しているので、共通の基底クラスを作る必要がありそう;
	// 当面はここで実装して、最終的にリファクタリングする予定;
	private boolean hasRequestSlave() {
		return udpConfig.isEnableRequestSlaveCategory(category);
	}

	private boolean hasResponseSlave() {
		return udpConfig.isEnableResponseSlaveCategory(category);
	}
	
	@Override
	public void peerSend(UdpPeer peer, DatagramPacket packet) {
		output("send original: " + util.Util.toHexString(packet.getData(), 0, packet.getLength(), 4), LOG_RAW_DATA);
		// request slave がいればそちらに送る;
		if( hasRequestSlave() ) {
			Content content = new Content(packet.getData(), 0, packet.getLength());
			try {
				// 送信に成功したらそこでおしまい;
				if( requestDelegatePipe.processContent(content, packet.getSocketAddress().toString(), TYPE_REQUEST, ignoreRUDP) ) {
					return;
				}
			}
			catch( IOException e ) {
				trace(e);
				// TODO: proxy に送信失敗した場合のエラー処理...;
				// 生のパケットを送ってしまえば問題ないはず...;
			}
		}
		// そうでなければ、peer に送る;
		// 何らかの理由で送信できなかった場合とかも含む;
		packet.setSocketAddress(host);
		peer.send(packet);
	}
	
	public void pushRequest(String client, Content content) {
		output("send converted: " + util.Util.toHexString(content.getBytes(), 0, content.length(), 4), LOG_RAW_DATA);
		UdpPeer peer = getPeer(client);
		DatagramPacket packet = new DatagramPacket(content.getBytes(), 0, content.length(), host);
		peer.send(packet);
	}

	@Override
	public void peerReceived(SocketAddress clientAddress, DatagramPacket packet) {
		output("recv original: " + util.Util.toHexString(packet.getData(), 0, packet.getLength(), 4), LOG_RAW_DATA);
		// response slave がいればそちらに送る;
		if( hasResponseSlave() ) {
			Content content = new Content(packet.getData(), 0, packet.getLength());
			try {
				if( responseDelegatePipe.processContent(content, clientAddress.toString(), TYPE_RESPONSE, ignoreRUDP) ) {
					return;
				}
			}
			catch( IOException e ) {
				trace(e);
				// TODO: proxy に送信失敗した場合のエラー処理;
			}
		}
		packet.setSocketAddress(clientAddress);
		sendClient(packet);
	}
	
	public void pushResponse(String client, Content content) {
		output("recv converted: " + util.Util.toHexString(content.getBytes(), 0, content.length(), 4), LOG_RAW_DATA);
		if( client.startsWith("/") ) { client = client.substring("/".length()); }
		Host host = Parser.parseHost(client);
		DatagramPacket packet = new DatagramPacket(content.getBytes(), 0, content.length(), new InetSocketAddress(host.getHost(), host.getPort()));
		sendClient(packet);
	}

}
