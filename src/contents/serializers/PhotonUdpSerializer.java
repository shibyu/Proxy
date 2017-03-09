package contents.serializers;

import static rules.PhotonUdpRule.*;
import static rules.RuleFactory.*;

import contents.Content;
import contents.IntermediateObject;
import rules.*;

public class PhotonUdpSerializer extends Serializer {
	
	private PhotonUdpRule rule;
	private int bufferSize;
	
	PhotonUdpSerializer() {
		rule = (PhotonUdpRule)(createRule(RULE_PHOTON_UDP));
		bufferSize = 65536;
	}

	@Override
	public Content execute(IntermediateObject object) {
		byte buffer[] = new byte[bufferSize];
		rule.setUserId(buffer, object.getInt(KEY_USER_ID));
		rule.setPacketToken(buffer, object.getInt(KEY_PACKET_TOKEN));
		rule.setSessionId(buffer, object.getInt(KEY_SESSION_ID));
		Object packets[] = (Object[])(object.get(KEY_PACKETS));
		// packet のセットをすると、packet 数も更新されてくる;
		int totalSize = rule.setPackets(buffer,packets);
		return new Content(buffer, 0, totalSize);
	}

}
