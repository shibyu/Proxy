package channels;

import static base.Config.*;
import static base.LogManager.*;

import java.io.*;
import java.nio.channels.*;

import tasks.*;
import util.Host;

// リクエストを別のポートに転送する;

public class PortMapChannel extends TcpChannel {

	private int fromPort;
	private Host target;
	
	public PortMapChannel(int fromPort, Host target) {
		// BaseChannel のコンストラクタは作っていないような気がするが、一応呼んでおく;
		super(CATEGORY_PORTMAP);
		this.fromPort = fromPort;
		this.target = target;
	}
	
	
	@Override
	public int getPort() {
		return fromPort;
	}
	
	@Override
	public Task createTask(SocketChannel clientConnection) {
		try {
			return new PortMapTask(clientConnection, category, target);
		}
		catch( IOException e ) {
			trace(e);
			return null;
		}
	}

}
