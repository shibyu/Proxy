package contents.deserializers;

import java.util.*;

import contents.Content;
import contents.IntermediateObject;
import exceptions.*;
import util.Parser;

//DeserializerFactory から作られるだけなので package private にしておく;
class JsonDeserializer implements Deserializer {
	
	private String buffer;
	private int pointer;

	@Override
	public IntermediateObject deserialize(Content content) {
		buffer = new String( content.getBytes() );
		pointer = 0;
		IntermediateObject result;
		try {
			result = readMap();
		}
		catch( Exception e ) {
			System.err.println("error @ " + pointer + "\n" + buffer);
			throw e;
		}
		if( readNext() == true ) {
			// TODO: proxy で変に書き換えるとここに到達することがあるので、適切な例外処理を行う;
			throw new ImplementationException("invalid JSON format: unexpected extra data detected");
		}
		return result;
	}
	
	private char getCurrent() {
		return buffer.charAt(pointer);
	}
	
	private boolean isCurrent(char value) {
		return getCurrent() == value;
	}
	
	private boolean readNext() {
		for( ; pointer < buffer.length(); ++pointer ) {
			char value = getCurrent();
			if( isWhiteSpace(value) == false ) {
				return true;
			}
		}
		// 何も見つからなかった...;
		return false;
	}
	
	private boolean isWhiteSpace(char value) {
		// TODO: 空白文字についてもう少しまじめに考える...;
		return (value == ' ' || value == '\t' || value == '\n');
	}
	
	private void search(char value) {
		if( readNext() == false ) {
			throw new ImplementationException("invalid JSON format: unexpected end of content");
		}
		if( isCurrent(value) == false ) {
			throw new ImplementationException("invalid JSON format: unexpected value detected: " + value + " required but found " + getCurrent());
		}
		++pointer;
	}
	
	private IntermediateObject readMap() {
		IntermediateObject result = new IntermediateObject();
		search('{');
		while( true ) {
			// TODO: これが必要なのは最初の一回だけなので、整理する...;
			if( readNext() == false ) {
				throw new ImplementationException("invalid JSON format: unexpected end of content");
			}
			if( isCurrent('}') ) { break; }
			String key = readString();
			search(':');
			Object value = readValue();
			result.put(key, value);
			if( readNext() == false ) {
				throw new ImplementationException("invalid JSON format: unexpected end of content");
			}
			if( isCurrent('}') ) { break; }
			search(',');
		}
		search('}');
		return result;
	}

	private Object readArray() {
		List<Object> result = new ArrayList<Object>();
		search('[');
		while( true ) {
			// TODO: これが必要なのは最初の一回だけなので整理する;
			if( readNext() == false ) {
				throw new ImplementationException("invalid JSON format: unexpected end of content");
			}
			if( isCurrent(']') ) { break; }
			// ここではデータ型は気にしない (最後に適切な型の配列に調整する);
			result.add( readValue() );
			if( readNext() == false ) {
				throw new ImplementationException("invalid JSON format: unexpected end of content");
			}
			if( isCurrent(']') ) { break; }
			search(',');
		}
		search(']');
		Object array[] = new Object[result.size()];
		result.toArray(array);
		return array;
	}

	private String readString() {
		// TODO: " のエスケープをどうするか？;
		search('"');
		boolean escape = false;
		StringBuilder result = new StringBuilder();
		for( ; pointer < buffer.length(); ++pointer ) {
			if( escape ) {
				result.append(getCurrent());
				escape = false;
			}
			else {
				if( isCurrent('\\') ) {
					escape = true;
				}
				else {
					if( isCurrent('"') ) {
						++pointer;
						return result.toString();
					}
					else {
						result.append(getCurrent());
					}
				}
			}
		}
		throw new ImplementationException("invalid JSON format: unexpected end of content");
	}
	
	private Object readValue() {
		if( readNext() == false ) {
			throw new ImplementationException("invalid JSON format: unexpected end of content");
		}
		// String;
		if( isCurrent('"') ) {
			return readString();
		}
		// Array;
		if( isCurrent('[') ) {
			return readArray();
		}
		// Map;
		if( isCurrent('{') ) {
			return readMap();
		}
		int start = pointer;
		for( ; pointer < buffer.length(); ++pointer ) {
			char current = getCurrent();
			if( isWhiteSpace(current) || current == ',' || current == '}' || current == ']' ) {
				String value = buffer.substring(start,  pointer);
				if( Parser.isNull(value) ) {
					return null;
				}
				else if( Parser.isBoolean(value) ) { 
					try {
						return Parser.parseBool(value);
					}
					catch( DataFormatException e ) {
						throw new ImplementationException("implementation error: " + value + " is not boolean");
					}
				}
				else if( value.indexOf(".") >= 0 ) {
					if( value.endsWith("f") || value.endsWith("F") ) {
						value = value.substring(0, value.length() - 1);
						return Parser.parseFloat(value);
					}
					return Parser.parseDouble(value);
				}
				else if( value.endsWith("l") || value.endsWith("L") ) {
					value = value.substring(0, value.length() - 1);
					return Parser.parseLong(value);
				}
				return Parser.parseInt(value);
			}
		}
		throw new ImplementationException("invalid JSON format: unexpected end of content");
	}

}
