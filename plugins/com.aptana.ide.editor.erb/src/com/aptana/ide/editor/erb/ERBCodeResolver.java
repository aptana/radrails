package com.aptana.ide.editor.erb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jruby.ast.Node;
import org.radrails.rails.core.RailsConventions;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.codeassist.ResolveContext;
import org.rubypeople.rdt.core.search.CollectingSearchRequestor;
import org.rubypeople.rdt.core.search.FieldReferenceMatch;
import org.rubypeople.rdt.core.search.IRubySearchConstants;
import org.rubypeople.rdt.core.search.IRubySearchScope;
import org.rubypeople.rdt.core.search.SearchEngine;
import org.rubypeople.rdt.core.search.SearchMatch;
import org.rubypeople.rdt.core.search.SearchParticipant;
import org.rubypeople.rdt.core.search.SearchPattern;
import org.rubypeople.rdt.internal.codeassist.RubyCodeResolver;
import org.rubypeople.rdt.internal.core.ERBScript;
import org.rubypeople.rdt.internal.ui.RubyPlugin;

public class ERBCodeResolver extends RubyCodeResolver
{

	private ResolveContext fContext;

	@Override
	public void select(ResolveContext context) throws RubyModelException
	{
		IRubyScript script = context.getScript();
		if (!(script instanceof ERBScript))
			return;

		super.select(context);
	}

	@Override
	protected void resolveInstanceVar(ResolveContext context, Node selected) throws RubyModelException
	{
		this.fContext = context;

		// Handle resolving instance vars back to the declaration in matching controller/action
		IType relatedController = getController(getScript());
		if (relatedController == null)
			return;
		getControllerInstanceVariables(relatedController);
	}
	
	@Override
	protected List<IType> resolveImplicitReceiver(IRubyScript script, Node root, int start)
	{
		// TODO Look through the helpers!
		return super.resolveImplicitReceiver(script, root, start);
	}

	private IRubyScript getScript()
	{
		return fContext.getScript();
	}

	private IType getController(IRubyScript script)
	{
		IResource resource = script.getResource();
		if (resource == null)
			return null;
		if (!(resource instanceof IFile))
			return null;
		return RailsConventions.getControllerTypeFromViewFile((IFile) resource);
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

	private IPath getViewPath()
	{
		return getScript().getPath();
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

	private void getControllerInstanceVariables(IType controller)
	{
		List<IRubyElement> resolved = new ArrayList<IRubyElement>();
		if (controller == null || !controller.exists())
			return;
		IMethod action = getMethod(controller, getActionName());
		IRubyElement scope = controller;
		if (action != null)
		{
			scope = action;
		}
		List<SearchMatch> matches = search(scope, IRubyElement.INSTANCE_VAR, IRubySearchConstants.DECLARATIONS);
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
				resolved.add(element);
			}
		}
		fContext.putResolved(resolved.toArray(new IRubyElement[resolved.size()]));
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
}
