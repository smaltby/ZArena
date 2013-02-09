package kabbage.killcounter;

import kabbage.customentitylibrary.CustomEntityWrapper;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.PluginManager;

public class KCListener implements Listener
{
	private KillCounter plugin;
	
	public void registerEvents(PluginManager pm)
	{
		plugin = KillCounter.instance;
		pm.registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onEntityDeath (EntityDeathEvent event)
	{
		Entity entity = event.getEntity();
		if(CustomEntityWrapper.instanceOf(entity))
		{
			CustomEntityWrapper customEnt = CustomEntityWrapper.getCustomEntity(entity);
			Player player = customEnt.getBestAttacker();
			if(player != null)
				plugin.addKill(player.getName());
		}
	}
}
