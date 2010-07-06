/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain Eclipse Public Licensed code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ide.editor.erb;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

import com.aptana.ide.editor.erb.parsing.ERBMimeType;
import com.aptana.ide.editor.html.lexing.HTMLTokenTypes;
import com.aptana.ide.editor.html.parsing.HTMLMimeType;
import com.aptana.ide.editors.unified.LanguageRegistry;
import com.aptana.ide.editors.unified.colorizer.IColorizerHandler;
import com.aptana.ide.lexer.Lexeme;
import com.aptana.ide.lexer.TokenCategories;
import com.aptana.ide.parsing.IParseState;

/**
 * @author Kevin Sawicki (ksawicki@aptana.com)
 */
public class ERBColorizer implements IColorizerHandler
{

	/**
	 * @see com.aptana.ide.editors.unified.colorizer.IColorizerHandler#getBackground(com.aptana.ide.parsing.IParseState,
	 *      com.aptana.ide.lexer.Lexeme)
	 */
	public Color getBackground(IParseState parseState, Lexeme lexeme)
	{
		if (lexeme.getToken().getTypeIndex() == HTMLTokenTypes.TEXT
				&& lexeme.getToken().getCategoryIndex() == TokenCategories.LITERAL)
		{
			if (lexeme.getText().matches("\\s*"))
			{
				int index = parseState.getLexemeList().getLexemeIndex(lexeme);
				if (index > -1 && index + 1 < parseState.getLexemeList().size())
				{
					lexeme = parseState.getLexemeList().get(index + 1);
					return getERBColor(parseState, lexeme);
				}
			}
		}
		else
		{
			return getERBColor(parseState, lexeme);
		}
		return null;
	}

	private Color getERBColor(IParseState parseState, Lexeme lexeme)
	{
		if (lexeme.getToken().getTypeIndex() == HTMLTokenTypes.PERCENT_OPEN
				&& lexeme.getCategoryIndex() == TokenCategories.PUNCTUATOR)
		{
			int index = parseState.getLexemeList().getLexemeIndex(lexeme);
			if (index > -1 && index + 1 < parseState.getLexemeList().size())
			{
				Lexeme firstRuby = parseState.getLexemeList().get(index + 1);
				if (firstRuby != null && ERBMimeType.MimeType.equals(firstRuby.getLanguage()))
				{
					return LanguageRegistry.getLanguageColorizer(ERBMimeType.MimeType).getBackground();
				}
			}
		}
		else if (lexeme.getToken().getTypeIndex() == HTMLTokenTypes.PERCENT_GREATER
				&& lexeme.getCategoryIndex() == TokenCategories.PUNCTUATOR)
		{
			int index = parseState.getLexemeList().getLexemeIndex(lexeme);
			if (index - 1 > -1)
			{
				Lexeme firstRuby = parseState.getLexemeList().get(index - 1);
				if (firstRuby != null && ERBMimeType.MimeType.equals(firstRuby.getLanguage()))
				{
					return LanguageRegistry.getLanguageColorizer(ERBMimeType.MimeType).getBackground();
				}
			}
		}
		return null;
	}

	/**
	 * @see com.aptana.ide.editors.unified.colorizer.IColorizerHandler#getEmptyLineColor(com.aptana.ide.parsing.IParseState,
	 *      int)
	 */
	public Color getEmptyLineColor(IParseState parseState, int offset)
	{
		return null;
	}

	/**
	 * @see com.aptana.ide.editors.unified.colorizer.IColorizerHandler#getLanguage()
	 */
	public String getLanguage()
	{
		return HTMLMimeType.MimeType;
	}

	/**
	 * @see com.aptana.ide.editors.unified.colorizer.IColorizerHandler#getStyleRange(com.aptana.ide.parsing.IParseState,
	 *      com.aptana.ide.lexer.Lexeme)
	 */
	public StyleRange getStyleRange(IParseState parseState, Lexeme lexeme)
	{
		return null;
	}

}
