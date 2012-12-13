package kabbage.customentitylibrary;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomEntityMoveEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private Location to;
	private Location from;
	private Entity ent;
	private boolean cancel;
	 
	public CustomEntityMoveEvent(Entity ent, Location from, Location to)
	{
		this.ent = ent;
		this.to = to;
		this.from = from;
	}
	
	@Override
	public HandlerList getHandlers()
	{
	    return handlers;
	}
	
	@Override
	public String getEventName()
	{
		return "CustomEntity Move Event";
	}
	
	public static HandlerList getHandlerList()
	{
	    return handlers;
	}
	
	public Entity getEntity()
	{
		return ent;
	}

	public Location getTo()
	{
		return to;
	}
	
	public Location getFrom()
	{
		return from;
	}
	
	public void setCancelled(boolean cancel)
	{
		this.cancel = cancel;
	}
	
	public boolean isCancelled()
	{
		return cancel;
	}
}
