package org.radrails.rails.internal.ui.actions.mvc;

import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.radrails.rails.core.Inflector;
import org.radrails.rails.core.RailsConventions;
import org.radrails.rails.ui.RailsUILog;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IParent;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.core.RubyMethod;
import org.rubypeople.rdt.internal.ui.rubyeditor.RubyDocumentProvider;
import org.rubypeople.rdt.internal.ui.rubyeditor.RubyEditor;
import org.rubypeople.rdt.ui.RubyUI;

public class ViewEditorActionDelegate extends MVCEditorActionDelegate
{

	private static final String APP_MODELS = "app/models";
	private static final String APP_HELPERS = "app/helpers";

	private static final String HAML_EDITOR_ID = "net.lucky_dip.hamleditor.editor.HamlEditor";
	private static final String RHTML_EDITOR_ID = "com.aptana.ide.editors.ERBEditor";

	public static final String[] VIEW_TYPES = { "rhtml", "rjs", "rxml", "html.erb", "xml.builder", "dryml", "haml" };

	@Override
	protected boolean isEnabled()
	{
		IFile currentFile = getCurrentFile();
		return RailsConventions.looksLikeController(currentFile) || RailsConventions.looksLikeHelper(currentFile)
				|| RailsConventions.looksLikeModel(currentFile) || RailsConventions.looksLikeTest(currentFile);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action)
	{
		IFile original = getCurrentFile();
		IProject project = original.getProject();
		String path = original.getProjectRelativePath().toString();
		try
		{
			if (attemptToOpenMvcView(project, path, "app/controllers", "_controller"))
				return;
			if (attemptToOpenMvcView(project, path, APP_HELPERS, "_helper"))
				return;
			if (attemptToOpenMvcView(project, path, APP_MODELS, ""))
				return;
			if (attemptToOpenMvcView(project, path, "test/functional", "_controller_test"))
				return;
			if (attemptToOpenMvcView(project, path, "test/unit", "_test"))
				return;
			if (attemptToOpenMvcModelOrController(project, path, original))
				return;

		}
		catch (RubyModelException e)
		{
			RailsUILog.logError("Error accessing ruby model", e);
		}
		catch (PartInitException e)
		{
			RailsUILog.logError("Error creating editor", e);
		}
		catch (CoreException e)
		{
			RailsUILog.logError("Error running action", e);
		}
	}

	private boolean attemptToOpenMvcView(IProject project, String path, String srcDir, String srcSuffix)
			throws RubyModelException, CoreException, PartInitException
	{
		Matcher m = Pattern.compile("^((?:.+?/)?)" + srcDir + "/(.+)" + srcSuffix + "\\.rb$").matcher(path);
		if (!m.matches())
			return false;

		String appRootDir = (m.group(1).length() == 0 ? "" : "/") + m.group(1);
		String viewName = m.group(2);

		RubyEditor editor = (RubyEditor) getActiveEditor();
		RubyDocumentProvider provider = (RubyDocumentProvider) editor.getDocumentProvider();
		IRubyScript script = (IRubyScript) provider.getWorkingCopy(editor.getEditorInput());
		TextSelection ts = (TextSelection) editor.getSelectionProvider().getSelection();
		String actionName = findAction(script, ts.getOffset());
		// If actionName is null, pop up a dialog asking which action/view they want!
		if (actionName == null)
		{
			IRubyScript controllerScript = null;
			if (srcDir.equals(APP_MODELS))
			{
				IFile original = getCurrentFile();
				IFile controller = RailsConventions.getControllerFromModel(original);
				controllerScript = RubyCore.create(controller);
				viewName = Inflector.pluralize(viewName);
			}
			else if (srcDir.equals(APP_HELPERS))
			{
				IFile original = getCurrentFile();
				IFile controller = RailsConventions.getControllerFromHelper(original);
				controllerScript = RubyCore.create(controller);
			}
			else if (srcDir.equals("test/functional"))
			{
				IFile original = getCurrentFile();
				IFile controller = RailsConventions.getControllerFromFunctionalTest(original);
				controllerScript = RubyCore.create(controller);
			}
			else if (srcDir.equals("test/unit"))
			{
				IFile original = getCurrentFile();
				IFile controller = RailsConventions.getControllerFromUnitTest(original);
				controllerScript = RubyCore.create(controller);
				viewName = Inflector.pluralize(viewName);
			}
			else
			{
				controllerScript = script;
			}
			IType[] types = controllerScript.getTypes();
			WhichActionDialog dialog = new WhichActionDialog(Display.getDefault().getActiveShell(), types[0]);
			if (dialog.open() == Dialog.OK)
			{
				actionName = dialog.getAction();
			}
		}
		if (actionName != null)
		{
			IFile fileHandle = findViewFile(project, appRootDir, viewName, actionName);
			if (fileHandle == null || !fileHandle.exists())
			{
				// new dialog
				String filename = ViewSelectionDialog.openConfirm(Display.getDefault().getActiveShell(), project,
						actionName);

				if (filename != null)
				{
					IPath filePath = project.getProjectRelativePath().append(
							project.getName() + appRootDir + "/app/views/" + viewName + "/" + filename);
					fileHandle = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
					if (!fileHandle.getParent().exists())
					{
						try
						{
							((IFolder) fileHandle.getParent()).create(false, true, null);
						}
						catch (Exception e)
						{
							// ignore
						}
					}
					// Use the contents that ERB New File wizard inserts!
					IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), "com.aptana.ide.editor.erb");
					String contents = store.getString("com.aptana.ide.editor.erb.ERBEDITOR_INITIAL_CONTENTS");
					fileHandle.create(new ByteArrayInputStream((contents).getBytes()), false /* force */, null);
				}
			}
			if (fileHandle != null && fileHandle.exists())
			{
				IEditorInput editorInput = new FileEditorInput(fileHandle);
				open(editorInput, fileHandle);
			}
		}
		return true;
	}

	private IFile findViewFile(IProject project, String appRootDir, String viewName, String action)
	{
		IPath thePath = new Path(appRootDir).append("app").append("views").append(viewName);
		for (int i = 0; i < VIEW_TYPES.length; i++)
		{
			IFile fileHandle = project.getFile(thePath.append(action + "." + VIEW_TYPES[i]));
			if (fileHandle.exists())
			{
				return fileHandle;
			}
		}
		IFolder folder = project.getFolder(thePath);
		if (folder == null || !folder.exists())
			return null;
		try
		{
			IResource[] children = folder.members();
			for (IResource child : children)
			{
				if (child.getName().startsWith(action + ".") && child.getType() == IResource.FILE)
				{
					return (IFile) child;
				}
			}
		}
		catch (CoreException e)
		{
			// ignore
		}

		return null;
	}

	private boolean attemptToOpenMvcModelOrController(IProject project, String path, IFile original)
			throws PartInitException
	{
		String appRootDir = "";
		Matcher m = Pattern.compile("^(.+?/)(app/views/.+)$").matcher(path);
		if (m.matches())
		{
			appRootDir = "/" + m.group(1);
			path = m.group(2);
		}

		if (path.startsWith("app/views"))
		{
			IPath tPath = original.getProjectRelativePath();
			String[] segments = tPath.segments();

			String patternStr = "app/views/(\\S+?)/" + segments[segments.length - 1];
			Pattern pattern = Pattern.compile(patternStr);
			Matcher matcher = pattern.matcher("");

			// Set the input
			matcher.reset(path);

			// Get tagname and contents of tag
			matcher.find(); // true
			String controllername = matcher.group(1);

			IPath thepath = project.getProjectRelativePath().append(
					project.getName() + appRootDir + "/app/controllers/" + controllername + "_controller.rb");
			IFile fileHandle = ResourcesPlugin.getWorkspace().getRoot().getFile(thepath);
			IPath mailerpath = project.getProjectRelativePath().append(
					project.getName() + appRootDir + "/app/models/" + controllername + ".rb");
			IFile fileHandleMailer = ResourcesPlugin.getWorkspace().getRoot().getFile(mailerpath);

			if (fileHandle.exists())
			{
				IEditorInput editorInput = new FileEditorInput(fileHandle);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput,
						RubyUI.ID_RUBY_EDITOR);
				return true;
			}
			else if (fileHandleMailer.exists())
			{
				IEditorInput editorInput = new FileEditorInput(fileHandleMailer);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput,
						RubyUI.ID_RUBY_EDITOR);
				return true;
			}
		}
		return false;
	}

	private void open(IEditorInput newEditorInput, IFile newFileHandle) throws PartInitException
	{
		if (shouldOpenInRHTMLEditor(newFileHandle))
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(newEditorInput,
					RHTML_EDITOR_ID);
			return;
		}
		if (shouldOpenInHAMLEditor(newFileHandle))
		{
			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().findEditor(HAML_EDITOR_ID);
			if (desc != null)
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(newEditorInput,
						HAML_EDITOR_ID);
				return;
			}
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(newEditorInput,
					RHTML_EDITOR_ID);
			return;
		}
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(newEditorInput,
				RubyUI.ID_RUBY_EDITOR);
	}

	private boolean shouldOpenInHAMLEditor(IFile fileHandle)
	{
		return fileHandle.getFileExtension().equals("haml") || fileHandle.getName().contains(".haml");
	}

	private boolean shouldOpenInRHTMLEditor(IFile fileHandle)
	{
		return fileHandle.getFileExtension().equals("rhtml") || fileHandle.getFileExtension().equals("erb")
				|| fileHandle.getFileExtension().equals("dryml");
	}

	public String findAction(IRubyScript script, int offset) throws RubyModelException
	{
		String retVal = null;
		IRubyElement method = findRubyMethod(script.getElementAt(offset));
		if (method != null)
		{
			retVal = method.getElementName();
		}
		else
		{
			IParent parent = ((IParent) script.getChildren()[0]);
			if (parent.hasChildren())
			{
				IRubyElement[] child = parent.getChildren();
				// TODO key: ctrl+shift+v - ClassCastException when not in a method, instead
				// RubyInstVar
				for (int j = 0; j < child.length; j++)
				{
					if (child[j] instanceof RubyMethod)
					{
						RubyMethod m = (RubyMethod) child[j];
						int methodStart = m.getSourceRange().getOffset();
						int methodEnd = (m.getSourceRange().getOffset() + m.getSourceRange().getLength());
						if ((offset >= methodStart) && (offset <= methodEnd))
						{
							retVal = m.getElementName();
							break;
						}
					}
				}
			}
		}
		return retVal;
	}

	private IRubyElement findRubyMethod(IRubyElement e)
	{
		if (isRubyMethod(e))
		{
			return e;
		}
		else if (e == null)
		{
			return null;
		}
		else
		{
			return findRubyMethod(e.getParent());
		}
	}

	private boolean isRubyMethod(IRubyElement e)
	{
		boolean retVal = false;
		if (e instanceof RubyMethod)
		{
			retVal = true;
		}
		return retVal;
	}

	private class WhichActionDialog extends MessageDialog
	{

		private IType type;
		private Combo combo;
		private String action;

		protected WhichActionDialog(Shell parentShell, IType type)
		{
			super(parentShell, "Cursor not inside an action", null, "For which action?", MessageDialog.QUESTION,
					new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
			this.type = type;
		}

		@Override
		protected Control createCustomArea(Composite parent)
		{
			try
			{
				IMethod[] methods = type.getMethods();
				if (methods == null || methods.length == 0)
				{
					Label label = new Label(parent, SWT.NULL);
					label
							.setText("The current controller has no actions. Please create a public method in the controller first");
				}
				else
				{
					combo = new Combo(parent, SWT.SINGLE);
					for (int i = 0; i < methods.length; i++)
					{
						if (methods[i].getVisibility() == IMethod.PUBLIC)
						{
							combo.add(methods[i].getElementName());
						}
					}
					if (combo.getItemCount() > 0)
						combo.select(0);
				}

			}
			catch (RubyModelException e)
			{
				RailsUILog.log(e);
			}

			return super.createCustomArea(parent);
		}

		@Override
		protected void buttonPressed(int buttonId)
		{
			if (buttonId == 0)
			{
				if (combo != null)
					action = combo.getText();
			}
			super.buttonPressed(buttonId);
		}

		public String getAction()
		{
			return action;
		}

	}

}
