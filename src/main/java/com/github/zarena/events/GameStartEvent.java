package com.github.zarena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStartEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private GameStartCause cause;
	
	public GameStartEvent(GameStartCause cause)
	{
		this.cause = cause;
	}
	
	public GameStartCause getCause()
	{
		return cause;
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
