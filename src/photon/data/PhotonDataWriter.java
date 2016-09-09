package photon.data;

import static photon.data.PhotonData.*;

import java.util.Map;

import exceptions.DataFormatException;
import exceptions.ImplementationException;
import util.DataIO;
import util.Parser;
import util.Util;

public class PhotonDataWriter {

	private byte buffer[];
	private int pointer;
	
	public PhotonDataWriter(byte buffer[], int pointer) {
		this.buffer = buffer;
		this.pointer = pointer;
	}
	
	public void writeErrorMessage(Object object) {
		writeObject(object);
	}
	
	public int writeContent(Object object) {
		writeByteKeyMap(object);
		return pointer;
	}
	
	private void writeByteKeyMap(Object object) {
		if( (object instanceof Map) == false ) {
			throw new ImplementationException("content is not Map");
		}
		Map<?, ?> map = (Map<?, ?>)(object);
		int length = map.size();
		writeInt16(length);
		for( Map.Entry<?, ?> entry : map.entrySet() ) {
			// この関数では key は byte 型決め打ちなので、型情報を出力しない;
			writeByteKey((String)(entry.getKey()), false);
			writeObject(entry.getValue());
		}
	}
	
	private void writeByteKey(String key, boolean hasType) {
		if( isByteKey(key) == false ) {
			throw new ImplementationException(key + " is not ByteKey");
		}
		try {
			if( hasType ) { writeByte(TYPE_BYTE); }
			writeByte( (Parser.parseByteArray(1, key.substring(PREFIX_BYTE_KEY.length())))[0] );
		}
		catch( DataFormatException e ) {
			throw new ImplementationException(key + " is not ByteKey");
		}		
	}
	
	private void writeIntKey(String key) {
		if( isIntKey(key) == false ) {
			throw new ImplementationException(key + " is not IntKey");
		}
		writeInt( Parser.parseInt(key.substring(PREFIX_INT_KEY.length())) );
	}
	
	private void writeByte(byte value) {
		buffer[pointer++] = value;
	}
	
	private boolean isByteKey(String key) {
		return key.startsWith(PREFIX_BYTE_KEY);
	}
	
	private boolean isIntKey(String key) {
		return key.startsWith(PREFIX_INT_KEY);
	}
	
	private void writeInt16(int value) {
		DataIO.writeInt16(buffer, pointer, value);;
		pointer += 2;
	}
	
	private void writeObject(Object object) {
		if( object == null ) {
			writeNull();
		}
		else if( object instanceof Boolean ) {
			writeBoolean((Boolean)(object));
		}
		else if( object instanceof Integer ) {
			writeInt((Integer)(object));
		}
		else if( object instanceof Byte ) {
			// TODO: ここにはこない？;
			writePhotonByte((Byte)(object));
		}
		else if( object instanceof Map ) {
			writeMap((Map<?, ?>)(object));
		}
		else if( object instanceof int[] ) {
			writeIntArray((int[])(object));
		}
		else if( object instanceof String[] ) {
			writeStringArray((String[])(object));
		}
		else if( object instanceof Object[] ) {
			writeObjectArray((Object[])(object));
		}
		else if( object instanceof String ) {
			String data = (String)(object);
			// TODO: ないとは思うが、ByteKey と同じ形の文字列が出てくるとちょっとだけ面倒;
			if( isByteKey(data) ) { writeByteKey(data, true); }
			else { writeString(data); }
		}
		else {
			throw new ImplementationException("unknown data type: " + object.getClass() + " : " + object);
		}
	}
	
	private void writeNull() {
		writeByte(TYPE_NULL);
	}
	
	private void writeBoolean(boolean value) {
		writeByte(TYPE_BOOLEAN);
		writeByte((byte)(value ? 0x01 : 0x00));
	}
	
	private void writeInt(int value) {
		writeByte(TYPE_INT);
		writeInt32(value);
	}
	
	private void writeInt32(int value) {
		DataIO.writeInt32(buffer, pointer, value);
		pointer += 4;
	}
	
	private void writePhotonByte(byte value) {
		writeByte(TYPE_BYTE);
		writeByte(value);
	}
	
	// 型情報は落ちてしまっているので ? にしておく;
	private void writeMap(Map<?, ?> value) {
		writeByte(TYPE_MAP);
		int length = value.size();
		writeInt16(length);
		for( Map.Entry<?, ?> entry : value.entrySet() ) {
			String key = (String)(entry.getKey());
			// この関数では key は byte 型か分からないので、型情報を出力する;
			if( isByteKey(key) ) { writeByteKey(key, true); }
			else if( isIntKey(key) ) { writeIntKey(key); }
			else { writeString(key); }
			writeObject(entry.getValue());
		}
	}
	
	private void writeIntArray(int values[]) {
		writeByte(TYPE_ARRAY);
		int length = values.length;
		writeInt16(length);
		writeByte(TYPE_INT);
		for( int value : values ) {
			writeInt32(value);
		}
	}
	
	private void writeStringArray(String values[]) {
		writeByte(TYPE_ARRAY);
		int length = values.length;
		writeInt16(length);
		writeByte(TYPE_STRING);
		for( String value : values ) {
			writeStringCore(value);
		}
	}
	
	private void writeObjectArray(Object values[]) {
		writeByte(TYPE_OBJECT_ARRAY);
		int length = values.length;
		writeInt16(length);
		for( Object value : values ) {
			writeObject(value);
		}
	}
	
	private void writeString(String value) {
		writeByte(TYPE_STRING);
		writeStringCore(value);
	}
	
	private void writeStringCore(String value) {
		int length = value.length();
		writeInt16(length);
		Util.writeByteArray(buffer, pointer, value.getBytes(), 0, length);
		pointer += length;
	}
	
}
