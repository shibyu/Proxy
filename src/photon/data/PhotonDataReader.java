package photon.data;

import static base.LogManager.*;
import static photon.data.PhotonData.*;

import contents.IntermediateObject;
import exceptions.ImplementationException;
import rules.PhotonRule;
import util.DataIO;
import util.Util;

public class PhotonDataReader {
	
	private byte data[];
	private int pointer;
	private boolean hasErrorMessage;

	public PhotonDataReader(byte data[], int pointer, boolean hasErrorMessage) {
		this.data = data;
		this.pointer = pointer;
		this.hasErrorMessage = hasErrorMessage;
	}
	
	private int readInt16() {
		int result = DataIO.readInt16(data, pointer);
		pointer += 2;
		return result;
	}
	
	private int readInt32() {
		int result = DataIO.readInt32(data, pointer);
		pointer += 4;
		return result;
	}
	
	private String readString() {
		int length = readInt16();
		String result = new String(data, pointer, length);
		pointer += length;
		return result;
	}
	
	private boolean readBoolean() {
		int result = readByte();
		return result != 0;
	}
	
	private Object readArray() {
		int length = readInt16();
		int type = readByte();
		switch( type ) {
		case TYPE_INT:
			return readIntArray(length);
		case TYPE_STRING:
			return readStringArray(length);
		default:
			throw new ImplementationException("not implemented array type: " + type);
		}
	}
	
	private Object[] readObjectArray() {
		int length = readInt16();
		Object result[] = new Object[length];
		for( int i = 0; i < length; ++i ) {
			result[i] = readObject();
		}
		return result;
	}
	
	private int[] readIntArray(int length) {
		int result[] = new int[length];
		for( int i = 0; i < length; ++i ) {
			result[i] = readInt32();
		}
		return result;
	}
	
	private String[] readStringArray(int length) {
		String result[] = new String[length];
		for( int i = 0; i < length; ++i ) {
			result[i] = readString();
		}
		return result;
	}
	
	private byte readByte() {
		return data[pointer++];
	}
	
	private IntermediateObject readMap() {
		int length = readInt16();
		IntermediateObject result = new IntermediateObject();
		for( int i = 0; i < length; ++i ) {
			String key = readKey();
			Object value = readObject();
			result.put(key, value);
		}
		return result;
	}
	
	public IntermediateObject readContent() {
		String errorMessage = null;
		if( hasErrorMessage ) {
			Object object = readObject();
			if( object == null ) {
				errorMessage = null;
			}
			else if( object instanceof String ) {
				errorMessage = (String)(object);
			}
			else {
				throw new ImplementationException("unknown data format: " + object.getClass() + " : " + object);
			}
		}
		IntermediateObject result = readByteKeyMap();
		if( hasErrorMessage ) {
			result.put(PhotonRule.KEY_ERROR_MESSAGE, errorMessage);
		}
		return result;
	}
	
	private IntermediateObject readByteKeyMap() {
		int length = readInt16();
		IntermediateObject result = new IntermediateObject();
		for( int i = 0; i < length; ++i ) {
			String key = createByteKey();
			Object value = readObject();
			result.put(key, value);
		}
		return result;
	}
	
	private Object readObject() {
		int type = readByte();
		switch( type ) {
		case TYPE_BYTE:
			// ここで byte を返してしまっても良いのだが、結局 byte 型であることを残さないといけないので、ByteKey にしてしまう;
			return createByteKey();
			// return readByte();
		case TYPE_INT:
			return readInt32();
		case TYPE_STRING:
			return readString();
		case TYPE_BOOLEAN:
			return readBoolean();
		case TYPE_ARRAY:
			return readArray();
		case TYPE_MAP:
			return readMap();
		case TYPE_OBJECT_ARRAY:
			return readObjectArray();
		case TYPE_NULL:
			return null;
		default:
			trace("error @ " + pointer + "\n" + Util.toHexString(data));
			throw new ImplementationException("unknown type: " + type);
		}
	}
	
	private String readKey() {
		int type = readByte();
		switch( type ) {
		case TYPE_BYTE:
			return createByteKey();
		case TYPE_STRING:
			return readString();
		case TYPE_INT:
			return createIntKey();
		default:
			throw new ImplementationException("not implemented key type: " + type);	
		}
	}
	
	private String createByteKey() {
		byte key = readByte();
		return PREFIX_BYTE_KEY + Util.toHexString(new byte[] { key });
	}
	
	private String createIntKey() {
		int key = readInt32();
		return PREFIX_INT_KEY + key;
	}

}
