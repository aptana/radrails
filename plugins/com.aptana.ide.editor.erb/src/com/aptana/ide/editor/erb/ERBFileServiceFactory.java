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
package com.aptana.ide.editor.erb;

import com.aptana.ide.editor.erb.parsing.ERBMimeType;
import com.aptana.ide.editor.erb.parsing.ExtendedHTMLParser;
import com.aptana.ide.editor.html.HTMLFileServiceFactory;
import com.aptana.ide.editors.unified.BaseFileLanguageService;
import com.aptana.ide.editors.unified.FileService;
import com.aptana.ide.editors.unified.LanguageRegistry;
import com.aptana.ide.editors.unified.ParentOffsetMapper;
import com.aptana.ide.parsing.IParseState;
import com.aptana.ide.parsing.IParser;
import com.aptana.ide.parsing.ParserInitializationException;

/**
 * @author Kevin Lindsey
 *
 */
public class ERBFileServiceFactory extends HTMLFileServiceFactory
{
	private static final String LANGUAGE = "text/html+rb";
	
	private static HTMLFileServiceFactory instance;
	
	/**
	 * ERBFileServiceFactory
	 */
	public ERBFileServiceFactory()
	{
		super();
	}
	
	/**
	 * @see com.aptana.ide.editor.html.HTMLFileServiceFactory#createChildFileServices(com.aptana.ide.parsing.IParser, com.aptana.ide.editors.unified.FileService, com.aptana.ide.parsing.IParseState, com.aptana.ide.editors.unified.ParentOffsetMapper)
	 */
	protected void createChildFileServices(IParser parser, FileService fileService, IParseState parseState, ParentOffsetMapper mapper)
	{
		super.createChildFileServices(parser, fileService, parseState, mapper);
		
		IParser erbParser = parser.getParserForMimeType(ERBMimeType.MimeType);
		BaseFileLanguageService phpService = new BaseFileLanguageService(fileService, parseState, erbParser, mapper);
		fileService.addLanguageService(ERBMimeType.MimeType, phpService);
	}

	/**
	 * @see com.aptana.ide.editor.html.HTMLFileServiceFactory#createParser()
	 */
	protected IParser createParser()
	{
		IParser result = null;
		
		try
		{
			if (LanguageRegistry.hasParser(LANGUAGE))
			{
				result = (IParser) LanguageRegistry.getParser(LANGUAGE);
			}
			else
			{
				// create new instance
				result = new ExtendedHTMLParser();
				
				// store in language registry cache for all future uses
				LanguageRegistry.registerParser(LANGUAGE, result);
			}
		}
		catch (ParserInitializationException e)
		{
			// fail silently
		}
		
		return result;
	}
	
	/**
	 * getInstance
	 * 
	 * @return HTMLFileServiceFactory
	 */
	public static HTMLFileServiceFactory getInstance()
	{
		if (instance == null)
		{
			instance = new ERBFileServiceFactory();
		}

		return instance;
	}
}
