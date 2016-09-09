package rules;

import base.Config;
import exceptions.ConfigurationException;

public class RuleFactory {
	
	public static final String RULE_PHOTON = "Photon";
	public static final String RULE_PHOTON_UDP = "PhotonUdp";

	// createRule を呼び出してから cast するのが面倒なので、直接作ってしまうことにした...;
	public static PhotonRule getPhotonRule() {
		return new PhotonRule();
	}
	
	public static PhotonUdpRule getPhotonUdpRule() {
		return new PhotonUdpRule();
	}
	
	public static CustomRule getRule(String category, boolean isTCP) {
		Config config = Config.getConfig(isTCP);
		String rule = config.getRule(category);
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
		throw new ConfigurationException("unknown rule: " + rule);
	}

}
