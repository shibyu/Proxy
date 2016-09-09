package util;

public class DataIO {

	public static void writeInt32(byte buffer[], int offset, int value) {
		writeInt(buffer, offset, 4, value);
	}
	
	public static void writeInt16(byte buffer[], int offset, int value) {
		writeInt(buffer, offset, 2, value);
	}
	
	public static void writeInt(byte buffer[], int offset, int length, int value) {
		for( int i = length - 1; i >= 0; --i ) {
			buffer[offset + i] = (byte)(value & 0xFF);
			value >>= 8;
		}
	}
	
	public static void writeInt32LE(byte buffer[], int offset, int value) {
		writeIntLE(buffer, offset, 4, value);
	}
	
	public static void writeInt16LE(byte buffer[], int offset, int value) {
		writeIntLE(buffer, offset, 2, value);
	}
	
	public static void writeIntLE(byte buffer[], int offset, int length, int value) {
		for( int i = 0; i < length; ++i ) {
			buffer[offset + i] = (byte)(value & 0xFF);
			value >>= 8;
		}
	}
	
	public static int readInt32(byte buffer[], int offset) {
		return readInt(buffer, offset, 4);
	}
	
	public static int readInt16(byte buffer[], int offset) {
		return readInt(buffer, offset, 2);
	}
	
	public static int readInt(byte buffer[], int offset, int length) {
		int result = 0;
		for( int i = 0; i < length; ++i ) {
			result <<= 8;
			result += buffer[offset + i] & 0xFF;
		}
		return result;
	}
	
	public static int readInt32LE(byte buffer[], int offset) {
		return readIntLE(buffer, offset, 4);
	}
	
	public static int readInt16LE(byte buffer[], int offset) {
		return readIntLE(buffer, offset, 2);
	}
		
	public static int readIntLE(byte buffer[], int offset, int length) {
		int result = 0;
		for( int i = length - 1; i >= 0; --i ) {
			result <<= 8;
			result += buffer[offset + i] & 0xFF;
		}
		return result;
	}

}
