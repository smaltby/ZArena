package com.github.zarena.utils;

public enum ConfigEnum
{
	VERSION("Version"),
	QUANTITY_STARTING("Quantity.Starting Quantity"),
	QUANTITY_INCREASE("Quantity.Increase Per Wave"),
	QUANTITY_EXPOTENTIAL_INCREASE("Quantity.Expotential Increase Per Wave"),
	QUANTITY_LIMIT("Quantity.Limit"),
	QUANTITY_SOFT_LIMIT("Quantity.Soft Limit?"),
	QUANTITY_FORMULA("Quantity.Custom Formula"),
	HEALTH_STARTING("Health.Starting Health"),
	HEALTH_INCREASE("Health.Increase Per Wave"),
	HEALTH_EXPOTENTIAL_INCREASE("Health.Expotential Increase Per Wave"),
	HEALTH_LIMIT("Health.Limit"),
	HEALTH_SOFT_LIMIT("Health.Soft Limit?"),
	HEALTH_FORMULA("Health.Custom Formula"),
	ALWAYS_NIGHT("Always Night"),
	AUTOSTART("Auto-Start"),
	AUTORUN("Auto-Run"),
	AUTOJOIN("Auto-Join"),
	SAVE_POSITION("Save Position on Game Join"),
	WAVE_DELAY("Wave Start Delay"),
	VOTING_LENGTH("Voting Length"),
	BROADCAST_ALL("Broadcast To All"),
	SEPERATE_INVENTORY("Seperate Inventory"),
	SEPERATE_MONEY("Seperate Money"),
	GAME_LEAVE_WORLD("Game Leave World"),
	GAME_LEAVE_LOCATION("Game Leave Location"),
	WORLD_EXCLUSIVE("World Exclusive"),
	DISABLE_HUNGER("Disable Hunger"),
	DISABLE_NON_ZA("Disable Non ZArena Commands In Game"),
	DISABLE_JOIN_WITH_INV("Disable Joining Game With Inventory"),
	PLAYER_LIMIT("Player Limit"),
	ENABLE_KILLCOUNTER("Enable Killcounter"),
	ENABLE_AFKKICKER("Set AFK Players as Dead"),
	XP_BAR_IS_MONEY("XP Bar Shows Money"),
	QUANTITY_ADJUST("Adjust Quantity Based on Player Amount"),
	SAVE_ITEMS("Save Items"),
	START_ITEMS("Start Items"),
	RESPAWN_EVERY_WAVES("Respawn Every X Waves"),
	RESPAWN_EVERY_TIME("Respawn Every X Minutes"),
	RESPAWN_REMINDER_DELAY("Respawn Reminder Delay"),
	SHOP_HEADER("Shop Sign Header"),
	TOLL_HEADER("Toll Sign Header"),
	DEFAULT_ZOMBIE("Default Entity File Name"),
	DEFAULT_SKELETON("Default Skeleton File Name"),
	DEFAULT_WOLF("Default Wolf File Name"),
	WOLF_PERCENT_SPAWN("Wolf Spawn Chance"),
	SKELETON_PERCENT_SPAWN("Skeleton Spawn Chance"),
	WOLF_WAVE_PERCENT_OCCUR("Wolf Wave Chance"),
	SKELETON_WAVE_PERCENT_OCCUR("Skeleton Wave Chance"),
	WOLF_WAVE_PERCENT_SPAWN("Wolf Spawn Chance During Wolf Wave"),
	SKELETON_WAVE_PERCENT_SPAWN("Skeleton Spawn Chance During Skeleton Wave"),
	DEFAULT_GAMEMODE("Default Gamemode File Name"),
	KILL_MONEY("Money on Kill"),
	MONEY_LOST("Money Percent Lost on Death"),
	USE_VAULT("Use Vault Economy"),
	CUSTOM_ITEMS("Sign Custom Items"),
	START_MONEY("Donator.Start Money"),
	EXTRA_VOTES("Donator.Extra Votes"),
	MOB_CAP("Mob Cap"),
	KEEP_ITEMS_ACROSS_GAMES("Keep Items Across Games"),
	KEEP_MONEY_ACROSS_GAMES("Keep Money Across Games"),
	NEXT_WAVE_IF_ENTITY_STUCK("Start Next Wave If An Entity Is Stuck"),
	SPAWN_CHANCE("Spawn Chance");

	String path;
	ConfigEnum(String path)
	{
		this.path = path;
	}

	public String toString()
	{
		return path;
	}
}
