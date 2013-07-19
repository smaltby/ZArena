package com.github.zarena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;


import com.github.zarena.utils.ConfigEnum;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import com.github.zarena.entities.ZEntityType;
import com.github.zarena.signs.ZSignCustomItem;
import com.github.zarena.utils.Constants;
import com.github.zarena.utils.Utils;

public class Gamemode
{
	private String name;
	private int initWave;
	private boolean isApocalypse;
	private boolean isScavenger;
	private boolean isNoRegen;
	private boolean isSlowRegen;
	private double weight;
	private double healthModifier;
	private double damageModifier;
	private double zombieAmountModifier;
	private double difficultyModifier;
	private List<ZEntityType> defaultZombies = new ArrayList<ZEntityType>();
	private List<ZEntityType> defaultWolves = new ArrayList<ZEntityType>();
	private List<ZEntityType> defaultSkeletons = new ArrayList<ZEntityType>();
	private List<ItemStack> startItems = new ArrayList<ItemStack>();
	private Map<String, Double> allowedEntityModifiers = new HashMap<String, Double>();
	private Map<String, Double> itemCostModifiers = new HashMap<String, Double>();

	public Gamemode(FileConfiguration config)
	{
		name = config.getString("Name", "Normal");
		initWave = config.getInt("Initial Wave", 1);
		isApocalypse = config.getBoolean("Apocalypse", false);
		isScavenger = config.getBoolean("Scavenger", false);
		isNoRegen = config.getBoolean("No Regen", false);
		isSlowRegen = config.getBoolean("Slow Regen", false);
		weight = config.getDouble("Gamemode Choice Weight", 1);
		healthModifier = config.getDouble("Health Modifier", 1);
		damageModifier = config.getDouble("Damage Modifier", 1);
		zombieAmountModifier = config.getDouble("Zombie Amount Modifier", 1);
		difficultyModifier = config.getDouble("Difficulty Modifier", 1);

		List<String> defaultsNames = new ArrayList<String>();
		if(config.contains("Default Zombie"))
			defaultsNames = config.getStringList("Default Zombie");
		if(defaultsNames.isEmpty())
			defaultsNames.add(config.getString("Default Zombie", ZArena.getInstance().getConfig().getString(ConfigEnum.DEFAULT_ZOMBIE.toString())));

		defaultZombies.addAll(handleConversion(defaultsNames));

		if(config.contains("Default Wolf"))
			defaultsNames = config.getStringList("Default Wolf");
		if(defaultsNames.isEmpty())
			defaultsNames.add(config.getString("Default Wolf", ZArena.getInstance().getConfig().getString(ConfigEnum.DEFAULT_WOLF.toString())));

		defaultWolves.addAll(handleConversion(defaultsNames));

		if(config.contains("Default Skeleton"))
			defaultsNames = config.getStringList("Default Skeleton");
		if(defaultsNames.isEmpty())
			defaultsNames.add(config.getString("Default Skeleton", ZArena.getInstance().getConfig().getString(ConfigEnum.DEFAULT_SKELETON.toString())));

		defaultSkeletons.addAll(handleConversion(defaultsNames));

		if(config.getStringList("Start Items") != null)
		{
			for(String arg : config.getStringList("Start Items"))
			{
				//If the second argument isn't an integer, we can assume it's part of the item
				if(arg.split("\\s").length > 1 && Utils.parseInt(arg.split("\\s")[1], -1) == -1)
					arg = arg.replaceFirst(" ", "_");
				int amount = 1;
				if(Utils.getConfigArgs(arg).length > 0)
					amount = Utils.parseInt(Utils.getConfigArgs(arg)[0], 1);
				ZSignCustomItem customItem = ZSignCustomItem.getCustomItem(arg.split("\\s"));
				if(customItem != null)
				{
					ItemStack item = customItem.getItem();
					if(amount != 1)
						item.setAmount(amount);
					startItems.add(item);
					continue;
				}
				Material material = Material.matchMaterial(arg.split("\\s")[0]);
				if(material != null)
				{
					ItemStack item = new ItemStack(material);
					if(amount != 1)
						item.setAmount(amount);
					startItems.add(item);
				}
			}
		}
		if(config.getStringList("Allowed Entities") != null)
		{
			for(String arg : config.getStringList("Allowed Entities"))
			{
				double spawnChanceMod = 1;
				String[] args = Utils.getConfigArgs(arg);
				if(args.length > 0)
					spawnChanceMod = Utils.parseDouble(args[0], 1);
				allowedEntityModifiers.put(arg.split("\\s")[0].toLowerCase().split(".")[0], spawnChanceMod);
			}
		}
		if(allowedEntityModifiers.isEmpty())
			allowedEntityModifiers.put("ALL", 1.0);
		if(config.getStringList("Allowed Items") != null)
		{
			for(String arg : config.getStringList("Allowed Items"))
			{
				if(arg.split("\\s").length > 1 && Utils.parseInt(arg.split("\\s")[1], -1) < 0)	//If the second argument isn't an integer, we can assume it's part of the item
					arg = arg.replaceFirst(" ", "_");
				double costMod = 1;
				String[] args = Utils.getConfigArgs(arg);
				if(args.length > 0)
					costMod = Utils.parseDouble(args[0], 1);
				itemCostModifiers.put(arg.split("\\s")[0].toUpperCase(), costMod);
			}
		}
		if(itemCostModifiers.isEmpty())
			itemCostModifiers.put("ALL", 1.0);
	}

	public boolean canBuyItem(String item)
	{
		if(itemCostModifiers.containsKey("ALL"))
			return true;
		if(itemCostModifiers.containsKey("NONE"))
			return false;
		return itemCostModifiers.containsKey(item.toUpperCase().replaceAll(" ", "_"));
	}

	public boolean canSpawn(String entity)
	{
		if(allowedEntityModifiers.containsKey("ALL"))
			return true;
		return allowedEntityModifiers.containsKey(entity.toLowerCase().split(".")[0]);
	}

	public double getChoiceWeight()
	{
		return weight;
	}

	public Double getCostModifier(String item)
	{
		if(!canBuyItem(item))	//Generally shouldn't ever be true
			return null;
		if(itemCostModifiers.get(item.toUpperCase().replaceAll(" ", "_")) != null)
			return itemCostModifiers.get(item.toUpperCase().replaceAll(" ", "_"));
		return itemCostModifiers.get("ALL");
	}

	public double getDamageModifier()
	{
		return damageModifier;
	}

	public List<ZEntityType> getDefaultSkeletons()
	{
		return defaultSkeletons;
	}

	public List<ZEntityType> getDefaultWolves()
	{
		return defaultWolves;
	}

	public List<ZEntityType> getDefaultZombies()
	{
		return defaultZombies;
	}

	public double getDifficultyModifier()
	{
		return difficultyModifier;
	}

	public double getHealthModifier()
	{
		return healthModifier;
	}

	public String getName()
	{
		return name;
	}

	public double getSpawnChanceModifier(String entity)
	{
		if(!(canSpawn(entity)))	//Generally shouldn't ever be true
			return 0;
		return allowedEntityModifiers.get(entity.toLowerCase().split(".")[0]);
	}

	public List<ItemStack> getStartItems()
	{
		return startItems;
	}

	public double getZombieAmountModifier()
	{
		return zombieAmountModifier;
	}

	public boolean isApocalypse()
	{
		return isApocalypse;
	}

	public boolean isNoRegen()
	{
		return isNoRegen;
	}

	public boolean isScavenger()
	{
		return isScavenger;
	}

	public boolean isSlowRegen()
	{
		return isSlowRegen;
	}

	public int getInitialWave()
	{
		return initWave;
	}

	@Override
	public String toString()
	{
		return name;
	}

	private ZEntityType convertToEntityType(String typeName)
	{
		WaveHandler waveHandler = ZArena.getInstance().getGameHandler().getWaveHandler();
		for(ZEntityType type : waveHandler.getAllEntityTypes())
		{
			if(type.getName().replaceAll(" ", "").equalsIgnoreCase(typeName))
				return type;
		}
		return null;
	}

	private List<ZEntityType> handleConversion(List<String> typeNames)
	{
		List<ZEntityType> types = new ArrayList<ZEntityType>();
		for(String name : typeNames)
		{
			ZEntityType type = convertToEntityType(name.replaceAll(".yml", "").replaceAll(" ", ""));
			if(type != null)
				types.add(type);
			else
				ZArena.log(Level.WARNING, "Entity type, "+name+", specified in the configuration of the gamemode, "+this.name+", could not be found.");
		}
		typeNames.clear();
		return types;
	}

	public static Gamemode getGamemode(String name)
	{
		Gamemode defaultGm = ZArena.getInstance().getGameHandler().defaultGamemode;
		if(defaultGm.getName().replaceAll(" ", "").equalsIgnoreCase(name.replaceAll("_", "")))
			return defaultGm;
		for(Gamemode gm : ZArena.getInstance().getGameHandler().gamemodes)
		{
			if(gm.getName().replaceAll(" ", "").equalsIgnoreCase(name.replaceAll("_", "")))
				return gm;
		}
		return null;
	}

	public static Gamemode getRandomGamemode()
	{
		return getRandomGamemode(new ArrayList<Gamemode>());
	}

	public static Gamemode getRandomGamemode(Gamemode exclude)
	{
		List<Gamemode> excludes = new ArrayList<Gamemode>();
		excludes.add(exclude);
		return getRandomGamemode(excludes);
	}

	public static Gamemode getRandomGamemode(List<Gamemode> excludes)
	{
		int totalWeight = 0;
		for(Gamemode gm : ZArena.getInstance().getGameHandler().gamemodes)
			totalWeight += (int) (gm.weight * 10);

		Gamemode[] weightedGameModes = new Gamemode[totalWeight];
		for(Gamemode gm : ZArena.getInstance().getGameHandler().gamemodes)
		{
			for(int weight = (int) (gm.weight * 10); weight > 0; weight--)
			{
				weightedGameModes[totalWeight - 1] = gm;
				totalWeight--;
			}
		}
		if(excludes.containsAll(ZArena.getInstance().getGameHandler().gamemodes))
			return weightedGameModes[new Random().nextInt(weightedGameModes.length)];
		Gamemode gamemode;
		do
		{
			gamemode = weightedGameModes[new Random().nextInt(weightedGameModes.length)];
		} while(excludes.contains(gamemode));
		return gamemode;
	}
}
