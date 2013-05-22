package com.github.zarena.commands;

import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.zarena.utils.Permissions;

/**
 * @author joshua
 */
public class CommandSenderWrapper
{
    protected CommandSender sender;
    protected Player player;
    protected boolean console;

    public CommandSenderWrapper(CommandSender sender)
    {
        this.sender = sender;

        player = (sender instanceof Player ? (Player) sender : null);

        console = !(sender instanceof Player);
    }



    /**
     * Returns the player representation of the sender.
     *
     * @return the Player, or null if the sender is not a player
     */
    public Player getPlayer()
    {
        return player;
    }

    /**
     * Returns the CommandSender that this class wraps
     *
     * @return
     */
    public CommandSender getSender()
    {
        return sender;
    }

    //**************************************************************************
    //PERMISSIONS-RELATED METHODS
    //**************************************************************************
    public boolean isAdmin()
    {
    	return hasExternalPermissions(Permissions.ADMIN.toString(), true);
    }
    
    public boolean canCreateLevels()
    {
    	return hasExternalPermissions(Permissions.LEVEL_EDITOR.toString(), true) || isAdmin();
    }
    
    public boolean canControlGames()
    {
    	return hasExternalPermissions(Permissions.GAME_CONTROL.toString(), true) || isAdmin();
    }
    
    public boolean canControlKillCounter()
    {
    	return hasExternalPermissions(Permissions.GAME_CONTROL.toString(), true) || isAdmin();
    }
    
    public boolean canEnterLeaveGames()
    {
    	return hasExternalPermissions(Permissions.ENTER_LEAVE.toString(), true) || canControlGames() || canCreateLevels() || isAdmin();
    }
    
    public boolean canVote()
    {
    	return hasExternalPermissions(Permissions.VOTER.toString(), true) || canControlGames() || canCreateLevels() || isAdmin();
    }
    
    public boolean autoJoin()
    {
    	return !hasExternalPermissions(Permissions.NOAUTOJOIN.toString(), false);
    }
    
    public int startMoney()
    {
    	for(Entry<String, Integer> e : Permissions.startMoneyPermissions.entrySet())
    	{
    		if(hasExternalPermissions(e.getKey(), false))
    		{
    			return e.getValue();
    		}
    	}
    	return 0;
    }
    
    public int extraVotes()
    {
    	for(Entry<String, Integer> e : Permissions.extraVotesPermissions.entrySet())
    	{
    		if(hasExternalPermissions(e.getKey(), false))
    		{
    			return e.getValue();
    		}
    	}
    	return 0;
    }
    
    /**
     *
     * @param node
     * @return
     */
    public boolean hasExternalPermissions(String node, boolean countOp)
    {
        return (!(this.getSender() instanceof Player)) || (this.getSender().isOp() && countOp) || this.getSender().hasPermission(node);
    }

    /**
     *
     * @return whether or not the CommandSender is the console
     */
    public boolean isConsole()
    {
        return console;
    }
    
    /**
     * Sends the sender a message.
     *
     * @param msg The message to be send
     */
    public void sendMessage(String msg)
    {
        sender.sendMessage(msg);
    }
}
