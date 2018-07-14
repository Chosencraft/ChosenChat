package com.chosen.www.chat.events;

import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.chosen.www.chat.ChatChannel;
import com.chosen.www.chat.ConfigManager;
import com.chosen.www.chat.MainChat;
import com.chosen.www.chat.Permissions;
import com.chosen.www.chat.commands.Commands;

import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class EventClass implements Listener {

	Plugin plugin;
	ConfigManager cfManager;
	Commands commands;
	Permissions permissions;
	
	public HashMap<String, String> playerChannels = new HashMap<String, String>();
	public HashMap<String, String> ranks = new HashMap<String, String>();
	
	public EventClass( Plugin mainPlugin ) {
		plugin = mainPlugin;
		cfManager = ((MainChat) mainPlugin).cfgm;
		commands = ((MainChat)mainPlugin).commands;
		permissions = new Permissions(mainPlugin);
		
	}
	
	public void updateChannel(String player) {
		
		playerChannels.put(player, cfManager.get("players.yml", player));
	}
	
	@EventHandler
	public void onJoin( PlayerJoinEvent event) {
		
		Player player = event.getPlayer();
		playerJoined(player);
	}
	
	public void playerJoined( Player player) {
		
		String playerUUID = player.getUniqueId().toString().replace("-", "");
		
		if ( cfManager.get("players.yml", playerUUID) == null) {
			cfManager.set("players.yml", playerUUID, "General");
			commands.getChannel("General").join(player);
			
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "new player " + player.getName() + " joined");
		}

		for ( String r : permissions.groups.keySet() ) {
			if ( player.hasPermission(r) ) {
				ranks.put(playerUUID, r);
				break;
			}
		}
		
		playerChannels.put(playerUUID, cfManager.get("players.yml", playerUUID));
	}
	
	@EventHandler
	public void onQuit( PlayerQuitEvent event ) {

		Player player = event.getPlayer();
		String playerUUID = player.getUniqueId().toString().replace("-", "");
		
		commands.swapChannel(player, "General");
		playerChannels.remove(playerUUID);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		
		Player player = event.getPlayer();
		String playerUUID = player.getUniqueId().toString().replace("-", "");
		String sentMessage = event.getMessage();
		ChatChannel channel = commands.getChannel(playerChannels.get(playerUUID));
		sendMessage(player, channel, sentMessage);
		event.setCancelled(true);
		
	}
	
	public void sendMessage( Player player, ChatChannel channel, String sentMessage ) {
		
		String playerUUID = player.getUniqueId().toString().replace("-", "");
		char channelChar = channel.getName().charAt(0);
		String channelColor = channel.getColor();
		
		Collection<? extends Player> recipients = Bukkit.getOnlinePlayers();
		
		/*
		 * NEED TO ADD SUPPORT FOR PRIVATE AND LOCAL CHANNELS HERE
		 * BASED ON CHANNEL PRIVACY AND LOCALNESS
		 */
		//remove recipients if channel is private
		if ( channel.isPrivate() ) {
			for ( Player p : recipients ) {
				if ( !channel.getPlayers().contains(p) ) {
					recipients.remove(p);
				}
			}
		}
		//remove recipients outside of range if channel is local
		if ( channel.isLocal() ) {
			Location mouth = player.getLocation();
			
			for ( Player p : recipients ) {
				Location ear = p.getLocation();
				if ( mouth.distance(ear) > 100 ) {
					recipients.remove(p);
				}
			}
		}
		
		String rank = ranks.get(playerUUID);
		String sentText = permissionCheck(player, sentMessage);
		
		//channel character
		String message = channelColor + "[" + channelChar + "] " 
				+ "&7[" + rank + "&7] "
				//player name
				+ "&f" + player.getDisplayName() + "&7: " 
				//message
				+ channelColor + sentText;
		
		for ( Player p : recipients ) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
		
	}

	private String permissionCheck(Player player, String message) {
		
		String output = message;
		String colorCodes = "&[0-9a-f]";
		String formatCodes = "&[k-or]";
		
		if ( !player.hasPermission("chosenchat.chat.color") ) {
			output = output.replaceAll(colorCodes, "");
		}
		
		if ( !player.hasPermission("chosenchat.chat.format") ) {
			output = output.replaceAll(formatCodes, "");
		} else {
			if ( !player.hasPermission("chosenchat.chat.format.bold") ) {
				output = output.replace("&l", "");
			}
		
			if ( !player.hasPermission("chosenchat.chat.format.strikethrough") ) {
				output = output.replaceAll("&m", "");
			}
			
			if ( !player.hasPermission("chosenchat.chat.format.underline") ) {
				output = output.replace("&n", "");
			}
		
			if ( !player.hasPermission("chosenchat.chat.format.italic") ) {
				output = output.replace("&o", "");
			}
			
			if ( !player.hasPermission("chosenchat.chat.format.reset") ) {
				output = output.replace("&r", "");
			}
		
			if ( !player.hasPermission("chosenchat.chat.format.magic") ) {
				output = output.replace("&k", "");
			}
		}
		
		return output;
	}
	
}
