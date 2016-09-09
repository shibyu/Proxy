package clients;

import static base.Config.*;
import static base.LogManager.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import contents.Content;
import formatters.HttpRequestFormatter;
import pipes.Pipe;
import pipes.ProcessorPipe;
import processors.HttpProcessor;

public class HttpClient extends TcpClient {

	// TODO: 接続先のホスト設定をできるようにする;
	private boolean useProxy;
	private int proxyPort;

	public HttpClient(String category) {
		super(category);
		int proxyPort = tcpConfig.getPort(CATEGORY_PROXY);
		if( tcpConfig.isEnable(CATEGORY_PROXY) && proxyPort > 0 ) {
			this.proxyPort = proxyPort;
			useProxy = true;
		}
	}
	
	protected int getPort() {
		if (useProxy) {
			return proxyPort;
		}
		return port;
	}

	@Override
	public void execute() {

		int bufferSize = tcpConfig.getBufferSize(CATEGORY_HTTP);
		HttpProcessor processor = new HttpProcessor(bufferSize);

		String line;
		
		HttpRequestFormatter formatter = new HttpRequestFormatter("127.0.0.1", port);
		formatter.setUri("/");

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				connect(getPort());
				Pipe pipe = new ProcessorPipe(null, input, System.out, bufferSize, processor);
				pipe.start();
				System.out.print("input> ");
				line = reader.readLine();
				if( clientExits(line) ) { break; }
				formatter.setContent(new Content(line.getBytes()));
				formatter.outputBytes(output);
				output.flush();
				pipe.terminate();
				pipe.join();
				disconnect();
			}
		}
		catch (IOException e) {
			trace(e);
		}
		catch( InterruptedException e ) {
			trace(e);
		}

	}

}
