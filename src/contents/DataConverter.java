package contents;

import contents.deserializers.*;
import contents.serializers.*;

public class DataConverter {
	
	private Deserializer deserializer;
	private Serializer serializer;

	public DataConverter(String dataFrom, String dataTo) {
		deserializer = DeserializerFactory.getInstance(dataFrom);
		serializer = SerializerFactory.getInstance(dataTo);
	}
	
	public Content convert(Content input) {
		return serializer.serialize( deserializer.deserialize(input) );
	}
	
}
