package com.chosen.www.chat.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.chosen.www.chat.ChatChannel;
import com.chosen.www.chat.ConfigManager;
import com.chosen.www.chat.MainChat;
import com.chosen.www.chat.Permissions;



public class Commands implements Listener,CommandExecutor {

	
	//permission error String
	private String cannotInto = "You don't have permission to use this command!";
	//command list
	public String cmd0 = "channel";
	
	public HashMap<String, ChatChannel> channels = new HashMap<String, ChatChannel>();
	
	Plugin plugin;
	ConfigManager cfManager;
	
	
	public Commands( Plugin mainPlugin ) {
		plugin = mainPlugin;
		cfManager = ((MainChat) mainPlugin).cfgm;
		
		Iterable<String> permanentChannels = cfManager.getConfig("channels.yml").getConfig().getKeys(false);
		//Initial setup of General Global chat if it doesn't exist
		//General chat's settings can be changed but it must exist or bad things happen
		if ( cfManager.get("channels.yml", "General") == null ) {
			ChatChannel general = new ChatChannel("General", true, false, false, "&2" );
			channels.put("General", general);
			System.out.println("created general chat because it did not exist");
		}
		
		//NEED TO INITIALIZE PERMANENT CHANNELS HERE
		for ( String channel : permanentChannels ) {
			boolean permanent = true;
			boolean local = cfManager.get("channels.yml", channel + ".local");
			boolean locked = cfManager.get("channels.yml", channel + ".private");
			String color = cfManager.get("channels.yml", channel + ".color");
			
			ChatChannel newChannel = new ChatChannel(channel, permanent, local, locked, color );
			channels.put(channel, newChannel);
		}
	}
	
	public ChatChannel getChannel( String channelName ) {
		return channels.get(channelName);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if ( !(sender instanceof Player) ) {
			sender.sendMessage(ChatColor.RED + "Only players can use this command");
		} else {
			
			Player player = (Player) sender;
			String playerUUID = player.getUniqueId().toString().replace("-", "");
			String activeChannel = cfManager.get("players.yml", playerUUID + ".activeChannel");
			
			if ( !player.hasPermission(Permissions.COMMAND_GENERAL) ) {
				player.sendMessage(ChatColor.RED + cannotInto);
			}
		
			//actual commands
			if ( command.getName().equalsIgnoreCase(cmd0) ) {
				channelCommand(player, activeChannel, args);
			//channel shortcuts
			} else {
//				Iterable<String> permanentChannels = cfManager.getConfig("channels.yml").getConfig().getKeys(false);
//	WIP			
//				for ( String channel : permanentChannels ) {
//					String shortcut = cfManager.get("channels.yml", channel + ".shortCut");
//					if ( command.getName().equalsIgnoreCase(shortcut) || 
//							command.getName().equalsIgnoreCase("" + shortcut.charAt(0)) ) {
//						if ( channels.get(channel).isPrivate() ) {
//							//deal with players not being able to shortcut to a channel
//							//such as admin chat
//						}
//						swapChannel(player, channel);
//						break;
//					}
//				}
			}
		}
		return true;
	}
	
	String[] messages = {
			ChatColor.GREEN + "-----{Channel Commands}-----",
			ChatColor.RED + "Usage: /channel <option>",
			ChatColor.YELLOW + "help: shows this message",
			ChatColor.YELLOW + "join: joins the specified channel",
			ChatColor.YELLOW + "list: shows all available channels",
			ChatColor.YELLOW + "create: creates a new channel",
			ChatColor.YELLOW + "delete: deletes a channel",
			ChatColor.YELLOW + "set: edits the settings of a channel"
			
	};
	
	private boolean channelCommand( Player player, String activeChannel ,String[] args) {
		if ( args.length < 1 ) {
			//add a help message
			String[] stats = getChannelStats(activeChannel);
			
			for ( String s : stats) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
			}
			
			return true;
		} else {
		
			switch (args[0].toLowerCase()) {
		
			case "help":
				player.sendMessage(messages);
				break;
				
			case "list":
				player.sendMessage(ChatColor.GREEN + "-----{Channel List}-----");
				for ( ChatChannel c : channels.values() ) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', c.getColor() + c.getName()));
				}
				player.sendMessage(ChatColor.GREEN + "------------------------");
				break;
			
			case "create":
				if ( !player.hasPermission(Permissions.COMMAND_CREATE_CHANNEL) ) {
					player.sendMessage(ChatColor.RED + cannotInto);
					return true;
				}
			
				if ( args.length < 2 ) {
					//add a help message
					player.sendMessage(ChatColor.RED + "Usage: /channel create <channel name>");
					break;
				}
		
				createChannel(args[1]);
				swapChannel(player, args[1]);
				break;
			
			case "delete":
				if ( !player.hasPermission(Permissions.COMMAND_DELETE_CHANNEL) ) {
					player.sendMessage(ChatColor.RED + cannotInto);
					return true;
				}
			
				if ( channels.get(activeChannel) == null ) {
					player.sendMessage(ChatColor.RED + "You aren't in a Channel! You can only delete the channel you are in");
				} else {
					for ( Player p : channels.get(activeChannel).getPlayers() ) {
					swapChannel(p, "General");
					}
					channels.remove(activeChannel);
				}
				
				break;
			
			case "join":
				if ( args.length < 2 ) {
					//add a help message
					player.sendMessage(ChatColor.RED + "Usage: /channel join <channel name>");
					break;
				} else {
					if ( channels.get(args[1]) == null ) {
						player.sendMessage(ChatColor.RED + "That Channel doesn't exist! Remember Channel names are case-sensitive!");
						break;
					} else {
						swapChannel(player, args[1]);
						break;
					}
				}
			
			case "set":
				if ( !player.hasPermission(Permissions.COMMAND_EDIT_CHANNEL) ) {
					player.sendMessage(ChatColor.RED + cannotInto);
					return true;
				}	
			
				if ( args.length < 2 ) {
					//add a help message
					player.sendMessage(ChatColor.RED + "Usage: /channel set <channel name> <setting> <value>");
					break;
				} else if ( args.length < 3 ) {
					player.sendMessage(setChannel( player, activeChannel, args[1], null));
				} else {
					player.sendMessage(setChannel( player, activeChannel, args[1], args[2]));
				}
				
				break; 
			default:
				
				break;
			}
		}
		return true;
	}

	private String[] getChannelStats(String activeChannel) {
		ChatChannel channel = channels.get(activeChannel);
		String color = channel.getColor();
		String[] stats = {
				color + "Current Channel: " + ChatColor.WHITE + channel.getName(),
				color + "Permanent: " + ChatColor.WHITE + channel.isPermanent(),
				color + "Local: " + ChatColor.WHITE + channel.isLocal(),
				color + "Private: " + ChatColor.WHITE + channel.isPrivate(),
				ChatColor.WHITE + "Color: " + color + channel.getColor()
		};
		
		return stats;
	}

	private void createChannel(String channelName) {
		
		ChatChannel newChannel = new ChatChannel(channelName, false, false, false, "white");
		channels.put(channelName, newChannel);
		
	}
	
	private String setChannel(Player player, String channelName, String setting, String value ) {
		
		ChatChannel channel = channels.get(channelName);
		String channelColor = channel.getColor();
		
		switch(setting.toLowerCase()) {
		
		case "permanent":
			
			if ( !player.hasPermission(Permissions.COMMAND_EDIT_CHANNEL) ) {
				player.sendMessage(ChatColor.RED + cannotInto);
				return cannotInto;
			}
			
			if ( value == null || (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false") ) ) {
				//add a help message
				return ChatColor.RED + "Sets the permanence of a Channel to true or false. Usage: /channel set private <value>";
			} else {
				channel.setPermanent(value);
				if ( channel.isPermanent() ) {
					cfManager.set("channels.yml", channelName + ".local", channel.isLocal());
					cfManager.set("channels.yml", channelName + ".private", channel.isPrivate());
					cfManager.set("channels.yml", channelName + ".color", channel.getColor());
					cfManager.set("channels.yml", channelName + ".shortCut", channel.getName().toLowerCase());
				} else {
					cfManager.set("channels.yml", channelName, null);
				}
				return ChatColor.translateAlternateColorCodes('&', channelColor + "set channel permanence to: " + value);
			}
		
		case "local":
			if ( value == null || (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false") ) ) {
				//add a help message
				return ChatColor.RED + "Sets the local-ness of a Channel to true or false. Usage: /channel set private <value>";
			} else {
				channels.get(channelName).setLocal(value);
				
				if ( channel.isPermanent() ) {
					cfManager.set("channels.yml", channelName + ".local", channel.isLocal());
				}
				
				return ChatColor.translateAlternateColorCodes('&', channelColor + "set channel localness to: " + value);
			}
			
		case "private":
			if ( value == null || (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false") ) ) {
				//add a help message
				return ChatColor.RED + "Sets the privacy of a Channel to true or false. Usage: /channel set private <value>";
			} else {
				channels.get(channelName).setPrivate(value);
				
				if ( channel.isPermanent() ) {
					cfManager.set("channels.yml", channelName + ".private", channel.isLocal());
				}
				
				return ChatColor.translateAlternateColorCodes('&', channelColor + "set channel privacy to: " + value);
			}
			
		case "color":
			if ( value == null ) {
				//add a help message
				return ChatColor.RED + "Sets the color of a Channel's text. The colors are: " + 
					ChatColor.AQUA + "\naqua, " + 
					ChatColor.BLACK + "black, " +
					ChatColor.BLUE + "blue, " + 
					ChatColor.DARK_AQUA + "\ndark_aqua, " +
					ChatColor.DARK_BLUE + "dark_blue, " + 
					ChatColor.DARK_GRAY + "dark_gray, " + 
					ChatColor.DARK_GREEN + "\ndark_green, " +
					ChatColor.DARK_PURPLE + "dark_purple, " + 
					ChatColor.DARK_RED + "dark_red, " + 
					ChatColor.GOLD + "\ngold, " + 
					ChatColor.GRAY + "gray, " + 
					ChatColor.GREEN + "green, " + 
					ChatColor.LIGHT_PURPLE + "\nlight_purple, " + 
					ChatColor.RED + "red, " + 
					ChatColor.WHITE + "white, " + 
					ChatColor.YELLOW + "\nyellow";
			} else {
				String newColor = channels.get(channelName).setColor(value);
				
				if ( channel.isPermanent() ) {
					cfManager.set("channels.yml", channelName + ".color", channel.getColorToString());
				}
				
				return ChatColor.translateAlternateColorCodes('&', channelColor + "Set channel color to: " + newColor);
			}
		
		case "colour":
			if ( value == null ) {
				//add a help message
				return ChatColor.RED + "Sets the colour of a Channel's text. The colors are: " + 
						ChatColor.AQUA + "\naqua, " + 
						ChatColor.BLACK + "black, " +
						ChatColor.BLUE + "blue, " + 
						ChatColor.DARK_AQUA + "\ndark_aqua, " +
						ChatColor.DARK_BLUE + "dark_blue, " + 
						ChatColor.DARK_GRAY + "dark_gray, " + 
						ChatColor.DARK_GREEN + "\ndark_green, " +
						ChatColor.DARK_PURPLE + "dark_purple, " + 
						ChatColor.DARK_RED + "dark_red, " + 
						ChatColor.GOLD + "\ngold, " + 
						ChatColor.GRAY + "gray, " + 
						ChatColor.GREEN + "green, " + 
						ChatColor.LIGHT_PURPLE + "\nlight_purple, " + 
						ChatColor.RED + "red, " + 
						ChatColor.WHITE + "white, " + 
						ChatColor.YELLOW + "\nyellow";
			} else {
				String newColor = channels.get(channelName).setColor(value);
				
				if ( channel.isPermanent() ) {
					cfManager.set("channels.yml", channelName + ".color", channel.getColorToString());
				}
				
				return ChatColor.translateAlternateColorCodes('&', channelColor + "Set channel colour to: " + newColor);
			}
		default:
			return ChatColor.RED + "That setting doesn't exist!";
		}
	}

	public void swapChannel(Player player, String joinedChannel) {
		String playerUUID = player.getUniqueId().toString().replace("-", "");
		String formerChannel = cfManager.get("players.yml", playerUUID + ".activeChannel");
		
		player.sendMessage("leaving the " + formerChannel + " channel");
		
		ChatChannel leftChannel = channels.get(formerChannel);
		leftChannel.leave(player);
		cfManager.set("players.yml", playerUUID + ".activeChannel", joinedChannel);
		
		if ( leftChannel.size() == 0 && !leftChannel.isPermanent() ) {
			channels.remove(formerChannel);
		}
		
		channels.get(joinedChannel).join(player);
		String activeChannel = cfManager.get("players.yml", playerUUID + ".activeChannel");
		String color = channels.get(activeChannel).getColor();
		
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "you joined the " + activeChannel + " channel"));
	}

}
