package kabbage.zarena;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;

import kabbage.zarena.signs.ZSign;
import kabbage.zarena.signs.ZTollSign;
import kabbage.zarena.utils.LocationSer;

public class ZLevel implements Externalizable
{
	private static final long serialVersionUID = "ZLEVEL".hashCode(); // DO NOT CHANGE
	/**
	 * The version of the ZLevel class. This MUST be incremented whenever the
	 * writeExternal or readExternal methods are changed.
	 */
	private static int VERSION = 2;
	
	private LocationSer dSpawn;
	private LocationSer iSpawn;
	private Map<String, LocationSer> zSpawns;
	private List<LocationSer> activeZSpawns;
	private List<String> bossSpawns;	//List of names of zombie spawns that also act as boss spawns
	private List<ZSign> zSigns;
	private String name;
	private String world;
	//There's no real point in making them transient. But screw it, I never get to use this keyword...
	private transient Random rnd = new Random();
	private transient List<LocationSer> inactiveZSpawns = new ArrayList<LocationSer>();
	
	/**
	 * Empty constructor for externalization.
	 */
	public ZLevel()
	{
		inactiveZSpawns = new ArrayList<LocationSer>();
	}
	
	public ZLevel(String name, Location spawn)
	{
		this();
		this.name = name;
		iSpawn = LocationSer.convertFromBukkitLocation(spawn);
		dSpawn = LocationSer.convertFromBukkitLocation(spawn);
		zSpawns = new HashMap<String, LocationSer>();
		activeZSpawns = new ArrayList<LocationSer>();
		bossSpawns = new ArrayList<String>();
		zSigns = new ArrayList<ZSign>();
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
		activeZSpawns.add(LocationSer.convertFromBukkitLocation(location));
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
		if (activeZSpawns.size() == 0)
			return null;
		Location spawn = null;
		do
		{
			spawn = LocationSer.convertToBukkitLocation(activeZSpawns.get(rnd.nextInt(activeZSpawns.size())));
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
		activeZSpawns.remove(location);
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
			activeZSpawns = (List<LocationSer>) in.readObject();
			zSpawns = (Map<String, LocationSer>) in.readObject();
			bossSpawns = new ArrayList<String>();
			zSigns = new ArrayList<ZSign>();
			iSpawn = (LocationSer) in.readObject();
			dSpawn = (LocationSer) in.readObject();
		}
		else if(ver == 1)
		{
			name = in.readUTF();
			activeZSpawns = (List<LocationSer>) in.readObject();
			zSpawns = (Map<String, LocationSer>) in.readObject();
			bossSpawns = new ArrayList<String>();
			zSigns = (List<ZSign>) in.readObject();
			iSpawn = (LocationSer) in.readObject();
			dSpawn = (LocationSer) in.readObject();
		}
		else if(ver == 2)
		{
			name = in.readUTF();
			activeZSpawns = (List<LocationSer>) in.readObject();
			zSpawns = (Map<String, LocationSer>) in.readObject();
			bossSpawns = (List<String>) in.readObject();
			zSigns = (List<ZSign>) in.readObject();
			iSpawn = (LocationSer) in.readObject();
			dSpawn = (LocationSer) in.readObject();
		}
		else
		{
			ZArena.logger.log(Level.WARNING, "An unsupported version of a ZLevel failed to load.");
			ZArena.logger.log(Level.WARNING, "The ZLevel: "+name+" may not be operational.");
		}
		resetInactiveZSpawns();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(VERSION);
		
		out.writeUTF(name);
		out.writeObject(activeZSpawns);
		out.writeObject(zSpawns);
		out.writeObject(bossSpawns);
		out.writeObject(zSigns);
		out.writeObject(iSpawn);
		out.writeObject(dSpawn);
	}
}
