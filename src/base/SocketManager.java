package base;

import static base.Config.*;
import static base.LogManager.*;

import java.io.*;
import java.net.*;
import java.nio.channels.*;

import util.Host;

// Socket 生成の wrapper;
// 普段の手続き自体、単純に new するだけなので、わざわざ wrapper を経由せずに実行してしまいがちなので効果は薄そうではある;
// とはいえ、time out のセット漏れをなくしたいので、極力これを経由して Socket を生成することとする;

public class SocketManager {
	
	private SocketManager() { }
	
	public static Socket tcpConnect(Host host) {
		return tcpConnect(host.getHost(), host.getPort());
	}
	
	public static Socket tcpConnect(String host, int port) {
		try {
			Socket socket = new Socket(host, port);
			socket.setSoTimeout(tcpConfig.getTimeout());
			socket.setKeepAlive(true);
			return socket;
		}
		catch( IOException e ) {
			trace(e);
			trace(host + ":" + port);
			return null;
		}
	}
	
	public static DatagramSocket udpConnect() {
		try {
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(udpConfig.getTimeout());
			return socket;
		}
		catch( IOException e ) {
			trace(e);
			trace("failed to create udp socket");
			return null;
		}
	}
	
	public static DatagramSocket udpListen(int port) {
		try {
			DatagramSocket socket = new DatagramSocket(port);
			socket.setSoTimeout(udpConfig.getTimeout());
			return socket;
		}
		catch( IOException e ) {
			trace(e);
			trace("failed to listen udp port: " + port);
			return null;
		}
	}
	
	public static void close(Socket socket) {
		if( socket == null ) { return; }
		try {
			socket.close();
		}
		catch( IOException e ) {
			trace(e);
		}
	}
	
	public static void close(DatagramSocket socket) {
		if( socket == null ) { return; }
		socket.close();
	}
	
	public static void close(SocketChannel channel) {
		try {
			if( channel != null ) {
				channel.close();
			}
		}
		catch( IOException e ) {
			trace(e);
		}
	}
	
}
