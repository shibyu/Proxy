package contents.serializers;

import java.util.Map;

import contents.Content;
import contents.IntermediateObject;
import exceptions.ImplementationException;

//SerializerFactory から作られるだけなので package private にしておく;
class JsonSerializer extends Serializer {
	
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
	public Content execute(IntermediateObject object) {
		StringBuilder builder = new StringBuilder();
		writeMap(builder, object);
		return new Content( builder.toString().getBytes() );
	}
	
	private void writeMap(StringBuilder builder, Map<?, ?> map) {
		if( map.size() == 0 ) {
			builder.append(" ").append("{ }");
			return;
		}
		builder.append("\n").append(padding).append("{\n");
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
		builder.append("\n").append(padding).append("}");
	}
	
	private void writeString(StringBuilder builder, String value) {
		builder.append("\"");
		// TODO: " のエスケープをどうしようか...;
		value = value.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
		builder.append(value);
		builder.append("\"");
	}
	
	private void writeNull(StringBuilder builder) {
		builder.append("null");
	}
	
	private void writeBoolean(StringBuilder builder, boolean value) {
		// そのまま結合しても良さそうな気はするが...？;
		builder.append(value ? "true" : "false");
	}
	
	private void writeLong(StringBuilder builder, long value) {
		builder.append(String.valueOf(value)).append("L");
	}
	
	private void writeInteger(StringBuilder builder, int value) {
		builder.append(String.valueOf(value));
	}
	
	private void writeFloat(StringBuilder builder, float value) {
		builder.append(String.valueOf(value)).append("F");
	}
	
	private void writeDouble(StringBuilder builder, double value) {
		builder.append(String.valueOf(value));
	}
	
	private void writeArray(StringBuilder builder, Object values[]) {
		builder.append("\n").append(padding).append("[\n");
		incrementLevel();
		builder.append(padding);
		String delimiter = "";
		for( Object value : values ) {
			builder.append(delimiter);
			delimiter = ", ";
			writeContent(builder, value);
		}
		decrementLevel();
		builder.append("\n").append(padding).append("]");
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
		else if( value instanceof Long ) {
			writeLong(builder, (Long)(value));
		}
		else if( value instanceof Float ) {
			writeFloat(builder, (Float)(value));
		}
		else if( value instanceof Double ) {
			writeDouble(builder, (Double)(value));
		}
		else if( value instanceof Map ) {
			writeMap(builder, (Map<?, ?>)(value));
		}
		// TODO: array は Object[] に統合できないか検討する...;
		else if( value instanceof Object[] ) {
			writeArray(builder, (Object[])(value));
		}
		else if( value instanceof String ) {
			writeString(builder, (String)(value));
		}
		else {
			throw new ImplementationException("unknown type: " + value);
		}
	}

}
