package main.java.com.github.zarena.entities;

import org.bukkit.configuration.file.FileConfiguration;

public class SkeletonTypeConfiguration extends EntityTypeConfiguration
{
	private boolean isWither;
	
	public SkeletonTypeConfiguration(FileConfiguration config)
	{
		super(config);
		isWither = config.getBoolean("Wither", false);
		ranged = config.getBoolean("Use Ranged", !isWither);
	}
	
	public boolean isWither()
	{
		return isWither;
	}
}
