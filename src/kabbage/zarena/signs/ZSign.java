package kabbage.zarena.signs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import kabbage.zarena.GameHandler;
import kabbage.zarena.PlayerStats;
import kabbage.zarena.ZArena;
import kabbage.zarena.ZLevel;
import kabbage.zarena.commands.utils.CommandSenderWrapper;
import kabbage.zarena.utils.Constants;
import kabbage.zarena.utils.LocationSer;

public abstract class ZSign implements Externalizable
{
	private static final long serialVersionUID = "ZSIGN".hashCode(); // DO NOT CHANGE
	private static final int VERSION = 1;
	
	public ZLevel level;
	LocationSer location;
	int price;
	
	/**
	 * Empty constructor for externalization.
	 */
	public ZSign()
	{
	}
	
	public ZSign(ZLevel level, LocationSer location, int price)
	{
		this.level = level;
		this.location = location;
		this.price = price;
	}
	
	public static void attemptCreateSign(ZLevel level, Player player, Block block)
	{
		Sign sign = (Sign) block.getState();
		String firstLine = sign.getLine(0);
		String shopHeader = ZArena.getInstance().getConfig().getString(Constants.SHOP_HEADER, "ZBuy");
		String tollHeader = ZArena.getInstance().getConfig().getString(Constants.TOLL_HEADER, "ZPay");
		if(firstLine.equals(shopHeader) || firstLine.equals(tollHeader))
		{
			if(ZArena.getInstance().getGameHandler().getLevel() == null)
			{
				player.sendMessage(ChatColor.RED + "A level must be loaded to add ZArena signs.");
				return;
			}

			if(new CommandSenderWrapper(player).canCreateLevels())
			{
				ZSign zSign = (firstLine.equals(shopHeader)) ? ZShopSign.attemptCreateSign(level, sign) : 	//Getting the state is necessary to casting 
					ZTollSign.attemptCreateSign(level, sign); 												//the block to a sign...
				if(zSign == null)
					player.sendMessage(ChatColor.RED + "ZArena sign creation failed.");
				else
				{
					player.sendMessage(ChatColor.GREEN + "ZArena sign creation succeeded.");
					ZArena.getInstance().getGameHandler().getLevel().addZSign(zSign);
				}
			}
		}
	}
	
	public static ZSign attemptCreateSign(ZLevel level, Block block)
	{
		String shopHeader = ZArena.getInstance().getConfig().getString(Constants.SHOP_HEADER, "ZBuy");
		String tollHeader = ZArena.getInstance().getConfig().getString(Constants.TOLL_HEADER, "ZPay");
		Sign sign = (Sign) block.getState();
		String firstLine = sign.getLine(0);
		if(firstLine.equals(shopHeader) || firstLine.equals(tollHeader))
		{
			ZSign zSign = (firstLine.equals(shopHeader)) ? ZShopSign.attemptCreateSign(level, sign) : 	//Getting the state is necessary to casting 
				ZTollSign.attemptCreateSign(level, sign); 												//the block to a sign...
			if(zSign != null)
				return zSign;
		}
		return null;
	}
	
	public ZLevel getLevel()
	{
		return level;
	}
	
	public Location getLocation()
	{
		return LocationSer.convertToBukkitLocation(location);
	}
	
	public int getPrice()
	{
		return price;
	}
	
	public boolean isAtLocation(Location location)
	{
		return this.location.equals(LocationSer.convertFromBukkitLocation(location));
	}
	
	public abstract void reload();
	
	public boolean onClick(Player player)
	{
		GameHandler gameHandler = ZArena.getInstance().getGameHandler();
		PlayerStats stats = gameHandler.getPlayerStats().get(player.getName());
		if(stats.getMoney() < price)
		{
			player.sendMessage(ChatColor.RED + "Insufficient funds.");
			return false;
		}
		if(executeClick(player))
		{
			stats.subMoney(price);
			stats.messageStats();
			return true;
		}
		return false;
	}
	
	public abstract boolean executeClick(Player player);
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int ver = in.readInt();
		
		if(ver == 0)
		{
			location = (LocationSer) in.readObject();
			price = in.readInt();
			for(ZLevel level : ZArena.getInstance().getGameHandler().getLevelHandler().getLevels())
			{
				if(level.getZSign(getLocation().getBlock()) != null)
				{
					this.level = level;
				}
			}
		}
		else if(ver == 1)
		{
			location = (LocationSer) in.readObject();
			price = in.readInt();
			level = (ZLevel) in.readObject();
		}
		else
		{
			ZArena.logger.log(Level.WARNING, "An unsupported version of a ZSign failed to load.");
			ZArena.logger.log(Level.WARNING, "The ZSign at: "+location.toString()+" may not be operational.");
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(VERSION);
		
		out.writeObject(location);
		out.writeInt(price);
		out.writeObject(level);
	}
}
