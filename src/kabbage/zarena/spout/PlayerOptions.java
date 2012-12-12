package kabbage.zarena.spout;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Level;

import kabbage.zarena.ZArena;

import org.bukkit.Bukkit;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

public class PlayerOptions implements Externalizable
{
	private static final long serialVersionUID = "PLAYEROPTIONS".hashCode(); //DO NOT CHANGE
	private static final int VERSION = 0;
	
	private transient GenericLabel infoScreen;
	
	private String player;
	//Buttons
	public boolean votingScreenEnabled;
	public boolean zombieTexturesEnabled;
	public boolean infoBarEnabled;
	//Checkmark thingies
	public boolean waveChecked;
	public boolean moneyChecked;
	public boolean killsChecked;
	public boolean remainingZombiesChecked;
	public boolean aliveCountChecked;
	public boolean gamemodeChecked;
	
	/**
	 * Empty constructor for externalization.
	 */
	public PlayerOptions()
	{
		infoScreen = new GenericLabel();
	}
	
	public PlayerOptions(String player)
	{
		infoScreen = new GenericLabel();
		
		this.player = player;
		
		votingScreenEnabled = true;
		zombieTexturesEnabled = true;
		infoBarEnabled = true;
		
		waveChecked = true;
		moneyChecked = true;
		killsChecked = false;
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
	
	public void openOptions()
	{
		ZOptionsButton votingScreen = new ZOptionsButton("Voting Popup: "+isEnabled(votingScreenEnabled));
		ZOptionsButton zombieTextures = new ZOptionsButton("Zombie Textures: "+isEnabled(zombieTexturesEnabled));
		ZOptionsButton infoBar = new ZOptionsButton("Info Bar: "+isEnabled(infoBarEnabled));
		
		votingScreen.setAnchor(WidgetAnchor.CENTER_CENTER);
		zombieTextures.setAnchor(WidgetAnchor.CENTER_CENTER);
		infoBar.setAnchor(WidgetAnchor.CENTER_CENTER);
		
		votingScreen.shiftXPos(-120);
		votingScreen.shiftYPos(-80);
		zombieTextures.shiftXPos(-120);
		zombieTextures.shiftYPos(-30);
		infoBar.shiftXPos(-120);
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
		points.setChecked(killsChecked);
		remaining.setChecked(remainingZombiesChecked);
		aliveCount.setChecked(aliveCountChecked);
		gamemode.setChecked(gamemodeChecked);
		
		ZCloseButton close = new ZCloseButton("Close");
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
			killsChecked = in.readBoolean();
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
		out.writeBoolean(killsChecked);
		out.writeBoolean(remainingZombiesChecked);
		out.writeBoolean(aliveCountChecked);
		out.writeBoolean(gamemodeChecked);
	}
}
