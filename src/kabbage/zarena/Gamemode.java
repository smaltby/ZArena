package kabbage.zarena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kabbage.zarena.signs.ZSignCustomItem;
import kabbage.zarena.utils.Constants;
import kabbage.zarena.utils.Utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class Gamemode
{
	private static List<Gamemode> gamemodes;
	
	private String name;
	private boolean isApocalypse;
	private boolean isScavenger;
	private boolean isNoRegen;
	private boolean isSlowRegen;
	private double weight;
	private double healthModifier;
	private double damageModifier;
	private double zombieAmountModifier;
	private double difficultyModifier;
	private List<String> defaultZombies = new ArrayList<String>();
	private List<String> defaultWolves = new ArrayList<String>();
	private List<String> defaultSkeletons = new ArrayList<String>();
	private List<ItemStack> startItems = new ArrayList<ItemStack>();
	private Map<String, Double> allowedEntityModifiers = new HashMap<String, Double>();
	private Map<String, Double> itemCostModifiers = new HashMap<String, Double>();

	public Gamemode(FileConfiguration config)
	{
		name = config.getString("Name", "Normal");
		isApocalypse = config.getBoolean("Apocalypse", false);
		isScavenger = config.getBoolean("Scavenger", false);
		isNoRegen = config.getBoolean("No Regen", false);
		isSlowRegen = config.getBoolean("Slow Regen", false);
		weight = config.getDouble("Gamemode Choice Weight", 1);
		healthModifier = config.getDouble("Health Modifier", 1);
		damageModifier = config.getDouble("Damage Modifier", 1);
		zombieAmountModifier = config.getDouble("Zombie Amount Modifier", 1);
		difficultyModifier = config.getDouble("Difficulty Modifier", 1);
		
		if(config.getStringList("Default Zombie") != null)
			defaultZombies = config.getStringList("Default Zombie");
		if(defaultZombies.isEmpty())
			defaultZombies.add(config.getString("Default Zombie", ZArena.getInstance().getConfig().getString(Constants.DEFAULT_ZOMBIE)));
		
		if(config.getStringList("Default Wolf") != null)
			defaultWolves = config.getStringList("Default Wolf");
		if(defaultWolves.isEmpty())
			defaultWolves.add(config.getString("Default Wolf", ZArena.getInstance().getConfig().getString(Constants.DEFAULT_WOLF)));
		
		if(config.getStringList("Default Skeleton") != null)
			defaultSkeletons = config.getStringList("Default Skeleton");
		if(defaultSkeletons.isEmpty())
			defaultSkeletons.add(config.getString("Default Skeleton", ZArena.getInstance().getConfig().getString(Constants.DEFAULT_SKELETON)));
		
		if(config.getStringList("Start Items") != null)
		{
			for(String arg : config.getStringList("Start Items"))
			{
				if(arg.split("\\s").length > 1 && Utils.parseInt(arg.split("\\s")[1], -1) < 0)	//If the second argument isn't an integer, we can assume it's part of the item
					arg = arg.replaceFirst(" ", "_");
				int amount = 1;
				if(Utils.getConfigArgs(arg).length > 1)
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
		
		//A weird way of ensuring the first created gamemode (the default one) isn't added to this list
		if(gamemodes == null)
			gamemodes = new ArrayList<Gamemode>();
		else
			gamemodes.add(this);
	}
	
	public boolean canBuyItem(String item)
	{
		if(itemCostModifiers.containsKey("ALL"))
			return true;
		if(itemCostModifiers.containsKey("NONE"))
			return false;
		if(itemCostModifiers.containsKey(item.toUpperCase().replaceAll(" ", "_")))
			return true;
		return false;
	}
	
	public boolean canSpawn(String entity)
	{
		if(allowedEntityModifiers.containsKey("ALL"))
			return true;
		if(allowedEntityModifiers.containsKey(entity.toLowerCase().split(".")[0]))
			return true;
		return false;
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
	
	public List<String> getDefaultSkeletons()
	{
		return defaultSkeletons;
	}

	public List<String> getDefaultWolves()
	{
		return defaultWolves;
	}
	
	public List<String> getDefaultZombies()
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
	
	@Override
	public String toString()
	{
		return name;
	}
	
	public static Gamemode getGamemode(String name)
	{
		Gamemode defaultGm = ZArena.getInstance().getGameHandler().defaultGamemode;
		if(defaultGm.getName().replaceAll(" ", "").equalsIgnoreCase(name.replaceAll("_", "")))
			return defaultGm;
		for(Gamemode gm : gamemodes)
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
		for(Gamemode gm : gamemodes)
		{
			totalWeight += (int) (gm.weight * 10);
		}
		Gamemode[] weightedGameModes = new Gamemode[totalWeight];
		for(Gamemode gm : gamemodes)
		{
			for(int weight = (int) (gm.weight * 10); weight > 0; weight--)
			{
				weightedGameModes[totalWeight - 1] = gm;
				totalWeight--;
			}
		}
		if(excludes.containsAll(gamemodes))
			return weightedGameModes[new Random().nextInt(weightedGameModes.length)];
		Gamemode gamemode;
		do
		{
			gamemode = weightedGameModes[new Random().nextInt(weightedGameModes.length)];
		} while(excludes.contains(gamemode));
		return gamemode;
	}
}
