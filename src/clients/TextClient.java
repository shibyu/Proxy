package clients;

import static base.LogManager.*;

import java.io.*;

import pipes.Pipe;
import pipes.ProcessorPipe;
import processors.CommandLineProcessor;
import processors.Processor;

public class TextClient extends TcpClient {
	
	public TextClient(String category) {
		super(category);
	}

	public void execute() {
		
		trace("connect server: " + port);
		Processor processor = new CommandLineProcessor(bufferSize); 
		
		try {
			connect();
			Pipe pipe = new ProcessorPipe(null, input, System.out, bufferSize, processor);
			pipe.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while( true ) {
				line = reader.readLine();
				if( clientExits(line) ) { break; }
				output.write((line + "\n").getBytes());
				output.flush();
			}
			pipe.terminate();
			pipe.join();
			disconnect();
		}
		catch( IOException e ) {
			trace(e);
		}
		catch( InterruptedException e ) {
			trace(e);
		}
		
	}
	
}
