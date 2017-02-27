package processors;

import static base.Constant.*;
import static base.LogManager.*;

import contents.Content;
import exceptions.ImplementationException;
import util.*;

// TODO: BinaryProcessor を継承させたい;
public class WebSocketProcessor extends Processor {
	
	private static final int WS_STATE_HEADER = 1;
	private static final int WS_STATE_PAYLOAD = 2;
	
	private int state;
	
	private byte header[];
	private int headerPointer;
	private long contentLength;
	private boolean enableMask;
	private byte mask[];
	private int maskPointer;
	private int headerSize;
	
	// 特に public にして困ることもない気はするけれど、package private にしておく;
	// HttpProcessor の中でしかインスタンスが作られない気がするので...;
	public WebSocketProcessor(int bufferSize) {
		super(bufferSize);
		header = new byte[14];
		initialize();
	}
	
	private void initialize() {
		state = WS_STATE_HEADER;
		headerPointer = 0;
		contentLength = 0;
		enableMask = false;
		mask = new byte[4];
		maskPointer = 0;
		headerSize = 2;
	}
	
	// 1 (FIN) 3 (Reserved) 4 (opcode);
	// 1 (Mask) 7 (length);
	// 16 (length == 126) / 64 (length == 127) / 0 otherwise;
	// 32 (Masking key if mask == 1);
	// payload ...;
	
	private void appendHeader(byte data) {
		header[headerPointer++] = data;
		// header の大きさは最初の 2バイトを読み込むと分かる;
		if( headerPointer == 2 ) {
			enableMask = (header[1] & 0x80) != 0;
			contentLength = (int)(header[1] & 0x7F);
			headerSize += enableMask ? 4 : 0;
			headerSize += contentLength == 126 ? 2 : 0;
			headerSize += contentLength == 127 ? 8 : 0;
		}
		// header を読み終わったところで、中身を拾っておく;
		if( headerPointer == headerSize ) {
			if( contentLength == 126 ) {
				contentLength = DataIO.readInt16(header, 2); 
			}
			else if( contentLength == 127 ) {
				contentLength = DataIO.readInt64(header, 2);
			}
			if( enableMask ) {
				// mask は header の最後にある;
				Util.writeByteArray(mask, 0, header, headerSize - 4, 4);
			}
			state = WS_STATE_PAYLOAD;
		}
	}
	
	private boolean isFinished() {
		return (header[0] & 0x80) != 0;
	}
	
	@Override
	public void process(byte buffer[], int offset, int length) {

		// デバッグ用に取り敢えず全部出力しておく;
		// TODO: ログレベルの定義;
		output("WS recv: " + Util.toHexString(buffer, offset, length), -10);
		
		while( offset < length ) {

			while( state == WS_STATE_HEADER && offset < length ) {
				appendHeader(buffer[offset++]);
			}
			
			while( state == WS_STATE_PAYLOAD && offset < length ) {
				byte data = buffer[offset++];
				data ^= mask[maskPointer & 0x03];
				++maskPointer;
				append(data);
				if( maskPointer == contentLength ) {
					break;
				}
			}
			
			if( state == WS_STATE_PAYLOAD && maskPointer == contentLength ) {
				if( isFinished() ) {
					Content content = new Content(innerBuffer, 0, innerPointer);
					content.putAdditionalInfo(WS_TYPE_KEY, getContentType());
					queueContent(content);
				}
				initialize();
			}

		}
		
	}
	
	private String getContentType() {
		int type = header[0] & 0x0F;
		switch( type ) {
		case WS_TYPE_TEXT:
			return WS_TYPE_TEXT_VALUE;
		case WS_TYPE_BINARY:
			return WS_TYPE_BINARY_VALUE;
		case WS_TYPE_CLOSE:
			return WS_TYPE_CLOSE_VALUE;
		case WS_TYPE_PING:
			return WS_TYPE_PING_VALUE;
		case WS_TYPE_PONG:
			return WS_TYPE_PONG_VALUE;
		default:
			throw new ImplementationException("unknown WS type: " + type);
		}
	}
	
}
