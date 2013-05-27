package com.github.zarena.signs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.github.zarena.GameHandler;
import com.github.zarena.PlayerStats;
import com.github.zarena.ZArena;
import com.github.zarena.ZLevel;
import com.github.zarena.commands.CommandSenderWrapper;
import com.github.zarena.utils.ChatHelper;
import com.github.zarena.utils.Constants;
import com.github.zarena.utils.LocationSer;
import com.github.zarena.utils.Message;

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
	
	public static ZSign attemptCreateSign(ZLevel level, Location location, Player player, String[] lines)
	{
		String firstLine = lines[0];
		String shopHeader = ZArena.getInstance().getConfig().getString(Constants.SHOP_HEADER, "ZBuy");
		String tollHeader = ZArena.getInstance().getConfig().getString(Constants.TOLL_HEADER, "ZPay");
		if(firstLine.equals(shopHeader) || firstLine.equals(tollHeader))
		{
			if(ZArena.getInstance().getGameHandler().getLevel() == null)
			{
				ChatHelper.sendMessage(Message.SIGN_PLACE_WITH_NO_LEVEL.formatMessage(), player);
				return null;
			}

			if(new CommandSenderWrapper(player).canCreateLevels())
			{
				ZSign zSign = (firstLine.equals(shopHeader)) ? ZShopSign.attemptCreateSign(level, location, lines) :
					ZTollSign.attemptCreateSign(level, location, lines);
				if(zSign == null)
					ChatHelper.sendMessage(Message.SIGN_PLACE_FAILURE.formatMessage(), player);
				else
				{
					ChatHelper.sendMessage(Message.SIGN_PLACE_SUCCESS.formatMessage(), player);
					ZArena.getInstance().getGameHandler().getLevel().addZSign(zSign);
					return zSign;
				}
			}
		}
		return null;
	}
	
	public static ZSign attemptCreateSign(ZLevel level, Location location, String[] lines)
	{
		String shopHeader = ZArena.getInstance().getConfig().getString(Constants.SHOP_HEADER, "ZBuy");
		String tollHeader = ZArena.getInstance().getConfig().getString(Constants.TOLL_HEADER, "ZPay");
		String firstLine = lines[0];
		if(firstLine.equals(shopHeader) || firstLine.equals(tollHeader))
		{
			ZSign zSign = (firstLine.equals(shopHeader)) ? ZShopSign.attemptCreateSign(level, location, lines) :
				ZTollSign.attemptCreateSign(level, location, lines);
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
	
	public Sign getSign()
	{
		return (Sign) getLocation().getBlock().getState();
	}
	
	public static Block getBlockOn(Location location)
	{
		org.bukkit.material.Sign s = (org.bukkit.material.Sign) location.getBlock().getState().getData();
		if(((Sign)location.getBlock().getState()).getType() == Material.WALL_SIGN)
			return location.getBlock().getRelative(s.getAttachedFace());
		return location.getBlock().getRelative(BlockFace.DOWN);
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
			ChatHelper.sendMessage(Message.INSUFFICIENT_FUNDS.formatMessage(), player);
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
			ZArena.log(Level.WARNING, "An unsupported version of a ZSign failed to load.");
			ZArena.log(Level.WARNING, "The ZSign at: "+location.toString()+" may not be operational.");
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
