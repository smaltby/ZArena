package com.github.zarena.spout;

import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.GenericButton;

public class ZCloseButton extends GenericButton
{
	public ZCloseButton(String text)
	{
		super(text);
	}
	@Override
	public void onButtonClick(final ButtonClickEvent event)
	{
		event.getPlayer().getMainScreen().getActivePopup().close();
	}
}
