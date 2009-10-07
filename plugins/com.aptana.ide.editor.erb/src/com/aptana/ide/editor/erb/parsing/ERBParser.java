/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL youelect, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
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
package com.aptana.ide.editor.erb.parsing;

import java.text.ParseException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.text.IRubyColorConstants;
import org.rubypeople.rdt.internal.ui.text.IRubyPartitions;
import org.rubypeople.rdt.internal.ui.text.MergingPartitionScanner;
import org.rubypeople.rdt.internal.ui.text.ruby.RubyTokenScanner;
import org.rubypeople.rdt.ui.text.IColorManager;

import com.aptana.ide.editor.erb.lexing.ERBTokenList;
import com.aptana.ide.editor.erb.lexing.RubyTokenTypes;
import com.aptana.ide.editor.html.lexing.HTMLTokenTypes;
import com.aptana.ide.editors.unified.LanguageRegistry;
import com.aptana.ide.editors.unified.parsing.UnifiedParser;
import com.aptana.ide.lexer.ILexer;
import com.aptana.ide.lexer.ITokenList;
import com.aptana.ide.lexer.Lexeme;
import com.aptana.ide.lexer.LexemeList;
import com.aptana.ide.lexer.LexerException;
import com.aptana.ide.lexer.TokenList;
import com.aptana.ide.parsing.IParseState;
import com.aptana.ide.parsing.ParserInitializationException;
import com.aptana.ide.parsing.nodes.IParseNode;

/**
 * @author Kevin Lindsey
 */
public class ERBParser extends UnifiedParser
{

	private LexemeList lexemeList;
	private int index;

	/**
	 * ERBParser
	 * 
	 * @throws ParserInitializationException
	 */
	public ERBParser() throws ParserInitializationException
	{
		super(ERBMimeType.MimeType);
	}

	/**
	 * @see com.aptana.ide.parsing.AbstractParser#createLanguageTokenList()
	 */
	protected ITokenList createLanguageTokenList()
	{
		return new ERBTokenList();
	}

	/**
	 * @see com.aptana.ide.parsing.AbstractParser#createParseState(com.aptana.ide.parsing.IParseState)
	 */
	public IParseState createParseState(IParseState parent)
	{
		IParseState result;

		if (parent == null)
		{
			result = new ERBParseState();
		}
		else
		{
			result = new ERBParseState(parent);
		}

		return result;
	}

	/**
	 * @see com.aptana.ide.parsing.AbstractParser#initializeLexer()
	 */
	public void initializeLexer() throws LexerException
	{
		ILexer lexer = this.getLexer();
		String language = this.getLanguage();

		lexer.setIgnoreSet(language, new int[] { HTMLTokenTypes.WHITESPACE }); //$NON-NLS-1$
		lexer.setLanguageAndGroup(language, "default"); //$NON-NLS-1$
	}

	/**
	 * @see com.aptana.ide.parsing.AbstractParser#parseAll(com.aptana.ide.parsing.nodes.IParseNode)
	 */
	public void parseAll(IParseNode parentNode) throws ParseException, LexerException
	{
		ILexer lexer = this.getLexer();
		lexer.setLanguageAndGroup(this.getLanguage(), "default"); //$NON-NLS-1$

		String source = lexer.getSource();
		source = source.substring(lexer.getCurrentOffset(), lexer.getEOFOffset());
		lexemeList = new LexemeList();
		index = 0;

		if (source.startsWith("#"))
		{
			TokenList tl = LanguageRegistry.getTokenList(ERBMimeType.MimeType);
			Lexeme lexeme = new Lexeme(tl.get(RubyTokenTypes.SINGLE_LINE_COMMENT), source, lexer.getCurrentOffset());
			lexemeList.add(lexeme);
		}
		else
		{
			// Break into partitions, then break each partition into tokens
			try
			{
				int start = lexer.getCurrentOffset();
				IDocument document = new Document(source);
				MergingPartitionScanner scanner = new MergingPartitionScanner();
				scanner.setRange(document, 0, source.length());
				IToken token1 = scanner.nextToken();
				while (!token1.isEOF())
				{
					int scannerOffset = scanner.getTokenOffset();
					int scannerLength = scanner.getTokenLength();
					String partitionName = (String) token1.getData();
					scanPartition(document, partitionName, scannerOffset, scannerLength, start);
					token1 = scanner.nextToken();
				}
			}
			catch (BadLocationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.advance();

		while (this.isEOS() == false)
		{
			this.advance();
		}
	}

	private void scanPartition(IDocument document, String partitionName, int startOffset, int length,
			int offsetToAddToTokens) throws BadLocationException
	{
		TokenList tl = LanguageRegistry.getTokenList(ERBMimeType.MimeType);
		ITokenScanner scanner2 = getScanner(partitionName);
		scanner2.setRange(document, startOffset, length);
		IToken token = scanner2.nextToken();
		while (!token.isEOF())
		{
			int scannerOffset = scanner2.getTokenOffset();
			int scannerLength = scanner2.getTokenLength();
			Integer integer = (Integer) token.getData();
			int jRubyType = integer.intValue();
			int ourType = RubyTokenTypes.getOurTokenType(jRubyType);
			if (ourType > -1)
			{
				Lexeme lexeme = new Lexeme(tl.get(ourType), document.get(scannerOffset, scannerLength), scannerOffset
						+ offsetToAddToTokens);
				lexemeList.add(lexeme);
			}
			// else
			// {
			// RubyPlugin.log("No token type for JRuby token " + jRubyType);
			// }
			token = scanner2.nextToken();
		}
	}

	private ITokenScanner getScanner(String partitionName)
	{
		if (partitionName.equals(IRubyPartitions.RUBY_DEFAULT))
		{
			return new RubyTokenScanner();
		}
		IColorManager colorManager = RubyPlugin.getDefault().getRubyTextTools().getColorManager();
		IPreferenceStore store = RubyPlugin.getDefault().getPreferenceStore();
		if (partitionName.equals(IRubyPartitions.RUBY_MULTI_LINE_COMMENT))
		{
			return new AptanaRubyCommentScanner(colorManager, store, IRubyColorConstants.RUBY_MULTI_LINE_COMMENT);
		}
		if (partitionName.equals(IRubyPartitions.RUBY_SINGLE_LINE_COMMENT))
		{
			return new AptanaRubyCommentScanner(colorManager, store, IRubyColorConstants.RUBY_SINGLE_LINE_COMMENT);
		}
		if (partitionName.equals(IRubyPartitions.RUBY_COMMAND))
		{
			return new AptanaSingleTokenRubyScanner(colorManager, store, IRubyColorConstants.RUBY_COMMAND);
		}
		if (partitionName.equals(IRubyPartitions.RUBY_STRING))
		{
			return new AptanaSingleTokenRubyScanner(colorManager, store, IRubyColorConstants.RUBY_STRING);
		}
		if (partitionName.equals(IRubyPartitions.RUBY_REGULAR_EXPRESSION))
		{
			return new AptanaSingleTokenRubyScanner(colorManager, store, IRubyColorConstants.RUBY_REGEXP);
		}
		return null;
	}

	@Override
	protected Lexeme getNextLexemeInLanguage() throws LexerException
	{
		if (index >= lexemeList.size())
		{
			getLexer().setCurrentOffset(getLexer().getEOFOffset()); // force it to report true to isEOS()
			return null;
		}
		return lexemeList.get(index++);
	}
}
