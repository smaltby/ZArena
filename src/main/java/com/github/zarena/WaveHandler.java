package com.github.zarena;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.github.achievementsx.AchievementsAPI;
import com.github.customentitylibrary.entities.CustomEntityWrapper;

import com.github.customentitylibrary.entities.CustomPigZombie;
import com.github.zarena.events.*;
import com.github.zarena.utils.*;
import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;
import net.minecraft.server.v1_7_R1.EntitySkeleton;
import net.minecraft.server.v1_7_R1.EntityWolf;
import net.minecraft.server.v1_7_R1.EntityZombie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.github.zarena.entities.ZEntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class WaveHandler implements Runnable, Listener
{
	private ZArena plugin;
	private GameHandler gameHandler;

	private double spawnChance;

	private int timeUntilNextWave;
	private Random rnd;
	private int wave;
	private int toSpawn;
	private int health;
	private List<CustomEntityWrapper> entities;
	private boolean wolfWave;
	private boolean skeletonWave;
	private int lastWolfWave;
	private int lastSkeletonWave;

	ZEntityType defaultZombieType;
	ZEntityType defaultSkeletonType;
	ZEntityType defaultWolfType;
	private List<ZEntityType> zombieTypes = new ArrayList<ZEntityType>();
	private List<ZEntityType> skeletonTypes = new ArrayList<ZEntityType>();
	private List<ZEntityType> wolfTypes = new ArrayList<ZEntityType>();

	private int taskID = -1;
	private int seconds;
	private int secondsWithFewEntities = 0;

	public WaveHandler(GameHandler instance)
	{
		plugin = ZArena.getInstance();
		Bukkit.getPluginManager().registerEvents(this, plugin);
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

	private void attemptSpawnEntity()
	{
		//Decide of we should be spawning an entity
		Gamemode gm = gameHandler.getGameMode();
		if(toSpawn <= 0 && !gm.isApocalypse())
			return;
		if(rnd.nextDouble() > (gm.isApocalypse() ? spawnChance /2 : spawnChance))
			return;
		if(entities.size() >= plugin.getConfig().getInt(ConfigEnum.MOB_CAP.toString()))
			return;

		//Get the modified wave, based on gamemode
		int modifiedWave = wave;
		if(gm.isApocalypse())
			modifiedWave = getApocalypseWave();
		modifiedWave *= gm.getDifficultyModifier();

		//Decide what kind of entity to spawn. If it's a wolf wave/skeleton wave, spawn based on that. Else, spawn normally.
		CustomEntityWrapper customEnt;
		if(wolfWave && rnd.nextDouble() < plugin.getConfig().getDouble(ConfigEnum.WOLF_WAVE_PERCENT_SPAWN.toString()))
			customEnt = chooseEntity(modifiedWave, wolfTypes, gm.getDefaultWolves());
		else if(skeletonWave && rnd.nextDouble() < plugin.getConfig().getDouble(ConfigEnum.SKELETON_WAVE_PERCENT_SPAWN.toString()))
			customEnt = chooseEntity(modifiedWave, skeletonTypes, gm.getDefaultSkeletons());
		else
		{
			if(rnd.nextDouble() < plugin.getConfig().getDouble(ConfigEnum.WOLF_PERCENT_SPAWN.toString()) && (wave > 1 || gm.isApocalypse()))
				customEnt = chooseEntity(modifiedWave, wolfTypes, gm.getDefaultWolves());
			else if(rnd.nextDouble() < plugin.getConfig().getDouble(ConfigEnum.SKELETON_PERCENT_SPAWN.toString()) && (wave > 1 || gm.isApocalypse()))
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
		FileConfiguration c = plugin.getConfig();
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
		FileConfiguration c = plugin.getConfig();
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

	public double calcSpawnChance(int wave)
	{
		try
		{
			String formula = plugin.getConfig().getString(ConfigEnum.SPAWN_CHANCE.toString());
			Calculable calc = new ExpressionBuilder(formula)
					.withVariable("x",wave)
					.build();
			return calc.calculate();
		} catch(UnknownFunctionException e)
		{
			ZArena.log(Level.WARNING, e.getMessage() + " in the custom formula for Spawn Chance, defined in the config.yml");
		} catch(UnparsableExpressionException e)
		{
			ZArena.log(Level.WARNING, e.getMessage() + " in the custom formula for Spawn Chance, defined in the config.yml");
		}
		return 0.05;
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
			if(t.getMinimumSpawnWave() <= wave && t.getMaximumSpawnWave() >= wave)
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
		net.minecraft.server.v1_7_R1.World nmsWorld = ((CraftWorld)spawn.getWorld()).getHandle();
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
		ChatHelper.broadcastMessage(type.getBroadcastOnSpawn(), gameHandler.getBroadcastPlayers());
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
		return (int) Math.ceil((Math.log(Math.pow((seconds+1)/20, 10)) + ((seconds+1)))/30);	//Logarithmic function
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
		return seconds;
	}

	public int getRemainingZombies()
	{
		return entities.size() + toSpawn;
	}

	public double getSpawnChance()
	{
		return spawnChance;
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

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		//Resets secondsWithFewEntities if one of the few remaining entities damages something, or gets damaged.
		//This is so the wave doesn't randomly end while the entities are still fighting, which may happen if one
		//of the remaining entities is a boss and can't be killed in merely 60 seconds
		CustomEntityWrapper entity;
		if(CustomEntityWrapper.instanceOf(event.getEntity()))
			entity = CustomEntityWrapper.getCustomEntity(event.getEntity());
		else if(CustomEntityWrapper.instanceOf(event.getDamager()))
			entity = CustomEntityWrapper.getCustomEntity(event.getDamager());
		else if(event.getDamager() instanceof Projectile)
		{
			LivingEntity shooter = ((Projectile) event.getDamager()).getShooter();
			if(shooter != null && CustomEntityWrapper.instanceOf(shooter))
				entity = CustomEntityWrapper.getCustomEntity(shooter);
			else
				return;
		} else
			return;
		if(entities.contains(entity))
			secondsWithFewEntities = 0;
	}

	private void prepareNextWave()
	{
		Gamemode gm = gameHandler.getGameMode();

		timeUntilNextWave = plugin.getConfig().getInt(ConfigEnum.WAVE_DELAY.toString());
		secondsWithFewEntities = 0;
		ChatHelper.broadcastMessage(Message.WAVE_START_IN.formatMessage(wave, timeUntilNextWave), gameHandler.getBroadcastPlayers());

		//Calculate the waves settings
		toSpawn = calcQuantity(wave);
		health = calcHealth(wave);
		health *= gm.getHealthModifier();
		toSpawn *= gm.getZombieAmountModifier();
		spawnChance = calcSpawnChance(wave);
		if(plugin.getConfig().getBoolean(ConfigEnum.QUANTITY_ADJUST.toString()))
			toSpawn *= 1.5/(1 + Math.pow(Math.E, gameHandler.getAliveCount()/-3) + .25);

		if(!gm.isApocalypse())
		{
			//Do calculations required for wolf and skeleton waves
			lastSkeletonWave++;
			lastWolfWave++;
			if(skeletonWave)
				skeletonWave = false;
			if(wolfWave)
				wolfWave = false;
			else
			{
				if(rnd.nextDouble() < plugin.getConfig().getDouble(ConfigEnum.WOLF_WAVE_PERCENT_OCCUR.toString()) && lastWolfWave > 4)
				{
					wolfWave = true;
					lastWolfWave = 0;
					ChatHelper.broadcastMessage(Message.WOLF_WAVE_APPROACHING.formatMessage());
				}
				else if(rnd.nextDouble() < plugin.getConfig().getDouble(ConfigEnum.SKELETON_WAVE_PERCENT_OCCUR.toString()) && lastSkeletonWave > 4)
				{
					skeletonWave = true;
					lastSkeletonWave = 0;
					ChatHelper.broadcastMessage(Message.SKELETON_WAVE_APPROACHING.formatMessage());
				}
			}
			//Respawn players, or inform them of when they will respawn, if applicable
			if(plugin.getConfig().getInt(ConfigEnum.RESPAWN_EVERY_WAVES.toString()) > 0)
			{
				for(PlayerStats stats : gameHandler.getPlayerStats().values())
				{
					if(!stats.isAlive())
					{
						int respawnEveryWaves = plugin.getConfig().getInt(ConfigEnum.RESPAWN_EVERY_WAVES.toString());
						if(stats.getWavesSinceDeath() >= respawnEveryWaves)
						{
							PlayerRespawnInGameEvent event = new PlayerRespawnInGameEvent(stats.getPlayer(), gameHandler.getStartItems(), PlayerRespawnCause.RESPAWN_WAVE);
							Bukkit.getPluginManager().callEvent(event);
							if(!event.isCancelled())
								gameHandler.respawnPlayer(stats.getPlayer(), event.getStartItems());
						} else
						{
							ChatHelper.sendMessage(Message.RESPAWN_IN_WAVES.formatMessage(stats.getPlayer().getName(),
									respawnEveryWaves - stats.getWavesSinceDeath()), stats.getPlayer());
						}
					}
				}
			}
		}
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
				timeUntilNextWave--;
				if(timeUntilNextWave <= 0)
					startWave();
			}
			else
			{
				//Hack to fix the spawn rate after I changed this run method to be called every second, as opposed to every minute
				//Will be made less suckish when I add configuration options for spawn rate in the config
				for(int i = 0; i < 20; i++)
					attemptSpawnEntity();
				updateEntityList();
				if(checkNextWave())
					setWave(wave + 1);

				//Often, entities get stuck. If there are only a few entities left, and players alive don't deal any damage
				//in the past minute, we can assume an entity is stuck (or the players left alive are pansies and are avoding them)
				//Note that the secondsWithFewEntities is reset when one of the entities is damaged, or deals damage
				if(plugin.getConfig().getBoolean(ConfigEnum.NEXT_WAVE_IF_ENTITY_STUCK.toString()))
				{
					if(entities.size() <= 3)
						secondsWithFewEntities++;
					if(secondsWithFewEntities > 60)
						setWave(wave + 1);
				}
				//Every five seconds, regenerate players healths if the gamemode says we should, reset the wave settings if
				//we're in an apocalypse, and update achievements if enabled
				if(seconds % 5 == 0)
				{
					if(gameHandler.getGameMode().isApocalypse())
					{
						health = calcHealth(getApocalypseWave());
						//Update achievements data, if enabled
						if(plugin.isAchievementsEnabled())
						{
							for(PlayerStats stats : gameHandler.getPlayerStats().values())
							{
								if(!stats.isAlive())
									continue;
								String playerName = stats.getPlayer().getName();
								String pluginName = "ZArena";
								if(gameHandler.getGameMode().isApocalypse())
								{
									if(!stats.hasDied())
									{
										AchievementsAPI.setDataIfMax(playerName, pluginName, "highestTimeInMap:" + gameHandler.getLevel().getName(), getGameLength());
										AchievementsAPI.setDataIfMax(playerName, pluginName, "highestTimeInGamemode:"+gameHandler.getGameMode().getName(), getGameLength());
										AchievementsAPI.setDataIfMax(playerName, pluginName, "highestTime", getGameLength());
									}
								}
							}
						}
					}
					if(gameHandler.getGameMode().isSlowRegen())
					{
						for(Player player : gameHandler.getPlayers())
						{
							double health = player.getHealth() + 1;
							if(health > player.getMaxHealth())
								health = player.getMaxHealth();
							player.setHealth(health);
							((CraftPlayer) player).getHandle().triggerHealthUpdate();
						}
					}
				}
			}
			//Ever minute, set the time to the beginning of night so we never end up in daytime (assuming the config says to do this)
			if(seconds % 60 == 0)
				Bukkit.getServer().getWorld(gameHandler.getLevel().getWorld()).setTime(39000);

			//Sometimes, players escape the server without setting off any listeners...
			for(String pName : gameHandler.getPlayerNames())
			{
				Player p = Bukkit.getPlayer(pName);
				if(p == null || !p.isOnline())
					gameHandler.removePlayer(pName);
			}

			//Is the alive count equal to 0? Yes? Well then end the bloody game!
			if(gameHandler.getAliveCount() <= 0)
			{
				gameHandler.stop();
				GameStopEvent event = new GameStopEvent(GameStopCause.ALL_DEAD);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if(plugin.getConfig().getBoolean(ConfigEnum.AUTORUN.toString()))
					gameHandler.getLevelVoter().start();
			}

			//If the config has it so players respawn after a set amount of minutes...then respawn players after a
			//set amount of minutes!
			if(plugin.getConfig().getInt(ConfigEnum.RESPAWN_EVERY_TIME.toString()) > 0)
			{
				for(PlayerStats stats : gameHandler.getPlayerStats().values())
				{
					if(!stats.isAlive())
					{
						if(stats.getTimeSinceDeath() >= plugin.getConfig().getInt(ConfigEnum.RESPAWN_EVERY_TIME.toString()) * 60)
						{
							PlayerRespawnInGameEvent event = new PlayerRespawnInGameEvent(stats.getPlayer(), gameHandler.getStartItems(), PlayerRespawnCause.RESPAWN_TIME);
							Bukkit.getPluginManager().callEvent(event);
							if(!event.isCancelled())
								gameHandler.respawnPlayer(stats.getPlayer(), event.getStartItems());
						} else if(stats.getTimeSinceDeath() % plugin.getConfig().getInt(ConfigEnum.RESPAWN_REMINDER_DELAY.toString()) == 0)
						{
							int secondsUntilSpawn = plugin.getConfig().getInt(ConfigEnum.RESPAWN_EVERY_TIME.toString()) * 60 - stats.getTimeSinceDeath();
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
			seconds++;
		}
	}

	/**
	 * Sets the wave. Make cause unforseen errors if not called internally.
	 * @param wave	wave
	 */
	public void setWave(int wave)
	{
		int previousWave = this.wave;
		this.wave = wave;
		prepareNextWave();

		WaveChangeEvent event = new WaveChangeEvent(previousWave, wave, timeUntilNextWave);
		Bukkit.getPluginManager().callEvent(event);
		timeUntilNextWave = event.getSecondsUntilStart();
		this.wave = event.getNewWave();
	}

	public void start()
	{
		wave = gameHandler.getGameMode().getInitialWave();
		seconds = 0;
		lastWolfWave = 0;
		lastSkeletonWave = 0;
		entities.clear();
		prepareNextWave();
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(ZArena.getInstance(), this, 0L, 20L);
	}

	/**
	 * Starts the wave
	 */
	private void startWave()
	{
		ChatHelper.broadcastMessage(Message.WAVE_START.formatMessage(wave, toSpawn, health), gameHandler.getBroadcastPlayers());
		for(Player p : gameHandler.getPlayers())
		{
			Gamemode gm = gameHandler.getGameMode();
			if(!gm.isNoRegen())
			{
				p.setHealth(p.getMaxHealth());
				((CraftPlayer) p).getHandle().triggerHealthUpdate();
			}
		}
		//Teleport enemies that were stuck in the last wave to a zombie spawn
		for(CustomEntityWrapper entity : entities) entity.getEntity().getBukkitEntity().teleport(gameHandler.getLevel().getRandomZombieSpawn());

		//Call event
		WaveStartEvent event = new WaveStartEvent(wave);
		Bukkit.getPluginManager().callEvent(event);

		//Update achievements data, if enabled
		if(plugin.isAchievementsEnabled())
		{
			for(PlayerStats stats : gameHandler.getPlayerStats().values())
			{
				if(!stats.isAlive())
					continue;
				String playerName = stats.getPlayer().getName();
				String pluginName = "ZArena";
				if(!gameHandler.getGameMode().isApocalypse())
				{
					AchievementsAPI.incrementData(playerName, pluginName, "wavesInMap:"+gameHandler.getLevel().getName(), 1);
					AchievementsAPI.incrementData(playerName, pluginName, "wavesInGamemode:"+gameHandler.getGameMode().getName(), 1);
					AchievementsAPI.incrementData(playerName, pluginName, "waves", 1);
					//Only do these stats if the player hasn't died because it's unfair if the player respawns on wave 20 or something, lives for 20 seconds,
					//but still gets his/her highest wave set to 20
					if(!stats.hasDied())
					{
						AchievementsAPI.setDataIfMax(playerName, pluginName, "highestWaveInMap:"+gameHandler.getLevel().getName(), getWave());
						AchievementsAPI.setDataIfMax(playerName, pluginName, "highestWaveInGamemode:"+gameHandler.getGameMode().getName(), getWave());
						AchievementsAPI.setDataIfMax(playerName, pluginName, "highestWave", getWave());
					}
				}
			}
		}
	}

	public void stop()
	{
		if(taskID != -1)
			Bukkit.getScheduler().cancelTask(taskID);

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
