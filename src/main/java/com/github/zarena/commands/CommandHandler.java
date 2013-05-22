package com.github.zarena.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;



import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.zarena.GameHandler;
import com.github.zarena.Gamemode;
import com.github.zarena.PlayerStats;
import com.github.zarena.WaveHandler;
import com.github.zarena.ZArena;
import com.github.zarena.ZLevel;
import com.github.zarena.events.GameStartCause;
import com.github.zarena.events.GameStartEvent;
import com.github.zarena.events.GameStopCause;
import com.github.zarena.events.GameStopEvent;
import com.github.zarena.events.LevelChangeCause;
import com.github.zarena.events.LevelChangeEvent;
import com.github.zarena.signs.ZTollSign;
import com.github.zarena.spout.PlayerOptions;
import com.github.zarena.spout.SpoutHandler;
import com.github.zarena.utils.ChatHelper;
import com.github.zarena.utils.Constants;
import com.github.zarena.utils.LocationSer;
import com.github.zarena.utils.Message;
import com.github.zarena.utils.StringEnums;

public class CommandHandler
{
	private ZArena plugin;
	private GameHandler gameHandler;
	private CommandSenderWrapper senderWrapper;
	private ECommand command;
	
	public CommandHandler(CommandSender sender, ECommand command)
	{
		plugin = ZArena.getInstance();
		gameHandler = plugin.getGameHandler();
		
		senderWrapper = new CommandSenderWrapper(sender);
		this.command = command;
	}

	public void createLevel(String levelName)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		if(gameHandler.isRunning() || gameHandler.isVoting())
		{
			senderWrapper.sendMessage(Message.NOT_ALLOWED_WHILE_RUNNING.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = new ZLevel(levelName, player.getLocation());
		gameHandler.getLevelHandler().addLevel(level);
		gameHandler.setLevel(level);
		senderWrapper.sendMessage(Message.CREATED_NEW_LEVEL.formatMessage("levelName"));
	}
	
	public void getGameMode()
	{
		Gamemode gm = gameHandler.getGameMode();
		if(gm == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		senderWrapper.sendMessage(Message.CURRENT_GAMEMODE.formatMessage(gm.toString()));
	}

	public void joinGame()
	{
		if(!senderWrapper.canEnterLeaveGames())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		if(plugin.getConfig().getBoolean(Constants.DISABLE_JOIN_WITH_INV))
		{
			boolean isClear = true;
			for(ItemStack item : player.getInventory().getContents())
			{
				if(item != null)
					isClear =  false;
			}
			if(!isClear)
			{
				senderWrapper.sendMessage(Message.INVENTORY_MUST_BE_CLEAR.formatMessage());
				return;
			}
		}
		gameHandler.addPlayer(player);
	}

	public void jumpToDSpawn()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		player.teleport(level.getDeathSpawn());
	}
	
	public void jumpToISpawn()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		player.teleport(level.getInitialSpawn());
	}

	public void jumpToZSpawn(String spawn)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		Location zSpawn = null;
		String zSpawnName = null;
		if(spawn.equalsIgnoreCase("%nearest%"))
		{
			zSpawnName = level.getNearestZombieSpawn(player.getLocation());
			zSpawn = level.getZombieSpawn(zSpawnName);
		}
		else
		{
			zSpawnName = spawn;
			zSpawn = level.getZombieSpawn(spawn);
		}
		if(zSpawn == null)
		{
			senderWrapper.sendMessage(Message.ZSPAWN_NOT_FOUND.formatMessage());
			return;
		}
		player.teleport(zSpawn);
		player.sendMessage(Message.JUMPED_TO_ZSPAWN.formatMessage(zSpawnName));
	}

	public void listLevels()
	{
		senderWrapper.sendMessage(ChatColor.RED+"Level List:");
		String levels = "";
		for(ZLevel level : gameHandler.getLevelHandler().getLevels())
		{
			levels = levels + ChatColor.BLUE + level.getName() + ChatColor.RED + ", ";
		}
		senderWrapper.sendMessage(levels);
	}
	
	public void leaveGame()
	{
		if(!senderWrapper.canEnterLeaveGames())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		gameHandler.removePlayer(player);
		senderWrapper.sendMessage(Message.LEAVE_GAME.formatMessage());
	}
	
	public void listAlive()
	{
		senderWrapper.sendMessage(Message.PLAYERS_ALIVE_HEADER.formatMessage());
		String totalMessage = "";
		for(PlayerStats stats : gameHandler.getPlayerStats().values())
		{
			if(stats.isAlive())
				totalMessage += Message.PLAYERS_ALIVE_ITEM.formatMessage(stats.getPlayer().getName());
		}
		totalMessage = totalMessage.substring(0, totalMessage.length() - 2);
		senderWrapper.sendMessage(totalMessage);
	}
	
	public void listSession()
	{
		senderWrapper.sendMessage(Message.PLAYERS_IN_SESSION_HEADER.formatMessage());
		String totalMessage = "";
		for(Player player : gameHandler.getPlayers())
			totalMessage += Message.PLAYERS_IN_SESSION_ITEM.formatMessage(player.getName());
		totalMessage = totalMessage.substring(0, totalMessage.length() - 2);
		senderWrapper.sendMessage(totalMessage);
	}
	
	public void listZSpawns()
	{
		if(!senderWrapper.canCreateLevels() && !senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		senderWrapper.sendMessage(Message.ZSPAWNS_LIST_HEADER.formatMessage());
		String totalMessage = "";
		for(String name : level.getZSpawnNames())
			totalMessage += Message.ZSPAWNS_LIST_ITEM.formatMessage(name);
		totalMessage = totalMessage.substring(0, totalMessage.length() - 2);
		senderWrapper.sendMessage(totalMessage);
	}

	public void loadLevel(String levelName)
	{
		if(!senderWrapper.canCreateLevels() && !senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		ZLevel level = gameHandler.getLevelHandler().getLevel(levelName);
		if(level == null)
		{
			senderWrapper.sendMessage(ChatColor.RED + "Level could not be found.");
			return;
		}
		
		LevelChangeEvent event = new LevelChangeEvent(gameHandler.getLevel(), level, LevelChangeCause.FORCE);
		Bukkit.getServer().getPluginManager().callEvent(event);
		gameHandler.setLevel(level);
		senderWrapper.sendMessage(ChatColor.GREEN+"Level sucessfuly loaded.");
	}
	
	public void markBossSpawn(String spawnName)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		if(level.removeBossSpawn(spawnName))
		{
			senderWrapper.sendMessage(Message.BOSS_SPAWN_UNSET.formatMessage());
			return;
		}
		if(level.addBossSpawn(spawnName))
			senderWrapper.sendMessage(Message.BOSS_SPAWN_SET.formatMessage());
		else
			senderWrapper.sendMessage(Message.BOSS_SPAWN_NOT_FOUND.formatMessage());
	}

	public void markSign(String signName, String[] flagArguments)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		ZTollSign sign = level.getZTollSign(signName);
		if(sign == null)
		{
			senderWrapper.sendMessage(Message.SIGN_NOT_FOUND.formatMessage());
			return;
		}
		for(String flag : flagArguments)
		{
			switch(StringEnums.valueOf(flag.toUpperCase()))
			{
			case UO: case USABLEONCE:
				if(sign.isUsableOnce())
				{
					sign.setUsableOnce(false);
					senderWrapper.sendMessage(Message.SIGN_UNMARKED_AS_USEABLE_ONCE.formatMessage());
					break;
				}
				sign.setUsableOnce(true);
				senderWrapper.sendMessage(Message.SIGN_MARKED_AS_USEABLE_ONCE.formatMessage());
				break;
			case OP: case OPPOSITE:
				if(sign.isOpposite())
				{
					sign.setOpposite(false);
					senderWrapper.sendMessage(Message.SIGN_UNMARKED_AS_OPPOSITE.formatMessage());
					break;
				}
				sign.setOpposite(true);
				senderWrapper.sendMessage(Message.SIGN_MARKED_AS_OPPOSITE.formatMessage());
			case NR: case NORESET:
				if(sign.isNoReset())
				{
					sign.setNoReset(false);
					senderWrapper.sendMessage(Message.SIGN_UNMARKED_AS_NON_RESETTING.formatMessage());
					break;
				}
				sign.setNoReset(true);
				senderWrapper.sendMessage(Message.SIGN_MARKED_AS_NON_RESETTING.formatMessage());
			default:
				senderWrapper.sendMessage(Message.FLAG_NOT_FOUND.formatMessage(flag));
			}
		}

	}
	
	public void markZSpawnToSign(String zSignName, String zSpawnerName)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		ZTollSign sign = level.getZTollSign(zSignName);
		if(sign == null)
		{
			senderWrapper.sendMessage(Message.SIGN_NOT_FOUND.formatMessage());
			return;
		}
		Location zSpawn = level.getZombieSpawn(zSpawnerName);
		LocationSer zSpawnSer = LocationSer.convertFromBukkitLocation(zSpawn);
		if(zSpawn == null)
		{
			senderWrapper.sendMessage(Message.ZSPAWN_NOT_FOUND.formatMessage());
			return;
		}
		if(sign.zSpawns.contains(zSpawnSer))
		{
			sign.zSpawns.remove(zSpawnSer);
			level.resetInactiveZSpawns();
			senderWrapper.sendMessage(Message.ZSPAWN_UNMARKED_AS_ACTIVE_WHEN_SIGN_ACTIVE.formatMessage());
			return;
		}
		sign.zSpawns.add(zSpawnSer);
		level.resetInactiveZSpawns();
		senderWrapper.sendMessage(Message.ZSPAWN_MARKED_AS_ACTIVE_WHEN_SIGN_ACTIVE.formatMessage());
	}
	
	public void openOptions()
	{
		if(plugin.isSpoutEnabled())
		{
			senderWrapper.sendMessage(Message.SPOUT_NOT_ENABLED.formatMessage());
			return;
		}
		if(!SpoutHandler.instanceofSpoutPlayer(senderWrapper.getPlayer()))
		{
			senderWrapper.sendMessage(Message.SPOUT_CLIENT_REQUIRED.formatMessage());
			return;
		}
		PlayerOptions options = plugin.getPlayerOptionsHandler().getOptions(senderWrapper.getPlayer().getName());
		options.openOptions();
	}
	
	public void reloadConfig()
	{
		if(!senderWrapper.isAdmin())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		senderWrapper.sendMessage(ChatColor.GREEN+"ZArena config reloaded.");
		ZArena.getInstance().reloadConfig();
		ZArena.getInstance().saveConfig();
	}
	
	public void reloadSigns()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		if(gameHandler.isRunning() || gameHandler.isVoting())
		{
			senderWrapper.sendMessage(Message.NOT_ALLOWED_WHILE_RUNNING.formatMessage());
			return;
		}
		if(command.getArgAtIndex(2).equalsIgnoreCase("all"))
		{
			for(ZLevel level : gameHandler.getLevelHandler().getLevels())
				level.reloadSigns();
		}
		else if(gameHandler.getLevel() != null)
			gameHandler.getLevel().reloadSigns();
		else
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
	}

	public void removeLevel(String levelName)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		if(gameHandler.isRunning() || gameHandler.isVoting())
		{
			senderWrapper.sendMessage(Message.NOT_ALLOWED_WHILE_RUNNING.formatMessage());
			return;
		}
		ZLevel level = gameHandler.getLevelHandler().getLevel(levelName);
		if(level == null)
		{
			senderWrapper.sendMessage(Message.LEVEL_NOT_FOUND.formatMessage(levelName));
			return;
		}
		gameHandler.getLevelHandler().removeLevel(level);
		if(level == gameHandler.getLevel())
			gameHandler.setLevel(null);
		senderWrapper.sendMessage(Message.LEVEL_REMOVED.formatMessage());
	}

	public void removeZSpawn(String spawn)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		boolean success;
		if(spawn.equalsIgnoreCase("%nearest%"))
			success = level.removeZombieSpawn(level.getNearestZombieSpawn(player.getLocation()));
		else
			success = level.removeZombieSpawn(spawn);
		
		if(success)
			senderWrapper.sendMessage(Message.ZSPAWN_REMOVED.formatMessage());
		else
			senderWrapper.sendMessage(Message.ZSPAWN_NOT_FOUND.formatMessage());
	}

	public void saveLevels()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		gameHandler.saveLevelHandler(false);
		senderWrapper.sendMessage(ChatColor.GREEN + "Levels saved successfully.");
	}
	
	public void sendInfo(String info) throws ArgumentCountException
	{
		if(!gameHandler.isRunning() && !gameHandler.isVoting())
		{
			senderWrapper.sendMessage(Message.GAME_MUST_BE_RUNNING.formatMessage());
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		WaveHandler waveHandler = gameHandler.getWaveHandler();
		int wave = (gameHandler.getGameMode().isApocalypse()) ? waveHandler.getApocalypseWave() : waveHandler.getWave();
		switch(StringEnums.valueOf(info.toUpperCase()))
		{
		case GENERAL:
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.wave, wave));
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.aliveCount, gameHandler.getAliveCount()));
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.remainingZombies, waveHandler.getRemainingZombies()));
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.level, gameHandler.getLevel()));
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.gamemode, gameHandler.getGameMode()));
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.giantsEnabled, gameHandler.getLevel().getRandomBossSpawn() != null));
			break;
		case ZOMBIESPERWAVE:
			String amounts = "";
			for(int checkWave = 1; checkWave <= 20; checkWave++)
			{
				int toSpawn = waveHandler.calcFunction(plugin.getConfig().getString(Constants.ZOMBIE_QUANTITY_FORMULA), checkWave, 
						plugin.getConfig().getInt(Constants.ZOMBIE_QUANTITY_LIMIT), plugin.getConfig().getDoubleList(Constants.ZOMBIE_QUANTITY_COEFFICIENTS));
				amounts += toSpawn;
			}
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.zombieAmountsOnTheFirst20Waves, amounts));
			break;
		case HEALTHPERWAVE:
			String healthAmounts = "";
			for(int checkWave = 1; checkWave <= 20; checkWave++)
			{
				int health = waveHandler.calcFunction(plugin.getConfig().getString(Constants.ZOMBIE_HEALTH_FORMULA), checkWave, 
						plugin.getConfig().getInt(Constants.ZOMBIE_HEALTH_LIMIT), plugin.getConfig().getDoubleList(Constants.ZOMBIE_HEALTH_COEFFICIENTS));
				healthAmounts += health;
			}
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.healthOfNormalZombiesOnTheFirst20Waves, healthAmounts));
			break;
		case WAVE:
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.wave, wave));
			break;
		case SPAWNCHANCE:
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.spawnChance, waveHandler.getSpawnChance()));
			break;
		case CHECKNEXTWAVE:
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.toSpawn, waveHandler.getRemainingZombies() - waveHandler.getEntites().size()));
			senderWrapper.sendMessage(Message.INFO_ITEM.formatMessage(ChatHelper.alive, waveHandler.getEntites().size()));
			break;
		default:
			throw new ArgumentCountException(2);
		}
	}
	
	public void setAlive(String playerName, String aliveDead)
	{
		if(!senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		if(!gameHandler.isRunning() && !gameHandler.isVoting())
		{
			senderWrapper.sendMessage(Message.GAME_MUST_BE_RUNNING.formatMessage());
			return;
		}
		Player player = plugin.getServer().getPlayer(playerName);
		if(player == null)
		{
			senderWrapper.sendMessage(Message.PLAYER_NOT_FOUND.formatMessage());
			return;
		}
		PlayerStats stats = gameHandler.getPlayerStats().get(player.getName());
		if(stats == null)
		{
			senderWrapper.sendMessage(Message.PLAYER_NOT_IN_GAME.formatMessage());
			return;
		}
		if(aliveDead.equalsIgnoreCase("true"))
			stats.setAlive(true);
		else if(aliveDead.equalsIgnoreCase("false"))
			stats.setAlive(false);
		else
		{
			senderWrapper.sendMessage(Message.ARG4_MUST_BE_TRUE_OR_FALSE.formatMessage());
			return;
		}
		senderWrapper.sendMessage(Message.SET_PLAYERS_ALIVE_STATUS.formatMessage());
	}

	public void setDSpawn()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		level.setDeathSpawn((player.getLocation()));
		senderWrapper.sendMessage(Message.DSPAWN_SET.formatMessage());
	}
	
	public void setGameMode(String gamemodeName)
	{
		if(!senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		if(!gameHandler.isRunning() && !gameHandler.isVoting())
		{
			senderWrapper.sendMessage(Message.GAME_MUST_BE_RUNNING.formatMessage());
			return;
		}
		Gamemode gm = Gamemode.getGamemode(gamemodeName);
		if(gm == null)
		{
			senderWrapper.sendMessage(Message.GAMEMODE_NOT_FOUND.formatMessage());
			return;
		}
		gameHandler.setGameMode(gm);
	}

	public void setISpawn()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		level.setInitialSpawn(player.getLocation());
		senderWrapper.sendMessage(Message.ISPAWN_SET.formatMessage());
	}
	
	public void setLeaveLocation()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		List<Double> locXYZ = new ArrayList<Double>();
		plugin.getConfig().set(Constants.GAME_LEAVE_WORLD, player.getWorld().getName());
		locXYZ.add(player.getLocation().getX());
		locXYZ.add(player.getLocation().getY());
		locXYZ.add(player.getLocation().getZ());
		plugin.getConfig().set(Constants.GAME_LEAVE_LOCATION, locXYZ);
		senderWrapper.sendMessage(Message.LEAVE_LOCATION_SET.formatMessage());
	}

	public void setWave(String waveString)
	{
		if(!senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		if(!gameHandler.isRunning() && !gameHandler.isVoting())
		{
			senderWrapper.sendMessage(Message.GAME_MUST_BE_RUNNING.formatMessage());
			return;
		}
		int wave;
		try
		{
			wave = Integer.parseInt(waveString);
		} catch(NumberFormatException e)
		{
			senderWrapper.sendMessage(Message.WAVE_MUST_BE_INTEGER.formatMessage());
			return;
		}
		gameHandler.getWaveHandler().setWave(wave);
		senderWrapper.sendMessage(Message.WAVE_SET.formatMessage());
	}

	public void setZSpawn(String spawnName)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(Message.NO_LEVEL_LOADED.formatMessage());
			return;
		}
		level.addZombieSpawn(spawnName, player.getLocation());
		senderWrapper.sendMessage(Message.ZSPAWN_SET.formatMessage(spawnName));
	}

	public void showGameStats()
	{
		if(!gameHandler.isRunning() && !gameHandler.isVoting())
		{
			senderWrapper.sendMessage(Message.GAME_MUST_BE_RUNNING.formatMessage());
			return;
		}
		senderWrapper.sendMessage(Message.GAME_STATS_HEADER.formatMessage());
		String statsString = "";
		for(Entry<String, PlayerStats> entry : gameHandler.getPlayerStats().entrySet())
		{
			Player player = entry.getValue().getPlayer();
			PlayerStats stats = entry.getValue();
			statsString += Message.GAME_STATS_ITEM.formatMessage(player.getName(), stats.getMoney(), stats.getPoints());
		}
		senderWrapper.sendMessage(statsString);
	}

	public void startGame()
	{
		if(!senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		if(gameHandler.isRunning() || gameHandler.isVoting())
		{
			senderWrapper.sendMessage(Message.GAME_ALREADY_RUNNING.formatMessage());
			return;
		}
		GameStartEvent event = new GameStartEvent(GameStartCause.FORCE);
		Bukkit.getServer().getPluginManager().callEvent(event);
		gameHandler.start();
		senderWrapper.sendMessage(Message.GAME_STARTED.formatMessage());
	}

	public void stopGame()
	{
		if(!senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		GameStopEvent event = new GameStopEvent(GameStopCause.FORCE);
		Bukkit.getServer().getPluginManager().callEvent(event);
		gameHandler.stop();
		senderWrapper.sendMessage(Message.GAME_STOPPED.formatMessage());
	}

	public void vote(String vote)
	{
		if(!senderWrapper.canVote())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		Player player = senderWrapper.getPlayer();
		if(!gameHandler.isVoting())
		{
			senderWrapper.sendMessage(Message.VOTING_NOT_TAKING_PLACE.formatMessage());
			return;
		}
		if(!gameHandler.getPlayers().contains(player))
		{
			senderWrapper.sendMessage(Message.MUST_BE_INGAME_TO_VOTE.formatMessage());
			return;
		}
		int voteNum;
		try
		{
			voteNum = Integer.parseInt(vote);
		} catch(NumberFormatException e)
		{
			senderWrapper.sendMessage(Message.VOTE_MUST_BE_VALID_INTEGER.formatMessage());
			return;
		}
		gameHandler.getLevelVoter().castVote(voteNum, player);
	}
}