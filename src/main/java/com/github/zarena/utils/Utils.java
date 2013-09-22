package com.github.zarena.utils;

import java.io.*;


import net.minecraft.server.v1_6_R3.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftInventoryPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import com.github.zarena.ZArena;

public class Utils
{
	/**
	 * Checks if two text files contain the same contents.
	 * @param file1	first file being compared
	 * @param file2	second file being compared
	 * @return		whether or not the two files contain the same contents
	 */
	public static boolean fileEquals(File file1, File file2)
	{
		if(!file1.exists() || !file2.exists())
			return false;
		try
		{
			new BufferedReader(new InputStreamReader(System.in));
			String s1 = "";
			String s2 = "";
			String y, z;

			BufferedReader bfr1 = new BufferedReader(new FileReader(file1));

			BufferedReader bfr2 = new BufferedReader(new FileReader(file2));

			while ((y = bfr1.readLine()) != null)
				s1 += y;

			while ((z = bfr2.readLine()) != null)
				s2 += z;

			bfr1.close();
			bfr2.close();

			if (s2.equals(s1))
				return true;
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Gets the armor of the player
	 * @param player the player who's armor is being gotten
	 * @return armor points of the player
	 */
	public static int getArmor(Player player)
	{
		PlayerInventory pi = player.getInventory();
		ItemStack helm = null;
		ItemStack chest = null;
		ItemStack pants = null;
		ItemStack boots = null;
		if(pi.getHelmet() != null)
			helm = player.getInventory().getHelmet();
		if(pi.getChestplate() != null)
			chest = player.getInventory().getChestplate();
		if(pi.getLeggings() != null)
			pants= player.getInventory().getLeggings();
		if(pi.getBoots() != null)
			boots = player.getInventory().getBoots();
		int armor = 0;
		if(helm != null) {
			switch (helm.getType())
			{
			case DIAMOND_HELMET:  armor = armor + 3;
			break;
			case IRON_HELMET:  armor = armor + 2;
			break;
			case CHAINMAIL_HELMET:  armor = armor + 2;
			break;
			case GOLD_HELMET:  armor = armor + 2;
			break;
			case LEATHER_HELMET:  armor = armor + 1;
			break;
			default:
				break;	
			}
		}
		if(chest != null) {
			switch (chest.getType())
			{
			case DIAMOND_CHESTPLATE:  armor = armor + 8;
			break;
			case IRON_CHESTPLATE:  armor = armor + 6;
			break;
			case CHAINMAIL_CHESTPLATE:  armor = armor + 5;
			break;
			case GOLD_CHESTPLATE:  armor = armor + 5;
			break;
			case LEATHER_CHESTPLATE:  armor = armor + 3;
			break;
			default:
				break;	
			}
		}
		if(pants != null) {
			switch (pants.getType())
			{
			case DIAMOND_LEGGINGS:  armor = armor + 6;
			break;
			case IRON_LEGGINGS:  armor = armor + 5;
			break;
			case CHAINMAIL_LEGGINGS:  armor = armor + 4;
			break;
			case GOLD_LEGGINGS:  armor = armor + 3;
			break;
			case LEATHER_LEGGINGS:  armor = armor + 2;
			break;
			default:
				break;	
			}	
		}
		if(boots != null) {
			switch (boots.getType())
			{
			case DIAMOND_BOOTS:  armor = armor + 3;
			break;
			case IRON_BOOTS:  armor = armor + 2;
			break;
			case CHAINMAIL_BOOTS:  armor = armor + 1;
			break;
			case GOLD_BOOTS:  armor = armor + 1;
			break;
			case LEATHER_BOOTS:  armor = armor + 1;
			break;
			default:
				break;	
			}		
		}
		return armor;
	}
	
	public static int getWeaponCount(PlayerInventory pm)
	{
		int weapons = 0;
		for(ItemStack i: pm.getContents())
		{
			Material m = null;
			try
			{
				m = i.getType();
			} catch(NullPointerException e)
			{
			}
			if(m != null)
			{
				switch(m)
				{
				case WOOD_SWORD:
				case STONE_SWORD:
				case GOLD_SWORD:
				case IRON_SWORD:
				case DIAMOND_SWORD:
				case WOOD_AXE:
				case STONE_AXE:
				case GOLD_AXE:
				case IRON_AXE:
				case DIAMOND_AXE:
				case BOW:
					weapons++;
					break;
				default:
					break;
				}
			}
		}
		return weapons;
	}
	
	//I suspect the verticalModifier isn't working...
	public static void knockBack(LivingEntity entity, Location source, double horizontalModifier, double verticalModifier)
	{
		if(((CraftLivingEntity) entity).getHandle().hurtTicks < 0)
			return;
		Location playerLocation = entity.getLocation();
		double xPower = source.getX() - playerLocation.getX();
		double yPower = 0.4D;
		double zPower;
		
        for (zPower = source.getZ() - playerLocation.getZ(); xPower * xPower + zPower * zPower < 1.0E-4D; zPower = (Math.random() - Math.random()) * 0.01D)
        {
        	xPower = (Math.random() - Math.random()) * 0.01D;
        }
		
		if (yPower > 0.4000000059604645)
        {
			yPower = 0.4000000059604645;
        }
        
		Vector v = entity.getVelocity();
		double motionX = v.getX();
		double motionY = v.getY();
		double motionZ = v.getZ();
		double horizontal = MathHelper.sqrt(xPower * xPower + zPower * zPower);
		double vertical = yPower;
		motionX /= 2.0D;
		motionY /= 2.0D;
		motionZ /= 2.0D;
		motionX -= xPower / horizontal * vertical;
		motionY += vertical;
		motionZ -= zPower / horizontal * vertical;
		
		motionX *= horizontalModifier;
		motionY *= verticalModifier;
		motionZ *= horizontalModifier;

		entity.setVelocity(new Vector(motionX, motionY, motionZ));
	}
	
	/**
	 * Ease of use method for extracting a File from the plugin .jar file to a specified directory.
	 * @param dir	    directory to extract to
	 * @param fileName	name of file being extracted
     * @param override  whether or not to override if the file already exists
	 * @return	        file that was moved, or file that was already there if there was one, or null if the file is null
	 * @throws IOException
	 */
	public static File extractFromJar(File dir, String fileName, boolean override) throws IOException
	{
        if (!dir.exists())	//Make directory if it doesn't exist
        	dir.mkdirs();
        
        File file = new File(dir, fileName);

        if (file.exists() && !override)
        	return file;
        
        InputStream in = ZArena.class.getResourceAsStream("/"+fileName);	//Get inputstream from base folder
        if (in == null)
        	throw new FileNotFoundException(fileName+" could not be found from the base folder.");

        FileOutputStream out = new FileOutputStream(file);
        byte[] buffer = new byte[1024];	//Create a buffer

        //Have the inputstream read the buffer and write it to it's new directory
        int read;
        while ((read = in.read(buffer)) > 0)
        	out.write(buffer, 0, read);

        //Close up everything
        in.close();
        out.flush();
        out.close();

        return file;
	}

    /**
     * Ease of use method for extracting a File from the plugin .jar file to a specified directory.
     * @param dir	directory to extract to
     * @param fileName	name of file being extracted
     * @return	file that was moved, or file that was already there if there was one, or null if the file is null
     * @throws IOException
     */
    public static File extractFromJar(File dir, String fileName) throws IOException
    {
        return extractFromJar(dir, fileName, false);
    }

	/**
	 * Parse a String to a int
	 * @param toParse	string to be parsed
	 * @param defaultValue	default value to return if 'toParse' is not parsable
	 * @return	int value of 'toParse', or 'defaultValue' if it is not parsable
	 */
	public static int parseInt(String toParse, int defaultValue)
	{
		try
		{
			return Integer.parseInt(toParse);
		} catch(NumberFormatException e)
		{
			return defaultValue;
		}
	}
	
	/**
	 * Parse a String to a double
	 * @param toParse	string to be parsed
	 * @param defaultValue	default value to return if 'toParse' is not parsable
	 * @return	double value of 'toParse', or 'defaultValue' if it is not parsable
	 */
	public static double parseDouble(String toParse, double defaultValue)
	{
		try
		{
			return Double.parseDouble(toParse);
		} catch(NumberFormatException e)
		{
			return defaultValue;
		}
	}
	
	/**
	 * Get arguments from a String gotten from a YAMLConfiguration entry, split by commas.
	 * @param regex	the String to get the arguments from
	 * @return		array of arguments
	 */
	public static String[] getConfigArgs(String regex)
	{
		String[] args = new String[0];
		if(regex.contains(" "))
		{
			String argsNotArray = regex.split("\\s")[1];
			args = argsNotArray.split(",");
		}
		return args;
	}

	/**
	 * Convert to the new config setup
	 * @param oldConfig old config
	 * @param newConfig new config
	 */
	public static void convertToNewConfig(Configuration newConfig, Configuration oldConfig)
	{
		newConfig.set(ConfigEnum.AUTOSTART.toString(), oldConfig.getBoolean("Options.Auto-Start"));
		newConfig.set(ConfigEnum.AUTORUN.toString(), oldConfig.getBoolean("Options.Auto-Run"));
		newConfig.set(ConfigEnum.AUTOJOIN.toString(), oldConfig.getBoolean("Options.Auto-Join"));
		newConfig.set(ConfigEnum.PLAYER_LIMIT.toString(), oldConfig.getInt("Options.Player Limit"));
		if(oldConfig.contains("Entities.Mob Cap"))
			newConfig.set(ConfigEnum.MOB_CAP.toString(), oldConfig.getInt("Entities.Mob Cap"));
		newConfig.set(ConfigEnum.VOTING_LENGTH.toString(), oldConfig.getInt("Options.Voting Length"));
		newConfig.set(ConfigEnum.WAVE_DELAY.toString(), oldConfig.getInt("Options.Wave Start Delay"));
		newConfig.set(ConfigEnum.WORLD_EXCLUSIVE.toString(), oldConfig.getBoolean("Options.World Exclusive"));
		newConfig.set(ConfigEnum.DISABLE_HUNGER.toString(), oldConfig.getBoolean("Options.Disable Hunger"));
		newConfig.set(ConfigEnum.RESPAWN_EVERY_WAVES.toString(), oldConfig.getInt("Options.Respawn Every X Waves (0 to disable)"));
		if(oldConfig.contains("Options.Respawn Every X Minutes (0 to disable)"))
			newConfig.set(ConfigEnum.RESPAWN_EVERY_TIME.toString(), oldConfig.getInt("Options.Respawn Every X Minutes (0 to disable)"));
		newConfig.set(ConfigEnum.START_ITEMS.toString(), oldConfig.getStringList("Options.Start Items"));
		newConfig.set(ConfigEnum.KILL_MONEY.toString(), oldConfig.getInt("Stats.Money on Kill"));
		newConfig.set(ConfigEnum.MONEY_LOST.toString(), oldConfig.getDouble("Stats.Money Percent Lost on Death"));
		if(oldConfig.contains("Stats.Use Vault Economy"))
			newConfig.set(ConfigEnum.USE_VAULT.toString(), oldConfig.getBoolean("Stats.Use Vault Economy"));
		
		newConfig.set(ConfigEnum.HEALTH_STARTING.toString(), oldConfig.getDoubleList("Zombies.Health.Coefficients").get(2));
		newConfig.set(ConfigEnum.HEALTH_INCREASE.toString(), oldConfig.getDoubleList("Zombies.Health.Coefficients").get(1));
		String formulaType = oldConfig.getString("Zombies.Health.Formula Type [Quadratic|Logistic|Logarithmic]");
		if(formulaType.equalsIgnoreCase("Logarithmic"))
			newConfig.set(ConfigEnum.HEALTH_EXPOTENTIAL_INCREASE.toString(), 0.5);
		else
			newConfig.set(ConfigEnum.HEALTH_EXPOTENTIAL_INCREASE.toString(), oldConfig.getDoubleList("Zombies.Health.Coefficients").get(0));
		if(formulaType.equalsIgnoreCase("Logistic"))
		{
			newConfig.set(ConfigEnum.HEALTH_LIMIT.toString(), oldConfig.getInt("Zombies.Health.Limit (Only applicaple for Logistic Forumula)"));
			newConfig.set(ConfigEnum.HEALTH_SOFT_LIMIT.toString(), true);
		} else
		{
			newConfig.set(ConfigEnum.HEALTH_LIMIT.toString(), 0);
			newConfig.set(ConfigEnum.HEALTH_SOFT_LIMIT.toString(), false);
		}

		newConfig.set(ConfigEnum.QUANTITY_STARTING.toString(), oldConfig.getDoubleList("Zombies.Quantity.Coefficients").get(2));
		newConfig.set(ConfigEnum.QUANTITY_INCREASE.toString(), oldConfig.getDoubleList("Zombies.Quantity.Coefficients").get(1));
		formulaType = oldConfig.getString("Zombies.Quantity.Formula Type [Quadratic|Logistic|Logarithmic]");
		if(formulaType.equalsIgnoreCase("Logarithmic"))
			newConfig.set(ConfigEnum.QUANTITY_EXPOTENTIAL_INCREASE.toString(), 0.5);
		else
			newConfig.set(ConfigEnum.QUANTITY_EXPOTENTIAL_INCREASE.toString(), oldConfig.getDoubleList("Zombies.Quantity.Coefficients").get(0));
		if(formulaType.equalsIgnoreCase("Logistic"))
		{
			newConfig.set(ConfigEnum.QUANTITY_LIMIT.toString(), oldConfig.getInt("Zombies.Quantity.Limit (Only applicaple for Logistic Forumula)"));
			newConfig.set(ConfigEnum.QUANTITY_SOFT_LIMIT.toString(), true);
		} else
		{
			newConfig.set(ConfigEnum.QUANTITY_LIMIT.toString(), 0);
			newConfig.set(ConfigEnum.QUANTITY_SOFT_LIMIT.toString(), false);
		}

		if(oldConfig.contains("Donator.Start Money"))
		{
			newConfig.set("Donator.Start Money", null);
			for(String nodeName : oldConfig.getConfigurationSection("Donator.Start Money").getKeys(false))
			{
				ConfigurationSection node = oldConfig.getConfigurationSection("Donator.Start Money." + nodeName);
				for(String key : node.getKeys(false))
					newConfig.set(ConfigEnum.START_MONEY.toString()+"."+node.getName()+"."+key, node.get(key));
			}
		}
		if(oldConfig.contains("Donator.Extra Votes"))
		{
			newConfig.set("Donator.Extra Votes", null);
			for(String nodeName : oldConfig.getConfigurationSection("Donator.Extra Votes").getKeys(false))
			{
				ConfigurationSection node = oldConfig.getConfigurationSection("Donator.Extra Votes." + nodeName);
				for(String key : node.getKeys(false))
					newConfig.set(ConfigEnum.EXTRA_VOTES.toString()+"."+node.getName()+"."+key, node.get(key));
			}
		}
		if(oldConfig.contains("SignCustomItems"))
		{
			newConfig.set("SignCustomItems", null);
			for(String nodeName : oldConfig.getConfigurationSection("SignCustomItems").getKeys(false))
			{
				ConfigurationSection node = oldConfig.getConfigurationSection("SignCustomItems." + nodeName);
				for(String key : node.getKeys(false))
					newConfig.set(ConfigEnum.CUSTOM_ITEMS.toString()+"."+node.getName()+"."+key, node.get(key));
			}
		}

		newConfig.set(ConfigEnum.ALWAYS_NIGHT.toString(), oldConfig.getBoolean("Options.Always Night"));
		newConfig.set(ConfigEnum.QUANTITY_ADJUST.toString(), oldConfig.getBoolean("Options.Adjust Quantity Based on Player Amount"));
		newConfig.set(ConfigEnum.SEPERATE_INVENTORY.toString(), oldConfig.getBoolean("Options.Seperate Inventory"));
		newConfig.set(ConfigEnum.DISABLE_JOIN_WITH_INV.toString(), oldConfig.getBoolean("Options.Disable Joining Game With Inventory"));
		newConfig.set(ConfigEnum.SAVE_POSITION.toString(), oldConfig.getBoolean("Options.Save Position on Game Join"));
		newConfig.set(ConfigEnum.GAME_LEAVE_WORLD.toString(), oldConfig.getString("Options.Game Leave World"));
		newConfig.set(ConfigEnum.GAME_LEAVE_LOCATION.toString(), oldConfig.getDoubleList("Options.Game Leave Location"));
		newConfig.set(ConfigEnum.XP_BAR_IS_MONEY.toString(), oldConfig.getBoolean("Options.XP Bar Shows Money"));
		newConfig.set(ConfigEnum.BROADCAST_ALL.toString(), oldConfig.getBoolean("Options.Broadcast To All"));
		newConfig.set(ConfigEnum.DISABLE_NON_ZA.toString(), oldConfig.getBoolean("Options.Disable Non ZArena Commands In Game"));
		newConfig.set(ConfigEnum.ENABLE_KILLCOUNTER.toString(), oldConfig.getBoolean("Options.Enable Killcounter"));
		if(oldConfig.contains("Options.Set AFK Players as Dead"))
			newConfig.set(ConfigEnum.ENABLE_AFKKICKER.toString(), oldConfig.getBoolean("Options.Set AFK Players as Dead"));
		if(oldConfig.contains("Options.Respawn Reminder Delay (Seconds)"))
			newConfig.set(ConfigEnum.RESPAWN_REMINDER_DELAY.toString(), oldConfig.getInt("Options.Respawn Reminder Delay (Seconds)"));
		newConfig.set(ConfigEnum.SHOP_HEADER.toString(), oldConfig.getString("Options.Shop Sign Header"));
		newConfig.set(ConfigEnum.TOLL_HEADER.toString(), oldConfig.getString("Options.Toll Sign Header"));
		newConfig.set(ConfigEnum.WOLF_PERCENT_SPAWN.toString(), oldConfig.getDouble("Entities.Wolf Spawn Chance"));
		newConfig.set(ConfigEnum.SKELETON_PERCENT_SPAWN.toString(), oldConfig.getDouble("Entities.Skeleton Spawn Chance"));
		newConfig.set(ConfigEnum.WOLF_WAVE_PERCENT_OCCUR.toString(), oldConfig.getDouble("Entities.Wolf Wave Chance"));
		newConfig.set(ConfigEnum.SKELETON_WAVE_PERCENT_OCCUR.toString(), oldConfig.getDouble("Entities.Skeleton Wave Chance"));
		newConfig.set(ConfigEnum.WOLF_WAVE_PERCENT_SPAWN.toString(), oldConfig.getDouble("Entities.Wolf Spawn Chance During Wolf Wave"));
		newConfig.set(ConfigEnum.SKELETON_WAVE_PERCENT_SPAWN.toString(), oldConfig.getDouble("Entities.Skeleton Spawn Chance During Wolf Wave"));
		newConfig.set(ConfigEnum.DEFAULT_ZOMBIE.toString(), oldConfig.getString("Entities.Default Entity File Name"));
		newConfig.set(ConfigEnum.DEFAULT_SKELETON.toString(), oldConfig.getString("Entities.Default Skeleton File Name"));
		newConfig.set(ConfigEnum.DEFAULT_WOLF.toString(), oldConfig.getString("Entities.Default Wolf File Name"));
		newConfig.set(ConfigEnum.DEFAULT_GAMEMODE.toString(), oldConfig.getString("Gamemodes.Default Gamemode File Name"));
	}

	public static CraftInventoryPlayer loadOfflinePlayerInventory(String playerName) throws IOException
	{
		NBTTagList list = (NBTTagList) getOfflinePlayerTagValue(playerName, "Inventory");
		net.minecraft.server.v1_6_R3.PlayerInventory pi = new net.minecraft.server.v1_6_R3.PlayerInventory(null);
		pi.b(list);
		return new CraftInventoryPlayer(pi);
	}

	public static void saveOfflinePlayerInventory(String playerName, CraftInventoryPlayer ci) throws IOException
	{
		setOfflinePlayerTagValue(playerName, "Inventory", ci.getInventory().a(new NBTTagList()));
	}

	public static Object getOfflinePlayerTagValue(String playerName, String tagName) throws IOException
	{
		String pathToPlayerFile = "world"+File.separator+"players"+File.separator+playerName+".dat";
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(pathToPlayerFile);
			NBTTagCompound compound = NBTCompressedStreamTools.a(is);

			switch(compound.get(tagName).getTypeId())
			{
				case 0: return null;
				case 1: return compound.getByte(tagName);
				case 2: return compound.getShort(tagName);
				case 3: return compound.getInt(tagName);
				case 4: return compound.getLong(tagName);
				case 5: return compound.getFloat(tagName);
				case 6: return compound.getDouble(tagName);
				case 7: return compound.getByteArray(tagName);
				case 8: return compound.getString(tagName);
				case 9: return compound.getList(tagName);
				case 10: return compound.getCompound(tagName);
				case 11: return compound.getIntArray(tagName);
				default: return null;
			}
		} finally
		{
			if(is != null)
				is.close();
		}
	}

	public static void setOfflinePlayerTagValue(String playerName, String tagName, Object tagValue) throws IOException
	{
		String pathToPlayerFile = "world"+File.separator+"players"+File.separator+playerName+".dat";
		FileInputStream is = null;
		FileOutputStream os = null;
		try
		{
			is = new FileInputStream(pathToPlayerFile);
			NBTTagCompound compound = NBTCompressedStreamTools.a(is);

			if(tagValue instanceof Boolean || tagValue.getClass().isAssignableFrom(boolean.class))
				compound.setBoolean(tagName, (Boolean) tagValue);
			else if(tagValue instanceof Byte || tagValue.getClass().isAssignableFrom(byte.class))
				compound.setByte(tagName, (Byte) tagValue);
			else if(tagValue instanceof Short || tagValue.getClass().isAssignableFrom(short.class))
				compound.setShort(tagName, (Short) tagValue);
			else if(tagValue instanceof Integer || tagValue.getClass().isAssignableFrom(int.class))
				compound.setInt(tagName, (Integer) tagValue);
			else if(tagValue instanceof Long || tagValue.getClass().isAssignableFrom(long.class))
				compound.setLong(tagName, (Long) tagValue);
			else if(tagValue instanceof Float || tagValue.getClass().isAssignableFrom(float.class))
				compound.setFloat(tagName, (Float) tagValue);
			else if(tagValue instanceof Double || tagValue.getClass().isAssignableFrom(double.class))
				compound.setDouble(tagName, (Double) tagValue);
			else if(tagValue instanceof byte[])
				compound.setByteArray(tagName, (byte[]) tagValue);
			else if(tagValue instanceof String)
				compound.setString(tagName, (String) tagValue);
			else if(tagValue instanceof NBTTagCompound)
				compound.setCompound(tagName, (NBTTagCompound) tagValue);
			else if(tagValue instanceof int[])
				compound.setIntArray(tagName, (int[]) tagValue);
			else if(tagValue instanceof NBTBase)
				compound.set(tagName, (NBTBase) tagValue);
			else
				throw new IllegalArgumentException("The type of the new value must be an NBT supported type.");
			os = new FileOutputStream(pathToPlayerFile);
			NBTCompressedStreamTools.a(compound, os);
		} finally
		{
			if(is != null)
				is.close();
			if(os != null)
				os.close();
		}
	}
}
