package kabbage.zarena.listeners;

import kabbage.zarena.PlayerStats;
import kabbage.zarena.ZArena;
import kabbage.zarena.events.WaveChangeEvent;
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
