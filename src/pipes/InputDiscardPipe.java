package pipes;

import static base.LogManager.*;

import tasks.*;

import java.io.IOException;
import java.net.*;

import base.SocketManager;
import processors.*;

// proxy からのレスポンスを見る必要がないので、捨てるための Pipe;
// TODO: 一個捨てれば十分なはずなので、現時点では取り敢えずそういう実装になっている;
public class InputDiscardPipe extends Pipe {

	private Socket processSocket;
	private Processor processor;
	
	public InputDiscardPipe(Task owner, Socket processSocket, Processor processor, int bufferSize) throws IOException {
		super(owner, processSocket.getInputStream(), null, bufferSize);
		this.processSocket = processSocket;
		this.processor = processor;
		
	}

	@Override
	public void transfer(byte[] buffer, int length) throws IOException {
		processor.process(buffer, length);
		if( processor.hasMoreContent() ) {
			terminate();
			try {
				join();
				SocketManager.close(processSocket);
			}
			catch( InterruptedException e ) {
				trace(e);
			}
		}
	}

}
