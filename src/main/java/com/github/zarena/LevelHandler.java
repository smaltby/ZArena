package com.github.zarena;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import com.github.zarena.utils.Constants;

public class LevelHandler implements Externalizable
{
	private static final long serialVersionUID = "LEVELHANDLER".hashCode(); // DO NOT CHANGE
	/**
	 * The version of the LevelHandler class. This MUST be incremented whenever the
	 * writeExternal or readExternal methods are changed.
	 */
	private static int VERSION = 0;
	
	private List<com.github.zarena.ZLevel> levels = new ArrayList<com.github.zarena.ZLevel>();
	private transient Random rnd;
	private String world;
	
	public LevelHandler()
	{
		rnd = new Random();
		world = ZArena.getInstance().getConfig().getString(Constants.GAME_WORLD);
	}
	
	public void addLevel(ZLevel level)
	{
		levels.add(level);
	}
	
	public void removeLevel(ZLevel level)
	{
		levels.remove(level);
	}
	
	public ZLevel getRandomLevel()
	{
		return levels.get(rnd.nextInt(levels.size()));
	}
	
	public ZLevel getRandomLevel(Collection<ZLevel> exludedLevels)
	{
		//We don't want to ever return null...so if the excludedLevels is a collection of all levels, screw it and just return any damn level
		if(exludedLevels.containsAll(levels))
			return levels.get(rnd.nextInt(levels.size()));
		ZLevel level = null;
		do
		{
			level = levels.get(rnd.nextInt(levels.size()));
		} while(exludedLevels.contains(level));
		return level;
	}
	
	public ZLevel getLevel(String levelName)
	{
		for(ZLevel level : levels)
		{
			if(level.getName().equalsIgnoreCase(levelName))
				return level;
		}
		return null;
	}
	
	public List<ZLevel> getLevels()
	{
		return levels;
	}
	
	public String getWorldName()
	{
		return world;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int ver = in.readInt();
		
		if(ver == 0)
		{
			levels = (List<ZLevel>) in.readObject();
		}
		else
		{
			ZArena.log(Level.SEVERE, "An unsupported version of a LevelHandler failed to load.");
			ZArena.log(Level.SEVERE, "Saved levels may be unplayable!");
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(VERSION);
		
		out.writeObject(levels);
	}
}
