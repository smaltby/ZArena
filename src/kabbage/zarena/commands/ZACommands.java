package kabbage.zarena.commands;

import kabbage.zarena.commands.utils.ArgumentCountException;
import kabbage.zarena.commands.utils.ECommand;
import kabbage.zarena.utils.StringEnums;

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
			switch(StringEnums.valueOf(command.get(1).toUpperCase()))
			{
			case ENTER: case JOIN:
				handler.joinGame();
				break;
			case LEAVE:
				handler.leaveGame();
				break;
			case RELOAD:
				handler.reloadConfig();
				break;
			case STATS:
				handler.showGameStats();
				break;
			case VOTE:
				helpMessage = "/zarena vote <level num>";
				handler.vote(command.get(2));
				break;
			case LIST:
				handler.listLevels();
				break;
			case NEW: case CREATE:
				helpMessage = "/zarena create <level name>";
				handler.createLevel(command.get(2));
				break;
			case DELETE: case REMOVE:
				helpMessage = "/zarena remove <level name>";
				handler.removeLevel(command.get(2));
				break;
			case SAVE:
				handler.saveLevels();
				break;
			case LOAD:
				helpMessage = "/zarena load <level name>";
				handler.loadLevel(command.get(2));
				break;
			case GAMEMODE:
				if(command.hasArgAtIndex(2))
					handler.setGameMode(command.get(2));
				handler.getGameMode();	//No else statement, so it still sends the player the new gamemode after setting it
				break;
			case ALIVE:
				handler.listAlive();
				break;
			case SETALIVE:
				helpMessage = "/zarena setalive <player> <true|false>";
				handler.setAlive(command.get(2), command.get(3));
				break;
			case SETLEAVELOCATION: case SETLEAVELOC:
				handler.setLeaveLocation();
				break;
			case OPTIONS:
				helpMessage = "zarena options";
				handler.openOptions();
				break;
			case SESSION:
				handler.listSession();
				break;
			case START:
				handler.startGame();
				break;
			case STOP:
				handler.stopGame();
				break;
			case SETWAVE:
				helpMessage = "/zarena setwave <wave>";
				handler.setWave(command.get(2));
				break;
			case INFO:
				handler.sendInfo("general");
				break;
			case WAVE:
				handler.sendInfo("wave");
				break;
			case DIA: case DIAGNOSTIC:
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
