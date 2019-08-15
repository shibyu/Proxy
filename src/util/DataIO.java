package util;

public class DataIO {

	public static void writeInt64(byte buffer[], int offset, long value) {
		writeInt(buffer, offset, 4, (int)((value >> 32) & 0xFFFFFFFF));
		writeInt(buffer, offset + 4, 4, (int)(value & 0xFFFFFFFF));
	}
	
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
	
	public static void writeInt64(ByteBuffer buffer, int offset, long value) {
		writeInt(buffer, offset, 4, (int)((value >> 32) & 0xFFFFFFFF));
		writeInt(buffer, offset + 4, 4, (int)(value & 0xFFFFFFFF));
	}
	
	public static void writeInt32(ByteBuffer buffer, int offset, int value) {
		writeInt(buffer, offset, 4, value);
	}
	
	public static void writeInt16(ByteBuffer buffer, int offset, int value) {
		writeInt(buffer, offset, 2, value);
	}
	
	public static void writeInt(ByteBuffer buffer, int offset, int length, int value) {
		for( int i = length - 1; i >= 0; --i ) {
			buffer.writeByte(offset + i, (byte)(value & 0xFF));
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
	
	public static void writeFloat(byte buffer[], int offset, float value) {
		writeInt32(buffer, offset, Float.floatToRawIntBits(value));
	}
	
	public static void writeFloat(ByteBuffer buffer, int offset, float value) {
		writeInt32(buffer, offset, Float.floatToRawIntBits(value));
	}
	
	public static void writeDouble(byte buffer[], int offset, double value) {
		writeInt64(buffer, offset, Double.doubleToRawLongBits(value));
	}
	
	public static void writeDouble(ByteBuffer buffer, int offset, double value) {
		writeInt64(buffer, offset, Double.doubleToRawLongBits(value));
	}
	
	public static long readInt64(byte buffer[], int offset) {
		return readLong(buffer, offset, 8);
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
	
	private static long readLong(byte buffer[], int offset, int length) {
		long result = 0;
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
	
	public static float readFloat(byte buffer[], int offset) {
		return Float.intBitsToFloat(readInt32(buffer, offset));
	}
	
	public static double readDouble(byte buffer[], int offset) {
		return Double.longBitsToDouble(readInt64(buffer, offset));
	}

}
