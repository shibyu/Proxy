package channels;

import static base.Config.*;
import static base.LogManager.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import tasks.*;

public class HttpChannel extends TcpChannel {
	
	private static final HttpChannel self = new HttpChannel();
	public static HttpChannel getInstance() { return self; }
	
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
