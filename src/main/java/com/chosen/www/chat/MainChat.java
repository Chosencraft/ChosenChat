package com.chosen.www.chat;

import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.chosen.www.chat.commands.Commands;
import com.chosen.www.chat.events.EventClass;

public class MainChat extends JavaPlugin {

	// Vault hooks
	// chat helps with formatting, and is used to grab the PREFIX of a player (AKA they're group)
	public static Chat chat;
	// permissions can be used for a lot of things, you may or may not use it.
	public static Permission permissions;
	// Essentials dependency is hand loaded from ../ChosenChat/dependencies
	// integration with gradle wasn't happening
	// Use this to check if player is muted
	public static Essentials essentials;
	
	public ConfigManager cfgm;
	public Commands commands;
	public EventClass events;
	
	public void onEnable() {

		// Hook into dependencies
		setupChat();
		setupPermissions();
		setupEssentials();
		
		loadConfigManager();

		commands = new Commands(this);
		this.getCommand("channel").setExecutor(commands);
		
//		Iterable<String> permanentChannels = cfgm.getConfig("channels.yml").getConfig().getKeys(false);
//		for ( String channel : permanentChannels ) {
//			
//			String shortCut = cfgm.get("channels.yml", channel + ".shortCut");
//			System.out.println("trying to register " + shortCut + " as a channel shortcut command");
//			this.getCommand(shortCut).setExecutor(commands);
//			this.getCommand("" + shortCut.charAt(0)).setExecutor(commands);
//		}
		
		this.getCommand("g").setExecutor(commands);
		this.getCommand("t").setExecutor(commands);
		this.getCommand("h").setExecutor(commands);
		this.getCommand("l").setExecutor(commands);
		
		events = new EventClass(this);
		commands.updateEvents(this);
		getServer().getPluginManager().registerEvents(events, this);
		//update configs for all players in the event of a hot reload
		for ( Player p : Bukkit.getOnlinePlayers() ) {
			events.playerJoined(p);
		}
		
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "ChosenChat enabled");
	}
	
	public void onDisable() {
		commands.shutdown();
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "ChosenChat disabled");
	}
	
	public void loadConfigManager() {
		
		cfgm = new ConfigManager();
		cfgm.setup();
		
	}

	/**
	 * Registers Essentials for the plugin
	 * @return True is registers, false if not
	 */
	private boolean setupEssentials()
	{
		essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
		return true;
	}


	/**
	 * Registers vault chat API for the plugin
	 * @return True if registers, false if not
	 */
	private boolean setupChat()
	{
		RegisteredServiceProvider<Chat> serviceProvider = getServer().getServicesManager().getRegistration(Chat.class);
		chat = serviceProvider.getProvider();
		return chat != null;
	}


	/**
	 * Registers vault permissions API for the plugin
	 * @return True if registers, false if not
	 */
	private boolean setupPermissions()
	{
		RegisteredServiceProvider<Permission> serviceProvider = getServer().getServicesManager().getRegistration(Permission.class);
		permissions = serviceProvider.getProvider();
		return permissions != null;
	}

}
