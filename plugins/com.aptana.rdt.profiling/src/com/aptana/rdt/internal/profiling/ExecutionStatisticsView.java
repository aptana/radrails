package com.aptana.rdt.internal.profiling;

import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.rubypeople.rdt.ui.TableViewerSorter;

import com.aptana.rdt.profiling.IProfilingListener;
import com.aptana.rdt.profiling.ProfilingPlugin;

/**
 * Equivalent to Netbeans "Hot Spots" view: http://www.javaperformancetuning.com/tools/netbeansprofiler/results-cpu.png
 * This view is meant to show you all the methods in a giant table and sort by various timings/call counts. basically you'd
 * use this view to sort by time or call count and find particular methods of your program that are very slow.
 * 
 * @author Chris Williams
 *
 */
public class ExecutionStatisticsView extends ViewPart implements IProfilingListener {
	
	private TableViewer fTableViewer;
	private Color gray;
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		fTableViewer = createStatisticsTable(parent);
		gray = ProfilingPlugin.createPianoedColor(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		
		createPopupMenu();		
		
		ProfilingPlugin.getDefault().addProfilingListener(this);
		if (ProfilingPlugin.getDefault().lastProfilingResult() != null) {
			profilingEnded(ProfilingPlugin.getDefault().lastProfilingResult());
		}
	}
	
	private TableViewer createStatisticsTable(Composite parent)
	{
		TableViewer view = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION);
		Table serverTable = view.getTable();
		serverTable.setHeaderVisible(true);
		serverTable.setLinesVisible(true);
		serverTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TableColumn classCol = new TableColumn(serverTable, SWT.LEFT);
		classCol.setText("Class");
		classCol.setWidth(150);

		TableColumn nameCol = new TableColumn(serverTable, SWT.LEFT);
		nameCol.setText("Method");
		nameCol.setWidth(150);

		TableColumn selfPercentCol = new TableColumn(serverTable, SWT.LEFT);
		selfPercentCol.setText("Self Time [%]");
		selfPercentCol.setWidth(100);
	   		
		TableColumn statusCol = new TableColumn(serverTable, SWT.LEFT);
		statusCol.setText("Invocations");
		statusCol.setWidth(50);
		
		TableColumn selfCol = new TableColumn(serverTable, SWT.LEFT);
		selfCol.setText("Self time");
		selfCol.setWidth(50);
		
		TableColumn waitCol = new TableColumn(serverTable, SWT.LEFT);
		waitCol.setText("Wait time");
		waitCol.setWidth(50);
		
		TableColumn childCol = new TableColumn(serverTable, SWT.LEFT);
		childCol.setText("Child time");
		childCol.setWidth(50);
		
		TableColumn avgTimeCol = new TableColumn(serverTable, SWT.LEFT);
		avgTimeCol.setText("Time per invocation");
		avgTimeCol.setWidth(150);
		
		view.setLabelProvider(new ExecutionLabelProvider());
		view.setContentProvider(new ExecutionContentProvider());
		TableViewerSorter.bind(view);
		return view;
	}

	/**
	 * Creates and registers the context menu
	 */
	private void createPopupMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new OpenMethodCallAction(fTableViewer.getTable()));
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS)); // Allow other plugins to add here
			}
		});
		Menu menu = menuMgr.createContextMenu(fTableViewer.getControl());
		fTableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, fTableViewer);
	}

	@Override
	public void setFocus() {
		fTableViewer.getTable().setFocus();
	}

	public void profilingEnded(List<ProfileThread> results) {
		if (fTableViewer != null) {
			fTableViewer.setInput(results);
			fTableViewer.setSorter(new TableViewerSorter(4)); // sort by self time by default		
			addProgressBars();       
		}
	}

	private void addProgressBars() {
		Table table = fTableViewer.getTable();
		int count = fTableViewer.getTable().getItemCount();
		for (int i = 0; i < count; i++) {
			TableItem item = table.getItem(i);
			if (i % 2 == 0) item.setBackground(gray);
			ProgressBar bar = new ProgressBar(table, SWT.NONE);
			MethodCall call = (MethodCall) item.getData();			
			bar.setMinimum(0);
			bar.setMaximum(100);
			bar.setSelection(Math.round(call.selfTimePercent()));
			TableEditor editor = new TableEditor(table);
			editor.grabHorizontal = editor.grabVertical = true;
			editor.setEditor(bar, item, 2);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		ProfilingPlugin.getDefault().removeProfilingListener(this);
	}

}
