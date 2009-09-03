/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.aptana.ide.editor.erb;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.radrails.rails.ui.RailsUILog;
import org.radrails.rails.ui.RailsUIPlugin;

import com.aptana.ide.editor.erb.preferences.IPreferenceConstants;

/**
 * Abstract <code>VerifyKeyListener</code> with some constants and helper
 * methods. Subclasses implement specific peer closing behavior, such as
 * matching close brackets to open brackets or matching <code>end</code> to
 * <code>def</code>.
 * 
 * @author mkent
 * 
 */
public class ExpressionCloser implements VerifyKeyListener, IPropertyChangeListener {

	// Character constants
	private static final char SINGLE_QUOTE_STRING = '\'';
	private static final char DOUBLE_QUOTE_STRING = '"';
	private static final String COMMENT_HASH = "#";
	
	private SourceViewer fSourceViewer;
	private boolean fEnabled;

	/**
	 * Constructor.
	 * 
	 * @param sourceViewer
	 *            the <code>SourceViewer</code> that the closer operates on
	 */
	public ExpressionCloser(SourceViewer sourceViewer) {
		fSourceViewer = sourceViewer;
		fEnabled = ERBPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.EDITORS_AUTO_CLOSE);
		RailsUIPlugin.getInstance().getPreferenceStore()
				.addPropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IPreferenceConstants.EDITORS_AUTO_CLOSE)) {
			fEnabled = ((Boolean) event.getNewValue()).booleanValue();
		}
	}

	/**
	 * Helper method to move the cursor in the <code>SourceViewer</code>.
	 * 
	 * @param cursorDelta
	 *            the number of units to move the cursor, positive numbers will
	 *            move it forward and negative numbers will move it backward
	 */
	protected void moveCursor(int cursorDelta) {
		int widgetCursorPos = fSourceViewer.getTextWidget().getCaretOffset();
		int docCursorPos = fSourceViewer.widgetOffset2ModelOffset(widgetCursorPos);
		fSourceViewer.setSelectedRange(docCursorPos + cursorDelta, 0);
	}

	/**
	 * Helper method to determine if the current line in the
	 * <code>SourceViewer</code> is a Ruby comment.
	 * 
	 * @return true if the current line is a Ruby comment, false otherwise
	 */
	protected boolean isCurrentLineRubyComment() {
		try {
			IDocument document = fSourceViewer.getDocument();
			Point selection = fSourceViewer.getSelectedRange();
			int cursorOffset = selection.x;

			int lineNumber = document.getLineOfOffset(cursorOffset);
			int lineLength = document.getLineLength(lineNumber);
			int lineOffset = document.getLineOffset(lineNumber);

			String lineText = document.get(lineOffset, lineLength);

			if (lineText.trim().startsWith(COMMENT_HASH)) {
				return true;
			}
		} catch (BadLocationException e) {
			RailsUILog.logError("Bad location", e);
		}

		return false;
	}

	/**
	 * Helper method to determine if the cursor in the <code>SourceViewer</code>
	 * is currently inside a string literal.
	 * 
	 * @return true if the cursor is inside a string, false otherwise
	 */
	protected boolean isCursorInsideString() {
		return isCursorInsideString(DOUBLE_QUOTE_STRING) || isCursorInsideString(SINGLE_QUOTE_STRING);
	}

	/**
	 * @param lineText
	 * @param stringDelimiter
	 * @param cursorPos
	 * @return
	 */
	protected boolean isCursorInsideString(char stringDelimiter) {
		try {
			IDocument document = fSourceViewer.getDocument();
			Point selection = fSourceViewer.getSelectedRange();
			int cursorOffset = selection.x;

			int lineNumber = document.getLineOfOffset(cursorOffset);
			int lineLength = document.getLineLength(lineNumber);
			int lineOffset = document.getLineOffset(lineNumber);
			int cursorPos = cursorOffset - lineOffset;

			String lineText = document.get(lineOffset, lineLength);

			boolean loop = false;
			int searchPos = 0;
			do {
				int openQuotePos = lineText.indexOf(stringDelimiter, searchPos);
				int closeQuotePos = lineText.indexOf(stringDelimiter,
						openQuotePos + 1);

				// If there is a complete string literal on the line, check the
				// cursor position
				if ((openQuotePos > -1) && (closeQuotePos > -1)) {
					// If the cursor is inside the string literal, return true
					if ((cursorPos > openQuotePos)
							&& (cursorPos <= closeQuotePos)) {
						return true;
					}
					// Otherwise, check for the next string literal
					else {
						searchPos = closeQuotePos + 1;
						loop = true;
					}
				}
				loop = false;
			} while (loop);

		} catch (BadLocationException e) {
			RailsUILog.logError("Bad location", e);
		}

		return false;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
	 */
	public void verifyKey(VerifyEvent event) {
		if (!fEnabled) {
			return;
		}

		// Don't auto-close inside Ruby comments
		if(isCurrentLineRubyComment()) {
			return;
		}
		
		// Don't auto-close inside string literals
		if(isCursorInsideString()) {
			return;
		}
		
		IDocument document = fSourceViewer.getDocument();
		Point selection = fSourceViewer.getSelectedRange();
		int cursorOffset = selection.x;
		int selectionLength = selection.y;

		// If character is %, this is an open Ruby expression tag, complete it
		if (event.character == '%') {
			try {
				char prevChar = document.getChar(cursorOffset - 1);

				if (prevChar == '<') {
					StringBuffer sb = new StringBuffer();
					sb.append(event.character);
					sb.append('%');
					sb.append('>');

					document.replace(cursorOffset, selectionLength, sb
							.toString());

					event.doit = false;
					moveCursor(1);
				}

			} catch (BadLocationException e) {
				RailsUILog.logError("Bad location", e);
			}
		}
	}
}
