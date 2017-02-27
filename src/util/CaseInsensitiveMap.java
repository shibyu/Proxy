package util;

import static base.LogManager.*;

import java.util.*;

import exceptions.ImplementationException;

public class CaseInsensitiveMap<VALUE> {
	
	private Map<String, VALUE> map;
	// 一応元の key を持っておくが、多分使わない気はする;
	private Map<String, String> originalKeys;
	
	public CaseInsensitiveMap() {
		map = new HashMap<String, VALUE>();
		originalKeys = new HashMap<String, String>();
	}
	
	// 強制上書き;
	public void put(String key, VALUE value) {
		map.put( key.toLowerCase(), value );
		output(key.toLowerCase() + " ==set==> " + value, -1);
		keepOriginalKey(key);
	}
	
	public void add(String key, VALUE value) {
		// なければそのまま入れてしまう;
		if( get(key) == null ) {
			put(key, value);
			return;
		}
		//　同じ key が　2回以上きた場合は...？;
		throw new ImplementationException("key conflict: " + key + " => " + value);
	}
	
	public VALUE get(String key) {
		output(key.toLowerCase() + " ==get==> " + map.get(key.toLowerCase()), -1);
		return map.get( key.toLowerCase() );
	}
	
	public String getOriginalKey(String key) {
		return originalKeys.get( key.toLowerCase() );
	}
	
	public Set<String> getOriginalKeySet() {
		return originalKeys.keySet();
	}
	
	public Collection<String> getOriginalKeyValues() {
		return originalKeys.values();
	}
	
	private void keepOriginalKey(String key) {
		if( originalKeys.containsKey( key.toLowerCase() ) ) { return; }
		originalKeys.put( key.toLowerCase(), key );
	}
	
}
