package com.github.zarena.afkmanager;

import com.github.zarena.events.GameStopEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;

public class AFKListener implements Listener
{
	public static AFKManager plugin;

	public AFKListener()
	{
		plugin = AFKManager.instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		plugin.onAction(event.getPlayer());
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		plugin.onAction(event.getPlayer());
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		plugin.onAction(event.getPlayer());
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		plugin.onAction(event.getPlayer());
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		plugin.onAction(event.getPlayer());
	}

	@EventHandler
	public void onItemHeldChange(PlayerItemHeldEvent event)
	{
		plugin.onAction(event.getPlayer());
	}

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event)
	{
		plugin.onAction(event.getPlayer());
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		plugin.onAction(event.getPlayer());
	}

	@EventHandler
	public void onGameStop(GameStopEvent event)
	{
		plugin.currentlyAFK.clear();
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			//Don't tell the plugin it's an action, or the player will be set as alive again
			plugin.currentlyAFK.remove(((Player) event.getEntity()).getName());
		}
	}
}
