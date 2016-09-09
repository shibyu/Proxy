package formatters;

import java.util.*;
import java.io.*;

import contents.Content;

import util.Host;
import base.Constant;

public class HttpRequestFormatter extends Formatter {

	private static final String METHOD_GET = "GET";
	private static final String METHOD_POST = "POST";
	// TODO: 他の method も真面目に対応する...;
	
	private String method;
	private String uri;
	
	private String host;
	private int port;

	private Map<String, String> additionalHeader;
	
	
	public HttpRequestFormatter(Host host) {
		this(host.getHost(), host.getPort());
	}
	
	public HttpRequestFormatter(String host, int port) {
		this(host, port, "/");
		additionalHeader = new HashMap<String, String>();
	}
	
	public void setupSlaveOption(int taskId, int type) {
		addHeader("TaskId", String.valueOf(taskId));
		String typeString = Constant.getTypeString(type);
		addHeader("SlaveType", typeString);
		// Fiddler 上で視認性を改善するために taskId と type を入れておく (type は port から分からなくもないが...);
		if( uri.equalsIgnoreCase("/") ) {
			uri = "/" + taskId + "/" + typeString;
		}
	}
	
	public void addHeader(String key, String value) {
		additionalHeader.put(key, value);
	}
	
	public HttpRequestFormatter(String host, int port, String uri) {
		super();
		method = METHOD_GET;
		this.host = host;
		this.port = port;
		setUri(uri);
	}
	
	@Override
	public void setContent(Content content) {
		super.setContent(content);
		// content があるので method を POST にする;
		method = METHOD_POST;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	private String getMethod() { return method; }

	private String getUri() {
		return uri;
	}

	private String getHost() { return host; }
	private int getPort() { return port; }
	
	@Override
	public void outputBytes(OutputStream output) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(getMethod()).append(" ").append("http://").append(getHost()).append(":").append(getPort()).append(getUri()).append(" HTTP/1.1\r\n");
		builder.append("Host: ").append(getHost()).append(":").append(getPort()).append("\r\n");
		for( Map.Entry<String, String> pair : additionalHeader.entrySet() ) {
			builder.append(pair.getKey()).append(": ").append(pair.getValue()).append("\r\n");
		}
		if( hasContent() ) {
			builder.append("Content-Length: ").append(content.length()).append("\r\n");
		}
		builder.append("\r\n");
		output.write( builder.toString().getBytes() );
		if( hasContent() ) {
			output.write( content.getBytes() );
		}
	}

}
