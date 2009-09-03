package org.radrails.rails.internal.core;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.SymbolNode;
import org.radrails.rails.core.Inflector;
import org.radrails.rails.core.RailsLog;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.codeassist.CodeResolver;
import org.rubypeople.rdt.core.codeassist.ResolveContext;
import org.rubypeople.rdt.core.search.CollectingSearchRequestor;
import org.rubypeople.rdt.core.search.IRubySearchConstants;
import org.rubypeople.rdt.core.search.IRubySearchScope;
import org.rubypeople.rdt.core.search.SearchEngine;
import org.rubypeople.rdt.core.search.SearchMatch;
import org.rubypeople.rdt.core.search.SearchParticipant;
import org.rubypeople.rdt.core.search.SearchPattern;
import org.rubypeople.rdt.core.util.Util;
import org.rubypeople.rdt.internal.ti.util.FirstPrecursorNodeLocator;
import org.rubypeople.rdt.internal.ti.util.INodeAcceptor;

public class RailsCodeResolver extends CodeResolver {
	
	private static final String HAS_AND_BELONGS_TO_MANY = "has_and_belongs_to_many";
	private static final String HAS_ONE = "has_one";
	private static final String HAS_MANY = "has_many";
	private static final String BELONGS_TO = "belongs_to";

	@Override
	public void select(ResolveContext context) throws RubyModelException {
		if (tryResolvingAssociation(context)) return;		
		narrowResolvedForMigrations(context);
	}

	private void narrowResolvedForMigrations(ResolveContext context) throws RubyModelException {
		IRubyElement[] resolved = context.getResolved();
		if (resolved == null || resolved.length <= 1) return;
		IType primary = context.getScript().findPrimaryType();
		if (primary == null) return;
		
		String superclass = primary.getSuperclassName();
		if (!superclass.equals("ActiveRecord::Migration")) return;
		
		String name = resolved[0].getElementName();
		if (!name.equals("create_table")) return;
		
		// filter to ActiveRecord::ConnectionsAdapters::SchemaStatements.create_table
		for (int i = 0; i < resolved.length; i++) {
			String typeName = ((IMethod) resolved[i]).getDeclaringType().getFullyQualifiedName();
			if (typeName.equals("ActiveRecord::ConnectionAdapters::SchemaStatements")) {
				context.putResolved(new IRubyElement[] {resolved[i]});
				return;
			}
		}
	}

	private boolean tryResolvingAssociation(ResolveContext context) throws RubyModelException {
		Node selected = context.getSelectedNode();
		if (!(selected instanceof SymbolNode)) return false;
		SymbolNode sym = (SymbolNode) selected;
		String symbolName = sym.getName();
		Node methodCall = FirstPrecursorNodeLocator.Instance().findFirstPrecursor(context.getAST(), selected.getPosition().getStartOffset(), new INodeAcceptor() {
		
			public boolean doesAccept(Node node) {
				return node instanceof FCallNode;
			}
		
		});
		if (methodCall == null) return false;
		FCallNode fCall = (FCallNode) methodCall;
		String methodName = fCall.getName();
		String modelName;
		if (methodName.equals(HAS_MANY) || methodName.equals(HAS_AND_BELONGS_TO_MANY)) {
			modelName = Inflector.singularize(symbolName);
			modelName = Util.underscoresToCamelCase(modelName);					
		} else if (methodName.equals(BELONGS_TO) || methodName.equals(HAS_ONE)) {
			modelName = Util.underscoresToCamelCase(symbolName);		
		} else {
			return false;
		}
		try {
			SearchEngine engine = new SearchEngine();
			SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
			CollectingSearchRequestor requestor = new CollectingSearchRequestor();
			SearchPattern pattern = SearchPattern.createPattern(IRubyElement.TYPE, modelName, IRubySearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
			IRubySearchScope scope = SearchEngine.createRubySearchScope(new IRubyElement[] {context.getScript().getRubyProject()});
			engine.search(pattern, participants, scope, requestor, null);
			List<SearchMatch> matches = requestor.getResults();
			for (SearchMatch searchMatch : matches) {
				IRubyElement element = (IRubyElement) searchMatch.getElement();
				context.putResolved(new IRubyElement[] {element});
				return true;
			}
		} catch (CoreException e) {
			RailsLog.log(e);
		}
		return false;
	}
}
