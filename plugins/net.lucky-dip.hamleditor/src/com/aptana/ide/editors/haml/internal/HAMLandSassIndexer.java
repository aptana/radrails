package com.aptana.ide.editors.haml.internal;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.aptana.ide.core.builder.BuildContext;
import com.aptana.ide.core.builder.BuildParticipant;
import com.aptana.ide.editor.css.CSSColors;
import com.aptana.ide.editor.css.IIndexConstants;
import com.aptana.ide.index.core.Index;
import com.aptana.ide.index.core.IndexManager;

public class HAMLandSassIndexer extends BuildParticipant
{

	private Index fIndex;
	private Set<Index> indices;

	private static final Pattern SASS_COLOR_PATTERN = Pattern.compile("#([a-f0-9A-F]{6}|[a-f0-9A-F]{3})");
	private static final Pattern SASS_CLASS_PATTERN = Pattern.compile("\\s*(\\.[\\w\\-]+)\\s+");
	private static final Pattern SASS_ID_PATTERN = Pattern.compile("\\s*(#[\\w\\-]+)\\s+");

	private static final Pattern HAML_CLASS_PATTERN = Pattern.compile("[\\w\\s]*(\\.[\\w\\-]+)");
	private static final Pattern HAML_ID_PATTERN = SASS_ID_PATTERN;

	public HAMLandSassIndexer()
	{
		super();
	}

	@Override
	public void buildStarting(List<BuildContext> contexts, boolean isBatch, IProgressMonitor monitor)
	{
		indices = new HashSet<Index>();
	}

	@Override
	public void build(BuildContext context, IProgressMonitor monitor)
	{
		String extension = context.getFile().getFileExtension();
		if (extension != null && extension.equalsIgnoreCase("sass"))
		{
			indexSASS(context);
		}
		else if (extension != null && extension.equalsIgnoreCase("haml"))
		{
			indexHAML(context);
		}
	}

	@Override
	public void buildFinishing(IProgressMonitor monitor)
	{
		// Save the indexes now (so it gets saved to disk!)
		saveModifiedIndices();
		indices.clear();
	}

	private void saveModifiedIndices()
	{
		for (Index index : indices)
		{
			try
			{
				if (index != null)
					index.save();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		indices.clear();
		indices = null;
	}

	private void indexHAML(BuildContext context)
	{
		String src = context.getContents();
		// Index classes
		Matcher m = HAML_CLASS_PATTERN.matcher(src);
		while (m.find())
		{
			String match = m.group(1);
			if (isMeasurement(match))
				continue;
			addIndex(context, IIndexConstants.CSS_CLASS, match.substring(1));
		}
		// Index IDs
		m = HAML_ID_PATTERN.matcher(src);
		while (m.find())
		{
			String match = m.group(1);
			if (isColor(match))
				continue;
			addIndex(context, IIndexConstants.CSS_IDENTIFIER, match.substring(1));
		}
	}

	private boolean isColor(String value)
	{
		if (value == null || value.trim().length() == 0)
			return false;
		if (CSSColors.namedColorExists(value))
			return true;
		if (value.startsWith("#") && (value.length() == 4 || value.length() == 7))
		{
			return SASS_COLOR_PATTERN.matcher(value).find();
		}
		return false;
	}

	private void addIndex(BuildContext context, String category, String word)
	{
		Index index = getIndex(context);
		indices.add(index);
		index.addEntry(category, word, context.getFile().getProjectRelativePath().toPortableString());
	}

	private void indexSASS(BuildContext context)
	{
		String src = context.getContents();
		// Index colors
		Matcher m = SASS_COLOR_PATTERN.matcher(src);
		while (m.find())
		{
			String match = m.group();
			if (!isColor(match))
				continue;
			addIndex(context, IIndexConstants.CSS_COLOR, CSSColors.to6CharHexWithLeadingHash(match));
		}
		// Index classes
		m = SASS_CLASS_PATTERN.matcher(src);
		while (m.find())
		{
			String match = m.group(1);
			if (isMeasurement(match))
				continue;
			addIndex(context, IIndexConstants.CSS_CLASS, match.substring(1));
		}
		// Index IDs
		m = SASS_ID_PATTERN.matcher(src);
		while (m.find())
		{
			String match = m.group(1);
			if (isColor(match))
				continue;
			addIndex(context, IIndexConstants.CSS_IDENTIFIER, match.substring(1));
		}
	}

	private boolean isMeasurement(String match)
	{
		return match.length() >= 4 && Character.isDigit(match.charAt(1)) && match.endsWith("em");
	}

	private Index getIndex(BuildContext context)
	{
		if (fIndex == null)
		{
			IProject project = context.getFile().getProject();
			fIndex = IndexManager.getInstance().getIndex(project.getFullPath().toPortableString());
		}
		return fIndex;
	}

	@Override
	public void cleanStarting(IProject project)
	{
		Index index = IndexManager.getInstance().getIndex(project.getFullPath().toPortableString());
		index.removeCategories(IIndexConstants.CSS_CLASS, IIndexConstants.CSS_IDENTIFIER);
	}

	@Override
	public boolean isActive(IProject project)
	{
		return true;
	}
}
