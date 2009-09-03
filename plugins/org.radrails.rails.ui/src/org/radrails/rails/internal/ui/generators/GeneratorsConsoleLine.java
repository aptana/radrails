package org.radrails.rails.internal.ui.generators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.radrails.rails.internal.ui.RailsConsoleLine;

public class GeneratorsConsoleLine extends RailsConsoleLine
{

	private static final String RAILS_ROOT = "#{RAILS_ROOT}";
	private static Pattern fgGeneratorPattern = Pattern
			.compile("\\s+\\w+\\s+((?:\\w+/)*\\w+\\.[(rb)(rhtml)(css)(js)(yml)(haml)(sass)(html\\.erb)(xml\\.builder)(rxml)])");
	private static Pattern OPEN_TRACE_LINE_PATTERN = Pattern.compile("\\s*(\\S.*?):(\\d+)(:|$)");	
	private static Pattern fgHAMLPattern = Pattern.compile("\\s+([a-zA-z0-9\\._/]+):(\\d+):in\\s+");

	public GeneratorsConsoleLine(String line, IProject project)
	{
		tryGeneratorPattern(line);
		if (!tryHAMLPattern(line))
			tryNormalPattern(line);
		if (isRelativePath() && project != null)
		{
			makeRelativeToWorkspace(project);
		}
	}

	private void tryNormalPattern(String line)
	{
		Matcher matcher = OPEN_TRACE_LINE_PATTERN.matcher(line);
		if (!matcher.find())
		{
			return;
		}

		fFilename = matcher.group(1);
		int matchEnd = line.indexOf(fFilename) + fFilename.length();
		if (fFilename.startsWith(RAILS_ROOT))
		{
			fFilename = "." + fFilename.substring(13);
		}
		String rest = line.substring(matchEnd + 1);
		int index = rest.indexOf(":");
		if (index == -1)
			index = rest.length();
		fLineNumber = Integer.parseInt(rest.substring(0, index));

		fOffset = matcher.start(1);
		fLength = fFilename.length();
		fLength += 13 + index;
	}

	private boolean tryHAMLPattern(String line)
	{
		Matcher matcher = fgHAMLPattern.matcher(line);
		if (!matcher.find())
		{
			return false;
		}

		fFilename = matcher.group(1);
		fOffset = matcher.start(1);
		fLength = matcher.end(2) - fOffset;
		fLineNumber = Integer.parseInt(matcher.group(2));
		return true;
	}
	
	private void tryGeneratorPattern(String line)
	{
		Matcher matcher = fgGeneratorPattern.matcher(line);
		if (!matcher.find())
		{
			return;
		}

		fFilename = matcher.group(1);
		int matchEnd = line.indexOf(fFilename) + fFilename.length();
		String rest = line.substring(matchEnd);
		fFilename += rest.trim();
		
		if (fFilename.endsWith(":"))
			fFilename = fFilename.substring(0, fFilename.length() - 1);

		fOffset = matcher.start(1);
		fLength = fFilename.length();
	}

	public static boolean isMatching(String line)
	{
		if (line == null)
			return false;
		if (line.trim().length() == 0)
			return false;
		if (line.startsWith("       route  "))
			return false;
		if (line.endsWith("..."))
			return false;

		Matcher matcher = fgGeneratorPattern.matcher(line);
		if (matcher.find())
			return true;
		
		matcher = fgHAMLPattern.matcher(line);
		if (matcher.find())
			return true;

		matcher = OPEN_TRACE_LINE_PATTERN.matcher(line);
		if (matcher.find())
		{
			String filename = matcher.group(1);
			return filename.startsWith(RAILS_ROOT);
		}
		return false;
	}

}
