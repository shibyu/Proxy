package contents.serializers;

import java.util.Map;

import contents.Content;
import contents.IntermediateObject;
import exceptions.ImplementationException;

//SerializerFactory から作られるだけなので package private にしておく;
class JsonSerializer implements Serializer {
	
	private int level;
	private String padding;

	JsonSerializer() {
		level = 0;
		padding = "";
	}
	
	private void updatePadding() {
		StringBuilder builder = new StringBuilder();
		for( int i = 0; i < level; ++i ) {
			builder.append("  ");
		}
		padding = builder.toString();
	}
	
	private void incrementLevel() {
		++level;
		updatePadding();
	}
	
	private void decrementLevel() {
		--level;
		updatePadding();
	}

	@Override
	public Content serialize(IntermediateObject object) {
		StringBuilder builder = new StringBuilder();
		writeMap(builder, object);
		return new Content( builder.toString().getBytes() );
	}
	
	private void writeMap(StringBuilder builder, Map<?, ?> map) {
		if( map.size() == 0 ) {
			builder.append(padding).append("{ }\n");
			return;
		}
		builder.append(padding).append("{\n");
		incrementLevel();
		String delimiter = "";
		for( Map.Entry<?, ?> entry : map.entrySet() ) {
			builder.append(delimiter);
			delimiter = ",\n";
			builder.append(padding);
			writeString(builder, (String)(entry.getKey()));
			builder.append(": ");
			writeContent(builder, entry.getValue());
		}
		decrementLevel();
		builder.append("\n").append(padding).append("}\n");
	}
	
	private void writeString(StringBuilder builder, String value) {
		builder.append("\"");
		// TODO: " のエスケープをどうしようか...;
		builder.append(value);
		builder.append("\"");
	}
	
	private void writeNull(StringBuilder builder) {
		builder.append("null");
	}
	
	private void writeBoolean(StringBuilder builder, boolean value) {
		builder.append(value ? "true" : "false");
	}
	
	private void writeInteger(StringBuilder builder, int value) {
		builder.append(String.valueOf(value));
	}
	
	private void writeIntArray(StringBuilder builder, int values[]) {
		builder.append(padding).append("[\n");
		incrementLevel();
		String delimiter = "";
		for( int value : values ) {
			builder.append(delimiter);
			delimiter = ", ";
			writeInteger(builder, value);
		}
		decrementLevel();
		builder.append(padding).append("]\n");
	}
	
	private void writeStringArray(StringBuilder builder, String values[]) {
		builder.append(padding).append("[\n");
		incrementLevel();
		String delimiter = "";
		for( String value : values ) {
			builder.append(delimiter);
			delimiter = ", ";
			builder.append("\"").append(value).append("\"");
		}
		decrementLevel();
		builder.append(padding).append("]\n");
	}
	
	private void writeObjectArray(StringBuilder builder, Object values[]) {
		builder.append(padding).append("[\n");
		incrementLevel();
		String delimiter = "";
		for( Object value : values ) {
			builder.append(delimiter);
			delimiter = ", ";
			writeContent(builder, value);
		}
		decrementLevel();
		builder.append(padding).append("]\n");
	}
	
	private void writeContent(StringBuilder builder, Object value) {
		if( value == null ) {
			writeNull(builder);
		}
		else if( value instanceof Boolean ) {
			writeBoolean(builder, (Boolean)(value));
		}
		else if( value instanceof Integer ) {
			writeInteger(builder, (Integer)(value));
		}
		else if( value instanceof Map ) {
			writeMap(builder, (Map<?, ?>)(value));
		}
		// TODO: array は Object[] に統合できないか検討する...;
		else if( value instanceof int[] ) {
			writeIntArray(builder, (int[])(value));
		}
		else if( value instanceof String[] ) {
			writeStringArray(builder, (String[])(value));
		}
		else if( value instanceof Object[] ) {
			writeObjectArray(builder, (Object[])(value));
		}
		else if( value instanceof String ) {
			writeString(builder, (String)(value));
		}
		else {
			throw new ImplementationException("unknown type: " + value);
		}
	}

}
