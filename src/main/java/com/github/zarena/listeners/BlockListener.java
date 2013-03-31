package main.java.com.github.zarena.listeners;

import main.java.com.github.zarena.GameHandler;
import main.java.com.github.zarena.ZArena;
import main.java.com.github.zarena.signs.ZSign;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;

public class BlockListener implements Listener
{
	private ZArena plugin;
	private GameHandler gameHandler;
	
	public BlockListener()
	{
		plugin = ZArena.getInstance();
		gameHandler = plugin.getGameHandler();
	}
	
	public void registerEvents(PluginManager pm, ZArena plugin)
	{
		pm.registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onSignChange(SignChangeEvent event)
	{
		if(event.isCancelled())
			return;
		Player player = event.getPlayer();
		ZSign.attemptCreateSign(gameHandler.getLevel(), player, event.getBlock());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(plugin.getGameHandler().getPlayers().contains(event.getPlayer()) && event.getPlayer().getGameMode() == GameMode.ADVENTURE)
			event.setCancelled(true);
	}
}
