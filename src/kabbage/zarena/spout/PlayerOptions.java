package kabbage.zarena.spout;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import kabbage.zarena.PlayerStats;
import kabbage.zarena.ZArena;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericTexture;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

public class PlayerOptions implements Externalizable
{
	private static final long serialVersionUID = "PLAYEROPTIONS".hashCode(); //DO NOT CHANGE
	private static final int VERSION = 1;
	
	private transient GenericTexture waveCounter;
	private transient GenericLabel waveCounterWave;		//Label that shows current wave in waveCounter
	private transient GenericLabel waveCounterZombies;	//Label that shows remaining zombies in waveCounter
	
	private String player;
	public boolean votingScreenEnabled;
	public boolean zombieTexturesEnabled;
	public boolean waveCounterEnabled;
	
	/**
	 * Empty constructor for externalization.
	 */
	public PlayerOptions()
	{
		waveCounter = new GenericTexture();
		waveCounter.setVisible(false);
		waveCounter.setDrawAlphaChannel(true);
		waveCounter.setUrl("http://i.imgur.com/fWiJl.png");
		waveCounter.setWidth(125);
		waveCounter.setHeight(125);
		waveCounter.setAnchor(WidgetAnchor.TOP_RIGHT);
		waveCounter.shiftXPos(-85);
		waveCounter.shiftYPos(-35);
		waveCounter.setPriority(RenderPriority.High);
		
		waveCounterWave = new GenericLabel("Wave: 0");
		waveCounterWave.setVisible(false);
		waveCounterWave.setAnchor(WidgetAnchor.TOP_RIGHT);
		waveCounterWave.setAlign(WidgetAnchor.CENTER_CENTER);
		waveCounterWave.shiftXPos(-35);
		waveCounterWave.shiftYPos(37);
		waveCounterWave.setTextColor(new Color(150, 0, 10));
		waveCounterWave.setHeight(10);
		waveCounterWave.setWidth(10);
		
		waveCounterZombies = new GenericLabel("0");
		waveCounterZombies.setVisible(false);
		waveCounterZombies.setAnchor(WidgetAnchor.TOP_RIGHT);
		waveCounterZombies.setAlign(WidgetAnchor.CENTER_CENTER);
		waveCounterZombies.shiftXPos(-40);
		waveCounterZombies.shiftYPos(18);
		waveCounterZombies.setTextColor(new Color(150, 0, 10));
		waveCounterZombies.setScale(2);
		waveCounterZombies.setHeight(20);
		waveCounterZombies.setWidth(20);
	}
	
	public PlayerOptions(String player)
	{
		this();
		
		this.player = player;
		
		votingScreenEnabled = true;
		zombieTexturesEnabled = true;
		waveCounterEnabled = true;
	}
	
	public String getPlayerName()
	{
		return player;
	}
	
	public GenericTexture getWaveCounter()
	{
		return waveCounter;
	}
	
	public GenericLabel getWaveCounterWave()
	{
		return waveCounterWave;
	}
	
	public GenericLabel getWaveCounterZombies()
	{
		return waveCounterZombies;
	}
	
	public void openOptions()
	{
		ZOptionsButton votingScreen = new ZOptionsButton("Voting Popup: "+isEnabled(votingScreenEnabled));
		ZOptionsButton zombieTextures = new ZOptionsButton("Zombie Textures: "+isEnabled(zombieTexturesEnabled));
		ZOptionsButton waveCounter = new ZOptionsButton("Wave Counter: "+isEnabled(waveCounterEnabled));
		
		votingScreen.setAnchor(WidgetAnchor.CENTER_CENTER);
		zombieTextures.setAnchor(WidgetAnchor.CENTER_CENTER);
		waveCounter.setAnchor(WidgetAnchor.CENTER_CENTER);
		
		votingScreen.shiftXPos(-160);
		votingScreen.shiftYPos(-80);
		zombieTextures.shiftXPos(-160);
		zombieTextures.shiftYPos(-30);
		waveCounter.shiftXPos(-160);
		waveCounter.shiftYPos(20);
		
		votingScreen.setWidth(150);
		votingScreen.setHeight(20);
		zombieTextures.setWidth(150);
		zombieTextures.setHeight(20);
		waveCounter.setWidth(150);
		waveCounter.setHeight(20);
		
		ZCloseButton close = new ZCloseButton("Close");
		close.setAlign(WidgetAnchor.CENTER_CENTER);
		close.setAnchor(WidgetAnchor.CENTER_CENTER);
		close.shiftXPos(0);
		close.shiftYPos(70);
		close.setWidth(100);
		close.setHeight(30);
		
		GenericPopup popup = new GenericPopup();
		
		popup.attachWidget(ZArena.getInstance(), votingScreen);
		popup.attachWidget(ZArena.getInstance(), zombieTextures);
		popup.attachWidget(ZArena.getInstance(), waveCounter);
		popup.attachWidget(ZArena.getInstance(), close);
		
		((SpoutPlayer) Bukkit.getPlayer(player)).getMainScreen().attachPopupScreen(popup);
	}
	
	private GenericTexture tabHeader;
	private List<GenericTexture> tabMain = new ArrayList<GenericTexture>();
	private List<GenericLabel> tabText = new ArrayList<GenericLabel>();
	public void openTabScreen()
	{
		tabHeader = new GenericTexture("http://i.imgur.com/t8CPF.png");
		tabHeader.setDrawAlphaChannel(true);
		tabHeader.setAnchor(WidgetAnchor.CENTER_CENTER);
		tabHeader.setWidth(212);
		tabHeader.setHeight(8);
		tabHeader.shiftYPos(-100);
		tabHeader.shiftXPos(tabHeader.getWidth() / -2 + 20);
		
		int index = 0;
		for(Player player : ZArena.getInstance().getGameHandler().getPlayers())
		{
			GenericTexture part = new GenericTexture("http://i.imgur.com/qvrX8.png");
			part.setDrawAlphaChannel(true);
			part.setAnchor(WidgetAnchor.CENTER_CENTER);
			part.setWidth(255);
			part.setHeight(15);
			part.shiftYPos(-90 + index * 15);
			part.shiftXPos(part.getWidth() / -2);
			part.setPriority(RenderPriority.Low);
			tabMain.add(part);
			
			PlayerStats stats = ZArena.getInstance().getGameHandler().getPlayerStats(player);
			
			GenericLabel name = new GenericLabel(player.getName());
			name.setAnchor(WidgetAnchor.CENTER_CENTER);
			name.setAlign(WidgetAnchor.TOP_CENTER);
			name.setHeight(10);
			name.setWidth(10);
			name.shiftYPos(-85 + index * 15);
			name.shiftXPos(-75);
			
			GenericLabel kills = new GenericLabel(stats.getPoints() + "");
			kills.setAnchor(WidgetAnchor.CENTER_CENTER);
			kills.setAlign(WidgetAnchor.TOP_CENTER);
			kills.setHeight(10);
			kills.setWidth(10);
			kills.shiftYPos(-85 + index * 15);
			
			GenericLabel money = new GenericLabel(stats.getMoney() + "");
			money.setAnchor(WidgetAnchor.CENTER_CENTER);
			money.setAlign(WidgetAnchor.TOP_CENTER);
			money.setHeight(10);
			money.setWidth(10);
			money.shiftYPos(-85 + index * 15);
			money.shiftXPos(50);
			
			GenericLabel health = new GenericLabel(player.getHealth() + "");
			health.setAnchor(WidgetAnchor.CENTER_CENTER);
			health.setAlign(WidgetAnchor.TOP_CENTER);
			health.setHeight(10);
			health.setWidth(10);
			health.shiftYPos(-85 + index * 15);
			health.shiftXPos(100);
			
			tabText.add(name);
			tabText.add(money);
			tabText.add(kills);
			tabText.add(health);
			
			index++;
		}
		
		((SpoutPlayer) Bukkit.getPlayer(player)).getMainScreen().attachWidget(ZArena.getInstance(), tabHeader);
		for(GenericTexture part : tabMain)
		{
			((SpoutPlayer) Bukkit.getPlayer(player)).getMainScreen().attachWidget(ZArena.getInstance(), part);
		}
		for(GenericLabel text : tabText)
		{
			((SpoutPlayer) Bukkit.getPlayer(player)).getMainScreen().attachWidget(ZArena.getInstance(), text);
		}
	}
	
	public void closeTabScreen()
	{
		if(tabHeader == null)
			return;
		((SpoutPlayer) Bukkit.getPlayer(player)).getMainScreen().removeWidget(tabHeader);
		for(GenericTexture part : tabMain)
		{
			((SpoutPlayer) Bukkit.getPlayer(player)).getMainScreen().removeWidget(part);
		}
		for(GenericLabel text : tabText)
		{
			((SpoutPlayer) Bukkit.getPlayer(player)).getMainScreen().removeWidget(text);
		}
		tabMain.clear();
		tabText.clear();
	}
	
	public void openVotingScreen(String[] options)
	{
		GenericPopup voting = new GenericPopup();
		for(int i = 0; i < 3; i++)
		{
			GenericButton button = new ZVotingButton((i+1) + ":" + options[i]);
			button.setHeight(24);
			button.setWidth(128);
			button.setAlign(WidgetAnchor.CENTER_CENTER);
			button.setAnchor(WidgetAnchor.CENTER_CENTER);
			button.shiftYPos(-24 + i * 24);
			button.shiftXPos(button.getWidth() / -2);
			voting.attachWidget(ZArena.getInstance(), button);
		}
		((SpoutPlayer) Bukkit.getPlayer(player)).getMainScreen().attachPopupScreen(voting);
	}
	
	private String isEnabled(boolean enabled)
	{
		return (enabled) ? "Enabled" : "Disabled";
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int ver = in.readInt();
		
		if(ver == 0)
		{
			player = in.readUTF();
			
			votingScreenEnabled = in.readBoolean();
			zombieTexturesEnabled = in.readBoolean();
			waveCounterEnabled = in.readBoolean();
		}
		else if(ver == 1)
		{
			player = in.readUTF();
			
			votingScreenEnabled = in.readBoolean();
			zombieTexturesEnabled = in.readBoolean();
			waveCounterEnabled = in.readBoolean();
		}
		else
		{
			ZArena.logger.log(Level.WARNING, "An unsupported version of the PlayerOptions failed to load.");
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(VERSION);
		
		out.writeUTF(player);
		
		out.writeBoolean(votingScreenEnabled);
		out.writeBoolean(zombieTexturesEnabled);
		out.writeBoolean(waveCounterEnabled);
	}
}
