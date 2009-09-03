package org.radrails.rails.internal.ui.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.viewers.AsynchronousSchedulingRuleFactory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.IConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.internal.console.IOConsoleViewer;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.progress.UIJob;
import org.radrails.rails.core.RailsLog;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.RailsUIPlugin;
import org.radrails.rails.ui.console.RailsShellCommandProvider;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.ITerminal;

import com.aptana.ide.core.IdeLog;
import com.aptana.rdt.rake.IRakeHelper;
import com.aptana.rdt.rake.RakePlugin;

public class RailsShell extends IOConsole implements IPartListener, IDocumentListener, ITerminal,
		IResourceChangeListener, ICompletionListener
{

	private static final String ENCODING = "UTF-8";
	private static final String OUTPUT_PARTITION_TYPE = "org.eclipse.ui.console.io_console_output_partition_type";
	private static final String INPUT_PARTITION_TYPE = "org.eclipse.ui.console.io_console_input_partition_type";
	private static List<RailsShellCommandProvider> fgProviders;

	private IOConsolePage page;
	private IOConsoleOutputStream fOutputStream;
	private IOConsoleInputStream fInput;
	private StreamListener listener;
	private IConsoleColorProvider fColorProvider;
	private IProcess fProcess;
	private ConsoleLineNotifier consoleLineNotifier;
	private IProject fProject;
	private ContentAssistant fContentAssistant;
	private RailsShellContentAssistProcessor fContentAssistProcessor;
	private IStreamsProxy streamsProxy;
	private IOConsoleOutputStream fFakeInputStream;
	private IOConsoleOutputStream fErrorStream;
	private Map<String, StreamListener> listeners;
	private boolean fActivated;
	private RailsShellExecutor executor;

	/**
	 * For jobs that are spitting out output, force them to run serially
	 */
	private ISchedulingRule rule = AsynchronousSchedulingRuleFactory.getDefault().newSerialRule();
	private ArrayList<String> fCommandHistory;
	private int fCommandIndex;
	private InputReadJob readJob;
	private boolean completionActive;
	protected ILaunch currentLaunch;

	public RailsShell()
	{
		super("Rails Shell", IRailsShellConstants.TERMINAL_ID, RailsUIPlugin.getImageDescriptor("icons/rails.gif"), ENCODING, true);
		fInput = getInputStream();
		fColorProvider = DebugUIPlugin.getDefault().getProcessConsoleManager().getColorProvider(
				IRubyLaunchConfigurationConstants.ID_RUBY_PROCESS_TYPE);
		fInput.setColor(fColorProvider.getColor(IDebugUIConstants.ID_STANDARD_INPUT_STREAM));
		fContentAssistProcessor = new RailsShellContentAssistProcessor();
		listeners = new HashMap<String, StreamListener>();
		executor = new RailsShellExecutor(this);
		Set<IProject> projects = RailsPlugin.getRailsProjects();
		if (projects != null && !projects.isEmpty())
			setProject(projects.iterator().next());
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		fCommandHistory = new ArrayList<String>();
	}

	@Override
	public IPageBookViewPage createPage(IConsoleView view)
	{
		page = (IOConsolePage) super.createPage(view);
		IDocument doc = getDocument();
		IHandlerService service = (IHandlerService) view.getSite().getService(IHandlerService.class);
		service.activateHandler("org.eclipse.ui.edit.text.contentAssist.proposals", new AbstractHandler()
		{

			public Object execute(ExecutionEvent evt) throws ExecutionException
			{
				fContentAssistant.showPossibleCompletions();
				return null;
			}

		});
		doc.addDocumentListener(this);
		view.getSite().getPage().addPartListener(this);
		return page;
	}

	public void attach(IProcess process)
	{
		if (fProcess != null)
		{
			// dicsonnect old process
			removePatternMatchListener(consoleLineNotifier);
		}

		fProcess = process;
		IConsoleLineTracker[] lineTrackers = DebugUIPlugin.getDefault().getProcessConsoleManager().getLineTrackers(
				process);
		if (lineTrackers.length > 0)
		{
			consoleLineNotifier = new ConsoleLineNotifier();
			addPatternMatchListener(consoleLineNotifier);
		}

		connect(process.getStreamsProxy());
	}

	public void connect(IStreamsProxy streamsProxy)
	{
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		IStreamMonitor streamMonitor = streamsProxy.getErrorStreamMonitor();
		if (streamMonitor != null)
		{
			connect(streamMonitor, IDebugUIConstants.ID_STANDARD_ERROR_STREAM);
			IOConsoleOutputStream stream = getStream(IDebugUIConstants.ID_STANDARD_ERROR_STREAM);
			if (stream != null)
			{
				stream.setActivateOnWrite(store.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR));
			}
		}
		streamMonitor = streamsProxy.getOutputStreamMonitor();
		if (streamMonitor != null)
		{
			connect(streamMonitor, IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM);
			IOConsoleOutputStream stream = getStream(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM);
			if (stream != null)
			{
				stream.setActivateOnWrite(store.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT));
			}
		}
		if (readJob == null)
		{
			readJob = new InputReadJob(streamsProxy);
			readJob.setSystem(true);
			readJob.schedule();
		}
		else
		{
			readJob.setStreamProxy(streamsProxy);
		}

		this.streamsProxy = streamsProxy;
	}

	public void connect(IStreamMonitor streamMonitor, String streamIdentifier)
	{
		synchronized (streamMonitor)
		{
			StreamListener listener = listeners.get(streamIdentifier);
			if (listener != null)
			{
				listener.getStreamMonitor().removeListener(listener);
			}
			listener = new StreamListener(streamIdentifier, streamMonitor, getStream(streamIdentifier));
			listeners.put(streamIdentifier, listener);
		}
	}

	@Override
	public void activate()
	{
		super.activate();
		if (!fActivated)
		{
			fOutputStream = newOutputStream();
			fOutputStream.setColor(fColorProvider.getColor(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM));

			fErrorStream = newOutputStream();
			fErrorStream.setColor(fColorProvider.getColor(IDebugUIConstants.ID_STANDARD_ERROR_STREAM));

			fFakeInputStream = newOutputStream();
			fFakeInputStream.setColor(fColorProvider.getColor(IDebugUIConstants.ID_STANDARD_INPUT_STREAM));
			write(
					IDebugUIConstants.ID_STANDARD_INPUT_STREAM,
					 "Welcome to the Rails Shell. This view is meant for advanced users and command line lovers as a text-based way\nto run rails "
							+ "commands such as: rails, script/generate, script/plugin, gem, rake, etc.\nThis shell can replace the functionality of the "
							+ "Rake Tasks, Rails Plugins, and generators views.\n\n" + IRailsShellConstants.PROMPT);

			DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener()
			{

				public void handleDebugEvents(DebugEvent[] events)
				{
					if (events == null)
						return;
					for (int i = 0; i < events.length; i++)
					{
						if (events[i].getKind() != DebugEvent.TERMINATE)
							continue;
						if (!(events[i].getSource() instanceof IProcess))
							continue;
						IProcess source = (IProcess) events[i].getSource();
						ILaunch launch = source.getLaunch();
						String id = launch.getAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL);
						if (id != null && id.equals(IRailsShellConstants.TERMINAL_ID))
						{
							writePrompt();
							fProcess = null;
						}
					}
				}

			});
			UIJob job = new UIJob("")
			{
				private int tries = 3;

				public IStatus runInUIThread(IProgressMonitor monitor)
				{
					if (page == null || page.getViewer() == null || page.getViewer().getTextWidget() == null)
					{
						if (tries-- > 0)
							schedule(2000);
						return Status.CANCEL_STATUS;
					}
					// Override HOME/Tab/Ctrl+C key combos
					page.getViewer().getTextWidget().addVerifyKeyListener(new VerifyKeyListener()
					{

						public void verifyKey(VerifyEvent e)
						{
							int keyCode = e.keyCode;
							if (keyCode == 'c' && e.stateMask == SWT.CONTROL) // 'Ctrl+c'
							{
								// Kill the current launch!
								if (fProcess != null && fProcess.canTerminate())
								{
									try
									{
										fProcess.terminate();
									}
									catch (DebugException e1)
									{
										IdeLog.logError(RailsUIPlugin.getInstance(), e1.getMessage(), e1);
									}
								}
								return;
							}
							if (keyCode == 9)
							{ // Tab
								e.doit = false;
								fContentAssistant.showPossibleCompletions();
								return;
							}
							if (keyCode == 16777223)
							{ // HOME
								e.doit = false;
								String contents = page.getViewer().getDocument().get();
								int offset = contents.lastIndexOf(IRailsShellConstants.PROMPT);
								if (offset == -1)
								{
									offset = page.getViewer().getTextWidget().getCharCount();
								}
								else
								{
									offset++;
								}
								page.getViewer().getTextWidget().setCaretOffset(offset);
							}
							else
							{
								if (completionActive)
								{
									return;
								}
								String command = null;
								if (isUpArrow(e))
								{
									e.doit = false; // FIXME For whatever reason this isn't stopping cursor from moving
									command = getCommandFromHistory(-1);
								}
								else if (isDownArrow(e))
								{
									e.doit = false;
									command = getCommandFromHistory(1);
								}
								else
								{
									return;
								}
								if (command == null)
									return;
								try
								{
									setCommand(command);
									page.getViewer().getTextWidget().setCaretOffset(getDocument().getLength());
								}
								catch (Exception e1)
								{
									RailsLog.log(e1);
								}
							}
						}

						private void setCommand(String command) throws BadLocationException
						{
							// Back up to beginning of first line and replace from there to end of document
							int lines = getDocument().getNumberOfLines();
							int offset = getDocument().getLineOffset(lines - 1);
							int length = getDocument().getLength() - offset;
							String toReplace = getDocument().get(offset, length);
							if (toReplace.trim().startsWith(IRailsShellConstants.PROMPT))
							{
								int index = toReplace.indexOf(IRailsShellConstants.PROMPT);
								offset += index + IRailsShellConstants.PROMPT.length();
								length -= index + IRailsShellConstants.PROMPT.length();
							}
							getDocument().replace(offset, length, command);

						}

						private boolean isUpArrow(VerifyEvent e)
						{
							return e.keyCode == 16777217;
						}

						private boolean isDownArrow(VerifyEvent e)
						{
							return e.keyCode == 16777218;
						}
					});
					return Status.OK_STATUS;
				}

			};
			job.schedule();

			fActivated = true;
		}
	}

	protected String getCommandFromHistory(int move)
	{
		if (fCommandHistory.isEmpty())
			return null;
		fCommandIndex += move;
		if (fCommandIndex <= 0)
		{
			fCommandIndex = 0;
		}
		else if (fCommandIndex >= fCommandHistory.size())
		{
			fCommandIndex = fCommandHistory.size();
			return "";
		}
		return fCommandHistory.get(fCommandIndex);
	}

	private void writePrompt()
	{
		write(IDebugUIConstants.ID_STANDARD_INPUT_STREAM, IRailsShellConstants.PROMPT);
	}

	private void installContentAssist()
	{
		if (page == null || fContentAssistant != null)
			return;
		IOConsoleViewer viewer = (IOConsoleViewer) page.getViewer();
		if (viewer == null)
			return;
		fContentAssistant = new ContentAssistant();
		fContentAssistant.setContentAssistProcessor(fContentAssistProcessor, INPUT_PARTITION_TYPE);
		fContentAssistant.setContentAssistProcessor(fContentAssistProcessor, OUTPUT_PARTITION_TYPE);
		fContentAssistant.enableAutoActivation(true);
		fContentAssistant.enableAutoInsert(true);
		fContentAssistant.enablePrefixCompletion(true);
		fContentAssistant.install(viewer);
		fContentAssistant.addCompletionListener(this);
	}

	public void partActivated(IWorkbenchPart part)
	{
		if (part instanceof IConsoleView)
		{
			page.setFocus();
		}
	}

	public void partBroughtToTop(IWorkbenchPart part)
	{
		installContentAssist();
	}

	public void partClosed(IWorkbenchPart part)
	{
	}

	public void partDeactivated(IWorkbenchPart part)
	{
	}

	public void partOpened(IWorkbenchPart part)
	{
		if (part instanceof IConsoleView)
		{
			installContentAssist();
		}
	}

	public void documentAboutToBeChanged(DocumentEvent event)
	{
		installContentAssist();
	}

	public void documentChanged(DocumentEvent event)
	{
		installContentAssist();
		String text = event.getText();
		IDocument doc = event.getDocument();

		String[] delimeters = doc.getLegalLineDelimiters();
		if (contains(text, delimeters))
		{
			// We hit a newline, execute the current command!
			String contents = doc.get();
			int index = contents.lastIndexOf("\n", event.getOffset() - text.length());
			if (index == -1)
				index = contents.lastIndexOf("\r", event.getOffset() - text.length()); // Handle \r too
			if (index != -1)
				contents = contents.substring(index + 1);
			// If it doesn't contain the prompt, it's probably not a command!
			index = contents.indexOf(IRailsShellConstants.PROMPT);
			if (index == -1)
				return;
			// strip the prompt
			contents = contents.substring(index + 1).trim();
			fCommandHistory.add(contents);
			fCommandIndex = fCommandHistory.size();
			executor.run(fProject, contents);
		}
	}

	protected IRakeHelper getRakeTasksHelper()
	{
		return RakePlugin.getDefault().getRakeHelper();
	}

	private boolean contains(String text, String[] delimeters)
	{
		for (int i = 0; i < delimeters.length; i++)
		{
			if (text.equals(delimeters[i]))
				return true;
		}
		return false;
	}

	private class StreamListener implements IStreamListener
	{

		private IOConsoleOutputStream fStream;
		private IStreamMonitor fStreamMonitor;
		private String fStreamId;
		private boolean fFlushed = false;
		private boolean fListenerRemoved = false;

		public StreamListener(String streamIdentifier, IStreamMonitor monitor, IOConsoleOutputStream stream)
		{
			this.fStreamId = streamIdentifier;
			this.fStreamMonitor = monitor;
			this.fStream = stream;
			fStreamMonitor.addListener(this);

			// fix to bug 121454. Ensure that output to fast processes is processed.
			streamAppended(null, monitor);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.debug.core.IStreamListener#streamAppended(java.lang.String,
		 * org.eclipse.debug.core.model.IStreamMonitor)
		 */
		public void streamAppended(String text, IStreamMonitor monitor)
		{
			if (fFlushed)
			{
				try
				{
					if (fStream != null)
					{
						fStream.write(text);
					}
				}
				catch (IOException e)
				{
					DebugUIPlugin.log(e);
				}
			}
			else
			{
				String contents = null;
				synchronized (fStreamMonitor)
				{
					fFlushed = true;
					contents = fStreamMonitor.getContents();
					if (fStreamMonitor instanceof IFlushableStreamMonitor)
					{
						IFlushableStreamMonitor m = (IFlushableStreamMonitor) fStreamMonitor;
						m.flushContents();
						m.setBuffered(false);
					}
				}
				try
				{
					if (contents != null && contents.length() > 0)
					{
						if (fStream != null)
						{
							fStream.write(contents);
						}
					}
				}
				catch (IOException e)
				{
					DebugUIPlugin.log(e);
				}
			}
		}

		public IStreamMonitor getStreamMonitor()
		{
			return fStreamMonitor;
		}

		public void closeStream()
		{
			if (fStreamMonitor == null)
			{
				return;
			}
			synchronized (fStreamMonitor)
			{
				fStreamMonitor.removeListener(this);
				if (!fFlushed)
				{
					String contents = fStreamMonitor.getContents();
					streamAppended(contents, fStreamMonitor);
				}
				fListenerRemoved = true;
				try
				{
					if (fStream != null)
					{
						fStream.close();
					}
				}
				catch (IOException e)
				{
				}
			}
		}

		public void dispose()
		{
			if (!fListenerRemoved)
			{
				closeStream();
			}
			fStream = null;
			fStreamMonitor = null;
			fStreamId = null;
		}
	}

	public void addLink(IConsoleHyperlink link, int offset, int length)
	{
		try
		{
			addHyperlink(link, offset, length);
		}
		catch (BadLocationException e)
		{
			DebugUIPlugin.log(e);
		}
	}

	public void addLink(IHyperlink link, int offset, int length)
	{
		try
		{
			addHyperlink(link, offset, length);
		}
		catch (BadLocationException e)
		{
			DebugUIPlugin.log(e);
		}
	}

	public IProcess getProcess()
	{
		return fProcess;
	}

	public IRegion getRegion(IConsoleHyperlink link)
	{
		return super.getRegion(link);
	}

	public IOConsoleOutputStream getStream(String streamIdentifier)
	{
		if (streamIdentifier.equals(IDebugUIConstants.ID_STANDARD_ERROR_STREAM))
			return fErrorStream;
		if (streamIdentifier.equals(IDebugUIConstants.ID_STANDARD_INPUT_STREAM))
			return fFakeInputStream;
		return fOutputStream;
	}

	public void setProject(final IProject project)
	{
		this.fProject = project;
		fContentAssistProcessor.setProject(project);
		Display.getDefault().asyncExec(new Runnable()
		{

			public void run()
			{
				if (project == null)
					setName("Rails Shell (" + RailsPlugin.getInstance().getRailsPath() + ")");
				else
					setName("Rails Shell - " + project.getName() + " (" + RailsPlugin.getInstance().getRailsPath()
							+ ")");
			}

		});

	}

	public void write(final String streamIdentifier, final String text)
	{
		if (streamIdentifier.equals(IDebugUIConstants.ID_STANDARD_INPUT_STREAM) && text.endsWith("\n"))
		{
			fCommandHistory.add(text.substring(0, text.length() - 1));
		}

		UIJob job = new UIJob("")
		{

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{

				try
				{
					IOConsoleOutputStream stream = getStream(streamIdentifier);
					stream.write(text);
					return Status.OK_STATUS;
				}
				catch (IOException e)
				{
					RailsUILog.log(e);
					return new Status(Status.ERROR, RailsUIPlugin.getPluginIdentifier(), -1,
							"Error writing text to rails shell", e);
				}
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setRule(rule);
		job.schedule(5);
	}

	@Override
	public void clearConsole()
	{
		super.clearConsole();
		writePrompt();
	}

	public void resourceChanged(IResourceChangeEvent event)
	{
		if (event == null)
			return;
		IResourceDelta delta = event.getDelta();
		traverseDelta(delta);
	}

	/**
	 * boolean indicates if we should continue to traverse.
	 * 
	 * @param delta
	 * @return
	 */
	private boolean traverseDelta(IResourceDelta delta)
	{
		if (delta == null)
			return true;
		IResource res = delta.getResource();
		if (res instanceof IWorkspaceRoot)
		{
			IResourceDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; i++)
			{
				if (!traverseDelta(children[i]))
					return false;
			}
			return true;
		}
		if (!(res instanceof IProject))
			return false;
		int deltaKind = delta.getKind();
		if (deltaKind != IResourceDelta.ADDED && deltaKind != IResourceDelta.REMOVED)
			return false;
		IProject project = (IProject) res;
		if (deltaKind == IResourceDelta.ADDED)
		{
			if (RailsPlugin.hasRailsNature(project))
			{
				setProject(project);
			}
			return false;
		}
		if (fProject != null && !project.equals(fProject))
			return false;
		Set<IProject> projects = RailsPlugin.getRailsProjects();
		for (final IProject possible : projects)
		{
			if (!possible.equals(project))
			{
				Display.getDefault().asyncExec(new Runnable()
				{

					public void run()
					{
						setProject(possible);
					}

				});
				return false;
			}
		}
		setProject(null);
		return false;
	}

	public static RailsShell open()
	{
		RailsShell console = new RailsShell();
		ConsolePlugin conMan = ConsolePlugin.getDefault();
		conMan.getConsoleManager().addConsoles(new IConsole[] { console });
		console.activate();
		return console;
	}

	private class InputReadJob extends Job
	{

		private IStreamsProxy streamsProxy;

		InputReadJob(IStreamsProxy streamsProxy)
		{
			super("Process Console Input Job"); //$NON-NLS-1$
			this.streamsProxy = streamsProxy;
		}

		public void setStreamProxy(IStreamsProxy streamsProxy2)
		{
			this.streamsProxy = streamsProxy2;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor)
		{
			try
			{
				byte[] b = new byte[1024];
				int read = 0;
				while (fInput != null && read >= 0)
				{
					read = fInput.read(b);
					if (read > 0)
					{
						String s = new String(b, 0, read);
						// FIXME If there' no active launch associated with this shell, don't pipe it, it's a new
						// command at the prompt
						if (fProcess != null && !fProcess.isTerminated())
						{
							streamsProxy.write(s);
						}
					}
				}
			}
			catch (IOException e)
			{
				RailsUILog.log(e);
			}
			return Status.OK_STATUS;
		}
	}

	public void assistSessionEnded(ContentAssistEvent event)
	{
		completionActive = false;
	}

	public void assistSessionStarted(ContentAssistEvent event)
	{
		completionActive = true;
	}

	public void selectionChanged(ICompletionProposal proposal, boolean smartToggle)
	{
	}

	public static List<RailsShellCommandProvider> getCommandProviders(IProject fProject, String fRunMode)
	{
		List<RailsShellCommandProvider> providers = getCommandProviders();
		for (RailsShellCommandProvider provider : providers)
		{
			provider.initialize(fProject, fRunMode);
		}
		return providers;
	}

	private static List<RailsShellCommandProvider> getCommandProviders()
	{
		if (fgProviders == null)
		{
			final Map<RailsShellCommandProvider, Integer> providersToPriority = new HashMap<RailsShellCommandProvider, Integer>();
			IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(
					RailsUIPlugin.getPluginIdentifier(), "railsShellCommandProviders");
			if (extension == null)
				return Collections.emptyList();

			// for all extensions of this point...
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			for (int j = 0; j < configElements.length; j++)
			{
				try
				{
					RailsShellCommandProvider provider = (RailsShellCommandProvider) configElements[j]
							.createExecutableExtension("class");
					String rawPriority = configElements[j].getAttribute("priority");
					int priority = 50;
					try
					{
						priority = Integer.parseInt(rawPriority);
					}
					catch (Exception e)
					{
						// ignore
					}
					providersToPriority.put(provider, priority);
				}
				catch (CoreException e)
				{
					RailsUILog.log(e);
				}
			}
			// Sort providers by ascending priority
			List<RailsShellCommandProvider> providers = new ArrayList<RailsShellCommandProvider>(providersToPriority
					.keySet());
			Collections.sort(providers, new Comparator<RailsShellCommandProvider>()
			{
				public int compare(RailsShellCommandProvider o1, RailsShellCommandProvider o2)
				{
					return providersToPriority.get(o1).compareTo(providersToPriority.get(o2));
				}
			});
			fgProviders = providers;
		}
		return fgProviders;
	}

}
