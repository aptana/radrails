package org.radrails.rails.internal.parser.warnings;

import org.jruby.ast.CallNode;
import org.radrails.rails.core.IDeprecationProblems;
import org.radrails.rails.internal.core.RailsPlugin;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

public class PushWithAttributes extends RubyLintVisitor {

	public PushWithAttributes() {
		super(RailsPlugin.getInstance().getOptions(), "");
	}

	@Override
	protected String getOptionKey() {
		return RailsPlugin.RAILS_DEPRECATION_PUSH_WITH_ATTRIBUTES;
	}
	
	@Override
	public Object visitCallNode(CallNode iVisited) {
		String name = iVisited.getName();
		if (name.equals("push_with_attributes")) {
			createProblem(iVisited.getPosition(), "Deprecated method call, use has_many :through for rich many-to-many associations");
		}
		return super.visitCallNode(iVisited);
	}

	@Override
	protected int getProblemID() {
		return IDeprecationProblems.DeprecatedPushWithAttributes;
	}
}
