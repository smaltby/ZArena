package com.github.zarena;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.github.customentitylibrary.entities.CustomEntityWrapper;

import com.github.customentitylibrary.entities.CustomPigZombie;
import com.github.zarena.utils.*;
import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;
import net.minecraft.server.v1_6_R2.EntitySkeleton;
import net.minecraft.server.v1_6_R2.EntityWolf;
import net.minecraft.server.v1_6_R2.EntityZombie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.entity.Player;

import com.github.zarena.entities.ZEntityType;
import com.github.zarena.events.GameStopCause;
import com.github.zarena.events.GameStopEvent;
import com.github.zarena.events.WaveChangeEvent;

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

	public void addType(ZEntityType type)
	{
		switch(StringEnums.valueOf(type.getPreferredType().toUpperCase()))
		{
			case ZOMBIE: case ZOMBIEPIGMAN:
				zombieTypes.add(type);
				break;
			case SKELETON:
				skeletonTypes.add(type);
				break;
			case WOLF:
				wolfTypes.add(type);
				break;
			default:
		}
	}

	private double calcChance(double priority, int wave)
	{
		double reduce = 2 - .75 / (1 + Math.pow(Math.E, -(wave/3 - 3)));
		double chance = .5;
		for(int i = 0; i < priority; i++)
		{
			chance /= reduce;
		}
		//Get the prev chance, and interpolate between it and the current chance. This helps account for priorities that aren't exact integers
		double prevChance = chance * reduce;
		double inbetween = Math.ceil(priority) - priority;
		return chance * (1 - inbetween) + prevChance * inbetween;
	}

	public int calcHealth(int wave)
	{
		Configuration c = plugin.getConfiguration();
		try
		{
			return calcInternal(wave, c.getDouble(ConfigEnum.HEALTH_STARTING.toString()), c.getDouble(ConfigEnum.HEALTH_INCREASE.toString()),
					c.getDouble(ConfigEnum.HEALTH_EXPOTENTIAL_INCREASE.toString()), c.getInt(ConfigEnum.HEALTH_LIMIT.toString()),
					c.getBoolean(ConfigEnum.HEALTH_SOFT_LIMIT.toString()), c.getString(ConfigEnum.HEALTH_FORMULA.toString()));
		} catch(UnknownFunctionException e)
		{
			ZArena.log(Level.WARNING, e.getMessage() + " in the custom formula for Health, defined in the config.yml");
		} catch(UnparsableExpressionException e)
		{
			ZArena.log(Level.WARNING, e.getMessage() + " in the custom formula for Health, defined in the config.yml");
		}
		//If we get here, the custom formula screwed up, and the user has been warned of such. Now just calc the health without any custom formula
		try
		{
			return calcInternal(wave, c.getDouble(ConfigEnum.HEALTH_STARTING.toString()), c.getDouble(ConfigEnum.HEALTH_INCREASE.toString()),
					c.getDouble(ConfigEnum.HEALTH_EXPOTENTIAL_INCREASE.toString()), c.getInt(ConfigEnum.HEALTH_LIMIT.toString()),
					c.getBoolean(ConfigEnum.HEALTH_SOFT_LIMIT.toString()), "");
		} catch(Exception e)
		{
			//Shouldn't be possible to get here
			e.printStackTrace();
			return 0;
		}
	}

	private int calcInternal(int wave, double starting, double increasePerWave, double expotentialIncreasePerWave, int limit,
							 boolean softLimit, String customFormula) throws UnparsableExpressionException, UnknownFunctionException
	{
		if(customFormula != null && !customFormula.isEmpty())
		{
			Calculable calc = new ExpressionBuilder(customFormula)
					.withVariable("x",wave)
					.build();
			return (int) calc.calculate();
		} else
		{
			int base = (int) (Math.pow(wave, expotentialIncreasePerWave) + wave * increasePerWave + starting);
			if(base > limit && limit > 0)
			{
				if(softLimit)
					return base + (base - limit) / 10;
				else
					return limit;
			} else
				return base;
		}
	}

	public int calcQuantity(int wave)
	{
		Configuration c = plugin.getConfiguration();
		try
		{
			return calcInternal(wave, c.getDouble(ConfigEnum.QUANTITY_STARTING.toString()), c.getDouble(ConfigEnum.QUANTITY_INCREASE.toString()),
					c.getDouble(ConfigEnum.QUANTITY_EXPOTENTIAL_INCREASE.toString()), c.getInt(ConfigEnum.QUANTITY_LIMIT.toString()),
					c.getBoolean(ConfigEnum.QUANTITY_SOFT_LIMIT.toString()), c.getString(ConfigEnum.QUANTITY_FORMULA.toString()));
		} catch(UnknownFunctionException e)
		{
			ZArena.log(Level.WARNING, e.getMessage() + " in the custom formula for Quantity, defined in the config.yml");
		} catch(UnparsableExpressionException e)
		{
			ZArena.log(Level.WARNING, e.getMessage() + " in the custom formula for Quantity, defined in the config.yml");
		}
		//If we get here, the custom formula screwed up, and the user has been warned of such. Now just calc the health without any custom formula
		try
		{
			return calcInternal(wave, c.getDouble(ConfigEnum.QUANTITY_STARTING.toString()), c.getDouble(ConfigEnum.QUANTITY_INCREASE.toString()),
					c.getDouble(ConfigEnum.QUANTITY_EXPOTENTIAL_INCREASE.toString()), c.getInt(ConfigEnum.QUANTITY_LIMIT.toString()),
					c.getBoolean(ConfigEnum.QUANTITY_SOFT_LIMIT.toString()), "");
		} catch(Exception e)
		{
			//Shouldn't be possible to get here
			e.printStackTrace();
			return 0;
		}
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
		//This mass of code randomly selects an entity to spawn
		ZEntityType type = null;
		for(ZEntityType t : types)
		{
			if(t.getMinimumSpawnWave() <= wave)
			{
				double chance = calcChance(t.getSpawnPriority(), wave);
				if(rnd.nextDouble() < chance)
				{
					if(type == null || t.getSpawnPriority() > type.getSpawnPriority())
						type = t;
				}
			}
		}
		if(type == null)
			type = defaultTypes.get(rnd.nextInt(defaultTypes.size()));

		//And now spawn it
		Location spawn = gameHandler.getLevel().getRandomZombieSpawn();
		if(spawn == null)
		{
			ChatHelper.broadcastMessage(Message.NO_ZOMBIE_SPAWNS.formatMessage());
			gameHandler.stop();
			return null;
		}
		net.minecraft.server.v1_6_R2.World nmsWorld = ((CraftWorld)spawn.getWorld()).getHandle();
		CustomEntityWrapper customEnt;
		if(type.getPreferredType().equalsIgnoreCase("zombiepigman"))
		{
			CustomPigZombie pig = new CustomPigZombie(nmsWorld);
			pig.angerLevel = 1;
			customEnt = CustomEntityWrapper.spawnCustomEntity(pig, spawn, type);
		} else if(type.getPreferredType().equalsIgnoreCase("zombie"))
		{
			customEnt = CustomEntityWrapper.spawnCustomEntity(new EntityZombie(nmsWorld), spawn, type);
		} else if(type.getPreferredType().equalsIgnoreCase("skeleton"))
		{
			customEnt = CustomEntityWrapper.spawnCustomEntity(new EntitySkeleton(nmsWorld), spawn, type);
		} else if(type.getPreferredType().equalsIgnoreCase("wolf"))
		{
			EntityWolf wolf = new EntityWolf(nmsWorld);
			wolf.setAngry(true);
			customEnt = CustomEntityWrapper.spawnCustomEntity(wolf, spawn, type);
		} else
		{
			ZArena.log(Level.WARNING, "The type of the entity configuration called "+type.getName()+" does not correspond to spawnable entity.");
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
				timeUntilNextWave -= 0.05;
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
							double health = player.getHealth() + 1;
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
					Bukkit.getServer().getWorld(gameHandler.getLevel().getWorld()).setTime(38000);
				tickCount++;
			}
			//Is the alive count less than 0? Yes? Well then end the bloody game!
			if(gameHandler.getAliveCount() <= 0)
			{
				GameStopEvent event = new GameStopEvent(GameStopCause.ALL_DEAD);
				Bukkit.getServer().getPluginManager().callEvent(event);
				gameHandler.stop();
				if(plugin.getConfiguration().getBoolean(ConfigEnum.AUTORUN.toString()))
					gameHandler.getLevelVoter().startVoting();
			}
			//If the config has it so players respawn after a set amount of minutes...then respawn players after a
			//set amount of minutes!
			if(tickCount % 20 == 0)
			{
				if(plugin.getConfiguration().getInt(ConfigEnum.RESPAWN_EVERY_TIME.toString()) > 0)
				{
					for(PlayerStats stats : gameHandler.getPlayerStats().values())
					{
						if(!stats.isAlive())
						{
							if(stats.getTimeSinceDeath() >= plugin.getConfiguration().getInt(ConfigEnum.RESPAWN_EVERY_TIME.toString()) * 60)
								gameHandler.respawnPlayer(stats.getPlayer());
							if(stats.getTimeSinceDeath() >= plugin.getConfiguration().getInt(ConfigEnum.RESPAWN_EVERY_TIME.toString()) * 60)
								gameHandler.respawnPlayer(stats.getPlayer());
							else if(stats.getTimeSinceDeath() % plugin.getConfiguration().getInt(ConfigEnum.RESPAWN_REMINDER_DELAY.toString()) == 0)
							{
								int secondsUntilSpawn = plugin.getConfiguration().getInt(ConfigEnum.RESPAWN_EVERY_TIME.toString()) * 60 - stats.getTimeSinceDeath();
								int minutesUntilSpawn = (int)TimeUnit.SECONDS.toMinutes(secondsUntilSpawn);
								secondsUntilSpawn = secondsUntilSpawn % 60;
								String message = "";
								if(minutesUntilSpawn != 0) message += minutesUntilSpawn + "min ";
								if(secondsUntilSpawn != 0) message += secondsUntilSpawn + "sec ";
								if(!message.isEmpty())
									ChatHelper.sendMessage(Message.RESPAWN_IN_TIME.formatMessage(stats.getPlayer().getName(),
										message), stats.getPlayer());
							}
						}
					}
				}
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
			timeUntilNextWave = plugin.getConfiguration().getInt(ConfigEnum.WAVE_DELAY.toString());
			ChatHelper.broadcastMessage(Message.WAVE_START_IN.formatMessage(modifiedWave, timeUntilNextWave), gameHandler.getBroadcastPlayers());
		}
		//Calculate the waves settings
		toSpawn = calcQuantity(wave);
		health = calcHealth(wave);
		health *= gm.getHealthModifier();
		toSpawn *= gm.getZombieAmountModifier();
		if(plugin.getConfiguration().getBoolean(ConfigEnum.QUANTITY_ADJUST.toString()))
			toSpawn *= 1.5/(1 + Math.pow(Math.E, gameHandler.getAliveCount()/-3) + .25);

		//Do what's required when a new waves occurs (which never happens in apocaylpse mode)
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
				if(rnd.nextDouble() < plugin.getConfiguration().getDouble(ConfigEnum.WOLF_WAVE_PERCENT_OCCUR.toString()) && lastWolfWave > 4)
				{
					wolfWave = true;
					lastWolfWave = 0;
					ChatHelper.broadcastMessage(Message.WOLF_WAVE_APPROACHING.formatMessage());
				}
				else if(rnd.nextDouble() < plugin.getConfiguration().getDouble(ConfigEnum.SKELETON_WAVE_PERCENT_OCCUR.toString()) && lastSkeletonWave > 4)
				{
					skeletonWave = true;
					lastSkeletonWave = 0;
					ChatHelper.broadcastMessage(Message.SKELETON_WAVE_APPROACHING.formatMessage());
				}
			}
			//Respawn players, or inform them of when they will respawn, if applicable
			if(plugin.getConfiguration().getInt(ConfigEnum.RESPAWN_EVERY_WAVES.toString()) > 0)
			{
				for(PlayerStats stats : gameHandler.getPlayerStats().values())
				{
					if(!stats.isAlive())
					{
						int respawnEveryWaves = plugin.getConfiguration().getInt(ConfigEnum.RESPAWN_EVERY_WAVES.toString());
						if(stats.getWavesSinceDeath() >= respawnEveryWaves)
							gameHandler.respawnPlayer(stats.getPlayer());
						else
						{
							ChatHelper.sendMessage(Message.RESPAWN_IN_WAVES.formatMessage(stats.getPlayer().getName(),
										respawnEveryWaves - stats.getWavesSinceDeath()), stats.getPlayer());
						}
					}
				}
			}
		}
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
		if(rnd.nextDouble() > (gm.isApocalypse() ? 0.025 : 0.05))
			return;
		if(entities.size() >= plugin.getConfiguration().getInt(ConfigEnum.MOB_CAP.toString()))
			return;

		//Get the modified wave, based on gamemode
		int modifiedWave = wave;
		if(gm.isApocalypse())
			modifiedWave = getApocalypseWave();
		modifiedWave *= gm.getDifficultyModifier();

		//Decide what kind of entity to spawn. If it's a wolf wave/skeleton wave, spawn based on that. Else, spawn normally.
		CustomEntityWrapper customEnt;
		if(wolfWave && rnd.nextDouble() < plugin.getConfiguration().getDouble(ConfigEnum.WOLF_WAVE_PERCENT_SPAWN.toString()))
			customEnt = chooseEntity(modifiedWave, wolfTypes, gm.getDefaultWolves());
		else if(skeletonWave && rnd.nextDouble() < plugin.getConfiguration().getDouble(ConfigEnum.SKELETON_WAVE_PERCENT_SPAWN.toString()))
			customEnt = chooseEntity(modifiedWave, skeletonTypes, gm.getDefaultSkeletons());
		else
		{
			if(rnd.nextDouble() < plugin.getConfiguration().getDouble(ConfigEnum.WOLF_PERCENT_SPAWN.toString()) && (wave > 1 || gm.isApocalypse()))
				customEnt = chooseEntity(modifiedWave, wolfTypes, gm.getDefaultWolves());
			else if(rnd.nextDouble() < plugin.getConfiguration().getDouble(ConfigEnum.SKELETON_PERCENT_SPAWN.toString()) && (wave > 1 || gm.isApocalypse()))
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
		if(wave == 1 && gameHandler.getGameMode().getInitialWave() != 1)
		{
			wave = gameHandler.getGameMode().getInitialWave();
			setWaveSettings(false);
		}
		ChatHelper.broadcastMessage(Message.WAVE_START.formatMessage(wave, toSpawn, health), gameHandler.getBroadcastPlayers());
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
