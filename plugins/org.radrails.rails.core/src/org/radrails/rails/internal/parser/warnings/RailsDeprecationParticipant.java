package org.radrails.rails.internal.parser.warnings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jruby.ast.Node;
import org.radrails.rails.core.RailsLog;
import org.radrails.rails.internal.core.RailsPlugin;
import org.rubypeople.rdt.core.IRubyModelMarker;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.compiler.BuildContext;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.compiler.CompilationParticipant;
import org.rubypeople.rdt.core.compiler.ReconcileContext;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.builder.RubyBuilder;

public class RailsDeprecationParticipant extends CompilationParticipant
{

	private Map<String, Long> timings;

	@Override
	public void reconcile(ReconcileContext context)
	{
		timings = new HashMap<String, Long>();
		try
		{
			List<RubyLintVisitor> visitors = createLintVisitors();
			List<CategorizedProblem> problems = parse(visitors, context.getAST());
			addProblems(context, IRubyModelMarker.RUBY_MODEL_PROBLEM_MARKER, problems);
		}
		catch (RubyModelException e)
		{
			RailsLog.log(e);
		}
	}

	private List<CategorizedProblem> parse(List<RubyLintVisitor> visitors, Node ast)
	{
		if (ast == null)
		{
			return Collections.emptyList();
		}
		List<CategorizedProblem> problems = new ArrayList<CategorizedProblem>();
		for (RubyLintVisitor visitor : visitors)
		{
			long start = System.currentTimeMillis();
			ast.accept(visitor);
			if (RubyBuilder.DEBUG)
			{
				addTiming(visitor.getClass().getSimpleName(), System.currentTimeMillis() - start);
			}
			problems.addAll(visitor.getProblems());
		}
		return problems;
	}

	private void addTiming(String simpleName, long length)
	{
		Long existingValue = timings.get(simpleName);
		if (existingValue == null)
			existingValue = 0L;
		timings.put(simpleName, existingValue + length);
	}

	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch, IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, files.length);
		timings = new HashMap<String, Long>();
		List<RubyLintVisitor> visitors = createLintVisitors();
		if (visitors.isEmpty())
		{
			sub.done();
			return;
		}
		for (BuildContext context : files)
		{
			sub
					.subTask("Looking for deprecated rails methods in "
							+ context.getFile().getLocation().toPortableString());
			List<CategorizedProblem> problems = parse(visitors, context.getAST());
			context.recordNewProblems(problems.toArray(new CategorizedProblem[problems.size()]));
			sub.worked(1);
		}
		if (RubyBuilder.DEBUG)
		{
			for (Map.Entry<String, Long> timing : timings.entrySet())
			{
				System.out.println(timing.getKey() + " took " + timing.getValue() + "ms");
			}
			timings.clear();
		}
		sub.done();
	}

	@Override
	public boolean isActive(IRubyProject project)
	{
		return RailsPlugin.hasRailsNature(project.getProject());
	}

	private List<RubyLintVisitor> createLintVisitors()
	{
		List<RubyLintVisitor> visitors = new ArrayList<RubyLintVisitor>();
		visitors.add(new InternalInstanceVariableReference());
		visitors.add(new DeprecatedRenderCalls());
		visitors.add(new DeprecatedRedirectCalls());
		visitors.add(new DeprecatedPostFormatMethods());
		visitors.add(new DeprecatedStartAndEndFormTag());
		visitors.add(new DeprecatedUpdateElementFunction());
		visitors.add(new DeprecatedImageLinkMethods());
		visitors.add(new HumanSizeHelperAlias());
		visitors.add(new DeprecatedActiveRecordFindMethods());
		visitors.add(new PushWithAttributes());
		List<RubyLintVisitor> filtered = new ArrayList<RubyLintVisitor>();
		for (RubyLintVisitor visitor : visitors)
		{
			if (visitor.isIgnored())
				continue;
			filtered.add(visitor);
		}
		return filtered;
	}
}
