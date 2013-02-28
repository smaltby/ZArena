package kabbage.zarena;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import kabbage.customentitylibrary.CustomEntityWrapper;
import kabbage.zarena.customentities.CustomSkeleton;
import kabbage.zarena.customentities.CustomWolf;
import kabbage.zarena.customentities.CustomZombie;
import kabbage.zarena.customentities.EntityTypeConfiguration;
import kabbage.zarena.customentities.SkeletonTypeConfiguration;
import kabbage.zarena.events.GameStopCause;
import kabbage.zarena.events.GameStopEvent;
import kabbage.zarena.events.WaveChangeEvent;
import kabbage.zarena.utils.ChatHelper;
import kabbage.zarena.utils.Constants;
import kabbage.zarena.utils.StringEnums;

public class WaveHandler implements Runnable
{
	private ZArena plugin;
	private GameHandler gameHandler;
	
	private double timeUntilNextWave;
	private double zombieSpawnChance;
	private Random rnd;
	private int wave;
	private int toSpawn;
	private int health;
	private List<CustomEntityWrapper> entities;
	private int tickCount;
	private boolean wolfWave;
	private boolean skeletonWave;
	private int lastWolfWave;
	private int lastSkeletonWave;
	
	EntityTypeConfiguration defaultZombieType;
	SkeletonTypeConfiguration defaultSkeletonType;
	EntityTypeConfiguration defaultWolfType;
	List<EntityTypeConfiguration> zombieTypes = new ArrayList<EntityTypeConfiguration>();
	List<SkeletonTypeConfiguration> skeletonTypes = new ArrayList<SkeletonTypeConfiguration>();
	List<EntityTypeConfiguration> wolfTypes = new ArrayList<EntityTypeConfiguration>();
	
	public WaveHandler(GameHandler instance)
	{
		plugin = ZArena.getInstance();
		gameHandler = instance;
		rnd = new Random();
		entities = new LinkedList<CustomEntityWrapper>();
		wave = 0;
		wolfWave = false;
		skeletonWave = false;
		lastSkeletonWave = 0;
		lastWolfWave = 0;
	}
	
	private void attemptSpawnEntity()
	{
		Gamemode gm = gameHandler.getGameMode();
		if(toSpawn > 0 || (gm.isApocalypse()))
		{
			if(rnd.nextDouble() < zombieSpawnChance)
			{
				CustomEntityWrapper ent = spawnEntity();
				if(ent != null)
					entities.add(ent);
			}
		}
	}
	
	/**
	 * Calculates function based on configuration settings
	 * @return output gotten from function
	 */
	public int calcFunction(String type, int x, int limit, List<Double> coefficients)
	{
		if(coefficients.size() > 3)
			coefficients = coefficients.subList(0, 3);
		else if(coefficients.size() < 3)
		{
			ZArena.logger.log(Level.WARNING, "Either Zombie Quantity or Zombie Health in the configuration is set incorrectly. Using default values instead until the" +
					"problem is fixed.");
			coefficients.clear();
			coefficients.add(.5d);
			coefficients.add(.5d);
			coefficients.add(5d);
		}
		if(type == null)
			type = "Quadratic";
		switch(StringEnums.valueOf(type.toUpperCase()))
		{
		case QUADRATIC:
			return calcQuadratic(x, coefficients);
		case LOGISTIC:
			return calcLogistic(x, limit, coefficients);
		case LOGARITHMIC:
			return calcLogarithmic(x, coefficients);
		default:
			return calcQuadratic(x, coefficients);
		}
	}
	
	/**
	 * Note, not a true logarithmic formula. It has been modified to be realistically useful for the plugin.
	 */
	private int calcLogarithmic(int x, List<Double> coefficients)
	{
		if (coefficients == null || coefficients.size() != 3) return 0;
		double a = coefficients.get(0);
		double b = coefficients.get(1);
		double c = coefficients.get(2);
		double base = Math.pow(10, 1/a);
		double answer = (Math.log(x)) / Math.log(base) + b * x + c;
		return (int) Math.round(answer);
	}
	
	/**
	 * Note, not a true logistic formula. It has been modified to be realistically useful for the plugin.
	 */
	private int calcLogistic(int x, int limit, List<Double> coefficients)
	{
		if (coefficients == null || coefficients.size() != 3) return 0;
		limit = (int) (limit * .75);	//With the weird way I set up the calculation, the limit tends to get exceeded by ~25% unless I do this
		double a = coefficients.get(0) / 3;	//Divide by 3 to prevent the rise from being to drastic unless it is wanted to
		double b = coefficients.get(1);
		double c = coefficients.get(2);
		double functionZero = limit / (1 + Math.pow(Math.E, ((-1*a)*(0 - 8))));	//Makes the function start at 0 for the theoretical wave 0
		double answer = limit / (1 + Math.pow(Math.E, ((-1*a)*(x - 8)))) - functionZero + b*x + c;
		return (int) Math.round(answer);
	}
	
	private int calcQuadratic(int x, List<Double> coefficients)
	{
		if (coefficients == null || coefficients.size() != 3) return 0;
		double a = coefficients.get(0);
		double b = coefficients.get(1);
		double c = coefficients.get(2);
		double answer = Math.pow(a*x, 2) + b*x + c;
		return (int) Math.round(answer);
	}
	
	
	private boolean checkNextWave()
	{
		Gamemode gm = gameHandler.getGameMode();
		if(gm.isApocalypse())
			return false;
		return toSpawn <= 0 && entities.isEmpty() && timeUntilNextWave <= 0;
	}
	
	public int getApocalypseWave()
	{
		return (int) Math.ceil((Math.log(Math.pow((tickCount+1), 10)) + ((tickCount+1)/40))/20);	//Logarithmic function
	}
	
	public SkeletonTypeConfiguration getDefaultSkeletonType()
	{
		return defaultSkeletonType;
	}
	
	public EntityTypeConfiguration getDefaultWolfType()
	{
		return defaultWolfType;
	}
	
	public EntityTypeConfiguration getDefaultZombieType()
	{
		return defaultZombieType;
	}
	
	public List<CustomEntityWrapper> getEntites()
	{
		return entities;
	}
	
	public int getGameLength()
	{
		return tickCount / 20;
	}
	
	public int getRemainingZombies()
	{
		return entities.size() + toSpawn;
	}
	
	public double getSpawnChance()
	{
		return zombieSpawnChance;
	}
	
	public List<SkeletonTypeConfiguration> getSkeletonTypes()
	{
		return skeletonTypes;
	}
	
	public List<EntityTypeConfiguration> getWolfTypes()
	{
		return wolfTypes;
	}
	
	public List<EntityTypeConfiguration> getZombieTypes()
	{
		return zombieTypes;
	}
	
	public int getWave()
	{
		return wave;
	}
	
	private void incrementWave()
	{
		wave++;
		setWaveSettings();
		WaveChangeEvent event = new WaveChangeEvent(wave - 1, wave);
		Bukkit.getPluginManager().callEvent(event);
	}
	
	public void resetWave()
	{
		wave = 1;
		tickCount = 0;
		lastWolfWave = 0;
		lastSkeletonWave = 0;
		entities.clear();
		setWaveSettings();
	}

	@Override
	public void run()
	{
		if(gameHandler.isRunning())
		{
			if(timeUntilNextWave > 0)
			{
				timeUntilNextWave -= Constants.TICK_LENGTH;
				if(timeUntilNextWave <= 0)
					startWave();
			}
			else
			{
				attemptSpawnEntity();
				if(tickCount % 20 == 0)
				{
					updateEntityList();
					if(checkNextWave())
						incrementWave();
					//Sometimes, players escape the server without setting off any listeners...
					for(String pName : gameHandler.getPlayerNames())
					{
						Player p = Bukkit.getPlayer(pName);
						if(p == null || !p.isOnline())
							gameHandler.removePlayer(pName);
					}
				}
				if(tickCount % 100 == 0)
				{
					if(gameHandler.getGameMode().isApocalypse())
						setWaveSettings(false);
					if(gameHandler.getGameMode().isSlowRegen())
					{
						for(Player player : gameHandler.getPlayers())
						{
							int health = player.getHealth() + 1;
							if(health > 20)
								health = 20;
							if(health < 0)	//Somehow...this has happened several times during beta testing.
								health = 0;
							player.setHealth(health);
						}
					}
				}

				if(tickCount % 4800 == 0)
					Bukkit.getServer().getWorld(plugin.getConfig().getString(Constants.GAME_WORLD)).setTime(38000);
				tickCount++;
			}
			if(gameHandler.getAliveCount() <= 0)
			{
				GameStopEvent event = new GameStopEvent(GameStopCause.ALL_DEAD);
				Bukkit.getServer().getPluginManager().callEvent(event);
				gameHandler.stop();
				if(plugin.getConfig().getBoolean(Constants.AUTORUN))
					gameHandler.getLevelVoter().startVoting();
			}
		}
	}
	
	public void setWave(int wave)
	{
		WaveChangeEvent event = new WaveChangeEvent(this.wave, wave);
		Bukkit.getPluginManager().callEvent(event);
		this.wave = wave;
		setWaveSettings(false);
	}

	/**
	 * Set the wave settings for this particular wave.
	 * @param newWave	whether or not the settings are being set because of a new wave
	 */
	private void setWaveSettings(boolean newWave)
	{
		Gamemode gm = gameHandler.getGameMode();
		int modifiedWave = wave;
		if(gm.isApocalypse())
			modifiedWave = getApocalypseWave();
		if(modifiedWave < 1)
			modifiedWave = 1;

		if(newWave)
		{
			timeUntilNextWave = plugin.getConfig().getInt(Constants.WAVE_DELAY);
			ChatHelper.broadcastMessage(String.format(ChatHelper.WAVE_START_SOON, modifiedWave, timeUntilNextWave), gameHandler.getBroadcastPlayers());
		}
		//Calculate the waves settings
		toSpawn = calcFunction(plugin.getConfig().getString(Constants.ZOMBIE_QUANTITY_FORMULA), modifiedWave, plugin.getConfig().getInt(Constants.ZOMBIE_QUANTITY_LIMIT), 
				plugin.getConfig().getDoubleList(Constants.ZOMBIE_QUANTITY_COEFFICIENTS));
		health = calcFunction(plugin.getConfig().getString(Constants.ZOMBIE_HEALTH_FORMULA), modifiedWave, plugin.getConfig().getInt(Constants.ZOMBIE_HEALTH_LIMIT), 
				plugin.getConfig().getDoubleList(Constants.ZOMBIE_HEALTH_COEFFICIENTS));
		health *= gm.getHealthModifier();
		toSpawn *= gm.getZombieAmountModifier();
		if(plugin.getConfig().getBoolean(Constants.QUANTITY_ADJUST))
			toSpawn *= 2/(1 + Math.pow(Math.E, gameHandler.getAliveCount()/-4 + 1.2));
		
		zombieSpawnChance = 0.15 / (1 + Math.pow(Math.E, ((double) -1/4 * (modifiedWave / 2))));	//Logistic function

		if(!gm.isApocalypse() && newWave)
		{
			lastSkeletonWave++;
			lastWolfWave++;
			if(skeletonWave)
				skeletonWave = false;
			if(wolfWave)
				wolfWave = false;
			else
			{
				if(rnd.nextDouble() < plugin.getConfig().getDouble(Constants.WOLF_WAVE_PERCENT_OCCUR) && lastWolfWave > 4)
				{
					wolfWave = true;
					lastWolfWave = 0;
					ChatHelper.broadcastMessage(ChatColor.RED+"Wolf Wave Approaching!", gameHandler.getBroadcastPlayers());
				}
				else if(rnd.nextDouble() < plugin.getConfig().getDouble(Constants.SKELETON_WAVE_PERCENT_OCCUR) && lastSkeletonWave > 4)
				{
					skeletonWave = true;
					lastSkeletonWave = 0;
					ChatHelper.broadcastMessage(ChatColor.RED+"Skeleton Wave Approaching!", gameHandler.getBroadcastPlayers());
				}
			}
			if(plugin.getConfig().getInt(Constants.RESPAWN_EVERY) > 0)
			{
				for(PlayerStats stats : gameHandler.getPlayerStats().values())
				{
					if(!stats.isAlive())
					{
						if(stats.getWavesSinceDeath() + 1 >= plugin.getConfig().getInt(Constants.RESPAWN_EVERY))
							gameHandler.respawnPlayer(stats.getPlayer());
					}
				}
			}
		}
		
		//Modify settings based on gamemode
		if(gm.isApocalypse())
			zombieSpawnChance /= 1.7;	//Lessen the spawn rate a bit for apocalypse, so you don't get overrun TOO quickly
	}
	
	/**
	 * Set the wave settings for this particular wave.
	 */
	private void setWaveSettings()
	{
		this.setWaveSettings(true);
	}
	
	/**
	 * Decides which type of entity should be spawned, and uses that entities spawn method to spawn it.
	 * @return	the spawned entity
	 */
	private CustomEntityWrapper spawnEntity()
	{
		Gamemode gm = gameHandler.getGameMode();
		
		int modifiedWave = wave;
		if(gm.isApocalypse())
			modifiedWave = getApocalypseWave();
		modifiedWave *= gm.getDifficultyModifier();
			
		CustomEntityWrapper customEnt = null;
		if(wolfWave && rnd.nextDouble() < plugin.getConfig().getDouble(Constants.WOLF_WAVE_PERCENT_SPAWN))
			customEnt = spawnWolf(modifiedWave);
		else if(skeletonWave && rnd.nextDouble() < plugin.getConfig().getDouble(Constants.SKELETON_WAVE_PERCENT_SPAWN))
			customEnt = spawnSkeleton(modifiedWave);
		else
		{
			if(rnd.nextDouble() < plugin.getConfig().getDouble(Constants.WOLF_PERCENT_SPAWN) && (wave > 1 || gm.isApocalypse()))
				customEnt = spawnWolf(modifiedWave);
			else if(rnd.nextDouble() < plugin.getConfig().getDouble(Constants.SKELETON_PERCENT_SPAWN) && (wave > 1 || gm.isApocalypse()))
				customEnt = spawnSkeleton(modifiedWave);
			else
				customEnt = spawnZombie(modifiedWave);
		}
		if(customEnt == null)	//The chunk might not be loaded, or the event might have been cancelled
			return null;
		customEnt.setMaxHealth((int) (health * (((EntityTypeConfiguration) customEnt.getType()).getHealthModifier())));
		customEnt.restoreHealth();
		toSpawn--;
		return customEnt;
	}
	
	/**
	 * Spawn a skeleton based on the wave and gamemode.
	 * @param wave	current wave, modified if the gamemode is particularly difficult
	 * @return	the spawned entity
	 */
	private CustomEntityWrapper spawnSkeleton(int wave)
	{
		SkeletonTypeConfiguration type = null;
		for(SkeletonTypeConfiguration skeletonType : skeletonTypes)
		{
			if(skeletonType.getMinimumSpawnWave() <= wave)
			{
				double chance = skeletonType.getSpawnChance() * wave;
				if(chance > .2)
					chance = .2;
				if(chance > rnd.nextDouble())
				{
					type = skeletonType;
					break;
				}
			}
		}
		if(type == null)
		{
			List<SkeletonTypeConfiguration> potentialEntities = new ArrayList<SkeletonTypeConfiguration>();
			for(SkeletonTypeConfiguration sType : skeletonTypes)
			{
				if(gameHandler.getGameMode().getDefaultSkeletons().contains(sType.getName()))
					potentialEntities.add(sType);
			}
			if(potentialEntities.size() > 0)
				type = potentialEntities.get(rnd.nextInt(potentialEntities.size()));
		}
		if(type == null)
			type = defaultSkeletonType;
		Location spawn = gameHandler.getLevel().getRandomZombieSpawn();
		CustomEntityWrapper customEnt = CustomSkeleton.spawn(spawn, type);
		return customEnt;
	}
	
	/**
	 * Spawn a wolf based on the wave and gamemode.
	 * @param wave	current wave, modified if the gamemode is particularly difficult
	 * @return	the spawned entity
	 */
	private CustomEntityWrapper spawnWolf(int wave)
	{
		EntityTypeConfiguration type = null;
		for(EntityTypeConfiguration wolfType : wolfTypes)
		{
			if(wolfType.getMinimumSpawnWave() <= wave)
			{
				double chance = wolfType.getSpawnChance() * wave;
				if(chance > .2)
					chance = .2;
				if(chance > rnd.nextDouble())
				{
					type = wolfType;
					break;
				}
			}
		}
		if(type == null)
		{
			List<EntityTypeConfiguration> potentialEntities = new ArrayList<EntityTypeConfiguration>();
			for(EntityTypeConfiguration wType : wolfTypes)
			{
				if(gameHandler.getGameMode().getDefaultWolves().contains(wType.getName()))
					potentialEntities.add(wType);
			}
			if(potentialEntities.size() > 0)
				type = potentialEntities.get(rnd.nextInt(potentialEntities.size()));
		}
		if(type == null)
			type = defaultWolfType;
		Location spawn = gameHandler.getLevel().getRandomZombieSpawn();
		CustomEntityWrapper customEnt = CustomWolf.spawn(spawn, type);
		return customEnt;
	}
	
	/**
	 * Spawn a zombie based on the wave and gamemode.
	 * @param wave	current wave, modified if the gamemode is particularly difficult
	 * @return	the spawned entity
	 */
	private CustomEntityWrapper spawnZombie(int wave)
	{
		EntityTypeConfiguration type = null;
		for(EntityTypeConfiguration zombieType : zombieTypes)
		{
			if(zombieType.getMinimumSpawnWave() <= wave)
			{
				double chance = zombieType.getSpawnChance() * wave;
				if(chance > .2)
					chance = .2;
				if(chance > rnd.nextDouble())
				{
					type = zombieType;
					break;
				}
			}
		}
		if(type == null)
		{
			List<EntityTypeConfiguration> potentialEntities = new ArrayList<EntityTypeConfiguration>();
			for(EntityTypeConfiguration zType : zombieTypes)
			{
				if(gameHandler.getGameMode().getDefaultZombies().contains(zType.getName()))
					potentialEntities.add(zType);
			}
			if(potentialEntities.size() > 0)
				type = potentialEntities.get(rnd.nextInt(potentialEntities.size()));
		}
		if(type == null)
			type = defaultZombieType;

		Location spawn = gameHandler.getLevel().getRandomZombieSpawn();
		if(spawn == null)
		{
			ChatHelper.broadcastMessage(ChatColor.RED+"Error: ZArena level has no zombie spawns. Stopping game.");
			gameHandler.stop();
			return null;
		}
		CustomEntityWrapper customEnt = CustomZombie.spawn(spawn, type);
		return customEnt;
	}

	/**
	 * Starts the wave
	 */
	private void startWave()
	{
		ChatHelper.broadcastMessage(String.format(ChatHelper.WAVE_START, wave, toSpawn, health), gameHandler.getBroadcastPlayers());
		for(Player p : gameHandler.getPlayers())
		{
			Gamemode gm = gameHandler.getGameMode();
			if(!gm.isNoRegen())
				p.setHealth(20);
		}
	}
	
	/**
	 * Checks to see if any entities have died.
	 */
	private void updateEntityList()
	{
		Stack<CustomEntityWrapper> toDelete = new Stack<CustomEntityWrapper>();
		for(CustomEntityWrapper e : entities)
		{
			if(!e.getEntity().isAlive())
				toDelete.add(e);
		}
		entities.removeAll(toDelete);
	}
}
