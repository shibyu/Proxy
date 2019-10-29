package rules;

import contents.Content;
import util.DataIO;

public class Rule {
	
	public static final int DATA_TYPE_INT32 = 1;
	public static final int DATA_TYPE_INT32LE = 2;
	public static final int DATA_TYPE_INT16 = 3;
	public static final int DATA_TYPE_INT16LE = 4;
	
	protected void writeIntValue(byte buffer[], int type, int offset, int value) {
		switch(type) {
		case DATA_TYPE_INT32: 
			DataIO.writeInt32(buffer, offset, value);
			break;
		case DATA_TYPE_INT32LE:
			DataIO.writeInt32LE(buffer, offset, value);
			break;
		case DATA_TYPE_INT16:
			DataIO.writeInt16(buffer, offset, value);
			break;
		case DATA_TYPE_INT16LE:
			DataIO.writeInt16LE(buffer, offset, value);
			break;
		default:
			// TODO: throw exception;
		}
	}
	
	protected int readIntValue(byte buffer[], int type, int offset) {
		switch(type) {
		case DATA_TYPE_INT32:
			return DataIO.readInt32(buffer, offset);
		case DATA_TYPE_INT32LE:
			return DataIO.readInt32LE(buffer, offset);
		case DATA_TYPE_INT16:
			return DataIO.readInt16(buffer, offset);
		case DATA_TYPE_INT16LE:
			return DataIO.readInt16LE(buffer, offset);
		default:
			// TODO: throw exception;
			return 0;
		}
	}
	
	public void format(byte[] bytes) {
		// TODO Auto-generated method stub
		
	}

	public int getHeaderSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public boolean isForceDirect(Content content) {
		return false;
	}

	public boolean checkMagic(byte[] innerBuffer) {
		// TODO Auto-generated method stub
		return false;
	}

	public String emitMagicForDebug(byte[] innerBuffer) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getTotalSize(byte[] innerBuffer) {
		// TODO Auto-generated method stub
		return 0;
	}

}
