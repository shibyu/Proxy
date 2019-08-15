package rules;

import static base.Constant.*;

import base.Config;

public class RuleFactory {
	
	public static final String RULE_PHOTON = "Photon";
	public static final String RULE_PHOTON_UDP = "PhotonUdp";

	public static Rule getRule(String category, boolean isTCP) {
		return getRule(category, TYPE_UNKNOWN, isTCP);
	}

	public static Rule getRule(String category, int type, boolean isTCP) {
		Config config = Config.getConfig(isTCP);
		String rule = config.getRule(category);
		if( rule == null ) {
			// TODO: UDP なので Rule に落とせていない...;
			if( isTCP == false ) { return null; }
			return new CustomRule(category, config);
		}
		return createRule(rule, type);
	}
	
	public static Rule createRule(String rule, int type) {
		if( rule.equalsIgnoreCase(RULE_PHOTON) ) {
			return new PhotonRule(type);
		}
		if( rule.equalsIgnoreCase(RULE_PHOTON_UDP) ) {
			return new PhotonUdpRule();
		}
		if( type != TYPE_UNKNOWN ) {
			rule += getTypeString(type);
		}
		return ConfigurableRuleLoader.loadConfigurableRule(rule);
	}

}
