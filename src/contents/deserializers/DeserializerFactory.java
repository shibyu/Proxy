package contents.deserializers;

import static contents.SerializationFormat.*;

import exceptions.ImplementationException;

public class DeserializerFactory {

	public static Deserializer getInstance(String key) {
		if( key.equalsIgnoreCase(NAME_PHOTON) ) {
			return new PhotonDeserializer();
		}
		if( key.equalsIgnoreCase(NAME_JSON) ) {
			return new JsonDeserializer();
		}
		if( key.equalsIgnoreCase(NAME_PHOTON_UDP) ) {
			return new PhotonUdpDeserializer();
		}
		if( key.equalsIgnoreCase(NAME_EXPO_REQUEST) ) {
			return new ExpoDeserializer(true);
		}
		if( key.equalsIgnoreCase(NAME_EXPO_RESPONSE) ) {
			return new ExpoDeserializer(false);
		}
		throw new ImplementationException("unknown deserializer: " + key);
	}
	
}
