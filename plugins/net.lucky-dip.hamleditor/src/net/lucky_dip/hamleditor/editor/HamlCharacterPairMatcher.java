package net.lucky_dip.hamleditor.editor;

import net.lucky_dip.hamleditor.HamlesqueBlock;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

public class HamlCharacterPairMatcher implements ICharacterPairMatcher {
	private int anchor = 0;

	public void clear() {
	}

	public void dispose() {
	}

	public int getAnchor() {
		return anchor;
	}

	public IRegion match(IDocument doc, int cursorPosition) {
		this.anchor = cursorPosition;
		Region res = null;

		try {
			String text = doc.get();
			String[] lines = text.split("\n");

			int lineNumber = doc.getLineOfOffset(cursorPosition);
			int lineOffset = doc.getLineOffset(lineNumber);
			String line = doc.get(lineOffset, doc.getLineLength(lineNumber));

			int lineSpaces = HamlesqueBlock.countIndentSpaces(line);
			int spaces = Math.min(cursorPosition - lineOffset, lineSpaces);			
			
			for (int i = lineNumber - 1; i >= 0; i--) {
				if (lines[i].trim().length() > 0) {
					int currentSpaces = HamlesqueBlock.countIndentSpaces(lines[i]);
					if (currentSpaces == (spaces - 2)) {
						res = new Region(doc.getLineOffset(i) + currentSpaces, 1);
						break;
					}
				}
			}
		}
		catch (BadLocationException e) {
		}

		return res;
	}
//
//	private int getElementLength(String line) {
//		int res = 0;
//		StringTokenizer tok = new StringTokenizer(line.trim(), "\r\n .,:;{");
//		if (tok.hasMoreTokens()) {
//			res = tok.nextToken().length();
//		}
//		return res;
//	}
}
