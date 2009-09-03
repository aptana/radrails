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

import org.eclipse.eclipsemonkey.utils.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyClass;
import org.jruby.RubyFixnum;
import org.jruby.RubyNumeric;
import org.jruby.RubyObject;
import org.jruby.RubyRange;
import org.jruby.RubyString;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * @author Chris Williams
 */
@JRubyClass(name="Editor")
public class Editor extends RubyObject {

	private static final long serialVersionUID = 3459594505705212000L;
	
	private static RubyClass __clazz;

	private static RubyClass createClass(Ruby runtime) {
		if (__clazz == null) {
			RubyClass clazz = runtime.defineClass("Editor", runtime.getObject(), ObjectAllocator.NOT_ALLOCATABLE_ALLOCATOR);
			clazz.defineAnnotatedMethods(Editor.class);
			__clazz = clazz;
		}				
		return __clazz;
	}

	private IEditorPart _editor;

	public Editor(Ruby runtime, IEditorPart editor) {
		super(runtime, createClass(runtime));
		this._editor = editor;
	}

	/**
	 * getDocument
	 * 
	 * @return IDocument
	 */
	private IDocument getDocument() {
		ITextEditor editor = getTextEditor();
		IDocumentProvider dp = editor.getDocumentProvider();
		IDocument doc = dp.getDocument(editor.getEditorInput());

		return doc;
	}

	/**
	 * Get the _editor
	 * 
	 * @return _editor
	 */
	public ITextEditor getTextEditor() {
		IEditorPart part = this._editor;
		ITextEditor result = null;

		if (part instanceof ITextEditor) {
			result = (ITextEditor) part;
		}

		return result;
	}
	
	/**
	 * getSource
	 * 
	 * @return RubyString
	 */
	@JRubyMethod(name = "source")
	public RubyString getSource() {
		return getRuntime().newString(this.getDocument().get());
	}
	
	/**
	 * getSelectionRange
	 * 
	 * @return Object
	 */
	@JRubyMethod(name = "selection_range", alias = "selectionRange")
	public IRubyObject getSelectionRange() {
		ITextSelection ts = (ITextSelection) getTextEditor().getSelectionProvider().getSelection();
		RubyRange range = RubyRange.newRange(getRuntime(), getRuntime().getCurrentContext(), RubyFixnum.newFixnum(getRuntime(), ts.getOffset()), RubyFixnum.newFixnum(getRuntime(), ts.getOffset() + ts.getLength()), false);
		return range;
	}
	
	/**
	 * applyEdit
	 * 
	 * @param offset
	 * @param deleteLength
	 * @param insertText
	 * @throws BadLocationException 
	 */
	@JRubyMethod(name= "apply_edit", alias = "applyEdit", required = 3)
	public IRubyObject applyEdit(ThreadContext context, IRubyObject offset, IRubyObject deleteLength, IRubyObject insertText) throws BadLocationException {
		IEditorPart part = this._editor;

		if (part != null && part instanceof ITextEditor) {
			// get document
			IDocument doc = getDocument();
			try	{
				doc.replace(RubyNumeric.num2int(offset), RubyNumeric.num2int(deleteLength), insertText.convertToString().toString());
			}
			catch (BadLocationException e) {
				throw e;
			}
		}
		return getRuntime().getTrue();
	}
	
	/**
	 * getLineDelimiter
	 * 
	 * @return String
	 */
	@JRubyMethod(name = "line_delimeter", alias = "lineDelimeter")
	public RubyString getLineDelimiter() {
		IDocument document = this.getDocument();
		RubyString result = getRuntime().newString("\n");

		if (document != null) {
			String[] delims = document.getLegalLineDelimiters();
			if (delims.length > 0) {
				result = getRuntime().newString(delims[0]);
			}
		}

		return result;
	}
	
	/**
	 * close
	 * 
	 * @param save
	 */
	@JRubyMethod(name = "close", required = 1)
	public RubyBoolean close(ThreadContext context, IRubyObject saveObj)	{
		ITextEditor editor = this.getTextEditor();
		boolean save = false;
		if (saveObj instanceof RubyBoolean)
		{
			save = ((RubyBoolean) saveObj).isTrue();
		}
		if (editor != null)	{
			editor.close(save);
			return getRuntime().getTrue();
		}
		return getRuntime().getFalse();
	}
	
	/**
	 * save
	 */
	@JRubyMethod(name = "save")
	public RubyBoolean save() {
		if (this._editor != null) {
			this._editor.doSave(null);
			return getRuntime().getTrue();
		}
		return getRuntime().getFalse();
	}
	
	/**
	 * getId
	 * 
	 * @return String
	 */
	@JRubyMethod(name = "id")
	public RubyString getId() {
		RubyString result = getRuntime().newString(StringUtils.EMPTY);

		if (this._editor != null) {
			result =  getRuntime().newString(this._editor.getSite().getId());
		}

		return result;
	}
	
	/**
	 * getSourceLength
	 * 
	 * @return Scriptable
	 */
	@JRubyMethod(name = "source_length", alias = "sourceLength")
	public RubyFixnum getSourceLength() {
		return getRuntime().newFixnum(this.getDocument().getLength());
	}
	
	/**
	 * getCurrentOffset
	 * 
	 * @return int
	 */
	@JRubyMethod(name = "current_offset", alias = "currentOffset")
	public RubyFixnum getCurrentOffset() {
		/**
		 * ResultRef
		 */
		class ResultRef	{
			public int result = -1;
		}
		
		final IWorkbench workbench = PlatformUI.getWorkbench();
		Display display = workbench.getDisplay();
		final ResultRef result = new ResultRef();
		
		display.syncExec(new Runnable()	{
			public void run() {
				ITextSelection ts = (ITextSelection) getTextEditor().getSelectionProvider().getSelection();				
				result.result =  ts.getOffset();
			}
		});
		
		return getRuntime().newFixnum(result.result);
	}
	
	/**
	 * @param roffset
	 * @param rlength
	 */
	@JRubyMethod(name = "select_and_reveal", alias = "selectAndReveal", required = 2)
	public RubyBoolean selectAndReveal(ThreadContext context, final IRubyObject offset, final IRubyObject length) {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		Display display = workbench.getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				ITextEditor editor = getTextEditor();
				editor.selectAndReveal(RubyNumeric.num2int(offset), RubyNumeric.num2int(length));
			}
		});
		return getRuntime().getTrue();
	}
}
