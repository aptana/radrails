package com.aptana.rdt.internal.profiling;

import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import com.aptana.rdt.profiling.IProfilingListener;
import com.aptana.rdt.profiling.ProfilingPlugin;

/**
 * This view is meant to show you how the flow of the program executed. It lets you drill down the execution paths and see
 * which methods call which.
 * 
 * @author Chris Williams
 *
 */
public class CallGraphView extends ViewPart implements IProfilingListener {
	
	private static final String[] COLUMNS = { "Method", "Time [%]", "Time", "Invocations" };
	
	private TreeViewer fViewer;
	private ITreeContentProvider fContentProvider;
	private ITableLabelProvider fLabelProvider;
	private Tree tree;

	private boolean useGray = false;
	private Color gray;

	@Override
	public void createPartControl(Composite parent) {
		tree = new Tree(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		GridData treeLayoutData = new GridData(GridData.FILL, GridData.FILL, true, true);
		treeLayoutData.heightHint = tree.getItemHeight() * 16;
		treeLayoutData.widthHint = 300;
		tree.setLayoutData(treeLayoutData);
		tree.setLayout(gridLayout);
		
		fViewer= new TreeViewer(tree);
		
		final TreeColumn column0 = new TreeColumn(tree, SWT.LEFT);
		column0.setWidth(300);
		column0.setText(COLUMNS[0]);
		
		final TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setWidth(250);
		column1.setText(COLUMNS[1]);
		
		final TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
		column2.setWidth(100);
		column2.setText(COLUMNS[2]);
		
		final TreeColumn column3 = new TreeColumn(tree, SWT.LEFT);
		column3.setWidth(100);
		column3.setText(COLUMNS[3]);
		
		fViewer.setColumnProperties(COLUMNS);
		fViewer.setUseHashlookup(true);
			
		setProviders();
		
		gray = ProfilingPlugin.createPianoedColor(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		
		tree.addTreeListener(new TreeListener() { // Must do this after creating the viewer!
			
			public void treeExpanded(TreeEvent e) {
				TreeItem item = (TreeItem) e.item;
				TreeItem[] children = item.getItems();
				addProgressBars(children);
			}
		
			public void treeCollapsed(TreeEvent e) {}
		
		});
		
		createPopupMenu();
		
		ProfilingPlugin.getDefault().addProfilingListener(this);
		if (ProfilingPlugin.getDefault().lastProfilingResult() != null) {
			profilingEnded(ProfilingPlugin.getDefault().lastProfilingResult());
		}
	}

	/**
	 * Creates and registers the context menu
	 */
	private void createPopupMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new OpenMethodCallAction(tree));
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS)); // Allow other plugins to add here
			}
		});
		Menu menu = menuMgr.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, fViewer);
	}

	private void setProviders() {		
		fContentProvider= createContentProvider(); // content provider must be set before the label provider
		fViewer.setContentProvider(fContentProvider);
	
		fLabelProvider= createLabelProvider();
		fViewer.setLabelProvider(fLabelProvider);
	}
	
	private ITableLabelProvider createLabelProvider() {
		return new CallGraphLabelProvider();
	}

	private ITreeContentProvider createContentProvider() {
		return new CallGraphContentProvider();
	}

	@Override
	public void setFocus() {
		fViewer.getTree().setFocus();		
	}

	public void profilingEnded(List<ProfileThread> results) {
		if (fViewer != null) {
			fViewer.setInput(results);
			addProgressBars();
		}		
	}
	
	private void addProgressBars() {
		addProgressBars(tree.getItems());
	}
	
	private void addProgressBars(TreeItem[] items) {
		if (items == null || items.length == 0) return;
		int count = items.length;	
		for (int i = 0; i < count; i++) {
			TreeItem item = items[i];
			Object data = item.getData();
			if (data == null) return;
			
			if (useGray) item.setBackground(gray);
			useGray = !useGray;
			ProgressBar bar = new ProgressBar(tree, SWT.NONE);
			
			int percent = 0;
			if (data instanceof MethodCall) {
				percent = Math.round(((MethodCall) data).selfTimePercent());
			} else if (data instanceof ProfileThread) {
				percent = 100;
			}
			bar.setMinimum(0);
			bar.setMaximum(100);
			bar.setSelection(percent);
			TreeEditor editor = new TreeEditor(tree);
			editor.grabHorizontal = editor.grabVertical = true;
			editor.setEditor(bar, item, 1);
			addProgressBars(item.getItems());
		}
	}
	
	@Override
	public void dispose() {
		ProfilingPlugin.getDefault().removeProfilingListener(this);
		super.dispose();
	}
}
