/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL youelect, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ide.editor.erb.contentassist;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.radrails.rails.core.RailsConventions;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.ui.text.RailsHeuristicCompletionComputer;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.ISourceFolder;
import org.rubypeople.rdt.core.ISourceFolderRoot;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.search.CollectingSearchRequestor;
import org.rubypeople.rdt.core.search.FieldReferenceMatch;
import org.rubypeople.rdt.core.search.IRubySearchConstants;
import org.rubypeople.rdt.core.search.IRubySearchScope;
import org.rubypeople.rdt.core.search.MethodReferenceMatch;
import org.rubypeople.rdt.core.search.SearchEngine;
import org.rubypeople.rdt.core.search.SearchMatch;
import org.rubypeople.rdt.core.search.SearchParticipant;
import org.rubypeople.rdt.core.search.SearchPattern;
import org.rubypeople.rdt.core.search.TypeNameRequestor;
import org.rubypeople.rdt.internal.core.RubyGlobal;
import org.rubypeople.rdt.internal.core.RubyModelManager;
import org.rubypeople.rdt.internal.corext.util.RDocUtil;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.text.ruby.RubyCompletionProcessor;
import org.rubypeople.rdt.internal.ui.text.template.contentassist.RubyTemplateAccess;
import org.rubypeople.rdt.ui.RubyElementLabelProvider;
import org.rubypeople.rdt.ui.text.RubyTextTools;

import com.aptana.ide.core.IdeLog;
import com.aptana.ide.editor.erb.ERBPlugin;
import com.aptana.ide.editor.erb.preferences.IPreferenceConstants;
import com.aptana.ide.editors.UnifiedEditorsPlugin;
import com.aptana.ide.editors.unified.EditorFileContext;
import com.aptana.ide.editors.unified.IUnifiedViewer;

/**
 * ERBContentAssistProcessor
 */
public class ERBContentAssistProcessor extends TemplateCompletionProcessor implements IContentAssistProcessor
{

	private static final String APPLICATION_CONTROLLER_CLASSNAME = "ApplicationController";

	private static final String APPLICATION_HELPER_FILENAME = "application_helper.rb";

	private static final String APPLICATION_CONTROLLER_FILENAME = "application.rb";

	private static final String PERFORMANCE_EVENT = "com.aptana.ide.editor.erb/perf/content_assist";

	private static final boolean MEASURE_PERFORMANCE = true;

	private static final String HELPERS = "helpers";
	private static final String CONTROLLERS = "controllers";
	private static final String HELPER_METHOD = "helper_method";

	private static Image fIconKeyword = UnifiedEditorsPlugin.getImage("icons/keyword.gif"); //$NON-NLS-1$
	private static Image fIconTemplate = ERBPlugin.getImage("icons/rails.gif"); //$NON-NLS-1$

	private EditorFileContext fContext;
	private static RubyElementLabelProvider labelProvider = new RubyElementLabelProvider();
	private int fOffset;

	private IUnifiedViewer unifiedViewer;
	private String fPrefix = "";
	private IEditorPart fEditor;

	private static List<CachedProposal> fgActionViewHelpers;

	private static String[] keywordProposals;

	private static String[] preDefinedGlobals = { "$!", "$@", "$_", "$.", "$&", "$n", "$~", "$=", "$/", "$\\", "$0",
			"$*", "$$", "$?", "$:" };

	private static String[] globalContexts = { "error message", "position of an error occurrence",
			"latest read string by `gets'", "latest read number of line by interpreter",
			"latest matched string by the regexep.", "latest matched string by nth parentheses of regexp.",
			"data for latest matche for regexp", "whether or not case-sensitive in string matching",
			"input record separator", "output record separator", "the name of the ruby script file",
			"command line arguments for the ruby scpript", "PID for ruby interpreter",
			"status of the latest executed child process", "array of paths that ruby interpreter searches for files" };

	/**
	 * ERBContentAssistProcessor constructor
	 * 
	 * @param editor
	 * @param context
	 */
	public ERBContentAssistProcessor(IEditorPart editor, EditorFileContext context)
	{
		super();
		fEditor = editor;
		fContext = context;
	}

	private File getControllersFolder()
	{
		try
		{
			IPath view = getViewPath();
			if (view == null)
				return null;
			IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(view.removeLastSegments(1));
			if (folder == null)
				return null;
			File dir = folder.getLocation().toFile();
			if (dir == null)
				return null;
			dir = dir.getParentFile();
			if (dir == null)
				return null;
			dir = dir.getParentFile(); // now in /app
			if (dir == null)
				return null;
			return new File(dir, CONTROLLERS);
		}
		catch (Exception e)
		{
			// ignore
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#extractPrefix(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	protected String extractPrefix(ITextViewer viewer, int offset)
	{
		int i = offset;
		IDocument document = viewer.getDocument();
		if (i > document.getLength())
		{
			return ""; //$NON-NLS-1$
		}

		try
		{
			while (i > 0)
			{
				char ch = document.getChar(i - 1);
				if (Character.isJavaIdentifierPart(ch))
				{
					i--;
					String pref = null;
					if (i - 3 > 0)
					{
						pref = document.get(i - 3, 3);
						if (pref.equals("<%="))
						{
							i -= 3;
							break;
						}
						else if (pref.endsWith("<%"))
						{
							pref = document.get(i - 2, 2);
							i -= 2;
							break;
						}
					}
					else if (i - 2 > 0)
					{
						pref = document.get(i - 2, 2);
						if (pref.endsWith("<%"))
						{
							i -= 2;
							break;
						}
					}
				}
				else if (ch == '=')
				{
					i--;
				}
				else if (ch == '%')
				{
					i--;
				}
				else if (ch == '<')
				{
					i--;
				}
				else
				{
					break;
				}
			}
			return document.get(i, offset - i);
		}
		catch (BadLocationException e)
		{
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
	{
		final PerformanceStats stats;
		boolean isDebug = isDebug();
		if (MEASURE_PERFORMANCE || isDebug)
		{
			stats = PerformanceStats.getStats(PERFORMANCE_EVENT, this);
			stats.startRun("computeCompletionProposals()");
		}
		else
		{
			stats = null;
		}

		long start = System.currentTimeMillis();

		this.fOffset = offset;
		if (viewer instanceof IUnifiedViewer)
		{
			unifiedViewer = (IUnifiedViewer) viewer;
		}
		this.fPrefix = getPrefix(viewer.getDocument(), offset);
		long prefix = System.currentTimeMillis();

		List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();

		// Try the special cases, if they get us results, return immediately
		if (isDebug)
			log(ERBPlugin.getDefault(), "trying completions for :controller => hash values");
		list.addAll(tryControllerCompletions()); // Try ":controller =>" completions
		if (!list.isEmpty())
			return list.toArray(new ICompletionProposal[list.size()]);
		if (isDebug)
			log(ERBPlugin.getDefault(), "trying completions for :action => hash values");
		list.addAll(tryActionCompletions()); // Try ":action =>" completions
		if (!list.isEmpty())
			return list.toArray(new ICompletionProposal[list.size()]);
		if (isDebug)
			log(ERBPlugin.getDefault(), "trying completions for :partial => hash values");
		list.addAll(tryPartialCompletions()); // Try ":partial =>" completions
		if (!list.isEmpty())
			return list.toArray(new ICompletionProposal[list.size()]);
		long special = System.currentTimeMillis();

		// Now try the general ruby completions
		if (isDebug)
			log(ERBPlugin.getDefault(), "trying Ruby completions");
		ContentAssistant assist = new ContentAssistant();
		RubyCompletionProcessor p = new RubyCompletionProcessor(fEditor, assist, IDocument.DEFAULT_CONTENT_TYPE);
		ICompletionProposal[] originals = p.computeCompletionProposals(viewer, fOffset);
		list.addAll(Arrays.asList(originals));
		long completions = System.currentTimeMillis();

		// If indices are ready, also try Rails helpers and controller instance vars
		// FIXME Refactor common stuff into RailsConventions
		long controllerInstance = 0;
		long matchingHelper = 0;
		long appHelper = 0;
		long actionViewHelpers = 0;
		if (indicesAreReady())
		{
			if (isDebug)
			{
				log(ERBPlugin.getDefault(), "Search indices are ready...");
				log(ERBPlugin.getDefault(),
						"trying completions for instance variables defined in matching action in controller");
			}
			IType relatedController = getController();
			list.addAll(getControllerInstanceVariables(relatedController)); // all instance variables referred to in
			// action FIXME Make
			controllerInstance = System.currentTimeMillis();
			// this only those that are written to?
			if (isDebug)
				log(ERBPlugin.getDefault(), "trying completions for methods in helper");
			list.addAll(getHelperMethods(getRelatedHelper())); // all methods inside matching helper TODO Limit to only
			// public?
			matchingHelper = System.currentTimeMillis();
			if (isDebug)
				log(ERBPlugin.getDefault(), "trying completions for methods in aplication_helper.rb");
			list.addAll(getHelperMethods(getApplicationHelper())); // all methods inside application_helper.rb
			appHelper = System.currentTimeMillis();
			if (isDebug)
				log(ERBPlugin.getDefault(), "trying completions for ActionView helpers in Rails");
			list.addAll(getActionViewHelpers()); // all public methods inside Rails ActionView helpers that are mixed
			// into every view
			actionViewHelpers = System.currentTimeMillis();
			if (isDebug)
				log(ERBPlugin.getDefault(), "trying completions for controller methods marked as helper");
			list.addAll(getControllerMethodsMarkedAsHelper(relatedController));
			if (isDebug)
				log(ERBPlugin.getDefault(),
						"trying completions for controller methods marked as helper in application_controller.rb");
			list.addAll(getControllerMethodsMarkedAsHelper(getApplicationController())); // TODO Actually search up
			// hierarchy, rather
			// than just check ApplicationController

			Collections.sort(list, new Comparator<ICompletionProposal>()
			{

				public int compare(ICompletionProposal o1, ICompletionProposal o2)
				{
					return o1.getDisplayString().compareTo(o2.getDisplayString());
				}

			});
		}
		if (isDebug)
			log(ERBPlugin.getDefault(), "trying completions for keywords");
		long keywordStart = System.currentTimeMillis();
		list.addAll(Arrays.asList(determineKeywordProposals(viewer, offset))); // all keywords
		if (isDebug)
			log(ERBPlugin.getDefault(), "trying completions for templates");
		long keywords = System.currentTimeMillis();
		list.addAll(Arrays.asList(determineTemplateProposals(viewer, offset))); // all templates
		long templates = System.currentTimeMillis();
		Collections.sort(list, new Comparator<ICompletionProposal>()
		{

			public int compare(ICompletionProposal o1, ICompletionProposal o2)
			{
				if (o1 != null && o1.getDisplayString() != null && o2 != null && o2.getDisplayString() != null)
				{
					return o1.getDisplayString().compareToIgnoreCase(o2.getDisplayString());
				}
				return 0;
			}

		});
		long sort = System.currentTimeMillis();
		if (MEASURE_PERFORMANCE || isDebug)
		{
			stats.endRun();
			long end = System.currentTimeMillis();
			StringWriter stringWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(stringWriter);
			PerformanceStats.printStats(writer);
			log(ERBPlugin.getDefault(), stringWriter.getBuffer().toString());
			log(ERBPlugin.getDefault(), "ERB Assist (total): " + (end - start));
			log(ERBPlugin.getDefault(), "ERB Assist (grabbing prefix): " + (prefix - start));
			log(ERBPlugin.getDefault(), "ERB Assist (special cases): " + (special - prefix));
			log(ERBPlugin.getDefault(), "ERB Assist (Normal Completions): " + (completions - special));
			log(ERBPlugin.getDefault(), "ERB Assist (Controller instance vars): " + (controllerInstance - completions));
			log(ERBPlugin.getDefault(), "ERB Assist (Matching helper): " + (matchingHelper - controllerInstance));
			log(ERBPlugin.getDefault(), "ERB Assist (App helper): " + (appHelper - matchingHelper));
			log(ERBPlugin.getDefault(), "ERB Assist (Action View Helpers): " + (actionViewHelpers - appHelper));
			log(ERBPlugin.getDefault(), "ERB Assist (keywords): " + (keywords - keywordStart));
			log(ERBPlugin.getDefault(), "ERB Assist (templates): " + (templates - keywords));
			log(ERBPlugin.getDefault(), "ERB Assist (Sorting): " + (sort - templates));
		}

		return list.toArray(new ICompletionProposal[list.size()]);
	}

	private IRubyScript getApplicationHelper()
	{
		IProject project = getProject();
		if (project == null)
			return null;
		IPath railsRoot = RailsPlugin.findRailsRoot(project);
		if (railsRoot == null)
			railsRoot = new Path("");
		IPath applicationControllerPath = railsRoot.append(HELPERS).append(APPLICATION_HELPER_FILENAME);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = workspaceRoot.getFile(applicationControllerPath);
		return RubyCore.create(file);
	}

	private IType getApplicationController()
	{
		IProject project = getProject();
		if (project == null)
			return null;
		IPath railsRoot = RailsPlugin.findRailsRoot(project);
		if (railsRoot == null)
			railsRoot = new Path("");
		IPath applicationControllerPath = railsRoot.append(CONTROLLERS).append(APPLICATION_CONTROLLER_FILENAME);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = workspaceRoot.getFile(applicationControllerPath);
		IRubyScript script = RubyCore.create(file);
		if (script == null)
			return null;
		return script.getType(APPLICATION_CONTROLLER_CLASSNAME);
	}

	private IProject getProject()
	{
		IFile viewFile = getViewFile();
		if (viewFile == null)
			return null;
		return viewFile.getProject();
	}

	private IRubyScript getRelatedHelper()
	{
		IFile helperFile = RailsConventions.getHelperFromView(getViewFile());
		if (helperFile == null)
			return null;
		return RubyCore.create(helperFile);
	}

	private void log(ERBPlugin plugin, String string)
	{
		IdeLog.log(plugin, IStatus.INFO, string, null);
	}

	private Collection<? extends ICompletionProposal> tryPartialCompletions()
	{
		List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
		String fullPrefix = getFullPrefix(unifiedViewer.getDocument(), fOffset);
		if (looksLikePartialValue(fullPrefix))
		{
			list.addAll(getPartialsCompletions(fullPrefix, fOffset));
		}
		return list;
	}

	private Collection<? extends ICompletionProposal> tryActionCompletions()
	{
		Map<String, File> completions = RailsHeuristicCompletionComputer.getActionCompletions(getControllersFolder(),
				unifiedViewer.getDocument(), fOffset);
		return tryCompletions(completions);
	}

	private Collection<? extends ICompletionProposal> tryControllerCompletions()
	{
		Map<String, File> completions = RailsHeuristicCompletionComputer.getControllerCompletions(
				getControllersFolder(), unifiedViewer.getDocument(), fOffset);
		return tryCompletions(completions);
	}

	private Collection<? extends ICompletionProposal> tryCompletions(Map<String, File> completions)
	{
		List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
		if (completions == null || completions.isEmpty())
			return list;
		for (String replacement : completions.keySet())
		{
			ICompletionProposal prop = new ERBCompletionProposal(replacement, fOffset, 0, replacement.length(), null,
					replacement, null, null, -1, unifiedViewer, null, completions.get(replacement).getAbsolutePath());
			list.add(prop);
		}
		return list;
	}

	private boolean looksLikePartialValue(String fullPrefix)
	{
		return Pattern.matches(".*:partial\\s*=>\\s*['|\"]?", fullPrefix);
	}

	private List<ICompletionProposal> getPartialsCompletions(String fullPrefix, int offset)
	{
		List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
		File[] partials = getPartials();
		for (int i = 0; i < partials.length; i++)
		{
			File partial = partials[i];
			// TODO Move a lot of this code into PartialProposal!
			String filename = partial.getName();
			String name = filename.substring(1);
			if (name.endsWith(".rhtml"))
			{
				name = name.substring(0, name.length() - 6);
			}
			else if (name.endsWith(".erb.html"))
			{
				name = name.substring(0, name.length() - 9);
			}
			// Handle partials in same folder vs other folders!
			IPath view = getViewPath().removeLastSegments(1);
			IFolder viewsFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(view);
			String viewsFolderPath = viewsFolder.getLocation().toOSString();
			String absolutePath = partial.getAbsolutePath();
			if (!absolutePath.startsWith(viewsFolderPath))
			{
				// need to use filename relative to app/views
				String parentName = partial.getParentFile().getName(); // FIXME This assumes no nested folders!
				name = parentName + '/' + name;
				filename = parentName + '/' + filename;
			}

			String replacement = name;
			// Only add these closing quotes if next char is not a closing quote
			char next = 'a';
			try
			{
				if (offset < unifiedViewer.getDocument().getLength())
					next = unifiedViewer.getDocument().getChar(offset);
			}
			catch (BadLocationException e)
			{
				// ignore
			}
			if (fullPrefix.endsWith("'"))
			{
				if (next != '\'')
					replacement = replacement + "'";
			}
			else if (fullPrefix.endsWith("\""))
			{
				if (next != '"')
					replacement = replacement + "\"";
			}
			else
			{
				replacement = "'" + replacement + "'";
			}
			ICompletionProposal prop = new PartialProposal(partial, replacement, offset, name, unifiedViewer, filename);
			list.add(prop);
		}
		return list;
	}

	private File[] getPartials()
	{
		IPath view = getViewPath();
		File dir = ResourcesPlugin.getWorkspace().getRoot().getFolder(view.removeLastSegments(1)).getLocation()
				.toFile();
		File viewsDir = dir.getParentFile();
		List<File> partials = getPartials(viewsDir);
		return partials.toArray(new File[partials.size()]);
	}

	/**
	 * Recursively grab partials in all subdirectories
	 * 
	 * @param dir
	 * @return - list of file
	 */
	private List<File> getPartials(File dir)
	{
		File[] subdirs = getSubdirectories(dir);
		List<File> list = new ArrayList<File>();
		for (int i = 0; i < subdirs.length; i++)
		{
			File[] partials = subdirs[i].listFiles(new FilenameFilter()
			{

				public boolean accept(File dir, String name)
				{
					return name.startsWith("_");
				}

			});
			for (int j = 0; j < partials.length; j++)
			{
				list.add(partials[j]);
			}
			list.addAll(getPartials(subdirs[i]));
		}
		return list;
	}

	private File[] getSubdirectories(File viewsDir)
	{
		return viewsDir.listFiles(new FileFilter()
		{

			public boolean accept(File pathname)
			{
				return pathname.isDirectory();
			}

		});
	}

	private boolean indicesAreReady()
	{
		SearchEngine engine = new SearchEngine();
		IRubySearchScope scope = SearchEngine.createWorkspaceScope(); // initialize all containers and variables
		try
		{
			engine.searchAllTypeNames(null,
					"!@$#!@".toCharArray(), //$NON-NLS-1$
					SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE, IRubySearchConstants.CLASS, scope,
					new TypeNameRequestor()
					{
						public void acceptType(boolean isModule, char[] packageName, char[] simpleTypeName,
								char[][] enclosingTypeNames, String path)
						{
							// no type to accept
						}
					},
					// will not activate index query caches if indexes are not ready, since it would take to long
					// to wait until indexes are fully rebuild
					IRubySearchConstants.CANCEL_IF_NOT_READY_TO_SEARCH, null);
		}
		catch (Exception e)
		{
			// if index isn't ready, don't do any searches.
			return false;
		}
		return true;
	}

	private List<ICompletionProposal> getControllerMethodsMarkedAsHelper(IType controller)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		if (controller == null || !controller.exists())
			return proposals;
		List<SearchMatch> matches = search(controller, HELPER_METHOD, IRubyElement.METHOD,
				IRubySearchConstants.REFERENCES);
		for (SearchMatch match : matches)
		{
			if (match instanceof MethodReferenceMatch)
			{
				MethodReferenceMatch refMatch = (MethodReferenceMatch) match;
				List<String> arguments = refMatch.getArguments();
				for (String arg : arguments)
				{
					IMethod method = getMethod(controller, arg);
					ICompletionProposal proposal = createProposal(method);
					if (proposal != null)
						proposals.add(proposal);
				}
			}
		}
		return proposals;
	}

	private IType getController()
	{
		return RailsConventions.getControllerTypeFromViewFile(getViewFile());
	}

	private IRubyProject getRubyProject()
	{
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(getViewPath());
		return RubyModelManager.getRubyModelManager().getRubyModel().getRubyProject(file);
	}

	private IPath getViewPath()
	{
		String uriString = fContext.getSourceProvider().getSourceURI();
		URI uri = URI.create(uriString);
		File aFile = new File(uri.getPath());
		IPath viewPath = Path.fromOSString(aFile.getAbsolutePath());
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath rootPath = workspaceRoot.getLocation();
		if (rootPath.isPrefixOf(viewPath))
		{
			viewPath = viewPath.removeFirstSegments(rootPath.segmentCount());
		}
		return viewPath.setDevice(null);
	}

	private static class CachedProposal
	{
		private String name;
		private String path;
		private int type;
		private Image image;
		private IRubyElement element;

		/**
		 * CachedProposal
		 * 
		 * @param element
		 */
		public CachedProposal(IRubyElement element)
		{
			this.name = element.getElementName();
			this.path = element.getPath().toOSString();
			this.element = element;
			this.type = element.getElementType();
			this.image = labelProvider.getImage(element);
		}

		/**
		 * @param offset
		 * @param prefix
		 * @param unifiedViewer
		 * @return - completion proposal
		 */
		public ERBCompletionProposal getProposal(int offset, String prefix, IUnifiedViewer unifiedViewer)
		{
			return new LazyERBCompletionProposal(element, name, offset - prefix.length(), prefix.length(), name
					.length(), image, name, null, null, type, unifiedViewer, null, path);
		}
	}

	/**
	 * Grabs the methods defined in all of the action view helpers. Caches these proposals after first retrieval because
	 * they shouldn't ever change.
	 */
	private List<ICompletionProposal> getActionViewHelpers()
	{
		if (fgActionViewHelpers == null || fgActionViewHelpers.isEmpty())
		{
			// FIXME This is very slow on the first time!!!! (10-25 seconds on my rather fast dev machine)
			long start = System.currentTimeMillis();
			List<CachedProposal> proposals = new ArrayList<CachedProposal>();
			ISourceFolderRoot root = getActionPackSourceRoot();
			long actionPackRoot = System.currentTimeMillis();
			if (root == null)
				return new ArrayList<ICompletionProposal>();
			ISourceFolder helperFolder = root.getSourceFolder(new String[] { "action_view", HELPERS });
			long sourceFolder = System.currentTimeMillis();
			List<SearchMatch> matches = search(helperFolder, "*Helper.*", IRubyElement.METHOD, IRubySearchConstants.DECLARATIONS);
			long finishedSearch = System.currentTimeMillis();
			for (SearchMatch match : matches)
			{
				IRubyElement element = (IRubyElement) match.getElement();
				if (element instanceof IMethod)
				{
					IMethod method = (IMethod) element;
					try
					{
						if (method.isPublic())
						{
							proposals.add(new CachedProposal(element));
						}
					}
					catch (RubyModelException e)
					{
						ERBPlugin.getDefault().getLog().log(e.getStatus());
					}
				}
			}
			if (MEASURE_PERFORMANCE)
			{
				long end = System.currentTimeMillis();
				System.err.println("Action View Helper Methods (total): " + (end - start));
				System.err.println("Action View Helper Methods (grab root): " + (actionPackRoot - start));
				System.err.println("Action View Helper Methods (helper folder): " + (sourceFolder - actionPackRoot));
				System.err.println("Action View Helper Methods (search): " + (finishedSearch - sourceFolder));
			}
			fgActionViewHelpers = proposals;
		}
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (CachedProposal cached : fgActionViewHelpers)
		{
			ICompletionProposal proposal = cached.getProposal(fOffset, fPrefix, unifiedViewer);
			if (proposal.getDisplayString().startsWith(fPrefix))
				proposals.add(proposal);
		}
		return proposals;
	}

	private ISourceFolderRoot getActionPackSourceRoot()
	{
		try
		{
			ISourceFolderRoot[] roots = getRubyProject().getSourceFolderRoots();
			for (int i = 0; i < roots.length; i++)
			{
				ISourceFolderRoot root = roots[i];
				IPath path = root.getPath();
				if (path.segmentCount() < 2)
					continue;
				String gem = path.segment(path.segmentCount() - 2);
				if (gem.startsWith("actionpack"))
				{
					return root;
				}
			}
		}
		catch (RubyModelException e)
		{
			ERBPlugin.getDefault().getLog().log(e.getStatus());
		}
		return null;
	}

	private ICompletionProposal[] determineKeywordProposals(ITextViewer viewer, int documentOffset)
	{
		initKeywordProposals();
		List<String> completionProposals = Arrays.asList(keywordProposals);

		// FIXME Refactor to combine the copied code in
		// determineRubyElementProposals
		List<ICompletionProposal> possibleProposals = new ArrayList<ICompletionProposal>();
		for (int i = 0; i < completionProposals.size(); i++)
		{
			String proposal = (String) completionProposals.get(i);
			if (proposal.startsWith(fPrefix))
			{
				String message;
				if (isPredefinedGlobal(proposal))
				{
					message = "{0} " + getContext(proposal);
				}
				else
				{
					message = "The <b>{0}</b> keyword";
				}
				String popupInfo = MessageFormat.format(message, new Object[] { proposal });
				IContextInformation info = new ContextInformation(proposal, popupInfo);

				String replacementString = proposal.substring(fPrefix.length(), proposal.length());
				Image icon = fIconKeyword;
				String decription = "Keyword";
				if (proposal.startsWith("$"))
				{ // FIXME This is a bit of a hack here
					icon = labelProvider.getImage(new RubyGlobal(null, proposal));
					decription = "Global";
				}
				ICompletionProposal proposalObj = new ERBCompletionProposal(replacementString, documentOffset, 0,
						proposal.length() - fPrefix.length(), icon, proposal, info, popupInfo, 0, null, null,
						decription);

				possibleProposals.add(proposalObj);
			}
		}
		return possibleProposals.toArray(new ICompletionProposal[possibleProposals.size()]);
	}

	/**
	 * @param proposal
	 * @return - context
	 */
	private String getContext(String proposal)
	{
		for (int i = 0; i < preDefinedGlobals.length; i++)
		{
			if (proposal.equals(preDefinedGlobals[i]))
				return globalContexts[i];
		}
		return "";
	}

	/**
	 * @param proposal
	 * @return - true if predefined global
	 */
	private boolean isPredefinedGlobal(String proposal)
	{
		for (int i = 0; i < preDefinedGlobals.length; i++)
		{
			if (proposal.equals(preDefinedGlobals[i]))
				return true;
		}
		return false;
	}

	/**
	 * 
	 */
	private void initKeywordProposals()
	{
		if (keywordProposals == null)
		{
			String[] keywords = RubyTextTools.getKeyWords();
			keywordProposals = new String[keywords.length + preDefinedGlobals.length];
			System.arraycopy(keywords, 0, keywordProposals, 0, keywords.length);
			System.arraycopy(preDefinedGlobals, 0, keywordProposals, keywords.length, preDefinedGlobals.length);
		}
	}

	private IFile getViewFile()
	{
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		return workspaceRoot.getFile(getViewPath());
	}

	private List<ICompletionProposal> getHelperMethods(IRubyScript helper)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		if (helper == null)
			return proposals;
		List<SearchMatch> matches = search(helper, IRubyElement.METHOD, IRubySearchConstants.DECLARATIONS);
		for (SearchMatch match : matches)
		{
			IRubyElement element = (IRubyElement) match.getElement();
			ICompletionProposal proposal = createProposal(element);
			if (proposal != null)
				proposals.add(proposal);
		}
		return proposals;
	}

	private String getActionName()
	{
		String viewName = getViewPath().lastSegment();
		if (viewName.endsWith(".rhtml"))
		{
			return viewName.substring(0, viewName.length() - ".rhtml".length());
		}
		else if (viewName.endsWith(".html.erb"))
		{
			return viewName.substring(0, viewName.length() - ".html.erb".length());
		}
		RubyPlugin
				.log("uh oh, we have a view without an .rhtml or .html.erb ending! We'll just take up until first period.");
		return viewName.substring(0, viewName.indexOf('.'));
	}

	private List<ICompletionProposal> getControllerInstanceVariables(IType controller)
	{
		// FIXME What about layouts?!
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		if (controller == null || !controller.exists())
			return proposals;
		IMethod action = getMethod(controller, getActionName());
		List<SearchMatch> matches = search(action, IRubyElement.INSTANCE_VAR, IRubySearchConstants.ALL_OCCURRENCES);
		for (SearchMatch match : matches)
		{
			IRubyElement element = null;
			if (match instanceof FieldReferenceMatch)
			{
				FieldReferenceMatch fieldRef = (FieldReferenceMatch) match;
				element = fieldRef.getBinding();
			}
			else
			{
				if (match != null)
					element = (IRubyElement) match.getElement();
			}
			IRubyElement parent = null;
			if (element != null)
			{
				parent = element.getParent();
			}
			if (parent != null && parent.equals(controller))
			{
				ICompletionProposal proposal = createProposal(element);
				if (proposal != null)
					proposals.add(proposal);
			}
		}
		return proposals;
	}

	private List<SearchMatch> search(IRubyElement scopeElement, int searchType, int categories)
	{
		return search(scopeElement, "*", searchType, categories);
	}

	private List<SearchMatch> search(IRubyElement scopeElement, String string, int searchType, int categories)
	{
		if (scopeElement == null)
			return Collections.emptyList();
		IRubySearchScope scope = SearchEngine.createRubySearchScope(new IRubyElement[] { scopeElement });
		SearchEngine engine = new SearchEngine();
		SearchPattern pattern = SearchPattern.createPattern(searchType, string, categories,
				SearchPattern.R_PATTERN_MATCH);
		SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
		CollectingSearchRequestor requestor = new CollectingSearchRequestor();
		try
		{
			engine.search(pattern, participants, scope, requestor, new NullProgressMonitor());
		}
		catch (CoreException e)
		{
			RubyPlugin.log(e);
		}
		return requestor.getResults();
	}

	private ICompletionProposal createProposal(IRubyElement element)
	{
		String name = element.getElementName();
		if (!name.startsWith(fPrefix))
		{
			return null;
		}
		return new ERBCompletionProposal(name, fOffset - fPrefix.length(), fPrefix.length(), name.length(),
				labelProvider.getImage(element), name, null, RDocUtil.getHTMLDocumentation(element), element
						.getElementType(), unifiedViewer, null, element.getPath().toOSString());
	}

	private IMethod getMethod(IType type, String methodName)
	{
		try
		{
			IMethod[] methods = type.getMethods();
			for (int i = 0; i < methods.length; i++)
			{
				if (methods[i].getElementName().equals(methodName))
				{
					return methods[i];
				}
			}
		}
		catch (RubyModelException e)
		{
			RubyPlugin.log(e);
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getContextType(org.eclipse.jface.text.ITextViewer,
	 *      org.eclipse.jface.text.IRegion)
	 */
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region)
	{
		return RhtmlTemplateManager.getDefault().getContextTypeRegistry().getContextType(RhtmlContextType.ID);
	}

	/**
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getImage(org.eclipse.jface.text.templates.Template)
	 */
	protected Image getImage(Template template)
	{
		return fIconTemplate;
	}

	private String getFullPrefix(IDocument doc, int offset)
	{
		int length = 0;
		String prefix = "";
		try
		{
			while ((offset - length > 0) && Pattern.matches("[^\\n|^\\r|^;]", doc.get(offset - length - 1, 1)))
			{
				length++;
			}
			prefix = doc.get(offset - length, length);
		}
		catch (BadLocationException e)
		{
			// ignore
		}
		return prefix;
	}

	private String getPrefix(IDocument doc, int offset)
	{
		int length = 0;
		String prefix = "";
		try
		{
			while ((offset - length > 0) && Pattern.matches("[\\w|@|$|:]", doc.get(offset - length - 1, 1)))
			{
				length++;
			}
			prefix = doc.get(offset - length, length);
		}
		catch (BadLocationException e)
		{
			// ignore
		}
		return prefix;
	}

	private ICompletionProposal[] determineTemplateProposals(ITextViewer refViewer, int documentOffset)
	{
		if (fPrefix.length() == 0)
		{
			return super.computeCompletionProposals(refViewer, documentOffset);
		}
		else
		{
			ICompletionProposal[] templateProposals = super.computeCompletionProposals(refViewer, documentOffset);
			List<ICompletionProposal> templateProposalList = new ArrayList<ICompletionProposal>(
					templateProposals.length);
			for (int i = 0; i < templateProposals.length; i++)
			{
				if (templateProposals[i].getDisplayString().toLowerCase().startsWith(fPrefix))
				{
					templateProposalList.add(templateProposals[i]);
				}
			}
			return templateProposalList.toArray(new ICompletionProposal[templateProposalList.size()]);
		}
	}

	/**
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getTemplates(java.lang.String)
	 */
	protected Template[] getTemplates(String contextTypeId)
	{
		Template[] rhtml = RhtmlTemplateManager.getDefault().getTemplateStore().getTemplates();
		Template[] ruby = RubyTemplateAccess.getDefault().getTemplateStore().getTemplates();
		Template[] superSet = new Template[rhtml.length + ruby.length];
		System.arraycopy(rhtml, 0, superSet, 0, rhtml.length);
		System.arraycopy(ruby, 0, superSet, rhtml.length, ruby.length);
		return superSet;
	}

	private boolean isDebug()
	{
		return ERBPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.CONTENT_ASSIST_DEBUG);
	}
}