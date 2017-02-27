package tasks;

import static base.Config.tcpConfig;
import static base.LogManager.LOG_TASK;
import static base.LogManager.output;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import pipes.*;

abstract public class SlaveAttachableTask extends Task {
	
	public SlaveAttachableTask(SocketChannel clientConnection, String category) throws IOException {
		super(clientConnection, category);
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub

	}

	protected Pipe requestPipe;
	protected Pipe responsePipe;

	protected void setRequestPipe(Pipe pipe) {
		requestPipe = pipe;
	}
	
	protected void setResponsePipe(Pipe pipe) {
		responsePipe = pipe;
	}
	
	public Pipe getRequestPipe() {
		return requestPipe;
	}
	
	public Pipe getResponsePipe() {
		return responsePipe;
	}
	
	public boolean hasRequestSlave() {
		return tcpConfig.isEnableRequestSlaveCategory(category);
	}
	
	public boolean hasResponseSlave() {
		return tcpConfig.isEnableResponseSlaveCategory(category);
	}
	
	protected void startAll() {
		if( requestPipe != null ) { requestPipe.start(); }
		if( responsePipe != null ) { responsePipe.start(); }
	}
	
	protected void joinAll() throws InterruptedException {
		if( requestPipe != null ) { requestPipe.join(); }
		if( responsePipe != null ) { responsePipe.join(); }
	}

	@Override
	public void terminate() {
		output("terminate @ " + this, LOG_TASK);
		terminate(requestPipe);
		terminate(responsePipe);
	}

}
