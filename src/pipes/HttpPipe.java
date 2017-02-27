package pipes;

import static base.Config.*;
import static base.Constant.*;
import static base.LogManager.*;

import java.net.*;

import base.SocketManager;

import java.io.*;

import contents.*;
import exceptions.*;
import formatters.*;
import processors.WebSocketProcessor;
import tasks.*;
import util.*;

// TODO: WebSocket に upgrade させてからは適用する Rule が Host ごとに異なるはずなので、それを実装しないとダメそう;

public abstract class HttpPipe extends Pipe implements PushablePipe {
	
	protected HttpTask typedOwner;
	protected int status;

	protected WebSocketProcessor webSocketProcessor;
	protected WebSocketFormatter webSocketFormatter;
	
	// TODO: proxy 関連の処理は DelegatePipe とうまくマージしたい;
	protected Socket proxyConnection;
	private Pipe subPipe;
	protected HttpRequestFormatter proxyRequestFormatter;
	
	public HttpPipe(HttpTask owner, InputStream input, OutputStream output, int bufferSize) throws IOException {
		super(owner, input, output, bufferSize);
		typedOwner = owner;
		status = HTTP_MODE_NORMAL;
	}
	
	public synchronized int getStatus() {
		return status;
	}

	protected synchronized void setStatus(int status) {
		this.status = status;
	}

	@Override
	public void transfer(byte[] buffer, int length) throws IOException {
		int status = getStatus();
		switch( status ) {
		case HTTP_MODE_NORMAL:
			httpTransfer(buffer, length);
			break;
		case HTTP_MODE_WEBSOCKET:
			webSocketTransfer(buffer, length);
			break;
		default:
			throw new ImplementationException("unknown http status: " + status);
		}
	}

	protected boolean isWebSocketContent(Content content) {
		String value = content.getAdditionalInfo(WS_TYPE_KEY);
		if( value == null ) { return false; }
		return value.equals(WS_TYPE_TEXT_VALUE) || value.equals(WS_TYPE_BINARY_VALUE);
	}
	
	public abstract void changeWebSocketMode();
	protected abstract void httpTransfer(byte buffer[], int length) throws IOException;
	protected abstract void webSocketTransfer(byte buffer[], int length) throws IOException;
	
	protected void setupProxy() throws IOException {
		if( tcpConfig.isKeepAlive(CATEGORY_PROXY) ) {
			if( proxyConnection != null ) { return; }
		}
		proxyConnection = SocketManager.tcpConnect( new Host(CATEGORY_PROXY) );
		if( tcpConfig.isKeepAlive(CATEGORY_PROXY) ) {
			subPipe = new VacantPipe(owner, proxyConnection.getInputStream(), bufferSize);
			subPipe.start();
		}
		if( proxyConnection == null ) {
			throw new ImplementationException("proxy connection is null");
		}
	}
	
	protected OutputStream getProxyOutputStream() throws IOException {
		setupProxy();
		return proxyConnection.getOutputStream();
	}
	
	@Override
	public void terminate() {
		if( subPipe != null ) { subPipe.terminate(); }
		super.terminate();
	}

	@Override
	public boolean push(Content content) {
		// ここには HttpContent がくるはず;
		if( (content instanceof HttpContent) == false ) {
			throw new ImplementationException("content is not HttpContent");
		}
		// HttpContent の情報を Content に落としてやる;
		HttpContent httpContent = (HttpContent)(content);
		String hexString = new String(httpContent.getBytes());
		try {
			byte binary[] = Parser.parseByteArray(httpContent.length() / 2, hexString);
			Content binaryContent = new Content(binary, 0, binary.length);
			binaryContent.putAdditionalInfo(WS_TYPE_KEY, httpContent.getHeaderValue(WS_TYPE_KEY));
			webSocketFormatter.setContent(binaryContent);
		}
		catch( DataFormatException e ) {
			trace(e);
			throw new ImplementationException("failed to parse hex string: " + hexString);
		}
		try {
			webSocketFormatter.outputBytes(output);
			return true;
		}
		catch( IOException e ) {
			trace(e);
			return false;
		}
	}
	
	private int contentId;
	
	protected abstract String getTypeString();
	protected abstract Host getSlaveHost();
	
	protected boolean delegateContent(Content content) {
		String typeString = getTypeString();
		String firstLine = "POST /" + owner.getTaskId() + "/" + typeString + " HTTP/1.1";
		CaseInsensitiveMap<String> headers = new CaseInsensitiveMap<String>();
		headers.add(WS_TYPE_KEY, content.getAdditionalInfo(WS_TYPE_KEY));
		headers.add("ContentId", String.valueOf(++contentId));
		headers.add("TaskId", String.valueOf(owner.getTaskId()));
		headers.add("SlaveType", getTypeString());
		headers.add("Host", getSlaveHost().toString());
		// binary をそのまま投げようとすると文字化けする場合があるので...;
		// platform の encoding を使わないようにすれば良いのだが、副作用もあり得るので...;
		// TODO: データの converter を入れれば binary がそのまま飛ぶこともないはずなので、それで解消したい;
		String hexContent = Util.toHexString(content.getBytes(), 0, content.length());
		HttpContent httpContent = new HttpContent(hexContent.getBytes(), 0, hexContent.length(), firstLine, headers);
		if( proxyRequestFormatter == null ) {
			proxyRequestFormatter = new HttpRequestFormatter(getSlaveHost());
		}
		proxyRequestFormatter.setContent(httpContent);
		try {
			proxyRequestFormatter.outputBytes(getProxyOutputStream());
			return true;
		}
		catch( IOException e ) {
			trace(e);
			return false;
		}
	}
	
}
