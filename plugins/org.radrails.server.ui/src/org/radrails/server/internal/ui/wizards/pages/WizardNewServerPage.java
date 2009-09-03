/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.server.internal.ui.wizards.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;
import org.radrails.rails.ui.wizards.NewProjectBasedResourceWizard;
import org.radrails.server.core.IServerConstants;
import org.radrails.server.core.ServerManager;
import org.radrails.server.internal.ui.wizards.NewServerWizard;
import org.rubypeople.rdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.rubypeople.rdt.internal.ui.viewsupport.DecoratingRubyLabelProvider;

/**
 * Main page for the NewMongrelServerWizard.
 * 
 * @author mbaumbach
 * 
 */
public class WizardNewServerPage extends WizardPage {

	private Text projectNameText;
	private Text serverNameText;
	private Text portText;
	private Combo serverTypeCombo;

	private ModifyListener nameModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			boolean valid = validatePage();
			setPageComplete(valid);
		}
	};

	/**
	 * Constructor.
	 * 
	 * @param pageName
	 */
	public WizardNewServerPage(String pageName) {
		super(pageName);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NULL);
		control.setLayout(new GridLayout());

		createInputControls(control);

		setControl(control);
		setPageComplete(validatePage());
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			if (projectNameText.getText().equals("")) {
				projectNameText.setFocus();
			} else if (serverNameText.getText().equals("")) {
				serverNameText.setFocus();
			} else {
				portText.setFocus();
			}
		}
	}

	/**
	 * Helper method to create the input controls.
	 * 
	 * @param parent
	 */
	private void createInputControls(Composite parent) {
		Composite control = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		control.setLayout(layout);
		control.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create the label for the project name input field
		Label projectLabel = new Label(control, SWT.LEFT);
		projectLabel.setText("Project:");

		// Create the project name input field
		projectNameText = new Text(control, SWT.BORDER);
		projectNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		NewServerWizard wizard = (NewServerWizard) getWizard();
		projectNameText.setText(wizard.getSelectedProjectName());
		projectNameText.addModifyListener(nameModifyListener);

		// Create the browse for project button
		Button projectSelectButton = new Button(control, SWT.PUSH);
		projectSelectButton.setText("Browse...");
		projectSelectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleProjectSelectButtonPressed();
			}
		});
		
		
		// Create the label for the server type combo box
		Label serverTypeLabel = new Label(control, SWT.LEFT);
		serverTypeLabel.setText("Type:");

		// Create the server type combo field
		serverTypeCombo = new Combo(control, SWT.DROP_DOWN);		
		serverTypeCombo.add(IServerConstants.TYPE_WEBRICK);
		serverTypeCombo.add(IServerConstants.TYPE_MONGREL);
		serverTypeCombo.add(IServerConstants.TYPE_LIGHTTPD);
		serverTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		serverTypeCombo.setText(IServerConstants.TYPE_WEBRICK);
		serverTypeCombo.addModifyListener(nameModifyListener);		

		// Blank
		new Label(control, SWT.NULL);
		
		// Create the label for the server name input field
		Label serverNameLabel = new Label(control, SWT.LEFT);
		serverNameLabel.setText("Name:");

		// Create the server name input field
		serverNameText = new Text(control, SWT.BORDER);
		serverNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		serverNameText.setText(wizard.getSelectedProjectName() + "Server");
		serverNameText.addModifyListener(nameModifyListener);

		// Blank
		new Label(control, SWT.NULL);

		// Create the label for the port number input field
		Label portNumberLabel = new Label(control, SWT.LEFT);
		portNumberLabel.setText("Port:");

		// Create the port number input field
		portText = new Text(control, SWT.BORDER);
		portText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		portText.setText(String.valueOf(ServerManager.getInstance()
				.getNextAvailablePort()));
		portText.addModifyListener(nameModifyListener);
	}

	/**
	 * Opens a list dialog populated with all the Rails projects in the
	 * workspace. The name of the selected project is placed in the text field
	 * when the dialog is closed.
	 */
	protected void handleProjectSelectButtonPressed() {
		// Create the dialog
		ListDialog dialog = new ListDialog(getShell());
		dialog.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// Do nothing
			}

			public void dispose() {
				// Do nothing
			}

			public Object[] getElements(Object inputElement) {
				return ((IWorkspaceRoot) inputElement).getProjects();
			}
		});
		dialog.setLabelProvider(new DecoratingRubyLabelProvider(new AppearanceAwareLabelProvider()));

		// Get the current project and select it
		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
		NewProjectBasedResourceWizard wizard = (NewProjectBasedResourceWizard) getWizard();
		dialog.setInitialSelections(new IProject[] { wizard
				.getProjectForName(getProjectName()) });

		// Set up the rest of the dialog
		dialog.setInput(wsroot);
		dialog.setMessage("Choose a project:");
		dialog.setTitle("Project Selection");
		dialog.create();
		dialog.open();

		// Get the result and put it in the text field
		Object[] result = dialog.getResult();
		if (result != null) {
			IProject project = ((IProject) result[0]);
			projectNameText.setText(project.getName());
		}
	}

	/**
	 * @return the project name from the input field
	 */
	public String getProjectName() {
		return projectNameText.getText();
	}

	/**
	 * @return the server name from the input field
	 */
	public String getServerName() {
		return serverNameText.getText();
	}

	/**
	 * @return the port number from the input field
	 */
	public String getPort() {
		return portText.getText();
	}

	/**
	 * @return the server type from the combo box
	 */
	public String getServerType() {
		return serverTypeCombo.getText();
	}
	
	/**
	 * Validates a page
	 * 
	 * @return true if page is valid
	 */
	private boolean validatePage() {
		setMessage(null);
		setErrorMessage(null);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		if (getProjectName().equals("")) {
			setErrorMessage("Project name empty");
			return false;
		}

		if (!workspace.validateName(getProjectName(), IResource.PROJECT).isOK()) {
			setErrorMessage("Invalid project name");
			return false;
		}

		if (!ResourcesPlugin.getWorkspace().getRoot().getProject(
				getProjectName()).exists()) {
			setErrorMessage("Project does not exist");
			return false;
		}	

		if (getServerName().equals("")) {
			setErrorMessage("Server name empty");
			return false;
		}

		if (portText.getText().equals("")) {
			setErrorMessage("Port number empty");
			return false;
		}

		if (ServerManager.getInstance().portInUse(getPort())) {
			setMessage("Port number already in use", WARNING);
		}
		
		if (ServerManager.getInstance().projectHasServer(getProjectName(), getServerType())) {
			setMessage("Project already has a " + getServerType() + " server", WARNING);
		}
		return true;
	}

}
