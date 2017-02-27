package photon.data;

public class PhotonData {

	public static final byte TYPE_NULL = 0x2a;
	public static final byte TYPE_BYTE = 0x62;
	public static final byte TYPE_LONG = 0x6c;
	public static final byte TYPE_INT = 0x69;
	public static final byte TYPE_STRING = 0x73;
	public static final byte TYPE_MAP = 0x68;
	public static final byte TYPE_FLOAT = 0x66;
	public static final byte TYPE_DOUBLE = 0x64;
	public static final byte TYPE_BYTE_ARRAY = 0x78;
	public static final byte TYPE_ARRAY = 0x79;
	public static final byte TYPE_OBJECT_ARRAY = 0x7A;
	public static final byte TYPE_BOOLEAN = 0x6F;
	
	// custom data ? 0x63 ?? U16 (length) ...;
	public static final byte TYPE_CUSTOM = 0x63;
	
	public static final String PREFIX_BYTE_KEY = "Byte_0x";
	public static final String PREFIX_INT_KEY = "Int_";
	public static final String PREFIX_LONG_KEY = "Long_";
	public static final String PREFIX_CUSTOM_VALUE = "CUSTOM_";
	public static final String PREFIX_BYTE_VALUE = "BYTE_";
	
	public static final String ARRAY_TYPE_OBJECT = "ObjectArray";
	public static final String ARRAY_TYPE_INT = "IntArray";
	public static final String ARRAY_TYPE_LONG = "LongArray";
	public static final String ARRAY_TYPE_BOOL = "BoolArray";
	public static final String ARRAY_TYPE_STRING = "StringArray";

}
