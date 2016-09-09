package contents.deserializers;

import static rules.PhotonRule.*;

import contents.Content;
import contents.IntermediateObject;
import rules.*;

//DeserializerFactory から作られるだけなので package private にしておく;
class PhotonDeserializer implements Deserializer {
	
	private PhotonRule rule;
	
	PhotonDeserializer() {
		rule = RuleFactory.getPhotonRule();
	}
	
	@Override
	public IntermediateObject deserialize(Content content) {
		byte buffer[] = content.getBytes();
		IntermediateObject result = new IntermediateObject();
		result.put(KEY_CONST, rule.getConst(buffer));
		result.put(KEY_FLAG, rule.getFlag(buffer));
		int offset = POS_TCP_MAGIC;
		rule.getPhotonCore(result, buffer, offset);
		return result;
	}

}
