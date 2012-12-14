package kabbage.customentitylibrary;

import java.util.ConcurrentModificationException;

import kabbage.zarena.ZArena;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.PluginManager;

public class CustomEntityLibrary
{
	static
	{
		LibraryEntityListener eListener = new LibraryEntityListener();
		PluginManager pm = Bukkit.getServer().getPluginManager();
		eListener.registerEvents(pm, ZArena.getInstance());
		
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(ZArena.getInstance(), new Runnable()
		{
			@Override
			public void run()
			{
				onTick();
			}
		}, 1L, 1L);
	}
	
	private static int tick = 0;
	public static void onTick()
	{
		if(tick % 20 == 0)
		{
			for(World w : ZArena.getInstance().getServer().getWorlds())
			{
				try
				{
					for(LivingEntity ent : w.getLivingEntities())
					{
						if(CustomEntityWrapper.instanceOf(ent))
						{
							CustomEntityWrapper entity = CustomEntityWrapper.getCustomEntity(ent);
							entity.getType().showSpecialEffects(ent);
						}
					}
				} catch(ConcurrentModificationException e) {}
			}
		}
		tick++;
	}
}
