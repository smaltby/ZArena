package kabbage.zarena;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kabbage.customentitylibrary.CustomEntityLibrary;
import kabbage.zarena.commands.DSpawnCommands;
import kabbage.zarena.commands.ISpawnCommands;
import kabbage.zarena.commands.ZACommands;
import kabbage.zarena.commands.ZSignCommands;
import kabbage.zarena.commands.ZSpawnCommands;
import kabbage.zarena.customentities.CustomGiant;
import kabbage.zarena.customentities.CustomSkeleton;
import kabbage.zarena.customentities.CustomWither;
import kabbage.zarena.customentities.CustomWolf;
import kabbage.zarena.customentities.CustomZombie;
import kabbage.zarena.customentities.EntityTypeConfiguration;
import kabbage.zarena.customentities.SkeletonTypeConfiguration;
import kabbage.zarena.events.GameStopCause;
import kabbage.zarena.events.GameStopEvent;
import kabbage.zarena.listeners.*;
import kabbage.zarena.signs.ZSignCustomItem;
import kabbage.zarena.spout.PlayerOptionsHandler;
import kabbage.zarena.spout.SpoutHandler;
import kabbage.zarena.utils.Constants;
import kabbage.zarena.utils.Permissions;
import kabbage.zarena.utils.Utils;

import net.minecraft.server.EntityGiantZombie;
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.EntityWither;
import net.minecraft.server.EntityWolf;
import net.minecraft.server.EntityZombie;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ZArena extends JavaPlugin
{
	private static ZArena instance;
	public static Logger logger;
	
	private GameHandler gameHandler; //Game handler
	private PlayerOptionsHandler playerOptionsHandler;
	private SpoutHandler spoutHandler;
	
	//Command executors
	private final ZACommands zaCommands = new ZACommands();
	private final ZSpawnCommands zSpawnCommands = new ZSpawnCommands();
	private final DSpawnCommands dSpawnCommands = new DSpawnCommands();
	private final ISpawnCommands iSpawnCommands = new ISpawnCommands();
	private final ZSignCommands zSignCommands = new ZSignCommands();
	
	//Listeners
	private EntityListener eListener;
	private PlayerListener pListener;
	private WorldListener wListener;
	private BlockListener bListener;
	//private SpoutListener sListener;
	
	public void onEnable()
	{
		instance = this;
		logger = Bukkit.getServer().getLogger();
		
		CustomEntityLibrary.load();
		
		loadConfiguration();	//Lots of stuff relies on the config, so load it early
		//Load some stuff the game handler relies on
		loadDefaults();
		if(getConfig().getBoolean(Constants.FIRST_TIME))
			loadPluginFirstTime();
		saveConfig();
		loadDonatorInfo();
		loadZSignCustomItems();
		
		gameHandler = new GameHandler(); //Create the Game Handler...needs to be done so early because stuff below rely on it
		
		//Load more stuff
		loadCustomZombie();
		loadGamemodeTypes();
		loadEntityTypes();
		loadFiles();
		
		//Register command executors
		getCommand("zarena").setExecutor(zaCommands);
		getCommand("zspawn").setExecutor(zSpawnCommands);
		getCommand("dspawn").setExecutor(dSpawnCommands);
		getCommand("ispawn").setExecutor(iSpawnCommands);
		getCommand("zsign").setExecutor(zSignCommands);
		
		//Register listeners
		eListener = new EntityListener();
		pListener = new PlayerListener();
		wListener = new WorldListener();
		bListener = new BlockListener();			
		PluginManager pm = Bukkit.getServer().getPluginManager();
		eListener.registerEvents(pm, this);
		pListener.registerEvents(pm, this);
		wListener.registerEvents(pm, this);
		bListener.registerEvents(pm, this);
		
		try
		{
			Plugin p = pm.getPlugin("SpoutPlugin");
			if(p != null)
			{
				//Disabled for the time being, as spout doesn't work
//				spoutHandler = new SpoutHandler();
//				spoutHandler.onEnable(sListener);	
			}
		} catch(Exception e) {}
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				onTick();
			}
		}, 1L, 1L);
	}
	
	public void onDisable()
	{
		GameStopEvent event = new GameStopEvent(GameStopCause.SERVER_STOP);
		Bukkit.getServer().getPluginManager().callEvent(event);
		gameHandler.stop();
		//Save stuff
		saveConfig();
		saveFiles();
	}
	
	public GameHandler getGameHandler()
	{
		return gameHandler;
	}
	
	public PlayerOptionsHandler getPlayerOptionsHandler()
	{
		return playerOptionsHandler;
	}
	
	public static ZArena getInstance()
	{
		return instance;
	}
	
	public SpoutHandler getSpoutHandler()
	{
		return spoutHandler;
	}
	
	private void loadConfiguration()
	{
		getConfig().options().copyDefaults(true); 
		saveConfig();
	}
	
    @SuppressWarnings("rawtypes")
	private void loadCustomZombie()
	{
		try
        {
			Class[] entityWithEggArgs = {Class.class, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE};
            Method entityWithEggList = EntityTypes.class.getDeclaredMethod("a", entityWithEggArgs);
            entityWithEggList.setAccessible(true);
            
            entityWithEggList.invoke(entityWithEggList, CustomZombie.class, "Zombie", 54, 44975, 7969893);
            entityWithEggList.invoke(entityWithEggList, EntityZombie.class, "Zombie", 54, 44975, 7969893);
            entityWithEggList.invoke(entityWithEggList, CustomWolf.class, "Wolf", 95, 14144467, 13545366);
            entityWithEggList.invoke(entityWithEggList, EntityWolf.class, "Wolf", 95, 14144467, 13545366);
            entityWithEggList.invoke(entityWithEggList, CustomSkeleton.class, "Skeleton", 51, 12698049, 4802889);
            entityWithEggList.invoke(entityWithEggList, EntitySkeleton.class, "Skeleton", 51, 12698049, 4802889);
            
            Class[] entityWithoutEggArgs = {Class.class, String.class, Integer.TYPE};
            Method entityWithoutEggList = EntityTypes.class.getDeclaredMethod("a", entityWithoutEggArgs);
            entityWithoutEggList.setAccessible(true);
            
            entityWithoutEggList.invoke(entityWithoutEggList, CustomWither.class, "WitherBoss", 64);
            entityWithoutEggList.invoke(entityWithoutEggList, EntityWither.class, "WitherBoss", 64);
            entityWithoutEggList.invoke(entityWithoutEggList, CustomGiant.class, "Giant", 53);
            entityWithoutEggList.invoke(entityWithoutEggList, EntityGiantZombie.class, "Giant", 53);
        }
        catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e)
        {
        	e.printStackTrace();
            setEnabled(false);
        }
	}
	
	private void loadDefaults()
	{
		FileConfiguration cfg = getConfig().options().configuration();
		cfg.addDefault(Constants.FIRST_TIME, true);
		cfg.addDefault(Constants.ALWAYS_NIGHT, true);
		cfg.addDefault(Constants.AUTOSTART, true);
		cfg.addDefault(Constants.AUTORUN, true);
		cfg.addDefault(Constants.AUTOJOIN, false);
		cfg.addDefault(Constants.SAVE_POSITION, true);
		cfg.addDefault(Constants.GAME_WORLD, getServer().getWorlds().get(0).getName());
		cfg.addDefault(Constants.GAME_LEAVE_WORLD, getServer().getWorlds().get(0).getName());
		
		List<Double> locXYZ = new ArrayList<Double>();
		locXYZ.add(getServer().getWorlds().get(0).getSpawnLocation().getX());
		locXYZ.add(getServer().getWorlds().get(0).getSpawnLocation().getY());
		locXYZ.add(getServer().getWorlds().get(0).getSpawnLocation().getZ());
		cfg.addDefault(Constants.GAME_LEAVE_LOCATION, locXYZ);
		
		cfg.addDefault(Constants.PLAYER_LIMIT, 12);
		cfg.addDefault(Constants.QUANTITY_ADJUST, true);
		cfg.addDefault(Constants.SAVE_ITEMS, true);
		cfg.addDefault(Constants.VOTING_LENGTH, 30);
		cfg.addDefault(Constants.BROADCAST_ALL, false);
		cfg.addDefault(Constants.WAVE_DELAY, 10);
		cfg.addDefault(Constants.WORLD_EXCLUSIVE, false);
		cfg.addDefault(Constants.DISABLE_HUNGER, true);
		cfg.addDefault(Constants.DISABLE_JOIN_WITH_INV, false);
		cfg.addDefault(Constants.DISABLE_NON_ZA, false);
		cfg.addDefault(Constants.RESPAWN_EVERY, 3);
		cfg.addDefault(Constants.SHOP_HEADER, "ZBuy");
		cfg.addDefault(Constants.TOLL_HEADER, "ZPay");
		cfg.addDefault(Constants.KILL_MONEY, 15);
		cfg.addDefault(Constants.MONEY_LOST, .2);
		cfg.addDefault(Constants.WOLF_PERCENT_SPAWN, .05);
		cfg.addDefault(Constants.SKELETON_PERCENT_SPAWN, .05);
		cfg.addDefault(Constants.WOLF_WAVE_PERCENT_OCCUR, .3);
		cfg.addDefault(Constants.SKELETON_WAVE_PERCENT_OCCUR, .2);
		cfg.addDefault(Constants.WOLF_WAVE_PERCENT_SPAWN, .9);
		cfg.addDefault(Constants.SKELETON_WAVE_PERCENT_SPAWN, .4);
		
		cfg.addDefault(Constants.ZOMBIE_HEALTH_FORMULA, "Logarithmic");
		cfg.addDefault(Constants.ZOMBIE_HEALTH_LIMIT, 0);
		
		List<Double> zombieHealth = new ArrayList<Double>();
		zombieHealth.add(3.0);
		zombieHealth.add(0.4);
		zombieHealth.add(8.0);
		cfg.addDefault(Constants.ZOMBIE_HEALTH_COEFFICIENTS, zombieHealth);
		
		cfg.addDefault(Constants.ZOMBIE_QUANTITY_FORMULA, "Logistic");
		cfg.addDefault(Constants.ZOMBIE_QUANTITY_LIMIT, 60);
		
		List<Double> zombieQuantity = new ArrayList<Double>();
		zombieQuantity.add(3.0);
		zombieQuantity.add(2.0);
		zombieQuantity.add(10.0);
		cfg.addDefault(Constants.ZOMBIE_QUANTITY_COEFFICIENTS, zombieQuantity);
		
		saveConfig();
	}
	
	private void loadDonatorInfo()
	{
		ConfigurationSection startMoney = getConfig().getConfigurationSection(Constants.START_MONEY);
		for(String donatorSectionString : startMoney.getKeys(false))
		{
			ConfigurationSection donatorSection = startMoney.getConfigurationSection(donatorSectionString);
			if(!donatorSection.contains("permission name") || !donatorSection.contains("value"))
				return;
			String permissionName = donatorSection.getString("permission name");
			int value = donatorSection.getInt("value");
			Permissions.startMoneyPermissions.put(permissionName, value);
		}
		ConfigurationSection extraVotes = getConfig().getConfigurationSection(Constants.EXTRA_VOTES);
		for(String donatorSectionString : extraVotes.getKeys(false))
		{
			ConfigurationSection donatorSection = extraVotes.getConfigurationSection(donatorSectionString);
			if(!donatorSection.contains("permission name") || !donatorSection.contains("value"))
				return;
			String permissionName = donatorSection.getString("permission name");
			int value = donatorSection.getInt("value");
			Permissions.extraVotesPermissions.put(permissionName, value);
		}
		Permissions.registerDonatorPermNodes(getServer().getPluginManager());
	}
	
	private void loadGamemodeTypes()
	{
		File gamemodeFile = new File(Constants.GAMEMODES_FOLDER+File.separator+getConfig().getString(Constants.DEFAULT_GAMEMODE));
		if(!gamemodeFile.exists())
		{
			logger.log(Level.WARNING, "ZArena: Default gamemode type file not found. Using default values");
			try
			{
				gamemodeFile.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}

		}
		YamlConfiguration gamemodeConfig = YamlConfiguration.loadConfiguration(gamemodeFile);
		Gamemode gamemode = new Gamemode(gamemodeConfig);
		gameHandler.defaultGamemode = gamemode;
		gameHandler.setGameMode(gamemode);
		if(getConfig().getStringList(Constants.GAMEMODES) != null)
		{
			for(String fileName : getConfig().getStringList(Constants.GAMEMODES))
			{
				gamemodeFile = new File(Constants.GAMEMODES_FOLDER+File.separator+fileName);
				if(!gamemodeFile.exists())
				{
					logger.log(Level.WARNING, "ZArena: Gamemode Type file for "+fileName+" not found. This Gamemode will be unusable.");
					continue;
				}
				gamemodeConfig = YamlConfiguration.loadConfiguration(gamemodeFile);
				gameHandler.gamemodes.add(new Gamemode(gamemodeConfig));
			}
		}
	}
	
	private void loadEntityTypes()
	{
		File zombieFile = new File(Constants.ENTITIES_FOLDER+File.separator+getConfig().getString(Constants.DEFAULT_ZOMBIE));
		if(!zombieFile.exists())
		{
			logger.log(Level.WARNING, "ZArena: Default zombie type file not found. Using default values");
			try
			{
				zombieFile.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		YamlConfiguration zombieConfig = YamlConfiguration.loadConfiguration(zombieFile);
		EntityTypeConfiguration zombieType = new EntityTypeConfiguration(zombieConfig);
		gameHandler.getWaveHandler().defaultZombieType = zombieType;
		
		File skeletonFile = new File(Constants.ENTITIES_FOLDER+File.separator+getConfig().getString(Constants.DEFAULT_SKELETON));
		if(!skeletonFile.exists())
		{
			logger.log(Level.WARNING, "ZArena: Default skeleton type file not found. Using default values");
			try
			{
				skeletonFile.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		YamlConfiguration skeletonConfig = YamlConfiguration.loadConfiguration(skeletonFile);
		SkeletonTypeConfiguration skeletonType = new SkeletonTypeConfiguration(skeletonConfig);
		gameHandler.getWaveHandler().defaultSkeletonType = skeletonType;
		
		File wolfFile = new File(Constants.ENTITIES_FOLDER+File.separator+getConfig().getString(Constants.DEFAULT_WOLF));
		if(!wolfFile.exists())
		{
			logger.log(Level.WARNING, "ZArena: Default wolf type file not found. Using default values");
			try
			{
				wolfFile.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		YamlConfiguration wolfConfig = YamlConfiguration.loadConfiguration(wolfFile);
		EntityTypeConfiguration wolfType = new EntityTypeConfiguration(wolfConfig);
		gameHandler.getWaveHandler().defaultWolfType = wolfType;

		if(getConfig().getStringList(Constants.ENTITIES) != null)
		{
			for(String fileName : getConfig().getStringList(Constants.ENTITIES))
			{
				File file = new File(Constants.ENTITIES_FOLDER+File.separator+fileName);
				if(!file.exists())
				{
					logger.log(Level.WARNING, "ZArena: Entity Type file for "+fileName+" not found. This Entity Type will not spawn.");
					continue;
				}
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				EntityTypeConfiguration entityConfig = new EntityTypeConfiguration(config);
				switch(entityConfig.getType())
				{
				case "zombie":
					gameHandler.getWaveHandler().zombieTypes.add(entityConfig);
					break;
				case "skeleton":
					entityConfig = new SkeletonTypeConfiguration(config);
					gameHandler.getWaveHandler().skeletonTypes.add((SkeletonTypeConfiguration) entityConfig);
					break;
				case "wolf":
					gameHandler.getWaveHandler().wolfTypes.add(entityConfig);
					break;
				}
			}
		}
	}
	
	private void loadFiles()
	{
		gameHandler.loadLevelHandler();
		if(spoutHandler != null)
			loadPlayerOptions();
	}
	
	private void loadPlayerOptions()
	{
		File path = new File(Constants.PLUGIN_FOLDER);

        FileInputStream fis;
        ObjectInputStream ois;

        try {
            fis = new FileInputStream(path);
            ois = new ObjectInputStream(fis);

            playerOptionsHandler = new PlayerOptionsHandler();
            playerOptionsHandler.readExternal(ois);

            ois.close();
            fis.close();

        } catch (Exception e)
        {
        	ZArena.logger.log(Level.WARNING, "ZArena: Couldn't load the PlayerOptions database. Ignore if this is the first time the plugin has been run.");
        	playerOptionsHandler = new PlayerOptionsHandler();
        }
	}
	
	private void loadPluginFirstTime()
	{
		getConfig().set(Constants.START_MONEY+".Donator1.permission name", "zarena.donator1");
		getConfig().set(Constants.START_MONEY+".Donator1.value", 15);
		getConfig().set(Constants.EXTRA_VOTES+".Donator1.permission name", "zarena.donator1");
		getConfig().set(Constants.EXTRA_VOTES+".Donator1.value", 1);
		getConfig().set(Constants.CUSTOM_ITEMS + ".Healing Potion.type", 373);
		getConfig().set(Constants.CUSTOM_ITEMS + ".Healing Potion.amount", 1);
		getConfig().set(Constants.CUSTOM_ITEMS + ".Healing Potion.damage value", 8229);
		getConfig().set(Constants.CUSTOM_ITEMS + ".Healing Potion.id", 0);
		getConfig().set(Constants.DEFAULT_ZOMBIE, "NormalZombie.yml");
		getConfig().set(Constants.DEFAULT_SKELETON, "NormalSkeleton.yml");
		getConfig().set(Constants.DEFAULT_WOLF, "NormalWolf.yml");
		getConfig().set(Constants.DEFAULT_GAMEMODE, "Normal.yml");
		
		List<String> gamemodeFileNameList = new ArrayList<String>();
		gamemodeFileNameList.add("Hardcore.yml");
		gamemodeFileNameList.add("Apocalypse.yml");
		gamemodeFileNameList.add("NoBuying.yml");
		getConfig().set(Constants.GAMEMODES, gamemodeFileNameList);
		
		List<String> entityFileNameList = new ArrayList<String>();
		entityFileNameList.add("FastZombie.yml");
		entityFileNameList.add("StrongZombie.yml");
		entityFileNameList.add("FireZombie.yml");
		entityFileNameList.add("GigaZombie.yml");
		entityFileNameList.add("FastSkeleton.yml");
		entityFileNameList.add("WitherSkeleton.yml");
		entityFileNameList.add("HellHound.yml");
		getConfig().set(Constants.ENTITIES, entityFileNameList);
		
		List<String> startItems = new ArrayList<String>();
		startItems.add("wood axe");
		getConfig().set(Constants.START_ITEMS, startItems);

		try
		{
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "FastSkeleton.yml");
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "FastZombie.yml");
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "FireZombie.yml");
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "GigaZombie.yml");
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "HellHound.yml");
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "NormalSkeleton.yml");
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "NormalWolf.yml");
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "NormalZombie.yml");
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "StrongZombie.yml");
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "WitherSkeleton.yml");

			Utils.extractFromJar(new File(Constants.GAMEMODES_FOLDER), "Apocalypse.yml");
			Utils.extractFromJar(new File(Constants.GAMEMODES_FOLDER), "Hardcore.yml");
			Utils.extractFromJar(new File(Constants.GAMEMODES_FOLDER), "NoBuying.yml");
			Utils.extractFromJar(new File(Constants.GAMEMODES_FOLDER), "Normal.yml");
		} catch (IOException e)
		{
			logger.log(Level.WARNING, "ZArena: Error loading default files. You can download them manually from the plugins dev.bukkit.org page.");
		}

		getConfig().set(Constants.FIRST_TIME, false);
	}

	private void loadZSignCustomItems()
	{
		ConfigurationSection customItems = getConfig().getConfigurationSection(Constants.CUSTOM_ITEMS);
		for(String customItemString : customItems.getKeys(false))
		{
			ConfigurationSection customItem = customItems.getConfigurationSection(customItemString);
			if(!customItem.contains("type")) //The type is a necessary paramater of the custom item. 
				continue;
			int type = customItem.getInt("type");
			int amount = (customItem.contains("amount")) ? customItem.getInt("amount") : 1;
			short damageValue = (customItem.contains("damage value")) ? (short) customItem.getInt("damage value") : Material.getMaterial(type).getMaxDurability();
			byte id = (customItem.contains("id")) ? (byte) customItem.getInt("id") : (byte) 0;
			
			String[] name = new String[2];
			String configName = customItem.getName();
			int spaceIndex = configName.indexOf(" ");
			if(spaceIndex == -1)
			{
				name[0] = configName;
				name[1] = "";
			}
			else
			{
				name[0] = configName.substring(0, spaceIndex);
				name[1] = configName.substring(spaceIndex + 1);
			}
			new ZSignCustomItem(name, type, amount, damageValue, id); //Creation of new instances of this object automatically add the instance to a list of them
		}
	}
	
	private int tick;
	private void onTick()
	{
		if(tick % 20 == 0)
		{
			if(spoutHandler != null)
				spoutHandler.updatePlayerOptions(gameHandler);
		}
		tick++;
	}
	
	private void saveFiles()
	{
		gameHandler.saveLevelHandler();
		savePlayerOptions();
	}
	
	private void savePlayerOptions()
	{
		File path = new File(Constants.PLUGIN_FOLDER);

        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            fos = new FileOutputStream(path);
            oos = new ObjectOutputStream(fos);

            playerOptionsHandler.writeExternal(oos);

            oos.close();
            fos.close();

        } catch (IOException e)
        {
        	ZArena.logger.log(Level.WARNING, "ZArena: Error saving the PlayerOptions database.");
        }
	}
}