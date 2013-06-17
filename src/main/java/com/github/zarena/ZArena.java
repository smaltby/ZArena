package com.github.zarena;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.github.customentitylibrary.CustomEntityLibrary;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.zarena.commands.DSpawnCommands;
import com.github.zarena.commands.ISpawnCommands;
import com.github.zarena.commands.ZACommands;
import com.github.zarena.commands.ZSignCommands;
import com.github.zarena.commands.ZSpawnCommands;
import com.github.zarena.entities.ZEntityTypeConfiguration;
import com.github.zarena.events.GameStopCause;
import com.github.zarena.events.GameStopEvent;
import com.github.zarena.killcounter.KillCounter;
import com.github.zarena.listeners.BlockListener;
import com.github.zarena.listeners.EntityListener;
import com.github.zarena.listeners.PlayerListener;
import com.github.zarena.listeners.WorldListener;
import com.github.zarena.signs.ZSignCustomItem;
import com.github.zarena.spout.PlayerOptionsHandler;
import com.github.zarena.spout.SpoutHandler;
import com.github.zarena.utils.ChatHelper;
import com.github.zarena.utils.Constants;
import com.github.zarena.utils.CustomObjectInputStream;
import com.github.zarena.utils.Message;
import com.github.zarena.utils.Metrics;
import com.github.zarena.utils.Permissions;
import com.github.zarena.utils.StringEnums;
import com.github.zarena.utils.Utils;

public class ZArena extends JavaPlugin
{
	private static ZArena instance;

	private Economy economy;
	private KillCounter kc;

	private GameHandler gameHandler; //Game handler
	private PlayerOptionsHandler playerOptionsHandler;

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

	private boolean spoutEnabled = false;

	public void onEnable()
	{
		instance = this;

		CustomEntityLibrary.load(this);

		PluginManager pm = Bukkit.getServer().getPluginManager();
		Plugin p = pm.getPlugin("Spout");
		if(p != null)
		{
			spoutEnabled = true;
			SpoutHandler.onEnable();
		}

		loadConfiguration();	//Lots of stuff relies on the config, so load it early
		//Load some stuff the game handler relies on
		loadDefaults();
		if(getConfig().getBoolean(Constants.FIRST_TIME))
			loadPluginFirstTime();
		if(getConfig().getBoolean(Constants.ENABLE_KILLCOUNTER))
		{
			kc = new KillCounter();
			kc.enable();
		}
		saveConfig();
		loadDonatorInfo();
		loadZSignCustomItems();

		gameHandler = new GameHandler(); //Create the Game Handler...needs to be done so early because stuff below rely on it

		//Load more stuff
		loadEntityTypes();
		loadGamemodeTypes();//Note: Has to be after loadEntityTypes
		loadFiles();

		//Load the language file and intialize the messages
		ChatHelper.loadLanguageFile();
		Message.setMessages();

		//Load metrics
		try
		{
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e)
		{/* Failed to submit the stats :-( */}
		//Load Vault economy
		if(getConfig().getBoolean(Constants.USE_VAULT))
			setupEconomy();

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
		eListener.registerEvents(pm, this);
		pListener.registerEvents(pm, this);
		wListener.registerEvents(pm, this);
		bListener.registerEvents(pm, this);

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
		if(getConfig().getBoolean(Constants.ENABLE_KILLCOUNTER))
			kc.disable();
		//Save stuff
		saveConfig();
		saveFiles();
		//Reset static stuff
		instance = null;
		spoutEnabled = false;
	}

	public Economy getEconomy()
	{
		return economy;
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

	public boolean isSpoutEnabled()
	{
		return spoutEnabled;
	}

	private void loadConfiguration()
	{
		getConfig().options().copyDefaults(true);
		saveConfig();
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
		cfg.addDefault(Constants.XP_BAR_IS_MONEY, false);
		cfg.addDefault(Constants.BROADCAST_ALL, false);
		cfg.addDefault(Constants.SEPERATE_INVENTORY, true);
		cfg.addDefault(Constants.WAVE_DELAY, 10);
		cfg.addDefault(Constants.WORLD_EXCLUSIVE, false);
		cfg.addDefault(Constants.DISABLE_HUNGER, true);
		cfg.addDefault(Constants.DISABLE_JOIN_WITH_INV, false);
		cfg.addDefault(Constants.DISABLE_NON_ZA, false);
		cfg.addDefault(Constants.ENABLE_KILLCOUNTER, true);
		cfg.addDefault(Constants.RESPAWN_EVERY_WAVES, 3);
		cfg.addDefault(Constants.RESPAWN_EVERY_TIME, 0);
		cfg.addDefault(Constants.RESPAWN_REMINDER_DELAY, 30);
		cfg.addDefault(Constants.SHOP_HEADER, "ZBuy");
		cfg.addDefault(Constants.TOLL_HEADER, "ZPay");
		cfg.addDefault(Constants.KILL_MONEY, 15);
		cfg.addDefault(Constants.MONEY_LOST, .2);
		cfg.addDefault(Constants.USE_VAULT, false);
		cfg.addDefault(Constants.MOB_CAP, 200);
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
		zombieQuantity.add(1.5);
		zombieQuantity.add(4.0);
		zombieQuantity.add(9.0);
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
		new File(Constants.GAMEMODES_FOLDER).mkdir();
		File gamemodeFile = new File(Constants.GAMEMODES_FOLDER+File.separator+getConfig().getString(Constants.DEFAULT_GAMEMODE));
		if(!gamemodeFile.exists())
		{
			log(Level.WARNING, "ZArena: Default gamemode type file not found. Using default values");
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
					log(Level.WARNING, "ZArena: Gamemode Type file for "+fileName+" not found. This Gamemode will be unusable.");
					continue;
				}
				gamemodeConfig = YamlConfiguration.loadConfiguration(gamemodeFile);
				gameHandler.gamemodes.add(new Gamemode(gamemodeConfig));
			}
		}
	}

	private void loadEntityTypes()
	{
		new File(Constants.ENTITIES_FOLDER).mkdir();
		File zombieFile = new File(Constants.ENTITIES_FOLDER+File.separator+getConfig().getString(Constants.DEFAULT_ZOMBIE));
		if(!zombieFile.exists())
		{
			log(Level.WARNING, "ZArena: Default zombie type file not found. Using default values");
			try
			{
				zombieFile.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		YamlConfiguration zombieConfig = YamlConfiguration.loadConfiguration(zombieFile);
		ZEntityTypeConfiguration zombieType = new ZEntityTypeConfiguration(zombieConfig);
		try
		{
			zombieConfig.save(zombieFile);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		gameHandler.getWaveHandler().defaultZombieType = zombieType;

		File skeletonFile = new File(Constants.ENTITIES_FOLDER+File.separator+getConfig().getString(Constants.DEFAULT_SKELETON));
		if(!skeletonFile.exists())
		{
			log(Level.WARNING, "ZArena: Default skeleton type file not found. Using default values");
			try
			{
				skeletonFile.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		YamlConfiguration skeletonConfig = YamlConfiguration.loadConfiguration(skeletonFile);
		ZEntityTypeConfiguration skeletonType = new ZEntityTypeConfiguration(skeletonConfig);
		try
		{
			skeletonConfig.save(skeletonFile);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		gameHandler.getWaveHandler().defaultSkeletonType = skeletonType;

		File wolfFile = new File(Constants.ENTITIES_FOLDER+File.separator+getConfig().getString(Constants.DEFAULT_WOLF));
		if(!wolfFile.exists())
		{
			log(Level.WARNING, "ZArena: Default wolf type file not found. Using default values");
			try
			{
				wolfFile.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		YamlConfiguration wolfConfig = YamlConfiguration.loadConfiguration(wolfFile);
		ZEntityTypeConfiguration wolfType = new ZEntityTypeConfiguration(wolfConfig);
		try
		{
			wolfConfig.save(wolfFile);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		gameHandler.getWaveHandler().defaultWolfType = wolfType;

		if(getConfig().getStringList(Constants.ENTITIES) != null)
		{
			for(String fileName : getConfig().getStringList(Constants.ENTITIES))
			{
				File file = new File(Constants.ENTITIES_FOLDER+File.separator+fileName);
				if(!file.exists())
				{
					log(Level.WARNING, "ZArena: Entity Type file for "+fileName+" not found. This Entity Type will not spawn.");
					continue;
				}
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				ZEntityTypeConfiguration entityConfig = new ZEntityTypeConfiguration(config);
				try
				{
					config.save(file);
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				switch(StringEnums.valueOf(entityConfig.getPreferredType().toUpperCase()))
				{
				case ZOMBIE: case ZOMBIEPIGMAN:
					gameHandler.getWaveHandler().zombieTypes.add(entityConfig);
					break;
				case SKELETON:
					entityConfig = new ZEntityTypeConfiguration(config);
					gameHandler.getWaveHandler().skeletonTypes.add(entityConfig);
					break;
				case WOLF:
					gameHandler.getWaveHandler().wolfTypes.add(entityConfig);
					break;
				default:
				}
			}
		}
	}

	private void loadFiles()
	{
		gameHandler.loadLevelHandler();
		if(spoutEnabled)
			loadPlayerOptions();
	}

	private void loadPlayerOptions()
	{
		File path = new File(Constants.OPTIONS_PATH);

        try
        {
        	FileInputStream fis = new FileInputStream(path);
            CustomObjectInputStream ois = new CustomObjectInputStream(fis);

            playerOptionsHandler = new PlayerOptionsHandler();
            playerOptionsHandler.readExternal(ois);

            ois.close();
            fis.close();

        } catch (Exception e)
        {
        	log(Level.WARNING, "ZArena: Couldn't load the PlayerOptions database. Ignore if this is the first time the plugin has been run.");
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
		entityFileNameList.add("ZombiePigman.yml");
		entityFileNameList.add("ZombieVillager.yml");
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
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "ZombiePigman.yml");
			Utils.extractFromJar(new File(Constants.ENTITIES_FOLDER), "ZombieVillager.yml");

			Utils.extractFromJar(new File(Constants.GAMEMODES_FOLDER), "Apocalypse.yml");
			Utils.extractFromJar(new File(Constants.GAMEMODES_FOLDER), "Hardcore.yml");
			Utils.extractFromJar(new File(Constants.GAMEMODES_FOLDER), "NoBuying.yml");
			Utils.extractFromJar(new File(Constants.GAMEMODES_FOLDER), "Normal.yml");

			Utils.extractFromJar(new File(Constants.PLUGIN_FOLDER), "language.yml");
		} catch (IOException e)
		{
			log(Level.WARNING, "ZArena: Error loading default files. You can download them manually from the plugins dev.bukkit.org page.");
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
			int amount = customItem.getInt("amount", 1);
			short damageValue = (short) customItem.getInt("damage value", 0);
			byte id = (byte) customItem.getInt("id", 0);

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

			Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
			for(String enchantName : customItem.getStringList("enchantments"))
			{
				String[] args = Utils.getConfigArgs(enchantName);
				Enchantment enchantment = Enchantment.getById(Utils.parseInt(enchantName.split("\\s")[0], -1));
				int level = (args.length > 0) ? Utils.parseInt(args[0], 1) : 1;
				enchantments.put(enchantment, level);
			}

			new ZSignCustomItem(name, type, amount, damageValue, id, enchantments); //Creation of new instances of this object automatically add the instance to a list of them
		}
	}

	private int tick;
	private void onTick()
	{
		if(tick % 20 == 0)
		{
			if(spoutEnabled)
				SpoutHandler.updatePlayerOptions();
		}
		tick++;
	}

	private void saveFiles()
	{
		gameHandler.saveLevelHandler(true);
		if(spoutEnabled)
			savePlayerOptions();
	}

	private void savePlayerOptions()
	{
		File path = new File(Constants.OPTIONS_PATH);

        try
        {
        	FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            playerOptionsHandler.writeExternal(oos);

            oos.close();
            fos.close();
        }
        catch (IOException e)
        {
        	e.printStackTrace();
        	log(Level.WARNING, "ZArena: Error saving the PlayerOptions database.");
        }
	}

	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

	public static void log(Level level, String msg)
	{
		getInstance().getLogger().log(level, msg);
	}

	public static void log(String msg)
	{
		log(Level.INFO, msg);
	}
}