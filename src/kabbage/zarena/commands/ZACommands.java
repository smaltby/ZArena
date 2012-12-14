package kabbage.zarena.commands;

import kabbage.zarena.commands.utils.ArgumentCountException;
import kabbage.zarena.commands.utils.ECommand;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ZACommands implements CommandExecutor
{
	public ZACommands()
	{
	}

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
			switch(command.get(1))
			{
			case "enter": case "join":
				handler.joinGame();
				break;
			case "leave":
				handler.leaveGame();
				break;
			case "reload":
				handler.reloadConfig();
				break;
			case "stats":
				handler.showGameStats();
				break;
			case "vote":
				helpMessage = "/zarena vote <level num>";
				handler.vote(command.get(2));
				break;
			case "list":
				handler.listLevels();
				break;
			case "new": case "create":
				helpMessage = "/zarena create <level name>";
				handler.createLevel(command.get(2));
				break;
			case "delete": case "remove":
				helpMessage = "/zarena remove <level name>";
				handler.removeLevel(command.get(2));
				break;
			case "save":
				handler.saveLevels();
				break;
			case "load":
				helpMessage = "/zarena load <level name>";
				handler.loadLevel(command.get(2));
				break;
			case "gamemode":
				if(command.hasArgAtIndex(2))
					handler.setGameMode(command.get(2));
				handler.getGameMode();	//No else statement, so it still sends the player the new gamemode after setting it
				break;
			case "alive":
				handler.listAlive();
				break;
			case "setalive":
				helpMessage = "/zarena setalive <player> <true|false>";
				handler.setAlive(command.get(2), command.get(3));
				break;
			case "setleavelocation": case "setleaveloc":
				handler.setLeaveLocation();
				break;
			case "options":
				helpMessage = "zarena options";
				handler.openOptions();
				break;
			case "session":
				handler.listSession();
				break;
			case "start":
				handler.startGame();
				break;
			case "stop":
				handler.stopGame();
				break;
			case "setwave":
				helpMessage = "/zarena setwave <wave>";
				handler.setWave(command.get(2));
				break;
			case "info":
				handler.sendInfo("general");
				break;
			case "wave":
				handler.sendInfo("wave");
				break;
			case "dia": case "diagnostic":
				helpMessage = "/zarena dia <general|healthperwave|zombiesperwave|wave|spawnchance|checknextwave>";
				handler.sendInfo(command.get(2));
				break;
			default:
				hardFailure = true;
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
			sender.sendMessage(ChatColor.RED + helpMessage);
		return true;
	}
}
