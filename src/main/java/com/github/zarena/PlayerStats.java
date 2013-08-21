package com.github.zarena;

import com.github.zarena.utils.ConfigEnum;
import net.milkbowl.vault.economy.Economy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.zarena.utils.ChatHelper;
import com.github.zarena.utils.Message;

public class PlayerStats implements Comparable<PlayerStats>
{
	private String player;
	private float money;
	private int kills;
	private Map<String, Integer> killsPerType = new HashMap<String, Integer>();	//Recorded for achievement purposes
	private boolean alive;
	private int deathWave;
	private long deathTime;

	//Saved pre-game info
	private Location oldLocation;
	private ItemStack[] items;
	private ItemStack[] armor;
	private GameMode oldGameMode;
	private int oldLevel;
	private double oldMoney;

	public PlayerStats(Player player)
	{
		this.player = player.getName();
		alive = false;
		resetStats();

		oldLocation = player.getLocation();
		items = player.getInventory().getContents();
		armor = player.getInventory().getArmorContents();
		oldGameMode = player.getGameMode();
		oldLevel = player.getLevel();
		if(usingVault() && ZArena.getInstance().getConfig().getBoolean(ConfigEnum.SEPERATE_MONEY.toString()))
		{
			oldMoney = getEconomy().getBalance(player.getName());
			getEconomy().withdrawPlayer(player.getName(), oldMoney);
		}
	}

	public PlayerStats(String player, Location location, ItemStack[] items, ItemStack[] armor, GameMode gameMode, int level, double money)
	{
		this.player = player;
		alive = false;
		resetStats();

		oldLocation = location;
		this.items = items;
		this.armor = armor;
		oldGameMode = gameMode;
		oldLevel = level;
		oldMoney = money;
	}

	public void setMoney(double money)
	{
		if(usingVault())
			getEconomy().depositPlayer(player, money - getEconomy().getBalance(player));
		else
			this.money = (float) money;
		if(ZArena.getInstance().getConfig().getBoolean(ConfigEnum.XP_BAR_IS_MONEY.toString()))
			getPlayer().setLevel((int) getMoney());
	}

	public void addMoney(double money)
	{
		if(usingVault())
			getEconomy().depositPlayer(player, money);
		else
			this.money += money;
		if(ZArena.getInstance().getConfig().getBoolean(ConfigEnum.XP_BAR_IS_MONEY.toString()))
			getPlayer().setLevel((int) getMoney());
	}

	public void addKills(int kills, String type)
	{
		this.kills += kills;
		killsPerType.put(type, killsPerType.containsKey(type) ? killsPerType.get(type) + kills : kills);
	}

	public ItemStack[] getInventoryArmor()
	{
		return armor;
	}

	public ItemStack[] getInventoryContents()
	{
		return items;
	}

	public float getMoney()
	{
		return (float) (usingVault() ? getEconomy().getBalance(player) : money);
	}

	public GameMode getOldGameMode()
	{
		return oldGameMode;
	}

	public int getOldLevel()
	{
		return oldLevel;
	}

	public Location getOldLocation()
	{
		return oldLocation;
	}

	public double getOldMoney()
	{
		return oldMoney;
	}

	public Player getPlayer()
	{
		return Bukkit.getPlayer(player);
	}

	public int getKills()
	{
		return kills;
	}

	public int getKills(String type)
	{
		return killsPerType.containsKey(type) ? killsPerType.get(type) : 0;
	}

	/**
	 * Returns the players time since death in seconds.
	 */
	public int getTimeSinceDeath()
	{
		//The +1 is to ensure this never returns 0. This prevents the plugin from sending the player a message on death while
		//simultaneously sending them a message updating the time until the respawn.
		return (int) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toSeconds(deathTime) + 1);
	}

	public int getWavesSinceDeath()
	{
		return ZArena.getInstance().getGameHandler().getWaveHandler().getWave() - deathWave;
	}

	public boolean isAlive()
	{
		return alive;
	}

	public void messageStats()
	{
		ChatHelper.sendMessage(Message.SEND_STATS.formatMessage(getMoney(), getKills()), getPlayer());
	}

	public void registerDeath()
	{
		deathWave = ZArena.getInstance().getGameHandler().getWaveHandler().getWave();
		deathTime = System.currentTimeMillis();
	}

	public void resetStats()
	{
		if(!ZArena.getInstance().getConfig().getBoolean(ConfigEnum.KEEP_MONEY_ACROSS_GAMES.toString()))
		{
			if(usingVault())
				getEconomy().withdrawPlayer(player, getEconomy().getBalance(player));
			else
				money = 0;
		}
		kills = 0;
		deathWave = ZArena.getInstance().getGameHandler().getWaveHandler().getWave();
		deathTime = System.currentTimeMillis();
	}

	public void setAlive(boolean alive)
	{
		this.alive = alive;
	}

	public void subMoney(double money)
	{
		if(usingVault())
			getEconomy().withdrawPlayer(player, money);
		else
		{
			this.money -= money;
			if(this.money < 0)
				this.money = 0;
		}
		if(ZArena.getInstance().getConfig().getBoolean(ConfigEnum.XP_BAR_IS_MONEY.toString()))
			getPlayer().setLevel((int) getMoney());
	}

	public void subKills(int kills)
	{
		this.kills -= kills;
	}

	@Override
	public int compareTo(PlayerStats stats)
	{
		if(stats.getKills() > this.getKills())
			return 1;
		else if(stats.getKills() == this.getKills())
		{
			if(stats.getMoney() > this.getMoney())
				return 1;
			else if(stats.getMoney() == this.getMoney())
			{
				if(this.getPlayer() == null && stats.getPlayer() == null)
					return 0;
				if(this.getPlayer() == null)
					return 1;
				if(stats.getPlayer() == null)
					return -1;
				if(stats.getPlayer().getHealth() > this.getPlayer().getHealth())
					return 1;
				else if(stats.getPlayer().getHealth() == this.getPlayer().getHealth())
					return 0;
				return -1;
			}
			return -1;
		}
		return -1;
	}

	private boolean usingVault()
	{
		return ZArena.getInstance().getConfig().getBoolean(ConfigEnum.USE_VAULT.toString()) && getEconomy() != null;
	}

	private Economy getEconomy()
	{
		return ZArena.getInstance().getEconomy();
	}
}
