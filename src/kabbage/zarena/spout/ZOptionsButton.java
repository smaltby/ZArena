package kabbage.zarena.spout;

import kabbage.zarena.ZArena;
import kabbage.zarena.utils.ChatHelper;

import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericButton;

public class ZOptionsButton extends GenericButton
{
	private ZArena plugin;
	public ZOptionsButton(String text)
	{
		super(text);
		plugin = ZArena.getInstance();
	}
	
	@Override
	public void onButtonClick(final ButtonClickEvent event)
	{
		ChatHelper.broadcastMessage("check");
		PlayerOptions options = plugin.getPlayerOptionsHandler().getOptions(event.getPlayer().getName());
		Button button = event.getButton();
		boolean enableDisable = button.getText().endsWith("Enabled") ? false : true;
		switch(button.getText().replaceAll("Enabled", "").replaceAll("Disabled", ""))
		{
		case "Voting Popup: ":
			options.votingScreenEnabled = enableDisable;
			break;
		case "Zombie Textures: ":
			options.zombieTexturesEnabled = enableDisable;
			break;
		case "Info Bar: ":
			options.infoBarEnabled = enableDisable;
			break;
		default:
			return;
		}
		button.setText((enableDisable) ? "Disabled" : "Enabled");
		//Close and open to update the button change.
		event.getPlayer().getMainScreen().getActivePopup().close();
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				PlayerOptions options2 = plugin.getPlayerOptionsHandler().getOptions(event.getPlayer().getName());
				options2.openOptions();
			}
		},10L);
		
	}
}
