package com.github.zarena.afkmanager;

import com.github.zarena.PlayerStats;
import com.github.zarena.ZArena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AFKManager
{
	public static AFKManager instance;
	Map<String, Long> lastAction = new HashMap<String, Long>();
	List<String> currentlyAFK = new ArrayList<String>();

	public void enable()
	{
		instance = this;

		Bukkit.getPluginManager().registerEvents(new AFKListener(), ZArena.getInstance());

		Bukkit.getScheduler().scheduleSyncRepeatingTask(ZArena.getInstance(), new Runnable()
		{
			@Override
			public void run()
			{
				onSecond();
			}

		}, 20L, 20L);
	}

	public void onAction(Player p)
	{
		lastAction.put(p.getName(), System.currentTimeMillis());
	}

	public void onSecond()
	{
		Iterator<String> i = lastAction.keySet().iterator();
		while(i.hasNext())
		{
			String pName = i.next();
			if(Bukkit.getPlayer(pName) == null)
			{
				i.remove();
				continue;
			}
			PlayerStats stats = ZArena.getInstance().getGameHandler().getPlayerStats(pName);
			if(stats == null || !stats.isAlive())
			{
				i.remove();
				continue;
			}
			if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastAction.get(pName)) > 120)
			{
				stats.setAlive(false);
				currentlyAFK.add(pName);
			} else if(currentlyAFK.contains(pName))
			{
				stats.setAlive(true);
			}
		}
	}
}
