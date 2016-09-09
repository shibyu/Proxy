package contents.deserializers;

import contents.Content;
import contents.IntermediateObject;

public interface Deserializer {
	
	public IntermediateObject deserialize(Content content);

}
