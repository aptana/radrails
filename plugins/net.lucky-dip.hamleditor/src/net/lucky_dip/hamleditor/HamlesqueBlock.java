package net.lucky_dip.hamleditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class to hold haml or sass text.
 * 
 * It provides a few helpful functions to sort and manipulate blocks too.
 * 
 * @author brad
 */
public class HamlesqueBlock {

	/**
	 * Counts the number of spaces at the beginning of the given line
	 */
	public static int countIndentSpaces(String line) {
		int res = 0;

		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) != ' ') {
				break;
			} else {
				res++;
			}
		}

		return res;
	}

	protected List<String> lines;

	/**
	 * Construct a new hamlesque block object to hold the given text
	 */
	public HamlesqueBlock(String text) {
		lines = textToLines(text);
	}

	/**
	 * Construct a new hamlesque block object to hold the given lines
	 */
	public HamlesqueBlock(String[] lines) {
		this.lines = Arrays.asList(lines);
	}

	/**
	 * Calculates all hamlesque blocks contained in this block
	 */
	public HamlesqueBlock[] getSubBlocks() {
		return getSubBlocks(0);
	}

	/**
	 * Returns the lines in this block
	 */
	public String[] getLines() {
		return lines.toArray(new String[lines.size()]);
	}

	/**
	 * Calculates all hamlesque blocks contained in this block, starting at the
	 * given line index.
	 */
	public HamlesqueBlock[] getSubBlocks(int startLine) {
		List<HamlesqueBlock> res = new ArrayList<HamlesqueBlock>();

		while (startLine < lines.size()) {
			if (isBlankLine(startLine)) {
				startLine++;
				continue;
			} else {
				HamlesqueBlock nextBlock = getBlockStartingAt(startLine);
				startLine += nextBlock.getLines().length;
				res.add(nextBlock);
			}
		}

		return (HamlesqueBlock[]) res.toArray(new HamlesqueBlock[0]);
	}

	/**
	 * Calculates and returns the block starting on the given line.
	 */
	private HamlesqueBlock getBlockStartingAt(int startLine) {
		int initialOffset = countIndentSpaces(lines.get(startLine));
		int endLine;

		for (endLine = startLine + 1; endLine < lines.size(); endLine++) {
			if (!isBlankLine(endLine)) {
				int currentOffset = countIndentSpaces(lines.get(endLine));
				if (currentOffset <= initialOffset) {
					endLine--;
					break;
				}
			}
		}

		// if (endLine < lines.length - 1) {
		// endLine--;
		// }

		// if there's a bunch of blank lines at the end of
		// the block, we need to trim them out.
		for (int i = endLine - 1; endLine > startLine; i--) {
			if (!isBlankLine(i)) {
				endLine = i;
				break;
			}
		}

		int length = endLine + 1 - startLine;
		String[] linesInBlock = new String[length];
		int x = 0;
		for (int i = startLine; i < endLine + 1; i++) {
			linesInBlock[x++] = lines.get(i);
		}
		return new HamlesqueBlock(linesInBlock);
	}

	/**
	 * Returns true if the line at the given index only contains whitespace.
	 * 
	 * Returns false otherwise.
	 */
	private boolean isBlankLine(int index) {
		if (index < 0 || index >= lines.size()) return false;
		String line = lines.get(index);
		return line.trim().length() == 0;

	}

	/**
	 * Splits the given text into an array of lines
	 */
	protected List<String> textToLines(String text) {
		return Arrays.asList(text.split("\n"));
	}

	/**
	 * Adds a line to this block at the given index
	 */
	protected void addLine(int i, String line) {
		lines.add(i, line);
	}

	/**
	 * Removes the line at the given index from this block
	 */
	protected String removeLine(int i) {
		return lines.remove(i);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (String line : lines) {
			sb.append(line);
			sb.append("\n");
		}
		if (!lines.isEmpty()) sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}
}
