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
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericTexture;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

public class PlayerOptions implements Externalizable
{
	private static final long serialVersionUID = "PLAYEROPTIONS".hashCode(); //DO NOT CHANGE
	private static final int VERSION = 0;
	
	private transient GenericLabel infoScreen;
	private transient GenericTexture waveCounter;
	private transient GenericLabel waveCounterWave;		//Label that shows current wave in waveCounter
	private transient GenericLabel waveCounterZombies;	//Label that shows remaining zombies in waveCounter
	
	private String player;
	//Buttons
	public boolean votingScreenEnabled;
	public boolean zombieTexturesEnabled;
	public boolean infoBarEnabled;
	//Checkmark thingies
	public boolean waveChecked;
	public boolean moneyChecked;
	public boolean pointsChecked;
	public boolean remainingZombiesChecked;
	public boolean aliveCountChecked;
	public boolean gamemodeChecked;
	
	/**
	 * Empty constructor for externalization.
	 */
	public PlayerOptions()
	{
		infoScreen = new GenericLabel();
		infoScreen.setVisible(false);
		infoScreen.setAnchor(WidgetAnchor.TOP_CENTER);
		infoScreen.setAlign(WidgetAnchor.CENTER_CENTER);
		infoScreen.shiftYPos(5);
		infoScreen.setHeight(10);
		infoScreen.setWidth(50);
		
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
		
		waveCounterWave = new GenericLabel();
		waveCounterWave.setVisible(false);
		waveCounterWave.setAnchor(WidgetAnchor.TOP_RIGHT);
		waveCounterWave.setAlign(WidgetAnchor.CENTER_CENTER);
		waveCounterWave.shiftXPos(-35);
		waveCounterWave.shiftYPos(37);
		waveCounterWave.setTextColor(new Color(150, 0, 10));
		waveCounterWave.setText("Wave: ");
		waveCounterWave.setHeight(10);
		waveCounterWave.setWidth(10);
		
		waveCounterZombies = new GenericLabel();
		waveCounterZombies.setVisible(false);
		waveCounterZombies.setAnchor(WidgetAnchor.TOP_RIGHT);
		waveCounterZombies.setAlign(WidgetAnchor.CENTER_CENTER);
		waveCounterZombies.shiftXPos(-35);
		waveCounterZombies.shiftYPos(20);
		waveCounterZombies.setTextColor(new Color(150, 0, 10));
		waveCounterZombies.setScale(2);
		waveCounterZombies.setText("0");
		waveCounterZombies.setHeight(20);
		waveCounterZombies.setWidth(20);
	}
	
	public PlayerOptions(String player)
	{
		this();
		
		this.player = player;
		
		votingScreenEnabled = true;
		zombieTexturesEnabled = true;
		infoBarEnabled = true;
		
		waveChecked = true;
		moneyChecked = true;
		pointsChecked = false;
		remainingZombiesChecked = true;
		aliveCountChecked = false;
		gamemodeChecked = false;
	}
	
	public String getPlayerName()
	{
		return player;
	}
	
	public GenericLabel getInfoScreen()
	{
		return infoScreen;
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
		ZOptionsButton infoBar = new ZOptionsButton("Info Bar: "+isEnabled(infoBarEnabled));
		
		votingScreen.setAnchor(WidgetAnchor.CENTER_CENTER);
		zombieTextures.setAnchor(WidgetAnchor.CENTER_CENTER);
		infoBar.setAnchor(WidgetAnchor.CENTER_CENTER);
		
		votingScreen.shiftXPos(-160);
		votingScreen.shiftYPos(-80);
		zombieTextures.shiftXPos(-160);
		zombieTextures.shiftYPos(-30);
		infoBar.shiftXPos(-160);
		infoBar.shiftYPos(20);
		
		votingScreen.setWidth(150);
		votingScreen.setHeight(20);
		zombieTextures.setWidth(150);
		zombieTextures.setHeight(20);
		infoBar.setWidth(150);
		infoBar.setHeight(20);
		
		GenericLabel infoOptions = new GenericLabel("Info Bar Options: ");
		ZOptionsCheckBox wave = new ZOptionsCheckBox("Wave");
		ZOptionsCheckBox money = new ZOptionsCheckBox("Money");
		ZOptionsCheckBox points = new ZOptionsCheckBox("Points");
		ZOptionsCheckBox remaining = new ZOptionsCheckBox("Zombies Remaining");
		ZOptionsCheckBox aliveCount = new ZOptionsCheckBox("Players Alive");
		ZOptionsCheckBox gamemode = new ZOptionsCheckBox("Gamemode");
		
		infoOptions.setAnchor(WidgetAnchor.CENTER_CENTER);
		wave.setAnchor(WidgetAnchor.CENTER_CENTER);
		money.setAnchor(WidgetAnchor.CENTER_CENTER);
		points.setAnchor(WidgetAnchor.CENTER_CENTER);
		remaining.setAnchor(WidgetAnchor.CENTER_CENTER);
		aliveCount.setAnchor(WidgetAnchor.CENTER_CENTER);
		gamemode.setAnchor(WidgetAnchor.CENTER_CENTER);
		
		infoOptions.shiftXPos(80);
		infoOptions.shiftYPos(-100);
		wave.shiftXPos(80);
		wave.shiftYPos(-80);
		money.shiftXPos(80);
		money.shiftYPos(-60);
		points.shiftXPos(80);
		points.shiftYPos(-40);
		remaining.shiftXPos(80);
		remaining.shiftYPos(-20);
		aliveCount.shiftXPos(80);
		aliveCount.shiftYPos(0);
		gamemode.shiftXPos(80);
		gamemode.shiftYPos(20);
		
		infoOptions.setHeight(10);
		infoOptions.setWidth(80);
		wave.setHeight(10);
		wave.setWidth(80);
		money.setHeight(10);
		money.setWidth(80);
		points.setHeight(10);
		points.setWidth(80);
		remaining.setHeight(10);
		remaining.setWidth(80);
		aliveCount.setHeight(10);
		aliveCount.setWidth(80);
		gamemode.setHeight(10);
		gamemode.setWidth(80);
		
		wave.setChecked(waveChecked);
		money.setChecked(moneyChecked);
		points.setChecked(pointsChecked);
		remaining.setChecked(remainingZombiesChecked);
		aliveCount.setChecked(aliveCountChecked);
		gamemode.setChecked(gamemodeChecked);
		
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
		popup.attachWidget(ZArena.getInstance(), infoBar);
		popup.attachWidget(ZArena.getInstance(), infoOptions);
		popup.attachWidget(ZArena.getInstance(), wave);
		popup.attachWidget(ZArena.getInstance(), money);
		popup.attachWidget(ZArena.getInstance(), points);
		popup.attachWidget(ZArena.getInstance(), remaining);
		popup.attachWidget(ZArena.getInstance(), aliveCount);
		popup.attachWidget(ZArena.getInstance(), gamemode);
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
			infoBarEnabled = in.readBoolean();
			
			waveChecked = in.readBoolean();
			moneyChecked = in.readBoolean();
			pointsChecked = in.readBoolean();
			remainingZombiesChecked = in.readBoolean();
			aliveCountChecked = in.readBoolean();
			gamemodeChecked = in.readBoolean();
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
		out.writeBoolean(infoBarEnabled);
		
		out.writeBoolean(waveChecked);
		out.writeBoolean(moneyChecked);
		out.writeBoolean(pointsChecked);
		out.writeBoolean(remainingZombiesChecked);
		out.writeBoolean(aliveCountChecked);
		out.writeBoolean(gamemodeChecked);
	}
}
