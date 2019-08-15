package photon.data;

import static photon.data.PhotonData.*;

import java.util.Map;

import exceptions.DataFormatException;
import exceptions.ImplementationException;
import util.*;

public class PhotonDataWriter {

	private ByteBuffer buffer;
	private int pointer;
	
	public PhotonDataWriter(ByteBuffer buffer, int pointer) {
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
	
	private void writeByteKey(String key, boolean emitType) {
		if( isByteKey(key) == false ) {
			throw new ImplementationException(key + " is not ByteKey");
		}
		try {
			if( emitType ) { writeByte(TYPE_BYTE); }
			writeByte( (Parser.parseByteArray(1, key.substring(PREFIX_BYTE_KEY.length())))[0] );
		}
		catch( DataFormatException e ) {
			throw new ImplementationException(key + " is not ByteKey");
		}		
	}
	
	private void writeIntKey(String key, boolean emitType) {
		if( isIntKey(key) == false ) {
			throw new ImplementationException(key + " is not IntKey");
		}
		writeInt( Parser.parseInt(key.substring(PREFIX_INT_KEY.length())), emitType );
	}
	
	private void writeLongKey(String key, boolean emitType) {
		if( isLongKey(key) == false ) {
			throw new ImplementationException(key + " is not LongKey");
		}
		writeLong( Parser.parseLong(key.substring(PREFIX_LONG_KEY.length())), emitType );
	}
	
	private void writeByte(byte value) {
		buffer.writeByte(pointer++, value);
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
		writeObject(object, true);
	}
	
	private void writeObject(Object object, boolean emitType) {
		if( object == null ) {
			writeNull();
		}
		else if( object instanceof Boolean ) {
			writeBoolean((Boolean)(object), emitType);
		}
		else if( object instanceof Integer ) {
			writeInt((Integer)(object), emitType);
		}
		else if( object instanceof Long ) {
			writeLong((Long)(object), emitType);
		}
		else if( object instanceof Float ) {
			writeFloat((Float)(object), emitType);
		}
		else if( object instanceof Double ) {
			writeDouble((Double)(object), emitType);
		}
		else if( object instanceof Byte ) {
			// TODO: ここにはこない？;
			writePhotonByte((Byte)(object), emitType);
		}
		else if( object instanceof Map ) {
			if( emitType == false ) {
				throw new ImplementationException("not implemented: typed map + map value");
			}
			writeMap((Map<?, ?>)(object));
		}
		else if( object instanceof Object[] ) {
			if( emitType == false ) {
				throw new ImplementationException("not implemented: typed map + array value");
			}
			writeArray((Object[])(object));
		}
		else if( object instanceof String ) {
			String data = (String)(object);
			// TODO: ないとは思うが、ByteKey と同じ形の文字列が出てくるとちょっとだけ面倒;
			if( isByteKey(data) ) {
				if( emitType == false ) {
					throw new ImplementationException("not implemented: typed map + byte key string value");
				}
				writeByteKey(data, true);
			}
			else { writeString(data, emitType); }
		}
		else {
			throw new ImplementationException("unknown data type: " + object.getClass() + " : " + object);
		}
	}
	
	private void writeNull() {
		writeByte(TYPE_NULL);
	}
	
	private void writeBoolean(boolean value, boolean emitType) {
		if( emitType ) {
			writeByte(TYPE_BOOLEAN);
		}
		writeBooleanCore(value);
	}
	
	private void writeBooleanCore(boolean value) {
		writeByte((byte)(value ? 0x01 : 0x00));
	}
	
	private void writeInt(int value, boolean emitType) {
		if( emitType ) {
			writeByte(TYPE_INT);
		}
		writeInt32(value);
	}
	
	private void writeInt32(int value) {
		DataIO.writeInt32(buffer, pointer, value);
		pointer += 4;
	}
	
	private void writeLong(long value, boolean emitType) {
		if( emitType ) {
			writeByte(TYPE_LONG);
		}
		writeInt64(value);
	}
	
	private void writeInt64(long value) {
		DataIO.writeInt64(buffer, pointer, value);
		pointer += 8;
	}
	
	private void writeFloat(float value, boolean emitType) {
		if( emitType ) {
			writeByte(TYPE_FLOAT);
		}
		writeFloatCore(value);
	}
	
	private void writeFloatCore(float value) {
		DataIO.writeFloat(buffer, pointer, value);
		pointer += 4;
	}
	
	private void writeDouble(double value, boolean emitType) {
		if( emitType ) {
			writeByte(TYPE_DOUBLE);
		}
		writeDoubleCore(value);
	}
	
	private void writeDoubleCore(double value) {
		DataIO.writeDouble(buffer, pointer, value);
		pointer += 8;
	}
	
	private void writePhotonByte(byte value, boolean emitType) {
		if( emitType ) {
			writeByte(TYPE_BYTE);
		}
		writeByte(value);
	}
	
	// 型情報は落ちてしまっているので ? にしておく;
	private void writeMap(Map<?, ?> value) {
		// 元々型情報を持っているっぽいので、そちらにあわせて処理する;
		if( value.containsKey(KEY_KEY_TYPE) && value.containsKey(KEY_VALUE_TYPE) ) {
			writeTypedMap(value);
			return;
		}
		writeByte(TYPE_MAP);
		int length = value.size();
		writeInt16(length);
		for( Map.Entry<?, ?> entry : value.entrySet() ) {
			String key = (String)(entry.getKey());
			// この関数では key は byte 型か分からないので、型情報を出力する;
			writeKey(key, true);
			writeObject(entry.getValue());
		}
	}
	
	private void writeTypedMap(Map<?, ?> value) {
		writeByte(TYPE_TYPED_MAP);
		int keyType = getTypeFromString((String)(value.remove(KEY_KEY_TYPE)));
		int valueType = getTypeFromString((String)(value.remove(KEY_VALUE_TYPE)));
		writeByte((byte)(keyType));
		writeByte((byte)(valueType));
		int length = value.size();
		writeInt16(length);
		for( Map.Entry<?, ?> entry : value.entrySet() ) {
			String key = (String)(entry.getKey());
			// key の型が Object でなければ、既に型情報は出力されているので割愛する;
			writeKey(key, keyType == 0);
			writeObject(entry.getValue(), keyType == 0);
		}
	}
	
	private void writeKey(String key, boolean emitType) {
		if( isByteKey(key) ) { writeByteKey(key, emitType); }
		else if( isLongKey(key) ) { writeLongKey(key, emitType); }
		else if( isIntKey(key) ) { writeIntKey(key, emitType); }
		else { writeString(key, emitType); }
	}
	
	private int getTypeFromString(String typeString) {
		if( typeString.equalsIgnoreCase(TYPE_STRING_OBJECT) ) {
			return 0;
		}
		else if( typeString.equalsIgnoreCase(TYPE_STRING_BYTE) ) {
			return TYPE_BYTE;
		}
		else if( typeString.equalsIgnoreCase(TYPE_STRING_INT) ) {
			return TYPE_INT;
		}
		else if( typeString.equalsIgnoreCase(TYPE_STRING_LONG) ) {
			return TYPE_LONG;
		}
		else if( typeString.equalsIgnoreCase(TYPE_STRING_BOOL) ) {
			return TYPE_BOOLEAN;
		}
		else if( typeString.equalsIgnoreCase(TYPE_STRING_FLOAT) ) {
			return TYPE_FLOAT;
		}
		else if( typeString.equalsIgnoreCase(TYPE_STRING_DOUBLE) ) {
			return TYPE_DOUBLE;
		}
		else {
			throw new ImplementationException("unknown type string: " + typeString);
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
			writeBooleanCore((boolean)(value));
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
	
	private void writeString(String value, boolean emitType) {
		if( value.startsWith(PREFIX_CUSTOM_VALUE) ) {
			writeCustom(value, emitType);
			return;
		}
		if( value.startsWith(PREFIX_BYTE_VALUE) ) {
			writeByteArray(value, emitType);
			return;
		}
		if( emitType ) {
			writeByte(TYPE_STRING);
		}
		writeStringCore(value);
	}
	
	private void writeCustom(String value, boolean emitType) {
		try {
			if( emitType ) {
				writeByte(TYPE_CUSTOM);
			}
			else {
				throw new ImplementationException("not implemented: typed map + custom type value");
			}
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
	
	private void writeByteArray(String value, boolean emitType) {
		try {
			if( emitType ) {
				writeByte(TYPE_BYTE_ARRAY);
			}
			else {
				throw new ImplementationException("not implemented: typed map + byte array value");
			}
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
