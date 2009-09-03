package net.lucky_dip.sasseditor.editor;

import java.util.Arrays;
import java.util.HashMap;

import net.lucky_dip.hamleditor.HamlesqueBlock;

public class SassBlock extends HamlesqueBlock {
	/**
	 * Construct a new hamlesque block object to hold the given text
	 */
	public SassBlock(String text) {
		super(text);
	}

	/**
	 * Construct a new hamlesque block object to hold the given lines
	 */
	public SassBlock(String[] lines) {
		super(lines);
	}

	/**
	 * Calculates all hamlesque blocks contained in this block, starting at the
	 * given line index.
	 */
	public HamlesqueBlock[] getSubBlocks(int startLine) {
		HamlesqueBlock[] blocks = super.getSubBlocks(startLine);

		HamlesqueBlock[] res = new HamlesqueBlock[blocks.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = new SassBlock(blocks[i].getLines());
		}

		return res;
	}

	/**
	 * Sorts this sass block into alphabetical order
	 */
	public void sort() {
		HamlesqueBlock[] blocks = getSubBlocks();

		HashMap blockMap = new HashMap();
		String[] firstLines = new String[blocks.length];

		for (int i = 0; i < blocks.length; i++) {
			firstLines[i] = blocks[i].getLines()[0].trim();
			blockMap.put(firstLines[i], blocks[i]);
		}

		Arrays.sort(firstLines);

		HamlesqueBlock[] sorted = new HamlesqueBlock[blocks.length];
		for (int i = 0; i < sorted.length; i++) {
			// this is dodgy as. remove the first line for a sec and
			// then we'll add it in at the end :-(
			SassBlock block = (SassBlock) blockMap.get(firstLines[i]);

			String firstLine = block.removeLine(0);
			block.sort();
			block.addLine(0, firstLine);

			sorted[i] = block;
		}

		StringBuffer organised = new StringBuffer();
		for (int i = 0; i < sorted.length; i++) {
			organised.append(sorted[i].toString());

			if (i < sorted.length - 1) {
				organised.append("\r\n");
			}
		}

		lines = textToLines(organised.toString());
	}
}
