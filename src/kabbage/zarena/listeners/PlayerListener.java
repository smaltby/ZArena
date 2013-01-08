package kabbage.zarena.listeners;

import kabbage.zarena.GameHandler;
import kabbage.zarena.ZArena;
import kabbage.zarena.ZLevel;
import kabbage.zarena.commands.utils.CommandSenderWrapper;
import kabbage.zarena.signs.ZSign;
import kabbage.zarena.signs.ZTollSign;
import kabbage.zarena.utils.Constants;

import org.bukkit.ChatColor;
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
			event.setRespawnLocation(gameHandler.getLevel().getDeathSpawn());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		if(plugin.getConfig().getBoolean(Constants.AUTOJOIN))
		{
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()	//Add a delay in case the player is in a different world from the game world
			{
				public void run()
				{
					gameHandler.addPlayer(event.getPlayer());
				}
			}, 1L);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		gameHandler.removePlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event)
	{
		gameHandler.removePlayer(event.getPlayer());
	}
	
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		if(plugin.getConfig().getBoolean(Constants.DISABLE_NON_ZA) && plugin.getGameHandler().getPlayers().contains(event.getPlayer()))
		{
			if(!event.getMessage().equalsIgnoreCase("zarena") && !event.getMessage().equalsIgnoreCase("za"))
			{
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
				player.sendMessage(ChatColor.RED + "ZSign removed.");
			}
			else if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
				ZSign.attemptCreateSign(gameHandler.getLevel(), player, block);
		}
	}
}
