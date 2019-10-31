package datagram;

import static base.Config.*;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import java.util.*;

import base.LogManager;
import util.*;

public class DnsListener extends UdpListener {
	
	//c ある程度進んだら trace に落とす;
	// TODO コンフィグファイルに落としたい気もする;
	private static final int DEFAULT_PORT = 53;
	
	private List<?> hosts;
	private boolean isDebug;
	private int logLevel;
	
	private String proxy;
	
	private Set<String> requests;
	
	private SocketAddress originServer;
	
	public DnsListener() {
		super(CATEGORY_DNS);
		hosts = udpConfig.getListProperty(CATEGORY_DNS, KEY_HOSTS);
		isDebug = udpConfig.getBoolProperty(CATEGORY_DNS, KEY_DEBUG);
		logLevel = isDebug ? LogManager.OUTPUT_DEBUG : LogManager.OUTPUT_TRACE;
		originServer = new InetSocketAddress(udpConfig.getStringProperty(CATEGORY_DNS, KEY_ORIGIN), DEFAULT_PORT);
		requests = new HashSet<String>();
		proxy = udpConfig.getStringProperty(CATEGORY_DNS, KEY_PROXY);
	}

	@Override
	public void peerSend(UdpPeer peer, DatagramPacket packet) {
		packet.setSocketAddress(originServer);
		peer.send(packet);
	}

	@Override
	public void peerReceived(SocketAddress clientAddress, DatagramPacket packet) {
		DNSEntity result = getDNSEntity(packet);
		if( result != null ) {
			String host = result.getHost();
			if( requests.add(host) ) {
				//c どういう DNS リクエストがあったかは必要なので、出力させる;
				LogManager.output("DNS Request: " + host, LogManager.OUTPUT_ALWAYS);
				debug(packet);
				log("DNS Entity: " + result);
			}
			if( checkHost(result.getHost()) ) {
				//c 自分に通信を流したい host なので、レスポンスを書き換える;
				packet = modifyPacket(packet, result.getIndex());
				//c これは重要なので出力必須;
				LogManager.output("modified DNS Response: " + host + " => " + proxy, LogManager.OUTPUT_ALWAYS);
			}
		}
		// origin server から受け取ったものはそのまま返せばいいので port map と同じでいいはず;
		packet.setSocketAddress(clientAddress);
		sendClient(packet);
	}
	
	private void log(String message) {
		LogManager.output(message, logLevel);
	}
	
	private DatagramPacket modifyPacket(DatagramPacket packet, int index) {
		byte raw[] = packet.getData();
		String tmp[] = proxy.split("\\.", 4);
		for( int i = 0; i < 4; ++i ) {
			raw[index + i] = (byte)(Integer.parseInt(tmp[i]) & 0xFF);
		}
		return new DatagramPacket(raw, raw.length, packet.getSocketAddress());
	}
	
	private boolean checkHost(String host) {
		for( Object data : hosts ) {
			if( matches(host, data.toString()) ) { return true; }
		}
		return false;
	}
	
	private boolean matches(String host, String rule) {
		// TODO: 仮実装で equals を使っておくが、もう少しまともなマッチングができるようになるとうれしいかも？;
		return rule.equals(host);
	}
	
	private DNSEntity getDNSEntity(DatagramPacket packet) {
		// TODO: 即値...;
		// response の方なので、クエリが保存されていることを確認しておきたい;
		byte raw[] = packet.getData();
		int queryCount = DataIO.readInt16(raw, 4);
		if( queryCount < 1 ) {
			//c なぜかクエリがいない...;
			return null;
		}
		String host = readHost(raw);
		int pos = host.length() + 18;
		int queryType = DataIO.readInt16(raw, pos - 4);
		if( queryType != 1 ) {
			log("not IPv4: " + queryType);
			return null;
		}
		// result の数だけなめる;
		int resultCount = DataIO.readInt16(raw, 6);
		for( int i = 0; i < resultCount; ++i ) {
			int resultType = DataIO.readInt16(raw, pos + 2);
			int length = DataIO.readInt16(raw, pos + 10);
			if( resultType == 1 ) {
				//c 見つかったっぽいので、これを返す;
				String ip = readIP(raw, pos + 12);
				return new DNSEntity(host, ip, pos + 12);
			}
			pos += length + 12;
		}
		//c ここにきたということは、それっぽい結果が見当たらなかったということ...;
		return null;
	}
	
	private String readHost(byte raw[]) {
		StringBuilder builder = new StringBuilder();
		int pos = 12;
		while( raw[pos] != 0 ) {
			int size = raw[pos];
			builder.append(new String(raw, pos + 1, size));
			pos += size + 1;
			if( raw[pos] != 0 ) { builder.append("."); }
		}
		return builder.toString();
	}
	
	private String readIP(byte raw[], int pos) {
		return toInt(raw[pos]) + "." + toInt(raw[pos + 1]) + "." + toInt(raw[pos + 2]) + "." + toInt(raw[pos + 3]);
	}
	
	private int toInt(byte value) {
		// byte って signed だったっけ？;
		return ((int)value & 0xFF);
	}
	
	private void debug(DatagramPacket packet) {
		byte raw[] = packet.getData();
		int length = raw.length;
		while( length > 0 && raw[length - 1] == 0 ) { --length; }
		byte tmp[] = Util.copyByteArray(raw, 0, length);
		log( new String(tmp) );
		log( Util.toBinaryString( tmp ) );
	}

	// TODO: 外に出す;
	private class DNSEntity {
		
		private String host;
		private String ip;
		private int ipIndex;
		
		public DNSEntity(String host, String ip, int ipIndex) {
			this.host = host;
			this.ip = ip;
			this.ipIndex = ipIndex;
		}
		
		public String getHost() { return host; }
		public String getIP() { return ip; }
		
		public int getIndex() { return ipIndex; }
		
		@Override
		public String toString() {
			return getHost() + " => " + getIP() + "(@" + getIndex() + ")";
		}
		
	}

}

