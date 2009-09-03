package org.eclipse.eclipsemonkey.lang.ruby.doms.editors;
/**
 * Copyright (c) 2007 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * @author Chris Williams
 */
@JRubyClass(name = "Editors")
public class Editors extends RubyObject {

	private static final long serialVersionUID = -3281382132109344902L;
	private static RubyClass __clazz;

	private static RubyClass createClass(Ruby runtime) {
		if (__clazz == null) {
			RubyClass clazz = runtime.defineClass("Editors", runtime.getObject(), ObjectAllocator.NOT_ALLOCATABLE_ALLOCATOR);
		 	clazz.defineAnnotatedMethods(Editors.class);
		 	__clazz = clazz;
		}		
		return __clazz;
	}
	
	public Editors(Ruby runtime) {
		super(runtime, createClass(runtime));
	}
	
	private static IEditorPart getActiveEditorStatic() {
		/**
		 * ActiveEditorRef
		 */
		class ActiveEditorRef
		{
			public IEditorPart activeEditor;
		}

		final IWorkbench workbench = PlatformUI.getWorkbench();
		final ActiveEditorRef activeEditor = new ActiveEditorRef();
		Display display = workbench.getDisplay();
		IEditorPart result;

		display.syncExec(new Runnable()
		{
			public void run()
			{
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

				// this can be null if you close all perspectives
				if (window != null && window.getActivePage() != null)
				{
					activeEditor.activeEditor = window.getActivePage().getActiveEditor();
				}
			}
		});

		result = activeEditor.activeEditor;

		return result;
	}
	
	/**
	 * getActiveEditor
	 * 
	 * @param thisObj
	 * @return IRubyObject
	 */
	@JRubyMethod(name = "active_editor", alias = {"get_active_editor", "activeEditor"})
	public IRubyObject getActiveEditor() {
		IEditorPart editor = getActiveEditorStatic();
		IRubyObject result;

		if (editor != null)
		{
			result = new Editor(getRuntime(), editor);
		}
		else
		{
			result = getRuntime().getNil();
		}

		return result;
	}
	
	/**
	 * getAll
	 * 
	 * @param thisObj
	 * @return IRubyObject
	 */
	@JRubyMethod(name = "all")
	public IRubyObject getAll() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final List<IRubyObject> editors = new ArrayList<IRubyObject>();
		Display display = workbench.getDisplay();

		display.syncExec(new Runnable()
		{
			public void run()
			{
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				IWorkbenchPage[] pages = window.getPages();

				for (int i = 0; i < pages.length; i++)
				{
					IWorkbenchPage page = pages[i];
					IEditorReference[] editorRefs = page.getEditorReferences();

					for (int j = 0; j < editorRefs.length; j++)
					{
						IEditorPart editor = editorRefs[j].getEditor(false);

						editors.add(new Editor(getRuntime(), editor));
					}
				}
			}
		});
		
		return getRuntime().newArray(editors);
	}

}
