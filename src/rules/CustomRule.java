package rules;

import base.Config;
import exceptions.*;
import util.*;

// TODO: 汎用性も今一つだし、そもそもこれが実用に耐えるかも不明なので、適切な抽象化を検討する必要がありそう;
// TODO: そもそもデータ変換をやりやすくするために、適切な設計を検討する必要がありあそう;

public class CustomRule {
	
	public static final int DATA_TYPE_NONE = 0;
	public static final int DATA_TYPE_INT32 = 2;
	public static final int DATA_TYPE_INT32LE = 3;
	public static final int DATA_TYPE_INT16 = 4;
	public static final int DATA_TYPE_INT16LE = 5;
	
	private int headerSize;
	private int lengthOffset;
	private int lengthType;
	
	private boolean isLengthIncludesHeader;
	
	private int magicSize;
	private int magicOffset;
	private byte magicValue[];

	// RuleFactory 経由でのみインスタンス生成することにするので、package private にしておく;
	CustomRule(String category, Config config) {
		this(config.getIntProperty(category, "HeaderSize"),
				config.getIntProperty(category, "LengthOffset"),
				config.getStringProperty(category, "LengthType"),
				config.getBoolProperty(category, "IsLengthIncludesHeader", true, false),
				config.getIntProperty(category, "MagicSize", true, 0),
				config.getIntProperty(category, "MagicOffset", true, 0),
				config.getStringProperty(category, "MagicValue", true, null));
	}
	
	public CustomRule(int headerSize, int lengthOffset, String lengthType, boolean isLengthIncludesHeader, int magicSize, int magicOffset, String magicValue) {
		this.headerSize = headerSize;
		this.lengthOffset = lengthOffset;
		setLengthType(lengthType);
		this.isLengthIncludesHeader = isLengthIncludesHeader;
		this.magicSize = magicSize;
		this.magicOffset = magicOffset;
		try {
			this.magicValue = Parser.parseByteArray(magicSize, magicValue);
		}
		catch( DataFormatException e ) {
			throw new ConfigurationException("invalid magic word: " + magicValue);
		}
	}
	
	private void setLengthType(String type) {
		if( type.equalsIgnoreCase("int32") ) {
			lengthType = DATA_TYPE_INT32;
		}
		else if( type.equalsIgnoreCase("int32le") ) {
			lengthType = DATA_TYPE_INT32LE;
		}
		else if( type.equalsIgnoreCase("int16") ) {
			lengthType = DATA_TYPE_INT16;
		}
		else if( type.equalsIgnoreCase("int16le") ) {
			lengthType = DATA_TYPE_INT16LE;
		}
		else {
			throw new ConfigurationException("unknown data type: " + type);
		}
	}

	public int getHeaderSize() {
		return headerSize;
	}
	
	public int getLengthOffset() {
		return lengthOffset;
	}
	
	public int getLengthType() {
		return lengthType;
	}
	
	private int getContentLength(int contentLength) {
		// header の長さが既に考慮されている場合は問題ない;
		if( isLengthIncludesHeader ) { return contentLength; }
		// header の長さも含める必要があるのであれば足さないとダメ;
		return contentLength + headerSize;
	}
	
	// protocol によって決まっている記述すべき長さ;
	public int getProtocolRequiredLength(int contentLength) {
		// header の長さも含めての長さであればそのまま;
		if( isLengthIncludesHeader ) { return contentLength; }
		// header の長さが固定長で、除いて記述する場合には、content の長さから引かないとダメ;
		return contentLength - headerSize;
	}
	
	public boolean checkMagic(byte buffer[]) {
		for( int i = 0; i < magicSize; ++i ) {
			if( buffer[magicOffset + i] != magicValue[i] ) { return false; }
		}
		return true;
	}
	
	public String emitMagicForDebug(byte buffer[]) {
		return Util.toHexString(magicValue) + " required; but found " + Util.toHexString(buffer, magicOffset, magicSize);
	}
	
	public void format(byte buffer[]) {
		// TODO: これは呼ばなくてもいいかも？;
		fillMagic(buffer);
		fillLength(buffer);
	}
	
	private void fillMagic(byte buffer[]) {
		for( int i = 0; i < magicSize; ++i ) {
			buffer[magicOffset + i] = magicValue[i];
		}
	}
	
	private void fillLength(byte buffer[]) {
		int offset = getLengthOffset();
		int length = getProtocolRequiredLength( buffer.length );
		switch( getLengthType() ) {
		case DATA_TYPE_INT32:
			DataIO.writeInt32(buffer, offset, length);
			break;
		case DATA_TYPE_INT32LE:
			DataIO.writeInt32LE(buffer, offset, length);
			break;
		case DATA_TYPE_INT16:
			DataIO.writeInt16(buffer, offset, length);
			break;
		case DATA_TYPE_INT16LE:
			DataIO.writeInt16LE(buffer, offset, length);
			break;
		case DATA_TYPE_NONE:
		default:
			throw new ImplementationException("unknown data type: " + getLengthType());
		}
	}
	
	public int getContentLength(byte buffer[]) {
		int offset = getLengthOffset();
		int length = 0;
		switch( getLengthType() ) {
		case DATA_TYPE_INT32:
			length = DataIO.readInt32(buffer, offset);
			break;
		case DATA_TYPE_INT32LE:
			length = DataIO.readInt32LE(buffer, offset);
			break;
		case DATA_TYPE_INT16:
			length = DataIO.readInt16(buffer, offset);
			break;
		case DATA_TYPE_INT16LE:
			length = DataIO.readInt16LE(buffer, offset);
			break;
		case DATA_TYPE_NONE:
		default:
			throw new ImplementationException("invalid data type: " + getLengthType());	
		}
		return getContentLength( length );
	}
	
}
