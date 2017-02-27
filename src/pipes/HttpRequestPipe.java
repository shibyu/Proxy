package pipes;

import static base.Constant.*;
import static base.Config.*;

import java.io.*;
import java.net.Socket;

import base.Config;
import base.SocketManager;
import contents.*;
import exceptions.*;
import formatters.*;
import processors.*;
import tasks.*;
import util.Host;

public class HttpRequestPipe extends HttpPipe {

	private HttpProcessor requestProcessor;
	private HttpRequestFormatter requestFormatter;
	private Host host;
	
	private Socket client;
	private Socket target;
	
	public HttpRequestPipe(HttpTask owner, Socket client, OutputStream output, int bufferSize) throws IOException {
		super(owner, client.getInputStream(), output, bufferSize);
		this.client = client;
		requestProcessor = new HttpProcessor(bufferSize);
	}

	// 外からモード変更するための method;
	// 中身を外に出さないようにするために作っただけ;
	@Override
	public void changeWebSocketMode() {
		setStatus(HTTP_MODE_WEBSOCKET);
		webSocketProcessor = new WebSocketProcessor(bufferSize);
		webSocketFormatter = WebSocketFormatter.createWebSocketRequestFormatter();
	}
	
	@Override
	protected void httpTransfer(byte buffer[], int length) throws IOException {
		requestProcessor.process(buffer, length);
		while( requestProcessor.hasMoreContent() ) {
			// そもそも論として、HTTP に対応する Proxy を作る必要性はない (既存ツールをそのまま使えば良い);
			// なので、何も考えずに WebSocket に特化した作りにしてしまうのが良いのではないか？;
			// TODO: 普通の HTTP Proxy を作る気分になったらここで実装を変える必要がある気がする;
			if( requestProcessor.isWebSocket() == false ) {
				throw new ConfigurationException("not a WebSocket request");
			}
			// 最初に接続先を決めるところ;
			if( host == null ) {
				host = requestProcessor.getHost();
				target = SocketManager.tcpConnect(host);
				setOutputStream(target.getOutputStream());
				requestFormatter = new HttpRequestFormatter(host);
			}
			Content content = requestProcessor.pullContent();
			requestFormatter.setContent(content);
			// WebSocket への upgrade を　Proxy に送ってしまっても問題ないが、そうなると HTTP で wrap したものと混在して気持ち悪い;
			if( requestProcessor.isWebSocket() == false && typedOwner.hasRequestSlave() ) {
				// TODO: proxy を使ってどうするか？;
				requestFormatter.outputBytes(getProxyOutputStream());
				throw new ImplementationException("not implemented yet: Http Proxy");
			}
			else {
				requestFormatter.outputBytes(output);
				// ここで WebSocket への upgrade の返事を待つ;
				// TODO: 普通の HTTP Proxy の場合には、別スレッドで扱えるようにとか考えないとまずい気もする;
				// 単純に動かすだけであれば、これを別スレッドにしてしまえば何の問題もないはず;
				typedOwner.registerResponsePipe( new HttpResponsePipe(typedOwner, target.getInputStream(), client.getOutputStream(), bufferSize) );
				// response を待って、ステートを変更させる;
				// response を待たずに WebSocket のリクエストを送ってくることはないと考える;
			}
		}
	}
	
	@Override
	protected void webSocketTransfer(byte buffer[], int length) throws IOException {
		webSocketProcessor.process(buffer, length);
		while( webSocketProcessor.hasMoreContent() ) {
			Content content = webSocketProcessor.pullContent();
			// WebSocket のリクエストを HTTP で wrap して Proxy に渡す;
			if( typedOwner.hasRequestSlave() && isWebSocketContent(content) ) {
				// 送信に失敗した場合は、そのままサーバに送り付けたいので fall through;
				if( delegateContent(content) ) { return; }
			}
			// それ以外の場合はそのままサーバに送り付ける;
			webSocketFormatter.setContent(content);
			webSocketFormatter.outputBytes(output);
		}
	}
	
	@Override
	protected String getTypeString() {
		return "Request";
	}
	
	@Override
	protected Host getSlaveHost() {
		Config config = Config.getConfig(true);
		return new Host(config.getHost(), config.getRequestSlavePort(CATEGORY_HTTP));
	}

}
