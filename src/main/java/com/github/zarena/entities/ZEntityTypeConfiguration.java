package com.github.zarena.entities;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import net.minecraft.server.v1_5_R2.EntityCreature;
import net.minecraft.server.v1_5_R2.EntityLiving;
import net.minecraft.server.v1_5_R2.PathfinderGoal;
import net.minecraft.server.v1_5_R2.PathfinderGoalFloat;

import org.bukkit.configuration.file.FileConfiguration;

import com.github.customentitylibrary.entities.EntityType;
import com.github.customentitylibrary.entities.EntityTypeConfiguration;
import com.github.customentitylibrary.pathfinders.PathfinderTargetSelector;
import com.github.customentitylibrary.utils.DefaultPathfinders;

public class ZEntityTypeConfiguration extends EntityTypeConfiguration implements ZEntityType
{
	double worthModifier;
	double healthModifier;
	int minSpawnWave;
	double spawnChance;
	boolean canDive;
	public ZEntityTypeConfiguration(FileConfiguration config)
	{
		super(config);
		worthModifier = config.getDouble("WorthModifier", 1.0);
		healthModifier = config.getDouble("HealthModifier", 1.0);
		minSpawnWave = config.getInt("MinimumSpawnWave", 1);
		spawnChance = config.getDouble("SpawnChance", .01);
		canDive = config.getBoolean("CanDive", false);
	}

	@Override
	public double getWorthModifier()
	{
		return worthModifier;
	}
	
	@Override
	public double getHealthModifier()
	{
		return healthModifier;
	}

	@Override
	public int getMinimumSpawnWave()
	{
		return minSpawnWave;
	}
	
	@Override
	public double getSpawnChance()
	{
		return spawnChance;
	}
	
	@Override
	public Map<Integer, PathfinderGoal> getTargetSelectors(EntityLiving ent, EntityType type)
	{
		Map<Integer, PathfinderGoal> targetSelectors = DefaultPathfinders.getTargetSelectors(ent, this);
		Stack<Integer> toRemove = new Stack<Integer>();
		for(Entry<Integer, PathfinderGoal> e : targetSelectors.entrySet())
		{
			//The default PathfinderTargetSelectors select all humans. We only want them selecting humans in the ZArena game
			if(e.getValue() instanceof PathfinderTargetSelector)
				e.setValue(new PathfinderTargetSelector((EntityCreature) ent, new ZArenaPlayerSelector(), type.getRange()));
			//Remove the float pathfinder if the entity can dive down
			if(canDive && e.getValue() instanceof PathfinderGoalFloat)
				toRemove.push(e.getKey());
		}
		for(Integer priority : toRemove) targetSelectors.remove(priority);
		return targetSelectors;
	}
	
	public boolean canDive()
	{
		return canDive;
	}
}
