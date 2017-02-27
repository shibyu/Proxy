package contents;

import util.CaseInsensitiveMap;

import java.util.*;

public class HttpContent extends Content {

	private String firstLine;
	private CaseInsensitiveMap<String> headers;
	
	public HttpContent(byte content[], int offset, int length, String firstLine, CaseInsensitiveMap<String> headers) {
		super(content, offset, length);
		this.firstLine = firstLine;
		this.headers = headers;
	}
	
	public String getFirstLine() {
		return firstLine;
	}
	
	public Collection<String> getHeaderKeySet() {
		return headers.getOriginalKeyValues();
	}
	
	public String getHeaderValue(String key) {
		return headers.get(key);
	}

}
