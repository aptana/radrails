package org.radrails.server.internal.ui.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.radrails.server.core.launching.IRailsAppLaunchConfigurationConstants;
import org.radrails.server.internal.ui.ServerUILog;
import org.radrails.server.ui.dialogs.EditServerDialog;
import org.rubypeople.rdt.core.IRubyElement;

public class RailsAppLaunchShortcut implements ILaunchShortcut {

	public void launch(ISelection selection, String mode) {
		if ( selection instanceof IStructuredSelection ) {
			Object object = ((IStructuredSelection)selection).getFirstElement();
			if ( object instanceof IResource ) {
				launch(((IResource)object).getProject(), mode);
			} else if (object instanceof IAdaptable) {
				IAdaptable adapt = (IAdaptable) object;
				IResource resource = (IResource) adapt.getAdapter(IResource.class);
				launch(resource.getProject(), mode);
			}
		}
	}

	public void launch(IEditorPart editor, String mode) {
		// TODO see if we can get parent project from editor input
		IEditorInput input = editor.getEditorInput();
		if (input == null) {
			ServerUILog.logError("Could not retrieve input from editor: " + editor.getTitle(), new Exception());
			return;
		}
		IRubyElement rubyElement = (IRubyElement) input.getAdapter(IRubyElement.class);
		if (rubyElement == null) {
			if (input instanceof IFileEditorInput) {
				IFileEditorInput fileInput = (IFileEditorInput) input;
				IFile file = fileInput.getFile();
				launch(file.getProject(), mode);
				return;
			}
			ServerUILog.logError("Editor input is not a ruby file or external ruby file.", new Exception());
			return;
		}
		launch(rubyElement.getRubyProject().getProject(), mode);
	}

	private void launch(IProject project, String mode) {
		if (!RailsPlugin.hasRailsNature(project)) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Not a Rails project", "Selected project is not a Rails project");
			return;
		}
		ILaunchConfiguration config = findLaunchConfiguration(project, mode);
		// FIXME If user has deleted their server, we need to prompt to create one again!
		if (config != null) {
			DebugUITools.launch(config, mode);
		}
	}

	private ILaunchConfiguration findLaunchConfiguration(IProject project, String mode) {
		ILaunchConfigurationType configType = getLaunchConfigType();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
			for (int i= 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				if(config.getAttribute(IRailsAppLaunchConfigurationConstants.PROJECT_NAME, "").equals(project.getName())) {
					return config;
				}
			}
		} catch (CoreException e) {
			ServerUILog.logError("error finding launch configuration", e);
		}
		return createConfiguration(project);
	}

	private ILaunchConfiguration createConfiguration(IProject project) {
		ILaunchConfiguration config = null;
		ILaunchConfigurationType configType = getLaunchConfigType();
		try {
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
					DebugPlugin.getDefault().getLaunchManager()
					.generateUniqueLaunchConfigurationNameFrom(project.getName()));
			RailsAppLaunchHelper.setDefaults(wc);
			
			String serverName = wc.getAttribute(IRailsAppLaunchConfigurationConstants.SERVER_NAME, (String)null);
			if (serverName == null) {
//				 If there is no server, make one?
				boolean createServer = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), LaunchingMessages.RailsAppLaunch_noServerExistsDialog_title, LaunchingMessages.RailsAppLaunch_noServerExistsDialog_msg);
				if (createServer) {
					EditServerDialog dialog = new EditServerDialog(Display.getCurrent().getActiveShell());
					dialog.dontAskForProject();
					if (dialog.open() == Window.OK) {
						Server s = new Server(project, dialog.getName(), dialog.getType(), dialog.getHost(), dialog.getPort(), dialog.getEnvironment());
						ServerManager.getInstance().addServer(s);
						wc.setAttribute(IRailsAppLaunchConfigurationConstants.SERVER_NAME, s.getName());
					} else {
						// don't create a config!
						return null;
					}
				}				
			}			
			config = wc.doSave();
		} catch (CoreException e) {
			ServerUILog.logError("error creating launch configuration", e);
		}
		return config;
	}
	
	private ILaunchConfigurationType getLaunchConfigType() {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		return manager.getLaunchConfigurationType(IRailsAppLaunchConfigurationConstants.LAUNCH_TYPE_ID);
	}
	
}
