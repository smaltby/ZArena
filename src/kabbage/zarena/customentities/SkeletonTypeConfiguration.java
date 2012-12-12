package kabbage.zarena.customentities;

import org.bukkit.configuration.file.FileConfiguration;

public class SkeletonTypeConfiguration extends EntityTypeConfiguration
{
	private boolean isWither;
	private int shootDelay;
	
	public SkeletonTypeConfiguration(FileConfiguration config)
	{
		super(config);
		isWither = config.getBoolean("Wither", false);
		shootDelay = config.getInt("ShootDelay", 60);
	}
	
	public boolean isWither()
	{
		return isWither;
	}
	
	public int getShootDelay()
	{
		return shootDelay;
	}
}
