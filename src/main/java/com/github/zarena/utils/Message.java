package com.github.zarena.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;

import com.github.zarena.ZArena;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.Map;

public enum Message
{
	GAME_END_MESSAGE("Game End Message", "WAVE"),
	GAME_END_APOCALYPSE_MESSAGE("Game End Apocalypse Message", "TIME"),
	VOTE_START("Vote Start"),
	VOTE_OPTION("Vote Option", "NUM", "MAP", "GAMEMODE"),
	VOTE_ENDS_IN("Vote Ends In", "TIME"),
	VOTES_FOR_EACH_MAP("Votes For Each Map", "NUM", "VOTES"),
	MAP_CHOSEN("Map Chosen", "MAP", "GAMEMODE"),
	WAVE_START_IN("Wave Start In", "WAVE", "TIME"),
	WOLF_WAVE_APPROACHING("Wolf Wave Approaching"),
	SKELETON_WAVE_APPROACHING("Skeleton Wave Approaching"),
	WAVE_START("Wave Start", "WAVE", "NUM", "HEALTH"),
	NO_ZOMBIE_SPAWNS("No Zombie Spawns"),
    ON_PLAYER_DEATH_GLOBAL("On Player Death Global", "PLAYER", "ALIVECOUNT"),

	BONUS_MONEY_KILL("Bonus Money Kill", "MODIFIER", "MOB"),
	ASSIST_KILL("Assist Kill", "MODIFIER", "MOB"),
	ITEM_DROP("Item Drop"),
	SIGN_REMOVE("Sign Remove"),
	NO_BUY("No Buy"),
	EXTRA_COST("Extra Cost", "MODIFIER"),
	INSUFFICIENT_FUNDS("Insufficient Funds"),
	PURCHASE_SUCCESSFUL("Purchase Successful"),
	SIGN_PLACE_WITH_NO_LEVEL("Sign Place With No Level"),
	SIGN_PLACE_FAILURE("Sign Place Failure"),
	SIGN_PLACE_SUCCESS("Sign Place Success"),
	GAME_FULL("Game Full"),
	SEND_STATS("Send Stats", "MONEY", "POINTS"),
	RESPAWN_IN_TIME("Respawn In Time", "PLAYER", "TIME"),
	RESPAWN_IN_TIME_AFTER_JOIN("Respawn In Time After Join", "PLAYER", "TIME"),
	RESPAWN_IN_TIME_AFTER_DEATH("Respawn In Time After Death", "PLAYER", "TIME"),
	RESPAWN_IN_WAVES("Respawn In Waves", "PLAYER", "WAVES"),
	RESPAWN_IN_WAVES_AFTER_JOIN("Respawn In Waves After Join", "PLAYER", "WAVES"),
	RESPAWN_IN_WAVES_AFTER_DEATH("Respawn In Waves After Death", "PLAYER", "WAVES"),
    ON_PLAYER_JOIN("On Player Join", "PLAYER"),
    ON_PLAYER_DEATH("On Player Death", "PLAYER"),

	INSUFFICIENT_PERMISSIONS("Insufficient Permissions"),
	NOT_ALLOWED_WHILE_RUNNING("Not Allowed While Running"),
	CREATED_NEW_LEVEL("Created New Level", "LEVEL"),
	NO_LEVEL_LOADED("No Level Loaded"),
	CURRENT_GAMEMODE("Current Gamemode", "GAMEMODE"),
	INVENTORY_MUST_BE_CLEAR("Inventory Must Be Clear"),
	ZSPAWN_NOT_FOUND("ZSpawn Not Found"),
	JUMPED_TO_ZSPAWN("Jumped To ZSpawn", "ZSPAWN"),
	LEVEL_LIST_HEADER("Level List Header"),
	LEVEL_LIST_ITEM("Level List Item", "LEVEL"),
	LEAVE_GAME("Leave Game"),
	PLAYERS_ALIVE_HEADER("Players Alive Header"),
	PLAYERS_ALIVE_ITEM("Players Alive Item", "PLAYER"),
	PLAYERS_IN_SESSION_HEADER("Players In Session Header"),
	PLAYERS_IN_SESSION_ITEM("Players In Session Item", "PLAYER"),
	ZSPAWNS_LIST_HEADER("ZSpawns List Header"),
	ZSPAWNS_LIST_ITEM("ZSpawns List Item", "ZSPAWN"),
	LEVEL_NOT_FOUND("Level Not Found"),
	LEVEL_LOADED("Level Loaded", "LEVEL"),
	BOSS_SPAWN_SET("Boss Spawn Set"),
	BOSS_SPAWN_UNSET("Boss Spawn Unset"),
	BOSS_SPAWN_NOT_FOUND("Boss Spawn Not Found"),
	SIGN_NOT_FOUND("Sign Not Found"),
	SIGN_MARKED_AS_USEABLE_ONCE("Sign Marked As Useable Once"),
	SIGN_UNMARKED_AS_USEABLE_ONCE("Sign Unmarked As Useable Once"),
	SIGN_MARKED_AS_OPPOSITE("Sign Marked As Opposite"),
	SIGN_UNMARKED_AS_OPPOSITE("Sign Unmarked As Opposite"),
	SIGN_MARKED_AS_NON_RESETTING("Sign Marked As Non Resetting"),
	SIGN_UNMARKED_AS_NON_RESETTING("Sign Unmarked As Non Resetting"),
	FLAG_NOT_FOUND("Flag Not Found", "FLAG"),
	ZSPAWN_MARKED_AS_ACTIVE_WHEN_SIGN_ACTIVE("ZSpawn Marked As Active When Sign Active"),
	ZSPAWN_UNMARKED_AS_ACTIVE_WHEN_SIGN_ACTIVE("ZSpawn Unmarked As Active When Sign Active"),
	SPOUT_NOT_ENABLED("Spout Not Enabled"),
	SPOUT_CLIENT_REQUIRED("Spout Client Required"),
	CONFIG_RELOADED("Config Reloaded"),
	LEVEL_REMOVED("Level Removed", "LEVEL"),
	ZSPAWN_REMOVED("ZSpawn Removed"),
	LEVELS_SAVED("Levels Saved"),
	GAME_MUST_BE_RUNNING("Game Must Be Running"),
	INFO_ITEM("Info Item", "NAME", "VALUE"),
	PLAYER_NOT_FOUND("Player Not Found"),
	PLAYER_NOT_IN_GAME("Player Not In Game"),
	ARG4_MUST_BE_TRUE_OR_FALSE("Arg 4 Must Be True Or False"),
	SET_PLAYERS_ALIVE_STATUS("Set Players Alive Status"),
	DSPAWN_SET("DSpawn Set"),
	GAMEMODE_NOT_FOUND("Gamemode Not Found"),
	ISPAWN_SET("ISpawn Set"),
	LEAVE_LOCATION_SET("Leave Location Set", "LOCATION"),
	WAVE_MUST_BE_INTEGER("Wave Must Be Integer"),
	WAVE_SET("Wave Set"),
	ZSPAWN_SET("ZSpawn Set", "ZSPAWN"),
	GAME_STATS_HEADER("Game Stats Header"),
	GAME_STATS_ITEM("Game Stats Item", "PLAYER", "MONEY", "POINTS"),
	GAME_ALREADY_RUNNING("Game Already Running"),
	GAME_STARTED("Game Started"),
	GAME_STOPPED("Game Stopped"),
	VOTING_NOT_TAKING_PLACE("Voting Not Taking Place"),
	MUST_BE_INGAME_TO_VOTE("Must Be Ingame To Vote"),
	VOTE_MUST_BE_VALID_INTEGER("Vote Must Be Valid Integer"),
	VOTE_MUST_RANGE_FROM1_TO3("Vote Must Range From 1 To 3"),
	VOTE_SUCCESSFUL("Vote Successful"),
	SET_KILLS_MUST_BE_GREATER_OR_EQUAL_TO0("Set Kills Must Be Greater Or Equal To 0"),
	ADD_KILLS_MUST_BE_GREATER_THAN0("Add Kills Must Be Greater Than 0"),
	SUB_KILLS_MUST_BE_GREATER_THAN0("Sub Kills Must Be Greater Than 0"),
	KILLS_SET("Kills Set", "PLAYER", "NUM"),
	TOP_KILLERS_HEADER("Top Killers Header"),
	TOP_KILLERS_ITEM("Top Killers Item", "NUM", "PLAYER", "KILLS"),
	PLAYER_KILLS_INFO("Player Kills Info", "PLAYER", "KILLS", "RANK", "TOTAL");

	String name;
	String message = "";
	String[] params;

	Message(String name, String... params)
	{
		this.name = name;
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
			ZArena.log(Level.SEVERE, "Yo, the length of args and params don't match. Look to the enum "+this+" and fix it. (Or send this message to the dev so he can fix it)");
		String messageCopy = message;
		if(messageCopy.equals("<disabled>"))
			return "";
	    for (ChatColor color : ChatColor.values())
	    	messageCopy = messageCopy.replaceAll("(?i)<" + color.name() + ">", "" + color);
	    for(int i = 0; i < params.length; i++)
	    	messageCopy = messageCopy.replaceAll("%"+params[i]+"%", args[i].toString());
		messageCopy = messageCopy.replaceAll("<new_line>", "\n");
	    return messageCopy;
	}

	/**
	 * Sets the messages for each enumeration item. Needs to be done AFTER ChatHelper has loaded the language files.
	 */
	public static void setMessages()
	{
        //If the language file is missing some messages
        boolean outOfSync = false;
		for(Message message : Message.values())
		{
			//This following set of expressions uses the word 'message' a lot.
			if(ChatHelper.messages.containsKey(message.name))
			{
				message.message = ChatHelper.messages.get(message.name);
			} else
			{
                outOfSync = true;
				message.message = YamlConfiguration.loadConfiguration(new File(Constants.LANGUAGE_PATH)).getString(message.name);
			}
		}
        //Reload the config from the plugins default resources, and restore the users preferences
        if(outOfSync)
        {
			File file = new File(Constants.PLUGIN_FOLDER);
			Configuration language = Configuration.loadConfiguration(ZArena.class.getResourceAsStream("/language.yml"));
			for(Map.Entry<String, String> e : ChatHelper.messages.entrySet())
				language.set(e.getKey(), e.getValue());
			try
			{
				language.save(file);
			} catch(IOException e)
			{
				e.printStackTrace();
			}
        }
	}
}
