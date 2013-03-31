package main.java.com.github.zarena.listeners;

import main.java.com.github.zarena.PlayerStats;
import main.java.com.github.zarena.ZArena;
import main.java.com.github.zarena.events.WaveChangeEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

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
	
	@EventHandler
	public void onWaveChange(WaveChangeEvent event)
	{
		int waveDifference = event.getNewWave() - event.getOldWave();
		for(PlayerStats stats : plugin.getGameHandler().getPlayerStats().values())
		{
			stats.setWavesSinceDeath(waveDifference + stats.getWavesSinceDeath());
		}
	}
}
