package base;

import java.util.*;

import static base.Constant.*;

import java.io.*;

import exceptions.*;
import util.Parser;
import contents.DataConverter;

public class Config {
	
	public static Config tcpConfig = new Config("../resources/config/tcp");
	public static Config udpConfig = new Config("../resources/config/udp");

	public static final String CATEGORY_BASE = "base";
	public static final String CATEGORY_ECHO = "echo";
	public static final String CATEGORY_HTTP = "http";
	public static final String CATEGORY_HTTP_ECHO = "httpecho";
	public static final String CATEGORY_PORTMAP = "portmap";
	public static final String CATEGORY_ADMIN = "admin";
	public static final String CATEGORY_PROXY = "proxy";
	private static final String CATEGORY_CUSTOM = "custom";
	private static final String CATEGORY_MASTER = "master";
	public static final String CATEGOLY_LOG = "log";
	public static final String CATEGORY_CLIENT = "client";
	
	private static final String KEY_PORT = "ListenPort";
	private static final String KEY_HOST = "Host";
	private static final String KEY_HOST_PORT = "HostPort";
	private static final String KEY_BUFFER_SIZE = "BufferSize";
	private static final String KEY_ENABLE = "Enable";
	private static final String KEY_TIMEOUT = "Timeout";
	private static final String KEY_REQUEST_INPUT_TYPE = "RequestInputType";
	private static final String KEY_REQUEST_OUTPUT_TYPE = "RequestOutputType";
	private static final String KEY_RESPONSE_INPUT_TYPE = "ResponseInputType";
	private static final String KEY_RESPONSE_OUTPUT_TYPE = "ResponseOutputType";
	private static final String KEY_ENABLE_REQUEST_SLAVE = "EnableRequestSlave";
	private static final String KEY_ENABLE_RESPONSE_SLAVE = "EnableResponseSlave";
	private static final String KEY_REQUEST_FORMAT = "RequestFormat";
	private static final String KEY_RESPONSE_FORMAT = "ResponseFormat";
	private static final String KEY_ORIGINAL_FORMAT = "OriginalFormat";
	private static final String KEY_PROXY_FORMAT = "ProxyFormat";
	public static final String KEY_MASTER = "Master";
	private static final String KEY_REQUEST_SLAVE_PORT = "RequestSlavePort";
	private static final String KEY_RESPONSE_SLAVE_PORT = "ResponseSlavePort";
	private static final String KEY_DENY = "Deny";
	private static final String KEY_FORMAT_FROM = "FormatFrom";
	private static final String KEY_FORMAT_TO = "FormatTo";
	private static final String KEY_RULE = "Rule";
	public static final String KEY_CLEANUP = "Cleanup";
	private static final String KEY_KEEP_ALIVE= "KeepAlive";
	private static final String KEY_REQUEST_DEBUG = "RequestDebug";
	private static final String KEY_RESPONSE_DEBUG = "ResponseDebug";
	
	public Config(String path) {
		rawConfig = new HashMap<String, Map<String, Object>>();
		load(path);
	}
	
	public static Config getConfig(boolean isTCP) {
		return isTCP ? tcpConfig : udpConfig;
	}
	
	private Map<String, Map<String, Object>> rawConfig;

	protected void load(String path) {
		File directory = new File(path);
		for( File file : directory.listFiles() ) {
			String name = file.getName();
			if( name.endsWith(".txt") ) {
				String category = name.substring(0, name.length() - ".txt".length());
				loadConfig(category, file);
			}
		}
	}
	
	private void loadConfig(String category, File file) {
		Map<String, Object> core = new HashMap<String, Object>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while( (line = reader.readLine()) != null ) {
				// Parser クラスの static method を呼び出す感じ;
				// switch している部分はさすがにどうしようもなさそう;
				if( line.startsWith("#") ) { continue; }
				// key:type:value;
				// value の中に : を入れられるようにしたいので、3個までしか切らない;
				String parts[] = line.split(":", 3);
				if( parts.length != 3 ) { continue; }
				if( parts[1].equalsIgnoreCase("int") ) {
					core.put(parts[0], Parser.parseInt(parts[2]));
				}
				else if( parts[1].equalsIgnoreCase("bool") || parts[1].equalsIgnoreCase("boolean") ) {
					try {
						core.put(parts[0], Parser.parseBool(parts[2]));
					}
					catch( DataFormatException e ) {
						throw new ConfigurationException("configuration error: " + e.getMessage());
					}
				}
				else if( parts[1].equalsIgnoreCase("map") ) {
					core.put(parts[0], Parser.parseMap(parts[2]));
				}
				else if( parts[1].equalsIgnoreCase("string") ) {
					core.put(parts[0], parts[2]);
				}
				else if( parts[1].equalsIgnoreCase("csv") ) {
					core.put(parts[0], Parser.parseCSV(parts[2]));
				}
				else {
					throw new ImplementationException("unknown configuration data type: " + parts[1]);
				}
			}
		}
		catch( FileNotFoundException e ) {
			// listFiles で拾ってきているのでこの例外が起こることはないはず;
			throw new UnknownException("File not found: " + file.getAbsolutePath());
		}
		catch( IOException e ) {
			throw new UnknownException("Failed to read: " + file.getAbsolutePath());
		}
		finally {
			try {
				if( reader != null ) { reader.close(); }
			}
			catch( IOException e ) {
				throw new UnknownException("Failed to close Reader: " + file.getAbsolutePath());
			}
		}
		rawConfig.put(category, core);
	}
	
	public boolean existsProperty(String category, String key) {
		return rawConfig.get(category).containsKey(key);
	}
	
	private String getMasterCategory(String subCategory) {
		String category = CATEGORY_MASTER + "_" + subCategory;
		if( rawConfig.containsKey(category) ) { return category; }
		return null;
	}
	
	public boolean isEnableRequestSlaveCategory(String category) {
		// proxy がいないのに slave にタスクを投げても仕方ないので...;
		if( tcpConfig.isEnable(CATEGORY_PROXY) == false ) { return false; }
		return getBoolProperty(category, KEY_ENABLE_REQUEST_SLAVE, true, false);
	}
	
	public boolean isEnableResponseSlaveCategory(String category) {
		// proxy がいないのに slave にタスクを投げても仕方ないので...;
		if( tcpConfig.isEnable(CATEGORY_PROXY) == false ) { return false; }
		return getBoolProperty(category, KEY_ENABLE_RESPONSE_SLAVE, true, false);
	}

	public List<String> getMasterCategories() {
		Map<String, Object> core = rawConfig.get(CATEGORY_MASTER);
		List<String> categories = new ArrayList<String>();
		for( String key : core.keySet() ) {
			if( getBoolProperty(CATEGORY_MASTER, key) ) {
				String category = getMasterCategory(key);
				if( category != null ) { categories.add(category); }
			}
		}
		return categories;
	}
	
	public int getSlavePort(String category, int type) {
		int port = 0;
		switch(type) {
		case TYPE_REQUEST:
			port = getRequestSlavePort(category);
			break;
		case TYPE_RESPONSE:
			port = getResponseSlavePort(category);
			break;
		default:
			throw new ConfigurationException("unknown slave type: " + type);
		}
		return port;
	}
	
	public int getRequestSlavePort(String category) {
		return getIntProperty(category, KEY_REQUEST_SLAVE_PORT);
	}
	
	public int getResponseSlavePort(String category) {
		return getIntProperty(category, KEY_RESPONSE_SLAVE_PORT);
	}
	
	public String getCustomCategory(String subCategory) {
		String category = CATEGORY_CUSTOM + "_" + subCategory;
		if( rawConfig.containsKey(category) ) { return category; }
		return null;
	}
	
	public List<String> getCustomCategories() {
		Map<String, Object> core = rawConfig.get(CATEGORY_CUSTOM);
		List<String> categories = new ArrayList<String>();
		for( String key : core.keySet() ) {
			// core を持っているのにもう一度親からたどるのでちょっと無駄ではあるが...;
			if( getBoolProperty(CATEGORY_CUSTOM, key) ) {
				String category = getCustomCategory(key);
				if( category != null ) { categories.add(category); }
			}
		}
		return categories;
	}
	
	public boolean getBoolProperty(String key) {
		return getBoolProperty(CATEGORY_BASE, key);		
	}
	
	public boolean getBoolProperty(String category, String key) {
		return getBoolProperty(category, key, false, false);
	}
	
	public boolean getBoolProperty(String category, String key, boolean hasDefault, boolean defaultValue) {
		if( rawConfig.containsKey(category) == false ) {
			throw new ImplementationException("Category not found: " + category);
		}
		Map<String, Object> core = rawConfig.get(category);
		try {
			if( core.containsKey(key) == false ) {
				if( hasDefault ) { return defaultValue; }
				// この後で例外が発生する;
			}
			return (boolean)(core.get(key));
		}
		catch( ClassCastException e ) {
			throw new ImplementationException("Type mismatch: " + category + ":" + key + " is not boolean");
		}
	}
	
	public int getIntProperty(String key) {
		return getIntProperty(CATEGORY_BASE, key);
	}
	
	public int getIntProperty(String category, String key) {
		return getIntProperty(category, key, false, 0);
	}
	
	public int getIntProperty(String category, String key, boolean hasDefault, int defaultValue) {
		if( rawConfig.containsKey(category) == false ) {
			throw new ImplementationException("Category not found: " + category);
		}
		Map<String, Object> core = rawConfig.get(category);
		try {
			if( core.containsKey(key) == false ) {
				if( hasDefault ) { return defaultValue; }
				// この後で例外が発生する;
			}
			return (int)(core.get(key));
		}
		catch( ClassCastException e ) {
			throw new ImplementationException("Type mismatch: " + category + ":" + key + " is not integer");
		}
	}
	
	public String getStringProperty(String category, String key) {
		return getStringProperty(category, key, false, null);
	}
	
	public String getStringProperty(String category, String key, boolean hasDefault, String defaultValue) {
		if( rawConfig.containsKey(category) == false ) {
			throw new ImplementationException("Category not found: " + category);
		}
		Map<String, Object> core = rawConfig.get(category);
		try {
			if( core.containsKey(key) == false ) {
				if( hasDefault ) { return defaultValue; }
				// この後で null を String に cast する？;
			}
			return (String)(core.get(key));
		}
		catch( ClassCastException e ) {
			// String に cast できないことなんてあるのか？;
			throw new ImplementationException("Type mismatch: " + category + ":" + key + " is not string");
		}
	}
	
	public String getHost() {
		return getHost(CATEGORY_BASE);
	}
	
	public String getHost(String category) {
		return getStringProperty(category, KEY_HOST);
	}
	
	public int getHostPort(String category) {
		return getIntProperty(category, KEY_HOST_PORT, true, -1);
	}
	
	public int getPort(String category) {
		return getIntProperty(category, KEY_PORT);
	}
	
	public String getRequestInputType(String category) {
		return getStringProperty(category, KEY_REQUEST_INPUT_TYPE);
	}
	
	public String getRequestOutputType(String category) {
		return getStringProperty(category, KEY_REQUEST_OUTPUT_TYPE);
	}
	
	public String getResponseInputType(String category) {
		return getStringProperty(category, KEY_RESPONSE_INPUT_TYPE);
	}
	
	public String getResponseOutputType(String category) {
		return getStringProperty(category, KEY_RESPONSE_OUTPUT_TYPE);
	}
	
	public String getRule(String category, int type) {
		String rule = getStringProperty(category, KEY_RULE, true, null);
		if( type != TYPE_UNKNOWN ) {
			rule += getTypeString(type);
		}
		return rule;
	}
	
	private String getRequestFormat(String category) {
		String result = getStringProperty(category, KEY_REQUEST_FORMAT, true, null);
		if( result == null ) {
			result = getStringProperty(category, KEY_ORIGINAL_FORMAT, true, null);
		}
		return result;
	}
	
	private String getResponseFormat(String category) {
		String result = getStringProperty(category, KEY_RESPONSE_FORMAT, true, null);
		if( result == null ) {
			result = getStringProperty(category, KEY_ORIGINAL_FORMAT, true, null);
		}
		return result;
	}
	
	private String getProxyFormat(String category) {
		return getStringProperty(category, KEY_PROXY_FORMAT, true, null);
	}
	
	public int getTimeout() {
		return getTimeout(CATEGORY_BASE);
	}
	
	public int getTimeout(String category) {
		// タイムアウトの場合はデフォルト値が存在する;
		// 0  はタイムアウトを設定しないことを意味する;
		// セットする必要性を感じなくなってきた...;
		return getIntProperty(category, KEY_TIMEOUT, true, 0);
	}
	
	public int getBufferSize(String category) {
		// buffer の大きさは適当で良いはずなので、初期値を設定しておく;
		return getIntProperty(category, KEY_BUFFER_SIZE, true, 1024);
	}
	
	public boolean isEnable(String category) {
		return getBoolProperty(category, KEY_ENABLE, true, false);
	}
	
	public boolean isKeepAlive(String category) {
		return getBoolProperty(category, KEY_KEEP_ALIVE, true, false);
	}
	
	// 一回 Object に入れてしまっているので、元の型情報が拾えない模様...;
	// これは仕方ないのか？でもひどい...;
	public Map<?, ?> getMapProperty(String category, String key) {
		try {
			return (Map<?, ?>)(rawConfig.get(category).get(key));
		}
		catch( ClassCastException e ) {
			throw new ImplementationException("Type mismatch: " + category + ":" + key + " is not map");
		}
	}
	
	public List<?> getListProperty(String category, String key) {
		try {
			return (List<?>)(rawConfig.get(category).get(key));
		}
		catch( ClassCastException e ) {
			throw new ImplementationException("Type mismatch: " + category + ":" + key + " is not list");
		}
	}
	
	public List<?> getDenyHosts() {
		return getListProperty(CATEGORY_BASE, KEY_DENY);
	}
	
	public DataConverter getRequestDataConverter(String category) {
		String input = getRequestFormat(category);
		String output = getProxyFormat(category);
		if( input == null || output == null ) { return null; }
		return new DataConverter(input, output);
	}
	
	public DataConverter getRequestReverseConverter(String category) {
		String input = getProxyFormat(category);
		String output = getRequestFormat(category);
		if( input == null || output == null ) { return null; }
		return new DataConverter(input, output);
	}
	
	public DataConverter getResponseDataConverter(String category) {
		String input = getResponseFormat(category);
		String output = getProxyFormat(category);
		if( input == null || output == null ) { return null; }
		return new DataConverter(input, output);
	}
	
	public DataConverter getResponseReverseConverter(String category) {
		String input = getProxyFormat(category);
		String output = getResponseFormat(category);
		if( input == null || output == null ) { return null; }
		return new DataConverter(input, output);
	}
	
	public DataConverter getDataConverter(String category) {
		if( existsProperty(category, KEY_FORMAT_FROM) && existsProperty(category, KEY_FORMAT_TO) ) {
			return new DataConverter(getStringProperty(category, KEY_FORMAT_FROM), getStringProperty(category, KEY_FORMAT_TO));
		}
		return null;
	}
	
	public boolean isRequestDebug(String category, int value) {
		return isContain(category, KEY_REQUEST_DEBUG, value);
	}

	public boolean isResponseDebug(String category, int value) {
		return isContain(category, KEY_RESPONSE_DEBUG, value);
	}

	private boolean isContain(String category, String key, int value) {
		List<?> list = getListProperty(category, key);
		// そもそもそんな Config がない場合...;
		if( list == null ) { return false; }
		for( Object data : list ) {
			if( Parser.parseInt(data.toString()) == value ) { return true; }
		}
		return false;
	}
	
}
