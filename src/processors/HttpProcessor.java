package processors;

import static base.LogManager.*;

import java.io.IOException;

import contents.*;
import exceptions.ImplementationException;
import util.*;

public class HttpProcessor extends Processor {

	private static final int STATE_HTTP_HEADER = 1;
	private static final int STATE_HTTP_CONTENT = 2;
	private static final int STATE_DELEGATED = 3;
	
	private int state;
	private int taskId;
	
	private String client;
	private int listenerId;
	
	private String firstLine;
	private CaseInsensitiveMap<String> headers;
	
	private boolean upgradeWebSocket;
	private WebSocketProcessor webSocketProcessor;
	
	public HttpProcessor(int bufferSize) {
		super(bufferSize);
		upgradeWebSocket = false;
		initialize();
	}
	
	public boolean isWebSocket() {
		return upgradeWebSocket;
	}
	
	private void setupWebSocketProcessor(byte buffer[], int offset, int length) {
		webSocketProcessor = new WebSocketProcessor(bufferSize);
		webSocketProcessor.process(buffer, offset, length);
	}
	
	private void initialize() {
		state = STATE_HTTP_HEADER;
		firstLine = null;
		headers = new CaseInsensitiveMap<String>();
	}
	
	private void queueContent() {
		// contentLength == innerPointer のはずだが、contentLength 以外の長さになっても困るので;
		Content content = new HttpContent( innerBuffer, 0, getContentLength(), firstLine, headers );
		queueContent(content);
	}
	
	private void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	
	public int getTaskId() { return taskId; }
	
	private void setClient(String client) {
		this.client = client;
	}
	
	public String getClient() { return client; }
	
	private void setListenerId(int listenerId) {
		this.listenerId = listenerId;
	}
	
	public int getListenerId() { return listenerId; }
	
	@Override
	public void process(byte buffer[], int offset, int length) throws IOException {
		
		if( state == STATE_DELEGATED ) {
			throw new ImplementationException("delegation failed...");
		}
		
		while( offset < length ) {
		
			if( state == STATE_HTTP_HEADER ) {
				while( offset < length ) {
					append(buffer[offset++]);
					if( isEOL() ) {
						parseHeader();
						if( state != STATE_HTTP_HEADER ) {
							break;
						}
					}
				}
			}
			
			if( state == STATE_HTTP_CONTENT ) {
				while( offset < length && reachContentEnd() == false ) {
					append(buffer[offset++]);
				}
				if( reachContentEnd() ) {
					queueContent();
					if( isWebSocket() ) {
						// WebSocket になる場合は、この後のデータをここでは処理できないので、丸投げしてしまう;
						// そして多分この processor はその後何も仕事をせずに破棄されるはず...;
						setupWebSocketProcessor(buffer, offset, length - offset);
						state = STATE_DELEGATED;
						break;
					}
					initialize();
				}
			}
		
		}
			
	}

	// check if ends with CR + LF;
	private boolean isEOL() {
		return (innerPointer >= 2 && innerBuffer[innerPointer - 2] == '\r' && innerBuffer[innerPointer - 1] == '\n'); 
	}
	
	private void endOfHeaders() {
		state = STATE_HTTP_CONTENT;
		// WebSocket への upgrade のチェックをする;
		if( matches(headers.get("Connection"), "Upgrade") && matches(headers.get("Upgrade"), "WebSocket") ) {
			upgradeWebSocket = true;
		}
	}
	
	private boolean matches(String value, String target) {
		if( value == null ) { return false; }
		// TODO: 取り敢えずは完全一致で見ておく;
		output(value + " <==matches==> " + target, -1);
		return value.trim().equalsIgnoreCase(target);
	}
	
	public Host getHost() {
		return Parser.parseHost(headers.get("Host"), 80);
	}

	// HTTP ヘッダを処理して、各種情報を拾っておく;
	// 空行 (ヘッダの終わり) の場合は状態を変更する;
	private void parseHeader() {
		// remove CR + LF;
		String line = new String(innerBuffer, 0, innerPointer - 2);
		// データを読み終えたのでクリアしておく;
		reset();
		// 最初の行は別途格納しておく...;
		if( firstLine == null ) {
			firstLine = line;
			return;
		}
		// ヘッダの終わり;
		if( line.equals("") ) {
			endOfHeaders();
			return;
		}
		String tmp[] = line.split(":", 2);
		if( tmp.length != 2 ) { return; }
		String key = tmp[0].trim();
		headers.add(key, tmp[1]);
		output(key + ": " + tmp[1], -1);
		if( key.equalsIgnoreCase("Content-Length") ) {
			setContentLength( Parser.parseInt(tmp[1]) );
			return;
		}
		if( key.equalsIgnoreCase("TaskId") ) {
			setTaskId( Parser.parseInt(tmp[1]) );
			return;
		}
		if(key.equalsIgnoreCase("OriginalClient") ) {
			setClient( tmp[1].trim() );
			return;
		}
		if( key.equalsIgnoreCase("UdpListener") ) {
			setListenerId( Parser.parseInt(tmp[1]) );
			return;
		}
	}
	
}
