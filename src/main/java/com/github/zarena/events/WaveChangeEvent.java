package com.github.zarena.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class WaveChangeEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private int oldWave;
	private int newWave;
	private List<Player> respawned;

	public WaveChangeEvent(int oldWave, int newWave, List<Player> respawned)
	{
		this.oldWave = oldWave;
		this.newWave = newWave;
		this.respawned = respawned;
	}
	
	public int getOldWave()
	{
		return oldWave;
	}
	
	public int getNewWave()
	{
		return newWave;
	}

	public List<Player> getNewlyRespawned()
	{
		return respawned;
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
