package kabbage.zarena;

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
	Location oldLocation;
	ItemStack[] items;
	ItemStack[] armor;
	GameMode oldGameMode;
	
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
	}
	
	public Player getPlayer()
	{
		return Bukkit.getPlayer(player);
	}
	
	public float getMoney()
	{
		return money;
	}
	
	public int getPoints()
	{
		return points;
	}
	
	public boolean isAlive()
	{
		return alive;
	}
	
	public int getWavesSinceDeath()
	{
		return wavesSinceDeath;
	}
	
	public Location getOldLocation()
	{
		return oldLocation;
	}
	
	public ItemStack[] getInventoryContents()
	{
		return items;
	}
	
	public ItemStack[] getInventoryArmor()
	{
		return armor;
	}
	
	public GameMode getOldGameMode()
	{
		return oldGameMode;
	}
	
	public void addMoney(double money)
	{
		this.money += money;
	}
	
	public void addPoints(int points)
	{
		this.points += points;
	}
	
	public void subMoney(double money)
	{
		this.money -= money;
		if(this.money < 0)
			this.money = 0;
	}
	
	public void subPoints(int points)
	{
		this.points -= points;
	}
	
	public void setAlive(boolean alive)
	{
		this.alive = alive;
	}
	
	public void setWavesSinceDeath(int wavesSinceDeath)
	{
		this.wavesSinceDeath = wavesSinceDeath;
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
	
}
