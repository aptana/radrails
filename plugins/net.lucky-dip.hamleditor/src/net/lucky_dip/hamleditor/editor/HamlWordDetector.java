package net.lucky_dip.hamleditor.editor;

import org.eclipse.jface.text.rules.IWordDetector;

public class HamlWordDetector implements IWordDetector {

	public boolean isWordPart(char c) {
		String s = String.valueOf(c);
		return s.matches("[\\w-]");
	}

	public boolean isWordStart(char c) {
		return String.valueOf(c).matches("\\w");
	}

}
