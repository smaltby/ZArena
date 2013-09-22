package com.github.zarena.entities;

import com.github.zarena.PlayerStats;
import com.github.zarena.ZArena;

import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.IEntitySelector;

public class ZArenaPlayerSelector implements IEntitySelector
{
	@Override
	public boolean a(Entity entity)
	{
		if(!(entity instanceof EntityPlayer))
			return false;
		PlayerStats stats = ZArena.getInstance().getGameHandler().getPlayerStats(((EntityPlayer) entity).getName());
		if(stats == null)
			return false;
		return stats.isAlive();
	}
}
