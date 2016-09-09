package tasks;

import static base.LogManager.*;

import java.io.*;
import java.nio.channels.SocketChannel;

import base.InputController;
import formatters.HttpResponseFormatter;
import processors.HttpProcessor;

// HTTP サーバで使用するタスク;
// 現状では HTTP サーバというか、echo を HTTP で warp したサービス;

public class HttpTask extends Task {
	
	public HttpTask(SocketChannel clientConnection, String category) throws IOException {
		super(clientConnection, category);
	}
	
	@Override
	public void execute() {

		// input = HTTP request;
		// output = HTTP response;
		
		HttpProcessor processor = new HttpProcessor(bufferSize);
		HttpResponseFormatter formatter = new HttpResponseFormatter();
		
		InputController controller = new InputController(clientInput);
		
		try {
			
			byte buffer[] = new byte[bufferSize];
			int length = 0;

			// TODO: 接続断をどう扱うか？ IOException が飛ばない場合とか;
			while( (length = controller.read(buffer)) >= 0 ) {
				processor.process(buffer, 0, length);
				while( processor.hasMoreContent() ) {
					formatter.setContent( processor.getContent() );
					formatter.outputBytes(clientOutput);
					clientOutput.flush();
				}
			}
			
		}
		catch( IOException e ) {
			trace(e);
		}

	}
	
	@Override
	public String getTaskName() {
		return "Http";
	}
	
	@Override
	public void terminate() {
		// Pipe を使用していないので閉じるものがない;
	}

}
