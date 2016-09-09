package util;

import exceptions.DataFormatException;
import java.util.*;

public class Parser {

	// インスタンス生成はしない;
	private Parser() {
		
	}
	
	public static int parseInt(String data) {
		data = data.trim();
		return Integer.parseInt(data);
	}
	
	public static boolean isNull(String data) {
		return (data.equalsIgnoreCase("null"));
	}
	
	public static boolean isBoolean(String data) {
		return (data.equalsIgnoreCase("true") || data.equalsIgnoreCase("false"));
	}
	
	public static boolean parseBool(String data) throws DataFormatException {
		data = data.trim();
		if( data.equalsIgnoreCase("true") ) {
			return true;
		}
		else if( data.equalsIgnoreCase("false") ) {
			return false;
		}
		else {
			throw new DataFormatException("invalid data format: " + data + " is not boolean");
		}
	}

	// TODO: 実装が適当なので整理する;
	public static Map<String, String> parseMap(String data) {
		Map<String, String> map = new HashMap<String, String>();
		String mappings[] = data.split(",");
		for( String mapping : mappings ) {
			String pair[] = mapping.split("=>");
			if( pair.length != 2 ) { continue; }
			map.put(pair[0].trim(), pair[1].trim());
		}
		return map;
	}

	// TODO: クオートとかそういうのに対応する;
	public static List<String> parseCSV(String data) {
		List<String> list = new ArrayList<String>();
		String parts[] = data.split(",");
		for( String part : parts ) {
			list.add(part.trim());
		}
		return list;
	}
	
	public static Host parseHost(String data) {
		String tmp[] = data.split(":", 2);
		int port = -1;
		if( tmp.length == 2 ) {
			port = parseInt(tmp[1]);
		}
		return new Host(tmp[0], port);
	}
	
	public static IP parseIP(String data) {
		String tmp[] = data.split("/", 2);
		int bits = 32;
		if( tmp.length == 2 ) {
			bits = parseInt(tmp[1]);
		}
		return new IP(tmp[0], bits);
	}
	
	public static byte[] parseByteArray(int size, String value) throws DataFormatException {
		if( value == null ) { size = 0; }
		byte result[] = new byte[size];
		if( size == 0 ) { return result; }
		int position = 0;
		if( value.startsWith("0x") ) { position += 2; }
		for( int i = 0; i < size; ++i ) {
			result[i] |= getValueFromHex(value.charAt(position++));
			result[i] <<= 4;
			result[i] |= getValueFromHex(value.charAt(position++));
		}
		return result;
	}
	
	private static byte getValueFromHex(char c) throws DataFormatException {
		if( '0' <= c && c <= '9' ) { return (byte)(c - '0'); }
		if( 'a' <= c && c <= 'f' ) { return (byte)(c - 'a' + 10); }
		if( 'A' <= c && c <= 'F' ) { return (byte)(c - 'A' + 10); }
		throw new DataFormatException("invalid data format: " + c);
	}
	
}
