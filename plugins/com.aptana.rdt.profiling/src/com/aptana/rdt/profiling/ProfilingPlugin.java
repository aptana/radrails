package com.aptana.rdt.profiling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.aptana.rdt.internal.profiling.ProfileThread;
import com.aptana.rdt.internal.profiling.StatisticsGrabber;

/**
 * The activator class controls the plug-in life cycle
 */
public class ProfilingPlugin extends AbstractUIPlugin
{

	private static final int BACKGROUND_SHIFT = 12;

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "com.aptana.rdt.profiling";

	// The shared instance
	private static ProfilingPlugin plugin;

	private static Map<RGB, Color> fgColorKey = new HashMap<RGB, Color>();;

	private StatisticsGrabber listener;

	private Set<IProfilingListener> profilingListeners;

	private List<ProfileThread> lastResult;

	/**
	 * The constructor
	 */
	public ProfilingPlugin()
	{
		plugin = this;
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		profilingListeners = new HashSet<IProfilingListener>();
		listener = new StatisticsGrabber();
		DebugPlugin.getDefault().addDebugEventListener(listener);
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		try
		{
			for (Color color : fgColorKey.values())
			{
				color.dispose();
			}
			fgColorKey.clear();
			DebugPlugin.getDefault().removeDebugEventListener(listener);
			listener = null;
			plugin = null;
		}
		finally
		{
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static ProfilingPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Add profiling listener
	 * 
	 * @param listener
	 */
	public void addProfilingListener(IProfilingListener listener)
	{
		profilingListeners.add(listener);
	}

	/**
	 * Remove profiling listener
	 * 
	 * @param listener
	 */
	public void removeProfilingListener(IProfilingListener listener)
	{
		profilingListeners.remove(listener);
	}

	/**
	 * Profiling ended
	 * 
	 * @param input
	 */
	public void profilingEnded(List<ProfileThread> input)
	{
		for (IProfilingListener listener : profilingListeners)
		{
			listener.profilingEnded(input);
		}
		lastResult = input;
	}

	/**
	 * Gets the last profiling result
	 * 
	 * @return - list
	 */
	public List<ProfileThread> lastProfilingResult()
	{
		return lastResult;
	}

	/**
	 * Logs an exception
	 * 
	 * @param e
	 */
	public static void log(Exception e)
	{
		getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, 1, e.getMessage(), e));
	}

	/**
	 * Gets a color
	 * 
	 * @param rgb
	 * @return - color
	 */
	public static Color getColor(RGB rgb)
	{
		if (fgColorKey.containsKey(rgb))
		{
			return fgColorKey.get(rgb);
		}
		Color color = new Color(Display.getDefault(), rgb);
		fgColorKey.put(rgb, color);
		return color;
	}

	/**
	 * Creates a pianoed color from an original color
	 * 
	 * @param bg
	 * @return - pianoed color
	 */
	public static Color createPianoedColor(Color bg)
	{
		if (bg == null)
		{
			return null;
		}
		boolean canGoDarker = bg.getRed() - BACKGROUND_SHIFT > 0 || bg.getGreen() - BACKGROUND_SHIFT > 0
				|| bg.getBlue() - BACKGROUND_SHIFT > 0;
		boolean canGoLighter = bg.getRed() + BACKGROUND_SHIFT > 0 || bg.getGreen() + BACKGROUND_SHIFT > 0
				|| bg.getBlue() + BACKGROUND_SHIFT > 0;
		int red = 0;
		int green = 0;
		int blue = 0;
		if (canGoDarker)
		{
			red = bg.getRed() - BACKGROUND_SHIFT > 0 ? (int) (bg.getRed() - BACKGROUND_SHIFT) : bg.getRed();
			green = bg.getGreen() - BACKGROUND_SHIFT > 0 ? (int) (bg.getGreen() - BACKGROUND_SHIFT) : bg.getGreen();
			blue = bg.getBlue() - BACKGROUND_SHIFT > 0 ? (int) (bg.getBlue() - BACKGROUND_SHIFT) : bg.getBlue();
		}
		else if (canGoLighter)
		{
			red = bg.getRed() + BACKGROUND_SHIFT > 0 ? (int) (bg.getRed() + BACKGROUND_SHIFT) : bg.getRed();
			green = bg.getGreen() + BACKGROUND_SHIFT > 0 ? (int) (bg.getGreen() + BACKGROUND_SHIFT) : bg.getGreen();
			blue = bg.getBlue() + BACKGROUND_SHIFT > 0 ? (int) (bg.getBlue() + BACKGROUND_SHIFT) : bg.getBlue();
		}
		if (canGoDarker || canGoLighter)
		{
			return getColor(new RGB(red, green, blue));
		}
		return null;
	}

}
