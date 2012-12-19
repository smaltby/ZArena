package kabbage.zarena.signs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

import kabbage.zarena.Gamemode;
import kabbage.zarena.ZArena;
import kabbage.zarena.ZLevel;
import kabbage.zarena.utils.LocationSer;

public class ZShopSign extends ZSign implements Externalizable
{
	private static final long serialVersionUID = "ZSHOPSIGN".hashCode(); // DO NOT CHANGE
	private static final int VERSION = 0;
	
	private int type;
	private int amount;
	private short damage;
	private byte id;
	
	/**
	 * Empty constructor for externalization.
	 */
	public ZShopSign()
	{
	}
	
	public ZShopSign(ZLevel level, Location location, ItemStack item, int price)
	{
		this(level, location, item.getType().getId(), item.getAmount(), item.getDurability(), item.getData().getData(), price);
	}
	
	public ZShopSign(ZLevel level, Location location, int type, int amount, short damage, byte id, int price)
	{
		super(level, LocationSer.convertFromBukkitLocation(location), price);
		this.type = type;
		this.amount = amount;
		this.damage = damage;
		this.id = id;
	}
	
	public static ZShopSign attemptCreateSign(ZLevel level, Sign sign)
	{
		String[] lines = sign.getLines();
		
		int price;
		try
		{
			price = Integer.parseInt(lines[3]);
		} catch(NumberFormatException e) //Fourth line isn't a number, and can't have a price put to it
		{
			return null;
		}
		
		ZSignCustomItem customItem = ZSignCustomItem.getCustomItem(new String[]{lines[1], lines[2]}); //First, check to see if there's a custom item with the name
		if(customItem != null)
			return new ZShopSign(level, sign.getLocation(), customItem.getItem(), price);
		
		String name = (!lines[2].isEmpty()) ? lines[1] + "_" + lines[2] : lines[1];
		Material material = Material.getMaterial(name.toUpperCase()); //Now check if there's a material with the name
		
		if(material == null)
			return null;
		
		return new ZShopSign(level, sign.getLocation(), material.getId(), 1, (short) 0, (byte) 0, price);
	}
	
	@SuppressWarnings("deprecation")
	public ItemStack getItem()
	{
		ItemStack stack;
		if(type == 373)
		{
			Potion potion = Potion.fromDamage(damage);
			if(damage > 10000)
				potion.setSplash(true);
			stack = potion.toItemStack(amount);
		}
		else
			stack = new ItemStack(type, amount, damage, id);
			stack.setDurability(damage);
		return stack;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean executeClick(Player player)
	{
		Gamemode gm = ZArena.getInstance().getGameHandler().getGameMode();
		if(!gm.canBuyItem(getItem().getType().toString()))
		{
			player.sendMessage(ChatColor.RED + "You may not purchase this item in this Gamemode.");
			return false;
		}
		Double costMod = gm.getCostModifier(getItem().getType().toString());
		if(costMod != null && costMod != 1)
		{
			player.sendMessage(ChatColor.RED + "Note: The price of this item in this Gamemode is multiplied by "+costMod+"x");
			double priceDifference = this.getPrice() * costMod - getPrice();
			if(ZArena.getInstance().getGameHandler().getPlayerStats(player).getMoney() < priceDifference + price)
			{
				player.sendMessage(ChatColor.RED + "Insufficient funds.");
				return false;
			}
			ZArena.getInstance().getGameHandler().getPlayerStats(player).subMoney(priceDifference);
		}
		player.getInventory().addItem(this.getItem());
		player.updateInventory();
		player.sendMessage(ChatColor.GREEN + "Purchase successful!");
		return true;
	}
	
	public void reload()
	{
		if(!(getLocation().getBlock().getState() instanceof Sign))
		{
			ZArena.logger.log(Level.INFO, "The sign at "+location.toString()+" has been removed due to it's sign having been removed;");
			level.removeZSign(this);
			return;
		}
		Sign sign = (Sign) getLocation().getBlock().getState();
		String[] lines = sign.getLines();
		ZSignCustomItem customItem = ZSignCustomItem.getCustomItem(new String[]{lines[1], lines[2]}); //First, check to see if there's a custom item with the name
		if(customItem != null)
		{
			ItemStack item = customItem.getItem();
			type = item.getType().getId();
			damage = item.getDurability();
			amount = item.getAmount();
			id = item.getData().getData();
		}
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		int ver = in.readInt();
		
		if(ver == 0)
		{
			type = in.readInt();
			amount = in.readInt();
			damage = in.readShort();
			id = in.readByte();
		}
		else
		{
			ZArena.logger.log(Level.WARNING, "An unsupported version of a ZShopSign failed to load.");
			ZArena.logger.log(Level.WARNING, "The ZSign at: "+location.toString()+" may not be operational.");
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeInt(VERSION);
		
		out.writeInt(type);
		out.writeInt(amount);
		out.writeShort(damage);
		out.writeByte(id);
	}
}
