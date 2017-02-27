package pipes;

import static base.Config.CATEGORY_HTTP;
import static base.Constant.*;

import java.io.*;

import base.Config;
import contents.*;
import exceptions.ImplementationException;
import formatters.HttpResponseFormatter;
import formatters.WebSocketFormatter;
import processors.*;
import tasks.HttpTask;
import util.Host;

public class HttpResponsePipe extends HttpPipe {
	
	private HttpProcessor responseProcessor;
	private HttpResponseFormatter responseFormatter;
	
	public HttpResponsePipe(HttpTask owner, InputStream input, OutputStream output, int bufferSize) throws IOException {
		super(owner, input, output, bufferSize);
		responseProcessor = new HttpProcessor(bufferSize);
		responseFormatter = new HttpResponseFormatter();
	}

	@Override
	public void changeWebSocketMode() {
		Pipe requestPipe = typedOwner.getRequestPipe();
		if( (requestPipe instanceof HttpRequestPipe) == false ) {
			// これはないはず...;
			throw new ImplementationException("requestPipe is not HttpRequestPipe");
		}
		((HttpRequestPipe)(requestPipe)).changeWebSocketMode();
		setStatus(HTTP_MODE_WEBSOCKET);
		webSocketProcessor = new WebSocketProcessor(bufferSize);
		webSocketFormatter = WebSocketFormatter.createWebSocketResponseFormatter();
	}

	@Override
	protected void httpTransfer(byte buffer[], int length) throws IOException {
		responseProcessor.process(buffer, length);
		while( responseProcessor.hasMoreContent() ) {
			if( responseProcessor.isWebSocket() == false ) {
				// TODO: WebSocket への upgrade に失敗したケース (問題がありそうなら実装する);
				// そもそもこの Proxy にリクエストを転送する時点で、対応しているサービス限定になっているはず;
				throw new ImplementationException("upgrade failure");
			}
			Content content = responseProcessor.pullContent();
			responseFormatter.setContent(content);
			changeWebSocketMode();
			// クライアントにレスポンスを返却する;
			responseFormatter.outputBytes(output);
		}
	}
	
	@Override
	protected void webSocketTransfer(byte buffer[], int length) throws IOException {
		webSocketProcessor.process(buffer, length);
		while( webSocketProcessor.hasMoreContent() ) {
			Content content = webSocketProcessor.pullContent();
			if( typedOwner.hasResponseSlave() && isWebSocketContent(content) ) {
				if( delegateContent(content) ) { return; }
			}
			webSocketFormatter.setContent(content);
			webSocketFormatter.outputBytes(output);
		}
	}
	
	@Override
	protected String getTypeString() {
		return "Response";
	}
	
	@Override
	protected Host getSlaveHost() {
		Config config = Config.getConfig(true);
		return new Host(config.getHost(), config.getResponseSlavePort(CATEGORY_HTTP));
	}

}
