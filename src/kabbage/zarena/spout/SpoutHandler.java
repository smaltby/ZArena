package kabbage.zarena.spout;

import kabbage.zarena.GameHandler;
import kabbage.zarena.PlayerStats;
import kabbage.zarena.ZArena;
import kabbage.zarena.listeners.SpoutListener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.getspout.spout.keyboard.SimpleKeyBindingManager;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.keyboard.Keyboard;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * A class to handle spout related features, if spout is enabled
 */
public class SpoutHandler
{
	public void onEnable(SpoutListener sListener)
	{
		sListener = new SpoutListener();
		sListener.registerEvents(Bukkit.getServer().getPluginManager(), ZArena.getInstance());
		SimpleKeyBindingManager keyBindingManager = new SimpleKeyBindingManager();
		keyBindingManager.registerBinding("Options Keybinding", Keyboard.KEY_O, "Opens the ZArena spout options screen.", sListener, ZArena.getInstance());
	}

	public void updatePlayerOptions(GameHandler handler)
	{
		//TODO Make below code less cluttered
		try
		{
			for(Player player : handler.getPlayers())
			{
				if(!(player instanceof SpoutPlayer))
					continue;
				PlayerOptions options = ZArena.getInstance().getPlayerOptionsHandler().getOptions(player.getName());
				PlayerStats stats = handler.getPlayerStats().get(player);
				GenericLabel infoLabel = options.getInfoScreen();
				if(options.infoBarEnabled)
				{
					infoLabel.setVisible(true);
					if(handler.isRunning())
					{
						String wave = (options.waveChecked) ? "Wave: "+handler.getWaveHandler().getWave()+" ... ":"";
						String money = (options.moneyChecked) ? "Money: "+stats.getMoney()+" ... ":"";
						String kills = (options.killsChecked) ? "Kills: "+stats.getPoints()+" ... ":"";
						String remaining = (options.remainingZombiesChecked) ? "Remainging Enemies: "+handler.getWaveHandler().getRemainingZombies()+" ... ":"";
						String alive= (options.aliveCountChecked) ? "Alive Count: "+handler.getAliveCount()+" ... ":"";
						String gamemode = "";
						gamemode = (options.gamemodeChecked) ? "GameMode: "+handler.getGameMode().toString()+" ... ":"";
						infoLabel.setText(wave+money+kills+remaining+alive+gamemode);
					}
					else if(handler.isVoting())
						infoLabel.setText("Voting is currently taking place.");
					else
						infoLabel.setText("Game currently not running. Please contact an administator to resume gameplay.");
				}
				else
					infoLabel.setVisible(false);
			}
		}
		catch(NullPointerException e)
		{}
	}
}
