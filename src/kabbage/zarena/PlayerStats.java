package kabbage.zarena;

import kabbage.zarena.utils.Constants;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerStats
{
	private String player;
	private float money;
	private int points;
	private boolean alive;
	private int wavesSinceDeath;
	
	//Saved pre-game info
	private Location oldLocation;
	private ItemStack[] items;
	private ItemStack[] armor;
	private GameMode oldGameMode;
	private int oldLevel;
	
	public PlayerStats(Player player)
	{
		this.player = player.getName();
		money = 0;
		points = 0;
		alive = false;
		
		oldLocation = player.getLocation();
		items = player.getInventory().getContents();
		armor = player.getInventory().getArmorContents();
		oldGameMode = player.getGameMode();
		oldLevel = player.getLevel();
	}
	
	public void addMoney(double money)
	{
		this.money += money;
		if(ZArena.getInstance().getConfig().getBoolean(Constants.XP_BAR_IS_MONEY))
			getPlayer().setLevel((int) this.money);
	}
	
	public void addPoints(int points)
	{
		this.points += points;
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
		return money;
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
	
	public Player getPlayer()
	{
		return Bukkit.getPlayer(player);
	}
	
	public int getPoints()
	{
		return points;
	}
	
	public int getWavesSinceDeath()
	{
		return wavesSinceDeath;
	}
	
	public boolean isAlive()
	{
		return alive;
	}
	
	public void messageStats()
	{
		getPlayer().sendMessage(ChatColor.DARK_GRAY+"Money: "+ChatColor.GRAY+money+ChatColor.DARK_GRAY+" Points: "+ChatColor.GRAY+points);
	}
	
	public void resetStats()
	{
		money = 0;
		points = 0;
		wavesSinceDeath = 0;
	}
	
	public void setAlive(boolean alive)
	{
		this.alive = alive;
	}
	
	public void setWavesSinceDeath(int wavesSinceDeath)
	{
		this.wavesSinceDeath = wavesSinceDeath;
	}
	
	public void subMoney(double money)
	{
		this.money -= money;
		if(this.money < 0)
			this.money = 0;
		if(ZArena.getInstance().getConfig().getBoolean(Constants.XP_BAR_IS_MONEY))
			getPlayer().setLevel((int) this.money);
	}
	
	public void subPoints(int points)
	{
		this.points -= points;
	}
}
