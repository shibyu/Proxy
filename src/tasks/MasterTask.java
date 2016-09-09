package tasks;

import static base.Config.*;
import static base.LogManager.*;
import static base.Constant.*;

import util.Host;
import base.SocketManager;
import pipes.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class MasterTask extends Task {
	
	private Pipe requestPipe;
	private Pipe responsePipe;

	public MasterTask(SocketChannel clientConnection, String category) throws IOException {
		super(clientConnection, category);
	}
	
	private void setRequestPipe(Pipe pipe) {
		requestPipe = pipe;
	}
	
	private void setResponsePipe(Pipe pipe) {
		responsePipe = pipe;
	}
	
	public DelegatePipe getRequestPipe() {
		if( requestPipe == null || (requestPipe instanceof DelegatePipe) == false ) { return null; }
		return (DelegatePipe)(requestPipe);
	}
	
	public DelegatePipe getResponsePipe() {
		if( responsePipe == null || (responsePipe instanceof DelegatePipe) == false ) { return null; }
		return (DelegatePipe)(responsePipe);
	}
	
	private boolean hasRequestSlave() {
		return tcpConfig.isEnableRequestSlaveCategory(category);
	}
	
	private boolean hasResponseSlave() {
		return tcpConfig.isEnableResponseSlaveCategory(category);
	}
	
	@Override
	public void execute() {
		
		Socket socket = null;
		
		try {

			socket = SocketManager.tcpConnect( new Host(category) );

			if( hasRequestSlave() ) {
				output("use request slave", -1);
				Pipe pipe = new DelegatePipe(this, clientInput, socket.getOutputStream(), bufferSize, category, true, TYPE_REQUEST);
				setRequestPipe(pipe);
			}
			else {
				setRequestPipe( new TruePipe(this, clientInput, socket.getOutputStream(), bufferSize) );
			}

			if( hasResponseSlave() ) {
				output("use response slave", -1);
				Pipe pipe = new DelegatePipe(this, socket.getInputStream(), clientOutput, bufferSize, category, true, TYPE_RESPONSE);
				setResponsePipe(pipe);
			}
			else {
				setResponsePipe( new TruePipe(this, socket.getInputStream(), clientOutput, bufferSize) );
			}
			
			startAll();
			joinAll();

		}
		
		catch( InterruptedException e ) {
			trace(e);
		}
		
		catch( IOException e ) {
			trace(e);
		}
		
		finally {
			SocketManager.close(socket);
		}

	}
	
	@Override
	public String getTaskName() {
		return "Master";
	}
	
	@Override
	public String getStatus() {
		StringBuilder status = new StringBuilder();
		status.append("Host: " + new Host(category));
		return status.toString();
	}
	
	private void startAll() {
		if( requestPipe != null ) { requestPipe.start(); }
		if( responsePipe != null ) { responsePipe.start(); }
	}
	
	private void joinAll() throws InterruptedException {
		if( requestPipe != null ) { requestPipe.join(); }
		if( responsePipe != null ) { responsePipe.join(); }
	}
	
	public void terminate() {
		output("terminate @ " + this, LOG_TASK);
		terminate(requestPipe);
		terminate(responsePipe);
	}

}
