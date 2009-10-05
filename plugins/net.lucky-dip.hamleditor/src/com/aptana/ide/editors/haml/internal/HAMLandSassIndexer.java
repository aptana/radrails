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
		for (BuildContext context : contexts)
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
		// Save the indexes now (so it gets saved to disk!)
		saveModifiedIndices();
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

	public static void main(String[] args)
	{
		// SASS
		Matcher m = SASS_COLOR_PATTERN.matcher("  color: #abcdef\n    color: #123");
		while (m.find())
			System.out.println("Sass Color Match: '" + m.group() + "'");

		m = SASS_CLASS_PATTERN.matcher(".chris\n  .red\n    color: red");
		while (m.find())
			System.out.println("Sass Class Match: '" + m.group(1) + "'");

		m = SASS_ID_PATTERN.matcher("#chris\n  color: red\n.users #userTab\n  color: #abc\ndiv\n  width: 42px");
		while (m.find())
			System.out.println("Sass ID Match: '" + m.group(1) + "'");
		
		// HAML
		m = HAML_CLASS_PATTERN.matcher(".content\n  .articles\n    .article.title Doogie Howser Comes Out\n    .article.date 2006-11-05\n");
		while (m.find())
			System.out.println("HAML Class Match: '" + m.group(1) + "'");

		m = HAML_ID_PATTERN.matcher("%div#things\n  %span#rice Chicken Fried\n  %p.beans{ :food => 'true' } The magical fruit\n  %h1.class.otherclass#id La La La\n");
		while (m.find())
			System.out.println("HAML ID Match: '" + m.group(1) + "'");
	}

}
