package contents.serializers;

import static rules.PhotonRule.*;
import static rules.RuleFactory.*;
import static base.Constant.*;

import exceptions.*;
import contents.Content;
import contents.IntermediateObject;
import rules.PhotonRule;
import util.*;

// SerializerFactory から作られるだけなので package private にしておく;
class PhotonSerializer extends Serializer {

	// TODO: バッファが足りなくなると死んでしまう問題がある...;
	
	private PhotonRule rule;
	
	PhotonSerializer() {
		rule = (PhotonRule)(createRule(RULE_PHOTON, TYPE_UNKNOWN));
	}

	@Override
	public Content execute(IntermediateObject object) {
		if( object.containsKey(KEY_ADHOC) ) {
			try {
				String data = (String)(object.get(KEY_ADHOC));
				byte content[] = Parser.parseByteArray(data.length() / 2, data);
				return new Content(content);
			}
			catch( DataFormatException e ) {
				throw new ImplementationException("Photon adhoc rule error: " + object.getInt(KEY_ADHOC));
			}
		}
		ByteBuffer buffer = new ByteBuffer();
		rule.setConst(buffer, object.getInt(KEY_CONST));
		rule.setFlag(buffer, object.getInt(KEY_FLAG));
		int size = rule.setPhotonCore(object, buffer, POS_TCP_MAGIC);
		byte content[] = buffer.getBuffer(0, size);
		// これは formatter が勝手に呼んでくれるような気はするが...;
		// 2回呼んでも問題ないはずなので保留にしておく;
		rule.format(content);
		return new Content(content);
	}

}
