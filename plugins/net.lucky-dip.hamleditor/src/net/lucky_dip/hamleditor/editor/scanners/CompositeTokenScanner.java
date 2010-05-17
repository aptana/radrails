/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.radrails.org/legal/cpl-v10.html
 *******************************************************************************/
package net.lucky_dip.hamleditor.editor.scanners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Abstract implementation of a composite token scanner. This scanner uses
 * an IPartitionTokenScanner to partition the given range, then uses the provided
 * token scanners to tokenize each partition.
 * 
 * @author mkent
 * 
 */
public abstract class CompositeTokenScanner implements ITokenScanner, IAbstractManagedScanner {

	protected IPartitionTokenScanner fPartitionScanner;

	protected Map fTokenScanners;

	protected LinkedList fTokensInfo;

	protected Object[] fCurTokenInfo;

	public CompositeTokenScanner(IPartitionTokenScanner partitionScanner) {
		fPartitionScanner = partitionScanner;
		fTokenScanners = createTokenScanners();
		fTokensInfo = new LinkedList();
	}

	protected Map createTokenScanners() {
		return new HashMap();
	}

	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		Iterator i = fTokenScanners.values().iterator();
		while(i.hasNext()) {
			IAbstractManagedScanner scanner = (IAbstractManagedScanner) i.next();
			if(scanner.affectsBehavior(event)) {
				scanner.adaptToPreferenceChange(event);
			}
		}
		
	}

	public boolean affectsBehavior(PropertyChangeEvent event) {
		boolean affects = false;
		Iterator i = fTokenScanners.values().iterator();
		while(i.hasNext()) {
			IAbstractManagedScanner scanner = (IAbstractManagedScanner) i.next();
			affects = affects || scanner.affectsBehavior(event);
		}
		return affects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(org.eclipse.jface.text.IDocument,
	 *      int, int)
	 */
	public void setRange(IDocument document, int offset, int length) {
		fPartitionScanner.setRange(document, offset, length);

		Collection parts = partitionRange(document, offset, length);
		tokenizePartitions(document, offset, length, parts);
	}

	protected Collection partitionRange(IDocument document, int offset,
			int length) {
		// Partition the ERb expression
		Collection parts = new ArrayList();
		IToken pTok = fPartitionScanner.nextToken();

		while (!pTok.isEOF()) {
			String pType = (String) pTok.getData();
			int tokLength = fPartitionScanner.getTokenLength();
			int tokOffset = fPartitionScanner.getTokenOffset();

			// Null content type means DEFAULT_CONTENT_TYPE
			if (pType == null) {
				// Concatenate into one DEFAULT partition
				while (pType == null && !pTok.isEOF()) {
					pTok = fPartitionScanner.nextToken();

					pType = (String) pTok.getData();
					tokLength += fPartitionScanner.getTokenLength();
				}

				// Subtract length of last token
				tokLength -= fPartitionScanner.getTokenLength();

				// Store token
				parts.add(new Object[] { IDocument.DEFAULT_CONTENT_TYPE,
						new Integer(tokOffset), new Integer(tokLength) });

				// Advance the offset and length to next token
				tokLength = fPartitionScanner.getTokenLength();
				tokOffset = fPartitionScanner.getTokenOffset();
			}

			// Store next token unless EOF
			if (!pTok.isEOF()) {
				parts.add(new Object[] { pType, new Integer(tokOffset),
						new Integer(tokLength) });
			}

			pTok = fPartitionScanner.nextToken();
		}
		return parts;
	}

	protected void tokenizePartitions(IDocument document, int offset,
			int length, Collection parts) {
		// Tokenize each sub-partition
		Iterator i = parts.iterator();
		while (i.hasNext()) {
			Object[] pInfo = (Object[]) i.next();
			String pType = (String) pInfo[0];
			Integer tokOffset = (Integer) pInfo[1];
			Integer tokLength = (Integer) pInfo[2];

			// Prepare the token scanner for the partition
			ITokenScanner tScan = (ITokenScanner) fTokenScanners.get(pType);
			tScan
					.setRange(document, tokOffset.intValue(), tokLength
							.intValue());

			IToken tk = tScan.nextToken();
			Integer tkLength = new Integer(tScan.getTokenLength());
			Integer tkOffset = new Integer(tScan.getTokenOffset());

			while (!tk.isEOF()) {
				Object[] tkInfo = new Object[] { tk, tkOffset, tkLength };
				fTokensInfo.add(tkInfo);

				tk = tScan.nextToken();
				tkLength = new Integer(tScan.getTokenLength());
				tkOffset = new Integer(tScan.getTokenOffset());
			}
		}

		// Insert an EOF token at the end
		Object[] eofInfo = new Object[] { Token.EOF,
				new Integer(offset + length), new Integer(0) };

		fTokensInfo.add(eofInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		// Get next token from stack
		fCurTokenInfo = (Object[]) fTokensInfo.removeFirst();
		IToken ctok = (IToken) fCurTokenInfo[0];

		return ctok;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenOffset()
	 */
	public int getTokenOffset() {
		Integer to = (Integer) fCurTokenInfo[1];
		return to.intValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenLength()
	 */
	public int getTokenLength() {
		Integer tl = (Integer) fCurTokenInfo[2];
		return tl.intValue();
	}
}
