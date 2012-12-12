package kabbage.zarena.customentities;

import kabbage.zarena.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageEffect
{
	public static final String POTION = "potion";
	public static final String FIRE = "fire";
	public static final String KNOCKBACK = "knockback";
	public static final String FORCE_EQUIP = "forceequip";
	public static final String TELEPORT = "teleport";
	public static final String TELEPORT_RELATIVE = "teleportrelative";
	public static final String DAMAGE_HUNGER = "damagehunger";
	
	private String name;
	private String[] args;
		
	public DamageEffect(String name, String[] args)
	{
		this.name = name;
		this.args = args;
	}
	
	public void dealEffect(Player player, Location source)
	{
		Location playerLoc = player.getLocation();
		switch(name.toLowerCase().replaceAll(" ", ""))
		{
		case POTION:
			PotionEffectType potionType = PotionEffectType.getByName(args[0].toUpperCase().replaceAll(" ", "_"));
			if(potionType != null)
				player.addPotionEffect(new PotionEffect(potionType, Utils.parseInt(args[1], 100), Utils.parseInt(args[2], 1)));
			break;
		case FIRE:
			player.setFireTicks(Utils.parseInt(args[0], 100));
			break;
		case KNOCKBACK:
			Utils.knockBack(player, source, Utils.parseDouble(args[0], 1.0), Utils.parseDouble(args[1], 1.0));
			break;
		case FORCE_EQUIP:
			ItemStack item = new ItemStack(Material.getMaterial(args[0]));
			if(item != null)
			{
				switch(Utils.parseInt(args[1], 0))
				{
				case 0:
					player.getInventory().setBoots(item);
					break;
				case 1:
					player.getInventory().setLeggings(item);
					break;
				case 2:
					player.getInventory().setChestplate(item);
					break;
				case 3:
					player.getInventory().setHelmet(item);
					break;
				default:
					player.getInventory().setBoots(item);
				}
			}
			break;
		case TELEPORT:
			World world = Bukkit.getWorld(args[0]);
			if(world == null)
				world = player.getWorld();
			player.teleport(new Location(world, Utils.parseDouble(args[1], playerLoc.getX()), Utils.parseDouble(args[2], playerLoc.getY()), 
					Utils.parseDouble(args[3], playerLoc.getZ())));
			break;
		case TELEPORT_RELATIVE:
			double prevX = playerLoc.getX();
			double prevY = playerLoc.getY();
			double prevZ = playerLoc.getZ();
			player.teleport(new Location(playerLoc.getWorld(), prevX + Utils.parseDouble(args[1], playerLoc.getX()), prevY + Utils.parseDouble(args[2], playerLoc.getY()), 
					Utils.parseDouble(args[3], prevZ + playerLoc.getZ())));
			break;
		case DAMAGE_HUNGER:
			player.setFoodLevel(player.getFoodLevel() - Utils.parseInt(args[0], 1));
			player.setSaturation(player.getSaturation() - Utils.parseInt(args[0], 1));
			break;
		default:
		}
	}
}
