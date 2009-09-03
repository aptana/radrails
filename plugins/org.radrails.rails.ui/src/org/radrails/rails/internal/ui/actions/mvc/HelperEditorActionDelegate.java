package org.radrails.rails.internal.ui.actions.mvc;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.radrails.rails.core.RailsConventions;
import org.radrails.rails.ui.RailsUILog;
import org.rubypeople.rdt.ui.RubyUI;

public class HelperEditorActionDelegate extends MVCEditorActionDelegate
{

	@Override
	protected boolean isEnabled()
	{
		IFile currentFile = getCurrentFile();
		return RailsConventions.looksLikeController(currentFile) || RailsConventions.looksLikeView(currentFile)
				|| RailsConventions.looksLikeModel(currentFile) || RailsConventions.looksLikeTest(currentFile);
	}

	public void run(IAction action)
	{
		IFile currentFile = getCurrentFile();
		if (RailsConventions.looksLikeHelper(currentFile))
			return;
		IFile helperFile = RailsConventions.getHelperFromModel(currentFile); // try from model
		if (helperFile == null)
			helperFile = RailsConventions.getHelperFromView(currentFile); // try from view
		if (helperFile == null)
			helperFile = RailsConventions.getHelperFromController(currentFile); // try from controller
		if (helperFile == null)
			helperFile = RailsConventions.getHelperFromFunctionalTest(currentFile); // try from functional test
		if (helperFile == null)
			helperFile = RailsConventions.getHelperFromUnitTest(currentFile); // try from unit test
		if (helperFile == null)
			return;
		IEditorInput editorInput = new FileEditorInput(helperFile);
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput,
					RubyUI.ID_RUBY_EDITOR);
		}
		catch (PartInitException e)
		{
			RailsUILog.logError("Error creating editor", e);
		}

	}

}
