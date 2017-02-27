package formatters;

import java.io.*;

import contents.HttpContent;

public class HttpResponseFormatter extends Formatter {
	
	// TODO: 他の status code を真面目に定義する...;
	private static final String STATUS_CODE_OK = "200 OK";
	
	private String statusCode;
	
	public HttpResponseFormatter() {
		super();
		statusCode = STATUS_CODE_OK;
	}
	
	private String getStatusCode() {
		return statusCode;
	}

	@Override
	public void outputBytes(OutputStream output) throws IOException {
		StringBuilder builder = new StringBuilder();
		if( content instanceof HttpContent ) {
			HttpContent httpContent = (HttpContent)(content);
			builder.append(httpContent.getFirstLine()).append("\r\n");
			for( String key : httpContent.getHeaderKeySet() ) {
				if( key.equalsIgnoreCase("Content-Length") ) { continue; }
				builder.append(key).append(": ").append(httpContent.getHeaderValue(key)).append("\r\n");
			}
		}
		else {
			builder.append("HTTP/1.1 ").append(getStatusCode()).append("\r\n");
		}
		builder.append("Content-Length: ").append(getContentLength()).append("\r\n");
		builder.append("\r\n");
		output.write( builder.toString().getBytes() );
		output.write( content.getBytes() );
	}

}
