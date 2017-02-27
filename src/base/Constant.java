package base;

import exceptions.ImplementationException;

public class Constant {
	
	public static final int HTTP_MODE_NORMAL = 1;
	public static final int HTTP_MODE_WEBSOCKET = 2;
	
	public static final int TYPE_REQUEST = 1;
	public static final int TYPE_RESPONSE = 2;

	public static String getTypeString(int type) {
		switch( type ) {
		case TYPE_REQUEST:
			return "Request";
		case TYPE_RESPONSE:
			return "Response";
		default:
			throw new ImplementationException("unknown type: " + type);
		}
	}

	public static final int WS_TYPE_TEXT = 1;
	public static final int WS_TYPE_BINARY = 2;
	public static final int WS_TYPE_CLOSE = 8;
	public static final int WS_TYPE_PING = 9;
	public static final int WS_TYPE_PONG = 10;
	// TODO: continuation: 0;
	// FIN のときには来ないかも？;

	public static final String WS_TYPE_KEY = "WS_TYPE";
	
	public static final String WS_TYPE_TEXT_VALUE = "WS_TYPE_TEXT";
	public static final String WS_TYPE_BINARY_VALUE = "WS_TYPE_BINARY";
	public static final String WS_TYPE_CLOSE_VALUE = "WS_TYPE_CLOSE";
	public static final String WS_TYPE_PING_VALUE = "WS_TYPE_PING";
	public static final String WS_TYPE_PONG_VALUE = "WS_TYPE_PONG";

}
