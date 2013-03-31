package main.java.com.github.zarena.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatHelper
{
	/**
	 * Argument 1: (int) Wave number<p>
	 * Argument 2: (floating point) Seconds until wave starts
	 */
	public static final String WAVE_START_SOON = ChatColor.GRAY+"Starting Wave: "+ChatColor.RED+"%d"+ChatColor.GRAY+" in "+ChatColor.RED+"%.1f"+
			ChatColor.GRAY+" Seconds";
	/**
	 * Argument 1: (int) Wave number<p>
	 * Argument 2: (int) Zombies in wave<p>
	 * Argument 3: (int) Health of zombies
	 */
	public static final String WAVE_START = ChatColor.GRAY+"Starting Wave "+ChatColor.RED+"%d"+ChatColor.GRAY+" with "+ChatColor.RED+"%d"+ChatColor.GRAY+
			" Zombies at "+ChatColor.RED+"%d"+ChatColor.GRAY+" Health";
	/**
	 * No arguments
	 */
	public static final String GIGA_SPAWN = ChatColor.DARK_PURPLE+"A Giga Zombie has spawned!";
	/**
	 * No arguments
	 */
	public static final String[] VOTE_START = new String[] {ChatColor.DARK_GREEN + "Level vote starting...", ChatColor.DARK_GREEN + "Type the command - "};
	/**
	 * Argument 1: (int) Option number<p>
	 * Argument 2: (String) Level name<p>
	 * Argument 3: (String) Gamemode name
	 */
	public static final String VOTE_OPTION = ChatColor.RED + "/za vote "+"%d"+ChatColor.GRAY+" for map "+ChatColor.DARK_AQUA+"%s"+ChatColor.GRAY+
			"<"+ChatColor.DARK_AQUA+"%s"+ChatColor.GRAY+">";
	/**
	 * Argument 1: (int) Seconds until voting ends
	 */
	public static final String VOTE_ENDS_IN = ChatColor.DARK_GREEN + "Voting ends in %d seconds.";
	/**
	 * Argument 1: (String) Level name<p>
	 * Argument 2: (String) Gamemode name
	 */
	public static final String MAP_CHOSEN = ChatColor.DARK_GRAY+"Level "+ChatColor.DARK_AQUA+"%s"+ChatColor.GRAY+"<"+ChatColor.DARK_AQUA+"%s"+ChatColor.GRAY+">"+ChatColor.DARK_GRAY+
			" has been chosen.";
	/**
	 * Argument 1: (int) Option number<p>
	 * Argument 2: (int) Votes for
	 */
	public static final String VOTES_FOR_MAP = ChatColor.RED+"Votes for %d: %d";
	/**
	 * No arguments
	 */
	public static final String INSUFFICIENT_PERMISSIONS = ChatColor.RED + "Insufficient permissions to do this.";
	/**
	 * No arguments
	 */
	public static final String NO_LEVEL_LOADED = ChatColor.RED + "No level loaded. Please load a level to use/edit.";
	/**
	 * No arguments
	 */
	public static final String NOT_ALLOWED_WHILE_RUNNING = ChatColor.RED + "You may not do this while the game is running.";
	/**
	 * No arguments
	 */
	public static final String GAME_MUST_BE_RUNNING = ChatColor.RED + "The game must be running to do this.";
	
	public static String broadcastMessage(String message, List<Player> players)
	{
		return broadcastMessage(new String[] {message}, players)[0];
	}
	
	public static String[] broadcastMessage(String[] message, List<Player> players)
	{
		return broadcastMessage(message, players.toArray(new Player[players.size()]));
	}
	
	public static String broadcastMessage(String message, Player[] players)
	{
		return broadcastMessage(new String[] {message}, players)[0];
	}
	
	public static String broadcastMessage(String message)
	{
		return broadcastMessage(new String[] {message})[0];
	}
	
	public static String[] broadcastMessage(String[] message)
	{
		return broadcastMessage(message, Bukkit.getServer().getOnlinePlayers());
	}
	
	public static String[] broadcastMessage(String[] message, Player[] players)
	{
		for(Player p : players)
		{
			for(String messagePart : message)
			{
				p.sendMessage(messagePart);
			}
		}
		for(String messagePart : message)
		{
			Bukkit.getConsoleSender().sendMessage(messagePart);
		}
		return message;
	}
}
