package contents.deserializers;

import contents.SerializationFormat;
import exceptions.ImplementationException;

public class DeserializerFactory {

	public static Deserializer getInstance(String key) {
		Deserializer deserializer = null;
		if( key.equalsIgnoreCase(SerializationFormat.NAME_PHOTON) ) {
			deserializer = new PhotonDeserializer();
		}
		else if( key.equalsIgnoreCase(SerializationFormat.NAME_JSON) ) {
			deserializer = new JsonDeserializer();
		}
		else if( key.equalsIgnoreCase(SerializationFormat.NAME_PHOTON_UDP) ) {
			deserializer = new PhotonUdpDeserializer();
		}
		else {
			throw new ImplementationException("unknown deserializer: " + key);
		}
		return deserializer;		
	}
	
}
