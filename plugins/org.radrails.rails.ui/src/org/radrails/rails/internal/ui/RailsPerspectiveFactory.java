/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.radrails.rails.ui.IRailsUIConstants;
import org.radrails.rails.ui.RailsUI;
import org.rubypeople.rdt.testunit.ITestUnitConstants;
import org.rubypeople.rdt.ui.IRubyConstants;
import org.rubypeople.rdt.ui.RubyUI;

import com.aptana.rdt.rake.IRakeUIConstants;

/**
 * Factory for the Rails perspective.
 * 
 * @author mkent
 * 
 */
public class RailsPerspectiveFactory implements IPerspectiveFactory {

	/**
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area
		String editorArea = layout.getEditorArea();

		// Top left: Ruby Explorer
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.2f, editorArea);
		topLeft.addView(RubyUI.ID_RUBY_EXPLORER);
		
		// Bottom right: Console and Servers view
		IFolderLayout consoleArea = layout.createFolder("consoleArea", IPageLayout.BOTTOM, 0.75f, editorArea);
		consoleArea.addPlaceholder(IRailsUIConstants.ID_SERVERS_VIEW);
		consoleArea.addPlaceholder(IRailsUIConstants.ID_GENERATORS_VIEW);
		consoleArea.addPlaceholder(IRakeUIConstants.ID_RAKE_VIEW);		
		consoleArea.addPlaceholder(IRailsUIConstants.ID_RAILS_PLUGINS_VIEW);	
		consoleArea.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		consoleArea.addView(IPageLayout.ID_PROBLEM_VIEW);
		consoleArea.addView(IPageLayout.ID_TASK_LIST);
		
		// Top right: Outline view
		IFolderLayout outlineArea = layout.createFolder("topRight", IPageLayout.BOTTOM, 0.60f, "topLeft");
		outlineArea.addView(IPageLayout.ID_OUTLINE);
				
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		layout.addActionSet(RailsUI.ID_ELEMENT_CREATION_ACTION_SET);
		
		layout.addNewWizardShortcut(IRubyConstants.ID_NEW_CLASS_WIZARD);
		layout.addNewWizardShortcut(ITestUnitConstants.ID_NEW_TESTCASE_WIZARD);
		
		layout.addShowViewShortcut(IRubyConstants.RI_VIEW_ID);
	}

}
