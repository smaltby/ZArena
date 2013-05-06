package com.github.zarena;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;

import com.github.customentitylibrary.entities.CustomEntityWrapper;
import com.github.customentitylibrary.entities.CustomPigZombie;
import com.github.customentitylibrary.entities.CustomSkeleton;
import com.github.customentitylibrary.entities.CustomWolf;
import com.github.customentitylibrary.entities.CustomZombie;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.zarena.entities.ZEntityType;
import com.github.zarena.events.GameStopCause;
import com.github.zarena.events.GameStopEvent;
import com.github.zarena.events.WaveChangeEvent;
import com.github.zarena.utils.ChatHelper;
import com.github.zarena.utils.Constants;
import com.github.zarena.utils.StringEnums;

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
	
	ZEntityType defaultZombieType;
	ZEntityType defaultSkeletonType;
	ZEntityType defaultWolfType;
	List<ZEntityType> zombieTypes = new ArrayList<ZEntityType>();
	List<ZEntityType> skeletonTypes = new ArrayList<ZEntityType>();
	List<ZEntityType> wolfTypes = new ArrayList<ZEntityType>();
	
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
	
	private double calcChance(int priority, int wave)
	{
		double reduce = 2 - .75 / (1 + Math.pow(Math.E, -(wave/3 - 3)));
		double chance = 1.0;
		for(int i = 0; i < priority; i++)
		{
			chance /= reduce;
		}
		return chance;
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
			ZArena.log(Level.WARNING, "Either Zombie Quantity or Zombie Health in the configuration is set incorrectly. Using default values instead until the" +
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
	
	private CustomEntityWrapper chooseEntity(int wave, List<ZEntityType> types, List<ZEntityType> defaultTypes)
	{
		//This mass of code randomly selects a list of entities to spawn
		List<ZEntityType> possibleTypes = new ArrayList<ZEntityType>();
		for(ZEntityType type : types)
		{
			if(type.getMinimumSpawnWave() <= wave)
			{
				double chance = calcChance(type.getSpawnPriority(), wave);
				if(rnd.nextDouble() < chance)
				{
					possibleTypes.add(type);
					break;
				}
			}
		}
		if(possibleTypes.isEmpty())
			possibleTypes.add(defaultTypes.get(rnd.nextInt(defaultTypes.size())));
		
		//Randomly select a type from all of the possible types to be spawned
		ZEntityType type = possibleTypes.get(rnd.nextInt(possibleTypes.size()));

		//And now spawn it
		Location spawn = gameHandler.getLevel().getRandomZombieSpawn();
		if(spawn == null)
		{
			ChatHelper.broadcastMessage(ChatColor.RED+"Error: ZArena level has no zombie spawns. Stopping game.");
			gameHandler.stop();
			return null;
		}
		CustomEntityWrapper customEnt;
		if(type.getPreferredType().equalsIgnoreCase("zombiepigman"))
		{
			CustomPigZombie pig = new CustomPigZombie(spawn.getWorld());
			pig.angerLevel = 1;
			customEnt = CustomEntityWrapper.spawnCustomEntity(pig, spawn, type);
		} else if(type.getPreferredType().equalsIgnoreCase("zombie"))
		{
			customEnt = CustomEntityWrapper.spawnCustomEntity(new CustomZombie(spawn.getWorld()), spawn, type);
		} else if(type.getPreferredType().equalsIgnoreCase("skeleton"))
		{
			customEnt = CustomEntityWrapper.spawnCustomEntity(new CustomSkeleton(spawn.getWorld()), spawn, type);
		} else if(type.getPreferredType().equalsIgnoreCase("wolf"))
		{
			CustomWolf wolf = new CustomWolf(spawn.getWorld());
			wolf.setAngry(true);
			customEnt = CustomEntityWrapper.spawnCustomEntity(wolf, spawn, type);
		} else
		{
			ZArena.log(Level.WARNING, "The type of the entity configuration called "+type.toString()+" does not correspond to spawnable entity.");
			return null;
		}
		return customEnt;
	}
	
	/**
	 * Get all entity types, including default types.
	 * @return	list of ZEntityType instances
	 */
	public List<ZEntityType> getAllEntityTypes()
	{
		List<ZEntityType> all = new ArrayList<ZEntityType>();
		//The below methods include default types, so we don't have to do that manually
		all.addAll(getSkeletonTypes());
		all.addAll(getWolfTypes());
		all.addAll(getZombieTypes());
		return all;
	}
	
	/**
	 * Get the the special apocalypse wave, which is used to determine the types of zombies that spawn during the
	 * technically waveless apocalypse mode.
	 * @return	number representing the internal wave used in calculations during apocalypse mode
	 */
	public int getApocalypseWave()
	{
		return (int) Math.ceil((Math.log(Math.pow((tickCount+1), 10)) + ((tickCount+1)/40))/30);	//Logarithmic function
	}
	
	/**
	 * Get the default skeleton type when it is not override by the current gamemode
	 * @return	type
	 */
	public ZEntityType getDefaultSkeletonType()
	{
		return defaultSkeletonType;
	}
	
	/**
	 * Get the default wolf type when it is not override by the current gamemode
	 * @return	type
	 */
	public ZEntityType getDefaultWolfType()
	{
		return defaultWolfType;
	}
	
	/**
	 * Get the default zombie type when it is not override by the current gamemode
	 * @return	type
	 */
	public ZEntityType getDefaultZombieType()
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
	
	/**
	 * Get a list of all skeleton types that may be chosen when a skeleton spawns, assuming the current gamemode doesn't block it.
	 * Includes default type.
	 * @return	list of types
	 */
	public List<ZEntityType> getSkeletonTypes()
	{
		List<ZEntityType> types = new ArrayList<ZEntityType>(skeletonTypes);
		types.add(defaultSkeletonType);
		return types;
	}
	
	/**
	 * Get a list of all wolf types that may be chosen when a wolf spawns, assuming the current gamemode doesn't block it.
	 * Includes default type.
	 * @return	list of types
	 */
	public List<ZEntityType> getWolfTypes()
	{
		List<ZEntityType> types = new ArrayList<ZEntityType>(wolfTypes);
		types.add(defaultWolfType);
		return types;
	}
	
	/**
	 * Get a list of all zombie types that may be chosen when a zombie spawns, assuming the current gamemode doesn't block it.
	 * Includes default type.
	 * @return	list of types
	 */
	public List<ZEntityType> getZombieTypes()
	{
		List<ZEntityType> types = new ArrayList<ZEntityType>(zombieTypes);
		types.add(defaultZombieType);
		return types;
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
		//There is a depressing amount of nested if statements in this method
		//I don't feel like doing anything about it, though...
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
				spawnEntity();
				//Every second, update the player list and entity list, and check if we should start the next wave
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
				//Every five seconds, regenerate players healths if the gamemode says we should, and reset the wave settings if
				//we're in an apocalypse
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

				//Ever 4 minutes, set the time to the beginning of night so we never end up in daytime (assuming the config says to do this)
				if(tickCount % 4800 == 0)
					Bukkit.getServer().getWorld(plugin.getConfig().getString(Constants.GAME_WORLD)).setTime(38000);
				tickCount++;
			}
			//Is the alive count less than 0? Yes? Well then end the bloody game!
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
	
	/**
	 * Sets the wave manually to a specified number. May cause unforseen errors that aren't caused by using the
	 * internal increment wave method.
	 * @param wave	wave
	 */
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
			toSpawn *= 1.5/(1 + Math.pow(Math.E, gameHandler.getAliveCount()/-3) + .25);
		
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
			zombieSpawnChance /= 2;	//Lessen the spawn rate a bit for apocalypse, so you don't get overrun TOO quickly
	}
	
	/**
	 * Set the wave settings for this particular wave.
	 */
	private void setWaveSettings()
	{
		this.setWaveSettings(true);
	}
	
	/**
	 * Decides which type of entity should be spawned, and spawns it.
	 */
	private void spawnEntity()
	{
		//Decide of we should be spawning an entity
		Gamemode gm = gameHandler.getGameMode();
		if(toSpawn <= 0 && !gm.isApocalypse())
			return;
		if(rnd.nextDouble() > zombieSpawnChance)
			return;
		
		//Get the modified wave, based on gamemode
		int modifiedWave = wave;
		if(gm.isApocalypse())
			modifiedWave = getApocalypseWave();
		modifiedWave *= gm.getDifficultyModifier();
		
		//Decide what kind of entity to spawn. If it's a wolf wave/skeleton wave, spawn based on that. Else, spawn normally.
//		List<ZEntityType> defaultSkeletons = (gm.getDefaultSkeletons().isEmpty()) ? new ArrayList<ZEntityType>(Arrays.asList(defaultSkeletonType)) : gm.getDefaultSkeletons();
//		List<ZEntityType> defaultWolves = (gm.getDefaultWolves().isEmpty()) ? new ArrayList<ZEntityType>(Arrays.asList(defaultWolfType)) : gm.getDefaultWolves();
//		List<ZEntityType> defaultZombies = (gm.getDefaultZombies().isEmpty()) ? new ArrayList<ZEntityType>(Arrays.asList(defaultZombieType)) : gm.getDefaultZombies();
		CustomEntityWrapper customEnt = null;
		if(wolfWave && rnd.nextDouble() < plugin.getConfig().getDouble(Constants.WOLF_WAVE_PERCENT_SPAWN))
			customEnt = chooseEntity(modifiedWave, wolfTypes, gm.getDefaultWolves());
		else if(skeletonWave && rnd.nextDouble() < plugin.getConfig().getDouble(Constants.SKELETON_WAVE_PERCENT_SPAWN))
			customEnt = chooseEntity(modifiedWave, skeletonTypes, gm.getDefaultSkeletons());
		else
		{
			if(rnd.nextDouble() < plugin.getConfig().getDouble(Constants.WOLF_PERCENT_SPAWN) && (wave > 1 || gm.isApocalypse()))
				customEnt = chooseEntity(modifiedWave, wolfTypes, gm.getDefaultWolves());
			else if(rnd.nextDouble() < plugin.getConfig().getDouble(Constants.SKELETON_PERCENT_SPAWN) && (wave > 1 || gm.isApocalypse()))
				customEnt = chooseEntity(modifiedWave, skeletonTypes, gm.getDefaultSkeletons());
			else
				customEnt = chooseEntity(modifiedWave, zombieTypes, gm.getDefaultZombies());
		}
		
		if(customEnt == null)	//The chunk might not be loaded, or the event might have been cancelled
			return;
			
		//Set the entities health, decrement the toSpawn count, and add the entity to the list of entities.
		//Note that the entity was already spawned in one of the chooseEntity methods above.
		customEnt.setMaxHealth((int) (health * (((ZEntityType) customEnt.getType()).getHealthModifier())));
		customEnt.restoreHealth();
		toSpawn--;
		entities.add(customEnt);
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
