package main.java.com.github.zarena.spout;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import main.java.com.github.zarena.ZArena;


public class PlayerOptionsHandler implements Externalizable
{
	private static final long serialVersionUID = "PLAYEROPTIONSHANDLER".hashCode(); //DO NOT CHANGE
	private static final int VERSION = 0;
	
	private List<PlayerOptions> playerOptionsList;
	
	public PlayerOptionsHandler()
	{
		playerOptionsList = new ArrayList<PlayerOptions>();
	}
	
	public void addOptions(PlayerOptions options)
	{
		playerOptionsList.add(options);
	}
	
	public PlayerOptions getOptions(String player)
	{
		for(PlayerOptions options : playerOptionsList)
		{
			if(options.getPlayerName().equalsIgnoreCase(player))
				return options;
		}
		PlayerOptions options = new PlayerOptions(player);
		playerOptionsList.add(options);
		return options;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int ver = in.readInt();
		
		if(ver == 0)
		{
			playerOptionsList = (List<PlayerOptions>) in.readObject();
		}
		else
		{
			ZArena.logger.log(Level.SEVERE, "An unsupported version of the PlayerOptionsHandler failed to load.");
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(VERSION);
		
		out.writeObject(playerOptionsList);
	}
}
