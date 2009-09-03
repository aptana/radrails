package net.lucky_dip.sasseditor.editor.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SassAttributeRule implements IPredicateRule {

	private IToken token;

	public SassAttributeRule(IToken token) {
		this.token = token;
	}

	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		return evaluate(scanner);
	}

	public IToken getSuccessToken() {
		return token;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int read = 0;
		int c;
		String str = "";
		IToken res = Token.UNDEFINED;

		while ((c = scanner.read()) != -1) {
			read++;
			// css attribute should break on spaces or newlines
			if (c != 10 && c != 13 && c != 32) {
				str += (char) c;
			} else {
				break;
			}
		}

		String elem = str.trim();
		if (elem.length() > 0 && elem.endsWith(":")) {
				res = token;
		}

		if (res == Token.UNDEFINED) {
			for (int i = 0; i < read; i++) {
				scanner.unread();
			}
		}

		return res;
	}
}
