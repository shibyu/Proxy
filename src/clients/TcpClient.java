package clients;

import static base.Config.*;

import java.io.*;
import java.net.*;

import base.SocketManager;

abstract public class TcpClient extends Client {
	
	protected int port;
	protected int bufferSize;
	
	public TcpClient(String category) {
		this(tcpConfig.getPort(category), tcpConfig.getBufferSize(category));
	}
	
	private TcpClient(int port, int bufferSize) {
		this.port = port;
		this.bufferSize = bufferSize;
	}
	
	protected Socket socket;
	protected InputStream input;
	protected OutputStream output;
	
	protected void connect() throws IOException {
		connect(port);
	}

	protected void connect(int port) throws IOException {
		connect(tcpConfig.getHost(), port);
	}
	
	protected void connect(String host, int port) throws IOException {
		socket = SocketManager.tcpConnect(host, port);
		input = socket.getInputStream();
		output = socket.getOutputStream();
	}
	
	protected void disconnect() {
		SocketManager.close(socket);
	}
	
}
