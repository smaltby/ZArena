package com.github.zarena;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.github.customentitylibrary.CustomEntityLibrary;

import com.github.zarena.afkmanager.AFKManager;
import com.github.zarena.utils.*;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
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

public class ZArena extends JavaPlugin
{
	private static ZArena instance;

	private Economy economy;
	private KillCounter kc;

	private GameHandler gameHandler; //Game handler
	private PlayerOptionsHandler playerOptionsHandler;

	private Configuration config;

	private boolean spoutEnabled = false;

	public void onEnable()
	{
		instance = this;

		reloadConfig();

		PluginManager pm = Bukkit.getServer().getPluginManager();
		//Enable stuff
		CustomEntityLibrary.enable(this);
		Plugin p = pm.getPlugin("Spout");
		if(p != null)
		{
			spoutEnabled = true;
			SpoutHandler.enable();
		}
		if(getConfiguration().getBoolean(ConfigEnum.ENABLE_KILLCOUNTER.toString()))
		{
			kc = new KillCounter();
			kc.enable();
		}
		if(getConfiguration().getBoolean(ConfigEnum.ENABLE_AFKKICKER.toString()))
		{
			new AFKManager().enable();
		}

		//Load some stuff the game handler relies on
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
		if(getConfiguration().getBoolean(ConfigEnum.USE_VAULT.toString()) && Bukkit.getPluginManager().getPlugin("Vault") != null)
			setupEconomy();

		//Register command executors
		getCommand("zarena").setExecutor(new ZACommands());
		getCommand("zspawn").setExecutor(new ZSpawnCommands());
		getCommand("dspawn").setExecutor(new DSpawnCommands());
		getCommand("ispawn").setExecutor(new ISpawnCommands());
		getCommand("zsign").setExecutor(new ZSignCommands());

		//Register listeners
		new EntityListener().registerEvents(pm, this);
		new PlayerListener().registerEvents(pm, this);
		new WorldListener().registerEvents(pm, this);
		new BlockListener().registerEvents(pm, this);

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
		if(getConfiguration().getBoolean(ConfigEnum.ENABLE_KILLCOUNTER.toString()))
			kc.disable();
		//Save stuff
		saveFiles();
		//Reset static stuff
		instance = null;
		spoutEnabled = false;
	}

	public FileConfiguration getConfig()
	{
		throw new UnsupportedOperationException("Please use getConfiguration as opposed to getConfig");
	}

	public Configuration getConfiguration()
	{
		return config;
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

	private void loadDonatorInfo()
	{
		ConfigurationNode startMoney = getConfiguration().getNode(ConfigEnum.START_MONEY.toString());
		for(String donatorSectionString : startMoney.getKeys())
		{
			ConfigurationNode donatorSection = startMoney.getNode(donatorSectionString);
			if(!donatorSection.contains("permission name") || !donatorSection.contains("value"))
				return;
			String permissionName = donatorSection.getString("permission name");
			int value = donatorSection.getInt("value");
			Permissions.startMoneyPermissions.put(permissionName, value);
		}
		ConfigurationNode extraVotes = getConfiguration().getNode(ConfigEnum.EXTRA_VOTES.toString());
		for(String donatorSectionString : extraVotes.getKeys())
		{
			ConfigurationNode donatorSection = extraVotes.getNode(donatorSectionString);
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
		File gamemodesFolder = new File(Constants.GAMEMODES_FOLDER);
		if(!gamemodesFolder.isDirectory())
			gamemodesFolder.mkdirs();

		boolean defaultGamemodeFound = false;
		for(File file : gamemodesFolder.listFiles())
		{
			if(file.getName().substring(file.getName().lastIndexOf('.')).equals(".yml"))
			{
				YamlConfiguration gamemodeConfig = YamlConfiguration.loadConfiguration(file);
				Gamemode gamemode = new Gamemode(gamemodeConfig);
				if(file.getName().equals(getConfiguration().getString(ConfigEnum.DEFAULT_GAMEMODE.toString())))
				{
					gameHandler.setDefaultGamemode(gamemode);
					gameHandler.defaultGamemode = gamemode;
					defaultGamemodeFound = true;
				} else
					gameHandler.gamemodes.add(gamemode);
			}
		}

		if(!defaultGamemodeFound)
		{
			log(Level.WARNING, "ZArena: Default gamemode type file not found. Using default values");
			gameHandler.setDefaultGamemode(new Gamemode(new YamlConfiguration()));
		}
	}

	private void loadEntityTypes()
	{
		File entitiesFolder = new File(Constants.ENTITIES_FOLDER);
		if(!entitiesFolder.isDirectory())
			entitiesFolder.mkdirs();

		boolean defaultZombieFound = false;
		boolean defaultWolfFound = false;
		boolean defaultSkeletonFound = false;
		for(File file : entitiesFolder.listFiles())
		{
			if(file.getName().substring(file.getName().lastIndexOf('.')).equals(".yml"))
			{
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				ZEntityTypeConfiguration entityConfig = new ZEntityTypeConfiguration(config);
				try {config.save(file);} catch(IOException e) {e.printStackTrace();}
				if(file.getName().equals(getConfiguration().getString(ConfigEnum.DEFAULT_ZOMBIE.toString())))
				{
					gameHandler.getWaveHandler().defaultZombieType = entityConfig;
					defaultZombieFound = true;
				} else if(file.getName().equals(getConfiguration().getString(ConfigEnum.DEFAULT_WOLF.toString())))
				{
					gameHandler.getWaveHandler().defaultWolfType = entityConfig;
					defaultWolfFound = true;
				} else if(file.getName().equals(getConfiguration().getString(ConfigEnum.DEFAULT_SKELETON.toString())))
				{
					gameHandler.getWaveHandler().defaultSkeletonType = entityConfig;
					defaultSkeletonFound = true;
				}
				else
				{
					switch(StringEnums.valueOf(entityConfig.getPreferredType().toUpperCase()))
					{
						case ZOMBIE: case ZOMBIEPIGMAN:
							gameHandler.getWaveHandler().zombieTypes.add(entityConfig);
							break;
						case SKELETON:
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
		if(!defaultZombieFound)
		{
			log(Level.WARNING, "Default zombie not found, using default values.");
			gameHandler.getWaveHandler().defaultZombieType = new ZEntityTypeConfiguration(new YamlConfiguration());
		}
		if(!defaultWolfFound)
		{
			log(Level.WARNING, "Default wolf not found, using default values.");
			gameHandler.getWaveHandler().defaultWolfType = new ZEntityTypeConfiguration(new YamlConfiguration());
		}
		if(!defaultSkeletonFound)
		{
			log(Level.WARNING, "Default skeleton not found, using default values.");
			gameHandler.getWaveHandler().defaultSkeletonType = new ZEntityTypeConfiguration(new YamlConfiguration());
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
			Utils.extractFromJar(new File(Constants.PLUGIN_FOLDER), "config.yml");
		} catch (IOException e)
		{
			log(Level.WARNING, "ZArena: Error loading default files. You can download them manually from the plugins dev.bukkit.org page.");
		}
	}

	private void loadZSignCustomItems()
	{
		ConfigurationNode customItems = getConfiguration().getNode(ConfigEnum.CUSTOM_ITEMS.toString());
		for(String customItemString : customItems.getKeys())
		{
			ConfigurationNode customItem = customItems.getNode(customItemString);
			if(!customItem.contains("type")) //The type is a necessary paramater of the custom item.
				continue;
			int type = customItem.getInt("type");
			int amount = customItem.getInt("amount", 1);
			short damageValue = (short)((int)customItem.getInt("damage value", 0));
			byte id = (byte) ((int)customItem.getInt("id", 0));

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

	@Override
	public void reloadConfig()
	{
		File configFile = new File(Constants.PLUGIN_FOLDER+"/config.yml");
		if(!configFile.exists())
			loadPluginFirstTime();
		config = new Configuration(configFile);
		//If true, update from old config to new config
		if(!config.contains(ConfigEnum.VERSION.toString()))
		{
			try
			{
				File old = File.createTempFile("configTemp", "yml");
				old.deleteOnExit();
				FileInputStream input = new FileInputStream(configFile);
				FileOutputStream output = new FileOutputStream(old);
				byte[] buffer = new byte[1024];	//Create a buffer
				//Have the inputstream read the buffer and write it to it's new directory
				int read = 0;
				while ((read = input.read(buffer)) > 0)
					output.write(buffer, 0, read);
				Utils.extractFromJar(new File(Constants.PLUGIN_FOLDER), "config.yml", true);
				Configuration newConfig = new Configuration(configFile);
				Configuration oldConfig = new Configuration(old);
				Utils.convertToNewConfig(newConfig, oldConfig);
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		//Determine if any values are missing from the config
		boolean somethingMissing = false;
		for(ConfigEnum c : ConfigEnum.values())
		{
			if(!config.contains(c.toString()))
			{
				somethingMissing = true;
				break;
			}
		}
		//If any values are missing, set them to the defaults, which are gotten from the default config.yml in the
		//plugin's jar file
		if(somethingMissing)
		{
			//Load the default config.yml to a temporary file
			File tempDefaults;
			try
			{
				tempDefaults = File.createTempFile("defaults",".yml");
				tempDefaults.deleteOnExit();
				InputStream in = ZArena.class.getResourceAsStream("/config.yml");	//Get inputstream from base folder
				if (in == null)
					throw new FileNotFoundException("config.yml could not be found.");

				FileOutputStream out = new FileOutputStream(tempDefaults);
				byte[] buffer = new byte[1024];	//Create a buffer

				//Have the inputstream read the buffer and write it to it's new directory
				int read = 0;
				while ((read = in.read(buffer)) > 0)
					out.write(buffer, 0, read);

				//Close up everything
				in.close();
				out.flush();
				out.close();
			} catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
			Configuration defaults = new Configuration(tempDefaults);
			for(ConfigEnum c : ConfigEnum.values())
			{
				if(!config.contains(c.toString()))
				{
					config.set(c.toString(), defaults.getProperty(c.toString()));
				}
			}
		}
	}

	@Override
	public void saveConfig()
	{
		config.save();
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
        if (economyProvider != null)
            economy = economyProvider.getProvider();
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