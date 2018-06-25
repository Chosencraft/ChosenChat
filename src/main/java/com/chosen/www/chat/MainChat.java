package com.chosen.www.chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.chosen.www.chat.commands.Commands;
import com.chosen.www.chat.events.EventClass;

public class MainChat extends JavaPlugin {

	public ConfigManager cfgm;
	public Commands commands;
	public EventClass events;
	
	public void onEnable() {
		
		loadConfigManager();
		
		commands = new Commands(this);
		this.getCommand("channel").setExecutor(commands);
		
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
	
}
