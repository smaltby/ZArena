package com.github.zarena.utils;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;


import org.bukkit.Bukkit;

import com.github.zarena.ZArena;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

/**
 * @author joshua
 */
public class LocationSer implements Externalizable, ConfigurationSerializable
{
	private static final long serialVersionUID = "LOCATIONSER".hashCode(); // DO NOT CHANGE
	/**
	 * The version of the LocationSer class. This MUST be incremented whenever the
	 * writeExternal or readExternal methods are changed.
	 */
	private static int VERSION = 0;

	private String world;
    private double x, y, z;
    
    /**
	 * Empty constructor for serialization.
	 */
    public LocationSer()
    {
    }

    public LocationSer(String world, double x, double y, double z)
	{
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     *
     * @return a string representation of the location's world
     */
    public String getWorld()
	{
        return world;
    }

    public void setWorld(String world)
	{
        this.world = world;
    }

    /**
     *
     * @return the x coordinate of the location
     */
    public double getX()
	{
        return x;
    }

    public void setX(double x)
	{
        this.x = x;
    }

    /**
     *
     * @return the y coordinate of the location
     */
    public double getY()
	{
        return y;
    }

    public void setY(double y)
	{
        this.y = y;
    }

    /**
     *
     * @return the z coordinate of the location
     */
    public double getZ()
	{
        return z;
    }

    public void setZ(double z)
	{
        this.z = z;
    }

    @Override
    public boolean equals(Object obj)
	{
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final LocationSer other = (LocationSer) obj;

        if (!this.world.equals(other.world))
            return false;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y))
            return false;
		return Double.doubleToLongBits(this.z) == Double.doubleToLongBits(other.z);
	}

    @Override
    public int hashCode()
	{
        int hash = 7;
        hash = 47 * hash + (this.world != null ? this.world.hashCode() : 0);
        hash = (int) (47 * hash + this.x);
        hash = (int) (47 * hash + this.y);
        hash = (int) (47 * hash + this.z);
        return hash;
    }
    
    @Override
    public String toString()
	{
        return "Location{" + "world=" + world + ",x=" + x + ",y=" + y + ",z=" + z + '}';
    }

    public static LocationSer convertFromBukkitLocation(org.bukkit.Location loc)
	{
        return new LocationSer(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    public static org.bukkit.Location convertToBukkitLocation(LocationSer loc)
	{
        return new org.bukkit.Location(Bukkit.getServer().getWorld(loc.world), loc.getX(), loc.getY(), loc.getZ());
    }
    
    public double distance(LocationSer l)
    {
    	return Math.sqrt(Math.pow(l.x-this.x, 2) + Math.pow(l.y-this.y,2) + Math.pow(l.z-this.z, 2));
    }
    
    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int ver = in.readInt();
		
		if(ver == 0)
		{
			world = in.readUTF();
			x = in.readDouble();
			y = in.readDouble();
			z = in.readDouble();
		}
		else
		{
			ZArena.log(Level.WARNING, "An unsupported version of an externalized Location failed to load.");
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(VERSION);
		
		out.writeUTF(world);
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeDouble(z);
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("World", world);
		map.put("X", x);
		map.put("Y", y);
		map.put("Z", z);

		return map;
	}

	public static LocationSer deserialize(Map<String, Object> map)
	{
		LocationSer locationSer = new LocationSer();
		locationSer.world = (String) map.get("World");
		locationSer.x = (Double) map.get("X");
		locationSer.y = (Double) map.get("Y");
		locationSer.z = (Double) map.get("Z");

		return locationSer;
	}
}
