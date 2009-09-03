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

public class ModelEditorActionDelegate extends MVCEditorActionDelegate
{

	@Override
	protected boolean isEnabled()
	{
		IFile currentFile = getCurrentFile();
		return RailsConventions.looksLikeController(currentFile) || RailsConventions.looksLikeHelper(currentFile)
				|| RailsConventions.looksLikeView(currentFile) || RailsConventions.looksLikeTest(currentFile);
	}

	public void run(IAction action)
	{
		IFile currentFile = getCurrentFile();
		IFile modelFile = RailsConventions.getModelFromController(currentFile);
		if (modelFile == null)
			modelFile = RailsConventions.getModelFromView(currentFile);
		if (modelFile == null)
			modelFile = RailsConventions.getModelFromHelper(currentFile);
		if (modelFile == null)
			modelFile = RailsConventions.getModelFromFunctionalTest(currentFile);
		if (modelFile == null)
			modelFile = RailsConventions.getModelFromUnitTest(currentFile);
		if (modelFile == null)
			return;
		IEditorInput editorInput = new FileEditorInput(modelFile);
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
