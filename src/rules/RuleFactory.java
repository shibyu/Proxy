package rules;

import base.Config;
import base.Constant;

public class RuleFactory {
	
	public static final String RULE_PHOTON = "Photon";
	public static final String RULE_PHOTON_UDP = "PhotonUdp";

	public static Rule getRule(String category, boolean isTCP) {
		return getRule(category, Constant.TYPE_UNKNOWN, isTCP);
	}

	public static Rule getRule(String category, int type, boolean isTCP) {
		Config config = Config.getConfig(isTCP);
		String rule = config.getRule(category, type);
		if( rule == null ) {
			// TODO: UDP なので Rule に落とせていない...;
			if( isTCP == false ) { return null; }
			return new CustomRule(category, config);
		}
		return createRule(rule);
	}
	
	public static Rule createRule(String rule) {
		if( rule.equalsIgnoreCase(RULE_PHOTON) ) {
			return new PhotonRule();
		}
		if( rule.equalsIgnoreCase(RULE_PHOTON_UDP) ) {
			return new PhotonUdpRule();
		}
		return ConfigurableRuleLoader.loadConfigurableRule(rule);
	}

}
