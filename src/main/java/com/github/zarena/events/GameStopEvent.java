package main.java.com.github.zarena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStopEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private GameStopCause cause;
	
	public GameStopEvent(GameStopCause cause)
	{
		this.cause = cause;
	}
	
	public GameStopCause getCause()
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
