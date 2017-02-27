package channels;

import static base.LogManager.*;
import static base.Config.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import tasks.HttpTask;
import tasks.Task;

public class HttpChannel extends TcpChannel {

	public static HttpChannel getInstance() { return self; }
	private static final HttpChannel self = new HttpChannel();
	
	private HttpChannel() {
		super(CATEGORY_HTTP);
	}

	@Override
	public Task createTask(SocketChannel clientConnection) {
		try {
			return new HttpTask(clientConnection, category);
		}
		catch( IOException e ) {
			trace(e);
			return null;
		}
	}

}
