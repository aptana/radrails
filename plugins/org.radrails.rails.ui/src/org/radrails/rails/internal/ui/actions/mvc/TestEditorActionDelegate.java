package org.radrails.rails.internal.ui.actions.mvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.radrails.rails.core.RailsConventions;
import org.radrails.rails.ui.RailsUILog;
import org.rubypeople.rdt.ui.RubyUI;

public class TestEditorActionDelegate extends MVCEditorActionDelegate
{
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action)
	{
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IFile original = ((FileEditorInput) part.getEditorInput()).getFile();
		IProject project = original.getProject();
		String filename = original.getProjectRelativePath().toString();

		if (attemptToOpenMvcRelatedFile(project, filename, "app/controllers", "", "test/functional", "_test"))
			return;
		if (attemptToOpenMvcRelatedFile(project, filename, "app/models", "", "test/unit", "_test"))
			return;
		if (attemptToOpenMvcRelatedFile(project, filename, "app/helpers", "_helper", "test/functional",
				"_controller_test"))
			return;
		if (attemptToOpenMvcRelatedFile(project, filename, "test/functional", "_test", "app/controllers", ""))
			return;
		if (attemptToOpenMvcRelatedFile(project, filename, "test/unit", "_test", "app/models", ""))
			return;

		openFile(RailsConventions.getFunctionalTestFromView(original));
	}

	private boolean attemptToOpenMvcRelatedFile(IProject project, String filename, String srcDir, String srcSuffix,
			String destDir, String destSuffix)
	{
		Matcher m = Pattern.compile("^((?:.+?/)?)" + srcDir + "/(.+)" + srcSuffix + "\\.rb$").matcher(filename);
		if (!m.matches())
			return false;

		IPath thePath = project.getProjectRelativePath().append(
				project.getName() + (m.group(1).length() == 0 ? "" : "/") + m.group(1) + "/" + destDir + "/"
						+ m.group(2) + destSuffix + ".rb");
		IFile fileHandle = ResourcesPlugin.getWorkspace().getRoot().getFile(thePath);
		return openFile(fileHandle);
	}

	private boolean openFile(IFile fileHandle)
	{
		IEditorInput editorInput = new FileEditorInput(fileHandle);
		try
		{
			if (fileHandle.exists())
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput,
						RubyUI.ID_RUBY_EDITOR);
				return true;
			}
		}
		catch (PartInitException e)
		{
			RailsUILog.logError("Error creating editor", e);
		}
		return false;
	}

	@Override
	protected boolean isEnabled()
	{
		IFile currentFile = getCurrentFile();
		return RailsConventions.looksLikeController(currentFile) || RailsConventions.looksLikeHelper(currentFile)
				|| RailsConventions.looksLikeModel(currentFile) || RailsConventions.looksLikeView(currentFile);
	}

}
