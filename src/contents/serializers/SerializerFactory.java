package contents.serializers;

import contents.SerializationFormat;
import exceptions.ImplementationException;

public class SerializerFactory {

	public static Serializer getInstance(String key) {
		Serializer serializer = null;
		if( key.equalsIgnoreCase(SerializationFormat.NAME_PHOTON) ) {
			serializer = new PhotonSerializer();
		}
		else if( key.equalsIgnoreCase(SerializationFormat.NAME_JSON) ) {
			serializer = new JsonSerializer();
		}
		else if( key.equalsIgnoreCase(SerializationFormat.NAME_PHOTON_UDP) ) {
			serializer = new PhotonUdpSerializer();
		}
		else {
			throw new ImplementationException("unknown serializer: " + key);
		}
		return serializer;		
	}
	
}
