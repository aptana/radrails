/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
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
package com.aptana.radrails.cloud.internal;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.aptana.ide.core.ui.CoreUIUtils;
import com.aptana.ide.server.cloud.syncing.ICloudDeployer.Endpoint;
import com.aptana.radrails.cloud.Activator;
import com.aptana.radrails.cloud.deploy.Messages;

/**
 * Simple dialog to confirm Rails deployment. (Originally derived from Winston's WarUploadDialog for Java on the Cloud).
 * 
 * @author Winston Prakash
 * @author cwilliams
 * @version 1.0
 */
public class EndpointDialog extends TitleAreaDialog
{

	private static final String LARGE_IMAGE = "icons/deploy_large.gif"; //$NON-NLS-1$
	private Endpoint target;
	private Combo combo;
	private Image image;
	private Text comment;
	private String commentStr;

	public EndpointDialog(final Shell parentShell, Endpoint target)
	{
		super(parentShell);
		this.target = target;
	}

	/**
	 * Create contents of the dialog
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite area = (Composite) super.createDialogArea(parent);
		setMessage(Messages.EndpointDialog_MSG_Verify_and_select_target);

		Composite container = new Composite(area, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createDeployOptionSection(container);
		createCommentSection(container);

		if (target == Endpoint.PUBLIC)
		{
			combo.setText(Messages.EndpointDialog_LBL_Public_endpoint);
		}
		else if (target == Endpoint.STAGING)
		{
			combo.setText(Messages.EndpointDialog_LBL_Staging_endpoint);
		}
		setTitle(Messages.EndpointDialog_TTL_Header);

		ImageDescriptor iconDesc = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, LARGE_IMAGE);
		image = iconDesc.createImage();
		setTitleImage(image);
		return area;
	}

	private void createDeployOptionSection(Composite container)
	{
		Composite deployComp = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginLeft = -5;
		deployComp.setLayout(layout);

		Label deployTo = new Label(deployComp, SWT.NULL);
		deployTo.setText(Messages.EndpointDialog_LBL_Deploy_to);

		combo = new Combo(deployComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.add(Messages.EndpointDialog_LBL_Public_endpoint);
		combo.add(Messages.EndpointDialog_LBL_Staging_endpoint);
		combo.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String selection = combo.getItem(combo.getSelectionIndex());
				if (selection.equals(Messages.EndpointDialog_LBL_Public_endpoint))
					target = Endpoint.PUBLIC;
				else
					target = Endpoint.STAGING;
				setTitle(MessageFormat.format(Messages.EndpointDialog_TTL_Header, selection));
			}
		});

		GridData deployLayoutData = new GridData();
		deployLayoutData.horizontalSpan = 2;
		deployComp.setLayoutData(deployLayoutData);
	}

	private Composite createCommentSection(Composite parent)
	{
		Composite commentArea = new Composite(parent, SWT.NONE);
		commentArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		commentArea.setLayout(layout);

		Label commentLabel = new Label(commentArea, SWT.BEGINNING);
		commentLabel.setText(Messages.EndpointDialog_MSG_Enter_deploy_comment);
		GridData commentLabelGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		commentLabel.setLayoutData(commentLabelGridData);

		Link helpLink = new Link(commentArea, SWT.END);
		helpLink.setText(Messages.EndpointDialog_MSG_Help_link);
		GridData helpLinkGridData = new GridData(SWT.END, SWT.CENTER, true, false);
		helpLink.setLayoutData(helpLinkGridData);
		helpLink.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				CoreUIUtils
						.openBrowserURL("http://www.aptana.com/docs/index.php/My_Cloud_-_Team#What_is_the_Cloud_Team_Comment_feature.3F"); //$NON-NLS-1$
			}
		});

		comment = new Text(commentArea, SWT.MULTI | SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		gridData.heightHint = 50;
		comment.setLayoutData(gridData);

		comment.setEnabled(true);
		comment.setText(""); //$NON-NLS-1$
		comment.selectAll();
		comment.addModifyListener(new ModifyListener()
		{

			public void modifyText(ModifyEvent e)
			{
				commentStr = comment.getText();
			}

		});
		return commentArea;
	}

	@Override
	public boolean close()
	{
		if (image != null)
			image.dispose();
		return super.close();
	}

	/**
	 * Create contents of the button bar
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		getButton(IDialogConstants.OK_ID).setText(Messages.EndpointDialog_LBL_Deploy_button);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(Messages.EndpointDialog_TTL);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize()
	{
		return new Point(500, 350);
	}

	public Endpoint getEndpoint()
	{
		return target;
	}

	public String getComment()
	{
		return commentStr;
	}
}
