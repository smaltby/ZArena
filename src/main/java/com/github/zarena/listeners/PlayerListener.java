package com.github.zarena.listeners;

import com.github.zarena.utils.ConfigEnum;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;

import com.github.zarena.GameHandler;
import com.github.zarena.ZArena;
import com.github.zarena.ZLevel;
import com.github.zarena.commands.CommandSenderWrapper;
import com.github.zarena.signs.ZSign;
import com.github.zarena.signs.ZTollSign;
import com.github.zarena.utils.ChatHelper;
import com.github.zarena.utils.Message;

public class PlayerListener implements Listener
{
	private ZArena plugin;
	private GameHandler gameHandler;

	public PlayerListener()
	{
		plugin = ZArena.getInstance();
		gameHandler = plugin.getGameHandler();
	}

	public void registerEvents(PluginManager pm, ZArena plugin)
	{
		pm.registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if(gameHandler.getLevel() != null && plugin.getGameHandler().getPlayerNames().contains(event.getPlayer().getName()))
		{
			//Teleport player to death spawn
			event.setRespawnLocation(gameHandler.getLevel().getDeathSpawn());

			if(gameHandler.isRunning())
			{
                boolean respawningEnabled = false;
				//Send messages informing the player when he will next respawn, if applicable
				int respawnEveryTime = plugin.getConfiguration().getInt(ConfigEnum.RESPAWN_EVERY_TIME.toString());
				if(respawnEveryTime != 0)
				{
					ChatHelper.sendMessage(Message.RESPAWN_IN_TIME_AFTER_DEATH.formatMessage(event.getPlayer().getName(),
												respawnEveryTime + "min"), event.getPlayer());
                    respawningEnabled = true;
				}
				int respawnEveryWaves = plugin.getConfiguration().getInt(ConfigEnum.RESPAWN_EVERY_WAVES.toString());
				if(respawnEveryWaves != 0)
				{
					ChatHelper.sendMessage(Message.RESPAWN_IN_WAVES_AFTER_DEATH.formatMessage(event.getPlayer().getName(),
												respawnEveryWaves), event.getPlayer());
                    respawningEnabled = true;
				}
                //Else, send a message informing the player to wait until the next game to play
                if(!respawningEnabled)
                {
                    ChatHelper.sendMessage(Message.ON_PLAYER_DEATH.formatMessage(event.getPlayer().getName()), event.getPlayer());
                }
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		if(plugin.getConfiguration().getBoolean(ConfigEnum.AUTOJOIN.toString()))
		{
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()	//Add a delay in case the player is in a different world from the game world
			{
				public void run()
				{
					if(new CommandSenderWrapper(event.getPlayer()).autoJoin())
						gameHandler.addPlayer(event.getPlayer());
				}
			}, 1L);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		gameHandler.removePlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerKick(PlayerKickEvent event)
	{
		gameHandler.removePlayer(event.getPlayer());
	}

	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		if(plugin.getConfiguration().getBoolean(ConfigEnum.DISABLE_NON_ZA.toString()) && plugin.getGameHandler().getPlayers().contains(event.getPlayer()))
		{
			if(!event.getMessage().matches("/zarena.*") && !event.getMessage().matches("/za.*"))
			{
				ZArena.log(event.getMessage());
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.isCancelled())
			return;
		Block block = event.getClickedBlock();
		if(block == null)
			return;
		ZLevel level = gameHandler.getLevel();
		if(level == null)
			return;
		Player player = event.getPlayer();
		if(gameHandler.isRunning() && gameHandler.getPlayers().contains(player))
		{
			if(block.getState() instanceof Sign)
			{
				ZSign sign = level.getZSign(block);
				if(event.getAction() == Action.RIGHT_CLICK_BLOCK && sign != null)
				{
					sign.onClick(player);
					if(sign instanceof ZTollSign)
						level.resetInactiveZSpawns();
				}
			}
			else
				for(ZSign sign : level.getZSigns())
				{
					if(sign instanceof ZTollSign)
						if(((ZTollSign) sign).getCostBlock().equals(block))
						{
							event.setCancelled(true);	//Cancel the event, as the block changes are handled by the sign
							if(sign.onClick(player))
								level.resetInactiveZSpawns();
						}
				}
		}
		else if(new CommandSenderWrapper(player).canCreateLevels() && player.getGameMode() == GameMode.CREATIVE
				&& (!gameHandler.getPlayers().contains(player) || !gameHandler.isRunning()))
		{
			if(!(block.getState() instanceof Sign))
				return;
			ZSign sign = level.getZSign(block);
			if(sign != null)
			{
				level.removeZSign(sign);
				ChatHelper.sendMessage(Message.SIGN_REMOVE.formatMessage(), player);
			}
			else if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				sign = ZSign.attemptCreateSign(gameHandler.getLevel(), block.getLocation(), player, ((Sign)block.getState()).getLines());
				if(sign != null && sign instanceof ZTollSign)
					sign.getSign().setLine(2, "");	//Clear the flags
			}
		}
	}
}
