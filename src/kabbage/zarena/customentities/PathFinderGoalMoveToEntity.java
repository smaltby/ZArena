package kabbage.zarena.customentities;

import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;

import net.minecraft.server.v1_5_R2.Entity;
import net.minecraft.server.v1_5_R2.EntityCreature;
import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.PathEntity;
import net.minecraft.server.v1_5_R2.PathfinderGoal;
import net.minecraft.server.v1_5_R2.RandomPositionGenerator;
import net.minecraft.server.v1_5_R2.Vec3D;
import kabbage.zarena.GameHandler;
import kabbage.zarena.PlayerStats;
import kabbage.zarena.ZArena;

public class PathFinderGoalMoveToEntity extends PathfinderGoal
{

	private EntityCreature entity;
	private float range;
	private float speed;
	private PathEntity path;
	private double x;
	private double y;
	private double z;
	private Vec3D targetArea; //targetArea.a being targetArea.x ... target.b being target.y ... target.c being target.z
	@SuppressWarnings("rawtypes")
	private Class target;
	private GameHandler gameHandler;
	
	@SuppressWarnings("rawtypes")
	public PathFinderGoalMoveToEntity(EntityCreature entitycreature, Class target, float speed, float range)
	{
		gameHandler = ZArena.getInstance().getGameHandler();
		this.entity = entitycreature;
		this.speed = speed;
		this.range = range;
		this.target = target;
		targetArea = this.getTarget(target);
		if(targetArea != null)
		{
			this.x = targetArea.c;
			this.y = targetArea.d;
			this.z = targetArea.e;
		}
		this.a();
	}

	/**
	 * Should this task execute?
	 */
	public boolean a()
	{
		this.c();
		this.path = this.entity.getNavigation().a(this.x, this.y, this.z);	//getNavigation().a sets a path to it's paramaters
		if (this.path != null)
			return true;
		else
		{
			//Vec3D.a means Vec3D.createVector, essentially. RandomPositionGenerator.a gets a random block towards the target from
			Vec3D vec3d = RandomPositionGenerator.a(this.entity, 10, 7, Vec3D.a(this.x, this.y, this.z));
			if (vec3d == null) 
				return false;
			else
			{
				this.path = this.entity.getNavigation().a(vec3d.c, vec3d.d, vec3d.e);	//getNavigation().a creates a path to it's paramaters. vec3d.a/b/c are x/y/z
				return this.path != null;
			}
		}
	}

	/**
	 * Should the task continue executing?
	 */
	public boolean b()
	{
		if (this.entity.getNavigation().f()) //getNavigation.f() checks to see if there is no path, in which case, return false
		{
			return false;
		}
		else if(targetArea == null) //Do we not have a target?
		{
			targetArea = getTarget(target); //Attempt to get a new one
			if(targetArea == null) //If the targetArea is still null after attempted to get a new target, return false
				return false;
		}
		float f = 4.0F;
		//this.entity.e gets the distance squared to it's paramaters
		return this.entity.e(this.x, this.y, this.z) > (double) (f * f);	//If the entity and its target are really damn close, return false
	}

	/**
	 * On start of task
	 */
	public void c()
	{
		this.
		targetArea = this.getTarget(target); //Targets move, so update the target's position
		if(targetArea != null)
		{
			this.x = targetArea.c;
			this.y = targetArea.d;
			this.z = targetArea.e;
		}
		else
			return;
		this.entity.getNavigation().a(this.path, this.speed);	//getNavigation().a sets a path to this.path at the speed of this.speed
	}
	
	@SuppressWarnings("rawtypes")
	/**
	 * This gets the target the entity has to move to. However, there is something weird with the navigation class, preventing the entity from
	 * having a target 16F or farther away from it. So, this gets a target ~16F away in the direction of the nearest instance of it's target class
	 * @param target what class of entities to target
	 * @return a location 16F meters along the way of the way to it's target
	 */
	private Vec3D getTarget(Class target)
	{
		//world.a means world.findNearestEntityWithinAnArea, where the Area is this.entity.boundingBox.grow.
		//Entity targetEntity =  this.entity.world.a(target, this.entity.boundingBox.grow(this.range, this.range, this.range), this.entity);
		double lastDistance = this.range * this.range;
		EntityPlayer lastPlayer = null;
		for(PlayerStats stats : gameHandler.getPlayerStats().values())
		{
			if(stats != null && stats.getPlayer() != null)
			{
				EntityPlayer entPlayer = ((CraftPlayer) stats.getPlayer()).getHandle();
				if(stats.isAlive())
				{
					Vec3D vec = Vec3D.a(entPlayer.locX, entPlayer.locY, entPlayer.locZ);
					if(vec.distanceSquared(Vec3D.a(entity.locX, entity.locY, entity.locZ)) < lastDistance)
					{
						lastPlayer = entPlayer;
						lastDistance = vec.distanceSquared(Vec3D.a(entity.locX, entity.locY, entity.locZ));
					}
				}
			}
		}
		Entity targetEntity = (Entity) lastPlayer;
		if(targetEntity == null)
			return null;
		//Vec3D entityLocation = Vec3D.a(entity.locX, entity.locY, entity.locZ); //Vec3D.a creates a new Vec3D instance and returns it
		Vec3D targetLocation = Vec3D.a(targetEntity.locX, targetEntity.locY, targetEntity.locZ);
		return targetLocation;
	}
}
