package com.github.zarena.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;

import com.github.zarena.GameHandler;
import com.github.zarena.ZArena;
import com.github.zarena.commands.CommandSenderWrapper;
import com.github.zarena.signs.ZSign;
import com.github.zarena.signs.ZTollSign;

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
		if(new CommandSenderWrapper(player).canCreateLevels() && player.getGameMode() == GameMode.CREATIVE 
				&& (!gameHandler.getPlayers().contains(player) || !gameHandler.isRunning()))
		{
			ZSign sign = ZSign.attemptCreateSign(gameHandler.getLevel(), event.getBlock().getLocation(), player, event.getLines());
			if(sign != null && sign instanceof ZTollSign)
				event.setLine(2, "");	//Clear the flags
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(plugin.getGameHandler().getPlayers().contains(event.getPlayer()) && event.getPlayer().getGameMode() == GameMode.ADVENTURE)
			event.setCancelled(true);
	}
}
