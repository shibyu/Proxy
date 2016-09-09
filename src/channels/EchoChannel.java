package channels;

import static base.Config.*;
import static base.LogManager.*;

import tasks.*;

import java.io.IOException;
import java.nio.channels.*;

public class EchoChannel extends TcpChannel {
	
	private static final EchoChannel self = new EchoChannel();
	public static EchoChannel getInstance() { return self; }
	
	private EchoChannel() {
		super(CATEGORY_ECHO);
	}

	@Override
	public Task createTask(SocketChannel clientConnection) {
		try {
			return new EchoTask(clientConnection, category);
		}
		catch( IOException e ) {
			trace(e);
			return null;
		}
	}

}
