package com.github.zarena.signs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

public class ZSignCustomItem
{
	private static List<ZSignCustomItem> itemList = new ArrayList<ZSignCustomItem>();
	private String[] name; //Is an array, as the name can fit into up to two lines on a sign
	private int type;
	private int amount;
	private short damage;
	private byte id;
	private Map<Enchantment, Integer> enchantments;
	
	public ZSignCustomItem(String[] name, int type, int amount, short damage, byte id, Map<Enchantment, Integer> enchantments)
	{
		this.name = name;
		this.type = type;
		this.amount = amount;
		this.damage = damage;
		this.id = id;
		this.enchantments = enchantments;
		
		itemList.add(this);
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
		
		for(Entry<Enchantment, Integer> e : enchantments.entrySet())
		{
			if(e.getKey() != null)
				stack.addUnsafeEnchantment(e.getKey(), e.getValue());
		}
		
		return stack;
	}
	
	public static ZSignCustomItem getCustomItem(String[] lines)
	{
		for(ZSignCustomItem customItem : itemList)
		{
			if(customItem.name[0].equals(lines[0]) && customItem.name[1].equals(lines[1]))
				return customItem;
		}
		return null;
	}
}
