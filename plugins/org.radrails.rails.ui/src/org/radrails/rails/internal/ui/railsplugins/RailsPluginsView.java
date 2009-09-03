package org.radrails.rails.internal.ui.railsplugins;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.radrails.rails.core.railsplugins.IRailsPluginListener;
import org.radrails.rails.core.railsplugins.RailsPluginDescriptor;
import org.radrails.rails.core.railsplugins.RailsPluginsManager;
import org.radrails.rails.core.railsplugins.RailsPluginsManager.RailsPluginException;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.actions.RailsProjectSelectionAction;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.RailsUIPlugin;
import org.radrails.rails.ui.browser.BrowserUtil;
import org.rubypeople.rdt.internal.ui.RubyExplorerTracker;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.RubyExplorerTracker.IRubyProjectListener;
import org.rubypeople.rdt.internal.ui.text.RubyColorManager;
import org.rubypeople.rdt.internal.ui.util.CollectionContentProvider;
import org.rubypeople.rdt.ui.TableViewerSorter;

/**
 * RailsPluginsView
 */
public class RailsPluginsView extends ViewPart implements ISelectionProvider, ISelectionChangedListener,
		IRailsPluginListener, IRubyProjectListener
{

	private static final String INSTALL = "Install";
	private static final String MANAGE = "Manage";
	private static final String PROJECT = "Current Rails Project: ";
	/**
	 * Remote Plugins / Install tab
	 */
	private TableViewer remotePluginTableViewer;
	private Table pluginsTable;

	/**
	 * Local Plugins / Manage Tab
	 */
	private TableViewer installedPluginsTableViewer;

	private Composite _composite;
	private Label projectLabel;
	private SashForm _outlineSash;
	/**
	 * Tabs
	 */
	private CTabFolder tabs;
	private CTabItem installTab;
	private CTabItem manageTab;

	private RubyColorManager fColorManager;

	private IProject project = null;
	private RailsProjectSelectionAction projectSelectionAction;

	/**
	 * For selections on both tabs
	 */
	private ISelection selection;
	private Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();

	private boolean useExternals = false;
	private boolean checkout = false;
	private int fLastSortColumn = -1;
	private boolean sortUp = false;

	/**
	 * Keeps a list of the TableEditors created, so that when we sort we can properly dispaose of them. Otherwise with
	 * the virtual table we get old table editors "stuck" on rows where the underlying data has changed and the editor
	 * has bad values.
	 */
	private List<TableEditor> fTableEditors = new ArrayList<TableEditor>();

	private Map<TableItem, Link> fLinks = new HashMap<TableItem, Link>();
	private Map<TableItem, Widget> fImages = new HashMap<TableItem, Widget>();

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent)
	{
		fColorManager = new RubyColorManager(true);

		RatingImage.init(parent.getDisplay());
		// create main container
		_composite = createComposite(parent);

		projectLabel = new Label(_composite, SWT.LEFT);
		projectLabel.setForeground(fColorManager.getColor(new RGB(128, 128, 128)));
		projectLabel.setText(PROJECT);
		GridData plData = new GridData(SWT.FILL, SWT.FILL, true, false);
		plData.horizontalIndent = 5;
		plData.verticalIndent = 5;
		projectLabel.setLayoutData(plData);

		// create tab sash
		_outlineSash = this.createSash(_composite);

		createTabs(_composite);

		getSite().setSelectionProvider(this);

		projectSelectionAction = new RailsProjectSelectionAction();
		projectSelectionAction.setListener(this);
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(projectSelectionAction);

		createPopupMenu();
		getViewSite().getActionBars().getMenuManager().add(new ToggleExternals());
		getViewSite().getActionBars().getMenuManager().add(new ToggleCheckout());
		RailsPluginsManager.addRailsPluginListener(this);
	}

	/**
	 * createComposite
	 * 
	 * @param parent
	 * @return Composite
	 */
	private Composite createComposite(Composite parent)
	{
		GridLayout contentAreaLayout = new GridLayout();
		contentAreaLayout.numColumns = 1;
		contentAreaLayout.marginHeight = 0;
		contentAreaLayout.marginWidth = 0;
		contentAreaLayout.makeColumnsEqualWidth = false;

		Composite result = new Composite(parent, SWT.NONE);

		result.setLayout(contentAreaLayout);
		result.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return result;
	}

	/**
	 * createSash
	 * 
	 * @param parent
	 * @return sash form
	 */
	private SashForm createSash(Composite parent)
	{
		// create layout data
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.exclude = true;

		// create form
		SashForm result = new SashForm(parent, SWT.VERTICAL | SWT.BORDER);

		// set layout data
		result.setLayoutData(gridData);
		result.setVisible(false);

		return result;
	}

	private void createTabs(Composite parent)
	{
		this.tabs = new CTabFolder(parent, SWT.TOP | SWT.BORDER);

		this.tabs.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabs.addSelectionListener(new SelectionListener()
		{

			public void widgetSelected(SelectionEvent e)
			{
				if (e.item instanceof CTabItem)
				{
					remotePluginTableViewer.setSelection(null);
					installedPluginsTableViewer.setSelection(null);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

		});

		installTab = createInstallTab();
		manageTab = createManageTab();

		this.tabs.setSelection(installTab);
	}

	private CTabItem createManageTab()
	{
		createTabLabel(MANAGE);

		SashForm manageForm = new SashForm(this.tabs, SWT.NONE);
		manageForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		CTabItem sourceTab = new CTabItem(this.tabs, SWT.NONE);
		sourceTab.setText(MANAGE);
		sourceTab.setControl(manageForm);

		createInstalledPluginsTable(manageForm);

		return sourceTab;
	}

	private CTabItem createInstallTab()
	{
		createTabLabel(INSTALL);

		SashForm preForm = new SashForm(tabs, SWT.NONE);
		preForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		CTabItem tab = new CTabItem(tabs, SWT.NONE);
		tab.setText(INSTALL);
		tab.setControl(preForm);

		createRemotePluginsTable(preForm);

		return tab;
	}

	private void createTabLabel(String label)
	{
		Composite previewComp = new Composite(this._outlineSash, SWT.NONE);
		previewComp.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label outlineLabel = new Label(previewComp, SWT.NONE);
		outlineLabel.setText(label);
		outlineLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	private void createRemotePluginsTable(Composite parent)
	{
		pluginsTable = new Table(parent, SWT.VIRTUAL | SWT.SINGLE | SWT.FULL_SELECTION);
		remotePluginTableViewer = new TableViewer(pluginsTable);
		pluginsTable.setHeaderVisible(true);
		pluginsTable.setLinesVisible(true);
		pluginsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		createColumn(pluginsTable, "Name", 150).addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				sort(0);
				super.widgetSelected(e);
			}

		});
		;
		createColumn(pluginsTable, "Rating", 75).addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				sort(1);
				super.widgetSelected(e);
			}

		});
		createColumn(pluginsTable, "License", 100).addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				sort(2);
				super.widgetSelected(e);
			}

		});
		;
		createColumn(pluginsTable, "Home", 175).addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				sort(3);
				super.widgetSelected(e);
			}

		});
		;

		remotePluginTableViewer.setLabelProvider(new RailsPluginsLabelProvider());
		remotePluginTableViewer.setContentProvider(new CollectionContentProvider());
		remotePluginTableViewer.addSelectionChangedListener(this);

		try
		{
			remotePluginTableViewer.setInput(RailsPluginsManager.getInstance().getPlugins());
		}
		catch (RailsPluginException e)
		{
			RailsUILog.log(e);
		}
		pluginsTable.addListener(SWT.SetData, new Listener()
		{
			public void handleEvent(Event event)
			{
				TableItem item = (TableItem) event.item;
				clearExistingForItem(item);
				createLink(item);
				setRatingImage(item);
			}
		});
		createLinksForHomepage();
	}

	protected void clearExistingForItem(TableItem item)
	{
		if (item == null)
			return;
		Link link = fLinks.remove(item);
		if (link != null)
		{
			link.dispose();
			link = null;
		}
		// TODO Remove table editor!
		// fTableEditors.

		Widget image = fImages.remove(item);
		if (image != null)
		{
			image.dispose();
			image = null;
		}
	}

	protected void sort(final int columnIndex)
	{
		if (fLastSortColumn == columnIndex)
		{
			sortUp = !sortUp;
		}
		fLastSortColumn = columnIndex;
		List<RailsPluginDescriptor> original = new ArrayList<RailsPluginDescriptor>();
		try
		{
			original = RailsPluginsManager.getInstance().getPlugins();
		}
		catch (RailsPluginException e)
		{
			RailsUILog.log(e);
		}
		Collections.sort(original, new Comparator<RailsPluginDescriptor>()
		{

			public int compare(RailsPluginDescriptor first, RailsPluginDescriptor second)
			{
				int value = 0;
				switch (columnIndex)
				{
					case 0:
						value = first.getRawName().compareTo(second.getRawName());
						break;
					case 1:
						value = Float.compare(first.getRating(), second.getRating());
						break;
					case 2:
						value = first.getLicense().compareTo(second.getLicense());
						break;
					case 3:
						value = first.getHome().compareTo(second.getHome());
						break;
				}
				if (sortUp)
				{
					value = -(value);
				}
				return value;
			}

		});
		pluginsTable.setItemCount(original.size());
		pluginsTable.clearAll();
		// Dispose custom table editors (or they "stick" to rows even though data has changed
		for (TableEditor editor : fTableEditors)
		{
			editor.getEditor().dispose();
			editor.dispose();
		}
		fTableEditors.clear();
		pluginsTable.setSortDirection(sortUp ? SWT.UP : SWT.DOWN);
		remotePluginTableViewer.setInput(original);
	}

	/**
	 * Sets the rating image on a table item
	 * 
	 * @param item
	 */
	protected void setRatingImage(TableItem item)
	{
		// Generate a special rating image depending on value
		RailsPluginDescriptor desc = (RailsPluginDescriptor) item.getData();
		float rating = desc.getRating();
		if (rating == -1)
		{
			return;
		}
		Image image = RatingImage.createRatingImage(rating, pluginsTable.getDisplay());
		Label bar = new Label(pluginsTable, SWT.NULL);
		bar.setImage(image);
		bar.setBackground(pluginsTable.getBackground());
		TableEditor editor = new TableEditor(pluginsTable);
		editor.grabHorizontal = editor.grabVertical = true;
		editor.setEditor(bar, item, 1);
		fTableEditors.add(editor);
		fImages.put(item, bar);
	}

	private void createInstalledPluginsTable(Composite parent)
	{
		installedPluginsTableViewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION);
		Table pluginsTable = installedPluginsTableViewer.getTable();
		pluginsTable.setHeaderVisible(true);
		pluginsTable.setLinesVisible(false);
		pluginsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		createColumn(pluginsTable, "Name", 300);

		installedPluginsTableViewer.setLabelProvider(new InstalledRailsPluginsLabelProvider());
		installedPluginsTableViewer.setContentProvider(new CollectionContentProvider());
		TableViewerSorter.bind(installedPluginsTableViewer);
		installedPluginsTableViewer.addSelectionChangedListener(this);
		installedPluginsTableViewer.setInput(RailsPluginsManager.getInstalledPlugins(RailsUIPlugin
				.getSelectedOrOnlyRailsProject()));
		getProjectTracker().addProjectListener(this);
		if (this.project == null)
		{
			Set<IProject> projects = RailsPlugin.getRailsProjects();
			if (projects != null && projects.size() > 0)
				projectSelected(projects.iterator().next());
		}
	}

	private RubyExplorerTracker getProjectTracker()
	{
		return RubyPlugin.getDefault().getProjectTracker();
	}

	private class ToggleExternals extends Action
	{
		/**
		 * ToggleExternals
		 */
		public ToggleExternals()
		{
			super("svn:externals", Action.AS_CHECK_BOX);
			setChecked(useExternals);
		}

		/**
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run()
		{
			super.run();
			useExternals = isChecked();
		}
	}

	private class ToggleCheckout extends Action
	{

		/**
		 * ToggleCheckout
		 */
		public ToggleCheckout()
		{
			super("svn:checkout", Action.AS_CHECK_BOX);
			setChecked(checkout);
		}

		/**
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run()
		{
			super.run();
			checkout = isChecked();
		}
	}

	/**
	 * Use externals
	 * 
	 * @return - true if using externals
	 */
	public boolean useExternals()
	{
		return useExternals;
	}

	/**
	 * Use checkout
	 * 
	 * @return - true if using checkout
	 */
	public boolean checkout()
	{
		return checkout;
	}

	private void createLinksForHomepage()
	{
		TableItem[] items = pluginsTable.getItems();
		for (int i = 0; i < items.length; i++)
		{
			createLink(items[i]);
		}
	}

	private void createLink(TableItem item)
	{
		TableEditor editor = new TableEditor(pluginsTable);
		RailsPluginDescriptor desc = (RailsPluginDescriptor) item.getData();
		if (desc == null)
			return;
		final String url = desc.getHome();
		if (url == null || url.trim().length() == 0)
			return;
		Link link = new Link(pluginsTable, SWT.NONE);
		link.addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					BrowserUtil.openBrowser(url);
				}
				catch (PartInitException e1)
				{
					RailsUILog.log(e1);
				}
				catch (MalformedURLException e1)
				{
					RailsUILog.log(e1);
				}
				super.widgetSelected(e);
			}

		});
		link.setBackground(pluginsTable.getBackground());
		link.setText("<a href=\"" + url + "\">" + url + "</a>");
		editor.grabHorizontal = true;
		editor.setEditor(link, item, 3);
		fTableEditors.add(editor);
		fLinks.put(item, link);
	}

	private TableColumn createColumn(Table table, String string, int size)
	{
		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText(string);
		column.setWidth(size);
		return column;
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose()
	{
		try
		{
			for (Widget w : fLinks.values())
			{
				w.dispose();
			}
			for (Link l : fLinks.values())
			{
				l.dispose();
			}
			RatingImage.dispose();
			fColorManager.dispose();
			remotePluginTableViewer = null;
			installedPluginsTableViewer = null;
			_composite = null;
			RailsPluginsManager.removeRailsPluginListener(this);
			getProjectTracker().removeProjectListener(this);
		}
		finally
		{
			super.dispose();
		}
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus()
	{
		remotePluginTableViewer.getTable().setFocus();
	}

	/**
	 * Creates and registers the context menu
	 */
	private void createPopupMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);

		menuMgr.addMenuListener(new IMenuListener()
		{
			public void menuAboutToShow(IMenuManager manager)
			{
				IContributionItem[] items = getViewSite().getActionBars().getToolBarManager().getItems();
				for (int i = 0; i < items.length; i++)
				{
					if (items[i] instanceof ActionContributionItem)
					{
						ActionContributionItem aci = (ActionContributionItem) items[i];
						if (aci.getAction() != null && aci.getAction() != projectSelectionAction)
						{
							manager.add(aci.getAction());
						}
					}
				}
			}
		});
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		Menu menu = menuMgr.createContextMenu(remotePluginTableViewer.getControl());
		remotePluginTableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, remotePluginTableViewer);
	}

	/**
	 * Refreshes the list of plugins.
	 */
	public void refreshPlugins()
	{
		if (installTabSelected())
		{
			Job j = new Job("Refresh plugins")
			{
				protected IStatus run(IProgressMonitor monitor)
				{
					try
					{
						monitor.beginTask("Loading Rails plugins", 30);
						SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 20);
						RailsPluginsManager.getInstance().updatePlugins(subMonitor);
						final List plugins = RailsPluginsManager.getInstance().getPlugins();
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable()
						{
							public void run()
							{
								pluginsTable.clearAll();
								remotePluginTableViewer.setInput(plugins);
							}
						});
						monitor.worked(10);
					}
					catch (Exception e)
					{
						RailsUILog.logError("Error loading Rails plugin list", e);
						MessageDialog.openError(getSite().getShell(), "Error refreshing plugin list", e.getMessage());
					}
					finally
					{
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			};
			j.schedule();
		}
		else
		{ // refresh locally installed plugins
			installedPluginsTableViewer.setInput(RailsPluginsManager.getInstalledPlugins(RailsUIPlugin
					.getSelectedOrOnlyRailsProject()));
		}
	}

	/**
	 * Is install tab selected
	 * 
	 * @return - true if selected
	 */
	public boolean installTabSelected()
	{
		CTabItem selectedTab = tabs.getSelection();
		return selectedTab.equals(installTab);
	}

	/**
	 * Is manage tab selected
	 * 
	 * @return - true if selected
	 */
	public boolean manageTabSelected()
	{
		return !installTabSelected();
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection()
	{
		return selection;
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection)
	{
		// do nothing
		this.selection = selection;
		for (Iterator iter = listeners.iterator(); iter.hasNext();)
		{
			ISelectionChangedListener listener = (ISelectionChangedListener) iter.next();
			listener.selectionChanged(new SelectionChangedEvent(this, selection));
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event)
	{
		setSelection(event.getSelection());
	}

	/**
	 * Adds an installed plugin to the table
	 * 
	 * @param project
	 * @param plugin
	 */
	public void pluginInstalled(IProject project, RailsPluginDescriptor plugin)
	{
		if (project.equals(RailsUIPlugin.getSelectedOrOnlyRailsProject()))
		{
			RailsPluginDescriptor copy = new RailsPluginDescriptor();
			copy.setProperty(RailsPluginDescriptor.NAME, plugin.getName());
			installedPluginsTableViewer.add(copy);
		}
	}

	/**
	 * Removes an installed plugin from the table
	 * 
	 * @param project
	 * @param plugin
	 */
	public void pluginRemoved(IProject project, RailsPluginDescriptor plugin)
	{
		if (project.equals(RailsUIPlugin.getSelectedOrOnlyRailsProject()))
			installedPluginsTableViewer.remove(plugin);
	}

	/**
	 * @see org.radrails.rails.core.railsplugins.IRailsPluginListener#remotePluginsRefreshed()
	 */
	public void remotePluginsRefreshed()
	{
		// do nothing(?)
	}

	/**
	 * Listen to project selection changes in Ruby Explorer
	 * 
	 * @param project
	 */
	public void projectSelected(IProject project)
	{
		if (installedPluginsTableViewer == null)
		{
			return;
		}
		if (RailsPlugin.hasRailsNature(project) && project.exists() && project.isOpen())
		{
			projectLabel.setText(PROJECT + project.getName());
			this.project = project;
		}
		else if (project == null || !project.exists())
		{
			projectLabel.setText(PROJECT + "<Select a Rails project>");
			_composite.layout(true, true);
			this.project = null;
		}
		_composite.layout(true, true);
		installedPluginsTableViewer.setInput(RailsPluginsManager.getInstalledPlugins(project));
	}

	/**
	 * Gets the project the plugins view is currently set to use
	 * 
	 * @return - project being used or null
	 */
	public IProject getProject()
	{
		return this.project;
	}

}
