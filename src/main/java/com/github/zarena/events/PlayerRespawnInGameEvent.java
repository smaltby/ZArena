package com.github.zarena.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class PlayerRespawnInGameEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private Player player;
	List<ItemStack> startItems;
	private PlayerRespawnCause cause;
	private boolean cancel;

	public PlayerRespawnInGameEvent(Player player, List<ItemStack> startItems, PlayerRespawnCause cause)
	{
		this.player = player;
		this.startItems = startItems;
		this.cause = cause;
	}

	public Player getPlayer()
	{
		return player;
	}

	public List<ItemStack> getStartItems()
	{
		return startItems;
	}

	public void setStartItems(List<ItemStack> startItems)
	{
		this.startItems = startItems;
	}

	public void addStartItems(ItemStack... items)
	{
		Collections.addAll(startItems, items);
	}

	public PlayerRespawnCause getCause()
	{
		return cause;
	}

	public boolean isCancelled()
	{
		return cancel;
	}

	public void setCancelled(boolean cancel)
	{
		this.cancel = cancel;
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
