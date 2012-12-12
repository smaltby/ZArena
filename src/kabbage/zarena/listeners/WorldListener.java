package kabbage.zarena.listeners;

import kabbage.zarena.ZArena;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class WorldListener implements Listener
{
	@SuppressWarnings("unused")
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
