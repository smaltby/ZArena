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
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
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
	protected Configuration statsBackup;

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
		if(getConfig().getBoolean(ConfigEnum.ENABLE_KILLCOUNTER.toString()))
		{
			kc = new KillCounter();
			kc.enable();
		}
		if(getConfig().getBoolean(ConfigEnum.ENABLE_AFKKICKER.toString()))
		{
			new AFKManager().enable();
		}

		//Load some stuff the game handler relies on
		loadDonatorInfo();
		loadZSignCustomItems();

		gameHandler = new GameHandler(); //Create the Game Handler...needs to be done so early because stuff below rely on it

		//If the server crashed, load backups of players data
		try
		{
			loadBackups();
		} catch(IOException e)
		{
			e.printStackTrace();
		}

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
		if(getConfig().getBoolean(ConfigEnum.USE_VAULT.toString()) && Bukkit.getPluginManager().getPlugin("Vault") != null)
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
		gameHandler.stop();
		GameStopEvent event = new GameStopEvent(GameStopCause.SERVER_STOP);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if(getConfig().getBoolean(ConfigEnum.ENABLE_KILLCOUNTER.toString()))
			kc.disable();
		//Save stuff
		saveFiles();
		//Being as though the onDisable method got sucessfully called, we can clear the stat backups, as this isn't a crash
		try
		{
			File statsBackupFile = new File(Constants.BACKUP_PATH);
			PrintWriter writer = new PrintWriter(statsBackupFile);
			writer.print("");
			writer.close();
		} catch(FileNotFoundException e)
		{
			ZArena.log(Level.WARNING, "Stats backup file was never properly created.");
		}
		//Reset static stuff
		instance = null;
		spoutEnabled = false;
	}

	@Override
	public FileConfiguration getConfig()
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

	private void loadBackups() throws IOException
	{
		File statsBackupFile = new File(Constants.BACKUP_PATH);
		if(!statsBackupFile.exists())
			statsBackupFile.createNewFile();
		statsBackup = Configuration.loadConfiguration(statsBackupFile);

		for(String key : statsBackup.getKeys(false))
		{
			ConfigurationSection section = statsBackup.getConfigurationSection(key);

			//Load location
			World world = Bukkit.getWorld(section.getString("world"));
			if(world == null)
				world = Bukkit.getWorlds().get(0);
			Location loc = new Location(world, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));

			//Load inventory
			ItemStack[] items = new ItemStack[0];
			if(section.getConfigurationSection("items") != null)
			{
				items = new ItemStack[section.getConfigurationSection("items").getKeys(false).size()];
				int index = 0;
				for(String itemKey : section.getConfigurationSection("items").getKeys(false))
					items[index++] = ItemStack.deserialize(section.getConfigurationSection("items."+itemKey).getValues(true));
			}

			//Load armor
			ItemStack[] armor = new ItemStack[0];
			if(section.getConfigurationSection("armor") != null)
			{
				armor = new ItemStack[section.getConfigurationSection("armor").getKeys(false).size()];
				int index = 0;
				for(String itemKey : section.getConfigurationSection("armor").getKeys(false))
					armor[index++] = ItemStack.deserialize(section.getConfigurationSection("armor."+itemKey).getValues(true));
			}

			//Load gamemode, level, and money
			GameMode gm = GameMode.getByValue(section.getInt("gamemode"));
			int level = section.getInt("level");
			double money = section.getDouble("money");

			//Restore pre game join stuff to player
			PlayerStats stats = new PlayerStats(key, loc, items, armor, gm, level, money);
			gameHandler.getPlayerStats().put(key, stats);
			gameHandler.removePlayer(key);
		}
	}

	private void loadDonatorInfo()
	{
		ConfigurationSection startMoney = getConfig().getConfigurationSection(ConfigEnum.START_MONEY.toString());
		for(String donatorSectionString : startMoney.getKeys(false))
		{
			ConfigurationSection donatorSection = startMoney.getConfigurationSection(donatorSectionString);
			if(!donatorSection.contains("permission name") || !donatorSection.contains("value"))
				return;
			String permissionName = donatorSection.getString("permission name");
			int value = donatorSection.getInt("value");
			Permissions.startMoneyPermissions.put(permissionName, value);
		}
		ConfigurationSection extraVotes = getConfig().getConfigurationSection(ConfigEnum.EXTRA_VOTES.toString());
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
				if(file.getName().equals(getConfig().getString(ConfigEnum.DEFAULT_GAMEMODE.toString())))
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
				if(file.getName().equals(getConfig().getString(ConfigEnum.DEFAULT_ZOMBIE.toString())))
				{
					gameHandler.getWaveHandler().defaultZombieType = entityConfig;
					defaultZombieFound = true;
				} else if(file.getName().equals(getConfig().getString(ConfigEnum.DEFAULT_WOLF.toString())))
				{
					gameHandler.getWaveHandler().defaultWolfType = entityConfig;
					defaultWolfFound = true;
				} else if(file.getName().equals(getConfig().getString(ConfigEnum.DEFAULT_SKELETON.toString())))
				{
					gameHandler.getWaveHandler().defaultSkeletonType = entityConfig;
					defaultSkeletonFound = true;
				}
				else
				{
					gameHandler.getWaveHandler().addType(entityConfig);
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
		ConfigurationSection customItems = getConfig().getConfigurationSection(ConfigEnum.CUSTOM_ITEMS.toString());
		for(String customItemString : customItems.getKeys(false))
		{
			ConfigurationSection customItem = customItems.getConfigurationSection(customItemString);
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
		config = Configuration.loadConfiguration(configFile);
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
				int read;
				while ((read = input.read(buffer)) > 0)
					output.write(buffer, 0, read);
				Utils.extractFromJar(new File(Constants.PLUGIN_FOLDER), "config.yml", true);
				Configuration newConfig = Configuration.loadConfiguration(configFile);
				Configuration oldConfig = Configuration.loadConfiguration(old);
				Utils.convertToNewConfig(newConfig, oldConfig);
				newConfig.save(old);
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		} else if(config.getInt(ConfigEnum.VERSION.toString()) == 1)
		{
			File entitiesFolder = new File(Constants.ENTITIES_FOLDER);
			for(File file : entitiesFolder.listFiles())
			{
				if(file.getName().substring(file.getName().lastIndexOf('.')).equals(".yml"))
				{
					YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
					config.set("Speed", config.getDouble("Speed", .23) * 4.347826086956522);
					try {config.save(file);} catch(IOException e) {e.printStackTrace();}
				}
			}
			config.set(ConfigEnum.VERSION.toString(), 2);
			try
			{
				config.save(configFile);
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
			Map<String, Object> oldValues = config.getValues(true);
			//Set the config back to the default so we preserve the correct order of stuff
			config = Configuration.loadConfiguration(ZArena.class.getResourceAsStream("/config.yml"));

			//Set back all of the user defined values
			for(Map.Entry<String, Object> e : oldValues.entrySet())
				config.set(e.getKey(), e.getValue());

			saveConfig();
		}
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