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
		return convertToTypedArray(result);
	}
	
	private Object convertToTypedArray(List<Object> list) {
		// generics を使って楽に書けそうな気がするのだが、静的な型でないと instanceof が使えないらしいので断念;
		// TODO: 関数群が無駄に増えそうなので、適宜別クラスに util 関数にでもして追い出したい;
		if( isIntArray(list) ) { return toIntArray(list); }
		if( isStringArray(list) ) { return toStringArray(list); }
		return toObjectArray(list);
	}
	
	private boolean isIntArray(List<Object> list) {
		for( Object object : list ) {
			if( (object instanceof Integer) == false ) { return false; }
		}
		return true;
	}
	
	private boolean isStringArray(List<Object> list) {
		for( Object object : list ) {
			if( (object instanceof String) == false ) { return false; }
		}
		return true;
	}
	
	private int[] toIntArray(List<Object> list) {
		int length = list.size();
		int result[] = new int[length];
		for( int i = 0; i < length; ++i ) {
			Object object = list.get(i);
			if( (object instanceof Integer) == false ) { throw new ImplementationException(object + " is not Integer"); }
			result[i] = (Integer)(object);
		}
		return result;
	}
	
	private String[] toStringArray(List<Object> list) {
		int length = list.size();
		String result[] = new String[length];
		for( int i = 0; i < length; ++i ) {
			Object object = list.get(i);
			if( (object instanceof String) == false ) { throw new ImplementationException(object + " is not String"); }
			result[i] = (String)(object);
		}
		return result;
	}
	
	private Object[] toObjectArray(List<Object> list) {
		int length = list.size();
		Object result[] = new Object[length];
		for( int i = 0; i < length; ++i ) {
			result[i] = list.get(i);
		}
		return result;
	}
	
	private String readString() {
		// TODO: " のエスケープをどうするか？;
		search('"');
		int start = pointer;
		for( ; pointer < buffer.length(); ++pointer ) {
			if( isCurrent('"') ) {
				String result = buffer.substring(start, pointer);
				++pointer;
				return result;
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
				return Parser.parseInt(value);
			}
		}
		throw new ImplementationException("invalid JSON format: unexpected end of content");
	}

}
