package util;

import java.io.IOException;
import java.io.InputStream;

public class DebugInputStream extends InputStream {
	
	private InputStream raw;
	
	public DebugInputStream(InputStream raw, int type) {
		this.raw = raw;
	}

	@Override
	public int read() throws IOException {
		return raw.read();
	}

}
