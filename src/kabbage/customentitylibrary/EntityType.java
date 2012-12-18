package kabbage.customentitylibrary;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

public interface EntityType
{
	public String toString();
	
	public int getHealth();
	
	public float getSpeed();
	
	public int getDamage();
	
	public int getArmorPiercingDamage();
	
	public int getRange();
	
	public double getWorthModifier();
	
	public ItemStack[] getItems();
	
	public List<DamageCause> getImmunities();

	public void dealEffects(Player player, Location source);
	
	public void showSpecialEffects(LivingEntity entity);
}
