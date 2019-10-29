package util;

public class ByteBuffer {
	
	private byte raw[];
	private static final int DEFAULT_SIZE = 1024;
	// 16MB;
	private static final int MAX_SIZE = 1 << 24;
	
	public ByteBuffer() {
		raw = new byte[DEFAULT_SIZE];
	}
	
	private void extend(int size) {
		if( size >= MAX_SIZE ) {
			// TODO: throw exception;
			return;
		}
		int reqSize = raw.length;
		while( reqSize <= size ) {
			reqSize *= 2;
		}
		byte extra[] = new byte[reqSize];
		Util.writeByteArray(extra, 0, raw, 0, raw.length);
		raw = extra;
	}
	
	public byte[] getBuffer(int offset, int length) {
		extend(offset + length - 1);
		return Util.copyByteArray(raw, offset, length);
	}
	
	public byte getByte(int offset) {
		extend(offset);
		return raw[offset];
	}
	
	public void writeByte(int offset, byte value) {
		
	}

}
