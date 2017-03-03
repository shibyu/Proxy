package rules;

import base.Config;
import base.Constant;
import exceptions.ConfigurationException;

public class RuleFactory {
	
	public static final String RULE_PHOTON = "Photon";
	public static final String RULE_PHOTON_UDP = "PhotonUdp";

	public static final String RULE_EXPO = "Expo";
	public static final String RULE_EXPO_REQUEST = "ExpoRequest";
	public static final String RULE_EXPO_RESPONSE = "ExpoResponse";

	// createRule を呼び出してから cast するのが面倒なので、直接作ってしまうことにした...;
	public static PhotonRule getPhotonRule() {
		return new PhotonRule();
	}
	
	public static PhotonUdpRule getPhotonUdpRule() {
		return new PhotonUdpRule();
	}
	
	public static CustomRule getRule(String category, boolean isTCP) {
		return getRule(category, Constant.TYPE_UNKNOWN, isTCP);
	}

	public static CustomRule getRule(String category, int type, boolean isTCP) {
		Config config = Config.getConfig(isTCP);
		String rule = config.getRule(category, type);
		if( rule == null ) {
			// TODO: UDP なので Rule に落とせていない...;
			if( isTCP == false ) { return null; }
			return new CustomRule(category, config);
		}
		return createRule(rule);
	}
	
	public static CustomRule createRule(String rule) {
		if( rule.equalsIgnoreCase(RULE_PHOTON) ) {
			return new PhotonRule();
		}
		if( rule.equalsIgnoreCase(RULE_PHOTON_UDP) ) {
			return new PhotonUdpRule();
		}
		if( rule.equalsIgnoreCase(RULE_EXPO_REQUEST) ) {
			return new ExpoRule(true);
		}
		if( rule.equalsIgnoreCase(RULE_EXPO_RESPONSE) ) {
			return new ExpoRule(false);
		}
		throw new ConfigurationException("unknown rule: " + rule);
	}

}
