package pipes;

import java.io.*;

import exceptions.ConfigurationException;
import processors.*;
import rules.CustomRule;
import tasks.Task;
import formatters.*;
import util.*;

//TODO: DSL を読んで、処理する;
//TODO: alignment を前提にしているので、必要があれば解消する (多分問題になることはまずないと思う);

public class CustomPipe extends Pipe {
	
	private Processor processor;
	private Formatter formatter;
	
	public CustomPipe(Task owner, InputStream input, OutputStream output, int bufferSize, DataType inputType, DataType outputType, CustomRule rule, Host host) {
		super(owner, input, output, bufferSize);
		setupProcessor(inputType, bufferSize, rule);
		setupFormatter(outputType, bufferSize, rule, host);
	}
	
	private void setupProcessor(DataType inputType, int bufferSize, CustomRule rule) {
		if( inputType.isBinary() ) {
			processor = new BinaryProcessor(bufferSize, rule);
		}
		else if( inputType.isHttp() ) {
			processor = new HttpProcessor(bufferSize);
		}
		else {
			throw new ConfigurationException("unknown input type: " + inputType.getType());
		}
	}
	
	private void setupFormatter(DataType outputType, int bufferSize, CustomRule rule, Host host) {
		if( outputType.isBinary() ) {
			formatter = new BinaryFormatter(rule);
		}
		else if( outputType.isHttpRequest() ) {
			formatter = new HttpRequestFormatter(host.getHost(), host.getPort());
		}
		else if( outputType.isHttpResponse() ) {
			formatter = new HttpResponseFormatter();
		}
		else {
			throw new ConfigurationException("unknown output type: " + outputType.getType());
		}
	}

	@Override
	public void transfer(byte[] buffer, int length) throws IOException {
		processor.process(buffer, length);
		while( processor.hasMoreContent() ) {
			++contentId;
			formatter.setContent( processor.getContent() );
			formatter.outputBytes(output);
			output.flush();
		}
	}

}
