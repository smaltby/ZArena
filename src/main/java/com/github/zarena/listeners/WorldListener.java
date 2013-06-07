package com.github.zarena.listeners;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import com.github.zarena.PlayerStats;
import com.github.zarena.ZArena;
import com.github.zarena.events.WaveChangeEvent;

public class WorldListener implements Listener
{
	private ZArena plugin;

	public WorldListener()
	{
		plugin = ZArena.getInstance();
	}

	public void registerEvents(PluginManager pm, ZArena plugin)
	{
		pm.registerEvents(this, plugin);
	}
}
