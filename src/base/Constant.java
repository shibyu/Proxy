package base;

import exceptions.ImplementationException;

public class Constant {
	
	public static final int TYPE_REQUEST = 1;
	public static final int TYPE_RESPONSE = 2;

	public static String getTypeString(int type) {
		switch( type ) {
		case TYPE_REQUEST:
			return "Request";
		case TYPE_RESPONSE:
			return "Response";
		default:
			throw new ImplementationException("unknown type: " + type);
		}
	}

}
