package com.github.zarena;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.github.zarena.signs.ZSign;
import com.github.zarena.signs.ZTollSign;
import com.github.zarena.utils.LocationSer;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;


public class ZLevel implements Externalizable, ConfigurationSerializable
{
	private static final long serialVersionUID = "ZLEVEL".hashCode(); // DO NOT CHANGE
	/**
	 * The version of the ZLevel class. This MUST be incremented whenever the
	 * writeExternal or readExternal methods are changed.
	 */
	private static final int VERSION = 2;
	
	private LocationSer dSpawn;
	private LocationSer iSpawn;
	private Map<String, LocationSer> zSpawns;
	private List<LocationSer> zSpawnLocations;
	private List<String> bossSpawns;	//List of names of zombie spawns that also act as boss spawns
	private List<ZSign> zSigns;
	private String name;
	private String world;

	private Random rnd = new Random();
	private List<LocationSer> inactiveZSpawns = new ArrayList<LocationSer>();
	
	/**
	 * Empty constructor for serialization.
	 */
	public ZLevel()
	{
		zSpawns = new HashMap<String, LocationSer>();
		zSpawnLocations = new ArrayList<LocationSer>();
		bossSpawns = new ArrayList<String>();
		zSigns = new ArrayList<ZSign>();
		inactiveZSpawns = new ArrayList<LocationSer>();
	}
	
	public ZLevel(String name, Location spawn)
	{
		this();
		this.name = name;
		iSpawn = LocationSer.convertFromBukkitLocation(spawn);
		dSpawn = LocationSer.convertFromBukkitLocation(spawn);
		world = spawn.getWorld().getName();
	}
	
	public boolean addBossSpawn(String name)
	{
		if(zSpawns.containsKey(name))
			return bossSpawns.add(name);
		return false;
	}
	
	public void addZombieSpawn(String name, Location location)
	{
		if(zSpawns.containsKey(name))
			removeZombieSpawn(name);
		zSpawns.put(name, LocationSer.convertFromBukkitLocation(location));
		zSpawnLocations.add(LocationSer.convertFromBukkitLocation(location));
	}
	
	public void addZSign(ZSign sign)
	{
		zSigns.add(sign);
	}
	
	public Location getDeathSpawn()
	{
		return LocationSer.convertToBukkitLocation(dSpawn);
	}
	
	public Location getInitialSpawn()
	{
		return LocationSer.convertToBukkitLocation(iSpawn);
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getNearestZombieSpawn(Location location)
	{
		String nameToReturn = null;
		LocationSer loc = LocationSer.convertFromBukkitLocation(location);
		double distance = 64; //If the nearest spawn is farther away than the preset value, then no spawn will be gotten.
		for(Entry<String, LocationSer> entry : zSpawns.entrySet())
		{
			String name = entry.getKey();
			LocationSer spawnLoc = entry.getValue();
			if(spawnLoc.distance(loc) < distance)
			{
				distance = spawnLoc.distance(loc);
				nameToReturn = name;
			}
		}
		return nameToReturn;
	}
	
	public Location getRandomBossSpawn()
	{
		if(bossSpawns.size() == 0)
			return null;
		return LocationSer.convertToBukkitLocation(zSpawns.get(bossSpawns.get(rnd.nextInt(bossSpawns.size()))));
	}
	
	public Location getRandomZombieSpawn()
	{
		if (zSpawnLocations.size() == 0)
			return null;
		Location spawn;
		do
		{
			spawn = LocationSer.convertToBukkitLocation(zSpawnLocations.get(rnd.nextInt(zSpawnLocations.size())));
		} while(inactiveZSpawns.contains(LocationSer.convertFromBukkitLocation(spawn)));
		return spawn;
	}
	
	public String getWorld()
	{
		return world;
	}
	
	public Location getZombieSpawn(String name)
	{
		if(zSpawns.get(name) == null)
			return null;
		return LocationSer.convertToBukkitLocation(zSpawns.get(name));
	}
	
	public List<ZSign> getZSigns()
	{
		return zSigns;
	}
	
	public ZSign getZSign(Block block)
	{
		for(ZSign sign : zSigns)
		{
			if(sign.isAtLocation(block.getLocation()))
				return sign;
		}
		return null;
	}
	
	public ZTollSign getZTollSign(String name)
	{
		for(ZSign sign : zSigns)
		{
			if(sign instanceof ZTollSign)
			{
				ZTollSign tollSign = (ZTollSign) sign;
				if(tollSign.getName().equals(name))
					return tollSign;
			}
		}
		return null;
	}
	
	public Set<String> getZSpawnNames()
	{
		return zSpawns.keySet();
	}
	
	public void reloadSigns()
	{
		//Can't iterate over due to Concurrent Modifications
		for(int i = 0; i < zSigns.size(); i++)
		{
			ZSign sign = zSigns.get(i);
			sign.reload();
		}
	}
	
	public boolean removeBossSpawn(String name)
	{
		return bossSpawns.remove(name);
	}
	
	public boolean removeZombieSpawn(String name)
	{
		LocationSer location = zSpawns.get(name);
		if(location == null)
			return false;
		zSpawns.remove(name);
		zSpawnLocations.remove(location);
		bossSpawns.remove(name);
		return true;
	}
	
	public void removeZSign(ZSign sign)
	{
		if(sign != null)
			zSigns.remove(sign);
		resetInactiveZSpawns();
	}
	
	public void resetInactiveZSpawns()
	{
		List<LocationSer> signActivatedZSpawns = new ArrayList<LocationSer>();
		inactiveZSpawns.clear();
		for(ZSign sign: zSigns)
		{
			if(sign instanceof ZTollSign)
			{
				ZTollSign tollSign = (ZTollSign) sign;
				if(!tollSign.isActive())
					inactiveZSpawns.addAll(tollSign.zSpawns);
				else
					signActivatedZSpawns.addAll(tollSign.zSpawns);
			}
		}
		inactiveZSpawns.removeAll(signActivatedZSpawns);
	}
	
	public void resetSigns()
	{
		for(ZSign sign : zSigns)
		{
			if(sign instanceof ZTollSign)
			{
				ZTollSign tollSign = (ZTollSign) sign;
				tollSign.resetCostBlock();
			}
		}
	}
	
	public void setDeathSpawn(Location location)
	{
		dSpawn = LocationSer.convertFromBukkitLocation(location);
	}
	
	public void setInitialSpawn(Location location)
	{
		iSpawn = LocationSer.convertFromBukkitLocation(location);
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("Name", name);
		map.put("World", world);
		map.put("Initial Spawn", iSpawn);
		map.put("Death Spawn", dSpawn);

		Map<String, Object> zSpawnMap = new TreeMap<String, Object>();
		for(Entry<String, LocationSer> entry : zSpawns.entrySet())
		{
			Map<String, Object> zSpawn = new LinkedHashMap<String, Object>();
			zSpawn.put("Location", entry.getValue());
			zSpawn.put("Boss", bossSpawns.contains(entry.getKey()));
			zSpawnMap.put(entry.getKey(), zSpawn);
		}
		map.put("ZSpawns", zSpawnMap);

		Map<String, Object> zSignMap = new LinkedHashMap<String, Object>();
		Integer index = 0;
		for(ZSign sign : zSigns)
		{
			zSignMap.put((index++).toString(), sign);
		}
		map.put("Signs", zSignMap);

		return map;
	}

	@SuppressWarnings("unchecked")
	public static ZLevel deserialize(Map<String, Object> map)
	{
		ZLevel level = new ZLevel();
		level.name = (String) map.get("Name");
		level.world = (String) map.get("World");
		level.iSpawn = (LocationSer) map.get("Initial Spawn");
		level.dSpawn = (LocationSer) map.get("Death Spawn");

		MemorySection zSpawnSection = (MemorySection) map.get("ZSpawns");
		for(String name : zSpawnSection.getKeys(false))
		{
			MemorySection zSpawn = (MemorySection) zSpawnSection.get(name);
			LocationSer location = (LocationSer) zSpawn.get("Location");
			boolean boss = (Boolean) zSpawn.get("Boss");

			level.zSpawns.put(name, location);
			level.zSpawnLocations.add(location);
			if(boss)
				level.bossSpawns.add(name);
		}

		MemorySection zSignSection = (MemorySection) map.get("Signs");
		for(String key : zSignSection.getKeys(false))
		{
			level.zSigns.add((ZSign) zSignSection.get(key));
		}

		return level;
	}
	
	@Override
	public String toString()
	{
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int ver = in.readInt();
		
		if(ver == 0)
		{
			name = in.readUTF();
			zSpawnLocations = (List<LocationSer>) in.readObject();
			zSpawns = (Map<String, LocationSer>) in.readObject();
			bossSpawns = new ArrayList<String>();
			zSigns = new ArrayList<ZSign>();
			iSpawn = (LocationSer) in.readObject();
			dSpawn = (LocationSer) in.readObject();
		}
		else if(ver == 1)
		{
			name = in.readUTF();
			zSpawnLocations = (List<LocationSer>) in.readObject();
			zSpawns = (Map<String, LocationSer>) in.readObject();
			bossSpawns = new ArrayList<String>();
			zSigns = (List<ZSign>) in.readObject();
			iSpawn = (LocationSer) in.readObject();
			dSpawn = (LocationSer) in.readObject();
		}
		else if(ver == 2)
		{
			name = in.readUTF();
			zSpawnLocations = (List<LocationSer>) in.readObject();
			zSpawns = (Map<String, LocationSer>) in.readObject();
			bossSpawns = (List<String>) in.readObject();
			zSigns = (List<ZSign>) in.readObject();
			iSpawn = (LocationSer) in.readObject();
			dSpawn = (LocationSer) in.readObject();
		}
		else
		{
			ZArena.log(Level.WARNING, "An unsupported version of a ZLevel failed to load.");
			ZArena.log(Level.WARNING, "The ZLevel: "+name+" may not be operational.");
		}
		world = iSpawn.getWorld();
		resetInactiveZSpawns();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(VERSION);
		
		out.writeUTF(name);
		out.writeObject(zSpawnLocations);
		out.writeObject(zSpawns);
		out.writeObject(bossSpawns);
		out.writeObject(zSigns);
		out.writeObject(iSpawn);
		out.writeObject(dSpawn);
	}
}
