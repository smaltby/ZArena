package com.github.zarena.utils;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ChatHelper
{	
	public static String gameEndMessage;
	public static String gameEndApocalypseMessage;
	public static String voteStart;
	public static String voteOption;
	public static String voteEndsIn;
	public static String votesForEachMap;
	public static String mapChosen;
	public static String waveStartIn;
	public static String wolfWaveApproaching;
	public static String skeletonWaveApproaching;
	public static String waveStart;
	public static String noZombieSpawns;
	
	public static String bonusMoneyKill;
	public static String assistKill;
	public static String itemDrop;
	public static String signRemove;
	public static String noBuy;
	public static String extraCost;
	public static String insufficientFunds;
	public static String purchaseSuccessful;
	public static String signPlaceWithNoLevel;
	public static String signPlaceFailure;
	public static String signPlaceSuccess;
	public static String gameFull;
	public static String sendStats;
	
	public static String insufficientPermissions;
	public static String notAllowedWhileRunning;
	public static String createdNewLevel;
	public static String noLevelLoaded;
	public static String currentGamemode;
	public static String inventoryMustBeClear;
	public static String zSpawnNotFound;
	public static String jumpedToZSpawn;
	public static String levelListHeader;
	public static String levelListItem;
	public static String leaveGame;
	public static String playersAliveHeader;
	public static String playersAliveItem;
	public static String playersInSessionHeader;
	public static String playersInSessionItem;
	public static String zSpawnsListHeader;
	public static String zSpawnsListItem;
	public static String levelNotFound;
	public static String levelLoaded;
	public static String bossSpawnSet;
	public static String bossSpawnUnset;
	public static String bossSpawnNotFound;
	public static String signNotFound;
	public static String signMarkedAsUseableOnce;
	public static String signUnmarkedAsUseableOnce;
	public static String signMarkedAsOpposite;
	public static String signUnmarkedAsOpposite;
	public static String signMarkedAsNonResetting;
	public static String signUnmarkedAsNonResetting;
	public static String flagNotFound;
	public static String zSpawnMarkedAsActiveWhenSignActive;
	public static String zSpawnUnmarkedAsActiveWhenSignActive;
	public static String spoutNotEnabled;
	public static String spoutClientRequired;
	public static String configReloaded;
	public static String levelRemoved;
	public static String zSpawnRemoved;
	public static String levelsSaved;
	public static String gameMustBeRunning;
	public static String infoItem;
	public static String playerNotFound;
	public static String playerNotInGame;
	public static String arg4MustBeTrueOrFalse;
	public static String setPlayersAliveStatus;
	public static String dSpawnSet;
	public static String gamemodeNotFound;
	public static String iSpawnSet;
	public static String leaveLocationSet;
	public static String waveMustBeInteger;
	public static String waveSet;
	public static String zSpawnSet;
	public static String gameStatsHeader;
	public static String gameStatsItem;
	public static String gameAlreadyRunning;
	public static String gameStarted;
	public static String gameStopped;
	public static String votingNotTakingPlace;
	public static String mustBeIngameToVote;
	public static String voteMustBeValidInteger;
	public static String voteMustRangeFrom1To3;
	public static String voteSuccessful;
	public static String setKillsMustBeGreaterOrEqualTo0;
	public static String addKillsMustBeGreaterThan0;
	public static String subKillsMustBeGreaterThan0;
	public static String killsSet;
	public static String topKillersHeader;
	public static String topKillersItem;
	public static String playerKillsInfo;
	
	public static String wave;
	public static String aliveCount;
	public static String remainingZombies;
	public static String level;
	public static String gamemode;
	public static String giantsEnabled;
	public static String healthOfNormalZombiesOnTheFirst20Waves;
	public static String zombieAmountsOnTheFirst20Waves;
	public static String spawnChance;
	public static String toSpawn;
	public static String alive;
	
	public static void loadLanguageFile()
	{
		YamlConfiguration language = YamlConfiguration.loadConfiguration(new File(Constants.LANGUAGE_PATH));
		gameEndMessage = language.getString("Game End Message");
		gameEndApocalypseMessage = language.getString("Game End Apocalypse Message");
		voteStart = language.getString("Vote Start");
		voteOption = language.getString("Vote Option");
		voteEndsIn = language.getString("Vote Ends In");
		votesForEachMap = language.getString("Votes For Each Map");
		mapChosen = language.getString("Map Chosen");
		waveStartIn = language.getString("Wave Start In");
		wolfWaveApproaching = language.getString("Wolf Wave Approaching");
		skeletonWaveApproaching = language.getString("Skeleton Wave Approaching");
		waveStart = language.getString("Wave Start");
		noZombieSpawns = language.getString("No Zombie Spawns");
		
		bonusMoneyKill = language.getString("Bonus Money Kill");
		assistKill = language.getString("Assist Kill");
		itemDrop = language.getString("Item Drop");
		signRemove = language.getString("Sign Remove");
		noBuy = language.getString("No Buy");
		extraCost = language.getString("Extra Cost");
		insufficientFunds = language.getString("Insufficient Funds");
		purchaseSuccessful = language.getString("Purchase Successful");
		signPlaceWithNoLevel = language.getString("Sign Place With No Level");
		signPlaceFailure = language.getString("Sign Place Failure");
		signPlaceSuccess = language.getString("Sign Place Success");
		gameFull = language.getString("Game Full");
		sendStats = language.getString("Send Stats");
		
		insufficientPermissions = language.getString("Insufficient Permissions");
		notAllowedWhileRunning = language.getString("Not Allowed While Running");
		createdNewLevel = language.getString("Created New Level");
		noLevelLoaded = language.getString("No Level Loaded");
		currentGamemode = language.getString("Current Gamemode");
		inventoryMustBeClear = language.getString("Inventory Must Be Clear");
		zSpawnNotFound = language.getString("ZSpawn Not Found");
		jumpedToZSpawn = language.getString("Jumped To ZSpawn");
		levelListHeader = language.getString("Level List Header");
		levelListItem = language.getString("Level List Item");
		leaveGame = language.getString("Leave Game");
		playersAliveHeader = language.getString("Players Alive Header");
		playersAliveItem = language.getString("Players Alive Item");
		playersInSessionHeader = language.getString("Players In Session Header");
		playersInSessionItem = language.getString("Players In Session Item");
		zSpawnsListHeader = language.getString("ZSpawns List Header");
		zSpawnsListItem = language.getString("ZSpawns List Item");
		levelNotFound = language.getString("Level Not Found");
		levelLoaded = language.getString("Level Loaded");
		bossSpawnSet = language.getString("Boss Spawn Set");
		bossSpawnUnset = language.getString("Boss Spawn Unset");
		bossSpawnNotFound = language.getString("Boss Spawn Not Found");
		signNotFound = language.getString("Sign Not Found");
		signMarkedAsUseableOnce = language.getString("Sign Marked As Useable Once");
		signUnmarkedAsUseableOnce = language.getString("Sign Unmarked As Useable Once");
		signMarkedAsOpposite = language.getString("Sign Marked As Opposite");
		signUnmarkedAsOpposite = language.getString("Sign Unmarked As Opposite");
		signMarkedAsNonResetting = language.getString("Sign Marked As Non Resetting");
		signUnmarkedAsNonResetting = language.getString("Sign Unmarked As Non Resetting");
		flagNotFound = language.getString("Flag Not Found");
		zSpawnMarkedAsActiveWhenSignActive = language.getString("ZSpawn Marked As Active When Sign Active");
		zSpawnUnmarkedAsActiveWhenSignActive = language.getString("ZSpawn Unmarked As Active When Sign Active");
		spoutNotEnabled = language.getString("Spout Not Enabled");
		spoutClientRequired = language.getString("Spout Client Required");
		configReloaded = language.getString("Config Reloaded");
		levelRemoved = language.getString("Level Removed");
		zSpawnRemoved = language.getString("ZSpawn Removed");
		levelsSaved = language.getString("Levels Saved");
		gameMustBeRunning = language.getString("Game Must Be Running");
		infoItem = language.getString("Info Item");
		playerNotFound = language.getString("Player Not Found");
		playerNotInGame = language.getString("Player Not In Game");
		arg4MustBeTrueOrFalse = language.getString("Arg 4 Must Be True Or False");
		setPlayersAliveStatus = language.getString("Set Players Alive Status");
		dSpawnSet = language.getString("DSpawn Set");
		gamemodeNotFound = language.getString("Gamemode Not Found");
		iSpawnSet = language.getString("ISpawn Set");
		leaveLocationSet = language.getString("Leave Location Set");
		waveMustBeInteger = language.getString("Wave Must Be Integer");
		waveSet = language.getString("Wave Set");
		zSpawnSet = language.getString("ZSpawn Set");
		gameStatsHeader = language.getString("Game Stats Header");
		gameStatsItem = language.getString("Game Stats Item");
		gameAlreadyRunning = language.getString("Game Already Running");
		gameStarted = language.getString("Game Started");
		gameStopped = language.getString("Game Stopped");
		votingNotTakingPlace = language.getString("Voting Not Taking Place");
		mustBeIngameToVote = language.getString("Must Be Ingame To Vote");
		voteMustBeValidInteger = language.getString("Vote Must Be Valid Integer");
		voteMustRangeFrom1To3 = language.getString("Vote Must Range From 1 To 3");
		voteSuccessful = language.getString("Vote Successful");
		setKillsMustBeGreaterOrEqualTo0 = language.getString("Set Kills Must Be Greater Than Or Equal To 0");
		addKillsMustBeGreaterThan0 = language.getString("Add Kills Must Be Greater Than 0");
		subKillsMustBeGreaterThan0 = language.getString("Sub Kills Must Be Greater Than 0");
		killsSet = language.getString("Kills Set");
		topKillersHeader = language.getString("Top Killers Header");
		topKillersItem = language.getString("Top Killers Item");
		playerKillsInfo = language.getString("Player Kills Info");
		
		wave = language.getString("Wave");
		aliveCount = language.getString("Alive Count");
		remainingZombies = language.getString("Remaining Zombies");
		level = language.getString("Level");
		gamemode = language.getString("Gamemode");
		giantsEnabled = language.getString("Giants Enabled");
		healthOfNormalZombiesOnTheFirst20Waves = language.getString("Health of Normal Zombies on the First 20 Waves");
		zombieAmountsOnTheFirst20Waves = language.getString("Zombie Amounts on the First 20 Waves");
		spawnChance = language.getString("Spawn Chance");
		toSpawn = language.getString("To Spawn");
		alive = language.getString("Alive");
	}
	
	public static String broadcastMessage(String message, List<Player> players)
	{
		return broadcastMessage(new String[] {message}, players)[0];
	}
	
	public static String[] broadcastMessage(String[] message, List<Player> players)
	{
		return broadcastMessage(message, players.toArray(new Player[players.size()]));
	}
	
	public static String broadcastMessage(String message, Player[] players)
	{
		return broadcastMessage(new String[] {message}, players)[0];
	}
	
	public static String broadcastMessage(String message)
	{
		return broadcastMessage(new String[] {message})[0];
	}
	
	public static String[] broadcastMessage(String[] message)
	{
		return broadcastMessage(message, Bukkit.getServer().getOnlinePlayers());
	}
	
	public static String[] broadcastMessage(String[] message, Player[] players)
	{
		for(Player p : players)
		{
			for(String messagePart : message)
			{
				p.sendMessage(messagePart);
			}
		}
		for(String messagePart : message)
		{
			Bukkit.getConsoleSender().sendMessage(messagePart);
		}
		return message;
	}
}
