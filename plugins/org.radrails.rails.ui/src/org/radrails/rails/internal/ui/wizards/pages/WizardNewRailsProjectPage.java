/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.wizards.pages;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.progress.UIJob;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.RailsInstallDialog;
import org.radrails.rails.internal.ui.wizards.NewRailsProjectWizard;
import org.radrails.rails.internal.ui.wizards.WizardMessages;
import org.radrails.rails.ui.RailsUIPlugin;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.rdt.core.gems.IGemManager;
import com.aptana.rdt.core.gems.Version;

/**
 * Main dialog page for {@link NewRailsProjectWizard NewRailsProjectWizard}.
 * 
 * @author mkent
 * @author cwilliams
 */
public class WizardNewRailsProjectPage extends WizardNewProjectCreationPage
{

	private static final String LATEST = WizardMessages.WizardNewRailsProjectPage_Latest_rails_version_label;
	private Button generateButton;
	private Button fStartServerButton;
	private Combo dbCombo;
	private Combo versionCombo;
	private Link installRails;
	private Cursor hand;

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
		super.createControl(parent);

		Composite composite = (Composite) super.getControl();
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
				dbCombo.setEnabled(generateButton.getSelection());
				versionCombo.setEnabled(generateButton.getSelection());
				validatePage();
			}

		});

		// Create check box for auto starting server
		fStartServerButton = new Button(optionsGroup, SWT.CHECK);
		fStartServerButton.setText(WizardMessages.WizardNewRailsProjectPage_Start_server_label);
		fStartServerButton.setSelection(true);

		validatePage();
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

	@Override
	protected boolean validatePage()
	{
		boolean result = super.validatePage();
		if (!result)
			return false;
		String projectName = getProjectName();
		if (projectName != null) {
			if (projectName.contains("'") || projectName.contains("\"")) {
				setMessage(WizardMessages.WizardNewRailsProjectPage_Invalid_Project_Name_msg, IMessageProvider.ERROR);
				return false;
			}
		}
		if (getGenerateButtonSelection() && railsNotInstalled())
		{
			setMessage(WizardMessages.WizardNewRailsProjectPage_Rails_not_installed_msg, IMessageProvider.WARNING);
		}
		return true;
	}

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
}