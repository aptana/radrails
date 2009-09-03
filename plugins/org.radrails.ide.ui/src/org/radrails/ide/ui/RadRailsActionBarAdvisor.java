/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.ide.ui;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 * 
 * @author	mbaumbach
 */
public class RadRailsActionBarAdvisor extends ActionBarAdvisor {

	// List of all the actions
	private NewWizardAction newAction;

	private IWorkbenchAction exitAction;

	private IWorkbenchAction aboutAction;

	private IWorkbenchAction saveAction;
	
	private IWorkbenchAction saveAsAction;

	private IWorkbenchAction saveAllAction;

	private IWorkbenchAction importAction;

	private IWorkbenchAction exportAction;

	private IWorkbenchAction propertiesAction;

	private IWorkbenchAction undoAction;

	private IWorkbenchAction redoAction;

	private IWorkbenchAction cutAction;

	private IWorkbenchAction copyAction;

	private IWorkbenchAction pasteAction;

	private IWorkbenchAction deleteAction;

	private IWorkbenchAction selectAllAction;

	private IWorkbenchAction findAction;

	private IWorkbenchAction preferencesMenu;

	private IWorkbenchAction closeAction;

	private IWorkbenchAction closeAllAction;

	private IContributionItem showViewMenuItems;

	private IContributionItem openPerspectiveMenuItems;

	private IWorkbenchAction helpContentsAction;

	private IWorkbenchAction switchWorkspaceAction;
	
	private IWorkbenchAction showViewMenuAction;

    private IWorkbenchAction showPartPaneMenuAction;

    private IWorkbenchAction nextPartAction;

    private IWorkbenchAction prevPartAction;

    private IWorkbenchAction nextEditorAction;

    private IWorkbenchAction prevEditorAction;

    private IWorkbenchAction nextPerspectiveAction;

    private IWorkbenchAction prevPerspectiveAction;

    private IWorkbenchAction activateEditorAction;

    private IWorkbenchAction maximizePartAction;
    
    private IWorkbenchAction minimizePartAction;

    private IWorkbenchAction switchToEditorAction;
    
    private IWorkbenchAction openEditorDropDownAction;
    
    private UpdateAction updateAction;

	/**
	 * Creates a new RadRailsActionBar advisor. Used to create menus and their
	 * respective actions.
	 * 
	 * @param	configurer	The action bar configurer to use.
	 */
	public RadRailsActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	/**
	 * @see org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui.IWorkbenchWindow)
	 */
	protected void makeActions(final IWorkbenchWindow window) {
		// File menu actions
		newAction = new NewWizardAction(window);
		newAction.setId("%radrails.wizards.newproject");
		newAction.setCategoryId(null);
		newAction.setText("&New...");
		newAction.setAccelerator(SWT.CTRL | 'N');
		closeAction = ActionFactory.CLOSE.create(window);
		closeAllAction = ActionFactory.CLOSE_ALL.create(window);
		saveAction = ActionFactory.SAVE.create(window);
		saveAsAction = ActionFactory.SAVE_AS.create(window);
		saveAllAction = ActionFactory.SAVE_ALL.create(window);
		importAction = ActionFactory.IMPORT.create(window);
		exportAction = ActionFactory.EXPORT.create(window);
		propertiesAction = ActionFactory.PROPERTIES.create(window);
		switchWorkspaceAction = IDEActionFactory.OPEN_WORKSPACE.create(window);
		exitAction = ActionFactory.QUIT.create(window);
		register(newAction);
		register(closeAction);
		register(closeAllAction);
		register(saveAction);
		register(saveAsAction);
		register(saveAllAction);
		register(importAction);
		register(exportAction);
		register(propertiesAction);
		register(switchWorkspaceAction);
		register(exitAction);

		// Edit menu actions
		undoAction = ActionFactory.UNDO.create(window);
		redoAction = ActionFactory.REDO.create(window);
		cutAction = ActionFactory.CUT.create(window);
		copyAction = ActionFactory.COPY.create(window);
		pasteAction = ActionFactory.PASTE.create(window);
		deleteAction = ActionFactory.DELETE.create(window);
		selectAllAction = ActionFactory.SELECT_ALL.create(window);
		findAction = ActionFactory.FIND.create(window);
		register(undoAction);
		register(redoAction);
		register(cutAction);
		register(copyAction);
		register(pasteAction);
		register(deleteAction);
		register(selectAllAction);
		register(findAction);
		
		// Window menu actions
		showViewMenuItems = ContributionItemFactory.VIEWS_SHORTLIST
				.create(window);
		openPerspectiveMenuItems = ContributionItemFactory.PERSPECTIVES_SHORTLIST
				.create(window);
		preferencesMenu = ActionFactory.PREFERENCES.create(window);
		register(preferencesMenu);
		
		// Navigation submenu in Window menu
        // Actions for invisible accelerators
        showViewMenuAction = ActionFactory.SHOW_VIEW_MENU.create(window);
        register(showViewMenuAction);

        showPartPaneMenuAction = ActionFactory.SHOW_PART_PANE_MENU
                .create(window);
        register(showPartPaneMenuAction);

        nextEditorAction = ActionFactory.NEXT_EDITOR.create(window);
        register(nextEditorAction);
        prevEditorAction = ActionFactory.PREVIOUS_EDITOR.create(window);
        register(prevEditorAction);
        ActionFactory.linkCycleActionPair(nextEditorAction, prevEditorAction);

        nextPartAction = ActionFactory.NEXT_PART.create(window);
        register(nextPartAction);
        prevPartAction = ActionFactory.PREVIOUS_PART.create(window);
        register(prevPartAction);
        ActionFactory.linkCycleActionPair(nextPartAction, prevPartAction);

        nextPerspectiveAction = ActionFactory.NEXT_PERSPECTIVE
                .create(window);
        register(nextPerspectiveAction);
        prevPerspectiveAction = ActionFactory.PREVIOUS_PERSPECTIVE
                .create(window);
        register(prevPerspectiveAction);
        ActionFactory.linkCycleActionPair(nextPerspectiveAction,
                prevPerspectiveAction);

        activateEditorAction = ActionFactory.ACTIVATE_EDITOR
                .create(window);
        register(activateEditorAction);

        maximizePartAction = ActionFactory.MAXIMIZE.create(window);
        register(maximizePartAction);

		minimizePartAction = ActionFactory.MINIMIZE.create(window);
		register(minimizePartAction);
        
        switchToEditorAction = ActionFactory.SHOW_OPEN_EDITORS
                .create(window);
        register(switchToEditorAction);

        openEditorDropDownAction = ActionFactory.SHOW_WORKBOOK_EDITORS
                .create(window);
        register(openEditorDropDownAction);

		// Help menu actions
		helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
		aboutAction = ActionFactory.ABOUT.create(window);
		updateAction = new UpdateAction(window);
		register(exitAction);
	}

	/**
	 * @see org.eclipse.ui.application.ActionBarAdvisor#fillMenuBar(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillMenuBar(IMenuManager menuBar) {
		// Create the top level menus
		MenuManager fileMenu = new MenuManager("&File",
				IWorkbenchActionConstants.M_FILE);
		MenuManager editMenu = new MenuManager("&Edit",
				IWorkbenchActionConstants.M_EDIT);
		MenuManager navigateMenu = new MenuManager("&Navigate",
				IWorkbenchActionConstants.M_NAVIGATE);
		MenuManager windowMenu = new MenuManager("&Window",
				IWorkbenchActionConstants.M_WINDOW);
		MenuManager openPerspectiveMenu = new MenuManager("&Open Perspective");
		openPerspectiveMenu.add(openPerspectiveMenuItems);
		MenuManager showViewMenu = new MenuManager("Show &View");
		showViewMenu.add(showViewMenuItems);
		MenuManager helpMenu = new MenuManager("&Help");

		// Add the top level menus to the menu bar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(navigateMenu);
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);

		// Add all of the actions and groupings to each type of top level menu

		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		fileMenu.add(newAction);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.NEW_GROUP));
		fileMenu.add(new Separator());
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		fileMenu.add(new Separator());
		fileMenu.add(closeAction);
		fileMenu.add(closeAllAction);
		fileMenu.add(new Separator());
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(saveAllAction);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_GROUP));
		fileMenu.add(new Separator());
		fileMenu.add(importAction);
		fileMenu.add(exportAction);
		fileMenu.add(new Separator());
		fileMenu.add(propertiesAction);
		fileMenu.add(new Separator());
		fileMenu.add(switchWorkspaceAction);
		fileMenu.add(new Separator());
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
		fileMenu.add(new Separator());
		fileMenu.add(exitAction);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));

		editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
		editMenu.add(undoAction);
		editMenu.add(redoAction);
		editMenu.add(new Separator());
		editMenu.add(cutAction);
		editMenu.add(copyAction);
		editMenu.add(pasteAction);
		editMenu.add(new Separator());
		editMenu.add(deleteAction);
		editMenu.add(selectAllAction);
		editMenu.add(new Separator());
		editMenu.add(findAction);
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));

		navigateMenu.add(new GroupMarker(IWorkbenchActionConstants.NAV_START));
		navigateMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		navigateMenu.add(new GroupMarker(IWorkbenchActionConstants.NAV_END));
		
		windowMenu.add(new GroupMarker(IWorkbenchActionConstants.WINDOW_EXT));
		windowMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		windowMenu.add(openPerspectiveMenu);
		windowMenu.add(showViewMenu);
		windowMenu.add(new Separator());
		
		// Navigation submenu
		MenuManager subMenu = new MenuManager(IDEWorkbenchMessages.Workbench_shortcuts, "shortcuts"); //$NON-NLS-1$
        windowMenu.add(subMenu);
        subMenu.add(showPartPaneMenuAction);
        subMenu.add(showViewMenuAction);
        subMenu.add(new Separator());
        subMenu.add(maximizePartAction);
        subMenu.add(minimizePartAction);
        subMenu.add(new Separator());
        subMenu.add(activateEditorAction);
        subMenu.add(nextEditorAction);
        subMenu.add(prevEditorAction);
        subMenu.add(switchToEditorAction);
        subMenu.add(openEditorDropDownAction);
        subMenu.add(new Separator());
        subMenu.add(nextPartAction);
        subMenu.add(prevPartAction);
        subMenu.add(new Separator());
        subMenu.add(nextPerspectiveAction);
        subMenu.add(prevPerspectiveAction);
		windowMenu.add(new Separator());
		windowMenu.add(preferencesMenu);

		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		helpMenu.add(helpContentsAction);
		helpMenu.add(new Separator());
		//helpMenu.add(updateAction);
		helpMenu.add(new Separator());
		helpMenu.add(aboutAction);
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
	}

} // RadRailsActionBarAdvisor