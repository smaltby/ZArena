package kabbage.zarena;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import kabbage.zarena.commands.utils.CommandSenderWrapper;
import kabbage.zarena.signs.ZSignCustomItem;
import kabbage.zarena.utils.ChatHelper;
import kabbage.zarena.utils.Constants;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class GameHandler
{	
	private ZArena plugin;
	private WaveHandler waveHandler;
	private LevelHandler levelHandler;
	private LevelVoter levelVoter;
	
	private int waveTaskID;
	protected int voterTaskID; 	//protected to allow easy access from level voter
	
	private boolean isRunning;
	protected boolean isVoting; //protected to allow easy access from level voter
	private boolean isWaiting;
	
	private List<String> players;
	private Map<String, PlayerStats> playerStats;
	
	private Gamemode gamemode;
	private ZLevel level;
	
	public Gamemode defaultGamemode;
	public List<Gamemode> gamemodes = new ArrayList<Gamemode>();
	
	public GameHandler()
	{
		plugin = ZArena.getInstance();
		isRunning = false;
		isVoting = false;
		isWaiting = plugin.getConfig().getBoolean(Constants.AUTOSTART);
		waveHandler = new WaveHandler(this);
		levelHandler = new LevelHandler();
		levelVoter = new LevelVoter(this);
		players = new ArrayList<String>();
		playerStats = new HashMap<String, PlayerStats>();
	}
	
	/**
	 * Adds a player to the game.
	 * @param player the player to add
	 */
	public void addPlayer(Player player)
	{
		if(players.size() >= plugin.getConfig().getInt(Constants.PLAYER_LIMIT))
		{
			player.sendMessage(ChatColor.DARK_PURPLE+"The game is currently full.");
			return;
		}
		if(players.contains(player.getName()))
			return;
		
		players.add(player.getName());
		PlayerStats stats = new PlayerStats(player);
		playerStats.put(player.getName(), stats);
		
		clearInventory(player.getInventory());
		player.setGameMode(org.bukkit.GameMode.ADVENTURE);
		
		int wave = waveHandler.getWave();
		if(isRunning)
		{
			if(wave == 1 && !(gamemode.isApocalypse()))
				addToGame(stats);
			else if(level != null)
				player.teleport(level.getDeathSpawn());
			else
				player.teleport(player.getWorld().getSpawnLocation());
		}
		else if(isVoting)
		{
			if(level != null)
				player.teleport(level.getDeathSpawn());
			else
				player.teleport(player.getWorld().getSpawnLocation());
			player.sendMessage(ChatHelper.VOTE_START);
			player.sendMessage(levelVoter.getVoteMessage());
			player.sendMessage(String.format(ChatHelper.VOTE_ENDS_IN, plugin.getConfig().getInt(Constants.VOTING_LENGTH)));
		}
		else
		{
			if(level != null)
				player.teleport(level.getDeathSpawn());
			if(isWaiting)
				start();
		}
	}
	
	private void addStartItems(PlayerInventory inv)
	{
		for(String item : plugin.getConfig().getStringList(Constants.START_ITEMS))
		{
			ZSignCustomItem customItem = ZSignCustomItem.getCustomItem(item.split("\\s"));
			if(customItem != null)
			{
				inv.addItem(customItem.getItem());
				continue;
			}
			ItemStack itemStack = new ItemStack(Material.getMaterial(item.replaceAll(" ", "_").toUpperCase()));
			if(itemStack != null)
				inv.addItem(itemStack);
		}
		for(ItemStack item : gamemode.getStartItems())
		{
			inv.addItem(item);
		}
	}
	
	/**
	 * Prepares everything about a player and his/her stats in preperation for joining a game
	 * @param stats the stats to reset, and the stats to get the player from who is then prepared for the game
	 */
	private void addToGame(PlayerStats stats)
	{
		stats.resetStats();
		stats.setAlive(true);
		
		Player player = stats.getPlayer();
		if(player == null)
			return;
		if(level != null)
			player.teleport(level.getInitialSpawn());
		else
			player.teleport(player.getWorld().getSpawnLocation());
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setSaturation(20);
		
		stats.addMoney(new CommandSenderWrapper(player).startMoney());
		
		for(PotionEffect effect : player.getActivePotionEffects())
		{
			player.removePotionEffect(effect.getType());
		}
		PlayerInventory pi = player.getInventory();
		clearInventory(pi);
		addStartItems(pi);
	}
	
	public void clearInventory(PlayerInventory inv)
	{
		inv.clear();
		inv.setHelmet(null);
		inv.setChestplate(null);
		inv.setLeggings(null);
		inv.setBoots(null);
	}
	
	public int getAliveCount()
	{
		int alive = 0;
		for(PlayerStats stats : playerStats.values())
		{
			if(stats.isAlive())
				alive++;
		}
		return alive;
	}
	
	public List<Player> getBroadcastPlayers()
	{
		List<Player> toBroadcast = getPlayers();
		if(plugin.getConfig().getBoolean(Constants.BROADCAST_ALL, false))
			toBroadcast = Arrays.asList(Bukkit.getOnlinePlayers());
		else if(plugin.getConfig().getBoolean(Constants.WORLD_EXCLUSIVE, false))
		{
			for(Player p : Bukkit.getServer().getOnlinePlayers())
			{
				if(p.getWorld().getName().equals(plugin.getConfig().getString(Constants.GAME_WORLD)))
					toBroadcast.add(p);
			}
		}
		return toBroadcast;
	}
	
	public Gamemode getGameMode()
	{
		return gamemode;
	}
	
	public ZLevel getLevel()
	{
		return level;
	}
	
	public LevelHandler getLevelHandler()
	{
		return levelHandler;
	}
	
	public LevelVoter getLevelVoter()
	{
		return levelVoter;
	}
	
	public synchronized List<String> getPlayerNames()
	{
		return players;
	}
	
	public synchronized List<Player> getPlayers()
	{
		List<Player> playerInstances = new ArrayList<Player>();
		for(String playerName : players)
		{
			playerInstances.add(Bukkit.getPlayer(playerName));
		}
		return playerInstances;
	}
	
	public Location getPlayersLeaveLocation(Player player)
	{
		if(plugin.getConfig().getBoolean(Constants.SAVE_POSITION))
		{
			Location oldLocation = getPlayerStats(player).getOldLocation();
			if(oldLocation != null)
				return oldLocation;
		}
		World world = Bukkit.createWorld(new WorldCreator(plugin.getConfig().getString(Constants.GAME_LEAVE_WORLD, "world")));
		List<Double> locXYZ = plugin.getConfig().getDoubleList(Constants.GAME_LEAVE_LOCATION);
		Location defaultLocation = new Location(world, locXYZ.get(0), locXYZ.get(1), locXYZ.get(2));
		return defaultLocation;
	}
	
	public synchronized Map<String, PlayerStats> getPlayerStats()
	{
		return playerStats;
	}
	
	public synchronized PlayerStats getPlayerStats(Player player)
	{
		return playerStats.get(player.getName());
	}
	
	public WaveHandler getWaveHandler()
	{
		return waveHandler;
	}
	
	public boolean isRunning()
	{
		return isRunning;
	}
	
	public boolean isVoting()
	{
		return isVoting;
	}
	
	public boolean isWaiting()
	{
		return isWaiting;
	}
	
	void loadLevelHandler()
	{
		File path = new File(Constants.LEVEL_PATH);

        FileInputStream fis;
        ObjectInputStream ois;

        try {
            fis = new FileInputStream(path);
            ois = new ObjectInputStream(fis);

            levelHandler = new LevelHandler();
            levelHandler.readExternal(ois);

            ois.close();
            fis.close();

        } catch (Exception e)
        {
        	ZArena.logger.log(Level.WARNING, "ZArena: Couldn't load the LevelHandler database. Ignore if this is the first time the plugin has been run.");
            levelHandler = new LevelHandler();
        }
	}
	
	/**
	 * Removes a player from the game.
	 * @param player player to remove
	 */
	public void removePlayer(Player player)
	{
		if(players.contains(player.getName()))
		{
			PlayerStats stats = getPlayerStats(player);
			clearInventory(player.getInventory());
			player.teleport(getPlayersLeaveLocation(player));
			player.setGameMode(stats.getOldGameMode());
			if(plugin.getConfig().getBoolean(Constants.SAVE_ITEMS))
			{
				PlayerInventory pi = player.getInventory();
				ItemStack[] contents = stats.getInventoryContents();
				if(contents != null)
					pi.setContents(contents);
				ItemStack[] armorContents = stats.getInventoryArmor();
				if(armorContents != null)
					pi.setArmorContents(armorContents);
			}
			
			players.remove(player.getName());
			playerStats.remove(player.getName());
		}
	}
	
	public void respawnPlayers()
	{
		for(Player player : getPlayers())
		{
			if(!getPlayerStats(player).isAlive())
			{
				getPlayerStats(player).setAlive(true);
				player.teleport(level.getInitialSpawn());
				player.setHealth(20);
				player.setFoodLevel(20);
				player.setSaturation(20);
				for(PotionEffect effect : player.getActivePotionEffects())
				{
					player.removePotionEffect(effect.getType());
				}
				PlayerInventory pi = player.getInventory();
				clearInventory(pi);
				addStartItems(pi);
			}
		}
	}
	
	public void saveLevelHandler()
	{
		File path = new File(Constants.LEVEL_PATH);

        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            fos = new FileOutputStream(path);
            oos = new ObjectOutputStream(fos);

            levelHandler.writeExternal(oos);

            oos.close();
            fos.close();

        } catch (IOException e)
        {
        	ZArena.logger.log(Level.WARNING, "ZArena: Error saving the LevelHandler database.");
        }
	}
	
	public void setGameMode(Gamemode gameMode)
	{
		this.gamemode = gameMode;
	}
	
	public void setLevel(ZLevel level)
	{
		this.level = level;
	}
	
	/**
	 * Starts the game
	 */
	public void start()
	{
		if(isRunning || isVoting)
			return;
		if(players.isEmpty())
		{
			isWaiting = plugin.getConfig().getBoolean(Constants.AUTOSTART);
			return;
		}
		if(level == null)
			levelVoter.startVoting();
		else
		{
			for(PlayerStats stats : playerStats.values())
			{
				Player player = stats.getPlayer();
				if(player.getWorld().getName() != plugin.getConfig().getString(Constants.GAME_WORLD))
				{
					boolean flag = false;
					if(!player.isOp())
					{
						player.setOp(true);
						flag = true;
					}
					plugin.getServer().dispatchCommand(player, "mv tp "+plugin.getConfig().getString(Constants.GAME_WORLD));
					if(flag)
						player.setOp(false);
				}
				addToGame(stats);
			}

			for(LivingEntity entity : plugin.getServer().getWorld(plugin.getConfig().getString(Constants.GAME_WORLD)).getLivingEntities())
			{
				if(!(entity instanceof Player))
					entity.setHealth(0);
			}
			isRunning = true;
			waveHandler.resetWave();
			waveTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, waveHandler, 1L, 1L);
		}
	}

	/**
	 * Stops the game
	 */
	public void stop()
	{
		if(!isRunning && !isVoting)
			return;
		if(isRunning)
			Bukkit.getScheduler().cancelTask(waveTaskID);
		if(isVoting)
		{
			Bukkit.getScheduler().cancelTask(voterTaskID);
			levelVoter.resetVoting();
		}
		isRunning = false;
		isWaiting = false;
		isVoting = false;
		if(level != null)
		{
			level.resetSigns();
			level.resetInactiveZSpawns();
		}
		for(Entity entity : plugin.getServer().getWorld(plugin.getConfig().getString(Constants.GAME_WORLD)).getEntities())
		{
			if(entity instanceof Player)
				continue;
			if(entity instanceof LivingEntity)
				((LivingEntity) entity).setHealth(0);
			else
				entity.remove();
		}
		for(PlayerStats stats : playerStats.values())
		{
			stats.resetStats();
			stats.setAlive(false);
			
			Player player = stats.getPlayer();
			if(player != null)
			{
				clearInventory(player.getInventory());
				for(PotionEffect effect : player.getActivePotionEffects())
				{
					player.removePotionEffect(effect.getType());
				}
			}
		}
		if(gamemode != null && gamemode.isApocalypse())
			ChatHelper.broadcastMessage(ChatColor.RED + "You survived "+waveHandler.getGameLength()+" seconds!", getBroadcastPlayers());
		else	
			ChatHelper.broadcastMessage(ChatColor.RED + "You reached wave "+waveHandler.getWave()+"!", getBroadcastPlayers());
	}
}