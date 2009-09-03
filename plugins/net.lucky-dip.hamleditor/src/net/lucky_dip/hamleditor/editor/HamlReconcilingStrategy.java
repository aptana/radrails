package net.lucky_dip.hamleditor.editor;

import java.util.ArrayList;

import net.lucky_dip.hamleditor.HamlesqueBlock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;

public class HamlReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private HamlesqueEditor editor;
	private IDocument document;
	private int currentLine;
	private String[] lines;

	protected final ArrayList positions = new ArrayList();

	public HamlReconcilingStrategy(HamlesqueEditor editor) {
		this.editor = editor;
	}

	public void initialReconcile() {
		positions.clear();
		currentLine = 0;

		String template = document.get();
		lines = template.split("\\n");

		// we can't have a region starting on the last line
		Position p;
		while (currentLine < lines.length) {
			p = getNextPosition();
			if (p != null) {
				positions.add(p);
			}
		}

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editor.updateFoldingStructure(positions);
			}

		});
	}

	private Position getNextPosition() {
		Position res = null;
		try {
			String line = lines[currentLine];

			// regions can't start on an empty line
			if (line.trim().length() > 0) {
				int start = document.getLineOffset(currentLine);
				int length = getNextPositionLength();

				if (start + length >= document.getLength()) {
					length = document.getLength() - start;
				}

				if (length > 0) {
					res = new Position(start, length);
				}
			}
			else {
				currentLine++;
			}
		}
		catch (BadLocationException e) {
			res = null;
		}

		return res;
	}

	/**
	 * Returns the length in characters from the start of currentLine to when
	 * the region begun here should end.
	 * 
	 * Returns 0 if no region found.
	 * 
	 * @throws BadLocationException
	 */
	private int getNextPositionLength() throws BadLocationException {
		int res = 0;
		int lastLine = getLastLineInNextRegion();

		if (lastLine > currentLine && lastLine < lines.length) {
			for (int i = currentLine; i <= lastLine; i++) {
				String trimmedLine = lines[i].trim();

				if (trimmedLine.length() > 0 || i < lastLine) {
					res += document.getLineLength(i);
				}
			}
		}

		currentLine = lastLine + 1;

		return res;
	}

	/**
	 * Figures out which lines should be included in the next region.
	 * 
	 * The index of the last line in the next region is returned.
	 */
	private int getLastLineInNextRegion() {
		int res = currentLine;

		int startOffset = HamlesqueBlock.countIndentSpaces(lines[currentLine]);

		for (int i = currentLine + 1; i < lines.length; i++) {
			int currentOffset = HamlesqueBlock.countIndentSpaces(lines[i]);
			String trimmedLine = lines[i].trim();

			if (currentOffset == startOffset && trimmedLine.length() > 0) {
				// region over
				break;
			}
			else if (currentOffset > startOffset || lines[i].trim().length() == 0) {
				res++;
			}
		}

		if (res > currentLine && res < lines.length) {
			// trim all trailing whitespace
			String lastLine;
			
			while ((lastLine = lines[res]).trim().length() == 0) {
				res--;
			}
		}

		return res;
	}

	public void setProgressMonitor(IProgressMonitor monitor) {
	}

	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		initialReconcile();
	}

	public void reconcile(IRegion partition) {
		initialReconcile();
	}

	public void setDocument(IDocument document) {
		this.document = document;
	}

}
