package com.github.zarena.events;



import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.github.zarena.ZLevel;


public class LevelChangeEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private ZLevel oldLevel;
	private ZLevel newLevel;
	private LevelChangeCause cause;
	
	public LevelChangeEvent(ZLevel oldLevel, ZLevel newLevel, LevelChangeCause cause)
	{
		this.oldLevel = oldLevel;
		this.newLevel = newLevel;
		this.cause = cause;
	}
	
	public ZLevel getOldLevel()
	{
		return oldLevel;
	}
	
	public ZLevel getNewLevel()
	{
		return newLevel;
	}
	
	public LevelChangeCause getCause()
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
