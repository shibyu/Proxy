package channels;

import static base.LogManager.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import base.Config;
import tasks.SlaveTask;
import tasks.Task;

public class SlaveChannel extends TcpChannel {
	
	public static final int TYPE_REQUEST_SLAVE = 1;
	public static final int TYPE_RESPONSE_SLAVE = 2;
	
	private int type;
	private boolean isTCP;
	
	public static SlaveChannel createRequestSlaveChannel(String category, boolean isTCP) {
		return new SlaveChannel(category, TYPE_REQUEST_SLAVE, isTCP);
	}

	public static SlaveChannel createResponseSlaveChannel(String category, boolean isTCP) {
		return new SlaveChannel(category, TYPE_RESPONSE_SLAVE, isTCP);
	}

	private SlaveChannel(String category, int type, boolean isTCP) {
		super(category);
		this.type = type;
		this.isTCP = isTCP;
	}
	
	@Override 
	public Config getConfig() {
		return Config.getConfig(isTCP);
	}
	
	@Override
	public int getPort() {
		return getConfig().getSlavePort(category, type);
	}
	
	@Override
	public Task createTask(SocketChannel clientConnection) {
		try {
			return new SlaveTask(clientConnection, category, type, isTCP);
		}
		catch( IOException e ) {
			trace(e);
			return null;
		}
	}

}
