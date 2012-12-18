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
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.PathfinderGoalFleeSun;
import net.minecraft.server.PathfinderGoalFloat;
import net.minecraft.server.PathfinderGoalHurtByTarget;
import net.minecraft.server.PathfinderGoalLookAtPlayer;
import net.minecraft.server.PathfinderGoalMeleeAttack;
import net.minecraft.server.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.PathfinderGoalRandomLookaround;
import net.minecraft.server.PathfinderGoalRandomStroll;
import net.minecraft.server.PathfinderGoalRestrictSun;

public class CustomSkeleton extends EntitySkeleton
{
	private SkeletonTypeConfiguration type;
	
	private CustomSkeleton(World world, Location location, SkeletonTypeConfiguration type)
	{
		super(((CraftWorld) world).getHandle());
		this.setPosition(location.getX(), location.getY(), location.getZ());
		this.type = type;
		
		if(type.isWither())
			this.setSkeletonType(1);
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
		
		this.goalSelector.a(1, new PathfinderGoalFloat(this));
		this.goalSelector.a(2, new PathfinderGoalRestrictSun(this));
		this.goalSelector.a(3, new PathfinderGoalFleeSun(this, this.bG));
		if(type.useRanged())
			this.goalSelector.a(4, new PathFinderGoalCustomArrowAttack(this, this.bG, type.getShootDelay(), 1));
		if(type.useMelee())
			this.goalSelector.a(5, new PathfinderGoalMeleeAttack(this, EntityHuman.class, this.bg, false));
		this.goalSelector.a(6, new PathFinderGoalMoveToEntity(this, EntityHuman.class, this.bG, type.getRange()));
		this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, this.bG));
		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 16.0F, 0, true));
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
	
	@Override
	public void a(NBTTagCompound nbttagcompound)
	{
        super.a(nbttagcompound);
        //A bunch of redundant stuff regarding skeleton type removed
	}
	
	public static CustomEntityWrapper spawn(Location location, SkeletonTypeConfiguration type)
	{
		CustomSkeleton ent = new CustomSkeleton(location.getWorld(), location, type);
		if(((CraftWorld) location.getWorld()).getHandle().addEntity(ent, SpawnReason.CUSTOM))
		{
			CustomEntityWrapper wrapper = CustomEntityWrapper.spawnCustomEntity(ent, location, type);
			ent.resetPathfinders();
			return wrapper;
		}
		return null;
	}
}
