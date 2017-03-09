package tasks;

import static base.Config.*;
import static base.LogManager.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import base.SocketManager;
import pipes.*;
import rules.*;
import util.DataType;
import util.Host;

// 処理が煩雑になってきたので、細かい処理は Pipe を 2本作って別々に流す;
// ただし、loop back もできるようにしておく;

public class CustomTask extends Task {
	
	private Pipe loopbackPipe;
	private Pipe requestPipe;
	private Pipe responsePipe;
	
	public CustomTask(SocketChannel clientConnection, String category) throws IOException {
		super(clientConnection, category);
	}

	@Override
	public void execute() {
		
		trace(category + " " + tcpConfig.getHost(category));
		
		Host host = new Host(category);
		Rule rule = RuleFactory.getRule(category, true);
		
		Socket socket = null;
		
		try {
			if( host.isLoopBack() ) {
				loopbackPipe = new CustomPipe(this, clientInput, clientOutput, bufferSize,
						new DataType( tcpConfig.getRequestInputType(category) ),
						new DataType( tcpConfig.getResponseOutputType(category) ),
						rule, null);
				loopbackPipe.start();
				loopbackPipe.join();
			}
			
			else {
				socket = SocketManager.tcpConnect(host.getHost(), host.getPort());
				requestPipe = new CustomPipe(this, clientInput, socket.getOutputStream(), bufferSize,
						new DataType( tcpConfig.getRequestInputType(category) ),
						new DataType( tcpConfig.getRequestOutputType(category) ),
						rule, host);
				responsePipe = new CustomPipe(this, socket.getInputStream(), clientOutput, bufferSize,
						new DataType( tcpConfig.getResponseInputType(category) ),
						new DataType( tcpConfig.getResponseOutputType(category) ),
						rule, host);
				requestPipe.start();
				responsePipe.start();
				requestPipe.join();
				responsePipe.join();
			}
		}
		catch( IOException e ) {
			trace(e);
		}
		catch( InterruptedException e ) {
			trace(e);
		}
		finally {
			SocketManager.close(socket);
		}

	}
	
	@Override
	public String getTaskName() {
		return "Custom";
	}
	
	@Override
	public void terminate() {
		terminate(loopbackPipe);
		terminate(requestPipe);
		terminate(responsePipe);
	}
	
}
