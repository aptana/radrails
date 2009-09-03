/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL youelect, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ide.editor.erb.wizards;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.Version;
import org.radrails.rails.internal.core.RailsPlugin;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.ISourceFolder;
import org.rubypeople.rdt.core.RubyCore;

import com.aptana.ide.core.StringUtils;
import com.aptana.ide.editor.erb.ERBPlugin;
import com.aptana.ide.editor.erb.preferences.IPreferenceConstants;
import com.aptana.ide.editors.unified.IUnifiedEditor;
import com.aptana.ide.editors.wizards.SimpleNewFileWizard;
import com.aptana.ide.editors.wizards.SimpleNewWizardPage;

/**
 * @author Chris Williams
 */
public class ERBNewFileWizard extends SimpleNewFileWizard implements INewWizard
{
	private static final String HTML_ERB = ".html.erb";
	private static final String RHTML = ".rhtml";

	/**
	 * ERBNewFileWizard
	 */
	public ERBNewFileWizard()
	{
		super();

		this.setWindowTitle(Messages.ERBNewFileWizard_Window_Title);
	}

	/**
	 * @see com.aptana.ide.editors.wizards.SimpleNewFileWizard#createNewFilePage(org.eclipse.jface.viewers.ISelection)
	 */
	protected SimpleNewWizardPage createNewFilePage(ISelection selection)
	{
		SimpleNewWizardPage page = new SimpleNewWizardPage(selection);

		page.setRequiredFileExtensions(new String[] { "erb", "rhtml" }); //$NON-NLS-1$
		page.setTitle(Messages.ERBNewFileWizard_Filename);
		page.setDescription(Messages.ERBNewFileWizard_Description);
		IPreferenceStore store = ERBPlugin.getDefault().getPreferenceStore();
		String filename = store.getString(IPreferenceConstants.ERBEDITOR_INITIAL_FILE_NAME);
		String extension = getProperExtension(selection);
		if (extension != null)
		{
			IPath path = new Path(filename);
			String oldExtension = path.getFileExtension();
			int toRemove = 0;
			if (oldExtension != null)
				toRemove = oldExtension.length() + 1;
			filename = filename.substring(0, filename.length() - toRemove) + extension;
		}
		page.setDefaultFileName(filename);
		return page;
	}

	private String getProperExtension(ISelection selection)
	{
		String extension = null;
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection structured = (IStructuredSelection) selection;
			Object first = structured.getFirstElement();
			if (!(first instanceof IResource) && (first instanceof IAdaptable))
			{
				IAdaptable adaptable = (IAdaptable) first;
				Object resource = adaptable.getAdapter(IResource.class);
				if (resource != null)
				{
					first = resource;
				}
			}
			if (first instanceof IResource)
			{
				IResource res = (IResource) first;
				IProject proj = res.getProject();
				String version = RailsPlugin.getRailsVersion(proj);
				if (version == null)
					return HTML_ERB;
				Version yeah = new Version(version);
				if (yeah.getMajor() >= 2)
					extension = HTML_ERB;
				else
					extension = RHTML;
			}
		}
		return extension;
	}

	/**
	 * @see com.aptana.ide.editors.wizards.SimpleNewFileWizard#getInitialFileContents()
	 */
	protected String getInitialFileContents()
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		IPreferenceStore store = ERBPlugin.getDefault().getPreferenceStore();
		String contents = store.getString(IPreferenceConstants.ERBEDITOR_INITIAL_CONTENTS);
		pw.println(contents);
		pw.close();

		return sw.toString();
	}

	private void doFinish(String containerName, String fileName, IProgressMonitor monitor) throws CoreException
	{
		// create a sample file
		monitor.beginTask(com.aptana.ide.editors.wizards.Messages.SimpleNewFileWizard_Creating + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer))
		{
			throwCoreException(StringUtils.format(
					com.aptana.ide.editors.wizards.Messages.SimpleNewFileWizard_ContainerDoesNotExist, containerName));
		}
		IContainer container = (IContainer) resource;
		IPath filePath = new Path(fileName);
		final IFile file = container.getFile(filePath);
		IRubyElement rubyContainer = RubyCore.create(resource);
		String source = getInitialFileContents();
		if (rubyContainer instanceof ISourceFolder)
		{
			ISourceFolder folder = (ISourceFolder) rubyContainer;
			folder.createRubyScript(filePath.lastSegment(), source, true, monitor);
		}

		monitor.worked(1);
		monitor.setTaskName(com.aptana.ide.editors.wizards.Messages.SimpleNewFileWizard_OpeningFileForEditing);
		getShell().getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try
				{
					IEditorPart part = IDE.openEditor(page, file, true);
					if (part instanceof IUnifiedEditor)
					{
						IUnifiedEditor te = (IUnifiedEditor) part;
						te.getViewer().getTextWidget().setCaretOffset(getInitialCaretOffset());
						// TODO: IM Fix
						// te.setParentDirectoryHint(FileExplorerView.lastSelected);
					}

				}
				catch (PartInitException e)
				{
				}
			}
		});
		monitor.worked(1);
	}

	// FIXME Allow overriding the doFinish method (make it protected in superclass!)
	public boolean performFinish()
	{
		SimpleNewWizardPage page = (SimpleNewWizardPage) getPages()[0];
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException
			{
				try
				{
					doFinish(containerName, fileName, monitor);
				}
				catch (CoreException e)
				{
					throw new InvocationTargetException(e);
				}
				finally
				{
					monitor.done();
				}
			}
		};
		try
		{
			getContainer().run(true, false, op);
		}
		catch (InterruptedException e)
		{
			return false;
		}
		catch (InvocationTargetException e)
		{
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), com.aptana.ide.editors.wizards.Messages.SimpleNewFileWizard_Error,
					realException.getMessage());
			return false;
		}
		return true;
	}

	// FIXME Make this method protected in superclass so we can just call it!
	private void throwCoreException(String message) throws CoreException
	{
		IStatus status = new Status(IStatus.ERROR, "com.aptana.ide.core.ui", IStatus.OK, message, null); //$NON-NLS-1$
		throw new CoreException(status);
	}
}
