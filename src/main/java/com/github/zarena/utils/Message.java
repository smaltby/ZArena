package com.github.zarena.utils;

import java.util.logging.Level;

import org.bukkit.ChatColor;

import com.github.zarena.ZArena;

public enum Message
{
	GAME_END_MESSAGE("WAVE"),
	GAME_END_APOCALYPSE_MESSAGE("TIME"),
	VOTE_START(),
	VOTE_OPTION("NUM", "MAP", "GAMEMODE"),
	VOTE_ENDS_IN("TIME"),
	VOTES_FOR_EACH_MAP("NUM", "VOTES"),
	MAP_CHOSEN("MAP", "GAMEMODE"),
	WAVE_START_IN("WAVE", "TIME"),
	WOLF_WAVE_APPROACHING(),
	SKELETON_WAVE_APPROACHING(),
	WAVE_START("WAVE", "NUM", "HEALTH"),
	NO_ZOMBIE_SPAWNS(),
	
	BONUS_MONEY_KILL("MODIFIER", "MOB"),
	ASSIST_KILL("MODIFIER", "MOB"),
	ITEM_DROP(),
	SIGN_REMOVE(),
	NO_BUY(),
	EXTRA_COST("MODIFIER"),
	INSUFFICIENT_FUNDS(),
	PURCHASE_SUCCESSFUL(),
	SIGN_PLACE_WITH_NO_LEVEL(),
	SIGN_PLACE_FAILURE(),
	SIGN_PLACE_SUCCESS(),
	GAME_FULL(),
	SEND_STATS("MONEY", "POINTS"),
	
	INSUFFICIENT_PERMISSIONS(),
	NOT_ALLOWED_WHILE_RUNNING(),
	CREATED_NEW_LEVEL("LEVEL"),
	NO_LEVEL_LOADED(),
	CURRENT_GAMEMODE("GAMEMODE"),
	INVENTORY_MUST_BE_CLEAR(),
	ZSPAWN_NOT_FOUND(),
	JUMPED_TO_ZSPAWN("ZSPAWN"),
	LEVEL_LIST_HEADER(),
	LEVEL_LIST_ITEM("LEVEL"),
	LEAVE_GAME(),
	PLAYERS_ALIVE_HEADER(),
	PLAYERS_ALIVE_ITEM("PLAYER"),
	PLAYERS_IN_SESSION_HEADER(),
	PLAYERS_IN_SESSION_ITEM("PLAYER"),
	ZSPAWNS_LIST_HEADER(),
	ZSPAWNS_LIST_ITEM("ZSPAWN"),
	LEVEL_NOT_FOUND(),
	LEVEL_LOADED("LEVEL"),
	BOSS_SPAWN_SET(),
	BOSS_SPAWN_UNSET(),
	BOSS_SPAWN_NOT_FOUND(),
	SIGN_NOT_FOUND(),
	SIGN_MARKED_AS_USEABLE_ONCE(),
	SIGN_UNMARKED_AS_USEABLE_ONCE(),
	SIGN_MARKED_AS_OPPOSITE(),
	SIGN_UNMARKED_AS_OPPOSITE(),
	SIGN_MARKED_AS_NON_RESETTING(),
	SIGN_UNMARKED_AS_NON_RESETTING(),
	FLAG_NOT_FOUND("FLAG"),
	ZSPAWN_MARKED_AS_ACTIVE_WHEN_SIGN_ACTIVE(),
	ZSPAWN_UNMARKED_AS_ACTIVE_WHEN_SIGN_ACTIVE(),
	SPOUT_NOT_ENABLED(),
	SPOUT_CLIENT_REQUIRED(),
	CONFIG_RELOADED(),
	LEVEL_REMOVED("LEVEL"),
	ZSPAWN_REMOVED(),
	LEVELS_SAVED(),
	GAME_MUST_BE_RUNNING(),
	INFO_ITEM("NAME", "VALUE"),
	PLAYER_NOT_FOUND(),
	PLAYER_NOT_IN_GAME(),
	ARG4_MUST_BE_TRUE_OR_FALSE(),
	SET_PLAYERS_ALIVE_STATUS(),
	DSPAWN_SET(),
	GAMEMODE_NOT_FOUND(),
	ISPAWN_SET(),
	LEAVE_LOCATION_SET("LOCATION"),
	WAVE_MUST_BE_INTEGER(),
	WAVE_SET(),
	ZSPAWN_SET("ZSPAWN"),
	GAME_STATS_HEADER(),
	GAME_STATS_ITEM("MONEY", "POINTS"),
	GAME_ALREADY_RUNNING(),
	GAME_STARTED(),
	GAME_STOPPED(),
	VOTING_NOT_TAKING_PLACE(),
	MUST_BE_INGAME_TO_VOTE(),
	VOTE_MUST_BE_VALID_INTEGER(),
	VOTE_MUST_RANGE_FROM1_TO3(),
	VOTE_SUCCESSFUL(),
	SET_KILLS_MUST_BE_GREATER_OR_EQUAL_TO0(),
	ADD_KILLS_MUST_BE_GREATER_THAN0(),
	SUB_KILLS_MUST_BE_GREATER_THAN0(),
	KILLS_SET("PLAYER", "NUM"),
	TOP_KILLERS_HEADER(),
	TOP_KILLERS_ITEM("NUM", "PLAYER", "KILLS"),
	PLAYER_KILLS_INFO("PLAYER", "KILLS", "RANK", "TOTAL");
	
	String message = "";
	String[] params;
	
	Message(String... params)
	{
		this.params = params;
	}
	
	public String[] getParams()
	{
		return params;
	}
	
	/**
	 * Get the unformatted message. To get the formatted message, use the formatMessage method.
	 * @return	raw message
	 */
	public String getRawMessage()
	{
		return message;
	}
	
	/**
	 * Formats a message. The message should be gotten from one of the public static variables of the ChatHelper class.
	 * @param args		arguments to use in formatting. Size should be equal to the size of message.getParams(). Non-String arguments will be toString()ified
	 * @return			formatted message
	 */
	public String formatMessage(Object... args)
	{
		if(args.length != params.length)
			ZArena.log(Level.SEVERE, "Yo, the length of args and params don't match. What the fuck man, this isn't complicated. Look to the enum "+this+" and fix this shit.");
		String messageCopy = message;
	    for (ChatColor color : ChatColor.values())
	    	messageCopy = messageCopy.replaceAll("(?i)<" + color.name() + ">", "" + color);
	    for(int i = 0; i < params.length; i++)
	    	messageCopy = messageCopy.replaceAll(params[i], args[i].toString());
	    return messageCopy;
	}
	
	/**
	 * Sets the messages for each enumeration item. Needs to be done AFTER ChatHelper has loaded the language files.
	 */
	public static void setMessages()
	{
		GAME_END_MESSAGE.message = ChatHelper.gameEndMessage;
		GAME_END_APOCALYPSE_MESSAGE.message = ChatHelper.gameEndApocalypseMessage;
		VOTE_START.message = ChatHelper.voteStart;
		VOTE_OPTION.message = ChatHelper.voteOption;
		VOTE_ENDS_IN.message = ChatHelper.voteEndsIn;
		VOTES_FOR_EACH_MAP.message = ChatHelper.votesForEachMap;
		MAP_CHOSEN.message = ChatHelper.mapChosen;
		WAVE_START_IN.message = ChatHelper.waveStartIn;
		WOLF_WAVE_APPROACHING.message = ChatHelper.wolfWaveApproaching;
		SKELETON_WAVE_APPROACHING.message = ChatHelper.skeletonWaveApproaching;
		WAVE_START.message = ChatHelper.waveStart;
		NO_ZOMBIE_SPAWNS.message = ChatHelper.noZombieSpawns;
		BONUS_MONEY_KILL.message = ChatHelper.bonusMoneyKill;
		ASSIST_KILL.message = ChatHelper.assistKill;
		ITEM_DROP.message = ChatHelper.itemDrop;
		SIGN_REMOVE.message = ChatHelper.signRemove;
		NO_BUY.message = ChatHelper.noBuy;
		EXTRA_COST.message = ChatHelper.extraCost;
		INSUFFICIENT_FUNDS.message = ChatHelper.insufficientFunds;
		PURCHASE_SUCCESSFUL.message = ChatHelper.purchaseSuccessful;
		SIGN_PLACE_WITH_NO_LEVEL.message = ChatHelper.signPlaceWithNoLevel;
		SIGN_PLACE_FAILURE.message = ChatHelper.signPlaceFailure;
		SIGN_PLACE_SUCCESS.message = ChatHelper.signPlaceSuccess;
		GAME_FULL.message = ChatHelper.gameFull;
		SEND_STATS.message = ChatHelper.sendStats;
		INSUFFICIENT_PERMISSIONS.message = ChatHelper.insufficientPermissions;
		NOT_ALLOWED_WHILE_RUNNING.message = ChatHelper.notAllowedWhileRunning;
		CREATED_NEW_LEVEL.message = ChatHelper.createdNewLevel;
		NO_LEVEL_LOADED.message = ChatHelper.noLevelLoaded;
		CURRENT_GAMEMODE.message = ChatHelper.currentGamemode;
		INVENTORY_MUST_BE_CLEAR.message = ChatHelper.inventoryMustBeClear;
		ZSPAWN_NOT_FOUND.message = ChatHelper.zSpawnNotFound;
		JUMPED_TO_ZSPAWN.message = ChatHelper.jumpedToZSpawn;
		LEVEL_LIST_HEADER.message = ChatHelper.levelListHeader;
		LEVEL_LIST_ITEM.message = ChatHelper.levelListItem;
		LEAVE_GAME.message = ChatHelper.leaveGame;
		PLAYERS_ALIVE_HEADER.message = ChatHelper.playersAliveHeader;
		PLAYERS_ALIVE_ITEM.message = ChatHelper.playersAliveItem;
		PLAYERS_IN_SESSION_HEADER.message = ChatHelper.playersInSessionHeader;
		PLAYERS_IN_SESSION_ITEM.message = ChatHelper.playersInSessionItem;
		ZSPAWNS_LIST_HEADER.message = ChatHelper.zSpawnsListHeader;
		ZSPAWNS_LIST_ITEM.message = ChatHelper.zSpawnsListItem;
		LEVEL_NOT_FOUND.message = ChatHelper.levelNotFound;
		LEVEL_LOADED.message = ChatHelper.levelLoaded;
		BOSS_SPAWN_SET.message = ChatHelper.bossSpawnSet;
		BOSS_SPAWN_UNSET.message = ChatHelper.bossSpawnUnset;
		BOSS_SPAWN_NOT_FOUND.message = ChatHelper.bossSpawnNotFound;
		SIGN_NOT_FOUND.message = ChatHelper.signNotFound;
		SIGN_MARKED_AS_USEABLE_ONCE.message = ChatHelper.signMarkedAsUseableOnce;
		SIGN_UNMARKED_AS_USEABLE_ONCE.message = ChatHelper.signUnmarkedAsUseableOnce;
		SIGN_MARKED_AS_OPPOSITE.message = ChatHelper.signMarkedAsOpposite;
		SIGN_UNMARKED_AS_OPPOSITE.message = ChatHelper.signUnmarkedAsOpposite;
		SIGN_MARKED_AS_NON_RESETTING.message = ChatHelper.signMarkedAsNonResetting;
		SIGN_UNMARKED_AS_NON_RESETTING.message = ChatHelper.signUnmarkedAsNonResetting;
		FLAG_NOT_FOUND.message = ChatHelper.flagNotFound;
		ZSPAWN_MARKED_AS_ACTIVE_WHEN_SIGN_ACTIVE.message = ChatHelper.zSpawnMarkedAsActiveWhenSignActive;
		ZSPAWN_UNMARKED_AS_ACTIVE_WHEN_SIGN_ACTIVE.message = ChatHelper.zSpawnUnmarkedAsActiveWhenSignActive;
		SPOUT_NOT_ENABLED.message = ChatHelper.spoutNotEnabled;
		SPOUT_CLIENT_REQUIRED.message = ChatHelper.spoutClientRequired;
		CONFIG_RELOADED.message = ChatHelper.configReloaded;
		LEVEL_REMOVED.message = ChatHelper.levelRemoved;
		ZSPAWN_REMOVED.message = ChatHelper.zSpawnRemoved;
		LEVELS_SAVED.message = ChatHelper.levelsSaved;
		GAME_MUST_BE_RUNNING.message = ChatHelper.gameMustBeRunning;
		INFO_ITEM.message = ChatHelper.infoItem;
		PLAYER_NOT_FOUND.message = ChatHelper.playerNotFound;
		PLAYER_NOT_IN_GAME.message = ChatHelper.playerNotInGame;
		ARG4_MUST_BE_TRUE_OR_FALSE.message = ChatHelper.arg4MustBeTrueOrFalse;
		SET_PLAYERS_ALIVE_STATUS.message = ChatHelper.setPlayersAliveStatus;
		DSPAWN_SET.message = ChatHelper.dSpawnSet;
		GAMEMODE_NOT_FOUND.message = ChatHelper.gamemodeNotFound;
		ISPAWN_SET.message = ChatHelper.iSpawnSet;
		LEAVE_LOCATION_SET.message = ChatHelper.leaveLocationSet;
		WAVE_MUST_BE_INTEGER.message = ChatHelper.waveMustBeInteger;
		WAVE_SET.message = ChatHelper.waveSet;
		ZSPAWN_SET.message = ChatHelper.zSpawnSet;
		GAME_STATS_HEADER.message = ChatHelper.gameStatsHeader;
		GAME_STATS_ITEM.message = ChatHelper.gameStatsItem;
		GAME_ALREADY_RUNNING.message = ChatHelper.gameAlreadyRunning;
		GAME_STARTED.message = ChatHelper.gameStarted;
		GAME_STOPPED.message = ChatHelper.gameStopped;
		VOTING_NOT_TAKING_PLACE.message = ChatHelper.votingNotTakingPlace;
		MUST_BE_INGAME_TO_VOTE.message = ChatHelper.mustBeIngameToVote;
		VOTE_MUST_BE_VALID_INTEGER.message = ChatHelper.voteMustBeValidInteger;
		VOTE_MUST_RANGE_FROM1_TO3.message = ChatHelper.voteMustRangeFrom1To3;
		VOTE_SUCCESSFUL.message = ChatHelper.voteSuccessful;
		SET_KILLS_MUST_BE_GREATER_OR_EQUAL_TO0.message = ChatHelper.setKillsMustBeGreaterOrEqualTo0;
		ADD_KILLS_MUST_BE_GREATER_THAN0.message = ChatHelper.addKillsMustBeGreaterThan0;
		SUB_KILLS_MUST_BE_GREATER_THAN0.message = ChatHelper.subKillsMustBeGreaterThan0;
		KILLS_SET.message = ChatHelper.killsSet;
		TOP_KILLERS_HEADER.message = ChatHelper.topKillersHeader;
		TOP_KILLERS_ITEM.message = ChatHelper.topKillersItem;
		PLAYER_KILLS_INFO.message = ChatHelper.playerKillsInfo;
	}
}
