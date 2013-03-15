package kabbage.zarena.customentities;

import java.lang.reflect.Field;

import kabbage.customentitylibrary.CustomEntityMoveEvent;
import kabbage.customentitylibrary.CustomEntityWrapper;

import net.minecraft.server.v1_5_R1.EntityGiantZombie;
import net.minecraft.server.v1_5_R1.EntityHuman;
import net.minecraft.server.v1_5_R1.PathfinderGoalBreakDoor;
import net.minecraft.server.v1_5_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_5_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_5_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_5_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_5_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_5_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_5_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_5_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_5_R1.PathfinderGoalSelector;

import org.bukkit.craftbukkit.v1_5_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R1.util.UnsafeList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class CustomGiant extends EntityGiantZombie
{
	private CustomGiant(World world, Location location)
	{
		super(((CraftWorld) world).getHandle());
		this.setPosition(location.getX(), location.getY(), location.getZ());
	}
	
	public void move(double d0, double d1, double d2)
	{
		CustomEntityMoveEvent event = new CustomEntityMoveEvent(this.getBukkitEntity(), new Location(this.world.getWorld(), lastX, lastY, lastZ), new Location(this.world.getWorld(), locX, locY, locZ));
		Bukkit.getServer().getPluginManager().callEvent(event);
		super.move(d0, d1, d2);
	}
	
	 @Override
	 public int getMaxHealth()
	 {
		 return 100;
	 }

	 /**
	  * Returns true if the newer Entity AI code should be run
	  */
	 @Override
	 protected boolean bh()
	 {
		 return true;
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

		 this.goalSelector.a(0, new PathfinderGoalFloat(this));
		 this.goalSelector.a(1, new PathfinderGoalBreakDoor(this));
		 this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, this.bI, false));
		 this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, this.bI));
		 this.goalSelector.a(5, new PathFinderGoalMoveToEntity(this, EntityHuman.class, this.bI, 256f));
		 this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, this.bI));
		 this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		 this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
		 this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
		 this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 16.0F, 0, true));
	 }

	 public static CustomEntityWrapper spawn(Location location, EntityTypeConfiguration entityType)
	 {
		 CustomGiant ent = new CustomGiant(location.getWorld(), location);
		 if(((CraftWorld) location.getWorld()).getHandle().addEntity(ent, SpawnReason.CUSTOM))
		 {
			 CustomEntityWrapper wrapper = CustomEntityWrapper.spawnCustomEntity(ent, location, entityType);
			 ent.resetPathfinders();
			 return wrapper;
		 }
		 return null;
	 }
}
