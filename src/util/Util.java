package util;

import java.util.Arrays;

import exceptions.DataFormatException;
import exceptions.ImplementationException;

public class Util {
	
	public static String toBinaryString(byte data[]) {
		return toBinaryString(data, 0, data.length);
	}
	
	public static String toBinaryString(byte data[], int offset, int length) {
		StringBuilder builder = new StringBuilder();
		for( int i = 0; i < length; ++i ) {
			if( offset + i < 0 ) { continue; }
			if( offset + i >= data.length ) { break; }
			byte val = data[offset + i];
			for( int j = 7; j >= 0; --j ) {
				builder.append(((val >> j) & 1) == 0 ? '0' : '1');
			}
			builder.append(' ');
		}
		return builder.toString();
	}
	
	public static String toHexString(byte data[]) {
		return toHexString(data, 0, data.length);
	}
	
	public static String toHexString(byte data[], int offset, int length) {
		return toHexString(data, offset, length, -1);
	}

	public static String toHexString(byte data[], int offset, int length, int sep) {
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<length; ++i) {
			if( offset + i < 0 ) { continue; }
			if( offset + i >= data.length ) { break; }
			byte val = data[offset + i];
			builder.append(toHex((val >> 4) & 0x0F)).append(toHex(val & 0x0F));
			if( sep > 0 && (i + 1) % sep == 0 ) {
				builder.append(" ");
			}
		}
		return builder.toString();
	}
	
	private static String toHex(int data) {
		if( 0 <= data && data < 10 ) {
			return "" + data;
		}
		if( 10 <= data && data < 16 ) {
			return "" + (char)('a' + data - 10);
		}
		throw new ImplementationException("out of range [0, 16): " + data);
	}
	
	// IP の match のみ;
	public static boolean hostMatches(String target, String rule) {
		IP targetIP = Parser.parseIP(target);
		IP ruleIP = Parser.parseIP(rule);
		return ruleIP.isInclude(targetIP);
	}
	
	public static byte[] copyByteArray(byte input[]) {
		return copyByteArray(input, 0, input.length);
	}
	
	public static byte[] copyByteArray(byte input[], int offset, int length) {
		return Arrays.copyOfRange(input, offset, length);
	}
	
	public static void writeByteArray(byte output[], int outputOffset, byte input[], int inputOffset, int length) {
		for( int i = 0; i < length; ++i ) {
			output[outputOffset + i] = input[inputOffset + i];
		}
	}
	
	public static void writeByteArray(ByteBuffer output, int outputOffset, byte input[], int inputOffset, int length) {
		for( int i = 0; i < length; ++i ) {
			output.writeByte(outputOffset + i, input[inputOffset + i]);
		}
	}
	
	public static int writeHexString(byte output[], int offset, String hexString) {
		try {
			int size = hexString.length() / 2;
			byte rawString[] = Parser.parseByteArray(size, hexString);
			writeByteArray(output, offset, rawString, 0, size);
			return size;
		}
		catch( DataFormatException e ) {
			throw new ImplementationException("invalid data: " + hexString);
		}
	}
	
	public static int writeHexString(ByteBuffer output, int offset, String hexString) {
		try {
			int size = hexString.length() / 2;
			byte rawString[] = Parser.parseByteArray(size, hexString);
			writeByteArray(output, offset, rawString, 0, size);
			return size;
		}
		catch( DataFormatException e ) {
			throw new ImplementationException("invalid data: " + hexString);
		}
	}
	
	public static byte[] mergeByteArray(byte array1[], byte array2[]) {
		byte result[] = new byte[ array1.length + array2.length ];
		for( int i = 0; i < array1.length; ++i ) {
			result[i] = array1[i];
		}
		for( int i = 0; i < array2.length; ++i ) {
			result[ array1.length + i ] = array2[i];
		}
		return result;
	}
	
}
