package org.radrails.rails.internal.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.radrails.rails.core.IRailsConstants;
import org.radrails.rails.internal.core.RailsPlugin;

/**
 * @author matt
 * @author cwilliams
 */
public class RailsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public RailsPreferencePage()
	{
		super(GRID);
		setPreferenceStore(new AdaptedPreferences(RailsPlugin.getInstance().getPluginPreferences()));
		setDescription(Messages.RailsPreferencePage_Description);
	}

	protected void createFieldEditors()
	{		
		
		Group scriptPathsGroup = new Group(getFieldEditorParent(), SWT.NONE);
		scriptPathsGroup.setText("Script Paths");
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 3;
		scriptPathsGroup.setLayoutData(gd);
		scriptPathsGroup.setLayout(new GridLayout(3, false));
		
		addScriptPathsSection(scriptPathsGroup);
				
		// Toggle auto-open of Rails Shell
		Group group = new Group(getFieldEditorParent(), SWT.NONE);
		group.setText("Rails Shell");
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 3;
		gd.heightHint = 40;
		group.setLayoutData(gd);
		
		addField(new BooleanFieldEditor(IRailsConstants.AUTO_OPEN_RAILS_SHELL, "Automatically open Rails Shell on Startup", group));
	}

	private void addScriptPathsSection(Composite parent)
	{
		Label header = new Label(parent, SWT.WRAP);
		GridData headerData = new GridData();
		headerData.horizontalSpan = 3;
		headerData.verticalSpan = 3;
		headerData.widthHint = 500;
		header.setLayoutData(headerData);
		header.setText(Messages.RailsPreferencePage_HeaderText);
		header.setFont(parent.getFont());

		Link link = new Link(parent, SWT.WRAP);
		link.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				PreferencesUtil.createPreferenceDialogOn(getShell(),
						"org.rubypeople.rdt.debug.ui.preferences.PreferencePageRubyInterpreter", null, null);
			}
		});
		link.setText(Messages.RailsPreferencePage_InterpretersLinkText);
		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.widthHint = 500;
		link.setLayoutData(data);

		addField(new FileFieldEditor(IRailsConstants.PREF_RAILS_PATH, Messages.RailsPreferencePage_RailsPathLabel,
				parent));
		Label detectedPath = new Label(parent, SWT.NONE);
		detectedPath.setText(Messages.RailsPreferencePage_DetectedPathLabel);
		detectedPath.setFont(parent.getFont());
		
		Label detectedRailsPath = new Label(parent, SWT.NONE);
		String detectedRails = RailsPlugin.getInstance().getRailsPath();
		detectedRailsPath.setFont(parent.getFont());
		if (detectedRails != null)
		{
			detectedRailsPath.setText(detectedRails);
		}
		else
		{
			detectedRailsPath.setText("Not detected");
		}
		new Label(getFieldEditorParent(), SWT.NONE);

		Label space = new Label(parent, SWT.NONE);
		space.setLayoutData(data);

		addField(new FileFieldEditor(IRailsConstants.PREF_MONGREL_PATH,
				Messages.RailsPreferencePage_MongrelRailsPathLabel, parent));
		Label detectedPath2 = new Label(parent, SWT.NONE);
		detectedPath2.setText(Messages.RailsPreferencePage_DetectedPathLabel);
		detectedPath2.setFont(parent.getFont());
		
		Label detectedMongrelPath = new Label(parent, SWT.NONE);
		detectedMongrelPath.setFont(parent.getFont());
		String detectedMongrel = RailsPlugin.getInstance().getMongrelPath();
		if (detectedMongrel != null)
		{
			detectedMongrelPath.setText(detectedMongrel);
		}
		else
		{
			detectedMongrelPath.setText("Not detected");
		}
		new Label(parent, SWT.NONE);
	}

	public void init(IWorkbench workbench)
	{

	}

	private class AdaptedPreferences extends PreferenceStore
	{

		private Preferences fPrefs;

		public AdaptedPreferences(Preferences prefs)
		{
			fPrefs = prefs;
		}

		public String getString(String name)
		{
			return fPrefs.getString(name);
		}

		public void setValue(String name, String value)
		{
			fPrefs.setValue(name, value);
		}

		public String getDefaultString(String name)
		{
			return fPrefs.getDefaultString(name);
		}
		
		@Override
		public void setValue(String name, boolean value)
		{
			fPrefs.setValue(name, value);
		}
		
		@Override
		public boolean getDefaultBoolean(String name)
		{
			return fPrefs.getDefaultBoolean(name);
		}

		@Override
		public boolean getBoolean(String name)
		{
			return fPrefs.getBoolean(name);
		}
	}
}
