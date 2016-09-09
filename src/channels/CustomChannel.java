package channels;

import static base.LogManager.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import tasks.CustomTask;
import tasks.Task;

public class CustomChannel extends TcpChannel {
	
	public CustomChannel(String category) {
		super(category);
	}

	@Override
	public Task createTask(SocketChannel clientConnection) {
		try {
			return new CustomTask(clientConnection, category);
		}
		catch( IOException e ) {
			trace(e);
			return null;
		}
	}

}
