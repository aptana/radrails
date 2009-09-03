package org.radrails.rails.internal.parser.warnings;

import org.jruby.ast.CallNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.ast.VCallNode;
import org.jruby.ast.types.INameNode;
import org.radrails.rails.core.IDeprecationProblems;
import org.radrails.rails.internal.core.RailsPlugin;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

public class DeprecatedActiveRecordFindMethods extends RubyLintVisitor {

	private static final String ACTIVE_RECORD_BASE = "ActiveRecord::Base";

	private static final String[] DEPRECATED = new String[] { "find_first",
			"find_all" };

	private static final String[] SOLUTIONS = new String[] { "find(:first)",
			"find(:all)" };

	private boolean inActiveRecord;

	public DeprecatedActiveRecordFindMethods() {
		super(RailsPlugin.getInstance().getOptions(), "");
	}
	
	@Override
	protected int getProblemID() {
		return IDeprecationProblems.DeprecatedARFindCalls;
	}

	@Override
	protected String getOptionKey() {
		return RailsPlugin.RAILS_DEPRECATION_ACTIVE_RECORD_FIND_METHODS;
	}
	
	@Override
	public Object visitRootNode(RootNode visited)
	{
		inActiveRecord = false;
		return super.visitRootNode(visited);
	}

	@Override
	public Object visitCallNode(CallNode iVisited) {
		String name = iVisited.getName();
		for (int i = 0; i < DEPRECATED.length; i++) {
			if (!name.equals(DEPRECATED[i]))
				continue;
			Node receiver = iVisited.getReceiverNode();
			if (receiver instanceof ConstNode) {
				createProblem(iVisited.getPosition(), "Deprecated method, use "
						+ SOLUTIONS[i] + " instead");
				break;
			}
		}
		return super.visitCallNode(iVisited);
	}
	
	@Override
	public Object visitClassNode(ClassNode iVisited) {
		Node superNode = iVisited.getSuperNode();
		String superName = ASTUtil.getSuperClassName(superNode);
		if (superName != null && superName.equals(ACTIVE_RECORD_BASE)) {
			inActiveRecord = true;
		}
		return super.visitClassNode(iVisited);
	}
	
	@Override
	public void exitClassNode(ClassNode iVisited) {
		inActiveRecord = false;
		super.exitClassNode(iVisited);
	}

	@Override
	public Object visitVCallNode(VCallNode iVisited) {
		checkForDeprecatedCalls(iVisited);
		return super.visitVCallNode(iVisited);
	}
	
	@Override
	public Object visitFCallNode(FCallNode iVisited) {
		checkForDeprecatedCalls(iVisited);
		return super.visitFCallNode(iVisited);
	}

	private void checkForDeprecatedCalls(INameNode iVisited) {
		if (!inActiveRecord) return;
		if (iVisited == null) return;
		String name = iVisited.getName();
		if (name == null || name.trim().length() == 0) return;
		for (int i = 0; i < DEPRECATED.length; i++) {
			if (!name.equals(DEPRECATED[i]))
				continue;
			createProblem(((Node)iVisited).getPosition(), "Deprecated method, use "	+ SOLUTIONS[i] + " instead");
			break;
		}
	}

}
