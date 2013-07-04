package com.github.zarena.listeners;


import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import com.github.zarena.ZArena;

public class WorldListener implements Listener
{
	public WorldListener()
	{

	}

	public void registerEvents(PluginManager pm, ZArena plugin)
	{
		pm.registerEvents(this, plugin);
	}
}
