package org.radrails.rails.internal.ui.actions.mvc;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.PartInitException;
import org.radrails.rails.core.RailsConventions;
import org.radrails.rails.ui.RailsUILog;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.search.CollectingSearchRequestor;
import org.rubypeople.rdt.core.search.IRubySearchConstants;
import org.rubypeople.rdt.core.search.IRubySearchScope;
import org.rubypeople.rdt.core.search.SearchEngine;
import org.rubypeople.rdt.core.search.SearchMatch;
import org.rubypeople.rdt.core.search.SearchParticipant;
import org.rubypeople.rdt.core.search.SearchPattern;
import org.rubypeople.rdt.internal.ui.actions.OpenActionUtil;

public class ControllerEditorActionDelegate extends MVCEditorActionDelegate implements IEditorActionDelegate
{

	public void run(IAction action)
	{
		IFile currentFile = getCurrentFile();
		if (RailsConventions.looksLikeController(currentFile))
			return;
		IFile controllerFile = RailsConventions.getControllerFromModel(currentFile);
		if (controllerFile == null)
			controllerFile = RailsConventions.getControllerFromView(currentFile);
		if (controllerFile == null)
			controllerFile = RailsConventions.getControllerFromHelper(currentFile);
		if (controllerFile == null)
			controllerFile = RailsConventions.getControllerFromFunctionalTest(currentFile);
		if (controllerFile == null)
			controllerFile = RailsConventions.getControllerFromUnitTest(currentFile);
		if (controllerFile == null)
			return;

		IRubyElement element = RubyCore.createRubyScriptFrom(controllerFile);
		if (RailsConventions.looksLikeView(currentFile))
		{
			try
			{
				String view = currentFile.getProjectRelativePath().lastSegment();
				int index = view.indexOf('.');
				if (index == -1)
				{
					index = view.length();
				}
				view = view.substring(0, index);
				SearchEngine engine = new SearchEngine();
				CollectingSearchRequestor requestor = new CollectingSearchRequestor();
				SearchPattern pattern = SearchPattern.createPattern(IRubyElement.METHOD, view,
						IRubySearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
				SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
				IRubySearchScope scope = SearchEngine.createRubySearchScope(new IRubyElement[] { element });
				engine.search(pattern, participants, scope, requestor, new NullProgressMonitor());
				List<SearchMatch> matches = requestor.getResults();
				if (matches != null && !matches.isEmpty())
				{
					element = (IRubyElement) matches.get(0).getElement();
				}
			}
			catch (CoreException e)
			{
				RailsUILog.logError("Error grabbing view method from controller", e);
			}
		}
		try
		{
			OpenActionUtil.open(element, true);
		}
		catch (PartInitException e)
		{
			RailsUILog.logError("Error creating editor", e);
		}
		catch (RubyModelException e)
		{
			RailsUILog.logError("Error creating editor", e);
		}
	}

	@Override
	protected boolean isEnabled()
	{
		return RailsConventions.looksLikeView(getCurrentFile()) || RailsConventions.looksLikeHelper(getCurrentFile())
				|| RailsConventions.looksLikeModel(getCurrentFile())
				|| RailsConventions.looksLikeTest(getCurrentFile());
	}
}
