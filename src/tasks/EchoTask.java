package tasks;

import static base.LogManager.*;

import pipes.*;

import java.nio.channels.SocketChannel;
import java.io.IOException;

public class EchoTask extends Task {
	
	private Pipe echoPipe;

	public EchoTask(SocketChannel clientConnection, String category) throws IOException {
		super(clientConnection, category);
	}
	
	@Override
	public void execute() {
		echoPipe = new TruePipe(this, clientInput, clientOutput, bufferSize);
		echoPipe.start();
		try {
			echoPipe.join();
		}
		catch( InterruptedException e ) {
			trace(e);
		}
	}
	
	@Override
	public String getTaskName() {
		return "Echo";
	}
	
	@Override
	public void terminate() {
		terminate(echoPipe);
	}
		
}
