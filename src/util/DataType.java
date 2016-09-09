package util;

public class DataType {
	
	private static final int TYPE_BINARY = 1;
	private static final int TYPE_HTTP = 2;
	private static final int TYPE_HTTP_REQUEST = 3;
	private static final int TYPE_HTTP_RESPONSE = 4;
	
	private int type;
	
	public DataType(String type) {
		if( type.equalsIgnoreCase("binary") ) { setType(TYPE_BINARY); }
		if( type.equalsIgnoreCase("http") ) { setType(TYPE_HTTP); }
		if( type.equalsIgnoreCase("httprequest") ) { setType(TYPE_HTTP_REQUEST); }
		if( type.equalsIgnoreCase("httpresponse") ) { setType(TYPE_HTTP_RESPONSE); }
	}

	private void setType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isBinary() {
		return type == TYPE_BINARY;
	}
	
	public boolean isHttp() {
		return type == TYPE_HTTP;
	}
	
	public boolean isHttpRequest() {
		return type == TYPE_HTTP_REQUEST;
	}
	
	public boolean isHttpResponse() {
		return type == TYPE_HTTP_RESPONSE;
	}
	
}
