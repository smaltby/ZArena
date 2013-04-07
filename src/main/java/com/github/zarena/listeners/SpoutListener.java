package com.github.zarena.listeners;

import com.github.customentitylibrary.CustomEntitySpawnEvent;
import com.github.customentitylibrary.entities.CustomEntityWrapper;
import com.github.customentitylibrary.entities.EntityTypeConfiguration;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.Spout;
import org.getspout.spoutapi.event.input.KeyBindingEvent;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.keyboard.BindingExecutionDelegate;
import org.getspout.spoutapi.keyboard.Keyboard;
import org.getspout.spoutapi.player.EntitySkinType;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.github.zarena.ZArena;
import com.github.zarena.spout.PlayerOptions;

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
		if(event.getScreenType() != ScreenType.GAME_SCREEN)
			return;
		if(event.getBinding().getDefaultKey() == Keyboard.KEY_O)
			plugin.getPlayerOptionsHandler().getOptions(event.getPlayer().getName()).openOptions();
		if(event.getBinding().getDefaultKey() == Keyboard.KEY_GRAVE)
			if(plugin.getGameHandler().getPlayers().contains(event.getPlayer()))
			{
				plugin.getPlayerOptionsHandler().getOptions(event.getPlayer().getName()).openTabScreen();
			}
	}

	@Override
	public void keyReleased(KeyBindingEvent event)
	{
		if(event.getBinding().getDefaultKey() == Keyboard.KEY_GRAVE)
			plugin.getPlayerOptionsHandler().getOptions(event.getPlayer().getName()).closeTabScreen();
	}
	
	@EventHandler
	public void onButtonClick(ButtonClickEvent event)
	{
		
	}
	
	@EventHandler
	public void setEntityTexture(CustomEntitySpawnEvent event)
	{
		if(event.isCancelled())
			return;
		CustomEntityWrapper entity = event.getEntity();
		EntityTypeConfiguration entityType = (EntityTypeConfiguration) entity.getType();
		for(SpoutPlayer player : Spout.getServer().getOnlinePlayers())
		{
			PlayerOptions options = ZArena.getInstance().getPlayerOptionsHandler().getOptions(player.getName());
			if(!options.zombieTexturesEnabled)
				continue;
			if(entityType.getType().equalsIgnoreCase("wolf"))
			{
				player.setEntitySkin((LivingEntity) entity.getEntity().getBukkitEntity(), entityType.getSkinURL(), EntitySkinType.WOLF_ANGRY);
				player.setEntitySkin((LivingEntity) entity.getEntity().getBukkitEntity(), entityType.getSkinURL(), EntitySkinType.WOLF_TAMED);
			}
			else
				player.setEntitySkin((LivingEntity) entity.getEntity().getBukkitEntity(), entityType.getSkinURL(), EntitySkinType.DEFAULT);
		}
	}
	
	@EventHandler
	public void setPlayerWidgets(SpoutCraftEnableEvent event)
	{
		SpoutPlayer sPlayer = event.getPlayer();
		PlayerOptions options = plugin.getPlayerOptionsHandler().getOptions(sPlayer.getName());
		sPlayer.getMainScreen().attachWidget(ZArena.getInstance(), options.getWaveCounter());
		sPlayer.getMainScreen().attachWidget(ZArena.getInstance(), options.getWaveCounterWave());
		sPlayer.getMainScreen().attachWidget(ZArena.getInstance(), options.getWaveCounterZombies());
	}
}
