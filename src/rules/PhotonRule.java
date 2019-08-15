package rules;

import static base.Constant.*;

import contents.*;
import exceptions.ImplementationException;
import exceptions.UnknownException;
import photon.data.PhotonDataReader;
import photon.data.PhotonDataWriter;
import util.*;

public class PhotonRule extends CustomRule {

	// ?? size (int32) 便宜上ここまでをヘッダにしておく;
	// 先頭は FB かと思っていたらそうではなかった...;
	// TODO: F0 のときは size がこないっぽい...;
	// flag (int16) F3 type (byte) code (byte) ここからは本体扱いで、後ろにいろいろつく;
	// UDP との兼ね合いで、F3 以降を core として取り扱うのが良さそうな感じになっている;
	
	// 先頭の一文字は固定ではなかった...;
	public static final String KEY_CONST = "Const";
	private static final int POS_CONST = 0;
	
	public static final String KEY_ADHOC = "Adhoc";

	public static final String KEY_FLAG = "Flag";
	private static final int POS_FLAG = 5;

	public static final int POS_TCP_MAGIC = 7;

	// ここから先が Photon の core 扱いになるため、位置をリセットする;	
	public static final String KEY_MAGIC = "Magic";
	private static final int POS_MAGIC = 0;
	
	public static final String KEY_PROTOCOL_TYPE = "ProtocolType";
	private static final int POS_PROTOCOL_TYPE = 1;
	
	public static final String KEY_OPERATION_CODE = "OperationCode";
	private static final int POS_OPERATION_CODE = 2;
	
	// response code があるのは response のときだけ;
	public static final String KEY_RESPONSE_CODE = "ResponseCode";
	private static final int POS_RESPONSE_CODE = 3;

	public static final String KEY_ERROR_MESSAGE = "ErrorMessage";

	public static final String KEY_CONTENT = "Content";
	// response code の分だけ content の位置がずれる;
	private static final int POS_RESPONSE_CONTENT = 5;
	private static final int POS_CONTENT = 3;
	
	private static final int PROTOCOL_TYPE_REQUEST = 2;
	private static final int PROTOCOL_TYPE_RESPONSE = 3;
	private static final int PROTOCOL_TYPE_EVENT = 4;
	// TODO: 他のプロトコル種別... (UDP 側ではあるようだが、TCP 側では未確認...);
	
	private int type;

	// RuleFactory 経由でのみインスタンス生成することにするので、package private にしておく;
	PhotonRule(int type) {
		// 0xFB 開始かと思ったらそんなことはなかった...;
		super(5, 1, "int32", true, 0, 0, "");
		this.type = type;
	}
	
	@Override
	public int getTotalSize(byte buffer[]) {
		if( buffer[0] == (byte)(0xFB) ) {
			return super.getTotalSize(buffer);
		}
		if( buffer[0] == (byte)(0xF0) ) {
			switch( type ) {
			case TYPE_REQUEST:
				return 5;
			case TYPE_RESPONSE:
				return 9;
			default:
				throw new UnknownException("unknown packet type: " + type);
			}
		}
		throw new ImplementationException("unknown packet type: " + Util.toHexString(buffer, 0, 5));
	}
	
	@Override
	public boolean isForceDirect(Content content) {
		byte buffer[] = content.getBytes();
		// とにかくこのパケットが謎過ぎるので、直接通信にしてしまう;
		if( buffer[0] == (byte)(0xF0) ) {
			return true;
		}
		return false;
	}
	
	public int getFlag(byte buffer[]) {
		return (int)(DataIO.readInt16(buffer, POS_FLAG));
	}
	
	public void setFlag(ByteBuffer buffer, int value) {
		DataIO.writeInt16(buffer, POS_FLAG, value);
	}
	
	public int getConst(byte buffer[]) {
		return (int)(buffer[POS_CONST]);
	}
	
	public void setConst(ByteBuffer buffer, int value) {
		buffer.writeByte(POS_CONST, (byte)(value));
	}
	
	public int getMagic(byte buffer[], int offset) {
		return (int)(buffer[offset + POS_MAGIC]);
	}
	
	public void setMagic(ByteBuffer buffer, int offset, int value) {
		buffer.writeByte(offset + POS_MAGIC, (byte)(value));
	}
	
	public int getProtocolType(byte buffer[], int offset) {
		return (int)(buffer[offset +  POS_PROTOCOL_TYPE]);
	}
	
	public int getProtocolType(ByteBuffer buffer, int offset) {
		return (int)(buffer.getByte(offset + POS_PROTOCOL_TYPE));
	}
	
	public void setProtocolType(ByteBuffer buffer, int offset, int value) {
		buffer.writeByte(offset + POS_PROTOCOL_TYPE, (byte)(value));
	}
	
	public boolean hasResponseCode(byte buffer[], int offset) {
		return hasResponseCode(getProtocolType(buffer, offset));
	}
	
	public boolean hasResponseCode(ByteBuffer buffer, int offset) {
		return hasResponseCode(getProtocolType(buffer, offset));
	}
	
	private boolean hasResponseCode(int type) {
		switch( type ) {
		case PROTOCOL_TYPE_RESPONSE:
			return true;
		case PROTOCOL_TYPE_REQUEST:
		case PROTOCOL_TYPE_EVENT:
		default:
			return false;
		}
	}
	
	public int getOperationCode(byte buffer[], int offset) {
		return (int)(buffer[offset + POS_OPERATION_CODE]);
	}
	
	public void setOperationCode(ByteBuffer buffer, int offset, int value) {
		buffer.writeByte(offset + POS_OPERATION_CODE, (byte)(value));
	}
	
	public int getResponseCode(byte buffer[], int offset) {
		if( hasResponseCode(buffer, offset) ) {
			return DataIO.readInt16(buffer, offset + POS_RESPONSE_CODE);
		}
		else {
			throw new ImplementationException("no response code: " + getProtocolType(buffer, offset));
		}
	}
	
	public void setResponseCode(ByteBuffer buffer, int offset, int value) {
		if( hasResponseCode(buffer, offset) ) {
			DataIO.writeInt16(buffer, offset + POS_RESPONSE_CODE, value);
			// この後に error message を出力する必要があるが、可変長のため、setContent の中でやる;
			// 長さの管理は PhotonDataWriter がやってくれる;
		}
		else {
			throw new ImplementationException("no response code: " + getProtocolType(buffer, offset));
		}
	}
	
	public Object getContent(byte buffer[], int offset) {
		switch( getProtocolType(buffer, offset) ) {
		case PROTOCOL_TYPE_RESPONSE:
			return getContent(buffer, offset + POS_RESPONSE_CONTENT, true);
		case PROTOCOL_TYPE_REQUEST:
		case PROTOCOL_TYPE_EVENT:
			return getContent(buffer, offset + POS_CONTENT, false);
		default:
			return Util.toHexString(buffer, offset + POS_CONTENT, buffer.length - (offset + POS_CONTENT));
		}
	}
	
	// top level は key が byte であることが固定されている;
	// それ以降の map は key のデータ型がついてくるので、類似度は高いが別に実装する必要がある;
	public IntermediateObject getContent(byte buffer[], int offset, boolean hasErrorMessage) {
		PhotonDataReader reader = new PhotonDataReader(buffer, offset, hasErrorMessage);
		return reader.readContent();
	}
	
	public int setContent(ByteBuffer buffer, int offset, Object object, Object errorMessage) {
		switch( getProtocolType(buffer, offset) ) {
		case PROTOCOL_TYPE_RESPONSE:
		{
			PhotonDataWriter writer = new PhotonDataWriter(buffer, offset + POS_RESPONSE_CONTENT);
			writer.writeErrorMessage(errorMessage);
			return writer.writeContent(object);
		}
		case PROTOCOL_TYPE_REQUEST:
		case PROTOCOL_TYPE_EVENT:
		{
			PhotonDataWriter writer = new PhotonDataWriter(buffer, offset + POS_CONTENT);
			return writer.writeContent(object);
		}
		default:
			String hexString = (String)(object);
			int size = Util.writeHexString(buffer, offset + POS_CONTENT, hexString);
			return offset + POS_CONTENT + size;
		}
	}
	
	public void getPhotonCore(IntermediateObject object, byte buffer[], int offset) {
		object.put(KEY_MAGIC, getMagic(buffer, offset));
		object.put(KEY_PROTOCOL_TYPE, getProtocolType(buffer, offset));
		object.put(KEY_OPERATION_CODE, getOperationCode(buffer, offset));
		if( hasResponseCode(buffer, offset) ) {
			object.put(KEY_RESPONSE_CODE, getResponseCode(buffer, offset));
		}
		Object data = getContent(buffer, offset);
		// error message は content の中にあると問題になるので一つ上のレイヤーに移動させる;
		if( hasResponseCode(buffer, offset) ) {
			if( (data instanceof IntermediateObject) == false ) {
				throw new ImplementationException("unknown data format: " + data.getClass() + " : " + data);
			}
			IntermediateObject intermediateData = (IntermediateObject)(data);
			object.put(KEY_ERROR_MESSAGE, intermediateData.remove(KEY_ERROR_MESSAGE));
		}
		object.put(KEY_CONTENT, data);
	}
	
	public int setPhotonCore(IntermediateObject object, ByteBuffer buffer, int offset) {
		setMagic(buffer, offset, object.getInt(KEY_MAGIC));
		setProtocolType(buffer, offset, object.getInt(KEY_PROTOCOL_TYPE));
		setOperationCode(buffer, offset, object.getInt(KEY_OPERATION_CODE));
		if( hasResponseCode(buffer, offset) ) {
			setResponseCode(buffer, offset, object.getInt(KEY_RESPONSE_CODE));
		}
		return setContent(buffer, offset, object.get(KEY_CONTENT), object.get(KEY_ERROR_MESSAGE));
	}
	
}
