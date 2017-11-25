package igrek.todotree.services.commander;


import java.util.Arrays;
import java.util.List;

import igrek.todotree.logger.Logs;
import igrek.todotree.services.preferences.Preferences;
import igrek.todotree.services.preferences.PropertyDefinition;
import igrek.todotree.services.resources.UserInfoService;

public class Commander {
	
	private Logs logger;
	private Preferences preferences;
	private UserInfoService userInfo;
	
	private static final String CMD_PREFIX = "###";
	
	public Commander(Logs logger, Preferences preferences, UserInfoService userInfo) {
		this.logger = logger;
		this.preferences = preferences;
		this.userInfo = userInfo;
	}
	
	public boolean execute(String content) {
		if (!content.startsWith(CMD_PREFIX))
			return false;
		
		String commandsAll = content.substring(CMD_PREFIX.length());
		if (commandsAll.isEmpty())
			return false;
		
		return executeCommand(Arrays.asList(commandsAll.split(" ")));
	}
	
	private boolean executeCommand(List<String> parts) {
		String main = parts.get(0);
		if (main.isEmpty())
			return false;
		List<String> params = parts.subList(1, parts.size());
		
		switch (main) {
			case "dupa":
				commandDupa(params);
				break;
			case "config":
				commandConfig(params);
				break;
			default:
				userInfo.showInfo("Unknown command: " + main);
				return false;
		}
		return true;
	}
	
	private void commandDupa(List<String> params) {
		userInfo.showInfo("Congratulations! You have discovered an Easter Egg.");
	}
	
	private void commandConfig(List<String> params) {
		if (params.size() < 2) {
			logger.warn("not enough params");
			return;
		}
		String property = params.get(0);
		String value = params.get(1);
		if (property.equals("lockdb")) {
			value = value.toLowerCase();
			boolean valueB = value.equals("true") || value.equals("on") || value.equals("1");
			preferences.setValue(PropertyDefinition.lockDB, valueB);
			userInfo.showInfo("Property saved: lockdb = " + valueB);
		} else {
			logger.warn("unknown property: " + property);
		}
		
	}
	
}
