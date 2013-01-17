package kabbage.zarena.customentities;

import java.lang.reflect.Field;

import kabbage.customentitylibrary.CustomEntityMoveEvent;
import kabbage.customentitylibrary.CustomEntityWrapper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.minecraft.server.v1_4_R1.EntityHuman;
import net.minecraft.server.v1_4_R1.EntityWither;
import net.minecraft.server.v1_4_R1.IEntitySelector;
import net.minecraft.server.v1_4_R1.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_4_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_4_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_4_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_4_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_4_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_4_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_4_R1.PathfinderGoalRestrictSun;
import net.minecraft.server.v1_4_R1.PathfinderGoalSelector;

import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_4_R1.util.UnsafeList;

public class CustomWither extends EntityWither
{
	private CustomWither(World world, Location location)
	{
		super(((CraftWorld) world).getHandle());
		this.setPosition(location.getX(), location.getY(), location.getZ());
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
		
		IEntitySelector entitySelector = null;
		try
		{
			Field f = EntityWither.class.getDeclaredField("bK");
			f.setAccessible(true);
			
			entitySelector = (IEntitySelector) f.get(EntityWither.class);
		} catch (NoSuchFieldException | SecurityException  | IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		
		this.goalSelector.a(1, new PathfinderGoalFloat(this));
		this.goalSelector.a(2, new PathfinderGoalRestrictSun(this));
		this.goalSelector.a(4, new PathfinderGoalArrowAttack(this, this.bG, 40, 20f));
		this.goalSelector.a(6, new PathFinderGoalMoveToEntity(this, EntityHuman.class, this.bG, 256f));
		this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, this.bG));
		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 30F, 0, true, true, entitySelector));
	}
	
	public static CustomEntityWrapper spawn(Location location, EntityTypeConfiguration entityType)
	{
		CustomWither ent = new CustomWither(location.getWorld(), location);
		if(((CraftWorld) location.getWorld()).getHandle().addEntity(ent, SpawnReason.CUSTOM))
		{
			CustomEntityWrapper wrapper = CustomEntityWrapper.spawnCustomEntity(ent, location, entityType);
			ent.resetPathfinders();
			return wrapper;
		}
		return null;
	}
}
