package com.github.zarena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WaveChangeEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private int oldWave;
	private int newWave;
	private int secondsUntilStart;

	public WaveChangeEvent(int oldWave, int newWave, int secondsUntilStart)
	{
		this.oldWave = oldWave;
		this.newWave = newWave;
		this.secondsUntilStart = secondsUntilStart;
	}
	
	public int getOldWave()
	{
		return oldWave;
	}
	
	public int getNewWave()
	{
		return newWave;
	}

	public void setNewWave(int newWave)
	{
		this.newWave = newWave;
	}

	public int getSecondsUntilStart()
	{
		return secondsUntilStart;
	}

	public void setSecondsUntilStart(int seconds)
	{
		secondsUntilStart = seconds;
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
