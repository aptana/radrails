/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.wizards.pages;

import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.progress.UIJob;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.RailsInstallDialog;
import org.radrails.rails.internal.ui.wizards.NewRailsProjectWizard;
import org.radrails.rails.internal.ui.wizards.WizardMessages;
import org.radrails.rails.ui.RailsUIPlugin;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.wizards.NewWizardMessages;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.DialogField;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.rubypeople.rdt.launching.RubyRuntime;
import org.rubypeople.rdt.ui.RubyUI;

import com.aptana.rdt.core.gems.IGemManager;
import com.aptana.rdt.core.gems.Version;

/**
 * Main dialog page for {@link NewRailsProjectWizard NewRailsProjectWizard}.
 * 
 * @author mkent
 * @author cwilliams
 */
public class WizardNewRailsProjectPage extends WizardPage
{

	private static final String LATEST = WizardMessages.WizardNewRailsProjectPage_Latest_rails_version_label;

	private NameGroup fNameGroup;
	private LocationGroup fLocationGroup;

	private Button generateButton;
	private Button fStartServerButton;
	private Combo dbCombo;
	private Combo versionCombo;
	private Link installRails;
	private Cursor hand;

	private org.radrails.rails.internal.ui.wizards.pages.WizardNewRailsProjectPage.Validator fValidator;

	/**
	 * Constructor.
	 * 
	 * @param pageName
	 *            the name of the wizard page
	 */
	public WizardNewRailsProjectPage(String pageName)
	{
		super(pageName);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent)
	{
		initializeDialogUnits(parent);

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// create UI elements
		fNameGroup = new NameGroup(composite, "");
		fLocationGroup = new LocationGroup(composite);

		// establish connections
		fNameGroup.addObserver(fLocationGroup);
		// FIXME When the location is "use existing source" then don't enable generation (or force it off and make user
		// re-check it)!

		// initialize all elements
		fNameGroup.notifyObservers();

		// create and connect validator
		fValidator = new Validator();
		fNameGroup.addObserver(fValidator);
		fLocationGroup.addObserver(fValidator);

		createOptionsGroup(composite);
		setControl(composite);
	}

	/**
	 * Helper method to create the options widgets.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private void createOptionsGroup(Composite parent)
	{
		// Create the group
		Group optionsGroup = new Group(parent, SWT.NONE);
		optionsGroup.setLayout(new GridLayout());
		optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		optionsGroup.setText(WizardMessages.WizardNewRailsProjectPage_Options_group_heading);

		// Create the check box
		Composite generate = new Composite(optionsGroup, SWT.NULL);
		GridLayout generateLayout = new GridLayout();
		generateLayout.numColumns = 2;
		generate.setLayout(generateLayout);
		generate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		generateButton = new Button(generate, SWT.CHECK);
		generateButton.setText(WizardMessages.WizardNewRailsProjectPage_Generate_project_skeleton);
		generateButton.setSelection(true);
		fLocationGroup.addObserver(new Observer()
		{

			public void update(Observable o, Object arg)
			{
				if (!fLocationGroup.isInWorkspace())
				{
					generateButton.setSelection(false);
					generateSelected(false);
				}
			}
		});

		hand = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);

		installRails = new Link(generate, SWT.NONE);
		installRails.setText(WizardMessages.WizardNewRailsProjectPage_Install_rails_link);
		installRails.setEnabled(true);
		installRails.setVisible(railsNotInstalled());
		installRails.setCursor(hand); // FIXME For me the hand appears when I hover about 10px below the link!
		installRails.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// Pop open a dialog to install rails!
				UIJob dialog = new RailsInstallDialog(RailsUIPlugin.getInstance().getGemManager());
				dialog.schedule();
			}
		});

		Composite db = new Composite(optionsGroup, SWT.NULL);
		GridLayout dbLayout = new GridLayout();
		dbLayout.numColumns = 2;
		db.setLayout(dbLayout);
		db.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(db, SWT.LEFT);
		label.setText(WizardMessages.WizardNewRailsProjectPage_Database_options_label);

		dbCombo = new Combo(db, SWT.DROP_DOWN | SWT.READ_ONLY);
		List<String> dbNames = RailsPlugin.getEligibleDatabaseNamesforCurrentVM();
		for (String dbName : dbNames)
		{
			dbCombo.add(dbName);
		}
		if (RubyRuntime.currentVMIsJRuby())
		{
			dbCombo.setText("derby");
		}
		else
		{
			dbCombo.setText("sqlite3");
		}
		Composite version = new Composite(optionsGroup, SWT.NULL);
		GridLayout versionLayout = new GridLayout();
		versionLayout.numColumns = 2;
		version.setLayout(versionLayout);
		version.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label versionLabel = new Label(version, SWT.LEFT);
		versionLabel.setText(WizardMessages.WizardNewRailsProjectPage_Rails_version_label);

		versionCombo = new Combo(version, SWT.DROP_DOWN | SWT.READ_ONLY);
		IGemManager gemManager = RailsUIPlugin.getInstance().getGemManager();

		List<Version> versions = gemManager.getVersions("rails");
		versionCombo.add(LATEST);
		for (Version version1 : versions)
		{
			versionCombo.add(version1.toString());
		}
		versionCombo.setText(LATEST);

		generateButton.addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				super.widgetSelected(e);
				generateSelected(generateButton.getSelection());
			}

		});

		// Create check box for auto starting server
		fStartServerButton = new Button(optionsGroup, SWT.CHECK);
		fStartServerButton.setText(WizardMessages.WizardNewRailsProjectPage_Start_server_label);
		fStartServerButton.setSelection(true);

		validatePage();
	}

	private void validatePage()
	{
		fValidator.update(null, null);
	}

	/**
	 * Creates a project resource handle for the current project name field value.
	 * <p>
	 * This method does not create the project resource; this is the responsibility of <code>IProject::create</code>
	 * invoked by the new project resource wizard.
	 * </p>
	 * 
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle()
	{
		return ResourcesPlugin.getWorkspace().getRoot().getProject(fNameGroup.getName());
	}

	/**
	 * @return the selection status of the generate rails skeleton option button
	 */
	public boolean getGenerateButtonSelection()
	{
		if (generateButton == null)
			return false;
		return generateButton.getSelection();
	}

	public String getDatabaseType()
	{
		if (dbCombo == null)
			return "";
		return dbCombo.getText();
	}

	public String getRailsVersion()
	{
		String version = versionCombo.getText();
		if (version.equals(LATEST))
			return null;
		return version;
	}

	public boolean startServer()
	{
		if (fStartServerButton == null)
			return false;
		return fStartServerButton.getSelection();
	}

	//
	// @Override
	// protected boolean validatePage()
	// {
	// boolean result = super.validatePage();
	// if (!result)
	// return false;
	// String projectName = getProjectName();
	// if (projectName != null) {
	// if (projectName.contains("'") || projectName.contains("\"")) {
	// setMessage(WizardMessages.WizardNewRailsProjectPage_Invalid_Project_Name_msg, IMessageProvider.ERROR);
	// return false;
	// }
	// }
	// if (getGenerateButtonSelection() && railsNotInstalled())
	// {
	// setMessage(WizardMessages.WizardNewRailsProjectPage_Rails_not_installed_msg, IMessageProvider.WARNING);
	// }
	// return true;
	// }

	private boolean railsNotInstalled()
	{
		return RailsPlugin.getInstance().getRailsPath() == null;
	}

	@Override
	public void dispose()
	{
		hand.dispose();
		super.dispose();
	}

	/**
	 * Request a location. Fires an event whenever the checkbox or the location field is changed, regardless of whether
	 * the change originates from the user or has been invoked programmatically.
	 */
	private final class LocationGroup extends Observable implements Observer, IStringButtonAdapter,
			IDialogFieldListener
	{

		protected final SelectionButtonDialogField fWorkspaceRadio;
		protected final SelectionButtonDialogField fExternalRadio;
		protected final StringButtonDialogField fLocation;

		private String fPreviousExternalLocation;

		private static final String DIALOGSTORE_LAST_EXTERNAL_LOC = RubyUI.ID_PLUGIN + ".last.external.project"; //$NON-NLS-1$

		public LocationGroup(Composite composite)
		{

			final int numColumns = 3;

			final Group group = new Group(composite, SWT.NONE);
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			group.setLayout(initGridLayout(new GridLayout(numColumns, false), true));
			group.setText(NewWizardMessages.RubyProjectWizardFirstPage_LocationGroup_title);

			fWorkspaceRadio = new SelectionButtonDialogField(SWT.RADIO);
			fWorkspaceRadio.setDialogFieldListener(this);
			fWorkspaceRadio.setLabelText(NewWizardMessages.RubyProjectWizardFirstPage_LocationGroup_workspace_desc);

			fExternalRadio = new SelectionButtonDialogField(SWT.RADIO);
			fExternalRadio.setLabelText(NewWizardMessages.RubyProjectWizardFirstPage_LocationGroup_external_desc);

			fLocation = new StringButtonDialogField(this);
			fLocation.setDialogFieldListener(this);
			fLocation.setLabelText(NewWizardMessages.RubyProjectWizardFirstPage_LocationGroup_locationLabel_desc);
			fLocation.setButtonLabel(NewWizardMessages.RubyProjectWizardFirstPage_LocationGroup_browseButton_desc);

			fExternalRadio.attachDialogField(fLocation);

			fWorkspaceRadio.setSelection(true);
			fExternalRadio.setSelection(false);

			fPreviousExternalLocation = ""; //$NON-NLS-1$

			fWorkspaceRadio.doFillIntoGrid(group, numColumns);
			fExternalRadio.doFillIntoGrid(group, numColumns);
			fLocation.doFillIntoGrid(group, numColumns);
			LayoutUtil.setHorizontalGrabbing(fLocation.getTextControl(null));
		}

		protected void fireEvent()
		{
			setChanged();
			notifyObservers();
		}

		protected String getDefaultPath(String name)
		{
			final IPath path = Platform.getLocation().append(name);
			return path.toOSString();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
		public void update(Observable o, Object arg)
		{
			if (isInWorkspace())
			{
				fLocation.setText(getDefaultPath(fNameGroup.getName()));
			}
			fireEvent();
		}

		public IPath getLocation()
		{
			if (isInWorkspace())
			{
				return Platform.getLocation();
			}
			return Path.fromOSString(fLocation.getText().trim());
		}

		public boolean isInWorkspace()
		{
			return fWorkspaceRadio.isSelected();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter#changeControlPressed(org.eclipse.jdt
		 * .internal.ui.wizards.dialogfields.DialogField)
		 */
		public void changeControlPressed(DialogField field)
		{
			final DirectoryDialog dialog = new DirectoryDialog(getShell());
			dialog.setMessage(NewWizardMessages.RubyProjectWizardFirstPage_directory_message);
			String directoryName = fLocation.getText().trim();
			if (directoryName.length() == 0)
			{
				String prevLocation = RubyPlugin.getDefault().getDialogSettings().get(DIALOGSTORE_LAST_EXTERNAL_LOC);
				if (prevLocation != null)
				{
					directoryName = prevLocation;
				}
			}

			if (directoryName.length() > 0)
			{
				final File path = new File(directoryName);
				if (path.exists())
					dialog.setFilterPath(directoryName);
			}
			final String selectedDirectory = dialog.open();
			if (selectedDirectory != null)
			{
				fLocation.setText(selectedDirectory);
				RubyPlugin.getDefault().getDialogSettings().put(DIALOGSTORE_LAST_EXTERNAL_LOC, selectedDirectory);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener#dialogFieldChanged(org.eclipse.jdt.
		 * internal.ui.wizards.dialogfields.DialogField)
		 */
		public void dialogFieldChanged(DialogField field)
		{
			if (field == fWorkspaceRadio)
			{
				final boolean checked = fWorkspaceRadio.isSelected();
				if (checked)
				{
					fPreviousExternalLocation = fLocation.getText();
					fLocation.setText(getDefaultPath(fNameGroup.getName()));
				}
				else
				{
					fLocation.setText(fPreviousExternalLocation);
				}
			}
			fireEvent();
		}
	}

	/**
	 * Request a project name. Fires an event whenever the text field is changed, regardless of its content.
	 */
	private final class NameGroup extends Observable implements IDialogFieldListener
	{

		protected final StringDialogField fNameField;

		public NameGroup(Composite composite, String initialName)
		{
			final Composite nameComposite = new Composite(composite, SWT.NONE);
			nameComposite.setFont(composite.getFont());
			nameComposite.setLayout(initGridLayout(new GridLayout(2, false), false));
			nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// text field for project name
			fNameField = new StringDialogField();
			fNameField.setLabelText(NewWizardMessages.RubyProjectWizardFirstPage_NameGroup_label_text);
			fNameField.setDialogFieldListener(this);

			setName(initialName);

			fNameField.doFillIntoGrid(nameComposite, 2);
			LayoutUtil.setHorizontalGrabbing(fNameField.getTextControl(null));
		}

		protected void fireEvent()
		{
			setChanged();
			notifyObservers();
		}

		public String getName()
		{
			return fNameField.getText().trim();
		}

		public void postSetFocus()
		{
			fNameField.postSetFocusOnDialogField(getShell().getDisplay());
		}

		public void setName(String name)
		{
			fNameField.setText(name);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener#dialogFieldChanged(org.eclipse.jdt.
		 * internal.ui.wizards.dialogfields.DialogField)
		 */
		public void dialogFieldChanged(DialogField field)
		{
			fireEvent();
		}

	}

	/**
	 * Validate this page and show appropriate warnings and error NewWizardMessages.
	 */
	private final class Validator implements Observer
	{

		public void update(Observable o, Object arg)
		{

			final IWorkspace workspace = RubyPlugin.getWorkspace();

			final String name = fNameGroup.getName();

			// check whether the project name field is empty
			if (name.length() == 0)
			{
				setErrorMessage(null);
				setMessage(NewWizardMessages.RubyProjectWizardFirstPage_Message_enterProjectName);
				setPageComplete(false);
				return;
			}

			// check whether the project name is valid
			final IStatus nameStatus = workspace.validateName(name, IResource.PROJECT);
			if (!nameStatus.isOK())
			{
				setErrorMessage(nameStatus.getMessage());
				setPageComplete(false);
				return;
			}

			// check whether project already exists
			final IProject handle = getProjectHandle();
			if (handle.exists())
			{
				setErrorMessage(NewWizardMessages.RubyProjectWizardFirstPage_Message_projectAlreadyExists);
				setPageComplete(false);
				return;
			}

			final String location = fLocationGroup.getLocation().toOSString();

			// check whether location is empty
			if (location.length() == 0)
			{
				setErrorMessage(null);
				setMessage(NewWizardMessages.RubyProjectWizardFirstPage_Message_enterLocation);
				setPageComplete(false);
				return;
			}

			// check whether the location is a syntactically correct path
			if (!Path.EMPTY.isValidPath(location))
			{
				setErrorMessage(NewWizardMessages.RubyProjectWizardFirstPage_Message_invalidDirectory);
				setPageComplete(false);
				return;
			}

			// check whether the location has the workspace as prefix
			IPath projectPath = Path.fromOSString(location);
			if (!fLocationGroup.isInWorkspace() && Platform.getLocation().isPrefixOf(projectPath))
			{
				setErrorMessage(NewWizardMessages.RubyProjectWizardFirstPage_Message_cannotCreateInWorkspace);
				setPageComplete(false);
				return;
			}

			// If we do not place the contents in the workspace validate the
			// location.
			if (!fLocationGroup.isInWorkspace())
			{
				final IStatus locationStatus = workspace.validateProjectLocation(handle, projectPath);
				if (!locationStatus.isOK())
				{
					setErrorMessage(locationStatus.getMessage());
					setPageComplete(false);
					return;
				}
			}

			setPageComplete(true);

			setErrorMessage(null);
			setMessage(null);
		}

	}

	/**
	 * Initialize a grid layout with the default Dialog settings.
	 */
	protected GridLayout initGridLayout(GridLayout layout, boolean margins)
	{
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		if (margins)
		{
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		}
		else
		{
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		return layout;
	}

	/**
	 * Returns the current project location path as entered by the user, or its anticipated initial value. Note that if
	 * the default has been returned the path in a project description used to create a project should not be set.
	 * <p>
	 * TODO At some point this method has to be converted to return an URI instead of an path. However, this first
	 * requires support from Platform/UI to specify a project location different than in a local file system. FIXME
	 * Check out ProjectContentsLocationArea for URI based implementation!
	 * </p>
	 * 
	 * @return the project location path or its anticipated initial value.
	 */
	public IPath getLocationPath()
	{
		return fLocationGroup.getLocation();
	}

	protected void generateSelected(boolean selected)
	{
		dbCombo.setEnabled(selected);
		versionCombo.setEnabled(selected);
		validatePage();
	}
}