package contents.serializers;

import static contents.SerializationFormat.*;

import exceptions.ImplementationException;

public class SerializerFactory {

	public static Serializer getInstance(String key) {
		if( key.equalsIgnoreCase(NAME_PHOTON) ) {
			return new PhotonSerializer();
		}
		if( key.equalsIgnoreCase(NAME_JSON) ) {
			return new JsonSerializer();
		}
		if( key.equalsIgnoreCase(NAME_PHOTON_UDP) ) {
			return new PhotonUdpSerializer();
		}
		if( key.startsWith(NAME_CUSTOM) ) {
			String name = key.substring(NAME_CUSTOM.length());
			return new ConfigurableSerializer(name);
		}
		throw new ImplementationException("unknown serializer: " + key);
	}
	
}
