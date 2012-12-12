package kabbage.zarena.listeners;

import kabbage.zarena.ZArena;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.event.input.KeyBindingEvent;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.keyboard.BindingExecutionDelegate;
import org.getspout.spoutapi.keyboard.Keyboard;

public class SpoutListener implements BindingExecutionDelegate, Listener
{
	private ZArena plugin;
	
	public SpoutListener()
	{
		plugin = ZArena.getInstance();
	}
	
	public void registerEvents(PluginManager pm, ZArena plugin)
	{
		pm.registerEvents(this, plugin);
	}

	@Override
	public void keyPressed(KeyBindingEvent event)
	{
		if(event.getBinding().getDefaultKey() == Keyboard.KEY_O)
			plugin.getPlayerOptionsHandler().getOptions(event.getPlayer().getName()).openOptions();
	}

	@Override
	public void keyReleased(KeyBindingEvent event)
	{
		
	}
	
	@EventHandler
	public void onButtonClick(ButtonClickEvent event)
	{
		
	}
}
