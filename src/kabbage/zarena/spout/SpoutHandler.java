package kabbage.zarena.spout;

import kabbage.zarena.GameHandler;
import kabbage.zarena.PlayerStats;
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
	public static boolean isEnabled = false;
	private static SpoutListener sListener = new SpoutListener();
	
	public static void onEnable()
	{
		isEnabled = true;
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
			PlayerStats stats = handler.getPlayerStats(player);
			if(stats == null)
				continue;
			GenericLabel infoLabel = options.getInfoScreen();
			GenericTexture waveCounter = options.getWaveCounter();
			GenericLabel wcWave = options.getWaveCounterWave();
			GenericLabel wcZombies = options.getWaveCounterZombies();
			if(options.infoBarEnabled)
			{
				infoLabel.setVisible(true);
				waveCounter.setVisible(true);
				wcWave.setVisible(true);
				wcZombies.setVisible(true);
				if(handler.isRunning())
				{
					String wave = (options.waveChecked) ? "Wave: "+handler.getWaveHandler().getWave()+" | ":"";
					String money = (options.moneyChecked) ? "Money: "+stats.getMoney()+" | ":"";
					String kills = (options.pointsChecked) ? "Points: "+stats.getPoints()+" | ":"";
					String remaining = (options.remainingZombiesChecked) ? "Remainging Enemies: "+handler.getWaveHandler().getRemainingZombies()+" | ":"";
					String alive= (options.aliveCountChecked) ? "Alive Count: "+handler.getAliveCount()+" | ":"";
					String gamemode = "";
					gamemode = (options.gamemodeChecked) ? "GameMode: "+handler.getGameMode().toString()+" | ":"";
					infoLabel.setText(" | "+wave+money+kills+remaining+alive+gamemode);
					wcWave.setText("Wave: "+handler.getWaveHandler().getWave());
					wcZombies.setText(""+handler.getWaveHandler().getRemainingZombies());
				}
				else if(handler.isVoting())
					infoLabel.setText("Voting is currently taking place.");
				else
					infoLabel.setText("Game currently not running. Please contact an administator to resume gameplay.");
				
			}
			else
			{
				infoLabel.setVisible(false);
				waveCounter.setVisible(false);
				wcWave.setVisible(false);
				wcZombies.setVisible(false);
			}
		}
	}
	
	public static void openVotingScreens()
	{
		for(SpoutPlayer player : Spout.getServer().getOnlinePlayers())
		{
			PlayerOptions options = ZArena.getInstance().getPlayerOptionsHandler().getOptions(player.getName());
			if(!options.votingScreenEnabled)
				continue;
			
		}
	}
}
