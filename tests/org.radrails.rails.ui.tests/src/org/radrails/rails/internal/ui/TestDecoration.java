package org.radrails.rails.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class TestDecoration implements IDecoration
{

	private List<ImageDescriptor> overlays;
	private List<String> prefixes;
	private ArrayList<String> suffixes;
	private Color foreground;
	private Font font;
	private Color background;

	public TestDecoration()
	{
		this.overlays = new ArrayList<ImageDescriptor>();
		this.prefixes = new ArrayList<String>();
		this.suffixes = new ArrayList<String>();
	}

	public void addOverlay(ImageDescriptor overlay)
	{
		this.overlays.add(overlay);
	}

	public void addOverlay(ImageDescriptor overlay, int quadrant)
	{
		this.overlays.add(overlay);
	}

	public void addPrefix(String prefix)
	{
		this.prefixes.add(prefix);
	}

	public void addSuffix(String suffix)
	{
		this.suffixes.add(suffix);
	}

	public IDecorationContext getDecorationContext()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setBackgroundColor(Color color)
	{
		this.background = color;
	}

	public void setFont(Font font)
	{
		this.font = font;
	}

	public void setForegroundColor(Color color)
	{
		this.foreground = color;
	}

	public List<ImageDescriptor> getOverlays()
	{
		return overlays;
	}

}
