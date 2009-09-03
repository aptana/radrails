/*******************************************************************************
 * Copyright (c) 2006 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.radrails.rails.internal.ui.autotest.IAutotestPreferenceConstants;
import org.radrails.rails.ui.RailsUIPlugin;

public class AutotestPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * Constructor.
	 */
	public AutotestPreferencePage() {
		super(GRID);

		setPreferenceStore(RailsUIPlugin.getInstance().getPreferenceStore());
		setDescription("Choose which test suites are run by autotest.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		Label runModes = new Label(getFieldEditorParent(), SWT.NULL);
		runModes.setText("Run autotest on:");
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.RUN_ON_SAVE, "Editor save",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.RUN_ON_INTERVAL,
				"Specified interval", getFieldEditorParent()));
		addField(new IntegerFieldEditor(
				IAutotestPreferenceConstants.INTERVAL_LENGTH,
				"Interval length (minutes)", getFieldEditorParent()));

		Label models = new Label(getFieldEditorParent(), SWT.NULL);
		models.setText("For models:");
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.MODEL_ASSOC_UNIT,
				"Associated unit test", getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.MODEL_ALL_UNIT, "All unit tests",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.MODEL_ALL_FUNCTIONAL,
				"All functional tests", getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.MODEL_ALL_INTEGRATION,
				"All integration tests", getFieldEditorParent()));

		Label controllers = new Label(getFieldEditorParent(), SWT.NULL);
		controllers.setText("For controllers:");
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.CONTROLLER_ASSOC_FUNCTIONAL,
				"Associated functional test", getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.CONTROLLER_ALL_FUNCTIONAL,
				"All functional tests", getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.CONTROLLER_ALL_UNIT,
				"All unit tests", getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.CONTROLLER_ALL_INTEGRATION,
				"All integration tests", getFieldEditorParent()));

		Label plugins = new Label(getFieldEditorParent(), SWT.NULL);
		plugins.setText("For plugins:");
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.PLUGIN_ASSOC,
				"Associated plugin test", getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.PLUGIN_ALL, "All plugin tests",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.PLUGIN_ALL_UNIT, "All unit tests",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.PLUGIN_ALL_FUNCTIONAL,
				"All functional tests", getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				IAutotestPreferenceConstants.PLUGIN_ALL_INTEGRATION,
				"All integration tests", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}

}
