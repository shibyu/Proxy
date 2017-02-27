package contents.serializers;

import contents.Content;
import contents.IntermediateObject;

abstract public class Serializer {

	public Content serialize(IntermediateObject object) {
		Content result = execute(object);
		result.setRUDPOnly(object.isRUDPOnly());
		return result;
	}
	
	abstract public Content execute(IntermediateObject object);
	
}
