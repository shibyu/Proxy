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
		if( key.startsWith(NAME_CUSTOM) ) {
			String name = key.substring(NAME_CUSTOM.length());
			return new ConfigurableDeserializer(name);
		}
		throw new ImplementationException("unknown deserializer: " + key);
	}
	
}
