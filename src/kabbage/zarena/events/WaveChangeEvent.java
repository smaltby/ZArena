package kabbage.zarena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WaveChangeEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private int oldWave;
	private int newWave;
	
	public WaveChangeEvent(int oldWave, int newWave)
	{
		this.oldWave = oldWave;
		this.newWave = newWave;
	}
	
	public int getOldWave()
	{
		return oldWave;
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
