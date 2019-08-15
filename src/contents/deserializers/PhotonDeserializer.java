package contents.deserializers;

import static rules.PhotonRule.*;
import static rules.RuleFactory.*;
import static base.Constant.*;

import contents.Content;
import contents.IntermediateObject;
import rules.*;
import util.Util;

//DeserializerFactory から作られるだけなので package private にしておく;
class PhotonDeserializer implements Deserializer {
	
	private PhotonRule rule;
	
	PhotonDeserializer() {
		rule = (PhotonRule)(createRule(RULE_PHOTON, TYPE_UNKNOWN));
	}
	
	@Override
	public IntermediateObject deserialize(Content content) {
		byte buffer[] = content.getBytes();
		IntermediateObject result = new IntermediateObject();
		// 0xF0 開始のものの暫定対応;
		// TODO: いつかちゃんと実装する...;
		if( buffer[0] == (byte)(0xF0) ) {
			result.put(KEY_ADHOC, Util.toHexString(buffer));
			return result;
		}
		result.put(KEY_CONST, rule.getConst(buffer));
		result.put(KEY_FLAG, rule.getFlag(buffer));
		int offset = POS_TCP_MAGIC;
		rule.getPhotonCore(result, buffer, offset);
		return result;
	}

}
