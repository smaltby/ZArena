package kabbage.zarena.spout;

import kabbage.zarena.GameHandler;
import kabbage.zarena.ZArena;
import kabbage.zarena.listeners.SpoutListener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.Spout;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericTexture;
import org.getspout.spoutapi.keyboard.Keyboard;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * A class to handle spout related features, if spout is enabled
 */
public class SpoutHandler
{
	private static SpoutListener sListener = new SpoutListener();
	
	public static void onEnable()
	{
		sListener = new SpoutListener();
		sListener.registerEvents(Bukkit.getServer().getPluginManager(), ZArena.getInstance());
		SpoutManager.getKeyBindingManager().registerBinding("ZArena Options", Keyboard.KEY_O, "Opens the ZArena spout options screen.", sListener, ZArena.getInstance());
		SpoutManager.getKeyBindingManager().registerBinding("ZArena Tab Screen", Keyboard.KEY_GRAVE, "Opens the custom ZArena tab screen if in-game.", sListener, ZArena.getInstance());
	}

	public static void updatePlayerOptions()
	{
		GameHandler handler = ZArena.getInstance().getGameHandler();
		//TODO Make below code less cluttered
		for(Player player : handler.getPlayers())
		{
			if(!(player instanceof SpoutPlayer))
				continue;
			PlayerOptions options = ZArena.getInstance().getPlayerOptionsHandler().getOptions(player.getName());
			GenericTexture waveCounter = options.getWaveCounter();
			GenericLabel wcWave = options.getWaveCounterWave();
			GenericLabel wcZombies = options.getWaveCounterZombies();
			if(options.waveCounterEnabled || handler.getPlayerStats(player) == null)
			{
				waveCounter.setVisible(true);
				wcWave.setVisible(true);
				wcZombies.setVisible(true);
				if(handler.isRunning())
				{
					wcWave.setText("Wave: "+handler.getWaveHandler().getWave());
					wcZombies.setText(""+handler.getWaveHandler().getRemainingZombies());
				}
				else
				{
					wcWave.setText("Wave: 0");
					wcZombies.setText("0");
				}
			}
			else
			{
				waveCounter.setVisible(false);
				wcWave.setVisible(false);
				wcZombies.setVisible(false);
			}
		}
	}
	
	public static void openVotingScreens(final String[] optionsArray)
	{
		for(final SpoutPlayer player : Spout.getServer().getOnlinePlayers())
		{
			PlayerOptions options = ZArena.getInstance().getPlayerOptionsHandler().getOptions(player.getName());
			if(!options.votingScreenEnabled)
				continue;
			if(player.isDead())
			{
				ZArena.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(ZArena.getInstance(), new Runnable()
				{
					@Override
					public void run()
					{
						PlayerOptions options = ZArena.getInstance().getPlayerOptionsHandler().getOptions(player.getName());
						if(options.votingScreenEnabled && Bukkit.getPlayer(player.getName()) != null)
							options.openVotingScreen(optionsArray);
					}
				}, 60L);
			}
			else
				options.openVotingScreen(optionsArray);
		}
	}
	
	public static boolean instanceofSpoutPlayer(Player player)
	{
		return player instanceof SpoutPlayer;
	}
}
