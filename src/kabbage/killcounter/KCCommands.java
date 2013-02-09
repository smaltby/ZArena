package kabbage.killcounter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kabbage.zarena.commands.utils.ArgumentCountException;
import kabbage.zarena.commands.utils.ECommand;
import kabbage.zarena.utils.StringEnums;

public class KCCommands implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		ECommand command = new ECommand(commandLabel, args);
		CommandHandler handler = new CommandHandler(sender, command);
		
		String helpMessage = "";
		boolean softFailure = false; //If true, the string helpMessage is sent to sender. return true.
		boolean hardFailure = false; //If true, return false. (sender gets sent the usage)
		try
		{
			switch(StringEnums.valueOf(command.get(1).toUpperCase()))
			{
			case TOP:
				handler.sendTopPlayers();
				return true;
			case SET:
				handler.setKills(command.get(1), command.get(2));
			default:
				if(command.get(1).startsWith("@")) 
					handler.sendPlayer(command.get(1));
				else 
					handler.sendPlayer(((Player) sender).getName());
			}
		} catch (ArgumentCountException ex) //If the sender does not use an adequate amount of arguments
		{
			if (ex.getErrorIndex() == 1)
				hardFailure = true;
			else
				softFailure = true;
		} catch(ClassCastException e) //If the command tries to get a Player from the sender, but the sender is the console
		{
			helpMessage = "You must be a Player to execute this command.";
			softFailure = true;
		}

		if(hardFailure)
			return false;
		else if(softFailure)
		{
			sender.sendMessage(ChatColor.RED + helpMessage);
			return true;
		}
		return true;
	}
}
