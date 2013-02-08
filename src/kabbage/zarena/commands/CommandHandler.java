package kabbage.zarena.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import kabbage.zarena.GameHandler;
import kabbage.zarena.Gamemode;
import kabbage.zarena.PlayerStats;
import kabbage.zarena.WaveHandler;
import kabbage.zarena.ZArena;
import kabbage.zarena.ZLevel;
import kabbage.zarena.commands.utils.ArgumentCountException;
import kabbage.zarena.commands.utils.CommandSenderWrapper;
import kabbage.zarena.commands.utils.ECommand;
import kabbage.zarena.events.GameStartCause;
import kabbage.zarena.events.GameStartEvent;
import kabbage.zarena.events.GameStopCause;
import kabbage.zarena.events.GameStopEvent;
import kabbage.zarena.events.LevelChangeCause;
import kabbage.zarena.events.LevelChangeEvent;
import kabbage.zarena.signs.ZTollSign;
import kabbage.zarena.spout.PlayerOptions;
import kabbage.zarena.spout.SpoutHandler;
import kabbage.zarena.utils.ChatHelper;
import kabbage.zarena.utils.Constants;
import kabbage.zarena.utils.LocationSer;
import kabbage.zarena.utils.StringEnums;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		if(gameHandler.isRunning() || gameHandler.isVoting())
		{
			senderWrapper.sendMessage(ChatHelper.NOT_ALLOWED_WHILE_RUNNING);
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = new ZLevel(levelName, player.getLocation());
		gameHandler.getLevelHandler().addLevel(level);
		gameHandler.setLevel(level);
		senderWrapper.sendMessage(ChatColor.GREEN+"Sucessfully created the new level "+ChatColor.BLUE+levelName);
	}
	
	public void getGameMode()
	{
		Gamemode gm = gameHandler.getGameMode();
		if(gm == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		senderWrapper.sendMessage(ChatColor.BLUE+"Current GameMode: "+gm.toString());
	}

	public void joinGame()
	{
		if(!senderWrapper.canEnterLeaveGames())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
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
				senderWrapper.sendMessage(ChatColor.RED + "Your inventory must be clear to join the game.");
				return;
			}
		}
		gameHandler.addPlayer(player);
	}

	public void jumpToDSpawn()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		player.teleport(level.getDeathSpawn());
	}
	
	public void jumpToISpawn()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		player.teleport(level.getInitialSpawn());
	}

	public void jumpToZSpawn(String spawn)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
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
			senderWrapper.sendMessage(ChatColor.RED + "ZSpawn could not be found.");
			return;
		}
		player.teleport(zSpawn);
		player.sendMessage(ChatColor.GREEN+"Jumped to zombie spawn: "+ChatColor.RED+zSpawnName);
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
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		Player player = senderWrapper.getPlayer();
		gameHandler.removePlayer(player);
		senderWrapper.sendMessage(ChatColor.GREEN+"Successfully left the ZArena game.");
	}
	
	public void listAlive()
	{
		senderWrapper.sendMessage(ChatColor.BLUE + "Players alive:");
		for(PlayerStats stats : gameHandler.getPlayerStats().values())
		{
			if(stats.isAlive())
				senderWrapper.sendMessage(ChatColor.GREEN + stats.getPlayer().getName());
		}
	}
	
	public void listSession()
	{
		senderWrapper.sendMessage(ChatColor.BLUE + "Players in game session:");
		for(Player player : gameHandler.getPlayers())
			senderWrapper.sendMessage(ChatColor.GREEN + player.getName());
	}
	
	public void listZSpawns()
	{
		if(!senderWrapper.canCreateLevels() && !senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		senderWrapper.sendMessage(ChatColor.GOLD+"ZSpawns:");
		String zSpawnsString = "";
		for(String name : level.getZSpawnNames())
		{
			zSpawnsString = zSpawnsString + ChatColor.RED+name+", ";
		}
		senderWrapper.sendMessage(zSpawnsString);
		
	}

	public void loadLevel(String levelName)
	{
		if(!senderWrapper.canCreateLevels() && !senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
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
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		if(level.removeBossSpawn(spawnName))
		{
			senderWrapper.sendMessage(ChatColor.GREEN+"Boss spawn unset.");
			return;
		}
		if(level.addBossSpawn(spawnName))
			senderWrapper.sendMessage(ChatColor.GREEN+"Boss spawn set.");
		else
			senderWrapper.sendMessage(ChatColor.RED+"Spawn could not be found. Boss spawn not set.");
	}

	public void markSign(String signName, String[] flagArguments)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		ZTollSign sign = level.getZTollSign(signName);
		if(sign == null)
		{
			senderWrapper.sendMessage(ChatColor.RED+"Sign could not be found.");
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
					senderWrapper.sendMessage(ChatColor.GREEN+"Sign successfully unmarked as usable once.");
					break;
				}
				sign.setUsableOnce(true);
				senderWrapper.sendMessage(ChatColor.GREEN+"Sign successfully marked as usable once.");
				break;
			case OP: case OPPOSITE:
				if(sign.isOpposite())
				{
					sign.setOpposite(false);
					senderWrapper.sendMessage(ChatColor.GREEN+"Sign successfully unmarked as opposite.");
					break;
				}
				sign.setOpposite(true);
				senderWrapper.sendMessage(ChatColor.GREEN+"Sign successfully marked as opposite.");
			case NR: case NORESET:
				if(sign.isNoReset())
				{
					sign.setNoReset(false);
					senderWrapper.sendMessage(ChatColor.GREEN+"Sign successfully unmarked as non resetting.");
					break;
				}
				sign.setNoReset(true);
				senderWrapper.sendMessage(ChatColor.GREEN+"Sign successfully marked as non resetting.");
			default:
				senderWrapper.sendMessage(ChatColor.RED+"Flag by the name of '-"+flag+"' not found. Skipping.");
			}
		}

	}
	
	public void markZSpawnToSign(String zSignName, String zSpawnerName)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		ZTollSign sign = level.getZTollSign(zSignName);
		if(sign == null)
		{
			senderWrapper.sendMessage(ChatColor.RED+"Sign could not be found.");
			return;
		}
		Location zSpawn = level.getZombieSpawn(zSpawnerName);
		LocationSer zSpawnSer = LocationSer.convertFromBukkitLocation(zSpawn);
		if(zSpawn == null)
		{
			senderWrapper.sendMessage(ChatColor.RED+"Spawn could not be found.");
			return;
		}
		if(sign.zSpawns.contains(zSpawnSer))
		{
			sign.zSpawns.remove(zSpawnSer);
			level.resetInactiveZSpawns();
			senderWrapper.sendMessage(ChatColor.GREEN+"ZSpawn successfully unmarked as only active when this sign is active.");
			return;
		}
		sign.zSpawns.add(zSpawnSer);
		level.resetInactiveZSpawns();
		senderWrapper.sendMessage(ChatColor.GREEN+"ZSpawn successfully marked as only active when this sign is active.");
	}
	
	public void openOptions()
	{
		if(plugin.isSpoutEnabled())
		{
			senderWrapper.sendMessage(ChatColor.RED+"Spout not enabled on this server.");
			return;
		}
		if(!SpoutHandler.instanceofSpoutPlayer(senderWrapper.getPlayer()))
		{
			senderWrapper.sendMessage(ChatColor.RED+"You must have the spout client to use this.");
			return;
		}
		PlayerOptions options = plugin.getPlayerOptionsHandler().getOptions(senderWrapper.getPlayer().getName());
		options.openOptions();
	}
	
	public void reloadConfig()
	{
		if(!senderWrapper.isAdmin())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
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
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		if(gameHandler.isRunning() || gameHandler.isVoting())
		{
			senderWrapper.sendMessage(ChatHelper.NOT_ALLOWED_WHILE_RUNNING);
			return;
		}
		if(command.getArgAtIndex(2).equalsIgnoreCase("all"))
		{
			for(ZLevel level : gameHandler.getLevelHandler().getLevels())
			{
				level.reloadSigns();
			}
		}
		else if(gameHandler.getLevel() != null)
			gameHandler.getLevel().reloadSigns();
		else
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
	}

	public void removeLevel(String levelName)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		if(gameHandler.isRunning() || gameHandler.isVoting())
		{
			senderWrapper.sendMessage(ChatHelper.NOT_ALLOWED_WHILE_RUNNING);
			return;
		}
		ZLevel level = gameHandler.getLevelHandler().getLevel(levelName);
		if(level == null)
		{
			senderWrapper.sendMessage(ChatColor.RED+"Level could not be found.");
			return;
		}
		gameHandler.getLevelHandler().removeLevel(level);
		if(level == gameHandler.getLevel())
			gameHandler.setLevel(null);
		senderWrapper.sendMessage(ChatColor.GREEN+"Level sucessfuly removed.");
	}

	public void removeZSpawn(String spawn)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		boolean success;
		if(spawn.equalsIgnoreCase("%nearest%"))
			success = level.removeZombieSpawn(level.getNearestZombieSpawn(player.getLocation()));
		else
			success = level.removeZombieSpawn(spawn);
		
		if(success)
			senderWrapper.sendMessage(ChatColor.GREEN+"ZSpawn sucessfuly removed.");
		else
			senderWrapper.sendMessage(ChatColor.RED+"ZSpawn could not be found.");
	}

	public void saveLevels()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		gameHandler.saveLevelHandler(false);
		senderWrapper.sendMessage(ChatColor.GREEN + "Levels saved successfully.");
	}
	
	public void sendInfo(String info) throws ArgumentCountException
	{
		if(!gameHandler.isRunning() && !gameHandler.isVoting())
		{
			senderWrapper.sendMessage(ChatHelper.GAME_MUST_BE_RUNNING);
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		WaveHandler waveHandler = gameHandler.getWaveHandler();
		int wave = (gameHandler.getGameMode().isApocalypse()) ? waveHandler.getApocalypseWave() : waveHandler.getWave();
		switch(StringEnums.valueOf(info.toUpperCase()))
		{
		case GENERAL:
			senderWrapper.sendMessage(ChatColor.GOLD+"Wave: "+wave);
			senderWrapper.sendMessage(ChatColor.GOLD+"Alive Count: "+gameHandler.getAliveCount());
			senderWrapper.sendMessage(ChatColor.GOLD+"Remaining Zombies: "+waveHandler.getRemainingZombies());
			senderWrapper.sendMessage(ChatColor.GOLD+"Level: "+gameHandler.getLevel());
			senderWrapper.sendMessage(ChatColor.GOLD+"Gamemode: "+gameHandler.getGameMode());
			senderWrapper.sendMessage(ChatColor.GOLD+"Giants Enabled: "+(gameHandler.getLevel().getRandomBossSpawn() != null));
			break;
		case ZOMBIESPERWAVE:
			senderWrapper.sendMessage(ChatColor.GOLD+"Health of normal zombies on the first 20 waves:");
			for(int checkWave = 1; checkWave <= 20; checkWave++)
			{
				int toSpawn = waveHandler.calcFunction(plugin.getConfig().getString(Constants.ZOMBIE_QUANTITY_FORMULA), checkWave, 
						plugin.getConfig().getInt(Constants.ZOMBIE_QUANTITY_LIMIT), plugin.getConfig().getDoubleList(Constants.ZOMBIE_QUANTITY_COEFFICIENTS));
				senderWrapper.sendMessage(ChatColor.BLUE +"Wave "+checkWave+": "+ChatColor.RED+toSpawn);
			}
			break;
		case HEALTHPERWAVE:
			senderWrapper.sendMessage(ChatColor.GOLD+"Zombie amounts on the first 20 waves:");
			for(int checkWave = 1; checkWave <= 20; checkWave++)
			{
				int health = waveHandler.calcFunction(plugin.getConfig().getString(Constants.ZOMBIE_HEALTH_FORMULA), checkWave, 
						plugin.getConfig().getInt(Constants.ZOMBIE_HEALTH_LIMIT), plugin.getConfig().getDoubleList(Constants.ZOMBIE_HEALTH_COEFFICIENTS));
				senderWrapper.sendMessage(ChatColor.BLUE +"Health "+checkWave+": "+ChatColor.RED+health);
			}
			break;
		case WAVE:
			senderWrapper.sendMessage(ChatColor.GOLD+"Wave: "+wave);
			break;
		case SPAWNCHANCE:
			senderWrapper.sendMessage(ChatColor.GOLD+"Spawn Chance: "+waveHandler.getSpawnChance());
			break;
		case CHECKNEXTWAVE:
			senderWrapper.sendMessage(ChatColor.GOLD+"To Spawn: "+(waveHandler.getRemainingZombies() - waveHandler.getEntites().size()));
			senderWrapper.sendMessage(ChatColor.GOLD+"Alive: "+waveHandler.getEntites().size());
			senderWrapper.sendMessage(ChatColor.GOLD+"");
			break;
		default:
			throw new ArgumentCountException(2);
		}
	}
	
	public void setAlive(String playerName, String aliveDead)
	{
		if(!senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		if(!gameHandler.isRunning() && !gameHandler.isVoting())
		{
			senderWrapper.sendMessage(ChatHelper.GAME_MUST_BE_RUNNING);
			return;
		}
		Player player = plugin.getServer().getPlayer(playerName);
		if(player == null)
		{
			senderWrapper.sendMessage(ChatColor.RED+"Player could not be found.");
			return;
		}
		PlayerStats stats = gameHandler.getPlayerStats().get(player.getName());
		if(stats == null)
		{
			senderWrapper.sendMessage(ChatColor.RED+"Target player not in game.");
			return;
		}
		if(aliveDead.equalsIgnoreCase("true"))
			stats.setAlive(true);
		else if(aliveDead.equalsIgnoreCase("false"))
			stats.setAlive(false);
		else
		{
			senderWrapper.sendMessage(ChatColor.RED+"Argument 4 must be either true or false.");
			return;
		}
		senderWrapper.sendMessage(ChatColor.GREEN+"Successfully set player's alive status.");
	}

	public void setDSpawn()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		level.setDeathSpawn((player.getLocation()));
		senderWrapper.sendMessage(ChatColor.GREEN + "DSpawn set successfully.");
	}
	
	public void setGameMode(String gamemodeName)
	{
		if(!senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		if(!gameHandler.isRunning() && !gameHandler.isVoting())
		{
			senderWrapper.sendMessage(ChatHelper.GAME_MUST_BE_RUNNING);
			return;
		}
		Gamemode gm = Gamemode.getGamemode(gamemodeName);
		if(gm == null)
		{
			senderWrapper.sendMessage(ChatColor.RED + "Could not find a gamemode by that name.");
			return;
		}
		gameHandler.setGameMode(gm);
	}

	public void setISpawn()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		level.setInitialSpawn(player.getLocation());
		senderWrapper.sendMessage(ChatColor.GREEN + "ISpawn set successfully.");
	}
	
	public void setLeaveLocation()
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		Player player = senderWrapper.getPlayer();
		List<Double> locXYZ = new ArrayList<Double>();
		plugin.getConfig().set(Constants.GAME_LEAVE_WORLD, player.getWorld().getName());
		locXYZ.add(player.getLocation().getX());
		locXYZ.add(player.getLocation().getY());
		locXYZ.add(player.getLocation().getZ());
		plugin.getConfig().set(Constants.GAME_LEAVE_LOCATION, locXYZ);
		senderWrapper.sendMessage(ChatColor.GREEN + "Leave location set successfully.");
	}

	public void setWave(String waveString)
	{
		if(!senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		if(!gameHandler.isRunning() && !gameHandler.isVoting())
		{
			senderWrapper.sendMessage(ChatHelper.GAME_MUST_BE_RUNNING);
			return;
		}
		int wave;
		try
		{
			wave = Integer.parseInt(waveString);
		} catch(NumberFormatException e)
		{
			senderWrapper.sendMessage(ChatColor.RED+"Wave must be a valid integer.");
			return;
		}
		gameHandler.getWaveHandler().setWave(wave);
		senderWrapper.sendMessage(ChatColor.GREEN+"Wave successfully set.");
	}

	public void setZSpawn(String spawnName)
	{
		if(!senderWrapper.canCreateLevels())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		Player player = senderWrapper.getPlayer();
		ZLevel level = gameHandler.getLevel();
		if(level == null)
		{
			senderWrapper.sendMessage(ChatHelper.NO_LEVEL_LOADED);
			return;
		}
		level.addZombieSpawn(spawnName, player.getLocation());
		senderWrapper.sendMessage(ChatColor.GREEN + "ZSpawn "+ChatColor.BLUE+spawnName+ChatColor.GREEN+" set successfully.");
	}

	public void showGameStats()
	{
		if(!gameHandler.isRunning() && !gameHandler.isVoting())
		{
			senderWrapper.sendMessage(ChatHelper.GAME_MUST_BE_RUNNING);
			return;
		}
		senderWrapper.sendMessage(ChatColor.GOLD+"Game Stats:");
		String statsString = "";
		for(Entry<String, PlayerStats> entry : gameHandler.getPlayerStats().entrySet())
		{
			Player player = entry.getValue().getPlayer();
			PlayerStats stats = entry.getValue();
			statsString = statsString + ChatColor.RED+player.getName()+ChatColor.BLUE+" "+stats.getMoney()+" money "+stats.getPoints()+" points. ";
		}
		senderWrapper.sendMessage(statsString);
	}

	public void startGame()
	{
		if(!senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		if(gameHandler.isRunning() || gameHandler.isVoting())
		{
			senderWrapper.sendMessage(ChatColor.RED + "Game already running.");
			return;
		}
		GameStartEvent event = new GameStartEvent(GameStartCause.FORCE);
		Bukkit.getServer().getPluginManager().callEvent(event);
		gameHandler.start();
		senderWrapper.sendMessage(ChatColor.GREEN + "Game started.");
	}

	public void stopGame()
	{
		if(!senderWrapper.canControlGames())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		GameStopEvent event = new GameStopEvent(GameStopCause.FORCE);
		Bukkit.getServer().getPluginManager().callEvent(event);
		gameHandler.stop();
		senderWrapper.sendMessage(ChatColor.GREEN + "Game stopped.");
	}

	public void vote(String vote)
	{
		if(!senderWrapper.canVote())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		Player player = senderWrapper.getPlayer();
		if(!gameHandler.isVoting())
		{
			senderWrapper.sendMessage(ChatColor.RED+"Voting not currently taking place.");
			return;
		}
		if(!gameHandler.getPlayers().contains(player))
		{
			senderWrapper.sendMessage(ChatColor.RED+"You must be in game to vote.");
			return;
		}
		int voteNum;
		try
		{
			voteNum = Integer.parseInt(vote);
		} catch(NumberFormatException e)
		{
			senderWrapper.sendMessage(ChatColor.RED+"Vote must be a valid integer.");
			return;
		}
		gameHandler.getLevelVoter().castVote(voteNum, player);
	}
}