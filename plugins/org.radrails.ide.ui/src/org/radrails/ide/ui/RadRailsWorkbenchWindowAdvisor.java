/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.ide.ui;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * This class specifies which components to draw on the main IDE, including the
 * perspective bar, fast view bar, and status bar. This also allows for 
 * modification to the window's size, location, title bar, etc.
 * 
 * @author	mbaumbach
 */
public class RadRailsWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
    
	public static final String RAILS_TOOLBAR_GROUP = "railsToolbar";
	
	/**
	 * Creates a new RadRailsWorkbenchWindowAdvisor. Used to specify the UI components
	 * to display on the workbench window.
	 * 
	 * @param	configurer	The configurer used to configurer the workbench window.
	 */
	public RadRailsWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

    /**
     * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)
     */
	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new RadRailsActionBarAdvisor(configurer);
	}

    /**
     * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
     */
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(800, 600));
		configurer.setShowProgressIndicator(true);
		configurer.setShowStatusLine(true);
        configurer.setShowPerspectiveBar(true);
        configurer.setShowCoolBar(true);
		configurer.setTitle("RadRails");
		configurer.setShowFastViewBars(true);
        configurer.getWorkbenchConfigurer().setSaveAndRestore(true);
        addToolbar(configurer.getActionBarConfigurer());
	}
    
	/**
	 * Creates the toolbar and populates it with buttons.
	 * 
	 * @param	configurer	The IActionBarConfigurer to use.
	 */
	private void addToolbar(IActionBarConfigurer configurer) {
        ICoolBarManager cbManager = configurer.getCoolBarManager();
        cbManager.add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
        
        // Setup the toolbar
        IToolBarManager appToolBar = new ToolBarManager(cbManager.getStyle());
        appToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
        appToolBar.add(new Separator(IWorkbenchActionConstants.SAVE_GROUP));
        appToolBar.add(new Separator(RAILS_TOOLBAR_GROUP));
        appToolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        IWorkbenchWindow window  = configurer.getWindowConfigurer().getWindow();
        
        // Add the actions to the toolbar
        IWorkbenchAction newAction = ActionFactory.NEW_WIZARD_DROP_DOWN.create(window);
        appToolBar.add(newAction);
        IWorkbenchAction saveAction = ActionFactory.SAVE.create(window);
        appToolBar.add(saveAction);
        IWorkbenchAction printAction = ActionFactory.PRINT.create(window);
        appToolBar.add(printAction);
        
        // Add the toolbar to the coolbars
        cbManager.add(new ToolBarContributionItem(appToolBar, IWorkbenchActionConstants.TOOLBAR_FILE));
	}
	
    /**
     * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowCreate()
     */
    public void postWindowCreate() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        // Center the IDE
        IWorkbenchWindow window = configurer.getWindow();
        Rectangle dim = window.getShell().getMonitor().getBounds();
        int xLoc = (dim.width / 2) - (window.getShell().getSize().x / 2);
        int yLoc = (dim.height / 2) - (window.getShell().getSize().y / 2);
        configurer.getWindow().getShell().setLocation(xLoc, yLoc);
    }
    
} // RadRailsWorkbenchWindowAdvisor