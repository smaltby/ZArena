package com.github.zarena.entities;

import java.util.List;
import java.util.ListIterator;

import net.minecraft.server.v1_6_R1.*;
import org.bukkit.configuration.file.FileConfiguration;

import com.github.customentitylibrary.entities.EntityTypeConfiguration;
import com.github.customentitylibrary.pathfinders.PathfinderTargetSelector;
import com.github.customentitylibrary.utils.DefaultPathfinders;

public class ZEntityTypeConfiguration extends EntityTypeConfiguration implements ZEntityType
{
	double worthModifier;
	double healthModifier;
	int minSpawnWave;
	double priority;
	public ZEntityTypeConfiguration(FileConfiguration config)
	{
		super(config);
		worthModifier = config.getDouble("WorthModifier", 1.0);
		healthModifier = config.getDouble("HealthModifier", 1.0);
		minSpawnWave = config.getInt("MinimumSpawnWave", 1);
		if(config.contains("SpawnChance"))
		{
			int convertedPriority = 20;
			double chance = config.getDouble("SpawnChance");
			double currChance = .25;
			for(int i = 1; i <= 20; i++)
			{
				double nextChance = currChance / 2;
				if(chance < currChance && chance > nextChance)
				{
					convertedPriority = i;
					break;
				} else
				{
					currChance = nextChance;
				}
			}
			config.set("SpawnPriority", convertedPriority);
			config.set("SpawnChance", null);
		} 
		priority = config.getDouble("SpawnPriority", 5.0);
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
	public double getSpawnPriority()
	{
		return priority;
	}
	
	@Override
	public List<PathfinderGoal> getTargetSelectors(EntityInsentient ent)
	{
		List<PathfinderGoal> targetSelectors = DefaultPathfinders.getTargetSelectors(ent, this);
		ListIterator<PathfinderGoal> iter = targetSelectors.listIterator();
		while(iter.hasNext())
		{
			PathfinderGoal g = iter.next();
			//The default PathfinderTargetSelectors select all humans. We only want them selecting humans in the ZArena game
			if(g instanceof PathfinderTargetSelector)
				iter.set(new PathfinderTargetSelector((EntityCreature) ent, new ZArenaPlayerSelector(), getRange(), canSeeInvisible()));
		}
		//Entity wolves should be designed solely for combat
		if(ent instanceof EntityWolf)
		{
			targetSelectors.set(3, new PathfinderGoalHurtByTarget((EntityCreature)ent, true));
			targetSelectors.add(new PathfinderTargetSelector((EntityCreature) ent, new ZArenaPlayerSelector(), getRange(), canSeeInvisible()));
		}
		return targetSelectors;
	}
}
