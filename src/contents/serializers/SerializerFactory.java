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
		if( key.equalsIgnoreCase(NAME_EXPO_REQUEST) || key.equalsIgnoreCase(NAME_EXPO_RESPONSE) ) {
			return new ExpoSerializer();
		}
		throw new ImplementationException("unknown serializer: " + key);
	}
	
}
