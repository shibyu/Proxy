package util;

import static base.Config.*;

public class Host {

	private String host;
	private int port;

	public Host(String category) {
		this(tcpConfig.getHost(category), tcpConfig.getHostPort(category));
	}
	
	public Host(int port) {
		this("127.0.0.1", port);
	}

	public Host(String host, int port) {
		this.host = host;
		if (port > 0) {
			this.port = port;
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isLoopBack() {
		return host.equalsIgnoreCase("loopback");
	}
	
	@Override
	public String toString() {
		return host + ":" + port;
	}

}
