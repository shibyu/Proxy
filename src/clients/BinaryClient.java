package clients;

import static base.Config.*;
import static base.LogManager.*;

import formatters.*;
import pipes.Pipe;
import pipes.ProcessorPipe;
import processors.*;
import rules.CustomRule;
import rules.RuleFactory;
import util.Host;

import java.io.*;

public class BinaryClient extends TcpClient {

	CustomRule rule;
	private String category;

	public BinaryClient(String category) {
		super( category );
		rule = RuleFactory.getRule(category, true);
		this.category = category;
		bufferSize = tcpConfig.getBufferSize(category);
		tcpConfig.getBufferSize(category);
	}

	@Override
	public void execute() {
		if( category.matches(".*unwrap.*") ) {
			unwrap();
		}
		else if( category.matches(".*wrap.*") ) {
			wrap();
		}
		else {
			plain();
		}
	}

	private void unwrap() {
		HttpProcessor processor = new HttpProcessor(bufferSize);
		Host host = new Host(tcpConfig.getCustomCategory("unwrap"));
		HttpRequestFormatter formatter = new HttpRequestFormatter(host);
		core(processor, formatter);
	}
	
	private void wrap() {
		BinaryProcessor processor = new BinaryProcessor(bufferSize, rule);
		BinaryFormatter formatter = new BinaryFormatter(rule);
		core(processor, formatter);
	}
	
	private void plain() {
		BinaryProcessor processor = new BinaryProcessor(bufferSize, rule);
		BinaryFormatter formatter = new BinaryFormatter(rule);
		core(processor, formatter);
	}

	private void core(Processor processor, Formatter formatter) {
		
		try {
			connect();
			Pipe pipe = new ProcessorPipe(null, input, System.out, bufferSize, processor);
			pipe.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while( true ) {
				System.out.print("input> ");
				line = reader.readLine();
				if( clientExits(line) ) { break; }
				if( formatter instanceof BinaryFormatter ) {
					((BinaryFormatter)(formatter)).addHeader(line.getBytes());
				}
				else {
					formatter.setContent(line.getBytes());
				}
				formatter.outputBytes(output);
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
