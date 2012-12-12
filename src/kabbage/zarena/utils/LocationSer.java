package kabbage.zarena.utils;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import kabbage.zarena.ZArena;

import org.bukkit.Bukkit;

/**
 * @author joshua
 */
public class LocationSer implements Externalizable
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
	 * Empty constructor for externalization.
	 */
    public LocationSer()
    {
    }

    /**
     *
     * @param world
     * @param x
     * @param y
     * @param z
     */
    public LocationSer(String world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     *
     * @return a string representation of the location's world
     */
    public String getWorld() {
        return world;
    }

    /**
     *
     * @param world
     */
    public void setWorld(String world) {
        this.world = world;
    }

    /**
     *
     * @return the x coordinate of the location
     */
    public double getX() {
        return x;
    }

    /**
     *
     * @param x
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     *
     * @return the y coordinate of the location
     */
    public double getY() {
        return y;
    }

    /**
     *
     * @param y
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     *
     * @return the z coordinate of the location
     */
    public double getZ() {
        return z;
    }

    /**
     *
     * @param z
     */
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LocationSer other = (LocationSer) obj;

        if (this.world != other.world && (this.world == null || !this.world.equals(other.world))) {
            return false;
        }
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.world != null ? this.world.hashCode() : 0);
        hash = (int) (47 * hash + this.x);
        hash = (int) (47 * hash + this.y);
        hash = (int) (47 * hash + this.z);
        return hash;
    }
    
    @Override
    public String toString() {
        return "Location{" + "world=" + world + ",x=" + x + ",y=" + y + ",z=" + z + '}';
    }

    /**
     *
     * @param loc
     * @return
     */
    public static LocationSer convertFromBukkitLocation(org.bukkit.Location loc) {
        return new LocationSer(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     *
     * @param s
     * @param loc
     * @return
     */
    public static org.bukkit.Location convertToBukkitLocation(LocationSer loc) {
        return new org.bukkit.Location(Bukkit.getServer().getWorld(loc.world), loc.getX(), loc.getY(), loc.getZ());
    }

    public List<String> toList() {
        LinkedList<String> ret = new LinkedList<>();
        ret.add("" + x);
        ret.add("" + y);
        ret.add("" + z);
        ret.add(world);

        return ret;
    }
    
    public double distance(LocationSer l)
    {
    	return Math.sqrt(Math.pow(l.x-this.x, 2) + Math.pow(l.y-this.y,2) + Math.pow(l.z-this.z, 2));
    }

    public static LocationSer fromList(List<String> list) {
    	LocationSer ret = new LocationSer(null, 0, 0, 0);

        ret.x = Double.parseDouble(list.get(0));
        ret.y = Double.parseDouble(list.get(1));
        ret.z = Double.parseDouble(list.get(2));
        ret.world = list.get(3);

        return ret;
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
			ZArena.logger.log(Level.WARNING, "An unsupported version of an externalized Location failed to load.");
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
}
