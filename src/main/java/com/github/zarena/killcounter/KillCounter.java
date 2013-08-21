package com.github.zarena.killcounter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;


import org.bukkit.Bukkit;

import com.github.zarena.ZArena;
import com.github.zarena.utils.Constants;

public class KillCounter
{
	public static KillCounter instance;

	// Needs to be two arraylists as opposed to a map to allow for dual random access and mutable value sorting
	private ArrayList<String> killsPlayers;
	private ArrayList<Integer> killsAmounts;
	
	public void enable()
	{
		instance = this;
		
		killsPlayers = new ArrayList<String>();
		killsAmounts = new ArrayList<Integer>();
		
		loadKills();

		new KCListener().registerEvents(Bukkit.getServer().getPluginManager());
		
		ZArena.getInstance().getCommand("killcounter").setExecutor(new KCCommands());
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(ZArena.getInstance(), new Runnable()
		{
			@Override
			public void run()
			{
				saveKills();
			}
		
		}, 1200L, 1200L);
	}
	
	public void disable()
	{
		saveKills();
	}
	
	public void addKill(String playerName)
	{
		int kills = killsPlayers.contains(playerName) ? killsAmounts.get(killsPlayers.indexOf(playerName)) + 1 : 1;
		setKills(playerName, kills);
	}
	
	public void setKills(String playerName, Integer kills)
	{
		// If the player has more kills than he had before, we try to raise him in the ranks, if he has less, we lower him in them
		int upDown = (killsPlayers.contains(playerName)) ? kills.compareTo(killsAmounts.get(killsPlayers.indexOf(playerName))) : 1;
		// If he has the same, then we don't need to change anything
		if(upDown == 0)
			return;
		int previousIndex = killsPlayers.indexOf(playerName);
		if(previousIndex == -1)
			previousIndex = killsPlayers.size();
		// The player currently being compared to our player
		String other;
		// The offset from the players previous rank. previousIndex + deltaIndex will be his new rank
		int deltaIndex = 0;
		do
		{
			deltaIndex -= upDown;
			if(killsPlayers.size() > previousIndex + deltaIndex && previousIndex + deltaIndex >= 0)
				other = killsPlayers.get(previousIndex + deltaIndex);
			else
				break;
		} while(kills.compareTo(getKills(other)) == upDown);
		deltaIndex += upDown;	// The player just failed against the last person tested against, so go back a step
		// Remove the player from both lists, assuming he was there in the first place
		if(killsPlayers.contains(playerName))
		{
			killsAmounts.remove(previousIndex);
			killsPlayers.remove(playerName);
		}
		// Now readd him with his new rank
		if(previousIndex == -1)
			previousIndex++;
		killsAmounts.add(previousIndex + deltaIndex, kills);
		killsPlayers.add(previousIndex + deltaIndex, playerName);
	}
	
	public Integer getKills(String playerName)
	{
		if(!killsPlayers.contains(playerName))
			return null;
		return killsAmounts.get(killsPlayers.indexOf(playerName));
	}
	
	public Entry<String, Integer> getEntry(int index)
	{
		if(killsPlayers.size() <= index) return null;
		return new AbstractMap.SimpleEntry<String, Integer>(killsPlayers.get(index), killsAmounts.get(index));
	}
	
	public int indexOf(String playerName)
	{
		if(!killsPlayers.contains(playerName))
			return -1;
		return killsPlayers.indexOf(playerName);
	}
	
	public int mapSize()
	{
		return killsPlayers.size();
	}
	
	private void saveKills()
	{
		File file = new File(Constants.KILLS_PATH);
		ObjectOutputStream out;
		try
		{
			file.createNewFile();
			out = new ObjectOutputStream(new FileOutputStream(Constants.KILLS_PATH, false));
			
			out.writeObject(killsPlayers);
			out.writeObject(killsAmounts);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadKills()
	{
		File file = new File(Constants.KILLS_PATH);
		if(!file.exists())
			return;
		ObjectInputStream in;
		try
		{
			in = new ObjectInputStream(new FileInputStream(Constants.KILLS_PATH));
			
			killsPlayers = (ArrayList<String>) in.readObject();
			killsAmounts = (ArrayList<Integer>) in.readObject();
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}