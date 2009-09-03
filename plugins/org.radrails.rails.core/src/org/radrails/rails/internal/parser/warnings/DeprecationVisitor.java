package org.radrails.rails.internal.parser.warnings;

import org.jruby.ast.FCallNode;
import org.jruby.ast.VCallNode;
import org.radrails.rails.internal.core.RailsPlugin;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

public abstract class DeprecationVisitor extends RubyLintVisitor {

	private String[] deprecated;
	private String[] solutions;

	public DeprecationVisitor(String[] deprecated, String solution) {
		super(RailsPlugin.getInstance().getOptions(), "");
		this.deprecated = deprecated;
		this.solutions = new String[deprecated.length];
		for (int i = 0; i < solutions.length; i++)
			solutions[i] = solution;
	}
	
	public DeprecationVisitor(String[] deprecated, String[] solutions) {
		super(RailsPlugin.getInstance().getOptions(), "");
		this.deprecated = deprecated;
		this.solutions = solutions;
	}
	
	@Override
	public Object visitFCallNode(FCallNode iVisited) {
		String name = iVisited.getName();
		for (int i = 0; i < deprecated.length; i++) {
			if (!name.equals(deprecated[i])) continue;
			createProblem(iVisited.getPosition(), solutions[i]);
			break;
		}
		return super.visitFCallNode(iVisited);
	}
	
	@Override
	public Object visitVCallNode(VCallNode iVisited) {
		String name = iVisited.getName();
		for (int i = 0; i < deprecated.length; i++) {
			if (!name.equals(deprecated[i])) continue;
			createProblem(iVisited.getPosition(), solutions[i]);
			break;
		}
		return super.visitVCallNode(iVisited);
	}
}
