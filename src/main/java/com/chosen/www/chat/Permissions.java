package com.chosen.www.chat;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.plugin.Plugin;

public class Permissions {
	
	//command permissions
	public static String COMMAND_GENERAL = "chosencraft.command.general";
	public static String COMMAND_CREATE_CHANNEL = "chosenchat.command.create";
	public static String COMMAND_DELETE_CHANNEL = "chosenchat.command.delete";
	public static String COMMAND_EDIT_CHANNEL = "chosenchat.command.set";
	public static String COMMAND_MAKE_PERMANENT = "chosenchat.command.set.permanent";
	
	public static String CHAT_COLOR = "chosenchat.chat.color";
	public static String CHAT_FORMAT = "chosenchat.chat.format";
	public static String CHAT_FORMAT_BOLD = "chosenchat.chat.format.bold";
	public static String CHAT_FORMAT_STRIKETHROUGH = "chosenchat.chat.format.strikethrough";
	public static String CHAT_FORMAT_UNDERLINE = "chosenchat.chat.format.underline";
	public static String CHAT_FORMAT_ITALIC = "chosenchat.chat.format.italic";
	public static String CHAT_FORMAT_RESET = "chosenchat.chat.format.reset";
	public static String CHAT_FORMAT_MAGIC = "chosenchat.chat.format.magic";
	
	ConfigManager cfManager;
	public HashMap<String, String> groups = new HashMap<String, String>();
	
	public Permissions( Plugin mainPlugin) {
		
		cfManager = ((MainChat) mainPlugin).cfgm;
		
		if ( cfManager.get("groups.yml", "chosenchat.groups.default") == null ) {
			groups.put("default", "&7default");
			System.out.println("created default group because it did not exist");
		}
		
		//need to loop thru all groups/ranks in config and add them to a list
		Set<String> configGroups = cfManager.getConfig("groups.yml").getConfig().getConfigurationSection("chosenchat.groups").getKeys(false);
		for ( String key : configGroups ) {
			groups.put(key, cfManager.get("groups.yml", "chosenchat.groups." + key));
		}
	}
	
	
	
}
