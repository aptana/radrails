package org.radrails.rails.internal.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.progress.UIJob;
import org.radrails.rails.internal.ui.preferences.IRailsPreferenceConstants;
import org.radrails.rails.ui.RailsUIPlugin;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.IGemManager;

public class RailsInstallDialog extends UIJob
{

	private static final String INTERPRETER_PREF_PAGE_ID = "org.rubypeople.rdt.debug.ui.preferences.PreferencePageRubyInterpreter";
	private static final String RAILS_PATH_PREF_PAGE_ID = "org.radrails.rails.ui.preferences.Rails";

	private static final String SETUP_PREFERENCES = "Setup interpreter";
	private static final String CANCEL = "Cancel";
	private static final String INSTALL_RAILS = "Install Rails";
	private static final String SET_RAILS_PATH = "Set path to Rails";

	private static final String MSG_PART_1 = "It appears that you do not have the rails gems installed for your current Ruby interpreter (";
	private static final String MSG_PART_2 = ").\n\nIf you would like us to install rails, please click the \""
			+ INSTALL_RAILS
			+ "\" button below.\n\nIf that is the wrong interpreter, please add/modify your interpreter settings by clicking the \""
			+ SETUP_PREFERENCES
			+ "\" button.\n\nIf that is the correct interpreter and rails is installed, you can manually set the path to rails by clicking the \""
			+ SET_RAILS_PATH + "\" button below.";
	private static final String TITLE = "Rails not found";

	private IGemManager fGemManager;

	public RailsInstallDialog(IGemManager gemManger)
	{
		super("Checking rails gem is installed");
		this.fGemManager = gemManger;
	}

	protected String getMessage()
	{
		// FIXME Pull out strings into .properties files for translation!
		StringBuffer buffer = new StringBuffer(MSG_PART_1);
		buffer.append(RubyRuntime.getDefaultVMInstall().getInstallLocation());
		buffer.append(MSG_PART_2);
		return buffer.toString();
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor)
	{
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(Display.getDefault().getActiveShell(), TITLE,
				null, getMessage(), MessageDialog.WARNING, new String[] { INSTALL_RAILS, SETUP_PREFERENCES,
						SET_RAILS_PATH, CANCEL }, 0, "Don't ask me anymore!", false)
		{

			@Override
			protected void buttonPressed(int buttonId)
			{
				super.buttonPressed(buttonId);

				if (getToggleState() && getPrefStore() != null && getPrefKey() != null)
				{
					getPrefStore().setValue(getPrefKey(), getToggleState());
				}
			}

		};
		dialog.setPrefStore(RailsUIPlugin.getInstance().getPreferenceStore());
		dialog.setPrefKey(IRailsPreferenceConstants.RAILS_INSTALLED_CHECKER_DISABLED);
		int result = dialog.open();
		if (result == MessageDialog.CANCEL)
		{
			return Status.CANCEL_STATUS;
		}

		if (result == IDialogConstants.INTERNAL_ID)
		{
			Job job = new UIJob("Installing rails...")
			{

				public IStatus runInUIThread(IProgressMonitor monitor)
				{
					return fGemManager.installGem(new Gem("rails", Gem.ANY_VERSION, null), monitor);
				}
			};
			job.setUser(true);
			job.schedule();
		}
		else if (result == IDialogConstants.INTERNAL_ID + 1)
		{
			openPreferencePage(INTERPRETER_PREF_PAGE_ID);
		}
		else if (result == IDialogConstants.INTERNAL_ID + 2)
		{
			openPreferencePage(RAILS_PATH_PREF_PAGE_ID);
		}
		return Status.OK_STATUS;
	}

	private void openPreferencePage(String pageId)
	{
		PreferenceDialog prefDialog = PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(),
				pageId, null, null);
		prefDialog.setBlockOnOpen(false);
		prefDialog.open();
	}

}