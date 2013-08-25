package com.github.zarena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WaveStartEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private int newWave;

	public WaveStartEvent(int newWave)
	{
		this.newWave = newWave;
	}

	public int getNewWave()
	{
		return newWave;
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
