package org.radrails.rails.internal.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class WizardMessages extends NLS {
	
	private static final String BUNDLE_NAME = WizardMessages.class.getName();
	
	public static String NewRailsProjectWizardAction_text;
	public static String NewRailsProjectWizardAction_description;
	public static String NewRailsProjectWizardAction_tooltip;

	public static String WizardNewRailsProjectPage_Invalid_Project_Name_msg;
	public static String WizardNewRailsProjectPage_Rails_not_installed_msg;
	public static String WizardNewRailsProjectPage_Generate_project_skeleton;
	public static String WizardNewRailsProjectPage_Options_group_heading;
	public static String WizardNewRailsProjectPage_Install_rails_link;
	public static String WizardNewRailsProjectPage_Rails_version_label;
	public static String WizardNewRailsProjectPage_Start_server_label;
	public static String WizardNewRailsProjectPage_Latest_rails_version_label;
	public static String WizardNewRailsProjectPage_Database_options_label;

	static {
		NLS.initializeMessages(BUNDLE_NAME, WizardMessages.class);
	}
}
