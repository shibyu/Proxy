package contents;

import java.util.*;

import exceptions.ImplementationException;

// Photon をターゲットにしている関係で、実質的に Map の wrapper にしかなっていない...;
// TODO: より汎用なデータ構造を考える...;
public class IntermediateObject extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;
	private boolean RUDPOnly;
	
	public IntermediateObject() {
		super();
		RUDPOnly = true;
	}

	public int getInt(String key) {
		Object object = get(key);
		if( (object instanceof Integer) == false ) {
			throw new ImplementationException("invalid data format: not integer: " + key + ":" + object);
		}
		return (Integer)(object);		
	}
	
	public boolean isRUDPOnly() {
		return RUDPOnly;
	}
	
	public void setRUDPOnly(boolean RUDPOnly) {
		this.RUDPOnly = RUDPOnly;
	}
	
}
