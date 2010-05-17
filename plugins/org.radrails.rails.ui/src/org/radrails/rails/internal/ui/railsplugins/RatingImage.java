package org.radrails.rails.internal.ui.railsplugins;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.radrails.rails.ui.RailsUIPlugin;

abstract class RatingImage
{

	private static final float MAX_STARS = 5.0f;
	private static Image empty;
	private static Image filled;
	private static Set<Image> fgImages = new HashSet<Image>();

	public static void init(Display display)
	{
		dispose();
		empty = RailsUIPlugin.getImageDescriptor("icons/unrated.png").createImage();
		filled = RailsUIPlugin.getImageDescriptor("icons/rated.png").createImage();
		fgImages = new HashSet<Image>();
	}

	public static void dispose()
	{
		if (fgImages != null)
		{
			for (Image image : fgImages)
			{
				image.dispose();
			}
			fgImages.clear();
			fgImages = null;
		}
		if (empty != null)
		{
			empty.dispose();
		}
		if (filled != null)
		{
			filled.dispose();
		}
		empty = null;
		filled = null;
	}

	// FIXME Share images with same number of stars!
	public static Image createRatingImage(float rating, Display display)
	{
		if (filled == null || empty == null)
			return null;

		rating = forceWithinRange(rating, 0, MAX_STARS);
		if (rating == MAX_STARS)
			return copyImage(filled, display);
		if (rating == 0.0)
			return copyImage(empty, display);

		try
		{
			int fullWidth = filled.getBounds().width;
			float ratingInStars = rating / MAX_STARS;
			int widthToFill = (int) (ratingInStars * fullWidth);
			if (widthToFill <= 0)
				return copyImage(empty, display);
			return overlay(filled, empty, widthToFill, display);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Assumes src and dest images are same size. Copies pixels from src on top of dest from left to right, starting at
	 * 0 and proceeding until widthToFill.
	 * 
	 * @param src
	 * @param dest
	 * @param widthToFill
	 * @param display
	 * @return The combined image
	 */
	private static Image overlay(Image src, Image dest, int widthToFill, Display display)
	{
		Rectangle bounds = src.getBounds();
		ImageData destData = dest.getImageData();
		ImageData srcData = src.getImageData();

		for (int y = 0; y < bounds.height; y++)
		{ // for each row of pixels
			byte[] alphas = new byte[widthToFill]; // copy over alpha values
			srcData.getAlphas(0, y, widthToFill, alphas, 0);
			destData.setAlphas(0, y, widthToFill, alphas, 0);

			int[] pixels = new int[widthToFill]; // copy over rgb values
			srcData.getPixels(0, y, widthToFill, pixels, 0);
			destData.setPixels(0, y, widthToFill, pixels, 0);
		}
		Image image = new Image(display, destData);
		fgImages.add(image);
		return image;
	}

	private static Image copyImage(Image image, Display display)
	{
		Image created = new Image(display, image, SWT.IMAGE_COPY);
		fgImages.add(created);
		return created;
	}

	private static float forceWithinRange(float rating, float min, float max)
	{
		if (rating < min)
			return min;
		if (rating > max)
			return max;
		return rating;
	}
}
