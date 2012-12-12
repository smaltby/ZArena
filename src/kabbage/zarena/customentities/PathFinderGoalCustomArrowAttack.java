package kabbage.zarena.customentities;

import org.bukkit.event.entity.EntityTargetEvent;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntitySnowball;
import net.minecraft.server.IRangedEntity;
import net.minecraft.server.MathHelper;
import net.minecraft.server.PathfinderGoal;
import net.minecraft.server.Vec3D;

/**
 * Customized for entities that use both melee and ranged attacks
 */
public class PathFinderGoalCustomArrowAttack extends PathfinderGoal
{
	private final EntityLiving a;	//Entity executing this pathfinder
    private final IRangedEntity b;	//Ranged entity executing this pathfinder
    private EntityLiving target;	//Target
    private int rangedAttackTime = 0;	//This decrements to 0 while the target is in range. When it reaches 0, the entity fires, and this goes back up to attackSpeed
    private float speed;//Speed
    private int f = 0;	//Not sure
    private int attackSpeed;	//Attack speed
    private int rangedAttackID;	//Different ID's fire different projectiles

    public PathFinderGoalCustomArrowAttack(IRangedEntity irangedentity, float speed, int attackSpeed, int rangedAttackID) {
        if (!(irangedentity instanceof EntityLiving)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        } else {
            this.b = irangedentity;
            this.a = (EntityLiving) irangedentity;
            this.speed = speed;
            this.attackSpeed = attackSpeed;
            this.rangedAttackID = rangedAttackID;
            this.a(3);
        }
    }

    /**
	 * Should this task execute?
	 */
    public boolean a() {
        EntityLiving entityliving = this.a.aG();

        if (entityliving == null) {
            return false;
        } else {
            this.target = entityliving;
            Vec3D entityLocation = Vec3D.a(a.locX, a.locY, a.locZ);
        	Vec3D targetLocation = Vec3D.a(target.locX, target.locY, target.locZ);
        	if(entityLocation.distanceSquared(targetLocation) < 4*4)	//If the target is getting to close, screw this and move out for a melee attack
        		return false;
            return true;
        }
    }

    /**
	 * Should the task continue executing?
	 */
    public boolean b() {
    	Vec3D entityLocation = Vec3D.a(a.locX, a.locY, a.locZ);
    	Vec3D targetLocation = Vec3D.a(target.locX, target.locY, target.locZ);
    	if(entityLocation.distanceSquared(targetLocation) < 4*4)	//If the target is getting to close, screw this and move out for a melee attack
    		return false;
        return this.a() || !this.a.getNavigation().f();
    }

    /**
	 * Stop executing
	 */
    public void d() {
        // CraftBukkit start
        EntityTargetEvent.TargetReason reason = this.target.isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
        org.bukkit.craftbukkit.event.CraftEventFactory.callEntityTargetEvent((Entity) b, null, reason);
        // CraftBukkit end
        this.target = null;
        this.f = 0;
    }

    /**
	 * Update task
	 */
    public void e() {
    	float range = 200f;
        double d0 = this.a.e(this.target.locX, this.target.boundingBox.b, this.target.locZ);
        boolean canSee = this.a.aA().canSee(this.target);

        if (canSee) {
            ++this.f;
        } else {
            this.f = 0;
        }

        if (d0 <= (double) range && this.f >= 20) {
            this.a.getNavigation().g();
        } else {
            this.a.getNavigation().a(this.target, this.speed);
        }

        this.a.getControllerLook().a(this.target, 30.0F, 30.0F);
        this.rangedAttackTime = Math.max(this.rangedAttackTime - 1, 0);
        if (this.rangedAttackTime <= 0) {
            if (d0 <= (double) range && canSee) {
                //this.b.d(this.target);
            	doRangedAttack();
                this.rangedAttackTime = this.attackSpeed;	//Reset timer to attack speed
            }
        }
    }
    
    /**
     * Performs a ranged attack according to the AI's rangedAttackID.
     */
    private void doRangedAttack()
    {
        if (this.rangedAttackID == 1)
        {
            EntityArrow var1 = new EntityArrow(a.world, this.a, this.target, 1.6F, 12.0F);
            a.world.makeSound(this.a, "random.bow", 1.0F, 1.0F / (this.a.aB().nextFloat() * 0.4F + 0.8F));
            a.world.addEntity(var1);
        }
        else if (this.rangedAttackID == 2)
        {
            EntitySnowball var9 = new EntitySnowball(this.a.world, this.a);
            double var2 = this.target.locX - this.a.locX;
            double var4 = this.target.locY + (double)this.target.getHeadHeight() - 1.100000023841858D - var9.locY;
            double var6 = this.target.locZ - this.a.locZ;
            float var8 = MathHelper.sqrt(var2 * var2 + var6 * var6) * 0.2F;
            var9.setPositionRotation(var2, var4 + (double)var8, var6, 1.6F, 12.0F);	//Maybe this should be var9.shoot
            a.world.makeSound(this.a, "random.bow", 1.0F, 1.0F / (this.a.aB().nextFloat() * 0.4F + 0.8F));
            a.world.addEntity(var9);
        }
    }
}
