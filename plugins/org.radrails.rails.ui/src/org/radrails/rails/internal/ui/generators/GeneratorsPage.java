package org.radrails.rails.internal.ui.generators;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.Page;
import org.radrails.rails.core.RailsLog;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.generators.Generator;
import org.radrails.rails.internal.generators.GeneratorLocatorsManager;
import org.radrails.rails.internal.ui.console.IRailsShellConstants;
import org.radrails.rails.ui.IRailsUIConstants;
import org.radrails.rails.ui.RailsUIPlugin;
import org.rubypeople.rdt.core.util.Util;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.RubyExplorerTracker.IRubyProjectListener;
import org.rubypeople.rdt.internal.ui.text.RubyColorManager;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.ide.core.ui.SWTUtils;

/**
 * GeneratorsPage
 */
public class GeneratorsPage extends Page implements IRubyProjectListener
{

	private static final int MAX_INPUT_HISTORY_SIZE = 25;
	private static final String PROJECT = "Current Rails Project: ";

	private Label projectNameLabel;
	private Composite genComp;
	private Label genLabel;
	private Combo genCombo;
	private Button createButton;
	private Button destroyButton;
	private Button pretendButton;
	private Button skipButton;
	private Button forceButton;
	private Button quietButton;
	private Button backtraceButton;
	private Label helpButton;
	private Button svnButton;
	private Composite paramComp;
	private Label paramLabel;
	private Combo paramText;
	private Composite generateView;

	private RubyColorManager fColorManager;

	private IProject project = null;
	private Cursor hand;
	private boolean expandedLabel;

	private static Map<String, String> helps = new HashMap<String, String>();

	/**
	 * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent)
	{
		generateView = new Composite(parent, SWT.NONE);
		generateView.setLayout(new GridLayout(3, false));
		generateView.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		fColorManager = new RubyColorManager(true);

		createGeneratorControls(generateView);

		RubyPlugin.getDefault().getProjectTracker().addProjectListener(this);
		IProject project = RubyPlugin.getDefault().getProjectTracker().getSelectedByNatureID(
				IRailsUIConstants.RAILS_PROJECT_NATURE);
		Set<IProject> projects = RailsPlugin.getRailsProjects();
		if (project != null)
		{
			this.projectSelected(project);

		}
		else if (projects != null && projects.size() > 0)
		{
			this.projectSelected(projects.iterator().next());
		}
	}

	/**
	 * The <code>Page</code> implementation of this <code>IPage</code> method disposes of this page's control (if it has
	 * one and it has not already been disposed). Subclasses may extend.
	 */
	public void dispose()
	{
		fColorManager.dispose();
		RubyPlugin.getDefault().getProjectTracker().removeProjectListener(this);
		super.dispose();
	}

	/**
	 * Helper method to create the necessary widgets.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private void createGeneratorControls(Composite parent)
	{
		hand = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		Composite comp = createMainArea(parent);
		createAdvancedSection(comp);
	}

	private Composite createMainArea(Composite comp)
	{
		projectNameLabel = new Label(comp, SWT.LEFT);
		projectNameLabel.setForeground(fColorManager.getColor(new RGB(128, 128, 128)));
		projectNameLabel.setText(PROJECT);
		projectNameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 3, 1));

		// Grid for generator selection: label, combo, help button
		genComp = new Composite(comp, SWT.NONE);
		genComp.setLayout(new GridLayout(3, false));
		genComp.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		genLabel = new Label(genComp, SWT.LEFT);
		genLabel.setText("Generator:");

		genCombo = new Combo(genComp, SWT.DROP_DOWN);
		GridData genComboData = new GridData();
		genComboData.widthHint = 200;
		genCombo.setLayoutData(genComboData);
		genCombo.setVisibleItemCount(20);

		helpButton = new Label(genComp, SWT.PUSH);
		helpButton.setToolTipText("Help");
		helpButton.setImage(RailsUIPlugin.getImage("icons/help.png"));
		// Run the command when the button is clicked
		helpButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				getHelp(getSelectedGenerator(), getProject());
			}
		});

		// Create parameters grid: label, text field, 'run' button
		paramComp = new Composite(comp, SWT.NONE);
		paramComp.setLayout(new GridLayout(3, false));
		paramComp.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		paramLabel = new Label(paramComp, SWT.LEFT);
		paramLabel.setText("Parameters:");

		paramText = new Combo(paramComp, SWT.BORDER);
		paramText.setLayoutData(genComboData);
		paramText.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent e)
			{
				// Do nothing
			}

			public void keyReleased(KeyEvent e)
			{
				// Take action if Enter was pressed
				if (e.character == SWT.CR)
				{
					executeCommand();
				}
			}
		});

		// Create the Go button
		Button genButton = new Button(comp, SWT.PUSH);
		genButton.setToolTipText("Run Generator");
		genButton.setImage(RailsUIPlugin.getImage("icons/nav_go.gif"));
		// Run the command when the button is clicked
		genButton.addSelectionListener(new SelectionListener()
		{

			public void widgetSelected(SelectionEvent e)
			{
				executeCommand();
			}

			public void widgetDefaultSelected(SelectionEvent e)
			{
				// Do nothing
			}

		});

		Label hint = new Label(comp, SWT.WRAP);
		hint.setText("If dropdown is empty, please hit yellow arrow 'refresh' icon.");
		hint.setForeground(fColorManager.getColor(new RGB(128, 128, 128)));
		hint.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 3, 1));
		return comp;
	}

	private Composite createAdvancedSection(final Composite parent)
	{
		final Composite advanced = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		advanced.setLayout(layout);
		GridData advancedData = new GridData(SWT.FILL, SWT.FILL, true, false);
		advancedData.horizontalSpan = 3;
		advanced.setLayoutData(advancedData);

		final Font boldFont = new Font(advanced.getDisplay(), SWTUtils.boldFont(advanced.getFont()));
		advanced.addDisposeListener(new DisposeListener()
		{

			public void widgetDisposed(DisposeEvent e)
			{
				if (hand != null && !hand.isDisposed())
				{
					hand.dispose();
				}
				if (boldFont != null && !boldFont.isDisposed())
				{
					boldFont.dispose();
				}
			}

		});

		final Label advancedIcon = new Label(advanced, SWT.LEFT);
		advancedIcon.setImage(RailsUIPlugin.getImage("icons/maximize.png")); //$NON-NLS-1$
		advancedIcon.setCursor(hand);
		advancedIcon.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		Label advancedLabel = new Label(advanced, SWT.LEFT);
		advancedLabel.setText("Advanced Options");
		advancedLabel.setCursor(hand);
		advancedLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		advancedLabel.setFont(boldFont);

		final Composite advancedOptions = new Composite(advanced, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginLeft = 15;
		advancedOptions.setLayout(layout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		gridData.exclude = true;
		advancedOptions.setLayoutData(gridData);
		advancedOptions.setVisible(false);

		MouseAdapter expander = new MouseAdapter()
		{

			public void mouseDown(MouseEvent e)
			{
				if (advancedOptions.isVisible())
				{
					advancedOptions.setVisible(false);
					advancedIcon.setImage(RailsUIPlugin.getImage("icons/maximize.png")); //$NON-NLS-1$
					((GridData) advancedOptions.getLayoutData()).exclude = true;
				}
				else
				{
					advancedOptions.setVisible(true);
					advancedIcon.setImage(RailsUIPlugin.getImage("icons/minimize.png")); //$NON-NLS-1$
					((GridData) advancedOptions.getLayoutData()).exclude = false;
				}
				generateView.layout(true, true);
			}

		};
		advancedIcon.addMouseListener(expander);
		advancedLabel.addMouseListener(expander);

		// Create the radio buttons for create/destroy
		Group modes = new Group(advancedOptions, SWT.NONE);
		modes.setText("Modes");
		modes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		modes.setLayout(new GridLayout(2, false));

		createButton = new Button(modes, SWT.RADIO);
		createButton.setText("Create");
		createButton.setSelection(true);
		destroyButton = new Button(modes, SWT.RADIO);
		destroyButton.setText("Destroy");

		// Create the text field for the options information
		Group optionsGroup = new Group(advancedOptions, SWT.NULL);
		optionsGroup.setLayout(new GridLayout(7, false));
		optionsGroup.setText("Options");

		pretendButton = new Button(optionsGroup, SWT.CHECK);
		pretendButton.setText("Pretend");
		forceButton = new Button(optionsGroup, SWT.CHECK);
		forceButton.setText("Force");
		skipButton = new Button(optionsGroup, SWT.CHECK);
		skipButton.setText("Skip");
		quietButton = new Button(optionsGroup, SWT.CHECK);
		quietButton.setText("Quiet");
		backtraceButton = new Button(optionsGroup, SWT.CHECK);
		backtraceButton.setText("Backtrace");
		svnButton = new Button(optionsGroup, SWT.CHECK);
		svnButton.setText("Use SVN");

		return advanced;
	}

	/**
	 * Runs the command specified in the view.
	 */
	private void executeCommand()
	{
		String script = getGenerateScript();

		// Check the options boxes
		String options = "";
		if (pretendButton.getSelection())
		{
			options += "p";
		}
		if (forceButton.getSelection())
		{
			options += "f";
		}
		if (skipButton.getSelection())
		{
			options += "s";
		}
		if (quietButton.getSelection())
		{
			options += "q";
		}
		if (backtraceButton.getSelection())
		{
			options += "t";
		}
		if (svnButton.getSelection())
		{
			options += "c";
		}
		if (options.length() > 0)
		{
			options = " -" + options;
		}

		// Prepare the full command
		String args = genCombo.getText() + " " + paramText.getText() + options;
		if (project != null)
		{
			if (project.exists() && project.isOpen())
			{
				// Update the input field history
				LinkedList<String> items = new LinkedList<String>(Arrays.asList(paramText.getItems()));
				items.addFirst(paramText.getText());
				if (items.size() > MAX_INPUT_HISTORY_SIZE)
				{
					items.removeLast();
				}
				paramText.setItems(items.toArray(new String[0]));

				launchTerminal(script.substring(script.indexOf("/")) + genCombo.getText(), script, args);
			}
			else
			{
				MessageDialog
						.openError(
								getControl().getShell(),
								"Selected project closed/doesn't exist",
								"The currently selected Rails project is either closed or has been deleted. Please open it or select another project inside the Ruby Explorer");
			}
		}
		else
		{
			MessageDialog.openError(getControl().getShell(), "No project selected",
					"Unable to get the currently selected Rails project inside the Ruby Explorer");
		}
	}

	private String getGenerateScript()
	{
		if (project == null)
		{
			return "script/generate";
		}
		IPath path = RailsPlugin.findRailsRoot(project);
		path = path.append("script");
		if (destroyButton.getSelection())
		{
			return path.append("destroy").toPortableString();
		}
		return path.append("generate").toPortableString();
	}

	private void launchTerminal(String title, final String file, final String args)
	{
		// launch the generator command
		try
		{
			ILaunchConfigurationWorkingCopy wc = RubyRuntime.createBasicLaunch(file, args, project);
			wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
			wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL, IRailsShellConstants.TERMINAL_ID);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_TERMINAL_COMMAND, "script/generate " + args);
			final ILaunchConfiguration config = wc.doSave();
			final String command = file + " " + args;
			Job job = new Job(command)
			{
				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					try
					{
						monitor.beginTask(command, 2);
						monitor.worked(1);
						ILaunch launch = config.launch(ILaunchManager.RUN_MODE, null);
						// spin the progress until the launch is complete
						while (!launch.isTerminated())
						{
							try
							{
								Thread.sleep(100);
							}
							catch (InterruptedException e)
							{
								// ignore
							}
						}
						monitor.worked(1);
						monitor.done();
					}
					catch (CoreException e)
					{
						RailsLog.logError("Error running generator", e);
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
		catch (CoreException e)
		{
			RailsLog.logError("Error running generator", e);
		}
	}

	/**
	 * Reload the list of generators in the combo box from the generators currently discovered in the
	 * GeneratorsLocatorManager without doing any reparsing
	 */
	private void reloadGeneratorsPullDown()
	{
		genCombo.removeAll();

		List<Generator> generators = GeneratorLocatorsManager.getInstance().getAllGenerators(project);
		for (Generator generator : generators)
		{
			genCombo.add(generator.getName());
		}
		if (genCombo.getItemCount() > 0)
			genCombo.setText(genCombo.getItem(0));

		genCombo.update();
	}

	/**
	 * Refreshes the generators
	 */
	public void refreshGenerators()
	{
		reloadGeneratorsPullDown();
	}

	/**
	 * @see org.eclipse.ui.part.Page#getControl()
	 */
	public Control getControl()
	{
		return generateView;
	}

	/**
	 * @see org.eclipse.ui.part.Page#setFocus()
	 */
	public void setFocus()
	{
		genCombo.setFocus();
	}

	boolean pulldownEmpty()
	{
		return genCombo != null && genCombo.getItemCount() == 0;
	}

	/**
	 * Sets control enablement
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled)
	{
		genCombo.setEnabled(enabled);
		paramText.setEnabled(enabled);
		createButton.setEnabled(enabled);
		destroyButton.setEnabled(enabled);
		pretendButton.setEnabled(enabled);
		skipButton.setEnabled(enabled);
		forceButton.setEnabled(enabled);
		quietButton.setEnabled(enabled);
		backtraceButton.setEnabled(enabled);
		helpButton.setEnabled(enabled);
		svnButton.setEnabled(enabled);
	}

	/**
	 * @see org.rubypeople.rdt.internal.ui.RubyExplorerTracker.IRubyProjectListener#projectSelected(org.eclipse.core.resources.IProject)
	 */
	public void projectSelected(IProject project)
	{
		if (projectNameLabel.isDisposed())
		{
			return;
		}
		if (RailsPlugin.hasRailsNature(project) && project.exists() && project.isOpen())
		{
			projectNameLabel.setText(PROJECT + project.getName());
			this.project = project;
			setEnabled(true);
		}
		else if (project == null || !project.exists())
		{
			projectNameLabel.setText(PROJECT + "<Select a Rails project>");
			if (!expandedLabel) // Only once should we expand this label
			{
				Point p = projectNameLabel.getSize();
				p.x += 100;
				projectNameLabel.setSize(p);
				projectNameLabel.redraw();
				expandedLabel = true;
			}
			setEnabled(false);
			this.project = null;
		}
		refreshGenerators();
	}

	String getSelectedGenerator()
	{
		return genCombo.getText();
	}

	IProject getProject()
	{
		return this.project;
	}

	/**
	 * Gets the help
	 * 
	 * @param generatorName
	 * @param project
	 */
	private static void getHelp(final String generatorName, final IProject project)
	{
		final String command = generatorName + " --help";
		final String version = RailsPlugin.getRailsVersion(project);
		final String uniqueName = generatorName + "_" + version;
		Job job = new Job("script/generate " + command)
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{

				try
				{
					String help = helps.get(uniqueName);
					if (help == null)
					{
						File file = RailsUIPlugin.getInstance().getStateLocation().append(uniqueName + "_help.txt")
								.toFile();
						String output = null;
						if (file.exists())
						{
							try
							{
								output = new String(Util.getFileCharContent(file, null));
							}
							catch (IOException e)
							{
								// ignore
							}
						}
						if (output == null)
						{
							IProject aProject = project;
							if (aProject == null)
							{
								if (RailsPlugin.getRailsProjects().size() == 0)
								{
									return new Status(Status.WARNING, RailsUIPlugin.getPluginIdentifier(), -1,
											"Please select a Rails project in the Ruby Explorer", null);
								}
								aProject = RailsPlugin.getRailsProjects().iterator().next();
							}
							ILaunchConfigurationWorkingCopy wc = RubyRuntime.createBasicLaunch("script/generate",
									command, aProject);
							output = RubyRuntime.launchInBackgroundAndRead(wc.doSave(), file);
						}
						helps.put(uniqueName, output);
						help = output;
					}
					final String toShow = help;
					Display.getDefault().asyncExec(new Runnable()
					{

						public void run()
						{
							MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Generators Help",
									toShow);
						}

					});

				}
				catch (IllegalStateException e)
				{
					return new Status(Status.ERROR, RailsUIPlugin.getPluginIdentifier(), -1, e.getMessage(), e);
				}
				catch (CoreException e)
				{
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

}
