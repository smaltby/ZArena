package kabbage.zarena.customentities;

import java.lang.reflect.Field;

import kabbage.customentitylibrary.CustomEntityMoveEvent;
import kabbage.customentitylibrary.CustomEntityWrapper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.minecraft.server.v1_4_5.Enchantment;
import net.minecraft.server.v1_4_5.EnchantmentManager;
import net.minecraft.server.v1_4_5.EntityArrow;
import net.minecraft.server.v1_4_5.EntityHuman;
import net.minecraft.server.v1_4_5.EntityLiving;
import net.minecraft.server.v1_4_5.EntityZombie;
import net.minecraft.server.v1_4_5.IRangedEntity;
import net.minecraft.server.v1_4_5.ItemStack;
import net.minecraft.server.v1_4_5.PathfinderGoalBreakDoor;
import net.minecraft.server.v1_4_5.PathfinderGoalFloat;
import net.minecraft.server.v1_4_5.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_4_5.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_4_5.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_4_5.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_4_5.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_4_5.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_4_5.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_4_5.PathfinderGoalSelector;

import org.bukkit.craftbukkit.v1_4_5.CraftWorld;
import org.bukkit.craftbukkit.v1_4_5.util.UnsafeList;

public class CustomZombie extends EntityZombie implements IRangedEntity
{
	private EntityTypeConfiguration type;
	
	private CustomZombie(World world, Location location, EntityTypeConfiguration type)
	{
		super(((CraftWorld) world).getHandle());
		this.setPosition(location.getX(), location.getY(), location.getZ());
		this.type = type;
	}
	
	@Override
	public void move(double d0, double d1, double d2)
	{
		CustomEntityMoveEvent event = new CustomEntityMoveEvent(this.getBukkitEntity(), new Location(this.world.getWorld(), lastX, lastY, lastZ), new Location(this.world.getWorld(), locX, locY, locZ));
		Bukkit.getServer().getPluginManager().callEvent(event);
		if(event.isCancelled())
			this.setPosition(lastX, lastY, lastZ);
		else
			super.move(d0, d1, d2);
	}

    //Do not drop loot
    protected int getLootId()
    {
        return -1;
    }

    //Do not drop rare items
    protected ItemStack l(int i)
    {
        return null;
    }
    
    @SuppressWarnings("rawtypes")
	private void resetPathfinders()
	{
		try
		{
			//Enable PathfinderGoalSelector's "a" field to be editable
			Field gsa = PathfinderGoalSelector.class.getDeclaredField("a");
			gsa.setAccessible(true);

			//Now take the instances goals/targets and set them as new lists so they can be rewritten
			gsa.set(this.goalSelector, new UnsafeList());
			gsa.set(this.targetSelector, new UnsafeList());
		} catch (NoSuchFieldException | SecurityException  | IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}

		this.goalSelector.a(0, new PathfinderGoalFloat(this));
		this.goalSelector.a(1, new PathfinderGoalBreakDoor(this));
		if(type.useRanged())
			this.goalSelector.a(2, new PathFinderGoalCustomArrowAttack(this, this.bG, type.getShootDelay(), 1));
		if(type.useMelee())
			this.goalSelector.a(3, new PathfinderGoalMeleeAttack(this, EntityHuman.class, this.bG, false));
		this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, this.bG));
		this.goalSelector.a(5, new PathFinderGoalMoveToEntity(this, EntityHuman.class, this.bG, type.getRange()));
		this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, this.bG));
		this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 16.0F, 0, true));
	}
    
    public static CustomEntityWrapper spawn(Location location, EntityTypeConfiguration type)
	{
    	CustomZombie ent = new CustomZombie(location.getWorld(), location, type);
		if(((CraftWorld) location.getWorld()).getHandle().addEntity(ent, SpawnReason.CUSTOM))
		{
			CustomEntityWrapper wrapper = CustomEntityWrapper.spawnCustomEntity(ent, location, type);
			ent.resetPathfinders();
			return wrapper;
		}
		return null;
	}

	@Override
	public void d(EntityLiving arg0)
	{
		//Copied from EntitySkeleton class
		EntityArrow entityarrow = new EntityArrow(this.world, this, arg0, 1.6F, 12.0F);
        int i = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_DAMAGE.id, this.bD());
        int j = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK.id, this.bD());

        if (i > 0)
            entityarrow.b(entityarrow.c() + (double) i * 0.5D + 0.5D);

        if (j > 0)
            entityarrow.a(j);

        if (EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_FIRE.id, this.bD()) > 0)
            entityarrow.setOnFire(100);

        this.makeSound("random.bow", 1.0F, 1.0F / (this.aB().nextFloat() * 0.4F + 0.8F));
        this.world.addEntity(entityarrow);
	}
}
