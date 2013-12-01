package com.github.zarena;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


import com.github.customentitylibrary.entities.CustomEntityWrapper;

import com.github.zarena.events.PlayerRespawnCause;
import com.github.zarena.events.PlayerRespawnInGameEvent;
import com.github.zarena.utils.*;
import net.minecraft.server.v1_7_R1.NBTTagDouble;
import net.minecraft.server.v1_7_R1.NBTTagList;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import com.github.zarena.commands.CommandSenderWrapper;
import com.github.zarena.signs.ZSignCustomItem;
import com.google.common.io.Files;

public class GameHandler
{
	private ZArena plugin;
	private WaveHandler waveHandler;
	private LevelHandler levelHandler;
	private LevelVoter levelVoter;

	private boolean isRunning;
	protected boolean isVoting; //protected to allow easy access from level voter
	private boolean isWaiting;

	private List<String> players;
	private Map<String, PlayerStats> playerStats;

	private Gamemode gamemode;
	private ZLevel level;

	public Gamemode defaultGamemode;
	public List<Gamemode> gamemodes = new ArrayList<Gamemode>();

	public GameHandler()
	{
		plugin = ZArena.getInstance();
		isRunning = false;
		isVoting = false;
		isWaiting = plugin.getConfig().getBoolean(ConfigEnum.AUTOSTART.toString());
		waveHandler = new WaveHandler(this);
		levelVoter = new LevelVoter(this);
		players = new ArrayList<String>();
		playerStats = new HashMap<String, PlayerStats>();
	}

	/**
	 * Adds a player to the game.
	 * @param player the player to add
	 */
	public void addPlayer(Player player)
	{
		if(players.size() >= plugin.getConfig().getInt(ConfigEnum.PLAYER_LIMIT.toString()))
		{
			ChatHelper.sendMessage(Message.GAME_FULL.formatMessage(), player);
			return;
		}
		if(players.contains(player.getName()))
			return;

		players.add(player.getName());
		PlayerStats stats = new PlayerStats(player);
		playerStats.put(player.getName(), stats);
		//Backup the players previous stats, so if the server crashes, they can be restored from the file
		backupStats(stats);

		if(plugin.getConfig().getBoolean(ConfigEnum.SEPERATE_INVENTORY.toString()))
			clearInventory(player.getInventory());

		player.setGameMode(org.bukkit.GameMode.ADVENTURE);

		int wave = waveHandler.getWave();
		if(isRunning)
		{
			if(wave == 1 && !(gamemode.isApocalypse()))
			{
				PlayerRespawnInGameEvent event = new PlayerRespawnInGameEvent(stats.getPlayer(), getStartItems(), PlayerRespawnCause.GAME_START);
				Bukkit.getPluginManager().callEvent(event);
				if(!event.isCancelled())
					addToGame(stats, event.getStartItems());
			} else
			{
				//Send messages informing the player when he will next respawn, if applicable
                boolean respawningEnabled = false;
				int respawnEveryTime = plugin.getConfig().getInt(ConfigEnum.RESPAWN_EVERY_TIME.toString());
				if(respawnEveryTime != 0)
				{
					ChatHelper.sendMessage(Message.RESPAWN_IN_TIME_AFTER_JOIN.formatMessage(player.getName(),
												respawnEveryTime + "min"), player);
                    respawningEnabled = true;
				}
				int respawnEveryWaves = plugin.getConfig().getInt(ConfigEnum.RESPAWN_EVERY_WAVES.toString());
				if(respawnEveryWaves != 0)
				{
					ChatHelper.sendMessage(Message.RESPAWN_IN_WAVES_AFTER_JOIN.formatMessage(stats.getPlayer().getName(),
												respawnEveryWaves), stats.getPlayer());
                    respawningEnabled = true;
				}
                //Else, send a message informing the player to wait until the next game to play
                if(!respawningEnabled)
                {
                    ChatHelper.sendMessage(Message.ON_PLAYER_JOIN.formatMessage(stats.getPlayer().getName()), stats.getPlayer());
                }
				if(level != null)
					player.teleport(level.getDeathSpawn());
				else
					player.teleport(player.getWorld().getSpawnLocation());
			}
		}
		else if(isVoting)
		{
			if(level != null)
				player.teleport(level.getDeathSpawn());
			else
				player.teleport(player.getWorld().getSpawnLocation());
			ChatHelper.sendMessage(Message.VOTE_START.formatMessage(), player);
			ChatHelper.sendMessage(levelVoter.getVoteMessage(), player);
			ChatHelper.sendMessage(Message.VOTE_ENDS_IN.formatMessage(plugin.getConfig().getInt(ConfigEnum.VOTING_LENGTH.toString())), player);
		}
		else
		{
			if(level != null)
				player.teleport(level.getDeathSpawn());
			if(isWaiting)
				start();
		}
	}

	/**
	 * Prepares everything about a player and his/her stats in preperation for joining a game
	 * @param stats the stats to reset, and the stats to get the player from who is then prepared for the game
	 * @param startItems items to start with
	 */
	private void addToGame(PlayerStats stats, List<ItemStack> startItems)
	{
		stats.resetStats();
		stats.setAlive(true);

		Player player = stats.getPlayer();
		if(player == null)
			return;
		if(level != null)
			player.teleport(level.getInitialSpawn());
		else
			player.teleport(player.getWorld().getSpawnLocation());
		player.setHealth(player.getMaxHealth());
		((CraftPlayer) player).getHandle().triggerHealthUpdate();
		player.setFoodLevel(20);
		player.setSaturation(20);

		stats.addMoney(new CommandSenderWrapper(player).startMoney());

		for(PotionEffect effect : player.getActivePotionEffects())
		{
			player.removePotionEffect(effect.getType());
		}
		PlayerInventory pi = player.getInventory();
		if(plugin.getConfig().getBoolean(ConfigEnum.SEPERATE_INVENTORY.toString()))
			clearInventory(pi);
		for(ItemStack item : startItems)
			pi.addItem(item);
	}

	private void backupStats(PlayerStats stats)
	{
		Configuration config = plugin.statsBackup;
		ConfigurationSection playerSection = config.createSection(stats.getPlayer().getName());
		Location oldLocation = stats.getOldLocation();
		playerSection.set("world", oldLocation.getWorld().getName());
		playerSection.set("x", oldLocation.getX());
		playerSection.set("y", oldLocation.getY());
		playerSection.set("z", oldLocation.getZ());
		int index = 0;
		for(ItemStack item : stats.getInventoryContents())
		{
			if(item != null)
				playerSection.set("items."+(index++), item.serialize());
		}
		index = 0;
		for(ItemStack armor : stats.getInventoryArmor())
		{
			if(armor != null)
				playerSection.set("armor."+(index++), armor.serialize());
		}
		playerSection.set("gamemode", stats.getOldGameMode().getValue());
		playerSection.set("money", stats.getOldMoney());
		playerSection.set("level", stats.getOldLevel());
		try
		{
			plugin.statsBackup.save(Constants.BACKUP_PATH);
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public void clearInventory(PlayerInventory inv)
	{
		inv.clear();
		inv.setHelmet(null);
		inv.setChestplate(null);
		inv.setLeggings(null);
		inv.setBoots(null);
	}

	public int getAliveCount()
	{
		int alive = 0;
		for(PlayerStats stats : playerStats.values())
		{
			if(stats.isAlive())
				alive++;
		}
		return alive;
	}

	public List<Player> getBroadcastPlayers()
	{
		List<Player> toBroadcast = new ArrayList<Player>();
		if(plugin.getConfig().getBoolean(ConfigEnum.BROADCAST_ALL.toString(), false))
			toBroadcast = Arrays.asList(Bukkit.getOnlinePlayers());
		else if(plugin.getConfig().getBoolean(ConfigEnum.WORLD_EXCLUSIVE.toString(), false))
		{
			for(Player p : Bukkit.getServer().getOnlinePlayers())
			{
				if(level != null)
				{
					if(p.getWorld().getName().equals(level.getWorld()))
						toBroadcast.add(p);
				} else
				{
					if(p.getWorld().getName().equals(levelHandler.getLevels().get(0).getWorld()))
						toBroadcast.add(p);
				}
			}
		} else
			toBroadcast = getPlayers();
		return toBroadcast;
	}

	public Gamemode getGameMode()
	{
		return gamemode;
	}

	public ZLevel getLevel()
	{
		return level;
	}

	public LevelHandler getLevelHandler()
	{
		return levelHandler;
	}

	public LevelVoter getLevelVoter()
	{
		return levelVoter;
	}

	public synchronized List<String> getPlayerNames()
	{
		return players;
	}

	public synchronized List<Player> getPlayers()
	{
		List<Player> playerInstances = new ArrayList<Player>();
		for(String playerName : players)
		{
			Player player = Bukkit.getPlayer(playerName);
			if(player != null)
				playerInstances.add(player);
		}
		return playerInstances;
	}

	public Location getPlayersLeaveLocation(String playerName)
	{
		if(plugin.getConfig().getBoolean(ConfigEnum.SAVE_POSITION.toString()))
		{
			Location oldLocation = getPlayerStats(playerName).getOldLocation();
			if(oldLocation != null)
				return oldLocation;
		}
		World world = Bukkit.createWorld(new WorldCreator(plugin.getConfig().getString(ConfigEnum.GAME_LEAVE_WORLD.toString(), "world")));
		List<Double> locXYZ = plugin.getConfig().getDoubleList(ConfigEnum.GAME_LEAVE_LOCATION.toString());
        return new Location(world, locXYZ.get(0), locXYZ.get(1), locXYZ.get(2));
	}

	public synchronized Map<String, PlayerStats> getPlayerStats()
	{
		return playerStats;
	}

	public synchronized PlayerStats getPlayerStats(String player)
	{
		return playerStats.get(player);
	}

	public synchronized PlayerStats getPlayerStats(Player player)
	{
		return playerStats.get(player.getName());
	}

	protected List<ItemStack> getStartItems()
	{
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(String item : plugin.getConfig().getStringList(ConfigEnum.START_ITEMS.toString()))
		{
			ZSignCustomItem customItem = ZSignCustomItem.getCustomItem(item.split("\\s"));
			if(customItem != null)
			{
				items.add(customItem.getItem());
				continue;
			}
			Material mat = Material.getMaterial(item.replaceAll(" ", "_").toUpperCase());
			if(mat == null)
				continue;
			ItemStack itemStack = new ItemStack(mat);
			items.add(itemStack);
		}
		for(ItemStack item : gamemode.getStartItems())
		{
			items.add(item);
		}
		return items;
	}

	public WaveHandler getWaveHandler()
	{
		return waveHandler;
	}

	public boolean isRunning()
	{
		return isRunning;
	}

	public boolean isVoting()
	{
		return isVoting;
	}

	public boolean isWaiting()
	{
		return isWaiting;
	}

	void loadLevelHandler()
	{
		File path = new File(Constants.LEVEL_PATH);

        try
        {
        	FileInputStream fis = new FileInputStream(path);
        	CustomObjectInputStream ois = new CustomObjectInputStream(fis);

            levelHandler = new LevelHandler();
            levelHandler.readExternal(ois);

            ois.close();
            fis.close();

        } catch (Exception e)
        {
        	ZArena.log(Level.WARNING, "ZArena: Couldn't load the LevelHandler database. Ignore if this is the first time the plugin has been run.");
            levelHandler = new LevelHandler();
        }
	}

	/**
	 * Removes a player from the game. Only use when the player managed to get off the server without being detected.
	 * @param playerName name of player to remove
	 */
	public void removePlayer(String playerName)
	{
		try
		{
			PlayerStats stats = getPlayerStats(playerName);
			if(plugin.getConfig().getBoolean(ConfigEnum.SEPERATE_INVENTORY.toString()))
			{
				//Set inventory using NBT tags
				CraftInventoryPlayer pi = Utils.loadOfflinePlayerInventory(playerName);
				clearInventory(pi);
				ItemStack[] contents = stats.getInventoryContents();
				if(contents != null)
					pi.setContents(contents);
				ItemStack[] armorContents = stats.getInventoryArmor();
				if(armorContents != null)
					pi.setArmorContents(armorContents);
				Utils.saveOfflinePlayerInventory(playerName, pi);
			}
			if(plugin.getConfig().getBoolean(ConfigEnum.SEPERATE_MONEY.toString()) && ZArena.getInstance().getEconomy() != null)
			{
				plugin.getEconomy().depositPlayer(playerName, stats.getOldMoney());
			}
			//Set location using NBT tags
			Location leaveLocation = getPlayersLeaveLocation(playerName);
			NBTTagList nbtLocation = new NBTTagList();
			nbtLocation.add(new NBTTagDouble(leaveLocation.getX()));
			nbtLocation.add(new NBTTagDouble(leaveLocation.getY()));
			nbtLocation.add(new NBTTagDouble(leaveLocation.getZ()));
			Utils.setOfflinePlayerTagValue(playerName, "Pos", nbtLocation);

			//Set gamemode and level using NBT tags
			Utils.setOfflinePlayerTagValue(playerName, "playerGameType", stats.getOldGameMode().getValue());
			Utils.setOfflinePlayerTagValue(playerName, "XpLevel", stats.getOldLevel());
		} catch(IOException e)
		{
			ZArena.log(Level.SEVERE, "Failed to properly remove "+playerName+" from the game. This player may have lost items, exp, and/or money.");
		} finally
		{
			players.remove(playerName);
			playerStats.remove(playerName);
			plugin.statsBackup.set(playerName, null);
			try
			{
				plugin.statsBackup.save(Constants.BACKUP_PATH);
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Removes a player from the game.
	 * @param player player to remove
	 */
	public void removePlayer(Player player)
	{
		if(players.contains(player.getName()))
		{
			PlayerStats stats = getPlayerStats(player);
			if(plugin.getConfig().getBoolean(ConfigEnum.SEPERATE_INVENTORY.toString()))
			{
				clearInventory(player.getInventory());
				PlayerInventory pi = player.getInventory();
				ItemStack[] contents = stats.getInventoryContents();
				if(contents != null)
					pi.setContents(contents);
				ItemStack[] armorContents = stats.getInventoryArmor();
				if(armorContents != null)
					pi.setArmorContents(armorContents);
			}
			if(plugin.getConfig().getBoolean(ConfigEnum.SEPERATE_MONEY.toString()) && ZArena.getInstance().getEconomy() != null)
			{
				plugin.getEconomy().depositPlayer(player.getName(), stats.getOldMoney());
			}
			player.teleport(getPlayersLeaveLocation(player.getName()));
			player.setGameMode(stats.getOldGameMode());
			player.setLevel(stats.getOldLevel());

			players.remove(player.getName());
			playerStats.remove(player.getName());
			plugin.statsBackup.set(player.getName(), null);
			try
			{
				plugin.statsBackup.save(Constants.BACKUP_PATH);
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void respawnPlayer(Player player, List<ItemStack> startItems)
	{
		if(!getPlayerStats(player).isAlive())
		{
			getPlayerStats(player).setAlive(true);
			player.teleport(level.getInitialSpawn());
			player.setHealth(player.getMaxHealth());
			((CraftPlayer) player).getHandle().triggerHealthUpdate();
			player.setFoodLevel(20);
			player.setSaturation(20);
			for(PotionEffect effect : player.getActivePotionEffects())
			{
				player.removePotionEffect(effect.getType());
			}
			if(plugin.getConfig().getBoolean(ConfigEnum.SEPERATE_INVENTORY.toString()))
			{
				PlayerInventory pi = player.getInventory();
				clearInventory(pi);
				for(ItemStack item : startItems)
					pi.addItem(item);
			}
		}
	}

	public void saveLevelHandler(boolean backup)
	{
		File path = new File(Constants.LEVEL_PATH);
		File[] backups = new File[5];
		backups[0] = path;

		for(int i = 1; i < 5; i++)
		{
			backups[i] = new File(Constants.PLUGIN_FOLDER+File.separator+"levelsbackup"+ i +".ext");
		}

		try
		{
			if(backup)
			{
				//If the two files are equal, there's no need for a backup
				if(!Utils.fileEquals(backups[0], backups[1]))
				{
					for(int i = 4; i > 0; i--)
					{
						if(backups[i - 1].exists())
							Files.move(backups[i - 1], backups[i]);
					}
				}
			}

			FileOutputStream fos = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

            levelHandler.writeExternal(oos);

            oos.close();
            fos.close();

        } catch (IOException e)
        {
        	ZArena.log(Level.WARNING, "ZArena: Error saving the LevelHandler database.");
        }
	}

	public void setGameMode(Gamemode gameMode)
	{
		this.gamemode = gameMode;
	}

	public void setLevel(ZLevel level)
	{
		this.level = level;
	}

	/**
	 * Starts the game
	 */
	public void start()
	{
		if(isRunning || isVoting)
			return;
		if(players.isEmpty())
		{
			isWaiting = plugin.getConfig().getBoolean(ConfigEnum.AUTOSTART.toString());
			return;
		}
		if(level == null)
			levelVoter.start();
		else
		{
			for(PlayerStats stats : playerStats.values())
			{
				PlayerRespawnInGameEvent event = new PlayerRespawnInGameEvent(stats.getPlayer(), getStartItems(), PlayerRespawnCause.GAME_START);
				Bukkit.getPluginManager().callEvent(event);
				if(!event.isCancelled())
					addToGame(stats, event.getStartItems());
			}

			isRunning = true;
			waveHandler.start();
		}
	}

	public void setDefaultGamemode(Gamemode gamemode)
	{
		defaultGamemode = gamemode;
		if(this.gamemode == null)
			this.gamemode = gamemode;
	}

	/**
	 * Stops the game
	 */
	public void stop()
	{
		if(!isRunning && !isVoting)
			return;
		if(isRunning)
			waveHandler.stop();
		if(isVoting)
			levelVoter.stop();
		isRunning = false;
		isWaiting = false;
		isVoting = false;
		if(level != null)
		{
			level.resetSigns();
			level.resetInactiveZSpawns();
			for(Entity entity : plugin.getServer().getWorld(level.getWorld()).getEntities())
			{
				if(CustomEntityWrapper.instanceOf(entity))
					((LivingEntity) entity).setHealth(0);
				else if(!(entity instanceof Player) && plugin.getConfig().getBoolean(ConfigEnum.WORLD_EXCLUSIVE.toString()))
					entity.remove();
			}
		}
		for(PlayerStats stats : playerStats.values())
		{
			stats.resetStats();
			stats.setAlive(false);

			Player player = stats.getPlayer();
			if(player != null)
			{
				if(plugin.getConfig().getBoolean(ConfigEnum.SEPERATE_INVENTORY.toString())
						&& !plugin.getConfig().getBoolean(ConfigEnum.KEEP_ITEMS_ACROSS_GAMES.toString()))
					clearInventory(player.getInventory());
				for(PotionEffect effect : player.getActivePotionEffects())
				{
					player.removePotionEffect(effect.getType());
				}
			}
		}
		if(gamemode.isApocalypse())
			ChatHelper.broadcastMessage(Message.GAME_END_APOCALYPSE_MESSAGE.formatMessage(waveHandler.getGameLength()), getBroadcastPlayers());
		else
			ChatHelper.broadcastMessage(Message.GAME_END_MESSAGE.formatMessage(waveHandler.getWave()), getBroadcastPlayers());
	}
}