package com.aptana.rdt.internal.profiling;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.rubypeople.rdt.internal.launching.LaunchingMessages;
import org.rubypeople.rdt.internal.launching.StandardVMRunner;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.VMRunnerConfiguration;

import com.aptana.rdt.profiling.IProfileUIConstants;
import com.aptana.rdt.profiling.ProfilingPlugin;

public class StandardVMProfiler extends StandardVMRunner {
		
	@Override
	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
		subMonitor.beginTask(LaunchingMessages.StandardVMRunner_Launching_VM____1, 2); 
		subMonitor.subTask(LaunchingMessages.StandardVMRunner_Constructing_command_line____2); 
		
		List<String> arguments= constructProgramString(config, monitor);
				
		// VM args are the first thing after the ruby program so that users can specify
		// options like '-client' & '-server' which are required to be the first option
		String[] allVMArgs = combineVmArgs(config, fVMInstance);
		addArguments(allVMArgs, arguments);
		
		String[] lp= config.getLoadPath();
		if (lp.length > 0) {
			arguments.addAll(convertLoadPath(config, lp));
		}
		addStreamSync(arguments);
		arguments.add(END_OF_OPTIONS_DELIMITER);
		
		injectFileToLaunch(arguments, launch);
		
		arguments.add(getFileToLaunch(config));
		addArguments(config.getProgramArguments(), arguments);
				
		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);
		
		String[] envp = getEnvironment(config);
		
		subMonitor.worked(1);

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		
		subMonitor.subTask(LaunchingMessages.StandardVMRunner_Starting_virtual_machine____3); 
		Process p= null;
		File workingDir = getWorkingDir(config);
		if (envp != null && envp.length > 0) {
			p= exec(cmdLine, workingDir, envp);
		} else {
			p = exec(cmdLine, workingDir);
		}
		if (p == null) {
			return;
		}
		
		// check for cancellation
		if (monitor.isCanceled()) {
			p.destroy();
			return;
		}		
		
		IProcess process= newProcess(launch, p, renderProcessLabel(cmdLine), getDefaultProcessMap());
		process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));
		process.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, launch.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME));
		process.setAttribute(IRubyLaunchConfigurationConstants.ATTR_REQUIRES_REFRESH, launch.getAttribute(IRubyLaunchConfigurationConstants.ATTR_REQUIRES_REFRESH));
		subMonitor.worked(1);
		subMonitor.done();		
	}

	private void injectFileToLaunch(List<String> arguments, ILaunch launch) {
		String file = ProfilingPlugin.getDefault().getStateLocation().append("profile_" + System.currentTimeMillis() + ".log").toFile().toString();
		launch.setAttribute(IProfileUIConstants.ATTR_PROFILE_OUTPUT, file);
		
		// Inject the path to the ruby-prof script!
		File vmInstallLocation = fVMInstance.getInstallLocation();
		String path = vmInstallLocation.getAbsolutePath();
		if (!vmInstallLocation.getName().equals("bin")) {
			path += File.separator + "bin";
		}
		path = path + File.separator + "ruby-prof";
		arguments.add(path);	
		arguments.add("-f");
		arguments.add(file);
		arguments.add("-p");
		arguments.add("graph");
		arguments.add("--replace-progname");
		arguments.add(END_OF_OPTIONS_DELIMITER);
	}

}
