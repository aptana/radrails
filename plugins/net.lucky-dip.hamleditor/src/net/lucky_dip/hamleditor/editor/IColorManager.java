package net.lucky_dip.hamleditor.editor;

import org.eclipse.swt.graphics.Color;

public interface IColorManager
{

	Color getColor(String hamlComment);

	void dispose();

}
