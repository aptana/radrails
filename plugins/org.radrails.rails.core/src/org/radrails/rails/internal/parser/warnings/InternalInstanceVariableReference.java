package org.radrails.rails.internal.parser.warnings;

import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon3Node;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.RootNode;
import org.radrails.rails.core.IDeprecationProblems;
import org.radrails.rails.internal.core.RailsPlugin;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

public class InternalInstanceVariableReference extends RubyLintVisitor {

	private boolean inController = false;
	private final static String[] DEPRECATED = new String[] { "@params", "@session", "@flash", "@request", "@cookies", "@headers", "@response"};

	public InternalInstanceVariableReference() {
		super(RailsPlugin.getInstance().getOptions(), "");
	}
	
	@Override
	public Object visitRootNode(RootNode visited)
	{
		inController = false;
		return super.visitRootNode(visited);
	}
	
	@Override
	protected int getProblemID() {
		return IDeprecationProblems.DeprecatedInstanceVariableReferences;
	}

	@Override
	protected String getOptionKey() {
		return RailsPlugin.RAILS_DEPRECATION_INSTANCE_VARIABLES;
	}

	@Override
	public Object visitClassNode(ClassNode iVisited) {
		Colon3Node node = iVisited.getCPath();
		String name = node.getName();
		if (name.endsWith("Controller")) {
			inController = true;
		}
		return super.visitClassNode(iVisited);
	}
	
	@Override
	public void exitClassNode(ClassNode iVisited) {
		inController = false;
		super.exitClassNode(iVisited);
	}

	@Override
	public Object visitInstVarNode(InstVarNode iVisited) {
		if (inController) {
			String name = iVisited.getName();
			for (int i = 0; i < DEPRECATED.length; i++) {
				if (!name.equals(DEPRECATED[i])) continue;
				createProblem(iVisited.getPosition(),
					"Rails Deprecation notice: change references from " + DEPRECATED[i] + " to " + DEPRECATED[i].substring(1));
				break;
			}
		}
		return super.visitInstVarNode(iVisited);
	}

}
