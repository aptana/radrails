package net.lucky_dip.sasseditor.editor.scanners;

import java.util.ArrayList;
import java.util.List;

import net.lucky_dip.sasseditor.editor.SassWordDetector;
import net.lucky_dip.sasseditor.editor.rules.SassAttributeRule;
import net.lucky_dip.sasseditor.editor.rules.SassTagRule;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

public class SassPartitionScanner extends RuleBasedPartitionScanner {
	private static SassPartitionScanner instance;

	public final static String SASS_CONSTANT = "__sass_constant";

	public final static String SASS_ATTRIBUTE = "__sass_attribute";

	public final static String SASS_CLASS = "__sass_class";

	public final static String SASS_ID = "__sass_id";

	public final static String SASS_TAG = "__sass_tag";

	public final static String[] SASS_PARTITION_TYPES = new String[] { SASS_CONSTANT,
			SASS_ATTRIBUTE, SASS_CLASS, SASS_ID, SASS_TAG };

	public static boolean IsSassPartitionType(String contentType) {
		boolean res = false;

		for (int i = 0; !res && i < SASS_PARTITION_TYPES.length; i++) {
			res = contentType.equals(SASS_PARTITION_TYPES[i]);
		}

		return res;
	}

	/**
	 * @return the singleton instance of the scanner
	 */
	public static SassPartitionScanner getInstance() {
		if (instance == null) {
			instance = new SassPartitionScanner();
		}
		return instance;
	}

	/**
	 * Constructor.
	 */
	private SassPartitionScanner() {
		super();

		IToken constantToken = new Token(SASS_CONSTANT);
		IToken attributeToken = new Token(SASS_ATTRIBUTE);
		IToken classToken = new Token(SASS_CLASS);
		IToken idToken = new Token(SASS_ID);
		IToken tagToken = new Token(SASS_TAG);

		List rules = new ArrayList();

		rules.add(new WordPatternRule(new SassWordDetector(), "#", "", classToken));
		rules.add(new WordPatternRule(new SassWordDetector(), "!", "", constantToken));
		rules.add(new WordPatternRule(new SassWordDetector(), ".", "", idToken));
		rules.add(new WordPatternRule(new SassWordDetector(), ":", "", attributeToken));
		rules.add(new SassAttributeRule(attributeToken));
		rules.add(new SassTagRule(tagToken));

		IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}