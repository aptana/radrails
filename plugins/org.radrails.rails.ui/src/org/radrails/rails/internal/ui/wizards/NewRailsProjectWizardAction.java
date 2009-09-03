package org.radrails.rails.internal.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.radrails.rails.ui.RailsUIPlugin;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.ui.actions.AbstractOpenWizardAction;

public class NewRailsProjectWizardAction extends AbstractOpenWizardAction {

	/**
	 * Creates an instance of the <code>NewRailsProjectWizardAction</code>.
	 */
	public NewRailsProjectWizardAction() {
		setText(WizardMessages.NewRailsProjectWizardAction_text); 
		setDescription(WizardMessages.NewRailsProjectWizardAction_description); 
		setToolTipText(WizardMessages.NewRailsProjectWizardAction_tooltip); 
		ImageDescriptor image = AbstractUIPlugin.imageDescriptorFromPlugin(
				RailsUIPlugin.getInstance().getBundle().getSymbolicName(),
				"icons/newproj_wiz.gif");
		setImageDescriptor(image);
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IRubyHelpContextIds.OPEN_PROJECT_WIZARD_ACTION);
		setShell(RubyPlugin.getActiveWorkbenchShell());
	}
	
	/* (non-Javadoc)
	 * @see org.rubypeople.rdt.ui.actions.AbstractOpenWizardAction#createWizard()
	 */
	protected final INewWizard createWizard() throws CoreException {
		return new NewRailsProjectWizard();
	}
	
	/* (non-Javadoc)
	 * @see org.rubypeople.rdt.ui.actions.AbstractOpenWizardAction#doCreateProjectFirstOnEmptyWorkspace(Shell)
	 */
	protected boolean doCreateProjectFirstOnEmptyWorkspace(Shell shell) {
		return true; // can work on an empty workspace
	}

}
