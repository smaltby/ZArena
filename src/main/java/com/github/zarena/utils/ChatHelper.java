package com.github.zarena.utils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ChatHelper
{
	public static Map<String, String> messages = new HashMap<String, String>();

	public static void loadLanguageFile()
	{
		YamlConfiguration language = YamlConfiguration.loadConfiguration(new File(Constants.LANGUAGE_PATH));
		for(String key : language.getKeys(false))
		{
			messages.put(key, language.getString(key));
		}
	}

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
		for(String messagePart : message)
		{
			if(messagePart != null && messagePart.length() > 0)
			{
				for(Player p : players)
				{
					p.sendMessage(messagePart);
				}
				Bukkit.getConsoleSender().sendMessage(messagePart);
			}
		}
		return message;
	}

	public static String sendMessage(String message, Player player)
	{
		return sendMessage(new String[] {message}, player)[0];
	}

	public static String[] sendMessage(String[] message, Player player)
	{
		for(String messagePart : message)
		{
			if(messagePart != null && messagePart.length() > 0)
			{
				player.sendMessage(messagePart);
			}
		}
		return message;
	}
}
