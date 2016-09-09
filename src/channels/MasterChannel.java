package channels;

import static base.LogManager.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import tasks.MasterTask;
import tasks.Task;

public class MasterChannel extends TcpChannel {
	
	public MasterChannel(String category) {
		super(category);
	}

	@Override
	public Task createTask(SocketChannel clientConnection) {
		try {
			return new MasterTask(clientConnection, category);
		}
		catch( IOException e ) {
			trace(e);
			return null;
		}
	}

}
