package contents.deserializers;

import static rules.PhotonUdpRule.*;

import contents.*;
import rules.*;

public class PhotonUdpDeserializer implements Deserializer {

	private PhotonUdpRule rule;
	
	PhotonUdpDeserializer() {
		rule = RuleFactory.getPhotonUdpRule();
	}

	@Override
	public IntermediateObject deserialize(Content content) {
		byte buffer[] = content.getBytes();
		IntermediateObject result = new IntermediateObject();
		result.put(KEY_USER_ID, rule.getUserId(buffer));
		result.put(KEY_PACKET_TOKEN, rule.getPacketToken(buffer));
		result.put(KEY_SESSION_ID, rule.getSessionId(buffer));
		IntermediateObject packets[] = rule.getPackets(buffer);
		for( IntermediateObject packet : packets ) {
			if( packet.isRUDPOnly() == false ) { result.setRUDPOnly(false); }
		}
		result.put(KEY_PACKETS, packets);
		return result;
	}

}
