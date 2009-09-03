/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.rails.internal.ui.actions.mvc;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.radrails.rails.internal.core.RailsPlugin;

import com.aptana.rdt.core.gems.Version;

/**
 * Selection dialog for view types. Used by the ViewAction to solicit input from the user.
 * 
 * @author mkent
 */
public class ViewSelectionDialog extends MessageDialog
{

	private String[] viewChoices;
	private Combo views;
	private String selectionText;
	private IProject project;

	/**
	 * Constructor. Initializes the contents of the selection list.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param project
	 * @param fileName
	 *            the name of the view to create
	 */
	protected ViewSelectionDialog(Shell parentShell, IProject project, String fileName)
	{
		super(parentShell, "Confirm view creation", null, "Select the view to create:", QUESTION, new String[] {
				IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
		this.project = project;
		viewChoices = new String[ViewEditorActionDelegate.VIEW_TYPES.length];
		for (int i = 0; i < ViewEditorActionDelegate.VIEW_TYPES.length; i++)
		{
			viewChoices[i] = fileName + "." + ViewEditorActionDelegate.VIEW_TYPES[i];
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		views = new Combo(composite, SWT.DROP_DOWN);
		views.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		views.setItems(viewChoices);
		// Select .html.erb for Rails 2.+, .rhtml for before that
		String version = RailsPlugin.getRailsVersion(project);
		if (version != null && version.trim().length() > 0 && new Version(version).isGreaterThanOrEqualTo("2.0.0"))
		{
			for (int i = 0; i < viewChoices.length; i++)
			{
				if (viewChoices[i].endsWith(".html.erb"))
				{
					views.select(i);
					break;
				}
			}
		}
		else
		{
			for (int i = 0; i < viewChoices.length; i++)
			{
				if (viewChoices[i].endsWith(".rhtml"))
				{
					views.select(i);
					break;
				}
			}
		}
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId)
	{
		if (buttonId == OK)
		{
			selectionText = views.getText();
		}
		super.buttonPressed(buttonId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent)
	{
		super.createButtonsForButtonBar(parent);
		views.setFocus();
	}

	/**
	 * Returns the selected text from the combo box.
	 * 
	 * @return the text
	 */
	public String getText()
	{
		return selectionText;
	}

	/**
	 * Opens a confirm dialog to create a view file. The user will select which extension type to use.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param fileName
	 *            the name of the view file
	 * @return the full name of the view file
	 */
	public static String openConfirm(Shell parent, IProject project, String fileName)
	{
		ViewSelectionDialog vsd = new ViewSelectionDialog(parent, project, fileName);

		String ret = null;
		if (vsd.open() == OK)
		{
			ret = vsd.getText();
		}
		return ret;
	}

}
