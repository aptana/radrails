package org.radrails.server.internal.ui.launching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.radrails.server.core.launching.IRailsAppLaunchConfigurationConstants;
import org.radrails.server.internal.ui.ServerUILog;

public class MainSettingsTab extends AbstractLaunchConfigurationTab {

	private Combo fServerCombo;
	private Combo fProjectCombo;
	private Listener fDirtyListener;
	private Text fBrowserText;
	private Text fActionText;
	private Button fNoBrowserRadio;
	private Button fInternalBrowserRadio;
	private Button fExternalBrowserRadio;
	private Button fBrowserTextBrowse;
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(composite);
		
		createSelectProjectArea(composite);
		createSelectServerArea(composite);
		createStartActionArea(composite);
		createBrowserArea(composite);
		
		fBrowserTextBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
				fileDialog.setFileName(fBrowserText.getText());
				String text = fileDialog.open();
				if (text != null) {
					fBrowserText.setText(text);
				}
			}
		});
		
		fProjectCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				loadServersCombo();
			}
		});
		fNoBrowserRadio.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				enableExternalBrowserWidgets(false);
				enableActionWidgets(false);
			}
		});
		fExternalBrowserRadio.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				enableExternalBrowserWidgets(true);
				enableActionWidgets(true);
			}
		});
		fInternalBrowserRadio.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				enableExternalBrowserWidgets(false);
				enableActionWidgets(true);
			}
		});
		
		fDirtyListener = new Listener(){
			public void handleEvent(Event event) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}			
		};
		hookListeners(true);
	}
	
	private void createSelectProjectArea(Composite parent) {
		Group projectGroup = new Group(parent, SWT.NONE);
		projectGroup.setText("Rails Application");
		
		GridData gd = new GridData(SWT.FILL, 20, true, false);
		projectGroup.setLayoutData(gd);
		
		FormLayout form = new FormLayout();
		projectGroup.setLayout(form);
		FormData data;
		form.marginTop = 10;
		form.marginBottom = 10;
		form.marginLeft = 10;
		form.marginRight = 10;
		
		int column1Offset = 0;
		int column2Offset = 120;
		
		Label appLabel = new Label(projectGroup, SWT.NONE);
		appLabel.setText("Application root:");
		data = new FormData();
		data.left = new FormAttachment(0, column1Offset);
		appLabel.setLayoutData(data);
		
		fProjectCombo = new Combo(projectGroup, SWT.DROP_DOWN);
		data = new FormData();
		data.top = new FormAttachment(appLabel, 0, SWT.TOP);
		data.left = new FormAttachment(0, column2Offset);
		data.width = 200;
		fProjectCombo.setLayoutData(data);
		fProjectCombo.setItems(getRailsProjectNames());
	}
	
	private static String[] getRailsProjectNames() {
		Set<IProject> projects = RailsPlugin.getRailsProjects();
		Collection<String> ret = new ArrayList<String>();
		for (IProject project : projects) {
			if (project.isAccessible())
				ret.add(project.getName());
		}
		return (String[]) ret.toArray(new String[ret.size()]);
	}
	
	private void createSelectServerArea(Composite parent) {
		Group serverGroup = new Group(parent, SWT.NONE);
		serverGroup.setText("Web Server");
		
		GridData gd = new GridData(SWT.FILL, 20, true, false);
		serverGroup.setLayoutData(gd);
		
		FormLayout form = new FormLayout();
		serverGroup.setLayout(form);
		FormData data;
		form.marginTop = 10;
		form.marginBottom = 10;
		form.marginLeft = 10;
		form.marginRight = 10;
		
		int column1Offset = 0;
		int column2Offset = 120;
		
		Label serverLabel = new Label(serverGroup, SWT.NONE);
		serverLabel.setText("Server:");
		data = new FormData();
		data.left = new FormAttachment(0, column1Offset);
		serverLabel.setLayoutData(data);
		
		fServerCombo = new Combo(serverGroup, SWT.DROP_DOWN);
		data = new FormData();
		data.top = new FormAttachment(serverLabel, 0, SWT.TOP);
		data.left = new FormAttachment(0, column2Offset);
		data.width = 200;
		fServerCombo.setLayoutData(data);
	}
	
	private void createStartActionArea(Composite parent) {
		Group actionGroup = new Group(parent, SWT.NONE);
		actionGroup.setText("Start Action");
		
		GridData gd = new GridData(SWT.FILL, 20, true, false);
		actionGroup.setLayoutData(gd);
		
		FormLayout form = new FormLayout();
		actionGroup.setLayout(form);
		FormData data;
		form.marginTop = 10;
		form.marginBottom = 10;
		form.marginLeft = 10;
		form.marginRight = 10;
		
		int column1Offset = 0;
		int column2Offset = 120;
		
		Label actionLabel = new Label(actionGroup, SWT.NONE);
		actionLabel.setText("Action URL:");
		data = new FormData();
		data.left = new FormAttachment(0, column1Offset);
		actionLabel.setLayoutData(data);
		
		fActionText = new Text(actionGroup, SWT.LEFT | SWT.BORDER);
		data = new FormData();
		data.top = new FormAttachment(actionLabel, 0, SWT.TOP);
		data.left = new FormAttachment(0, column2Offset);
		data.width = 200;
		fActionText.setLayoutData(data);
	}
	
	private void createBrowserArea(Composite parent) {
		Group browserGroup = new Group(parent, SWT.NONE);
		browserGroup.setText("Web Browser");
		
		GridData gd = new GridData(SWT.FILL, 20, true, false);
		browserGroup.setLayoutData(gd);
		
		FormLayout form = new FormLayout();
		browserGroup.setLayout(form);
		FormData data;
		form.marginTop = 10;
		form.marginBottom = 10;
		form.marginLeft = 10;
		form.marginRight = 10;
		
		int column1Offset = 0;
		
		fNoBrowserRadio = new Button(browserGroup, SWT.RADIO);
		fNoBrowserRadio.setText("Do not launch browser");
		data = new FormData();
		data.left = new FormAttachment(0, column1Offset);
		fNoBrowserRadio.setLayoutData(data);
		
		fInternalBrowserRadio = new Button(browserGroup, SWT.RADIO);
		fInternalBrowserRadio.setText("Use internal browser");
		data = new FormData();
		data.top = new FormAttachment(fNoBrowserRadio, 10, SWT.BOTTOM);
		data.left = new FormAttachment(0, column1Offset);
		fInternalBrowserRadio.setLayoutData(data);
		
		fExternalBrowserRadio = new Button(browserGroup, SWT.RADIO);
		fExternalBrowserRadio.setText("Use external browser");
		data = new FormData();
		data.top = new FormAttachment(fInternalBrowserRadio, 10, SWT.BOTTOM);
		data.left = new FormAttachment(0, column1Offset);
		fExternalBrowserRadio.setLayoutData(data);
		
		
		fBrowserText = new Text(browserGroup, SWT.LEFT | SWT.BORDER);
		data = new FormData();
		data.left = new FormAttachment(fExternalBrowserRadio, 10, SWT.BOTTOM);
		data.top = new FormAttachment(fInternalBrowserRadio, 5, SWT.BOTTOM);
		data.width = 250;
		fBrowserText.setLayoutData(data);
		
		fBrowserTextBrowse = new Button(browserGroup, SWT.PUSH);
		fBrowserTextBrowse.setText("Browse...");
		data = new FormData();
		data.top = new FormAttachment(fInternalBrowserRadio, 5, SWT.BOTTOM);
		data.left = new FormAttachment(fBrowserText, 8, SWT.RIGHT);
		fBrowserTextBrowse.setLayoutData(data);
	}
	
	private void enableExternalBrowserWidgets(boolean enabled) {
		fBrowserText.setEnabled(enabled);
		fBrowserTextBrowse.setEnabled(enabled);
	}
	
	private void enableActionWidgets(boolean enabled) {
		fActionText.setEnabled(enabled);
	}
	
	private void hookListeners(boolean hook) {
		if ( hook ) {
			fProjectCombo.addListener(SWT.Modify, fDirtyListener);
			fServerCombo.addListener(SWT.Modify, fDirtyListener);
			fNoBrowserRadio.addListener(SWT.Selection, fDirtyListener);
			fInternalBrowserRadio.addListener(SWT.Selection, fDirtyListener);
			fExternalBrowserRadio.addListener(SWT.Selection, fDirtyListener);
			fBrowserText.addListener(SWT.Modify, fDirtyListener);
			fActionText.addListener(SWT.Modify, fDirtyListener);
		} else {
			fProjectCombo.removeListener(SWT.Modify, fDirtyListener);
			fServerCombo.removeListener(SWT.Modify, fDirtyListener);
			fNoBrowserRadio.removeListener(SWT.Selection, fDirtyListener);
			fInternalBrowserRadio.removeListener(SWT.Selection, fDirtyListener);
			fExternalBrowserRadio.removeListener(SWT.Selection, fDirtyListener);
			fBrowserText.removeListener(SWT.Modify, fDirtyListener);
			fActionText.removeListener(SWT.Modify, fDirtyListener);
		}
	}
	
	private void loadServersCombo() {
		int index = fProjectCombo.getSelectionIndex();
		if (index == -1) return;
		String projectName = fProjectCombo.getItem(index);
		String[] serverNames = getServerNamesForProject(projectName);
		fServerCombo.setItems(serverNames);
		if(serverNames.length > 0) {
			fServerCombo.setText(serverNames[0]);
		}
	}
	
	private String[] getServerNamesForProject(String name) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		Collection<Server> servers = ServerManager.getInstance().getServersForProject(project);
		Collection<String> ret = new ArrayList<String>();
		for (Server server : servers) {
			ret.add(server.getName());
		}
		return (String[]) ret.toArray(new String[ret.size()]);
	}

	public String getName() {
		return "Main";
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		hookListeners(false);
		// initialize radios
		fNoBrowserRadio.setSelection(false);
		fInternalBrowserRadio.setSelection(false);
		fExternalBrowserRadio.setSelection(false);
		// initialize text fields
		fActionText.setText("");
		fBrowserText.setText("");
		
		try {
			fProjectCombo.select(fProjectCombo.indexOf(configuration.getAttribute(IRailsAppLaunchConfigurationConstants.PROJECT_NAME, "")));
			loadServersCombo();
			fServerCombo.select(fServerCombo.indexOf(configuration.getAttribute(IRailsAppLaunchConfigurationConstants.SERVER_NAME, "")));
			if(configuration.getAttribute(IRailsAppLaunchConfigurationConstants.LAUNCH_BROWSER, false)) {
				fActionText.setText(configuration.getAttribute(IRailsAppLaunchConfigurationConstants.ACTION_PATH, ""));
				if(configuration.getAttribute(IRailsAppLaunchConfigurationConstants.USE_INTERNAL_BROWSER, false)) {
					fInternalBrowserRadio.setSelection(true);
					enableExternalBrowserWidgets(false);
					enableActionWidgets(true);
				} else if(configuration.getAttribute(IRailsAppLaunchConfigurationConstants.USE_EXTERNAL_BROWSER, false)) {
					fExternalBrowserRadio.setSelection(true);
					fBrowserText.setText(configuration.getAttribute(IRailsAppLaunchConfigurationConstants.BROWSER_EXE, ""));
					enableExternalBrowserWidgets(true);
					enableActionWidgets(true);
				}
			} else {
				fNoBrowserRadio.setSelection(true);
				enableExternalBrowserWidgets(false);
				enableActionWidgets(false);
			}
		} catch (CoreException e) {
			ServerUILog.logError("error accessing default attributes", e);
		} finally {
			hookListeners(true);
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if(fProjectCombo.getSelectionIndex() >= 0) {
			configuration.setAttribute(IRailsAppLaunchConfigurationConstants.PROJECT_NAME, fProjectCombo.getItem(fProjectCombo.getSelectionIndex()));
		}
		if(fServerCombo.getSelectionIndex() >=0) {
			configuration.setAttribute(IRailsAppLaunchConfigurationConstants.SERVER_NAME, fServerCombo.getItem(fServerCombo.getSelectionIndex()));
		}
		if(fNoBrowserRadio.getSelection()) {
			configuration.setAttribute(IRailsAppLaunchConfigurationConstants.LAUNCH_BROWSER, false);
		} else {
			configuration.setAttribute(IRailsAppLaunchConfigurationConstants.LAUNCH_BROWSER, true);
			configuration.setAttribute(IRailsAppLaunchConfigurationConstants.ACTION_PATH, fActionText.getText());
			if(fInternalBrowserRadio.getSelection()) {
				configuration.setAttribute(IRailsAppLaunchConfigurationConstants.USE_INTERNAL_BROWSER, true);
				configuration.setAttribute(IRailsAppLaunchConfigurationConstants.USE_EXTERNAL_BROWSER, false);
			} else if(fExternalBrowserRadio.getSelection()) {
				configuration.setAttribute(IRailsAppLaunchConfigurationConstants.USE_INTERNAL_BROWSER, false);
				configuration.setAttribute(IRailsAppLaunchConfigurationConstants.USE_EXTERNAL_BROWSER, true);
				configuration.setAttribute(IRailsAppLaunchConfigurationConstants.BROWSER_EXE, fBrowserText.getText());
			}
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		RailsAppLaunchHelper.setDefaults(configuration);
	}

}
