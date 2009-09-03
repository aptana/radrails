/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.server.ui.dialogs;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.radrails.db.core.DatabaseManager;
import org.radrails.db.core.IDatabaseConstants;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.ui.RailsUIPlugin;
import org.radrails.server.core.IServerConstants;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.radrails.server.core.ServerPlugin;
import org.rubypeople.rdt.internal.ui.dialogs.StatusDialog;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.DialogField;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.StringDialogField;

import com.aptana.rdt.core.gems.Gem;

/**
 * Dialog to display server properties for editing.
 * 
 * @author mkent
 */
public class EditServerDialog extends StatusDialog implements IDialogFieldListener
{

	private String name;
	private String type;
	private String port;
	private String environment;
	private String host;

	private String currName;
	private String currType;
	private String currPort;
	private String currHost;
	private String currEnvironment;
	private boolean fAskForProject;
	private IProject project;

	private StringDialogField nameField;
	private StringDialogField hostField;
	private StringDialogField portField;
	private ComboDialogField environmentField;
	private ComboDialogField typeField;
	private Combo projectCombo;

	/**
	 * Constructor.
	 * 
	 * @param parentShell
	 *            parent shell of the dialog
	 * @param currName
	 *            current name of the server
	 * @param currType
	 *            current type of server
	 * @param currHost
	 *            current host/IP of the server
	 * @param currPort
	 *            current port of the server
	 * @param currEnvironment
	 *            current runtime environment of the server
	 */
	public EditServerDialog(Shell parentShell, String currName, String currType, String currHost, String currPort,
			String currEnvironment)
	{
		super(parentShell); // TODO Just take in a Server object!

		this.currName = currName;
		this.currType = currType;
		this.currHost = currHost;
		if (this.currHost == null)
		{
			this.currHost = Server.DEFAULT_RADRAILS_HOST;
		}
		this.currPort = String.valueOf(currPort);
		this.currEnvironment = currEnvironment;
		dontAskForProject();

		createFields();
	}

	private void createFields()
	{
		nameField = new StringDialogField();
		nameField.setLabelText("Name:");
		nameField.setText(currName);

		typeField = new ComboDialogField(SWT.DROP_DOWN | SWT.READ_ONLY);
		typeField.setLabelText("Type:");
		String[] types = new String[] { IServerConstants.TYPE_WEBRICK, IServerConstants.TYPE_MONGREL,
				IServerConstants.TYPE_LIGHTTPD };
		typeField.setItems(types);
		typeField.setText(currType);
		typeField.setDialogFieldListener(new IDialogFieldListener()
		{

			public void dialogFieldChanged(DialogField field)
			{
				ComboDialogField duh = (ComboDialogField) field;
				String text = duh.getText();
				if (text != null && text.equals(IServerConstants.TYPE_MONGREL))
				{
					if (!RailsPlugin.getInstance().getGemManager().gemInstalled("mongrel")
							&& RailsPlugin.getInstance().getMongrelPath() == null)
					{
						if (MessageDialog.openQuestion(getShell(), "Mongrel not installed",
								"Mongrel does not appear to be installed. Would you like to begin installing it?"))
						{
							getShell().close();
							Job job = new Job("Installing mongrel...")
							{
								@Override
								protected IStatus run(IProgressMonitor monitor)
								{
									return RailsPlugin.getInstance().getGemManager().installGem(
											new Gem("mongrel", Gem.ANY_VERSION, null), monitor);
								}
							};
							job.setUser(true);
							job.schedule();
						}
					}
				}
			}

		});

		hostField = new StringDialogField();
		hostField.setLabelText("Host:");
		hostField.setText(currHost);
		hostField.setDialogFieldListener(this);

		portField = new StringDialogField();
		portField.setLabelText("Port:");
		portField.setText(currPort);
		portField.setDialogFieldListener(this);

		environmentField = new ComboDialogField(SWT.DROP_DOWN | SWT.READ_ONLY);
		environmentField.setLabelText("Environment:");
		Set<String> environments = DatabaseManager.getEnvironments();
		environments.add(currEnvironment);
		environmentField.setItems(environments.toArray(new String[environments.size()]));
		environmentField.setText(currEnvironment);
	}

	public EditServerDialog(Shell shell)
	{
		this(shell, "webrick development server", IServerConstants.TYPE_WEBRICK, Server.DEFAULT_RADRAILS_HOST,
				ServerManager.getInstance().getNextAvailablePort(), IDatabaseConstants.ENV_DEVELOPMENT);
		fAskForProject = true;
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Server properties");
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent)
	{
		Composite control = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		control.setLayout(layout);

		if (fAskForProject)
		{
			Label projectLabel = new Label(control, SWT.LEFT);
			projectLabel.setText("Project:");

			projectCombo = new Combo(control, SWT.DROP_DOWN | SWT.READ_ONLY);
			Set<IProject> projects = RailsPlugin.getInstance().getRailsProjects();
			for (IProject project : projects)
			{
				projectCombo.add(project.getName());
			}
			IProject project = RailsUIPlugin.getInstance().getSelectedRailsProject();
			if (project != null)
			{
				projectCombo.setText(project.getName());
			}
			else if (projects != null && projects.size() > 0)
			{
				projectCombo.setText(projectCombo.getItem(0));
			}
			projectCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		nameField.doFillIntoGrid(control, 2);
		typeField.doFillIntoGrid(control, 2);
		hostField.doFillIntoGrid(control, 2);
		portField.doFillIntoGrid(control, 2);
		environmentField.doFillIntoGrid(control, 2);

		return control;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	public void buttonPressed(int buttonId)
	{
		if (buttonId == IDialogConstants.OK_ID)
		{
			name = nameField.getText();
			port = portField.getText();
			environment = environmentField.getText();
			type = typeField.getText();
			host = hostField.getText().trim();
			if (projectCombo != null)
			{
				String projectName = projectCombo.getText();
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
			okPressed();
		}
		else if (buttonId == IDialogConstants.CANCEL_ID)
		{
			cancelPressed();
		}
	}

	/**
	 * Return server name
	 * 
	 * @return name the name of the server
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Return server port
	 * 
	 * @return port the port the server runs on
	 */
	public String getPort()
	{
		return port;
	}

	/**
	 * Return server host
	 * 
	 * @return host the host/IP the server runs on
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Return server environment
	 * 
	 * @return environment the runtime environment of the server
	 */
	public String getEnvironment()
	{
		return environment;
	}

	/**
	 * Return server type
	 * 
	 * @return type the type(mongrel, webrick, lighttpd) of the server
	 */
	public String getType()
	{
		return type;
	}

	public IProject getProject()
	{
		return project;
	}

	public void dontAskForProject()
	{
		fAskForProject = false;
	}

	protected void validate()
	{
		String host = hostField.getText();
		if (host.trim().length() == 0)
		{
			updateStatus(new Status(Status.ERROR, ServerPlugin.PLUGIN_ID, -1, "Empty host", null));
			return;
		}

		String port = portField.getText();
		if (port.trim().length() == 0)
		{
			updateStatus(new Status(Status.ERROR, ServerPlugin.PLUGIN_ID, -1, "Empty port", null));
			return;
		}
		try
		{
			int portInt = Integer.parseInt(port);
			if (portInt < 1)
			{
				updateStatus(new Status(Status.ERROR, ServerPlugin.PLUGIN_ID, -1, "Negative integer port", null));
				return;
			}
		}
		catch (NumberFormatException e)
		{
			updateStatus(new Status(Status.ERROR, ServerPlugin.PLUGIN_ID, -1, "Non-integer port", e));
			return;
		}
		updateStatus(Status.OK_STATUS);
	}

	public void dialogFieldChanged(DialogField field)
	{
		validate();
	}
}
