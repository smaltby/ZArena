package kabbage.zarena.customentities;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import kabbage.customentitylibrary.EntityType;
import kabbage.zarena.utils.Utils;

public class EntityTypeConfiguration implements EntityType
{
	FileConfiguration config;
	String type;
	String name;
	int damage;
	int armorPierce;
	int health;
	double healthModifier;
	int range;
	float speed;
	double worthModifier;
	List<DamageCause> immunities = new ArrayList<DamageCause>();
	ItemStack[] items = new ItemStack[5];
	int minimumSpawnWave;
	double spawnChance;
	int shootDelay;
	boolean melee;
	boolean ranged;
	String rangedType;
	String skinURL;
	List<DamageEffect> damageEffects = new ArrayList<DamageEffect>();
	List<SpecialEffect> specialEffects = new ArrayList<SpecialEffect>();
	
	public EntityTypeConfiguration(FileConfiguration config)
	{
		this.config = config;
		type = config.getString("Type", "Zombie");
		name = config.getString("Name", type);
		damage = config.getInt("Damage", 2);
		armorPierce = config.getInt("Armor Piercing", 0);
		health = 10;	//Pointless default value. Health will be changed on entity spawn depending on the health modifier and the wave's default health.
		healthModifier = config.getDouble("HealthModifier", 1);
		range = config.getInt("Range", 256);
		speed = (float) config.getDouble("Speed", .23f);
		worthModifier = config.getDouble("WorthModifier", 1);
		minimumSpawnWave = config.getInt("MinimumSpawnWave", 1);
		spawnChance = config.getDouble("SpawnChance", .05);
		shootDelay = config.getInt("ShootDelay", 60);
		melee = config.getBoolean("Use Melee", true);
		ranged = config.getBoolean("Use Ranged", false);
		rangedType = config.getString("Ranged Attack Type", "Arrow");
		skinURL = config.getString("Skin URL", (type.equalsIgnoreCase("skeleton")) ? "http://i.imgur.com/L2Zy5.png" : (type.equalsIgnoreCase("wolf")) ? "http://i.imgur.com/9Iimp.png" : "http://i.imgur.com/XJuFX.png");
		if(config.getStringList("Immunities") != null)
		{
			for(String damageCauseName : config.getStringList("Immunities"))
			{
				DamageCause damageCause = DamageCause.valueOf(damageCauseName.toUpperCase().replaceAll(" ", "_"));
				if(damageCause != null)
					immunities.add(damageCause);
			}
		}
		items[0] = new ItemStack(Material.valueOf(config.getString("Weapon", Material.AIR.toString()).toUpperCase().replaceAll(" ", "_")).getId());
		int index = 1;
		if(config.getStringList("Armor") != null)
		{
			for(String itemName : config.getStringList("Armor"))
			{
				Material material = Material.getMaterial(itemName.toUpperCase().replaceAll(" ", "_"));
				if(material != null)
					items[index] = new ItemStack(material.getId());
				index++;
				if(index > 4)
					break;
			}
		}
		if(config.getStringList("DamageEffects") != null)
		{
			for(String effectName : config.getStringList("DamageEffects"))
			{
				String[] args = Utils.getConfigArgs(effectName);
				DamageEffect damageEffect = new DamageEffect(effectName.split("\\s")[0], args);
				damageEffects.add(damageEffect);
			}
		}
		if(config.getStringList("SpecialEffects") != null)
		{
			for(String effectName : config.getStringList("SpecialEffects"))
			{
				String[] args = Utils.getConfigArgs(effectName);
				SpecialEffect specialEffect = new SpecialEffect(effectName.split("\\s")[0], args);
				specialEffects.add(specialEffect);
			}
		}
	}
	
	@Override
	public int getDamage()
	{
		return damage;
	}
	
	@Override
	public int getArmorPiercingDamage()
	{
		return armorPierce;
	}

	@Override
	public int getHealth()
	{
		return health;
	}
	
	public double getHealthModifier()
	{
		return healthModifier;
	}
	
	@Override
	public int getRange()
	{
		return range;
	}
	
	public boolean useMelee()
	{
		return melee;
	}
	
	public boolean useRanged()
	{
		return ranged;
	}
	
	public String getRangedAttackType()
	{
		return rangedType;
	}

	@Override
	public float getSpeed()
	{
		return speed;
	}
	
	public String getType()
	{
		return type.toLowerCase();
	}
	
	public String getName()
	{
		return name;
	}

	@Override
	public double getWorthModifier()
	{
		return worthModifier;
	}

	@Override
	public List<DamageCause> getImmunities()
	{
		return immunities;
	}

	@Override
	public ItemStack[] getItems()
	{
		return items;
	}
	
	public int getMinimumSpawnWave()
	{
		return minimumSpawnWave;
	}
	
	public String getSkinURL()
	{
		return skinURL;
	}
	
	public double getSpawnChance()
	{
		return spawnChance;
	}
	
	public int getShootDelay()
	{
		return shootDelay;
	}
	
	@Override
	public void dealEffects(Player player, Location location)
	{
		if(((CraftPlayer) player).getHandle().hurtTicks > 0)
			return;
		for(DamageEffect effect : damageEffects)
		{
			effect.dealEffect(player, location);
		}
	}

	@Override
	public void showSpecialEffects(LivingEntity entity)
	{
		for(SpecialEffect effect : specialEffects)
		{
			effect.showEffect(entity);
		}
	}

	@Override
	public String toString()
	{
		return name;
	}
}
