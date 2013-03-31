package main.java.com.github.zarena.spout;

import main.java.com.github.zarena.ZArena;

import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.GenericButton;

public class ZVotingButton extends GenericButton
{
	private ZArena plugin;
	public ZVotingButton(String text)
	{
		super(text);
		plugin = ZArena.getInstance();
	}
	
	@Override
	public void onButtonClick(final ButtonClickEvent event)
	{
		int vote = Integer.parseInt(event.getButton().getText().substring(0, 1));
		plugin.getGameHandler().getLevelVoter().castVote(vote, event.getPlayer());
		event.getPlayer().getMainScreen().getActivePopup().close();
	}
}
