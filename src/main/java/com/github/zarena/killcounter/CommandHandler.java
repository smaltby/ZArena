package com.github.zarena.killcounter;

import java.util.Map.Entry;

import org.bukkit.command.CommandSender;

import com.github.zarena.commands.CommandSenderWrapper;
import com.github.zarena.commands.ECommand;
import com.github.zarena.utils.Message;
import com.github.zarena.utils.Utils;

public class CommandHandler
{
	private KillCounter kc;
	private CommandSenderWrapper senderWrapper;
	private ECommand command;
	
	public CommandHandler(CommandSender sender, ECommand command)
	{
		kc = KillCounter.instance;
		
		senderWrapper = new CommandSenderWrapper(sender);
		this.command = command;
	}
	
	public void addKills(String name, String amountString)
	{
		if(!senderWrapper.canControlKillCounter())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		int amount = Utils.parseInt(amountString, -1);
		if(amount < 0)
			senderWrapper.sendMessage(Message.ADD_KILLS_MUST_BE_GREATER_THAN0.formatMessage());
		kc.setKills(name, kc.getKills(name) + amount);
		senderWrapper.sendMessage(Message.KILLS_SET.formatMessage(name, kc.getKills(name)));
	}

	public void sendTopPlayers()
	{
		int playersToShow = Utils.parseInt(command.getArgAtIndex(2), 5);
		if(playersToShow > 30)
			playersToShow = 30;
		senderWrapper.sendMessage(Message.TOP_KILLERS_HEADER.formatMessage());
		for(int i = 0; i < playersToShow; i++)
		{
			Entry<String, Integer> entry = kc.getEntry(i);
			if(entry == null) break;
			senderWrapper.sendMessage(Message.TOP_KILLERS_ITEM.formatMessage(i+1, entry.getKey(), entry.getValue()));
		}
	}

	public void setKills(String name, String amountString)
	{
		if(!senderWrapper.canControlKillCounter())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		int amount = Utils.parseInt(amountString, -1);
		if(amount < 0)
			senderWrapper.sendMessage(Message.SET_KILLS_MUST_BE_GREATER_OR_EQUAL_TO0.formatMessage());
		kc.setKills(name, amount);
		senderWrapper.sendMessage(Message.KILLS_SET.formatMessage(name, kc.getKills(name)));
		
	}

	public void sendPlayer(String name)
	{
		Integer kills = kc.getKills(name);
		if(kills == null)
		{
			senderWrapper.sendMessage(Message.PLAYER_NOT_FOUND.formatMessage());
			return;
		}
		int rank = kc.indexOf(name) + 1;
		senderWrapper.sendMessage(Message.PLAYER_KILLS_INFO.formatMessage(name, kills, rank, kc.mapSize()));
	}
	
	public void subKills(String name, String amountString)
	{
		if(!senderWrapper.canControlKillCounter())
		{
			senderWrapper.sendMessage(Message.INSUFFICIENT_PERMISSIONS.formatMessage());
			return;
		}
		int amount = Utils.parseInt(amountString, -1);
		if(amount < 0)
			senderWrapper.sendMessage(Message.SUB_KILLS_MUST_BE_GREATER_THAN0.formatMessage());
		kc.setKills(name, kc.getKills(name) - amount);
		senderWrapper.sendMessage(Message.KILLS_SET.formatMessage(name, kc.getKills(name)));
	}
}