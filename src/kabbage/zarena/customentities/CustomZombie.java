package kabbage.zarena.customentities;

import java.lang.reflect.Field;

import kabbage.customentitylibrary.CustomEntityMoveEvent;
import kabbage.customentitylibrary.CustomEntityWrapper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.UnsafeList;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.ItemStack;
import net.minecraft.server.PathfinderGoalBreakDoor;
import net.minecraft.server.PathfinderGoalFloat;
import net.minecraft.server.PathfinderGoalHurtByTarget;
import net.minecraft.server.PathfinderGoalLookAtPlayer;
import net.minecraft.server.PathfinderGoalMeleeAttack;
import net.minecraft.server.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.PathfinderGoalRandomLookaround;
import net.minecraft.server.PathfinderGoalRandomStroll;

public class CustomZombie extends EntityZombie
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
			Field gsa = net.minecraft.server.PathfinderGoalSelector.class.getDeclaredField("a");
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
		this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, this.bG, false));
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
}
