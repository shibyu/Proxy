package channels;

import static base.Config.*;
import static base.LogManager.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import tasks.*;

public class HttpEchoChannel extends TcpChannel {
	
	private static final HttpEchoChannel self = new HttpEchoChannel();
	public static HttpEchoChannel getInstance() { return self; }
	
	private HttpEchoChannel() {
		super(CATEGORY_HTTP_ECHO);
	}

	@Override
	public Task createTask(SocketChannel clientConnection) {
		try {
			return new HttpEchoTask(clientConnection, category);
		}
		catch( IOException e ) {
			trace(e);
			return null;
		}
	}

}
