/*******************************************************************************
 * Copyright (c) 2006 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.radrails.org/legal/epl-v10.html
 *******************************************************************************/

package net.lucky_dip.sasseditor.editor;

import net.lucky_dip.sasseditor.editor.scanners.SassPartitionScanner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;

/**
 * 
 * 
 * @author brad
 * 
 */
public class SassIndentLineAutoEditStrategy implements IAutoEditStrategy {
	public static final String INDENT_STRING = "  ";

	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (c.length == 0 && c.text != null
				&& TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1) {
			autoIndentAfterNewLine(d, c);
		}
		else if (c.text.equals("\t")) {
			c.text = INDENT_STRING;
		}
	}

	/**
	 * Adds two spaces to the indentation of the previous line.
	 * 
	 * @param d
	 *            the document to work on
	 * @param c
	 *            the command to deal with
	 */
	private void autoIndentAfterNewLine(IDocument d, DocumentCommand c) {

		if (c.offset == -1 || d.getLength() == 0)
			return;

		try {
			String lastPartitionType = null;
			if (d instanceof IDocumentExtension3) {
				IDocumentExtension3 id3 = (IDocumentExtension3) d;
				lastPartitionType = id3.getDocumentPartitioner(SassEditor.SASS_PARTITIONING)
						.getContentType(c.offset - 1);
			}

			// find start of line
			int p = (c.offset == d.getLength() ? c.offset - 1 : c.offset);
			IRegion info = d.getLineInformationOfOffset(p);
			int start = info.getOffset();

			// find white spaces
			int end = findEndOfWhiteSpace(d, start, c.offset);

			// indent if we just finished an element
			StringBuffer buf = new StringBuffer(c.text);
			if (lastPartitionType != null
					&& (lastPartitionType.equalsIgnoreCase(SassPartitionScanner.SASS_CLASS)
						|| lastPartitionType.equalsIgnoreCase(SassPartitionScanner.SASS_ID))) {
				buf.append("  ");
			}

			if (end > start) {
				// append to input
				buf.append(d.get(start, end - start));
			}

			c.text = buf.toString();

		}
		catch (BadLocationException excp) {
			// stop work
		}
	}

	/**
	 * Returns the first offset greater than <code>offset</code> and smaller
	 * than <code>end</code> whose character is not a space or tab character.
	 * If no such offset is found, <code>end</code> is returned.
	 * 
	 * @param document
	 *            the document to search in
	 * @param offset
	 *            the offset at which searching start
	 * @param end
	 *            the offset at which searching stops
	 * @return the offset in the specified range whose character is not a space
	 *         or tab
	 * @exception BadLocationException
	 *                if position is an invalid range in the given document
	 */
	protected int findEndOfWhiteSpace(IDocument document, int offset, int end)
			throws BadLocationException {
		while (offset < end) {
			char c = document.getChar(offset);
			if (c != ' ' && c != '\t') {
				return offset;
			}
			offset++;
		}
		return end;
	}
}
