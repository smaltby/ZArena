package com.github.zarena.utils;

import java.io.File;

public class Constants
{
	public static final double TICK_LENGTH = 0.05;

	public static final String FIRST_TIME = "First Time Plugin Load";
	private static final String ZOMBIE_QUANTITY = "Zombies.Quantity";
	public static final String ZOMBIE_QUANTITY_COEFFICIENTS = ZOMBIE_QUANTITY+".Coefficients";
	public static final String ZOMBIE_QUANTITY_FORMULA = ZOMBIE_QUANTITY+".Formula Type [Quadratic|Logistic|Logarithmic]";
	public static final String ZOMBIE_QUANTITY_LIMIT = ZOMBIE_QUANTITY+".Limit (Only applicaple for Logistic Forumula)";
	private static final String ZOMBIE_HEALTH = "Zombies.Health";
	public static final String ZOMBIE_HEALTH_COEFFICIENTS = ZOMBIE_HEALTH+".Coefficients";
	public static final String ZOMBIE_HEALTH_FORMULA = ZOMBIE_HEALTH+".Formula Type [Quadratic|Logistic|Logarithmic]";
	public static final String ZOMBIE_HEALTH_LIMIT = ZOMBIE_HEALTH+".Limit (Only applicaple for Logistic Forumula)";
	public static final String ALWAYS_NIGHT = "Options.Always Night";
	public static final String AUTOSTART = "Options.Auto-Start";
	public static final String AUTORUN = "Options.Auto-Run";
	public static final String AUTOJOIN = "Options.Auto-Join";
	public static final String SAVE_POSITION = "Options.Save Position on Game Join";
	public static final String WAVE_DELAY = "Options.Wave Start Delay";
	public static final String VOTING_LENGTH = "Options.Voting Length";
	public static final String BROADCAST_ALL = "Options.Broadcast To All";
	public static final String SEPERATE_INVENTORY = "Options.Seperate Inventory";
	public static final String GAME_WORLD = "Options.Game World";
	public static final String GAME_LEAVE_WORLD = "Options.Game Leave World";
	public static final String GAME_LEAVE_LOCATION = "Options.Game Leave Location";
	public static final String WORLD_EXCLUSIVE = "Options.World Exclusive";
	public static final String DISABLE_HUNGER = "Options.Disable Hunger";
	public static final String DISABLE_NON_ZA = "Options.Disable Non ZArena Commands In Game";
	public static final String DISABLE_JOIN_WITH_INV = "Options.Disable Joining Game With Inventory";
	public static final String PLAYER_LIMIT = "Options.Player Limit";
	public static final String ENABLE_KILLCOUNTER = "Options.Enable Killcounter";
	public static final String ENABLE_AFKKICKER = "Options.Set AFK Players as Dead";
	public static final String XP_BAR_IS_MONEY = "Options.XP Bar Shows Money";
	public static final String QUANTITY_ADJUST = "Options.Adjust Quantity Based on Player Amount";
	public static final String SAVE_ITEMS = "Options.Save Items";
	public static final String START_ITEMS = "Options.Start Items";
	public static final String RESPAWN_EVERY_WAVES = "Options.Respawn Every X Waves (0 to disable)";
	public static final String RESPAWN_EVERY_TIME = "Options.Respawn Every X Minutes (0 to disable)";
	public static final String RESPAWN_REMINDER_DELAY = "Options.Respawn Reminder Delay (Seconds)";
	public static final String SHOP_HEADER = "Options.Shop Sign Header";
	public static final String TOLL_HEADER = "Options.Toll Sign Header";
	public static final String MOB_CAP = "Entities.Mob Cap";
	public static final String DEFAULT_ZOMBIE = "Entities.Default Entity File Name";
	public static final String DEFAULT_SKELETON = "Entities.Default Skeleton File Name";
	public static final String DEFAULT_WOLF = "Entities.Default Wolf File Name";
	public static final String ENTITIES = "Entities.Entity File Name List";
	public static final String WOLF_PERCENT_SPAWN = "Entities.Wolf Spawn Chance";
	public static final String SKELETON_PERCENT_SPAWN = "Entities.Skeleton Spawn Chance";
	public static final String WOLF_WAVE_PERCENT_OCCUR = "Entities.Wolf Wave Chance";
	public static final String SKELETON_WAVE_PERCENT_OCCUR = "Entities.Skeleton Wave Chance";
	public static final String WOLF_WAVE_PERCENT_SPAWN = "Entities.Wolf Spawn Chance During Wolf Wave";
	public static final String SKELETON_WAVE_PERCENT_SPAWN = "Entities.Skeleton Spawn Chance During Wolf Wave";
	public static final String DEFAULT_GAMEMODE = "Gamemodes.Default Gamemode File Name";
	public static final String GAMEMODES = "Gamemodes.Gamemode File Name List";
	public static final String KILL_MONEY = "Stats.Money on Kill";
	public static final String MONEY_LOST = "Stats.Money Percent Lost on Death";
	public static final String USE_VAULT = "Stats.Use Vault Economy";
	public static final String CUSTOM_ITEMS = "SignCustomItems";
	public static final String START_MONEY = "Donator.Start Money";
	public static final String EXTRA_VOTES = "Donator.Extra Votes";

	public static final String PLUGIN_FOLDER = "plugins"+File.separator+"ZArena";
	public static final String ENTITIES_FOLDER = PLUGIN_FOLDER+File.separator+"entities";
	public static final String GAMEMODES_FOLDER = PLUGIN_FOLDER+File.separator+"gamemodes";
	public static final String LANGUAGE_PATH = PLUGIN_FOLDER+File.separator+"language.yml";;
	public static final String LEVEL_PATH = PLUGIN_FOLDER+File.separator+"levels.ext";
	public static final String OPTIONS_PATH =  PLUGIN_FOLDER+File.separator+"options.ext";
	public static final String KILLS_PATH = PLUGIN_FOLDER+File.separator+"kills.ext";

}
