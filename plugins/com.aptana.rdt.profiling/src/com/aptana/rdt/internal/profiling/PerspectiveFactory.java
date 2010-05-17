package com.aptana.rdt.internal.profiling;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

import com.aptana.rdt.profiling.IProfileUIConstants;

public class PerspectiveFactory implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		IFolderLayout consoleFolder = layout.createFolder("console", IPageLayout.BOTTOM, (float)0.75, layout.getEditorArea());
		consoleFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		consoleFolder.addView(IPageLayout.ID_TASK_LIST);
		consoleFolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		consoleFolder.addPlaceholder(IPageLayout.ID_PROP_SHEET);
		
		IFolderLayout navFolder= layout.createFolder("navigator", IPageLayout.TOP, (float) 0.45, layout.getEditorArea());
		navFolder.addView(IProfileUIConstants.ID_EXECUTION_STATS_VIEW);
		navFolder.addView(IProfileUIConstants.ID_CALL_GRAPH_VIEW);
		navFolder.addPlaceholder(IPageLayout.ID_RES_NAV);
		
		IFolderLayout toolsFolder= layout.createFolder("tools", IPageLayout.RIGHT, (float) 0.50, "navigator");
//		toolsFolder.addView(IDebugUIConstants.ID_VARIABLE_VIEW);
//		toolsFolder.addView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
//		toolsFolder.addPlaceholder(IDebugUIConstants.ID_EXPRESSION_VIEW);
//		toolsFolder.addPlaceholder(IDebugUIConstants.ID_REGISTER_VIEW);
		
		IFolderLayout outlineFolder= layout.createFolder("outline", IPageLayout.RIGHT, (float) 0.75, layout.getEditorArea());
		outlineFolder.addView(IPageLayout.ID_OUTLINE);
		
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);
	}

}
