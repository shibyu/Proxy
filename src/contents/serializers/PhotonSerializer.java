package contents.serializers;

import static rules.PhotonRule.*;

import contents.Content;
import contents.IntermediateObject;
import rules.PhotonRule;
import rules.RuleFactory;
import util.Util;

// SerializerFactory から作られるだけなので package private にしておく;
class PhotonSerializer extends Serializer {

	// TODO: バッファが足りなくなると死んでしまう問題がある...;
	
	private PhotonRule rule;
	private int bufferSize;
	
	PhotonSerializer() {
		rule = RuleFactory.getPhotonRule();
		bufferSize = 65536;
	}

	@Override
	public Content execute(IntermediateObject object) {
		byte buffer[] = new byte[bufferSize];
		rule.setConst(buffer, object.getInt(KEY_CONST));
		rule.setFlag(buffer, object.getInt(KEY_FLAG));
		int size = rule.setPhotonCore(object, buffer, POS_TCP_MAGIC);
		byte content[] = Util.copyByteArray(buffer, 0, size);
		// これは formatter が勝手に呼んでくれるような気はするが...;
		// 2回呼んでも問題ないはずなので保留にしておく;
		rule.format(content);
		return new Content(content);
	}

}
