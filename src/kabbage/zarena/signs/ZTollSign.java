package kabbage.zarena.signs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import net.minecraft.server.v1_5_R1.BlockDoor;
import net.minecraft.server.v1_5_R1.BlockTrapdoor;
import net.minecraft.server.v1_5_R1.EntityPlayer;
import net.minecraft.server.v1_5_R1.MinecraftServer;
import net.minecraft.server.v1_5_R1.PlayerInteractManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_5_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import kabbage.zarena.ZArena;
import kabbage.zarena.ZLevel;
import kabbage.zarena.utils.LocationSer;
import kabbage.zarena.utils.StringEnums;

public class ZTollSign extends ZSign implements Externalizable
{
	private static final long serialVersionUID = "ZTOLLSIGN".hashCode(); // DO NOT CHANGE
	private static final int VERSION = 3;
	
	private LocationSer costBlockLocation; //The location of the block that costs money to be used
	private boolean usableOnce;	//Whether or not this sign can only be used once
	private boolean opposite; 	//The sign's costblock starts out open/on, as opposed to closed/off
	private boolean noReset; 	//The sign's costblock doesn't reset, staying the same across multiple games
	private boolean active;
	private String name;
	
	public List<LocationSer> zSpawns;	//List of zSpawns that are only active when this sign is active
	
	/**
	 * Empty constructor for externalization.
	 */
	public ZTollSign()
	{
		resetCostBlock();
	}
	
	public ZTollSign(ZLevel level, Location location, LocationSer costBlockLocation, int price, String name, String[] flags)
	{
		super(level, LocationSer.convertFromBukkitLocation(location), price);
		this.costBlockLocation = costBlockLocation;
		this.name = name;
		active = false;
		zSpawns = new ArrayList<LocationSer>();
		for(String flag : flags)
		{
			switch(StringEnums.valueOf(flag.toUpperCase().replaceAll("-", "")))
			{
			case UO: case USABLEONCE:
				setUsableOnce(true);
				break;
			case OP: case OPPOSITE:
				setOpposite(true);
				break;
			case NR: case NORESET:
				setNoReset(true);
				break;
			default:
			}
		}
	}
	
	public static ZTollSign attemptCreateSign(ZLevel level, Sign sign)
	{
		String[] lines = sign.getLines();
		
		int price;
		try
		{
			price = Integer.parseInt(lines[1]);
		} catch(NumberFormatException e) //Second line isn't a number, and can't have a price put to it
		{
			return null;
		}
		LocationSer costBlockLocation = getTollableBlock(ZSign.getBlockOn(sign).getLocation());
		if(costBlockLocation == null)
			costBlockLocation = getTollableBlock(sign.getLocation());
		if(costBlockLocation == null)
			return null;
		
		String[] flags = lines[2].split("\\s");
		sign.setLine(2, "");
		
		if(lines[3] == null)
			return null;
		return new ZTollSign(level, sign.getLocation(), costBlockLocation, price, lines[3], flags);
	}
	
	private boolean canBeUsed()
	{
		boolean usable = true;
		if(active && !opposite && usableOnce)
			usable = false;
		else if(!active && opposite && usableOnce)
			usable = false;
		return usable;
	}
	
	public Block getCostBlock()
	{
		if(costBlockLocation != null)
			return LocationSer.convertToBukkitLocation(costBlockLocation).getBlock();
		return null;
	}
	
	public String getName()
	{
		return name;
	}
	
	private static LocationSer getTollableBlock(Location pos)
	{
		BlockFace[] verticalFaces = new BlockFace[] {BlockFace.UP, BlockFace.SELF, BlockFace.DOWN};
		BlockFace[] horizontalFaces = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.SELF};
		
		for (BlockFace bf : verticalFaces)
		{
			Block current = pos.getBlock().getRelative(bf);
			for (BlockFace bf2 : horizontalFaces)
			{
				if (current.getRelative(bf2).getType() == Material.LEVER
						|| current.getRelative(bf2).getType() == Material.WOODEN_DOOR || current.getRelative(bf2).getType() == Material.TRAP_DOOR
						|| current.getRelative(bf2).getType() == Material.WOOD_BUTTON || current.getRelative(bf2).getType() == Material.STONE_BUTTON
						|| current.getRelative(bf2).getType() == Material.IRON_DOOR_BLOCK) 
				{
					return LocationSer.convertFromBukkitLocation(current.getRelative(bf2).getLocation());
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean executeClick(Player player)
	{
		Block costBlock = getCostBlock();
		net.minecraft.server.v1_5_R1.Block nmsBlock = net.minecraft.server.v1_5_R1.Block.byId[costBlock.getType().getId()];
		net.minecraft.server.v1_5_R1.World nmsWorld = ((CraftWorld) costBlock.getWorld()).getHandle();
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		switch(costBlock.getType())
		{
		case WOODEN_DOOR: case IRON_DOOR: case IRON_DOOR_BLOCK:
			if(!active && canBeUsed())
			{
				((BlockDoor) nmsBlock).setDoor(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), true);
				active = true;
			}
			else if(active && canBeUsed())
			{
				((BlockDoor) nmsBlock).setDoor(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), false);
				active = false;
			}
			else
				return false;
			return true;
		case TRAP_DOOR:
			if(!active && canBeUsed())
			{
				((BlockTrapdoor) nmsBlock).setOpen(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), true);
				active = true;
			}
			else if(active && canBeUsed())
			{
				((BlockTrapdoor) nmsBlock).setOpen(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), false);
				active = false;
			}
			else
				return false;
			return true;
		case LEVER:
			if(!active && canBeUsed())
			{
				nmsBlock.interact(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), nmsPlayer, 0, 0f, 0f, 0f);
				active = true;
			}
			else if(active && canBeUsed())
			{
				nmsBlock.interact(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), nmsPlayer, 0, 0f, 0f, 0f);
				active = false;
			}
			else
				return false;
			return true;
		case STONE_BUTTON: case WOOD_BUTTON:
			if(canBeUsed())
			{
				nmsBlock.interact(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), nmsPlayer, 0, 0f, 0f, 0f);
				active = true;
			}
			else
				return false;
			return true;
		default:
			return false;
		}
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public boolean isOpposite()
	{
		return opposite;
	}
	
	public boolean isNoReset()
	{
		return noReset;
	}
	
	public boolean isUsableOnce()
	{
		return usableOnce;
	}
	
	public void reload()
	{
		if(!(getLocation().getBlock().getState() instanceof Sign))
		{
			ZArena.logger.log(Level.INFO, "The sign at "+location.toString()+" has been removed due to it's sign having been removed;");
			level.removeZSign(this);
			return;
		}
		if(getCostBlock() == null)
		{
			costBlockLocation = getTollableBlock(ZSign.getBlockOn(getSign()).getLocation());
			if(getCostBlock() == null)
				costBlockLocation = getTollableBlock(getLocation());
			if(getCostBlock() == null)
			{
				ZArena.logger.log(Level.INFO, "The sign at "+location.toString()+" has been removed due to the block it tolls having been removed.");
				level.removeZSign(this);
			}
		}
	}
	
	public void resetCostBlock()
	{
		if(noReset)
			return;
		Block costBlock = getCostBlock();
		if(costBlock == null)
			return;
		net.minecraft.server.v1_5_R1.Block nmsBlock = net.minecraft.server.v1_5_R1.Block.byId[costBlock.getType().getId()];
		net.minecraft.server.v1_5_R1.World nmsWorld = ((CraftWorld) costBlock.getWorld()).getHandle();
		//We'll name him...Joe!
		EntityPlayer player = new EntityPlayer(MinecraftServer.getServer(), nmsWorld, "Joe", new PlayerInteractManager(nmsWorld));
		switch(costBlock.getType())
		{
		case WOODEN_DOOR: case IRON_DOOR: case IRON_DOOR_BLOCK:
			((BlockDoor) nmsBlock).setDoor(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), opposite);
			active = opposite;
			break;
		case TRAP_DOOR:
			((BlockTrapdoor) nmsBlock).setOpen(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), opposite);
			active = opposite;
			break;
		case LEVER:
			if(8 - (nmsWorld.getData(costBlock.getX(), costBlock.getY(), costBlock.getZ()) & 8) != 8 && !opposite)
				nmsBlock.interact(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), player, 0, 0f, 0f, 0f);
			else if(8 - (nmsWorld.getData(costBlock.getX(), costBlock.getY(), costBlock.getZ()) & 8) == 8 && opposite)
				nmsBlock.interact(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), player, 0, 0f, 0f, 0f);
			active = opposite;
			break;
		case STONE_BUTTON: case WOOD_BUTTON:
			active = opposite;
			break;
		default:
		}
		//Bye bye Joe :(
		Arrays.asList(player.server.getPlayers()).remove(player.getName());
	}
	
	public void setNoReset(boolean noReset)
	{
		this.noReset = noReset;
	}
	
	public void setOpposite(boolean opposite)
	{
		this.opposite = opposite;
	}

	public void setUsableOnce(boolean usable)
	{
		usableOnce = usable;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		int ver = in.readInt();
		
		if(ver == 0)
		{
			costBlockLocation = (LocationSer) in.readObject();
			active = false;
			name = generateString(new Random(), "abcdefghigsadjas", 10);
			zSpawns = new ArrayList<LocationSer>();
			usableOnce = false;
		}
		else if(ver == 1)
		{
			costBlockLocation = (LocationSer) in.readObject();
			active = in.readBoolean();
			name = in.readUTF();
			zSpawns = new ArrayList<LocationSer>();
			usableOnce = false;
		}
		else if(ver == 2)
		{
			costBlockLocation = (LocationSer) in.readObject();
			active = in.readBoolean();
			name = in.readUTF();
			zSpawns = (List<LocationSer>) in.readObject();
			usableOnce = false;
		}
		else if(ver == 3)
		{
			costBlockLocation = (LocationSer) in.readObject();
			active = in.readBoolean();
			name = in.readUTF();
			zSpawns = (List<LocationSer>) in.readObject();
			usableOnce = in.readBoolean();
		}
		else
		{
			ZArena.logger.log(Level.WARNING, "An unsupported version of a ZTollSign failed to load.");
			ZArena.logger.log(Level.WARNING, "The ZSign at: "+location.toString()+" may not be operational.");
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeInt(VERSION);
		
		out.writeObject(costBlockLocation);
		out.writeBoolean(active);
		out.writeUTF(name);
		out.writeObject(zSpawns);
		out.writeBoolean(usableOnce);
	}
	
	private static String generateString(Random rng, String characters, int length)
	{
	    char[] text = new char[length];
	    for (int i = 0; i < length; i++)
	    {
	        text[i] = characters.charAt(rng.nextInt(characters.length()));
	    }
	    return new String(text);
	}
}
