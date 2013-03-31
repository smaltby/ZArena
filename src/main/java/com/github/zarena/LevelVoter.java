package main.java.com.github.zarena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.Random;

import main.java.com.github.zarena.commands.CommandSenderWrapper;
import main.java.com.github.zarena.events.GameStartCause;
import main.java.com.github.zarena.events.GameStartEvent;
import main.java.com.github.zarena.events.LevelChangeCause;
import main.java.com.github.zarena.events.LevelChangeEvent;
import main.java.com.github.zarena.spout.SpoutHandler;
import main.java.com.github.zarena.utils.ChatHelper;
import main.java.com.github.zarena.utils.Constants;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class LevelVoter implements Runnable
{
	private GameHandler gameHandler;
	private ZArena plugin;
	private Map<ZLevel, Gamemode> options;
	private Random rnd;
	
	private Map<Player, Integer> mappedVotes;
	private int[] votes;
	private String[] currentVoteMessage;
	
	public LevelVoter(GameHandler instance)
	{
		plugin = ZArena.getInstance();
		gameHandler = instance;
		options = new LinkedHashMap<ZLevel, Gamemode>();
		mappedVotes = new HashMap<Player, Integer>();
		votes = new int[3];
		currentVoteMessage = new String[]{"","",""};
		rnd = new Random();
	}
	
	/**
	 * Cast a vote to one of the level choices.
	 * 
	 * @param vote the number representing which level being voted on
	 * @param player the player casting the vote. If he has certain permissions, he may get extra votes, and if he enters a vote for a level that isn't there,
	 * he will be informed of such
	 */
	public void castVote(int vote, Player player)
	{
		if(vote < 1 || vote > 3)
		{
			player.sendMessage(ChatColor.RED+"Vote must be an integer ranging from 1 to 3.");
			return;
		}
		int voteCount = 1;
		voteCount += new CommandSenderWrapper(player).extraVotes();
		//Set start index to 0, not 1.
		vote -= 1;
		//Remove previous votes of this player, if applicable
		if(mappedVotes.containsKey(player))
			votes[mappedVotes.get(player)] -= voteCount;
		//Add the vote(s)
		votes[vote] += voteCount;
		mappedVotes.put(player, vote);
		if(mappedVotes.size() == gameHandler.getPlayers().size())	//If everyone voted
		{
			Bukkit.getScheduler().cancelTask(gameHandler.voterTaskID);
			run();
		}
		player.sendMessage(ChatColor.GREEN+"Thank you for voting!");
	}
	
	public String[] getVoteMessage()
	{
		return currentVoteMessage;
	}
	
	/**
	 * Start the vote. Choose the levels and gamemodes to be voted on.
	 */
	public void startVoting()
	{
		gameHandler.isVoting = true;
		int maps = gameHandler.getLevelHandler().getLevels().size();
		if(maps == 0)
			ZArena.logger.log(Level.WARNING, "The game has attempted to start a vote, but there are no levels to be voted on.");
		if(maps > 3)
			maps = 3;
		int normals = rnd.nextInt(2) + 1;
		//Set up the options with levels and gamemodes
		List<Gamemode> takenGamemodes = new ArrayList<Gamemode>();
		for(int i = 0; i < maps; i++)
		{
			Gamemode optionGm = gameHandler.defaultGamemode;
			if((!(normals >= 3 - i) && rnd.nextFloat() >= .5) || normals == 0)
			{
				optionGm = Gamemode.getRandomGamemode(takenGamemodes);
				takenGamemodes.add(optionGm);
			}
			else
				normals--;
			options.put(gameHandler.getLevelHandler().getRandomLevel(options.keySet()), optionGm);
		}
		
		ChatHelper.broadcastMessage(ChatHelper.VOTE_START, gameHandler.getBroadcastPlayers());
		byte b = 1;
		//Go through the entries, broadcasting each one.
		for(Entry<ZLevel, Gamemode> entry : options.entrySet())
		{
			if(entry.getKey() == null)
				continue;
			String levelName = entry.getKey().getName();
			String gmName = entry.getValue().toString();
			currentVoteMessage[b-1] = ChatHelper.broadcastMessage(String.format(ChatHelper.VOTE_OPTION, b, levelName, gmName), gameHandler.getBroadcastPlayers());
			b++;
		}
		ChatHelper.broadcastMessage(String.format(ChatHelper.VOTE_ENDS_IN, plugin.getConfig().getInt(Constants.VOTING_LENGTH)), gameHandler.getBroadcastPlayers());
		
		//Send vote screens to spout players with the option enabled
		final String[] optionsArray = new String[3];
		int index = 0;
		for(Entry<ZLevel, Gamemode> option : options.entrySet())
		{
			optionsArray[index] = option.getKey().getName()+"<"+option.getValue().getName()+">";
			index++;
		}
		if(plugin.isSpoutEnabled())
			SpoutHandler.openVotingScreens(optionsArray);
		gameHandler.voterTaskID = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, plugin.getConfig().getInt(Constants.VOTING_LENGTH) * 20);
	}

	public void resetVoting()
	{
		options.clear();
		mappedVotes.clear();
		votes = new int[3];
		currentVoteMessage = new String[]{"","",""};
		gameHandler.isVoting = false;
	}
	
	/**
	 * Check the votes to see which level got the most.
	 */
	@Override
	public void run()
	{
		int leadingVoteIndex = 0;
		int highest = -1;
		//Go through the vote array, seeing which index has the most votes. Set that index as the leadingVoteIndex
		for(int i = 0; i < 3; i++)
		{
			ChatHelper.broadcastMessage(String.format(ChatHelper.VOTES_FOR_MAP, i + 1, votes[i]), gameHandler.getBroadcastPlayers());
			if(votes[i] > highest)
			{
				highest = votes[i];
				leadingVoteIndex = i;
			}
		}
		byte b = 0;
		//Go through the entries, broadcasting the one that is in the index equal to leadingVoteIndex, and setting it as the next game's level
		for(Entry<ZLevel, Gamemode> entry : options.entrySet())
		{
			if(b == leadingVoteIndex)
			{
				LevelChangeEvent event = new LevelChangeEvent(gameHandler.getLevel(), entry.getKey(), LevelChangeCause.VOTE);
				Bukkit.getServer().getPluginManager().callEvent(event);
				gameHandler.setLevel(entry.getKey());
				gameHandler.setGameMode(entry.getValue());
				ChatHelper.broadcastMessage(String.format(ChatHelper.MAP_CHOSEN, entry.getKey().getName(), entry.getValue()), gameHandler.getBroadcastPlayers());
				break;
			}
			b++;
		}
		resetVoting();
		GameStartEvent event = new GameStartEvent(GameStartCause.VOTE);
		Bukkit.getServer().getPluginManager().callEvent(event);
		//Run the next game with the chosen level/gamemode
		gameHandler.start();
	}
}
