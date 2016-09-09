package processors;

import util.Parser;

import java.io.IOException;

import contents.Content;

public class HttpProcessor extends Processor {

	private static final int STATE_HTTP_HEADER = 1;
	private static final int STATE_HTTP_CONTENT = 2;
	
	private int state;
	private int taskId;
	
	private String client;
	private int listenerId;
	
	public HttpProcessor(int bufferSize) {
		super(bufferSize);
		state = STATE_HTTP_HEADER;
	}
	
	private void queueContent() {
		// contentLength == innerPointer のはずだが、contentLength 以外の長さになっても困るので;
		Content content = new Content( innerBuffer, 0, getContentLength() );
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
					state = STATE_HTTP_HEADER;
				}
			}
		
		}
			
	}

	// check if ends with CR + LF;
	private boolean isEOL() {
		return (innerPointer >= 2 && innerBuffer[innerPointer - 2] == '\r' && innerBuffer[innerPointer - 1] == '\n'); 
	}

	// HTTP ヘッダを処理して、各種情報を拾っておく;
	// 空行 (ヘッダの終わり) の場合は状態を変更する;
	private void parseHeader() {
		// remove CR + LF;
		String line = new String(innerBuffer, 0, innerPointer - 2);
		// データを読み終えたのでクリアしておく;
		reset();
		// ヘッダの終わり;
		if( line.equals("") ) {
			state = STATE_HTTP_CONTENT;
			return;
		}
		String tmp[] = line.split(":", 2);
		if( tmp.length == 2 && tmp[0].equalsIgnoreCase("Content-Length") ) {
			setContentLength( Parser.parseInt(tmp[1]) );
			return;
		}
		if( tmp.length == 2 && tmp[0].equalsIgnoreCase("TaskId") ) {
			setTaskId( Parser.parseInt(tmp[1]) );
			return;
		}
		if( tmp.length == 2 && tmp[0].equalsIgnoreCase("OriginalClient") ) {
			setClient( tmp[1].trim() );
			return;
		}
		if( tmp.length == 2 && tmp[0].equalsIgnoreCase("UdpListener") ) {
			setListenerId( Parser.parseInt(tmp[1]) );
			return;
		}
	}
	
}
