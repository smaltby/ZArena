package kabbage.zarena.customentities;

import java.lang.reflect.Field;

import kabbage.customentitylibrary.CustomEntityMoveEvent;
import kabbage.customentitylibrary.CustomEntityWrapper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.minecraft.server.v1_5_R2.EntityHuman;
import net.minecraft.server.v1_5_R2.EntitySkeleton;
import net.minecraft.server.v1_5_R2.NBTTagCompound;
import net.minecraft.server.v1_5_R2.PathfinderGoalFleeSun;
import net.minecraft.server.v1_5_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_5_R2.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_5_R2.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_5_R2.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_5_R2.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_5_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_5_R2.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_5_R2.PathfinderGoalRestrictSun;
import net.minecraft.server.v1_5_R2.PathfinderGoalSelector;

import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R2.util.UnsafeList;

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
			Field gsa = PathfinderGoalSelector.class.getDeclaredField("a");
			gsa.setAccessible(true);

			//Now take the instances goals/targets and set them as new lists so they can be rewritten
			gsa.set(this.goalSelector, new UnsafeList());
			gsa.set(this.targetSelector, new UnsafeList());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		this.goalSelector.a(1, new PathfinderGoalFloat(this));
		this.goalSelector.a(2, new PathfinderGoalRestrictSun(this));
		this.goalSelector.a(3, new PathfinderGoalFleeSun(this, this.bI));
		if(type.useRanged())
			this.goalSelector.a(4, new PathFinderGoalCustomArrowAttack(this, this.bI, type.getShootDelay(), 1));
		if(type.useMelee())
			this.goalSelector.a(5, new PathfinderGoalMeleeAttack(this, EntityHuman.class, this.bI, false));
		this.goalSelector.a(6, new PathFinderGoalMoveToEntity(this, EntityHuman.class, this.bI, type.getRange()));
		this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, this.bI));
		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 16.0F, 0, true));
	}
	
	@Override
	public void move(double d0, double d1, double d2)
	{
		CustomEntityMoveEvent event = new CustomEntityMoveEvent(this.getBukkitEntity(), new Location(this.world.getWorld(), lastX, lastY, lastZ), new Location(this.world.getWorld(), locX, locY, locZ));
		if(event != null)
			Bukkit.getServer().getPluginManager().callEvent(event);
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
