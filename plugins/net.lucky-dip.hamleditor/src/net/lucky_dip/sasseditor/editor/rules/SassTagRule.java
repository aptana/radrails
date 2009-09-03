package net.lucky_dip.sasseditor.editor.rules;

import net.lucky_dip.hamleditor.HTMLCSSKeywords;
import net.lucky_dip.sasseditor.editor.SassWordDetector;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SassTagRule implements IPredicateRule {

	private IToken token;
	private SassWordDetector wordDetector;

	public SassTagRule(IToken token) {
		this.token = token;
		this.wordDetector = new SassWordDetector();
	}

	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		return evaluate(scanner);
	}

	public IToken getSuccessToken() {
		return token;
	}

	/**
	 * A tag is a html tag on a line by itself
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		int read = 0;
		int c;
		String str = "";
		IToken res = Token.UNDEFINED;

		boolean startOfRow = scanner.getColumn() == 0;
		char last = 0;
		if (!startOfRow) {
			scanner.unread();
			last = (char) scanner.read();
		}

		while ((c = scanner.read()) != -1) {
			read++;
			if (c != 10 && c != 13) {
				str += (char) c;
			}
			else {
				break;
			}
		}

		if ((startOfRow || !wordDetector.isWordPart(last)) && HTMLCSSKeywords.isHtmlTag(str.trim())) {
			res = token;
		}
		else {
			for (int i = 0; i < read; i++) {
				scanner.unread();
			}
		}

		return res;
	}

}
