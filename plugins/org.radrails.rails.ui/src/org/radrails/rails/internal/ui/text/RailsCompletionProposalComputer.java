package org.radrails.rails.internal.ui.text;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.jruby.ast.RootNode;
import org.jruby.lexer.yacc.SyntaxException;
import org.radrails.rails.core.Inflector;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.text.RailsHeuristicCompletionComputer;
import org.rubypeople.rdt.core.CompletionProposal;
import org.rubypeople.rdt.core.Flags;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.search.CollectingSearchRequestor;
import org.rubypeople.rdt.core.search.IRubySearchConstants;
import org.rubypeople.rdt.core.search.IRubySearchScope;
import org.rubypeople.rdt.core.search.SearchEngine;
import org.rubypeople.rdt.core.search.SearchMatch;
import org.rubypeople.rdt.core.search.SearchParticipant;
import org.rubypeople.rdt.core.search.SearchPattern;
import org.rubypeople.rdt.internal.codeassist.CompletionContext;
import org.rubypeople.rdt.internal.codeassist.RubyElementRequestor;
import org.rubypeople.rdt.internal.core.LogicalType;
import org.rubypeople.rdt.internal.core.RubyScript;
import org.rubypeople.rdt.internal.core.parser.RubyParser;
import org.rubypeople.rdt.internal.ti.ITypeGuess;
import org.rubypeople.rdt.internal.ti.ITypeInferrer;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.rubyeditor.ASTProvider;
import org.rubypeople.rdt.internal.ui.text.ruby.RubyCompletionProposal;
import org.rubypeople.rdt.internal.ui.text.ruby.RubyContentAssistInvocationContext;
import org.rubypeople.rdt.ui.text.ruby.CompletionProposalCollector;
import org.rubypeople.rdt.ui.text.ruby.ContentAssistInvocationContext;
import org.rubypeople.rdt.ui.text.ruby.IRubyCompletionProposal;
import org.rubypeople.rdt.ui.text.ruby.IRubyCompletionProposalComputer;

public class RailsCompletionProposalComputer implements IRubyCompletionProposalComputer
{

	private ContentAssistInvocationContext fContext;

	private static final String[] TableNameFirstArgs = new String[] { "rename_column", "drop_table", "remove_column",
			"remove_index", "add_column", "add_index", "change_column", "create_table" };

	private static final String[] ColumnNameSecondArgs = new String[] { "rename_column", "remove_column", "add_index",
			"change_column" };

	public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor)
	{
		this.fContext = context;
		List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();

		// Completions for :controller, or :action
		Map<String, File> completions = RailsHeuristicCompletionComputer.getControllerCompletions(
				getControllersFolder(context), context.getDocument(), context.getInvocationOffset());
		completions.putAll(RailsHeuristicCompletionComputer.getActionCompletions(getControllersFolder(context), context
				.getDocument(), context.getInvocationOffset()));
		for (String replacement : completions.keySet())
		{
			ICompletionProposal prop = new RubyCompletionProposal(replacement, context.getInvocationOffset(), 0, null,
					replacement, 10000);
			list.add(prop);
		}

		if (context instanceof RubyContentAssistInvocationContext)
		{
			RubyContentAssistInvocationContext rubyContext = (RubyContentAssistInvocationContext) context;
			IType type = getInferredActiveRecord(rubyContext);
			if (type != null)
			{
				list.addAll(addActiveRecordFieldMethods(type, rubyContext)); // Completions
				// for
				// db
				// fields
				// /
				// finders
				// on
				// ActiveRecord
				// models
				list.addAll(addActiveRecordAssociations(type, rubyContext)); // Completions
				// for
				// associations
				// of
				// model
			}
			list.addAll(addMigrationMethods(rubyContext)); // Completions for
			// methods available
			// in migrations
			list.addAll(addMigrationMethodArgumentSuggestions(rubyContext));
		}
		fContext = null;
		return list;
	}

	private String getArgumentsToMethodCall()
	{
		String prefix = getStatementPrefix();
		String methodCall = getMethodName();
		String args = prefix.trim().substring(methodCall.length());
		if (args.startsWith("("))
			args = args.substring(1);
		return args;
	}

	private String getMethodName()
	{
		String prefix = getStatementPrefix();
		String methodCall = prefix.trim();
		int space = methodCall.indexOf(" ");
		if (space != -1)
		{
			methodCall = methodCall.substring(0, space);
		}
		space = methodCall.indexOf("(");
		if (space != -1)
		{
			methodCall = methodCall.substring(0, space);
		}
		return methodCall;
	}

	private Collection<? extends ICompletionProposal> addMigrationMethodArgumentSuggestions(
			RubyContentAssistInvocationContext context)
	{
		IRubyScript script = getScript(context);
		if (!isDBMigration(script))
			return Collections.emptyList();

		CompletionProposalCollector completion = createCollector(context);

		String methodName = getMethodName();
		String args = getArgumentsToMethodCall();
		int argumentIndex = calculateArgIndex(args);
		if (contains(methodName, TableNameFirstArgs))
		{
			// offer up the table names for first arg
			if (argumentIndex == 0)
			{
				Collection<String> tableNames = getDBTableNames(script);
				for (String tableName : tableNames)
				{
					if (!tableName.startsWith(":"))
						tableName = ":" + tableName;
					if (tableName.equals(":table_name"))
						continue;
					CompletionProposal proposal = new CompletionProposal(CompletionProposal.KEYWORD, tableName, 201);
					proposal.setName(tableName);
					int start = context.getInvocationOffset();
					proposal.setReplaceRange(start, start + tableName.length());
					completion.accept(proposal);
				}
			}
		}
		if (contains(methodName, ColumnNameSecondArgs))
		{
			if (argumentIndex == 1)
			{ // suggest field names!
				String tableName = getArgAt(0, args).trim().substring(1); // drop
				// the
				// ":"
				Collection<String> fieldNames = getDBFieldNames(script, Inflector.singularize(tableName));
				for (String fieldName : fieldNames)
				{
					fieldName = "'" + fieldName + "'";
					CompletionProposal proposal = new CompletionProposal(CompletionProposal.KEYWORD, fieldName, 201);
					proposal.setName(fieldName);
					int start = context.getInvocationOffset();
					proposal.setReplaceRange(start, start + fieldName.length());
					completion.accept(proposal);
				}
			}
		}
		return Arrays.asList(completion.getRubyCompletionProposals());
	}

	private boolean contains(String methodName, String[] array)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i].equals(methodName))
				return true;
		}
		return false;
	}

	private String getArgAt(int i, String argsRaw)
	{
		String[] args = argsRaw.split(",");
		return args[i];
	}

	private int calculateArgIndex(String prefix)
	{
		// TODO Auto-generated method stub
		String[] args = prefix.split(",");
		if (args.length == 1)
		{
			if (prefix.indexOf(",") == -1)
				return 0;
			return 1;
		}
		return args.length;
	}

	private Collection<? extends ICompletionProposal> addActiveRecordAssociations(IType type,
			RubyContentAssistInvocationContext context)
	{
		if (type == null)
			return Collections.emptyList();
		IRubyScript script = type.getRubyScript();
		RootNode ast = ASTProvider.getASTProvider().getAST(script, ASTProvider.WAIT_YES, new NullProgressMonitor());
		if (ast == null)
			return Collections.emptyList();
		ActiveRecordAssociationsVisitor visitor = new ActiveRecordAssociationsVisitor();
		ast.accept(visitor);
		List<IMethod> fields = visitor.getMethods();
		CompletionProposalCollector collector = createCollector(context);
		for (IMethod method : fields)
		{
			collector.accept(createProposal(context, type.getElementName(), method));
		}
		return Arrays.asList(collector.getRubyCompletionProposals());
	}

	protected CompletionProposalCollector createCollector(RubyContentAssistInvocationContext context)
	{
		return new CompletionProposalCollector(context);
	}

	private Collection<? extends ICompletionProposal> addMigrationMethods(RubyContentAssistInvocationContext context)
	{
		if (getStatementPrefix().trim().length() > 0)
			return Collections.emptyList();
		IRubyScript script = getScript(context);
		if (!isDBMigration(script))
			return Collections.emptyList();

		CompletionProposalCollector completion = createCollector(context);
		String typeName = "ActiveRecord::ConnectionAdapters::SchemaStatements";
		List<IType> types = findTypeDeclarations(typeName, script);
		try
		{
			for (IType type : types)
			{
				IMethod[] methods = type.getMethods();
				for (int i = 0; i < methods.length; i++)
				{
					IMethod method = methods[i];
					if (method == null || !method.isPublic())
						continue;
					completion.accept(createProposal(context, typeName, method));
				}
			}

		}
		catch (CoreException e)
		{
			RailsUILog.log(e);
		}
		return Arrays.asList(completion.getRubyCompletionProposals());
	}

	private boolean isDBMigration(IRubyScript script)
	{
		if (script == null)
			return false;
		IPath path = script.getPath();
		return getMigrationPath(script).isPrefixOf(path);
	}

	private IPath getMigrationPath(IRubyScript script)
	{
		if (script == null)
			return null;
		IPath railsRoot = RailsPlugin.findRailsRoot(script.getRubyProject().getProject());
		return script.getRubyProject().getPath().append(railsRoot).append("db").append("migrate");
	}

	private List<IType> findTypeDeclarations(String typeName, IRubyScript script)
	{
		List<IType> types = new ArrayList<IType>();
		try
		{
			SearchEngine engine = new SearchEngine();
			SearchPattern pattern = SearchPattern.createPattern(IRubyElement.TYPE, typeName,
					IRubySearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
			SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
			IRubySearchScope scope = SearchEngine.createRubySearchScope(new IRubyElement[] { script.getRubyProject() });
			CollectingSearchRequestor requestor = new CollectingSearchRequestor();
			engine.search(pattern, participants, scope, requestor, new NullProgressMonitor());
			List<SearchMatch> matches = requestor.getResults();
			for (SearchMatch match : matches)
			{
				IType type = (IType) match.getElement();
				types.add(type);
			}
		}
		catch (CoreException e)
		{
			RailsUILog.log(e);
		}
		return types;
	}

	/**
	 * Create a method completion proposal.
	 * 
	 * @param context
	 * @param typeName
	 * @param method
	 * @return
	 */
	private CompletionProposal createProposal(ContentAssistInvocationContext context, String typeName, IMethod method)
	{
		String methodName = method.getElementName();
		CharSequence prefix = "";
		try
		{
			prefix = context.computeIdentifierPrefix();
		}
		catch (BadLocationException e)
		{
			RailsUILog.log(e);
		}

		if (!methodName.startsWith(prefix.toString()))
			return null;
		CompletionProposal proposal = new CompletionProposal(CompletionProposal.METHOD_REF, methodName, 101); // 101
		// relevance
		// to push
		// it over
		// the
		// standard
		// completions
		proposal.setDeclaringType(typeName);
		proposal.setElement(method);
		proposal.setName(methodName);
		int flags = Flags.AccPublic;
		if (method.isSingleton())
			flags = flags | Flags.AccStatic;
		proposal.setFlags(flags);

		int start = context.getInvocationOffset() - prefix.length();
		proposal.setReplaceRange(start, start + methodName.length());

		return proposal;
	}

	/**
	 * Grab all ITypes inferred to be our receiver
	 * 
	 * @param context
	 * @return
	 */
	private List<IType> inferType(RubyContentAssistInvocationContext context)
	{
		List<IType> inferred = new ArrayList<IType>();
		IRubyScript script = getScript(context);
		if (script == null)
			return inferred;

		try
		{
			CompletionContext myContext = new CompletionContext(script, context.getInvocationOffset() - 1);
			Collection<ITypeGuess> guesses = getTypeInferrer().infer(myContext.getCorrectedSource(),
					myContext.getOffset());
			if (guesses == null)
				return inferred;
			RubyElementRequestor requestor = new RubyElementRequestor(script);
			for (ITypeGuess guess : guesses)
			{
				IType[] types = requestor.findType(guess.getType());
				if (types != null && types.length > 0)
					inferred.add(new LogicalType(types));
			}
		}
		catch (RubyModelException e)
		{
			RailsUILog.log(e);
		}
		return inferred;
	}

	protected ITypeInferrer getTypeInferrer()
	{
		return RubyCore.getTypeInferrer();
	}

	/**
	 * Infer the receiver, and if it could possibly be an ActiveRecord, return that type guess.
	 * 
	 * @param context
	 * @return an IType of an ActiveRecord model that we have inferred may be our receiver
	 */
	private IType getInferredActiveRecord(RubyContentAssistInvocationContext context)
	{
		List<IType> types = inferType(context);
		for (IType type : types)
		{
			try
			{
				// FIXME Check up the entire hierarchy!
				if ("ActiveRecord::Base".equals(type.getSuperclassName()))
					return type;
			}
			catch (RubyModelException e)
			{
				RailsUILog.log(e);
			}
		}
		return null;
	}

	/**
	 * Infer the type we're being invoked on. If it's an ActiveRecord model, then suggest each DB field (as defined in
	 * migrations) accessor, writer and find_by finder method.
	 * 
	 * @param context
	 * @return
	 */
	private List<IRubyCompletionProposal> addActiveRecordFieldMethods(IType type,
			RubyContentAssistInvocationContext context)
	{
		if (type == null)
			return Collections.EMPTY_LIST;

		CompletionProposalCollector collector = createCollector(context);
		Set<String> fieldNames = getDBFieldNames(getScript(context), type.getElementName());
		for (String fieldName : fieldNames)
		{
			// add accessor
			collector.accept(createProposal(context, type.getElementName(), new PsuedoMethod(fieldName, null,
					Flags.AccPublic)));

			// add writer
			collector.accept(createProposal(context, type.getElementName(), new PsuedoMethod(fieldName + "=",
					new String[] { fieldName }, Flags.AccPublic)));

			// add dynamic finders
			collector.accept(createProposal(context, type.getElementName(), new PsuedoMethod("find_by_" + fieldName,
					new String[] { fieldName }, Flags.AccPublic | Flags.AccStatic)));
			collector.accept(createProposal(context, type.getElementName(), new PsuedoMethod(
					"find_all_by_" + fieldName, new String[] { fieldName }, Flags.AccPublic | Flags.AccStatic)));
		}
		// TODO Add more complicated finder methods that combine multiple
		// fields?
		return Arrays.asList(collector.getRubyCompletionProposals());
	}

	private IRubyScript getScript(RubyContentAssistInvocationContext context)
	{
		return context.getRubyScript();
	}

	private Set<String> getDBFieldNames(IRubyScript script, String modelName)
	{
		Set<String> fieldNames = new HashSet<String>();
		File[] scripts = getMigrationScripts(script);
		for (int j = 0; j < scripts.length; j++)
		{
			IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(
					new Path(scripts[j].getAbsolutePath()));
			IRubyScript migrateScript = RubyCore.create(iFile);
			RootNode ast = null;
			if (migrateScript.isWorkingCopy())
			{
				try
				{
					String prefix = getStatementPrefix();
					int fudgeFactor = 1;
					while (prefix.endsWith(" "))
					{
						fudgeFactor++;
						prefix = prefix.substring(0, prefix.length() - 1);
					}
					CompletionContext correctingContext = new CompletionContext(migrateScript, fContext
							.getInvocationOffset()
							- fudgeFactor);
					if (correctingContext.isBroken())
					{
						try
						{
							RubyParser parser = new RubyParser();
							ast = (RootNode) parser.parse(correctingContext.getCorrectedSource()).getAST();
						}
						catch (SyntaxException e)
						{
							// ignore
						}
					}
				}
				catch (RubyModelException e)
				{
					RailsUILog.log(e);
				}
			}
			if (ast == null)
			{
				ast = RubyPlugin.getDefault().getASTProvider().getAST(migrateScript, ASTProvider.WAIT_YES,
						new NullProgressMonitor());
				if (ast == null)
				{
					ast = (RootNode) ((RubyScript) migrateScript).lastGoodAST;
				}
			}
			if (ast == null)
				continue;
			MigrationVisitor visitor = new MigrationVisitor();
			ast.accept(visitor);
			fieldNames.addAll(visitor.getFieldNames(Inflector.pluralize(modelName)));
		}
		return fieldNames;
	}

	private String getStatementPrefix()
	{
		try
		{
			return fContext.computeStatementPrefix().toString();
		}
		catch (BadLocationException e)
		{
			RailsUILog.log(e);
			return "";
		}
	}

	private Set<String> getDBTableNames(IRubyScript script)
	{
		Set<String> fieldNames = new HashSet<String>();
		File[] scripts = getMigrationScripts(script);
		for (int j = 0; j < scripts.length; j++)
		{
			IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(
					new Path(scripts[j].getAbsolutePath()));
			IRubyScript migrateScript = RubyCore.create(iFile);
			RootNode ast = RubyPlugin.getDefault().getASTProvider().getAST(migrateScript, ASTProvider.WAIT_YES,
					new NullProgressMonitor());
			if (ast == null)
			{
				ast = (RootNode) ((RubyScript) migrateScript).lastGoodAST;
			}
			if (ast == null)
				continue;
			MigrationVisitor visitor = new MigrationVisitor();
			ast.accept(visitor);
			fieldNames.addAll(visitor.getTableNames());
		}
		return fieldNames;
	}

	private File[] getMigrationScripts(IRubyScript script)
	{
		IPath migrationFolder = getMigrationPath(script);
		if (migrationFolder == null)
			return new File[0];
		migrationFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(migrationFolder).getLocation();
		File[] scripts = migrationFolder.toFile().listFiles(new FilenameFilter()
		{

			public boolean accept(File dir, String name)
			{
				return name.endsWith(".rb");
			}

		});
		if (scripts == null)
			return new File[0];
		return scripts;
	}

	private File getControllersFolder(ContentAssistInvocationContext context)
	{
		if (!(context instanceof RubyContentAssistInvocationContext))
			return null;
		RubyContentAssistInvocationContext rContext = (RubyContentAssistInvocationContext) context;
		IRubyScript script = rContext.getRubyScript();
		if (script == null)
			return null;
		IProject project = script.getRubyProject().getProject();
		IPath path = RailsPlugin.findRailsRoot(project);
		IFolder folder = project.getFolder(path.append("app").append("controllers"));
		if (folder == null)
			return null;
		return folder.getLocation().toFile();
	}

	public List computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor)
	{
		return Collections.EMPTY_LIST;
	}

	public String getErrorMessage()
	{
		return null;
	}

	public void sessionEnded()
	{
	}

	public void sessionStarted()
	{
	}

}
