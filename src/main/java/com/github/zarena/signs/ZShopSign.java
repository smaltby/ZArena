package com.github.zarena.signs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

import com.github.zarena.Gamemode;
import com.github.zarena.ZArena;
import com.github.zarena.ZLevel;
import com.github.zarena.utils.ChatHelper;
import com.github.zarena.utils.LocationSer;
import com.github.zarena.utils.Message;

public class ZShopSign extends ZSign implements Externalizable
{
	private static final long serialVersionUID = "ZSHOPSIGN".hashCode(); // DO NOT CHANGE
	private static final int VERSION = 1;
	
	private int type;
	private int amount;
	private short damage;
	private byte id;
	private Map<Integer, Integer> enchantments = new HashMap<Integer, Integer>();
	
	/**
	 * Empty constructor for externalization.
	 */
	public ZShopSign()
	{
	}
	
	public ZShopSign(ZLevel level, Location location, ItemStack item, int price)
	{
		super(level, LocationSer.convertFromBukkitLocation(location), price);
		this.type = item.getType().getId();
		this.amount = item.getAmount();
		this.damage = item.getDurability();
		this.id = item.getData().getData();
		for(Entry<Enchantment, Integer> e : item.getEnchantments().entrySet())
		{
			if(e.getKey() != null)
				enchantments.put(e.getKey().getId(), e.getValue());
		}
	}
	
	@SuppressWarnings("deprecation")
	public static ZShopSign attemptCreateSign(ZLevel level, Location location, String[] lines)
	{
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
			return new ZShopSign(level, location, customItem.getItem(), price);
		
		String name = (!lines[2].isEmpty()) ? lines[1] + "_" + lines[2] : lines[1];
		Material material = Material.getMaterial(name.toUpperCase()); //Now check if there's a material with the name
		
		if(material == null)
			return null;
		
		return new ZShopSign(level, location, new ItemStack(material, 1, (short) 0, (byte) 0), price);
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
		for(Entry<Integer, Integer> e : enchantments.entrySet())
		{
			stack.addUnsafeEnchantment(Enchantment.getById(e.getKey()), e.getValue());
		}
		return stack;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean executeClick(Player player)
	{
		Gamemode gm = ZArena.getInstance().getGameHandler().getGameMode();
		if(!gm.canBuyItem(getItem().getType().toString()))
		{
			ChatHelper.sendMessage(Message.NO_BUY.formatMessage(), player);
			return false;
		}
		Double costMod = gm.getCostModifier(getItem().getType().toString());
		if(costMod != null && costMod != 1)
		{
			ChatHelper.sendMessage(Message.EXTRA_COST.formatMessage(costMod), player);
			double priceDifference = this.getPrice() * costMod - getPrice();
			if(ZArena.getInstance().getGameHandler().getPlayerStats(player).getMoney() < priceDifference + price)
			{
				ChatHelper.sendMessage(Message.INSUFFICIENT_FUNDS.formatMessage(), player);
				return false;
			}
			ZArena.getInstance().getGameHandler().getPlayerStats(player).subMoney(priceDifference);
		}
		player.getInventory().addItem(this.getItem());
		player.updateInventory();
		ChatHelper.sendMessage(Message.PURCHASE_SUCCESSFUL.formatMessage(), player);
		return true;
	}
	
	public void reload()
	{
		if(!(getLocation().getBlock().getState() instanceof Sign))
		{
			ZArena.log(Level.INFO, "The sign at "+location.toString()+" has been removed due to it's sign having been removed;");
			getLevel().removeZSign(this);
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
			enchantments.clear();
			for(Entry<Enchantment, Integer> e : item.getEnchantments().entrySet())
			{
				enchantments.put(e.getKey().getId(), e.getValue());
			}
		}
	}
	
	@SuppressWarnings({ "unchecked" })
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
			enchantments = new HashMap<Integer, Integer>();
		}
		else if(ver == 1)
		{
			type = in.readInt();
			amount = in.readInt();
			damage = in.readShort();
			id = in.readByte();
			enchantments = (Map<Integer, Integer>) in.readObject();
		}
		else
		{
			ZArena.log(Level.WARNING, "An unsupported version of a ZShopSign failed to load.");
			ZArena.log(Level.WARNING, "The ZSign at: "+location.toString()+" may not be operational.");
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
		out.writeObject(enchantments);
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = super.serialize();
		map.put("Type", type);
		map.put("Amount", amount);
		map.put("Damage", damage);
		map.put("ID", id);

		Map<String, Object> enchantmentsMap = new LinkedHashMap<String, Object>();
		Integer index = 0;
		for(Entry<Integer, Integer> entry : enchantments.entrySet())
		{
			Map<String, Object> enchantment = new LinkedHashMap<String, Object>();
			enchantment.put("ID", entry.getKey());
			enchantment.put("Level", entry.getValue());
			enchantmentsMap.put((index++).toString(), enchantment);
		}
		map.put("Enchantments", enchantmentsMap);
		map.put("Class", ZShopSign.class.getName());

		return map;
	}

	@SuppressWarnings("unchecked")
	public static ZShopSign deserialize(Map<String, Object> map)
	{
		ZShopSign shopSign = new ZShopSign();
		shopSign.level = (String) map.get("Level");
		shopSign.location = (LocationSer) map.get("Location");
		shopSign.price = (Integer) map.get("Price");

		shopSign.type = (Integer) map.get("Type");
		shopSign.amount = (Integer) map.get("Amount");
		shopSign.damage = ((Integer) map.get("Damage")).shortValue();
		shopSign.id = ((Integer) map.get("ID")).byteValue();

		Map<String, Object> enchantmentsMap = (Map<String, Object>) map.get("Enchantments");
		for(Object o : enchantmentsMap.values())
		{
			Map<String, Object> enchantment = (Map<String, Object>) o;
			int id = (Integer) enchantment.get("ID");
			int level = (Integer) enchantment.get("Level");
			shopSign.enchantments.put(id, level);
		}

		return shopSign;
	}
}
