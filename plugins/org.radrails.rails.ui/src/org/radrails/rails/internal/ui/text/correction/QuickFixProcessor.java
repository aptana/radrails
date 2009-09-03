package org.radrails.rails.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.radrails.rails.core.IDeprecationProblems;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.ui.text.ruby.IInvocationContext;
import org.rubypeople.rdt.ui.text.ruby.IProblemLocation;
import org.rubypeople.rdt.ui.text.ruby.IQuickFixProcessor;
import org.rubypeople.rdt.ui.text.ruby.IRubyCompletionProposal;

public class QuickFixProcessor implements IQuickFixProcessor {

	public IRubyCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		if (locations == null || locations.length == 0) {
			return null;
		}

		HashSet<Integer> handledProblems = new HashSet<Integer>(locations.length);
		ArrayList<IRubyCompletionProposal> resultingCollections = new ArrayList<IRubyCompletionProposal>();
		for (int i = 0; i < locations.length; i++) {
			IProblemLocation curr = locations[i];
			Integer id = new Integer(curr.getProblemId());
			if (handledProblems.add(id)) {
				process(context, curr, resultingCollections);
			}
		}
		return (IRubyCompletionProposal[]) resultingCollections.toArray(new IRubyCompletionProposal[resultingCollections.size()]);
	}

	private void process(IInvocationContext context, IProblemLocation problem, Collection<IRubyCompletionProposal> proposals) throws CoreException {
		int id = problem.getProblemId();
		if (id == 0) { // no proposals for none-problem locations
			return;
		}
		switch (id) {
		case IDeprecationProblems.DeprecatedInstanceVariableReferences:
			IRubyScript script = context.getRubyScript();
			String src = script.getSource();
			String name = src.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
			String fixed = name.substring(1);
			LocalCorrectionsSubProcessor.addReplacementProposal(fixed, "Convert from " + name + " to " + fixed, problem, proposals);
			break;
		case IDeprecationProblems.DeprecatedHumanSizeAlias:
			LocalCorrectionsSubProcessor.addReplacementProposal(problem.getOffset(), "human_size".length(), "number_to_human_size", "Convert from human_size to number_to_human_size", proposals);
			break;
		case IDeprecationProblems.DeprecatedRenderCalls:
			fixOldRenderCall(context, problem, proposals);
			break;
		default:
			break;
		}
	}

	private void fixOldRenderCall(IInvocationContext context, IProblemLocation problem, Collection<IRubyCompletionProposal> proposals) throws RubyModelException {
		IRubyScript script = context.getRubyScript();
		String src = script.getSource();
		String name = src.substring(problem.getOffset(), problem.getOffset() + problem.getLength()).trim();
		int index = name.indexOf(" ");
		if (index == -1) {
			index = name.indexOf("(");
		} 
		if (index != -1) {
			name = name.substring(0, index);
		}
		// FIXME USe problem args to help with this?
		String[] possible = new String[] {"render_text", "render_file", "render_template", "render_partial", "render_action"};
		for (int i = 0; i < possible.length; i++) {
			if (name.equals(possible[i])) {
				String[] parts = name.split("_");
				if (parts == null || parts.length < 2) continue;
				String fixed = "render :" + parts[1] + " =>";
				LocalCorrectionsSubProcessor.addReplacementProposal(problem.getOffset(), possible[i].length(), fixed, "Convert from " + possible[i] + " to " + fixed, proposals);
				return;
			}
		}
	}

	public boolean hasCorrections(IRubyScript unit, int problemId) {
		switch (problemId) {
		case IDeprecationProblems.DeprecatedInstanceVariableReferences:
		case IDeprecationProblems.DeprecatedHumanSizeAlias:
		case IDeprecationProblems.DeprecatedRenderCalls:
			return true;
		default:
			return false;
		}
	}
}
