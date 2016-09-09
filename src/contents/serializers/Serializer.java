package contents.serializers;

import contents.Content;
import contents.IntermediateObject;

public interface Serializer {

	public Content serialize(IntermediateObject object);
	
}
