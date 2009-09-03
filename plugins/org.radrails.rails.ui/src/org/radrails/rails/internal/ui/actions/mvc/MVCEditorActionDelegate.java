package org.radrails.rails.internal.ui.actions.mvc;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.radrails.rails.internal.core.RailsPlugin;

public abstract class MVCEditorActionDelegate implements IEditorActionDelegate
{

	private IFile currentFile = null;
	private IEditorPart activeEditor;

	public MVCEditorActionDelegate()
	{
		super();
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor)
	{
		this.activeEditor = targetEditor;
		getCurrentFile(targetEditor);
		setEnabled(action);
	}

	protected IEditorPart getActiveEditor()
	{
		return activeEditor;
	}

	private void getCurrentFile(IEditorPart targetEditor)
	{
		IEditorPart part = targetEditor;
		if (part == null)
			return;
		IEditorInput input = part.getEditorInput();
		if (!(input instanceof IFileEditorInput))
			return;
		currentFile = ((IFileEditorInput) input).getFile();
	}

	protected IFile getCurrentFile()
	{
		return currentFile;
	}

	public void selectionChanged(IAction action, ISelection selection)
	{
//		setEnabled(action);
	}

	protected void setEnabled(IAction action)
	{
		IFile currentFile = getCurrentFile();
		if (currentFile == null || !RailsPlugin.hasRailsNature(currentFile.getProject()))
		{
			action.setEnabled(false);
			return;
		}
		action.setEnabled(isEnabled());
	}

	protected abstract boolean isEnabled();

}