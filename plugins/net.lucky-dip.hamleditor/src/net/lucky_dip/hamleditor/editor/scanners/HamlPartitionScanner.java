package net.lucky_dip.hamleditor.editor.scanners;

import java.util.ArrayList;
import java.util.List;

import net.lucky_dip.hamleditor.editor.HamlWordDetector;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

public class HamlPartitionScanner extends RuleBasedPartitionScanner {
	private static HamlPartitionScanner instance;

	public final static String HAML_DOCTYPE = "__haml_doctype";

	public final static String HAML_COMMENT = "__haml_comment";

	public final static String HAML_RUBY = "__haml_ruby";

	public final static String HAML_CLASS = "__haml_class";

	public final static String HAML_ID = "__haml_id";

	public final static String HAML_ELEMENT = "__haml_element";

	public final static String[] HAML_PARTITION_TYPES = new String[] { HAML_DOCTYPE, HAML_RUBY,
			HAML_CLASS, HAML_ID, HAML_COMMENT, HAML_ELEMENT };

	public static boolean IsHAMLPartitionType(String contentType) {
		boolean res = false;

		for (int i = 0; !res && i < HAML_PARTITION_TYPES.length; i++) {
			res = contentType.equals(HAML_PARTITION_TYPES[i]);
		}

		return res;
	}

	/**
	 * @return the singleton instance of the scanner
	 */
	public static HamlPartitionScanner getInstance() {
		if (instance == null) {
			instance = new HamlPartitionScanner();
		}
		return instance;
	}

	/**
	 * Constructor.
	 */
	private HamlPartitionScanner() {
		super();

		IToken doctype = new Token(HAML_DOCTYPE);
		IToken comment = new Token(HAML_COMMENT);
		IToken element = new Token(HAML_ELEMENT);
		IToken classToken = new Token(HAML_CLASS);
		IToken idToken = new Token(HAML_ID);
		IToken rubyToken = new Token(HAML_RUBY);

		List rules = new ArrayList();
		rules.add(new WordPatternRule(new HamlWordDetector(), "%", "", element));
		rules.add(new SingleLineRule("!!!", "", doctype, (char) 0, false));
		rules.add(new MultiLineRule("<!--", "-->", comment, (char) 0, true));
		rules.add(new SingleLineRule("/", null, comment));
		rules.add(new WordPatternRule(new HamlWordDetector(), ".", "", classToken));
		rules.add(new WordPatternRule(new HamlWordDetector(), "#", "", idToken));

		rules.add(new SingleLineRule("=", "", rubyToken, (char) 0, true, false));
		rules.add(new SingleLineRule("~", "", rubyToken, (char) 0, true, false));
		rules.add(new SingleLineRule("[", "]", rubyToken, (char) 0, true, false));
		rules.add(new SingleLineRule("-", "", rubyToken, (char) 0, true, false));
		rules.add(new SingleLineRule("{", "}", rubyToken, (char) 0, true));

		IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
