package formatters;

import static base.Constant.*;

import java.io.IOException;
import java.io.OutputStream;

import exceptions.ImplementationException;

public class WebSocketFormatter extends Formatter {

	// mask を 0 にしてしまえば分かりやすいので...;
	private boolean debugMode = true;
	
	public static WebSocketFormatter createWebSocketRequestFormatter() {
		return new WebSocketFormatter(TYPE_REQUEST);
	}
	
	public static WebSocketFormatter createWebSocketResponseFormatter() {
		return new WebSocketFormatter(TYPE_RESPONSE);
	}
	
	private int type;
	private byte mask[];
	
	public WebSocketFormatter(int type) {
		this.type = type;
		switch(type) {
		case TYPE_REQUEST:
			generateMask();
			break;
		case TYPE_RESPONSE:
			break;
		default:
			throw new ImplementationException("unknown type: " + type);
		}
	}
	
	private void generateMask() {
		int size = 4;
		mask = new byte[size];
		for( int i = 0; i < size; ++i ) {
			// [1, 255] の値をランダムで作りたい;
			if( debugMode == false ) {
				mask[i] = (byte)(Math.random() * 255 + 1);
			}
		}
	}
	
	private int getContentType() {
		String value = content.getAdditionalInfo(WS_TYPE_KEY);
		if( value != null ) {
			value = value.trim();
			if( value.equals(WS_TYPE_TEXT_VALUE) ) { return WS_TYPE_TEXT; }
			if( value.equals(WS_TYPE_BINARY_VALUE) ) { return WS_TYPE_BINARY; }
			if( value.equals(WS_TYPE_CLOSE_VALUE) ) { return WS_TYPE_CLOSE; }
			if( value.equals(WS_TYPE_PING_VALUE) ) { return WS_TYPE_PING; }
			if( value.equals(WS_TYPE_PONG_VALUE) ) { return WS_TYPE_PONG; }
		}
		throw new ImplementationException("unknown WS type: " + value);
	}
	
	private int getMaskType() {
		return (type == TYPE_REQUEST) ? 0x80 : 0;
	}
	
	private int getBasicLength() {
		if( content.length() < 126 ) { return content.length(); }
		if( content.length() < 65536 ) { return 126; }
		// 多分ここには来ないはず...;
		return 127;
	}
	
	private void outputActualLength(OutputStream output) throws IOException {
		// 既に出力が完了している;
		if( content.length() < 126 ) { return; }
		int shift = 64;
		if( content.length() < 65536 ) { shift = 16; }
		while( shift > 0 ) {
			shift -= 8;
			write( (byte)((content.length() >> shift) & 0xFF), output );
		}
	}
	
	private void outputMask(OutputStream output) throws IOException {
		if( type != TYPE_REQUEST ) { return; }
		output.write(mask);
		debug(util.Util.toHexString(mask));
	}
	
	private byte getMask(int i) {
		if( type != TYPE_REQUEST ) { return 0; }
		return mask[i & 0x03];
	}

	@Override
	public void outputBytes(OutputStream output) throws IOException {
		debug("WS send: ");
		// FIN でない状況が分からないので、取り敢えずは 0x80 を立てておく;
		write( (byte)(0x80 | getContentType()), output );
		write( (byte)(getMaskType() | getBasicLength()), output );
		outputActualLength(output);
		outputMask(output);
		byte payload[] = content.getBytes();
		for( int i = 0; i < content.length(); ++i ) {
			write( (byte)(payload[i] ^ getMask(i)), output );
		}
		debug();
	}
	
	private void write(byte data, OutputStream output) throws IOException {
		debug(util.Util.toHexString(new byte[] { data }));;
		output.write(data);
	}
	
	private void debug() {
		if( debugMode ) { System.out.println(); }
	}
	
	private void debug(String message) {
		if( debugMode ) { System.out.print(message); }
	}
	
}
