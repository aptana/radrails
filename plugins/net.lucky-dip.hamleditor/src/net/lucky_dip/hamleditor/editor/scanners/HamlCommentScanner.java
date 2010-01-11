package net.lucky_dip.hamleditor.editor.scanners;

import net.lucky_dip.hamleditor.editor.IColorManager;
import net.lucky_dip.hamleditor.editor.IHamlEditorColorConstants;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class HamlCommentScanner extends RuleBasedScanner
{

	public HamlCommentScanner(IColorManager manager)
	{
		IToken comment = new Token(new TextAttribute(manager.getColor(IHamlEditorColorConstants.HAML_COMMENT)));
		IRule[] rules = new IRule[3];
		rules[0] = new SingleLineRule("/", null, comment, (char) 0, true);
		rules[1] = new MultiLineRule("<!--", "-->", comment, (char) 0, true);
		// FIXME Need to handle nested content and color that properly!
		rules[2] = new SingleLineRule("-#", null, comment, (char) 0, true);

		setRules(rules);
	}
}
