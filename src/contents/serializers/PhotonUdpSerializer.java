package contents.serializers;

import static rules.PhotonUdpRule.*;
import static rules.RuleFactory.*;
import static base.Constant.*;

import contents.Content;
import contents.IntermediateObject;
import rules.*;
import util.ByteBuffer;

public class PhotonUdpSerializer extends Serializer {
	
	private PhotonUdpRule rule;
	
	PhotonUdpSerializer() {
		rule = (PhotonUdpRule)(createRule(RULE_PHOTON_UDP, TYPE_UNKNOWN));
	}

	@Override
	public Content execute(IntermediateObject object) {
		ByteBuffer buffer = new ByteBuffer();
		rule.setUserId(buffer, object.getInt(KEY_USER_ID));
		rule.setPacketToken(buffer, object.getInt(KEY_PACKET_TOKEN));
		rule.setSessionId(buffer, object.getInt(KEY_SESSION_ID));
		Object packets[] = (Object[])(object.get(KEY_PACKETS));
		// packet のセットをすると、packet 数も更新されてくる;
		int totalSize = rule.setPackets(buffer,packets);
		return new Content(buffer.getBuffer(0, totalSize));
	}

}
