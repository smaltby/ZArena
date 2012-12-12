package kabbage.zarena.spout;

import kabbage.zarena.ZArena;

import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericCheckBox;

public class ZOptionsCheckBox extends GenericCheckBox
{
	private ZArena plugin;
	
	public ZOptionsCheckBox(String text)
	{
		super(text);
		plugin = ZArena.getInstance();
	}
	
	@Override
	public void onButtonClick(ButtonClickEvent event)
	{
		PlayerOptions options = plugin.getPlayerOptionsHandler().getOptions(event.getPlayer().getName());
		Button button = event.getButton();
		switch(button.getText())
		{
		case "Wave":
			options.waveChecked = !options.waveChecked;
			break;
		case "Money":
			options.moneyChecked = !options.moneyChecked;
			break;
		case "Kills":
			options.killsChecked = !options.killsChecked;
			break;
		case "Zombies Remaining":
			options.remainingZombiesChecked = !options.remainingZombiesChecked;
			break;
		case "Players Alive":
			options.aliveCountChecked = !options.aliveCountChecked;
			break;
		case "Gamemode":
			options.gamemodeChecked = !options.gamemodeChecked;
			break;
		default:
		}
	}
}
