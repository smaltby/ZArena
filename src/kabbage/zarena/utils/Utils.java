package kabbage.zarena.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import kabbage.zarena.ZArena;

import net.minecraft.server.v1_4_5.MathHelper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class Utils
{
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
	 * Checks if two text files contain the same contents.
	 * @param file1	first file being compared
	 * @param file2	second file being compared
	 * @return		whether or not the two files contain the same contents
	 */
	public static boolean fileEquals(File file1, File file2)
	{
		try
		{
			new BufferedReader(new InputStreamReader(System.in));
			String s1 = "";
			String s2 = "";
			String y = "", z = "";

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
			} catch(NullPointerException e) {
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
		motionX -= xPower / (double)horizontal * (double)vertical;
		motionY += (double)vertical;
		motionZ -= zPower / (double)horizontal * (double)vertical;
		
		motionX *= horizontalModifier;
		motionY *= verticalModifier;
		motionZ *= horizontalModifier;

		entity.setVelocity(new Vector(motionX, motionY, motionZ));
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
        if (!dir.exists())	//Make directory if it doesn't exist
        	dir.mkdirs();
        
        File file = new File(dir, fileName);
        
        if (file.exists())	//What we are extracting from the jar file are default files. If the files are already there, we don't want to overwrite them
        	return file;
        
        InputStream in = ZArena.class.getResourceAsStream("/res/"+fileName);	//Get inputstream, all jar files for ZArena are stored in the 'res' folder
        if (in == null)	//If it's null, return null
        	return null;

        FileOutputStream out = new FileOutputStream(file);
        byte[] buffer = new byte[1024];	//Create a buffer

        //Have the inputstream read the buffer and write it to it's new directory
        int read = 0;
        while ((read = in.read(buffer)) > 0)
        	out.write(buffer, 0, read);

        //Close up everything
        in.close();
        out.flush();
        out.close();

        return file;
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
}
