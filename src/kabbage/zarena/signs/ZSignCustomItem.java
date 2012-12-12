package kabbage.zarena.signs;

import java.util.ArrayList;
import java.util.List;

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
	
	public ZSignCustomItem(String[] name, int type, int amount, short damage, byte id)
	{
		this.name = name;
		this.type = type;
		this.amount = amount;
		this.damage = damage;
		this.id = id;
		
		itemList.add(this);
	}
	
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
