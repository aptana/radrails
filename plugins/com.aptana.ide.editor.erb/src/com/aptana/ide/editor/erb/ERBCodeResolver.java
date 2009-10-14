package com.aptana.ide.editor.erb;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.VCallNode;
import org.jruby.ast.types.INameNode;
import org.radrails.rails.core.RailsConventions;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.ISourceFolder;
import org.rubypeople.rdt.core.ISourceFolderRoot;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.codeassist.ResolveContext;
import org.rubypeople.rdt.core.search.FieldReferenceMatch;
import org.rubypeople.rdt.core.search.IRubySearchConstants;
import org.rubypeople.rdt.core.search.SearchEngine;
import org.rubypeople.rdt.core.search.SearchMatch;
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
		this.fContext = context;
		super.select(context);
	}

	@Override
	protected void resolveInstanceVar(ResolveContext context, Node selected) throws RubyModelException
	{
		// Handle resolving instance vars back to the declaration in matching controller/action
		IType relatedController = getController();
		if (relatedController == null)
			return;
		getControllerInstanceVariables(relatedController);
	}

	@Override
	protected void resolveMethodCall(ResolveContext context, Node selected) throws RubyModelException
	{
		if ((selected instanceof FCallNode) || (selected instanceof VCallNode))
		{
			INameNode nameNode = (INameNode) selected;
			String methodName = nameNode.getName();
			List<IRubyElement> scopes = new ArrayList<IRubyElement>();
			IRubyScript helper = getRelatedHelper();
			if (helper != null)
			{
				scopes.add(helper);
			}
			try
			{
				ISourceFolderRoot root = getActionPackSourceRoot();
				if (root != null)
				{
					ISourceFolder helperFolder = root.getSourceFolder(new String[] { "action_view", "helpers" });
					List<SearchMatch> matches = search(SearchEngine
							.createRubySearchScope(new IRubyElement[] { helperFolder }), IRubyElement.TYPE, "*Helper",
							IRubySearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
					for (SearchMatch match : matches)
					{
						IType actionViewHelper = (IType) match.getElement();
						scopes.add(actionViewHelper);
					}
				}

				List<SearchMatch> matches = search(SearchEngine.createRubySearchScope(scopes
						.toArray(new IRubyElement[0])), IRubyElement.METHOD, methodName,
						IRubySearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
				for (SearchMatch match : matches)
				{
					IMethod method = (IMethod) match.getElement();
					putResolved(context, new IRubyElement[] { method });
					return;
				}
			}
			catch (CoreException e)
			{
				// ignore
			}
		}
		super.resolveMethodCall(context, selected);
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

	private IRubyProject getRubyProject()
	{
		return getScript().getRubyProject();
	}

	private IRubyScript getRelatedHelper()
	{
		IFile helperFile = RailsConventions.getHelperFromView(getFile());
		if (helperFile == null)
			return null;
		return RubyCore.create(helperFile);
	}

	private IRubyScript getScript()
	{
		return fContext.getScript();
	}

	private IFile getFile()
	{
		IRubyScript script = getScript();
		if (script == null)
			return null;
		IResource resource = script.getResource();
		if (resource == null)
			return null;
		if (!(resource instanceof IFile))
			return null;
		return (IFile) resource;
	}

	private IType getController()
	{
		return RailsConventions.getControllerTypeFromViewFile(getFile());
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
		try
		{
			List<SearchMatch> matches = search(SearchEngine.createRubySearchScope(new IRubyElement[] { scope }),
					IRubyElement.INSTANCE_VAR, "*", IRubySearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
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
		}
		catch (CoreException e)
		{
			// ignore
		}
		putResolved(fContext, resolved.toArray(new IRubyElement[resolved.size()]));
	}
}
