package com.github.zarena;

import com.github.zarena.signs.ZSign;
import com.github.zarena.signs.ZTollSign;
import com.github.zarena.utils.Constants;
import com.github.zarena.utils.LocationSer;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class LevelHandler implements Externalizable
{
	private static final long serialVersionUID = "LEVELHANDLER".hashCode(); // DO NOT CHANGE
	/**
	 * The version of the LevelHandler class. This MUST be incremented whenever the
	 * writeExternal or readExternal methods are changed.
	 */
	private static final int VERSION = 0;
	
	private List<ZLevel> levels = new ArrayList<ZLevel>();
	private Random rnd;
	
	public LevelHandler()
	{
		rnd = new Random();
	}
	
	public void addLevel(ZLevel level)
	{
		levels.add(level);
	}
	
	public void removeLevel(ZLevel level)
	{
		levels.remove(level);
	}
	
	public ZLevel getRandomLevel()
	{
		return levels.get(rnd.nextInt(levels.size()));
	}
	
	public ZLevel getRandomLevel(Collection<ZLevel> exludedLevels)
	{
		//We don't want to ever return null...so if the excludedLevels is a collection of all levels, screw it and just return any damn level
		if(exludedLevels.containsAll(levels))
			return levels.get(rnd.nextInt(levels.size()));
		ZLevel level;
		do
		{
			level = levels.get(rnd.nextInt(levels.size()));
		} while(exludedLevels.contains(level));
		return level;
	}
	
	public ZLevel getLevel(String levelName)
	{
		for(ZLevel level : levels)
		{
			if(level.getName().equalsIgnoreCase(levelName))
				return level;
		}
		return null;
	}
	
	public List<ZLevel> getLevels()
	{
		return levels;
	}

	public void saveLevels()
	{
		for(ZLevel level : levels)
		{
			YamlConfiguration levelYaml = new YamlConfiguration();
			levelYaml.set("root", level.serialize());

			File levelFile = new File(Constants.LEVELS_FOLDER+File.separator+level.getName()+".yml");
			try
			{
				levelYaml.save(levelFile);
			} catch(IOException e)
			{
				e.printStackTrace();
				ZArena.log(Level.WARNING, "ZArena: Couldn't save the the level "+level.getName()+".");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void loadLevels()
	{
		File levelsFolder = new File(Constants.LEVELS_FOLDER);
		levelsFolder.mkdir();
		// Get all files that end in .yml in the levels folder, using a filename filter
		File[] levelFiles = levelsFolder.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".yml");
			}

		});
		for(File levelFile : levelFiles)
		{
			MemorySection root = (MemorySection) YamlConfiguration.loadConfiguration(levelFile).get("root");
			Map<String, Object> rootMap = new HashMap<String, Object>();
			for(String key : root.getKeys(true))
			{
				rootMap.put(key, root.get(key));
			}
			ZLevel level = ZLevel.deserialize(rootMap);
			levels.add(level);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int ver = in.readInt();
		
		if(ver == 0)
		{
			levels = (List<ZLevel>) in.readObject();
			//Handle update from .ext level storage to .yml
			for(ZLevel level : levels)
			{
				for(ZSign sign : level.getZSigns())
				{
					if(!(sign instanceof ZTollSign))
						continue;
					ZTollSign tollSign = (ZTollSign) sign;
					for(LocationSer locationSer : tollSign.oldZSpawns)
					{
						Location location = LocationSer.convertToBukkitLocation(locationSer);
						for(String zSpawnName : level.getZSpawnNames())
						{
							if(level.getZombieSpawn(zSpawnName).equals(location))
								tollSign.zSpawns.add(zSpawnName);
						}
					}
				}
			}
		}
		else
		{
			ZArena.log(Level.SEVERE, "An unsupported version of a LevelHandler failed to load.");
			ZArena.log(Level.SEVERE, "Saved levels may be unplayable!");
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(VERSION);
		
		out.writeObject(levels);
	}
}
