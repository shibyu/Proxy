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
	
	private void writeLongKey(String key) {
		if( isLongKey(key) == false ) {
			throw new ImplementationException(key + " is not LongKey");
		}
		writeLong( Parser.parseLong(key.substring(PREFIX_LONG_KEY.length())) );
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
	
	private boolean isLongKey(String key) {
		return key.startsWith(PREFIX_LONG_KEY);
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
		else if( object instanceof Long ) {
			writeLong((Long)(object));
		}
		else if( object instanceof Float ) {
			writeFloat((Float)(object));
		}
		else if( object instanceof Double ) {
			writeDouble((Double)(object));
		}
		else if( object instanceof Byte ) {
			// TODO: ここにはこない？;
			writePhotonByte((Byte)(object));
		}
		else if( object instanceof Map ) {
			writeMap((Map<?, ?>)(object));
		}
		else if( object instanceof Object[] ) {
			writeArray((Object[])(object));
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
	
	private void writeLong(long value) {
		writeByte(TYPE_LONG);
		writeInt64(value);
	}
	
	private void writeInt64(long value) {
		DataIO.writeInt64(buffer, pointer, value);
		pointer += 8;
	}
	
	private void writeFloat(float value) {
		writeByte(TYPE_FLOAT);
		writeFloatCore(value);
	}
	
	private void writeFloatCore(float value) {
		DataIO.writeFloat(buffer, pointer, value);
		pointer += 4;
	}
	
	private void writeDouble(double value) {
		writeByte(TYPE_DOUBLE);
		writeDoubleCore(value);
	}
	
	private void writeDoubleCore(double value) {
		DataIO.writeDouble(buffer, pointer, value);
		pointer += 8;
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
			else if( isLongKey(key) ) { writeLongKey(key); }
			else if( isIntKey(key) ) { writeIntKey(key); }
			else { writeString(key); }
			writeObject(entry.getValue());
		}
	}
	
	private void writeArray(Object values[]) {
		String type = values[0].toString();
		if( type.equalsIgnoreCase(ARRAY_TYPE_OBJECT) ) {
			writeObjectArray(values);
		}
		else if( type.equalsIgnoreCase(ARRAY_TYPE_INT) ) {
			writeIntArray(values);
		}
		else if( type.equalsIgnoreCase(ARRAY_TYPE_LONG) ) {
			writeLongArray(values);
		}
		else if( type.equalsIgnoreCase(ARRAY_TYPE_BOOL) ) {
			writeBooleanArray(values);
		}
		else if( type.equalsIgnoreCase(ARRAY_TYPE_STRING) ) {
			writeStringArray(values);
		}
		else {
			throw new ImplementationException("unknown array type: " + type);
		}
	}
	
	private void writeIntArray(Object values[]) {
		writeByte(TYPE_ARRAY);
		int length = values.length - 1;
		writeInt16(length);
		writeByte(TYPE_INT);
		for( int i = 0; i < length; ++i ) {
			Object value = values[i + 1];
			if( (value instanceof Integer) == false ) {
				throw new ImplementationException("type mismatch: int required: " + value);
			}
			writeInt32((int)(value));
		}
	}
	
	private void writeLongArray(Object values[]) {
		writeByte(TYPE_ARRAY);
		int length = values.length - 1;
		writeInt16(length);
		writeByte(TYPE_LONG);
		for( int i = 0; i < length; ++i ) {
			Object value = values[i + 1];
			if( (value instanceof Long) == false ) {
				throw new ImplementationException("type mismatch: long required: " + value);
			}
			writeInt64((long)(value));
		}
	}
	
	private void writeStringArray(Object values[]) {
		writeByte(TYPE_ARRAY);
		int length = values.length - 1;
		writeInt16(length);
		writeByte(TYPE_STRING);
		for( int i = 0; i < length; ++i ) {
			Object value = values[i + 1];
			if( (value instanceof String) == false ) {
				throw new ImplementationException("type mismatch: String required: " + value);
			}
			writeStringCore((String)(value));
		}
	}
	
	private void writeBooleanArray(Object values[]) {
		writeByte(TYPE_ARRAY);
		int length = values.length - 1;
		writeInt16(length);
		writeByte(TYPE_BOOLEAN);
		for( int i = 0; i < length; ++i ) {
			Object value = values[i + 1];
			if( (value instanceof Boolean) == false ) {
				throw new ImplementationException("type mismatch: boolean required: " + value);
			}
			writeBoolean((boolean)(value));
		}
	}
	
	private void writeObjectArray(Object values[]) {
		writeByte(TYPE_OBJECT_ARRAY);
		int length = values.length - 1;
		writeInt16(length);
		for( int i = 0; i < length; ++i ) {
			writeObject(values[i + 1]);
		}
	}
	
	private void writeString(String value) {
		if( value.startsWith(PREFIX_CUSTOM_VALUE) ) {
			writeCustom(value);
			return;
		}
		if( value.startsWith(PREFIX_BYTE_VALUE) ) {
			writeByteArray(value);
			return;
		}
		writeByte(TYPE_STRING);
		writeStringCore(value);
	}
	
	private void writeCustom(String value) {
		try {
			writeByte(TYPE_CUSTOM);
			String parts[] = value.split("_");
			Util.writeByteArray(buffer, pointer, Parser.parseByteArray(1, parts[1]), 0, 1);
			++pointer;
			int length = parts[2].length() / 2;
			writeInt16(length);
			Util.writeByteArray(buffer, pointer, Parser.parseByteArray(length, parts[2]), 0, length);
			pointer += length;
		}
		catch( DataFormatException e ) {
			throw new ImplementationException("implementation error @ custom data: " + value);
		}
	}
	
	private void writeByteArray(String value) {
		try {
			writeByte(TYPE_BYTE_ARRAY);
			String content = value.substring(PREFIX_BYTE_VALUE.length());
			int length = content.length() / 2;
			writeInt32(length);
			Util.writeByteArray(buffer, pointer, Parser.parseByteArray(length, content), 0, length);
			pointer += length;
		}
		catch( DataFormatException e ) {
			throw new ImplementationException("implementation error @ byte array: " + value);
		}
	}
	
	private void writeStringCore(String value) {
		int length = value.length();
		writeInt16(length);
		Util.writeByteArray(buffer, pointer, value.getBytes(), 0, length);
		pointer += length;
	}
	
}
