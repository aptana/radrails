package com.aptana.rdt.internal.profiling;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.search.CollectingSearchRequestor;
import org.rubypeople.rdt.core.search.IRubySearchConstants;
import org.rubypeople.rdt.core.search.IRubySearchScope;
import org.rubypeople.rdt.core.search.SearchEngine;
import org.rubypeople.rdt.core.search.SearchMatch;
import org.rubypeople.rdt.core.search.SearchParticipant;
import org.rubypeople.rdt.core.search.SearchPattern;
import org.rubypeople.rdt.internal.ui.actions.OpenActionUtil;

import com.aptana.rdt.profiling.ProfilingPlugin;

public class OpenMethodCallAction extends Action {

	private Object source;

	public OpenMethodCallAction(Tree tree) {
		super("Open Method");
		this.source = tree;
	}

	public OpenMethodCallAction(Table table) {
		super("Open Method");
		this.source = table;
	}
	
	@Override
	public boolean isEnabled() {
		Object data = getSelected();
		if (!(data instanceof MethodCall)) return false;
		MethodCall call = (MethodCall) data;
		return !call.getClassName().equals("Global");
	}

	@Override
	public void run() {
		Object data = getSelected();
		if (data instanceof MethodCall) {
			MethodCall call = (MethodCall) data;
			SearchEngine engine = new SearchEngine();
			SearchPattern pattern = SearchPattern.createPattern(call.getFullMethodName(), IRubySearchConstants.METHOD, IRubySearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
			SearchParticipant[] participants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
			IRubySearchScope scope = SearchEngine.createWorkspaceScope();
			CollectingSearchRequestor requestor = new CollectingSearchRequestor();					
			try {
				engine.search(pattern, participants, scope, requestor, null);
			} catch (CoreException e1) {
				ProfilingPlugin.log(e1);
			}
			List<SearchMatch> matches = requestor.getResults();
			for (SearchMatch match : matches) {
				IMethod element = (IMethod) match.getElement();					
				try {
					OpenActionUtil.open(element, true);
				} catch (PartInitException e1) {
					ProfilingPlugin.log(e1);
				} catch (RubyModelException e1) {
					ProfilingPlugin.log(e1);
				}						
			}
		}				
	}

	private Object getSelected() {
		if (source instanceof Table) {
			Table table = (Table) source;
			TableItem[] items = table.getSelection();
			if (items != null && items.length > 0) {
				return items[0].getData();
			}
		} else if (source instanceof Tree) {
			TreeItem[] selected = ((Tree)source).getSelection();
			if (selected != null && selected.length > 0) {
				return selected[0].getData();
			}
		}
		return null;
	}
}
