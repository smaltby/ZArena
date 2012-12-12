package kabbage.zarena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerStats
{
	private String player;
	private float money;
	private int points;
	private boolean alive;
	
	public PlayerStats(String player)
	{
		this.player = player;
		money = 0;
		points = 0;
		alive = false;
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
	
	public void messageStats()
	{
		getPlayer().sendMessage(ChatColor.DARK_GRAY+"Money: "+ChatColor.GRAY+money+ChatColor.DARK_GRAY+" Points: "+ChatColor.GRAY+points);
	}
	
	public void resetStats()
	{
		money = 0;
		points = 0;
	}
	
}
